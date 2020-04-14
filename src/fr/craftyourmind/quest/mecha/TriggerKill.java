package fr.craftyourmind.quest.mecha;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import fr.craftyourmind.manager.CYMChecker;
import fr.craftyourmind.manager.CYMChecker.ICheckerKill;
import fr.craftyourmind.manager.checker.Kill;

public class TriggerKill extends Mechanism{

	public static final int ENTITY = 0;
	public static final int PLAYER = 1;
	public static final int DIES = 2;
	
	public int type = 0;
	public StringData targetT = new StringData();
	public StringData targetName = new StringData();
	public IntegerData num = new IntegerData(1);
	public boolean notice = false;
	public boolean targetType = true; // true = target type ; false = target name
	
	public TriggerKill() {}
	@Override
	public int getType() { return MechaType.TRIKILL; }
	@Override
	public String getParams() {
		return new StringBuilder("2").append(DELIMITER).append(type).append(DELIMITER).append(targetT).append(DELIMITER).append(targetName).append(DELIMITER).append(num).append(DELIMITER)
				.append(notice).append(DELIMITER).append(targetType).toString();
	}
	@Override
	public String getParamsGUI() {
		StringBuilder param = new StringBuilder();
		for(EntityType e : EntityType.values()){
			if(e.isSpawnable() && e.isAlive()) param.append(e.getKey().toString()).append(DELIMITER);
		}
		return param.toString();
	}
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 4){
			type = Integer.valueOf(params[0]);
			targetT.load(params[1]);
			num.load(params[2]);
			notice = Boolean.valueOf(params[3]);
			sqlSave();
		}else{
			int version = Integer.valueOf(params[0]);
			if(version == 2){
				type = Integer.valueOf(params[1]);
				targetT.load(params[2]);
				targetName.load(params[3]);
				num.load(params[4]);
				notice = Boolean.valueOf(params[5]);
				targetType = Boolean.valueOf(params[6]);
				if(type == PLAYER) targetType = false;
			}
		}
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateKill(this, mc, driver); }
	// ------------------ StateKill ------------------
	class StateKill extends AbsMechaStateEntitySave implements ICheckerKill{
		
		public StringData targetT = new StringData();
		public StringData targetName = new StringData();
		public IntegerData num = new IntegerData(1);
		private int current = 0;
		private LivingEntity killer;	
		private LivingEntity victim;
		private Kill kill;
		
		public StateKill(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void cloneData() {
			super.cloneData();
			targetT.clone(this, TriggerKill.this.targetT);
			targetName.clone(this, TriggerKill.this.targetName);
			num.clone(this, TriggerKill.this.num);
		}
		@Override
		public void start() {
			super.start();
			if(kill != null) kill.stop();
			kill = CYMChecker.startKill(driver.getChecker(), this);
		}
		@Override
		public boolean check() {
			boolean kill = false;
			String notice = "KILL";
			String tt = targetT.get();
			String tn = targetName.get();
			String nameNotice = ((targetType)?tt:tn);
			if(type == ENTITY){
				if(targetType){ if(!tt.isEmpty() && victim.getType() == EntityType.valueOf(tt)) kill = true;
				}else if(victim.getCustomName() != null && victim.getCustomName().equalsIgnoreCase(tn)) kill = true;
				
			}else if (type == PLAYER){
				if(victim.getType() == EntityType.PLAYER){
					if(tn.isEmpty() && driver.getEntity() != victim) kill = true;
					else if(((HumanEntity)victim).getName().equalsIgnoreCase(tn)) kill = true;
				}
			}else if(type == DIES && driver.getEntity() == victim){
				kill = true;
				nameNotice = driver.getNameEntity();
				notice = "DIES";
			}
			killer = null;
			victim = null;
			if(kill){
				current++;
				sendNotice(new StringBuilder().append(ChatColor.GRAY).append("+++ ").append(notice).append(" ")
						.append(nameNotice).append(" : ").append(current).append("/").append(num.get()).append(" +++").toString());
				if(current >= num.get()){
					current = 0;
					return true;
				}
			}
			return false;
		}
	
		private void sendNotice(String msg){ if(notice) driver.sendMessage(msg); }
		@Override
		public void stop() {
			super.stop();
			if(kill != null) kill.stop();
		}
		@Override
		public void setKiller(LivingEntity killer) { this.killer = killer; }
		@Override
		public void setVictim(LivingEntity victim) { this.victim = victim; }
	}
}