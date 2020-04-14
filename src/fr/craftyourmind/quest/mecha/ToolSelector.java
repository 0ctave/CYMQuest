package fr.craftyourmind.quest.mecha;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import fr.craftyourmind.manager.CYMManager;
import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.QuestTools;
import fr.craftyourmind.quest.command.CmdChunkPosition;

public class ToolSelector extends AbsMechaList{

	private static final int BLOCK = 1;
	private static final int TARGET = 2;
	private static final int PLAYERS = 3;
	
	private static Map<Integer, Class<? extends IMechaList>> params = new HashMap<Integer, Class<? extends IMechaList>>();
	static{
		params.put(BLOCK, BLOCK.class);
		params.put(TARGET, TARGET.class);
		params.put(PLAYERS, PLAYERS.class);
	}
	
	private MechaDirectionRelative directR = new MechaDirectionRelative();
	
	public ToolSelector() { }
	@Override
	public Map<Integer, Class<? extends IMechaList>> getMechaParam() { return params; }
	@Override
	public int getType() { return MechaType.TOOSELECT; }
	@Override
	protected String getStringParams() { return 100+DELIMITER+directR.getParams(); }
	@Override
	protected int loadParams(int index, String[] params) {
		int version = Integer.valueOf(params[index]);
		if(version > 99){
			index++;
			index = directR.loadParams(index, params);
		}
		return index;
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateSelector(this, mc, driver); }
	// ------------------ StateSelector ------------------
	class StateSelector extends AbsMechaStateEntityList{
		
		private MechaDirectionRelative directR = new MechaDirectionRelative();
		
