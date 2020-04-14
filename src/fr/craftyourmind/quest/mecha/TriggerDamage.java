package fr.craftyourmind.quest.mecha;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import fr.craftyourmind.manager.CYMChecker.ICheckerDamage;
import fr.craftyourmind.manager.checker.Damage;
import fr.craftyourmind.manager.command.AbsCYMCommand;
import fr.craftyourmind.manager.command.ICYMCommandData;
import fr.craftyourmind.quest.mecha.MechaControler.Mode;

public class TriggerDamage extends Mechanism{

	public static final int CAUSE = 0;
	public static final int MODIFIER = 1;
	public static final int PROJECTILE = 2;
	
	private boolean recieved = true;
	private boolean given = false;
	private boolean launchOn = false;
	private List<DamageCause> causes = new ArrayList<DamageCause>();
	private List<String> projectiles = new ArrayList<String>();
	private List<Class> projectilesClass = new ArrayList<Class>();
	private List<DamageModifierTri> damageModifiers = new ArrayList<DamageModifierTri>();
	
	@Override
	public ICYMCommandData newCommandData() { return new CmdDamage(); }
	@Override
	public boolean isMechaStoppable(){ return true; }
	@Override
	public int getType() { return MechaType.TRIDAMAGE; }
	@Override
	public String getParams() {
		String causeParams = causes.size()+"";
		for(DamageCause dc : causes) causeParams += DELIMITER+dc.name();
		String projParams = projectiles.size()+"";
		for(String proj : projectiles) projParams += DELIMITER+proj;
		String dmtParams = damageModifiers.size()+"";
		for(DamageModifierTri dmt : damageModifiers) dmtParams += DELIMITER+dmt.damageMod.name()+DELIMITER+dmt.initMod+DELIMITER+dmt.addMod+DELIMITER+dmt.mod;
		return 0+DELIMITER+recieved+DELIMITER+given+DELIMITER+launchOn+DELIMITER+causeParams+DELIMITER+projParams+DELIMITER+dmtParams;
	}
	@Override
	protected void loadParams(String[] params) {
		int index = 0;
		int version = Integer.valueOf(params[index++]);
		recieved = Boolean.valueOf(params[index++]);
		given = Boolean.valueOf(params[index++]);
		launchOn = Boolean.valueOf(params[index++]);
		causes.clear();
		int size = Integer.valueOf(params[index++]);
		for(int i = 0 ; i < size ; i++){
			DamageCause dc = DamageCause.valueOf(params[index++]);
			if(dc != null) causes.add(dc);
		}
		projectiles.clear();
		size = Integer.valueOf(params[index++]);
		for(int i = 0 ; i < size ; i++)
			projectiles.add(params[index++]);
		damageModifiers.clear();
		size = Integer.valueOf(params[index++]);
		for(int i = 0 ; i < size ; i++){
			DamageModifierTri dmt = new DamageModifierTri();
			dmt.damageMod = DamageModifier.valueOf(params[index++]);
			dmt.initMod = Boolean.valueOf(params[index++]);
			dmt.addMod = Boolean.valueOf(params[index++]);
			dmt.mod.load(params[index++]);
			if(dmt.damageMod != null) damageModifiers.add(dmt);
		}
		projectilesClass.clear();
		try{ for(String className : projectiles) projectilesClass.add(Class.forName("org.bukkit.entity."+className));
		}catch (ClassNotFoundException e){ e.printStackTrace(); }
	}
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateDamage(this, mc, driver); }
	// ------------------ StateDamage ------------------
	class StateDamage extends AbsMechaStateEntitySave implements ICheckerDamage{
		private boolean recieved, given;
		private Entity victim, damager;	
		private EntityDamageEvent event;
		private List<DamageModifierTri> damageModifiers = new ArrayList<DamageModifierTri>();
		private Damage damage;
		public StateDamage(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void cloneData() {
			super.cloneData();
			damageModifiers.clear();
			for(DamageModifierTri dm : TriggerDamage.this.damageModifiers) damageModifiers.add(dm.clone(this));
		}
		@Override
		public Mode getMode() { return Mode.MULTIPLE; }
		@Override
		public void start() {
			super.start();
			damage = new Damage(driver.getChecker(), this);
		}
		@Override
		public boolean check() {
			if((recieved && TriggerDamage.this.recieved) || (given && TriggerDamage.this.given)){
				if(!causes.isEmpty()){ 
					if(causes.contains(event.getCause())){
						if(event.getCause() == DamageCause.PROJECTILE){
							if(!projectiles.isEmpty()){
								boolean containsPro = false;
								Entity damager = ((EntityDamageByEntityEvent)event).getDamager();
								for(Class classProj : projectilesClass){
									if(classProj.isInstance(damager)){
										containsPro = true;
										break;
									}
								}
								if(!containsPro) return false;
							}
						}
					}else return false;
				}
				for(DamageModifierTri dmt : damageModifiers) dmt.modifier(event);
				return true;
			}
			return false;
		}
		@Override
		protected void afterCheckOK() {
			sendMessage();
			if(!getMechanism().permanent) stop();
			if(launchOn){
				if(TriggerDamage.this.recieved && damager != null){					
					IMechaDriver d = MechaDriver.newDriverGuest(getContainer(), damager);
					d.setEntitySource(getEntity());
					TriggerDamage.this.launch(d);
				}else if(TriggerDamage.this.given){
					IMechaDriver d = MechaDriver.newDriverGuest(getContainer(), victim);
					d.setEntitySource(getEntity());
					TriggerDamage.this.launch(d);
				}
			}else if(!getMechanism().single) launch();
		}
		@Override
		public void stop() { super.stop(); if(damage != null) damage.stop(); }
		@Override
		public void setEvent(EntityDamageEvent event) { this.event = event; }
		@Override
		public void setRecieved(boolean recieved) { this.recieved = recieved; }
		@Override
		public void setGiven(boolean given) { this.given = given; }
		@Override
		public void setVictim(Entity victim) { this.victim = victim; }
		@Override
		public void setDamager(Entity damager) { this.damager = damager; }
	}
	
	class DamageModifierTri{
		private DamageModifier damageMod;
		private boolean initMod = false;
		private boolean addMod = true;
		private DoubleData mod = new DoubleData();
		public void modifier(EntityDamageEvent event){
			double damage = event.getDamage(damageMod);
			if(initMod) damage = mod.get(); else damage += mod.get();
			event.setDamage(damageMod, damage);
		}
		public DamageModifierTri clone(AbsMechaStateEntity mse) {
			DamageModifierTri dm = new DamageModifierTri();
			dm.damageMod = damageMod;
			dm.initMod = initMod;
			dm.addMod = addMod;
			dm.mod.clone(mse, mod);
			return dm;
		}
	}
	// ------------------ CmdDamage ------------------
	class CmdDamage extends AbsCYMCommand{
		public CmdDamage(){ super(id); }
		@Override
		public void initChilds() { }
		@Override
		public void initActions() { addAction(new CAUSE()); addAction(new MODIFIER()); addAction(new PROJECTILE()); }
		abstract class AbsCmdDamageList extends AbsCYMCommandAction{
			private List<String> list;
			public abstract List<String> getList();
			@Override
			public void initSend(Player p) { list = getList(); }
			@Override
			public void sendWrite() throws IOException { writeListStr(list); }
			@Override
			public void receiveRead() throws IOException { }
			@Override
			public void receive(Player p) { sendCmdGui(p, this); }
		}
		class CAUSE extends AbsCmdDamageList{
			@Override
			public int getId() { return CAUSE; }
			@Override
			public AbsCYMCommandAction clone() { return new CAUSE(); }
			@Override
			public List<String> getList() {
				List<String> list = new ArrayList<String>();
				for(DamageCause dc : DamageCause.values()) list.add(dc.name());
				return list;
			}			
		}
		class MODIFIER extends AbsCmdDamageList{
			@Override
			public int getId() { return MODIFIER; }
			@Override
			public AbsCYMCommandAction clone() { return new MODIFIER(); }
			@Override
			public List<String> getList() {
				List<String> list = new ArrayList<String>();
				for(DamageModifier dm : DamageModifier.values()) list.add(dm.name());
				return list;
			}			
		}
		class PROJECTILE extends AbsCmdDamageList{
			@Override
			public int getId() { return PROJECTILE; }
			@Override
			public AbsCYMCommandAction clone() { return new PROJECTILE(); }
			@Override
			public List<String> getList() {
				List<String> list = new ArrayList<String>();
				list.add("Arrow"); list.add("Egg"); list.add("EnderPearl"); list.add("Fireball"); list.add("Fish"); list.add("FishHook");
				list.add("LargeFireball"); list.add("SmallFireball"); list.add("Snowball"); list.add("ThrownExpBottle"); list.add("ThrownPotion"); list.add("WitherSkull");
				return list;
			}			
		}
	}
}