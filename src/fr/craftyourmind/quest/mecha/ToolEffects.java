package fr.craftyourmind.quest.mecha;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import fr.craftyourmind.manager.command.AbsCYMCommand;
import fr.craftyourmind.manager.command.CmdParticle;
import fr.craftyourmind.manager.command.ICYMCommandData;
import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.QuestTools;
import fr.craftyourmind.quest.mecha.IntegerData;
import fr.craftyourmind.quest.command.CmdChunkPosition;

public class ToolEffects extends Mechanism{

	public static final int SOUND = 1;
	public static final int EFFECT = 2;
	public static final int ENTITYEFFECT = 3;
	public static final int NOTE = 4;
	public static final int PARTICLE = 5;
	public static final int FIREWORK = 6;
	public static final int OTHER = 7;
	public static final int EXPLOSION = 8;
	
	public IntegerData nbPop = new IntegerData(1);
	private IEffect effect;
	private MechaCoordRelative coordR = new MechaCoordRelative();

	@Override
	public ICYMCommandData newCommandData() { return new CmdEffects(); }
	@Override
	public int getType() { return MechaType.TOOEFFECTS; }
	@Override
	public String getParams() { return 1+DELIMITER+effect.getId()+DELIMITER+nbPop+DELIMITER+coordR.getParams()+DELIMITER+effect.getParams(); }
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		int version = Integer.valueOf(params[0]);
		if(version == 0){
			int typeEffect = Integer.valueOf(params[1]);
			nbPop.load(params[2]);
			coordR.setCoord(params[3], Integer.valueOf(params[4]), Integer.valueOf(params[5]), Integer.valueOf(params[6]), Integer.valueOf(params[7]), Boolean.valueOf(params[8]));
			boolean onPlayer = Boolean.valueOf(params[9]);
			if(onPlayer) coordR.setOnPlayer();
			effect = getEffect(typeEffect);
			effect.loadParams(10, params);
			sqlSave();
		}else if(version == 1){
			int typeEffect = Integer.valueOf(params[1]);
			nbPop.load(params[2]);
			int index = coordR.loadParams(3, params);
			effect = getEffect(typeEffect);
			effect.loadParams(index, params);
		}
	}
	
	public IEffect getEffect(int type){
		if(effect != null && effect.getId() == type) return effect;
		return newEffect(type);
	}
	public IEffect newEffect(int type){
		if(type == SOUND) return new SOUND();
		else if(type == EFFECT) return new EFFECT();
		else if(type == ENTITYEFFECT) return new ENTITYEFFECT();
		else if(type == NOTE) return new NOTE();
		else if(type == PARTICLE) return new PARTICLE();
		else if(type == FIREWORK) return new FIREWORK();
		else if(type == EXPLOSION) return new EXPLOSION();
		return new EFFECT();
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateFX(this, mc, driver); }
	// ------------------ StateFX ------------------
	class StateFX extends AbsMechaStateEntity{

		private IntegerData nbPop = new IntegerData();
		private MechaCoordRelative coordR = new MechaCoordRelative();
		private IEffectState state;
		
		public StateFX(Mechanism m, MechaControler mc, IMechaDriver driver) {
			super(m, mc, driver);
			state = effect.getFxState();
		}
		@Override
		public void cloneData() {
			super.cloneData();
			nbPop.clone(this, ToolEffects.this.nbPop);
			coordR.clone(this, ToolEffects.this.coordR);
			state.cloneData(this);
		}
		@Override
		public void start() {
			if(driver.hasEntity()){
				Location locR = coordR.getLocationRelative(driver);
				if(locR != null){
					for(int i = 0 ; i < nbPop.get() ; i++){
						Location loc = QuestTools.getLocation(locR.getWorld(), locR.getX(), locR.getY(), locR.getZ(), coordR.getRadius(), coordR.isOnGround());
						state.goPop(driver, driver.getPlayer(), driver.getEntity(), loc);				
					}
				}
			}
			launchMessage();
		}
	}
	// ------------------ SOUND ------------------
	class SOUND implements IEffect{
		private String typeSound = "";
		private FloatData volume = new FloatData(1), pitch = new FloatData();
		private boolean onlyPlayer;
		private Sound sound;
		@Override
		public int getId() { return SOUND; }
		@Override
		public String getParams() {
			return typeSound+DELIMITER+volume+DELIMITER+pitch+DELIMITER+onlyPlayer;
		}
		@Override
		public void loadParams(int index, String[] params) {
			typeSound = params[index++];
			volume.load(params[index++]);
			pitch.load(params[index++]);
			onlyPlayer = Boolean.valueOf(params[index++]);
			if(!typeSound.isEmpty()) sound = Sound.valueOf(typeSound);
			else sound = null;
		}
		@Override
		public IEffectState getFxState() {
			return new IEffectState() {
				private FloatData volume = new FloatData(1), pitch = new FloatData();
				@Override
				public void cloneData(StateFX fx) {
					volume.clone(fx, SOUND.this.volume);
					pitch.clone(fx, SOUND.this.pitch);
				}
				@Override
				public void goPop(IMechaDriver driver, Player p, Entity e, Location loc) {
					if(sound != null && loc != null){
						if(onlyPlayer){ if(p != null) p.playSound(loc, sound, volume.get(), pitch.get());
						}else loc.getWorld().playSound(loc, sound, volume.get(), pitch.get());
					}
				}
			};
		}
	}
	// ------------------ EFFECT ------------------
	class EFFECT implements IEffect{
		private String typeEffect = "";
		private IntegerData data = new IntegerData(), radius = new IntegerData();
		private boolean onlyPlayer;
		private Effect fx;
		@Override
		public int getId() { return EFFECT; }
		@Override
		public String getParams() {
			return typeEffect+DELIMITER+data+DELIMITER+radius+DELIMITER+onlyPlayer;
		}
		@Override
		public void loadParams(int index, String[] params) {
			typeEffect = params[index++];
			data.load(params[index++]);
			radius.load(params[index++]);
			onlyPlayer = Boolean.valueOf(params[index++]);
			if(!typeEffect.isEmpty()){
				for(Effect e : Effect.values()){
					if(typeEffect.equals(e.name())){
						fx = e;
						break;
					}
				}
				//fx = Effect.valueOf(typeEffect);
			}else fx = null;
		}
		@Override
		public IEffectState getFxState() {
			return new IEffectState() {
				private IntegerData data = new IntegerData(), radius = new IntegerData();
				@Override
				public void cloneData(StateFX fx) {
					data.clone(fx, EFFECT.this.data);
					radius.clone(fx, EFFECT.this.radius);
				}
				@Override
				public void goPop(IMechaDriver driver, Player p, Entity e, Location loc) {
					if(fx != null && loc != null){
						if(onlyPlayer){ if(p != null) p.playEffect(loc, fx, (int)data.get()); 
						}else{
							if(radius.get() > 0) loc.getWorld().playEffect(loc, fx, (int)data.get(), (int)radius.get());
							else loc.getWorld().playEffect(loc, fx, (int)data.get());
						}
					}
				}
			};
		}
	}
	// ------------------ ENTITYEFFECT ------------------
	class ENTITYEFFECT implements IEffect{
		private String typeEntityEffect = "";
		private EntityEffect entityfx;
		@Override
		public int getId() { return ENTITYEFFECT; }
		@Override
		public String getParams() {
			return typeEntityEffect;
		}
		@Override
		public void loadParams(int index, String[] params) {
			if(params.length != index) typeEntityEffect = params[index];
			if(!typeEntityEffect.isEmpty()) entityfx = EntityEffect.valueOf(typeEntityEffect);
			else entityfx = null;
		}
		@Override
		public IEffectState getFxState() {
			return new IEffectState() {
				@Override
				public void cloneData(StateFX fx) { }
				@Override
				public void goPop(IMechaDriver driver, Player p, Entity e, Location loc) {
					if(entityfx != null) e.playEffect(entityfx);
				}
			};
		}
	}
	// ------------------ NOTE ------------------
	class NOTE implements IEffect{
		@Override
		public int getId() { return NOTE; }
		@Override
		public String getParams() {
			return "";
		}
		@Override
		public void loadParams(int index, String[] params) {

		}
		@Override
		public IEffectState getFxState() {
			return new IEffectState() {
				@Override
				public void cloneData(StateFX fx) {
					
				}
				@Override
				public void goPop(IMechaDriver driver, Player p, Entity e, Location loc) {
					
				}
			};
		}
	}
	// ------------------ PARTICLE ------------------
	public class PARTICLE implements IEffect{
		private String typeParticle = "";
		private DoubleData velX = new DoubleData(), velY = new DoubleData(), velZ = new DoubleData();
		private boolean cloud, onlyPlayer = true, useVelocity;
		private IntegerData nbParticule = new IntegerData(20);
		private FloatData width = new FloatData(1), height = new FloatData(1), length = new FloatData(1);
		private MechaVelocity velocity = new MechaVelocity();
		private MechaDirectionRelative directR = new MechaDirectionRelative();
		private IntegerData idMat = new IntegerData(1);
		private IntegerData dataMat = new IntegerData();
		@Override
		public int getId() { return PARTICLE; }
		@Override
		public String getParams() {
			return new StringBuilder("0").append(DELIMITER).append(typeParticle).append(DELIMITER).append(velX).append(DELIMITER).append(velY).append(DELIMITER).append(velZ).append(DELIMITER)
					.append(cloud).append(DELIMITER).append(useVelocity).append(DELIMITER).append(nbParticule).append(DELIMITER).append(width).append(DELIMITER).append(height).append(DELIMITER).append(length).append(DELIMITER)
					.append(onlyPlayer).append(DELIMITER).append(velocity.getParams()).append(DELIMITER).append(directR.getParams()).append(DELIMITER)
					.append(idMat).append(DELIMITER).append(dataMat).toString();
		}
		@Override
		public void loadParams(int index, String[] params) {
			if(params.length <= index + 8){
				typeParticle = params[index++];
				velX.load(params[index++]);
				velY.load(params[index++]);
				velZ.load(params[index++]);
				cloud = Boolean.valueOf(params[index++]);
				width.load(params[index++]);
				height.load(params[index++]);
				if(params.length > index) onlyPlayer = Boolean.valueOf(params[index++]);
				sqlSave();
			}else{
				int version = Integer.valueOf(params[index++]);
				typeParticle = params[index++];
				velX.load(params[index++]);
				velY.load(params[index++]);
				velZ.load(params[index++]);
				cloud = Boolean.valueOf(params[index++]);
				useVelocity = Boolean.valueOf(params[index++]);
				nbParticule.load(params[index++]);
				width.load(params[index++]);
				height.load(params[index++]);
				length.load(params[index++]);
				onlyPlayer = Boolean.valueOf(params[index++]);
				index = velocity.loadParams(index, params);
				index = directR.loadParams(index, params);
				idMat.load(params[index++]);
				dataMat.load(params[index++]);
			}
		}
		@Override
		public IEffectState getFxState() {
			return new IEffectState() {
				private MechaVelocity velocity = new MechaVelocity();
				private MechaDirectionRelative directR = new MechaDirectionRelative();
				private DoubleData velX = new DoubleData(), velY = new DoubleData(), velZ = new DoubleData();
				private IntegerData nbParticule = new IntegerData();
				private FloatData width = new FloatData(), height = new FloatData(), length = new FloatData();
				private IntegerData idMat = new IntegerData();
				private IntegerData dataMat = new IntegerData();
				@Override
				public void cloneData(StateFX fx) {
					velocity.clone(fx, PARTICLE.this.velocity);
					directR.clone(fx, PARTICLE.this.directR);
					velX.clone(fx, PARTICLE.this.velX);
					velY.clone(fx, PARTICLE.this.velY);
					velZ.clone(fx, PARTICLE.this.velZ);
					nbParticule.clone(fx, PARTICLE.this.nbParticule);
					width.clone(fx, PARTICLE.this.width);
					height.clone(fx, PARTICLE.this.height);
					length.clone(fx, PARTICLE.this.length);
					idMat.clone(fx, PARTICLE.this.idMat);
					dataMat.clone(fx, PARTICLE.this.dataMat);
				}
				@Override
				public void goPop(IMechaDriver driver, Player p, Entity e, Location loc) {
					if(loc != null){
						double vx = velX.get(); double vy = velY.get(); double vz = velZ.get();
						if(directR.useDirection()){
							Location locD = directR.getDirectionVelocity(driver);
							Vector v = velocity.getVelocity(locD.getYaw(), locD.getPitch());
							vx = v.getX(); vy = v.getY(); vz = v.getZ();
						}
						if(onlyPlayer){ if(p != null) CmdParticle.send(p, loc, typeParticle, vx, vy, vz, cloud, useVelocity, nbParticule.get(), width.get(), height.get(), length.get(), idMat.get(), dataMat.get()); }
						else{
							String wpop = loc.getWorld().getName();
							int xpop = loc.getBlockX(), ypop = loc.getBlockY(), zpop = loc.getBlockZ();
							for(QuestPlayer qp : CmdChunkPosition.getPlayers(loc, 20)){
								if(QuestTools.insideLoc(wpop, xpop, ypop, zpop, qp.getPlayer().getLocation(), 20))
									CmdParticle.send(qp.getPlayer(), loc, typeParticle, vx, vy, vz, cloud, useVelocity, nbParticule.get(), width.get(), height.get(), length.get(), idMat.get(), dataMat.get());
							}
						}
					}
				}
			};
		}
	}
	// ------------------ FIREWORK ------------------
	class FIREWORK implements IEffect{
		private String typeFirework = "";
		private boolean trail, flicker, onlyExplosion;
		private int power;
		private List<Color> colors = new ArrayList<Color>();
		@Override
		public int getId() { return FIREWORK; }
		@Override
		public String getParams() {
			String cs = colors.size()+"";
			for(Color c : colors) cs += DELIMITER+c.asRGB();
			return typeFirework+DELIMITER+trail+DELIMITER+flicker+DELIMITER+onlyExplosion+DELIMITER+power+DELIMITER+cs;
		}
		@Override
		public void loadParams(int index, String[] params) {
			typeFirework = params[index++];
			trail = Boolean.valueOf(params[index++]);
			flicker = Boolean.valueOf(params[index++]);
			onlyExplosion = Boolean.valueOf(params[index++]);
			power = Integer.valueOf(params[index++]);
			colors.clear();
			int size = Integer.valueOf(params[index++]);
			for(int i = 0 ; i < size ; i++) colors.add(Color.fromRGB(Integer.valueOf(params[index++])));
		}
		@Override
		public IEffectState getFxState() {
			return new IEffectState() {
				@Override
				public void cloneData(StateFX fx) { }
				@Override
				public void goPop(IMechaDriver driver, Player p, Entity e, Location loc) {
					if(!typeFirework.isEmpty() && !colors.isEmpty() && loc != null){
						Firework fireWork = loc.getWorld().spawn(loc, Firework.class);
						FireworkMeta fireWorkMeta = fireWork.getFireworkMeta();
				        FireworkEffect effect = FireworkEffect.builder().trail(trail).flicker(flicker).withColor(colors).with(FireworkEffect.Type.valueOf(typeFirework)).build();
				        fireWorkMeta.addEffects(new FireworkEffect[] { effect });
				        fireWorkMeta.setPower(power);
				        fireWork.setFireworkMeta(fireWorkMeta);
				        if(onlyExplosion)
					        CmdParticle.fireworkExplosion(loc, fireWork);
					}
				}
			};
		}
	}
	// --------------------------- EXPLOSION ---------------------------
	class EXPLOSION implements IEffect{
		public FloatData power = new FloatData();
		public boolean fire = false;
		public boolean breakBlocks = true;
		@Override
		public int getId() { return EXPLOSION; }
		@Override
		public String getParams() {
			return 1+DELIMITER+power+DELIMITER+fire+DELIMITER+breakBlocks;
		}
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			power.load(params[index++]);
			fire = Boolean.valueOf(params[index++]);
			breakBlocks = Boolean.valueOf(params[index++]);
		}
		@Override
		public IEffectState getFxState() {
			return new IEffectState() {
				public FloatData power = new FloatData();
				@Override
				public void cloneData(StateFX fx) { power.clone(fx, EXPLOSION.this.power); }
				@Override
				public void goPop(IMechaDriver driver, Player p, Entity e, Location loc) {
					if(loc != null) loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), power.get(), fire, breakBlocks);
				}
			};
		}
	}
	// ------------------ CmdEffects ------------------
	class CmdEffects extends AbsCYMCommand{
		public CmdEffects() { super(id); }
		@Override
		public void initChilds() { }
		@Override
		public void initActions() { addAction(new SOUNDDATA());  addAction(new EFFECTDATA()); addAction(new ENTITYEFFECTDATA()); addAction(new NOTEDATA()); addAction(new PARTICLEDATA()); addAction(new FIREWORKDATA()); }
		
		abstract class AbsCmdActionEffect extends AbsCYMCommandAction{
			private List<String> list;
			protected boolean initList, isCurrentEffect, play;
			private String params = "";
			public abstract List<String> getList();
			@Override
			public void initSend(Player p) {
				isCurrentEffect = effect.getId() == getId();
				if(isCurrentEffect) params = effect.getParams();
				if(initList) list = getList();
			}
			@Override
			public void sendWrite() throws IOException {
				write(isCurrentEffect);
				if(isCurrentEffect) write(params);
				write(initList);
				if(initList) writeListStr(list);
			}
			@Override
			public void receiveRead() throws IOException {
				play = readBool();
				params = readStr();
				initList = readBool();
			}
			@Override
			public void receive(Player p) {
				if(play){
					IEffect fx = newEffect(getId());
					fx.loadParams(0, params.split(DELIMITER));
					IEffectState es = fx.getFxState();
					es.cloneData(null);
					es.goPop(getContainer().newDriverGuest(QuestPlayer.get(p)), p, p, p.getLocation().add(1, 1, 0));
					
				}else sendCmdGui(p, this);
			}
		}
		
		class SOUNDDATA extends AbsCmdActionEffect{
			@Override
			public int getId() { return SOUND; }
			@Override
			public AbsCYMCommandAction clone() { return new SOUNDDATA(); }
			@Override
			public List<String> getList() {
				List<String> list = new ArrayList<String>();
				for(Sound s : Sound.values()) list.add(s.name());
				return list;
			}
		}
		
		class EFFECTDATA extends AbsCmdActionEffect{
			@Override
			public int getId() { return EFFECT; }
			@Override
			public AbsCYMCommandAction clone() { return new EFFECTDATA(); }
			@Override
			public List<String> getList() {
				List<String> list = new ArrayList<String>();
				for(Effect e : Effect.values()) if(!e.name().equals("ITEM_BREAK")) list.add(e.name());
				return list;
			}
		}
		
		class ENTITYEFFECTDATA extends AbsCmdActionEffect{
			@Override
			public int getId() { return ENTITYEFFECT; }
			@Override
			public AbsCYMCommandAction clone() { return new ENTITYEFFECTDATA(); }
			@Override
			public List<String> getList() {
				List<String> list = new ArrayList<String>();
				for(EntityEffect e : EntityEffect.values()) list.add(e.name());
				return list;
			}
		}
		
		class NOTEDATA extends AbsCmdActionEffect{
			@Override
			public int getId() { return NOTE; }
			@Override
			public AbsCYMCommandAction clone() { return new NOTEDATA(); }
			@Override
			public List<String> getList() {
				List<String> list = new ArrayList<String>();
				
				return list;
			}
		}
		
		class PARTICLEDATA extends AbsCmdActionEffect{
			@Override
			public int getId() { return PARTICLE; }
			@Override
			public AbsCYMCommandAction clone() { return new PARTICLEDATA(); }
			@Override
			public List<String> getList() {
				List<String> list = new ArrayList<String>();
				
				return list;
			}
		}
		
		class FIREWORKDATA extends AbsCmdActionEffect{
			public List<Integer> idC;
			public List<String> nameC;
			@Override
			public int getId() { return FIREWORK; }
			@Override
			public AbsCYMCommandAction clone() { return new FIREWORKDATA(); }
			@Override
			public List<String> getList() {
				List<String> l = new ArrayList<String>();
				for(FireworkEffect.Type t : FireworkEffect.Type.values()) l.add(t.name());
				return l;
			}
			@Override
			public void initSend(Player p) {
				super.initSend(p);
				if(initList){
					idC = new ArrayList<Integer>(); nameC = new ArrayList<String>();
					idC.add(Color.AQUA.asRGB());idC.add(Color.BLACK.asRGB());idC.add(Color.BLUE.asRGB());idC.add(Color.FUCHSIA.asRGB());idC.add(Color.GRAY.asRGB());idC.add(Color.GREEN.asRGB());idC.add(Color.LIME.asRGB());idC.add(Color.MAROON.asRGB());idC.add(Color.NAVY.asRGB());idC.add(Color.OLIVE.asRGB());idC.add(Color.ORANGE.asRGB());idC.add(Color.PURPLE.asRGB());idC.add(Color.RED.asRGB());idC.add(Color.SILVER.asRGB());idC.add(Color.TEAL.asRGB());idC.add(Color.WHITE.asRGB());idC.add(Color.YELLOW.asRGB());
					nameC.add("AQUA");nameC.add("BLACK");nameC.add("BLUE");nameC.add("FUCHSIA");nameC.add("GRAY");nameC.add("GREEN");nameC.add("LIME");nameC.add("MAROON");nameC.add("NAVY");nameC.add("OLIVE");nameC.add("ORANGE");nameC.add("PURPLE");nameC.add("RED");nameC.add("SILVER");nameC.add("TEAL");nameC.add("WHITE");nameC.add("YELLOW");
				}
			}
			@Override
			public void sendWrite() throws IOException {
				super.sendWrite();
				if(initList) writeList(idC, nameC);
			}		
		}
	}
	
	public interface IEffect {
		public int getId();
		public String getParams();
		public void loadParams(int index, String[] params);
		public IEffectState getFxState();
	}
	public interface IEffectState {
		public void cloneData(StateFX fx);
		public void goPop(IMechaDriver driver, Player p, Entity e, Location loc);
	}
}