		public StateSelector(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void cloneData() { super.cloneData(); directR.clone(this, ToolSelector.this.directR); }
	}
	// ------------------ CoordRelative ------------------
	abstract class AbsCoordRelative implements IMechaList{
		protected MechaCoordRelative coordR = new MechaCoordRelative();
		@Override
		public String getParams() { return 0+DELIMITER+coordR.getParams(); }
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			coordR.loadParams(index, params);
		}
	}
	// ------------------ BLOCK ------------------
	class BLOCK extends AbsCoordRelative{
		@Override
		public int getId() { return BLOCK; }
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateSelector>() {
				protected MechaCoordRelative coordR = new MechaCoordRelative();
				@Override
				public void cloneData(StateSelector s) { coordR.clone(s, BLOCK.this.coordR); }
				@Override
				public void start(StateSelector s) {
					IMechaDriver driver = s.driver;
					Location loc = coordR.getLocationRandomRadius(driver);
					if(loc != null){
						driver.setLocation(loc);
						if(s.directR.useDirection()) driver.setDirection(s.directR.getDirection(driver, loc));
						s.launchMessage();
					}
				}
				@Override
				public void stop(StateSelector s) { }
			};
		}
	}
	// ------------------ TARGET ------------------
	class TARGET implements IMechaList{
		private static final int BLOCK = 0;
		private static final int PLAYER = 1;
		private int target;
		private IntegerData distance = new IntegerData();
		private boolean isLaunched;
		private boolean selectPlayer = true;
		private boolean selectEntity = true;
		private boolean selectNpc = false;
		@Override
		public int getId() { return TARGET; }
		@Override
		public String getParams() {
			return 1+DELIMITER+isLaunched+DELIMITER+target+DELIMITER+distance+DELIMITER+selectPlayer+DELIMITER+selectEntity+DELIMITER+selectNpc;
		}
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			isLaunched = Boolean.valueOf(params[index++]);
			target = Integer.valueOf(params[index++]);
			distance.load(params[index++]);
			if(version == 1){
				selectPlayer = Boolean.valueOf(params[index++]);
				selectEntity = Boolean.valueOf(params[index++]);
				selectNpc = Boolean.valueOf(params[index++]);
			}
			if(target < 0) target = 0;
			if(distance.get() < 0) distance.set(0);
			else if(distance.get() > 64) distance.set(64);
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateSelector>() {
				private IntegerData distance = new IntegerData();
				@Override
				public void cloneData(StateSelector s) {
					distance.clone(s, TARGET.this.distance);
					if(distance.get() < 0) distance.set(0); 
					else if(distance.get() > 64) distance.set(64);
				}
				@Override
				public void start(StateSelector s) {
					IMechaDriver driver = s.driver;
					Entity e = driver.getEntity();
					if(e != null && e instanceof LivingEntity){
						LivingEntity le = (LivingEntity)e;
						if(!isLaunched && target == BLOCK){
							Block b = le.getTargetBlock(transparents, distance.get());
							if(b != null){
								driver.setLocation(b.getLocation());
								if(s.directR.useDirection()) driver.setDirection(s.directR.getDirection(driver, b.getLocation()));
								s.launchMessage();
							}
						}else if(!isLaunched && target == PLAYER){
							LivingEntity ptarget = getTarget(le);
							if(ptarget != null){
								driver.setLocation(ptarget.getLocation());
								if(s.directR.useDirection()) driver.setDirection(s.directR.getDirection(driver, ptarget.getLocation()));
								s.launchMessage();
							}
						}else if(isLaunched && target == PLAYER){
							LivingEntity ptarget = getTarget(le);
							if(ptarget != null){
								IMechaDriver d = null;
								if(ptarget.getType() == EntityType.PLAYER){
									if(selectPlayer && !CYMManager.isNPC(ptarget)){
										QuestPlayer qp = QuestPlayer.get(ptarget);
										if(qp != null) d = getContainer().newDriverGuest(qp);
									}else if(selectNpc && CYMManager.isNPC(ptarget))
										d = getContainer().newDriver(ptarget);									
								}else if(selectEntity)
									d = getContainer().newDriver(ptarget);
								
								if(d != null){									
									d.setLocation(driver.getLocation());
									d.setEntitySource(driver.getEntity());
									Location locDirectR = driver.getDirection();
									if(s.directR.useDirection()) locDirectR = s.directR.getDirection(driver, ptarget.getLocation());
									d.setDirection(locDirectR);
									launch(d);
									s.sendMessage();
								}
							}
						}
					}
				}
				@Override
				public void stop(StateSelector s) { }
				
				private LivingEntity getTarget(LivingEntity le){
					List<Block> list = le.getLineOfSight(transparents, distance.get());
					List<Location> locs = new ArrayList<Location>();
					for(Block b : list){
						locs.add(b.getRelative(BlockFace.UP).getLocation());
						locs.add(b.getLocation());
						locs.add(b.getRelative(BlockFace.DOWN).getLocation());
					}
					List<Entity> entities = le.getNearbyEntities(distance.get(), distance.get(), distance.get());
					for (Entity e : entities) {
						if(e instanceof LivingEntity && !e.isDead() && e.isValid() && locs.contains(e.getLocation().getBlock().getLocation())){
							if(e.getType() != EntityType.PLAYER || le.getType() != EntityType.PLAYER || ((Player)le).canSee((Player)e))
								return (LivingEntity) e;
						}
					}
					return null;
				}
			};
		}
	}
	// ------------------ PLAYERS ------------------
	class PLAYERS extends AbsCoordRelative{
		private boolean selectPlayer = true;
		private boolean selectEntity = true;
		private boolean selectNpc = false;
		private boolean selectHimself = false;
		@Override
		public int getId() { return PLAYERS; }
		@Override
		public String getParams() {
			return 1+DELIMITER+coordR.getParams()+DELIMITER+selectPlayer+DELIMITER+selectEntity+DELIMITER+selectNpc+DELIMITER+selectHimself;
		}
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			index = coordR.loadParams(index, params);
			if(version == 1){
				selectPlayer = Boolean.valueOf(params[index++]);
				selectEntity = Boolean.valueOf(params[index++]);
				selectNpc = Boolean.valueOf(params[index++]);
				selectHimself = Boolean.valueOf(params[index++]);
			}
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateSelector>() {
				protected MechaCoordRelative coordR = new MechaCoordRelative();
				@Override
				public void cloneData(StateSelector s) { coordR.clone(s, PLAYERS.this.coordR); }
				@Override
				public void start(StateSelector s) {
					IMechaDriver driver = s.driver;
					Location loc = coordR.getLocationRandomRadius(driver);
					if(loc != null){
						IMechaContainer mcon = getContainer();
						String w = loc.getWorld().getName();
						int x = loc.getBlockX(), y = loc.getBlockY(), z = loc.getBlockZ();				
						Location locDirectR = driver.getDirection();
						if(s.directR.useDirection()) locDirectR = s.directR.getDirection(driver, loc);
						int driverentityId = driver.hasEntity() ? driver.getEntity().getEntityId() : 0;
						List<Integer> playerIds = new ArrayList<Integer>(); // select NPC
						if(selectPlayer){
							List<QuestPlayer> list = CmdChunkPosition.getPlayers(loc, coordR.getRadius());
							for(QuestPlayer qp : list){
								int playerentityid = (qp.getPlayer() != null) ? qp.getPlayer().getEntityId() : 0;
								if(playerentityid > 0 && (selectHimself || playerentityid != driverentityId) && QuestTools.insideLoc(w, x, y, z, qp.getPlayer().getLocation(),  coordR.getRadius())){
									IMechaDriver d = mcon.newDriverGuest(qp);
									d.setLocation(driver.getLocation());
									d.setEntitySource(driver.getEntity());
									d.setDirection(locDirectR);
									launch(d);
									playerIds.add(playerentityid);
								}
							}
						}
						if(selectEntity){
							for(Entity e : loc.getWorld().getLivingEntities()){						
								if(((selectNpc && e.getType() == EntityType.PLAYER && !playerIds.contains(e.getEntityId())) || e.getType() != EntityType.PLAYER) && (selectHimself || e.getEntityId() != driverentityId)  && QuestTools.insideLoc(w, x, y, z, e.getLocation(), coordR.getRadius())){
									IMechaDriver d = mcon.newDriver(e);
									d.setLocation(driver.getLocation());
									d.setEntitySource(driver.getEntity());
									d.setDirection(locDirectR);
									launch(d);
								}
							}
						}
						s.sendMessage();
					}
				}
				@Override
				public void stop(StateSelector s) { }
			};
		}
	}
	
	public static final Set<Material> airTransparents = new HashSet();
	static{ airTransparents.add(Material.AIR); }
	
	public static final Set<Material> transparents = new HashSet();
	static{
		transparents.add(Material.AIR);
		transparents.add(Material.CYAN_CARPET);
		transparents.add(Material.BLACK_CARPET);
		transparents.add(Material.BLUE_CARPET);
		transparents.add(Material.BROWN_CARPET);
		transparents.add(Material.GRAY_CARPET);
		transparents.add(Material.GREEN_CARPET);
		transparents.add(Material.LIME_CARPET);
		transparents.add(Material.LIGHT_BLUE_CARPET);
		transparents.add(Material.LIGHT_GRAY_CARPET);
		transparents.add(Material.MAGENTA_CARPET);
		transparents.add(Material.ORANGE_CARPET);
		transparents.add(Material.PINK_CARPET);
		transparents.add(Material.WHITE_CARPET);
		transparents.add(Material.RED_CARPET);
		transparents.add(Material.PURPLE_CARPET);
		transparents.add(Material.YELLOW_CARPET);
		transparents.add(Material.DEAD_BUSH);
		transparents.add(Material.DETECTOR_RAIL);
		transparents.add(Material.REPEATER);
		transparents.add(Material.FLOWER_POT);
		transparents.add(Material.LADDER);
		transparents.add(Material.LEVER);
		transparents.add(Material.TALL_GRASS);
		transparents.add(Material.NETHER_WART);
		transparents.add(Material.END_PORTAL);
		transparents.add(Material.NETHER_PORTAL);
		transparents.add(Material.POWERED_RAIL);
		transparents.add(Material.RAIL);
		transparents.add(Material.ACTIVATOR_RAIL);
		transparents.add(Material.COMPARATOR);
		transparents.add(Material.REDSTONE_TORCH);
		transparents.add(Material.REDSTONE_WIRE);
		transparents.add(Material.SPRUCE_FENCE_GATE);
		transparents.add(Material.ACACIA_FENCE_GATE);
		transparents.add(Material.BIRCH_FENCE_GATE);
		transparents.add(Material.DARK_OAK_FENCE_GATE);
		transparents.add(Material.JUNGLE_FENCE_GATE);
		transparents.add(Material.OAK_FENCE_GATE);
		transparents.add(Material.SPRUCE_FENCE);
		transparents.add(Material.ACACIA_FENCE);
		transparents.add(Material.BIRCH_FENCE);
		transparents.add(Material.DARK_OAK_FENCE);
		transparents.add(Material.JUNGLE_FENCE);
		transparents.add(Material.OAK_FENCE);
		transparents.add(Material.SPRUCE_SAPLING);
		transparents.add(Material.ACACIA_SAPLING);
		transparents.add(Material.BIRCH_SAPLING);
		transparents.add(Material.DARK_OAK_SAPLING);
		transparents.add(Material.JUNGLE_SAPLING);
		transparents.add(Material.OAK_SAPLING);
		transparents.add(Material.SPRUCE_SIGN);
		transparents.add(Material.ACACIA_SIGN);
		transparents.add(Material.BIRCH_SIGN);
		transparents.add(Material.DARK_OAK_SIGN);
		transparents.add(Material.JUNGLE_SIGN);
		transparents.add(Material.OAK_SIGN);
		transparents.add(Material.SPRUCE_WALL_SIGN);
		transparents.add(Material.ACACIA_WALL_SIGN);
		transparents.add(Material.BIRCH_WALL_SIGN);
		transparents.add(Material.DARK_OAK_WALL_SIGN);
		transparents.add(Material.JUNGLE_WALL_SIGN);
		transparents.add(Material.OAK_WALL_SIGN);
		transparents.add(Material.SPRUCE_BUTTON);
		transparents.add(Material.ACACIA_BUTTON);
		transparents.add(Material.BIRCH_BUTTON);
		transparents.add(Material.DARK_OAK_BUTTON);
		transparents.add(Material.JUNGLE_BUTTON);
		transparents.add(Material.OAK_BUTTON);
		transparents.add(Material.SPRUCE_PRESSURE_PLATE);
		transparents.add(Material.ACACIA_PRESSURE_PLATE);
		transparents.add(Material.BIRCH_PRESSURE_PLATE);
		transparents.add(Material.DARK_OAK_PRESSURE_PLATE);
		transparents.add(Material.JUNGLE_PRESSURE_PLATE);
		transparents.add(Material.OAK_PRESSURE_PLATE);
		transparents.add(Material.SNOW);
		transparents.add(Material.LAVA);
		transparents.add(Material.WATER);
		transparents.add(Material.STONE_BUTTON);
		transparents.add(Material.STONE_PRESSURE_PLATE);
		transparents.add(Material.SUGAR_CANE);
		transparents.add(Material.TORCH);
		transparents.add(Material.TRIPWIRE);
		transparents.add(Material.VINE);
		transparents.add(Material.LILY_PAD);
		transparents.add(Material.COBWEB);
	}
}