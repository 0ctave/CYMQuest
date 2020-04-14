package fr.craftyourmind.quest.mecha;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ActionPlayer extends AbsMechaList{

	private static final int HEALTH = 1;
	private static final int DAMAGE = 2;
	private static final int TEXTURE = 3;
	private static final int COMPASS = 4;
	private static final int BEDSPAWN = 5;
	private static final int VELOCITY = 6;
	private static final int WEATHER = 7;
	private static final int FLY = 8;
	private static final int TELEPORT = 9;
	private static final int TARGET = 10;
	private static Map<Integer, Class<? extends IMechaList>> params = new HashMap<Integer, Class<? extends IMechaList>>();
	static{
		params.put(HEALTH, HEALTH.class);
		params.put(DAMAGE, DAMAGE.class);
		params.put(TEXTURE, TEXTURE.class);
		params.put(COMPASS, COMPASS.class);
		params.put(BEDSPAWN, BEDSPAWN.class);
		params.put(VELOCITY, VELOCITY.class);
		params.put(WEATHER, WEATHER.class);
		params.put(FLY, FLY.class);
		params.put(TELEPORT, TELEPORT.class);
		params.put(TARGET, TARGET.class);
	}
	public ActionPlayer() { }
	@Override
	public Map<Integer, Class<? extends IMechaList>> getMechaParam() { return params; }
	@Override
	public int getType() { return MechaType.ACTPLAYER; }
	@Override
	protected String getStringParams() { return ""; }
	@Override
	protected int loadParams(int index, String[] params) { return index; }
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateEntity(this, mc, driver); }
	// ------------------ StateEntity ------------------
	class StateEntity extends AbsMechaStateEntityList{
		
		public StateEntity(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void start() {
			super.start();
			launchMessage();
		}
	}
	// -------------- HEALTH --------------
	class HEALTH implements IMechaList{
		private static final int INIT = 0;
		private static final int ADD = 1;
		private static final int RESETMAX = 2;
		private int option;
		private DoubleData health = new DoubleData();
		private DoubleData maxHealth = new DoubleData();
		private DoubleData healthScale = new DoubleData();
		@Override
		public int getId() { return HEALTH; }
		@Override
		public String getParams() {
			return new StringBuilder("2").append(DELIMITER).append(health).append(DELIMITER).append(maxHealth).append(DELIMITER).append(option).append(DELIMITER).append(healthScale).toString();
		}
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			health.load(params[index++]);
			maxHealth.load(params[index++]);
			option = Integer.valueOf(params[index++]);
			if(version >= 2) healthScale.load(params[index++]);
			if(maxHealth.get() < 0) maxHealth.set(0d); else if(maxHealth.get() > 1000) maxHealth.set(1000d);
			if(maxHealth.get() != 0 && option == INIT && health.get() > maxHealth.get()) health.set(maxHealth.get());
			if(healthScale.get() < 0) healthScale.set(0d); else if(healthScale.get() > 1000) healthScale.set(1000d);
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateEntity>() {
				private DoubleData health = new DoubleData();
				private DoubleData maxHealth = new DoubleData();
				@Override
				public void cloneData(StateEntity s) {
					health.clone(s, HEALTH.this.health);
					maxHealth.clone(s, HEALTH.this.maxHealth);
					if(maxHealth.get() < 0) maxHealth.set(0d); else if(maxHealth.get() > 1000) maxHealth.set(1000d);
					if(maxHealth.get() != 0 && option == INIT && health.get() > maxHealth.get()) health.set(maxHealth.get());
					if(healthScale.get() < 0) healthScale.set(0d); else if(healthScale.get() > 1000) healthScale.set(1000d);
				}
				@Override
				public void start(StateEntity s) {
					IMechaDriver driver = s.driver;
					Entity e = driver.getEntity();
					if(e != null){
						if(e instanceof Damageable){
							Damageable d = (Damageable)e;
							if(maxHealth.get() > 0){
								double max = maxHealth.get();
								if(option == ADD) max += d.getMaxHealth();
								d.setMaxHealth(max);
							}
							if(option == RESETMAX) d.resetMaxHealth();
							if(health.get() > 0){
								double h = health.get();
								if(option == ADD) h = h+d.getHealth();
								if(h > d.getMaxHealth()) h = d.getMaxHealth();
								d.setHealth(h);
							}
						}
						if(healthScale.get() > 0 && driver.hasPlayer()){
							Player p = driver.getPlayer();
							p.setHealthScaled(true);
							double scale = healthScale.get();
							if(option == ADD) scale = scale+p.getHealthScale();
							p.setHealthScale(scale);
						}
					}
				}
				@Override
				public void stop(StateEntity s) { }
			};
		}
	}
	// -------------- DAMAGE --------------
	class DAMAGE implements IMechaList{
		private DoubleData damage = new DoubleData();
		@Override
		public int getId() { return DAMAGE; }
		@Override
		public String getParams() { return 1+DELIMITER+damage; }
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			damage.load(params[index++]);
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateEntity>() {
				private DoubleData damage = new DoubleData();
				@Override
				public void cloneData(StateEntity s) { damage.clone(s, DAMAGE.this.damage); }
				@Override
				public void start(StateEntity s) {
					IMechaDriver driver = s.driver;
					Entity e = driver.getEntity();
					if(e != null && e instanceof Damageable){
						Damageable d = (Damageable)e;
						if(driver.hasEntitySource()) d.damage(damage.get(), driver.getEntitySource());
						else d.damage(damage.get());
					}
				}
				@Override
				public void stop(StateEntity s) { }
			};
		}
	}
	// -------------- TEXTURE --------------
	class TEXTURE implements IMechaList{
		private StringData url = new StringData();
		@Override
		public int getId() { return TEXTURE; }
		@Override
		public String getParams() { return 1+DELIMITER+url; }
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			url.load("");
			if(params.length > index) url.load(params[index++]);
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateEntity>() {
				private StringData url = new StringData();
				@Override
				public void cloneData(StateEntity s) { url.clone(s, TEXTURE.this.url); }
				@Override
				public void start(StateEntity s) {
					Player p = s.driver.getPlayer();
					if(p != null) p.setResourcePack(url.get());
				}
				@Override
				public void stop(StateEntity s) { }
			};
		}
	}
	// -------------- COMPASS --------------
	class COMPASS implements IMechaList{
		private MechaCoordRelative coordR = new MechaCoordRelative();
		@Override
		public int getId() { return COMPASS; }
		@Override
		public String getParams() { return 1+DELIMITER+coordR.getParams(); }
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			coordR.loadParams(index, params);
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateEntity>() {
				private MechaCoordRelative coordR = new MechaCoordRelative();
				@Override
				public void cloneData(StateEntity s) { coordR.clone(s, COMPASS.this.coordR); }
				@Override
				public void start(StateEntity s) {
					Player p = s.driver.getPlayer();
					if(p != null){
						Location loc = coordR.getLocationRandomRadius(s.driver);
						if(loc != null) p.setCompassTarget(loc);
					}
				}
				@Override
				public void stop(StateEntity s) { }
			};
		}
	}
	// -------------- BEDSPAWN --------------
	class BEDSPAWN implements IMechaList{
		private MechaCoordRelative coordR = new MechaCoordRelative();
		private boolean force;
		@Override
		public int getId() { return BEDSPAWN; }
		@Override
		public String getParams() {
			return 1+DELIMITER+coordR.getParams()+DELIMITER+force;
		}
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			index = coordR.loadParams(index, params);
			force = Boolean.valueOf(params[index]);
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateEntity>() {
				@Override
				public void cloneData(StateEntity s) { }
				@Override
				public void start(StateEntity s) {
					Player p = s.driver.getPlayer();
					if(p != null){
						Location loc = coordR.getLocationRandomRadius(s.driver);
						if(loc != null) p.setBedSpawnLocation(loc, force);
					}
				}
				@Override
				public void stop(StateEntity s) { }
			};
		}
	}
	// -------------- VELOCITY --------------
	class VELOCITY implements IMechaList{
		private MechaVelocity velocity = new MechaVelocity();
		private MechaDirectionRelative directR = new MechaDirectionRelative();
		@Override
		public int getId() { return VELOCITY; }
		@Override
		public String getParams() {
			return new StringBuilder("2").append(DELIMITER).append(velocity.getParams()).append(DELIMITER).append(directR.getParams()).toString();
		}
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			if(version == 1){
				boolean dirYawPlayer = Boolean.valueOf(params[index++]);
				boolean dirPitchPlayer = Boolean.valueOf(params[index++]);
				index = velocity.loadParams(index, params);
				float yaw = Float.valueOf(params[index++]);
				float pitch = Float.valueOf(params[index++]);
				directR.setUseDirect(true);
				directR.setDirection(yaw, pitch, dirYawPlayer, dirPitchPlayer);
				directR.setOnPlayer();
				sqlSave();
			}else  if(version == 2){
				index = velocity.loadParams(index, params);
				directR.loadParams(index, params);
			}
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateEntity>() {
				private MechaVelocity velocity = new MechaVelocity();
				@Override
				public void cloneData(StateEntity s) {
					velocity.clone(s, VELOCITY.this.velocity);
				}
				@Override
				public void start(StateEntity s) {
					Entity e = s.driver.getEntity();
					if(e != null){
			            Location direct = directR.getDirectionVelocity(s.driver);
			            e.setVelocity(velocity.getVelocity(direct.getYaw(), direct.getPitch()));
					}
				}
				@Override
				public void stop(StateEntity s) { }
			};
		}
	}
	// -------------- WEATHER --------------
	class WEATHER implements IMechaList{
		private static final int RESET = 0;
		private static final int CLEAR = 1;
		private static final int DOWNFALL = 2;
		private int option;
		@Override
		public int getId() { return WEATHER; }
		@Override
		public String getParams() { return 1+DELIMITER+option; }
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			option = Integer.valueOf(params[index++]);
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateEntity>() {
				@Override
				public void cloneData(StateEntity s) { }
				@Override
				public void start(StateEntity s) {
					Player p = s.driver.getPlayer();
					if(p != null){
						if(option == RESET) p.resetPlayerWeather();
						else if(option == CLEAR) p.setPlayerWeather(WeatherType.CLEAR);
						else if(option == DOWNFALL) p.setPlayerWeather(WeatherType.DOWNFALL);
					}
				}
				@Override
				public void stop(StateEntity s) { }
			};
		}
	}
	// -------------- FLY --------------
	class FLY implements IMechaList{
		private boolean allowFly, flying;
		private FloatData speed = new FloatData();
		@Override
		public int getId() { return FLY; }
		@Override
		public String getParams() {
			return 1+DELIMITER+allowFly+DELIMITER+flying+DELIMITER+speed;
		}
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			allowFly = Boolean.valueOf(params[index++]);
			flying = Boolean.valueOf(params[index++]);
			speed.load(params[index++]);
			if(speed.get() < -1) speed.set(-1f); else if(speed.get() > 1) speed.set(1f);
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateEntity>() {
				private FloatData speed = new FloatData();
				@Override
				public void cloneData(StateEntity s) {
					speed.clone(s, FLY.this.speed);
					if(speed.get() < -1) speed.set(-1f); else if(speed.get() > 1) speed.set(1f);
				}
				@Override
				public void start(StateEntity s) {
					Player p = s.driver.getPlayer();
					if(p != null){
						p.setAllowFlight(allowFly);
						if(flying && !allowFly) return;
						p.setFlying(flying);
						p.setFlySpeed(speed.get());
					}
				}
				@Override
				public void stop(StateEntity s) { }
			};
		}
	}
	// -------------- TELEPORT --------------
	public class TELEPORT implements IMechaList{
		private MechaCoordRelative coordR = new MechaCoordRelative();
		private MechaDirectionRelative directR = new MechaDirectionRelative();
		@Override
		public int getId() { return TELEPORT; }
		@Override
		public String getParams() {
			return 2+DELIMITER+coordR.getParams()+DELIMITER+directR.getParams();
		}
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			if(version == 1){
				index = coordR.loadParams(index, params);
				String yawName = params[index];
				boolean direction = true;
				if(yawName.equals("SOUTH")) directR.setSouth();
				else if(yawName.equals("NORTH")) directR.setNorth();
				else if(yawName.equals("EAST")) directR.setEast();
				else if(yawName.equals("WEST")) directR.setWest();
				else if(yawName.equals("NO")) direction = false;
				directR.setUseDirect(direction);
			}else if(version == 2){
				index = coordR.loadParams(index, params);
				directR.loadParams(index, params);
			}
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateEntity>() {
				private MechaCoordRelative coordR = new MechaCoordRelative();
				private MechaDirectionRelative directR = new MechaDirectionRelative();
				@Override
				public void cloneData(StateEntity s) {
					coordR.clone(s, TELEPORT.this.coordR);
					directR.clone(s, TELEPORT.this.directR);
				}
				@Override
				public void start(StateEntity s) {
					IMechaDriver driver = s.driver;
					Entity e = driver.getEntity();
					if(e != null){
						Location loc = coordR.getLocationRandomRadius(driver);
						if(loc != null){
							Location locdirect = null;
							if(directR.useDirection()) locdirect = directR.getDirection(driver, loc);
							else locdirect = e.getLocation();
							loc.setYaw(locdirect.getYaw());
							loc.setPitch(locdirect.getPitch());
							e.teleport(loc);
						}
					}
				}
				@Override
				public void stop(StateEntity s) { }
			};
		}
	}
	// -------------- TARGET --------------
	public class TARGET implements IMechaList{
		private boolean isTarget = true, sourceIsTarget = false;
		@Override
		public int getId() { return TARGET; }
		@Override
		public String getParams() {
			return 0+DELIMITER+isTarget+DELIMITER+sourceIsTarget;
		}
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			isTarget = Boolean.valueOf(params[index++]);
			sourceIsTarget = Boolean.valueOf(params[index++]);
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateEntity>() {
				@Override
				public void cloneData(StateEntity s) { }
				@Override
				public void start(StateEntity s) {
					IMechaDriver driver = s.driver;
					Entity e = driver.getEntity();
					if(isTarget){
						if(driver.getEntitySource() instanceof Creature && e instanceof LivingEntity && driver.getEntitySource().getEntityId() != e.getEntityId())
							((Creature)driver.getEntitySource()).setTarget((LivingEntity)e);
					}else if(sourceIsTarget){
						if(driver.getEntitySource() instanceof LivingEntity && e instanceof Creature && driver.getEntitySource().getEntityId() != e.getEntityId())
							((Creature)e).setTarget((LivingEntity)driver.getEntitySource());
					}
				}
				@Override
				public void stop(StateEntity s) { }
			};
		}
	}
}