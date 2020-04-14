package fr.craftyourmind.quest.mecha;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Cat.Type;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import fr.craftyourmind.manager.command.AbsCYMCommand;
import fr.craftyourmind.manager.command.ICYMCommandData;
import fr.craftyourmind.quest.Plugin;
import fr.craftyourmind.quest.mecha.ActionPopEntity.ITrait;

public class ActionPopEntity extends Mechanism{

	public static final int ENTITY = 0;
	public static final int EXPLOSION = 1;
	public static final int MOBSPAWNER = 2;
	
	public static final int HELMET = 0;
	public static final int CHESPLATE = 1;
	public static final int LEGGINGS = 2;
	public static final int BOOTS = 3;
	public static final int ITEMINHAND = 4;
	
	public static final int DAMAGEABLE = 3;
	public static final int AGEABLE = 4;
	public static final int SLIME = 5;
	public static final int TAMEABLE = 6;
	public static final int ZOMBIE = 7;
	public static final int EXPLOSIVE = 8;
	public static final int FIREBALL = 9;
	public static final int LIVINGENTITY = 10;
	public static final int CREATURE = 11;
	public static final int COLOR = 12;
	public static final int PROJECTILE = 18;
	
	public static final int PIGZOMBIE = 13;
	public static final int HORSE = 14;
	public static final int SHEEP = 15;
	public static final int CREEPER = 16;
	public static final int EXPERIENCEORB = 17;
	public static final int IRONGOLEM = 19;
	public static final int CAT = 20;
	public static final int PIG = 21;
	public static final int TNTPRIMED = 22;
	public static final int SKELETON = 23;
	public static final int VILLAGER = 24;
	public static final int WOLF = 25;
	public static final int GUARDIAN = 26;
	public static final int RABBIT = 27;
	
	public static List<TraitsEntity> traitsEntity = new ArrayList<TraitsEntity>();
	static{
		for(EntityType e : EntityType.values()){
			if(e.isSpawnable() && e != EntityType.PAINTING && e != EntityType.ENDER_PEARL && e != EntityType.ITEM_FRAME && e != EntityType.LEASH_HITCH){
				TraitsEntity te = new TraitsEntity();
				te.name = e.getKey().toString();
				initTraitsClass(te.idTraitsEntity, te.classTraitsEntity, e.getEntityClass());
				traitsEntity.add(te);
			}
		}
	}
	
	public static TraitsEntity getTraitsEntity(String name){
		for(TraitsEntity te : traitsEntity) if(name.equals(te.name)) return te;
		return null;
	}
	
	public static boolean bypassSpawnProtect = false;
	
	public int typePop = 0;
	public IntegerData nbPop = new IntegerData(1);
	private MechaCoordRelative coordR = new MechaCoordRelative();
	public int idEntityType = 0; // old
	public float power = 0; // old
	public boolean fire = false; // old
	public boolean breakBlocks = true; // old
	public boolean onlyIfDeadOld = false; // old
	public boolean cleanEntityOld = true; // old
	private List<LivingEntity> pops = new ArrayList<LivingEntity>();
	private IPopEntity popEntity;
	private boolean isLaunched = false;
	
	public ActionPopEntity() {}
	@Override
	public ICYMCommandData newCommandData() { return new CmdPopEntity(); }
	@Override
	public boolean isMechaStoppable(){ return true; }
	
	private boolean isDead(List<LivingEntity> entities){
		boolean goPop = true;
		if(entities.isEmpty()) goPop = true;
		else{ for(Entity e : entities){ if(!e.isDead() && e.isValid()){ goPop = false; break; } }
			if(goPop) entities.clear();
		} return goPop;
	}
	
	private void cleanEntity(List<LivingEntity> entities){
		for(LivingEntity e : entities){ e.remove(); MechaDriverEntity.onEntityDeath(e); }
		entities.clear();
	}
	@Override
	public int getType() { return MechaType.ACTPOPENTITY; }
	@Override
	public String getParams() {
		return new StringBuilder("9").append(DELIMITER).append(popEntity.getId()).append(DELIMITER).append(nbPop).append(DELIMITER).append(coordR.getParams()).append(DELIMITER).append(popEntity.getParams()).toString();	
	}
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		int versions = Integer.valueOf(params[0]);
		if(versions == 3){
			typePop = Integer.valueOf(params[1]);
			idEntityType = Integer.valueOf(params[2]);
			nbPop.load(params[3]);
			coordR.setCoord(params[4], Integer.valueOf(params[5]), Integer.valueOf(params[6]), Integer.valueOf(params[7]), Integer.valueOf(params[8]), Boolean.valueOf(params[9]));
			boolean onPlayer = Boolean.valueOf(params[10]);
			if(onPlayer) coordR.setOnPlayer();
			power = Float.valueOf(params[11]);
			fire = Boolean.valueOf(params[12]);
			breakBlocks = Boolean.valueOf(params[13]);
			if(typePop == EXPLOSION){
				popEntity = getPopEntity(typePop);
				popEntity.loadParams(0, new String[]{1+"", power+"", fire+"", breakBlocks+""});
			}else if(typePop == MOBSPAWNER){
				popEntity = getPopEntity(typePop);
				popEntity.loadParams(0, new String[]{1+"", EntityType.fromId(idEntityType).getKey().toString()});
			}else if(typePop == ENTITY){
				popEntity = getPopEntity(typePop);
				List<ITrait> traits = new ArrayList<ITrait>();
				int size = Integer.valueOf(params[14]);
				int index = 15;
				if(size > 0){
					List<Integer> types = new ArrayList<Integer>();
					List<Integer> idMats = new ArrayList<Integer>();
					List<Float> dropChances = new ArrayList<Float>();
					List<String> enchants = new ArrayList<String>();
					List<Integer> enchantLvls = new ArrayList<Integer>();
					List<Integer> effects = new ArrayList<Integer>();
					List<Integer> durations = new ArrayList<Integer>();
					List<Integer> amplifiers = new ArrayList<Integer>();
					boolean equipment = false;
					boolean potion = false;
					for(int i = 0 ; i < size ; i++){
						types.add(Integer.valueOf(params[index++]));
						idMats.add(Integer.valueOf(params[index++]));
						dropChances.add(Float.valueOf(params[index++]));
						enchants.add(params[index++]);
						enchantLvls.add(Integer.valueOf(params[index++]));
						effects.add(Integer.valueOf(params[index++]));
						durations.add(Integer.valueOf(params[index++]));
						amplifiers.add(Integer.valueOf(params[index++]));
						if(idMats.get(i) > 0) equipment = true;
						if(effects.get(i) > 0) potion = true;
					}
					if(potion || equipment){
						int nbPo = 0;
						String pPotion = "";
						for(int i = 0 ; i < size ; i++){
							int idpo = effects.get(i);
							if(idpo > 0){
								nbPo++;
								pPotion += DELIMITER+PotionEffectType.getById(effects.get(i)).getName()+DELIMITER+durations.get(i)+DELIMITER+amplifiers.get(i);
							}
						}
						String pPotion2 = nbPo + pPotion;
						String pEqui = size+"";
						for(int i = 0 ; i < size ; i++){
							String ench = (enchantLvls.get(i) == 0)? "" : Enchantment.getByKey(NamespacedKey.minecraft(enchants.get(i))).getName();
							pEqui += DELIMITER+types.get(i)+DELIMITER+idMats.get(i)+DELIMITER+0+DELIMITER+dropChances.get(i)+DELIMITER+ench+DELIMITER+enchantLvls.get(i);
						}
						ITrait tLivingE = new LIVINGENTITY();
						String pLinvingE = 1+DELIMITER+pPotion2+DELIMITER+pEqui;
						tLivingE.loadParams(0, pLinvingE.split(DELIMITER));
						traits.add(tLivingE);
					}
					String nameEntity = (idEntityType == 0)?"":EntityType.fromId(idEntityType).getKey().toString();
					String pEntity = 1+DELIMITER+nameEntity+DELIMITER+onlyIfDeadOld+DELIMITER+cleanEntityOld+DELIMITER+false+DELIMITER+false+DELIMITER+0.5+DELIMITER+200+DELIMITER+0+DELIMITER+0+DELIMITER+traits.size();
					for(ITrait t : traits) pEntity += DELIMITER+t.getId()+DELIMITER+t.getParams();
					popEntity.loadParams(0, pEntity.split(DELIMITER));
				}
			}
			sqlSave();
		}else if(versions == 6){
			typePop = Integer.valueOf(params[1]);
			idEntityType = Integer.valueOf(params[2]);
			nbPop.load(params[3]);
			coordR.setCoord(params[4], Integer.valueOf(params[5]), Integer.valueOf(params[6]), Integer.valueOf(params[7]), Integer.valueOf(params[8]), Boolean.valueOf(params[9]));
			boolean onPlayer = Boolean.valueOf(params[10]);
			if(onPlayer) coordR.setOnPlayer();
			power = Float.valueOf(params[11]);
			fire = Boolean.valueOf(params[12]);
			breakBlocks = Boolean.valueOf(params[13]);
			onlyIfDeadOld = Boolean.valueOf(params[14]);
			if(typePop == EXPLOSION){
				popEntity = getPopEntity(typePop);
				popEntity.loadParams(0, new String[]{1+"", power+"", fire+"", breakBlocks+""});
			}else if(typePop == MOBSPAWNER){
				popEntity = getPopEntity(typePop);
				popEntity.loadParams(0, new String[]{1+"", EntityType.fromId(idEntityType).getKey().toString()});
			}else if(typePop == ENTITY){
				popEntity = getPopEntity(typePop);
				List<ITrait> traits = new ArrayList<ITrait>();
				int size = Integer.valueOf(params[15]);
				int index = 16;
				if(size > 0){
					List<Integer> types = new ArrayList<Integer>();
					List<Integer> idMats = new ArrayList<Integer>();
					List<Float> dropChances = new ArrayList<Float>();
					List<String> enchants = new ArrayList<String>();
					List<Integer> enchantLvls = new ArrayList<Integer>();
					List<Integer> effects = new ArrayList<Integer>();
					List<Integer> durations = new ArrayList<Integer>();
					List<Integer> amplifiers = new ArrayList<Integer>();
					boolean equipment = false;
					boolean potion = false;
					for(int i = 0 ; i < size ; i++){
						types.add(Integer.valueOf(params[index++]));
						idMats.add(Integer.valueOf(params[index++]));
						dropChances.add(Float.valueOf(params[index++]));
						enchants.add(params[index++]);
						enchantLvls.add(Integer.valueOf(params[index++]));
						effects.add(Integer.valueOf(params[index++]));
						durations.add(Integer.valueOf(params[index++]));
						amplifiers.add(Integer.valueOf(params[index++]));
						if(idMats.get(i) > 0) equipment = true;
						if(effects.get(i) > 0) potion = true;
					}
					if(potion || equipment){
						int nbPo = 0;
						String pPotion = "";
						for(int i = 0 ; i < size ; i++){
							int idpo = effects.get(i);
							if(idpo > 0){
								nbPo++;
								pPotion += DELIMITER+PotionEffectType.getById(effects.get(i)).getName()+DELIMITER+durations.get(i)+DELIMITER+amplifiers.get(i);
							}
						}
						String pPotion2 = nbPo + pPotion;
						String pEqui = size+"";
						for(int i = 0 ; i < size ; i++){
							String ench = (enchantLvls.get(i) == 0)? "" : Enchantment.getByKey(NamespacedKey.minecraft(enchants.get(i))).getKey().toString();
							pEqui += DELIMITER+types.get(i)+DELIMITER+idMats.get(i)+DELIMITER+0+DELIMITER+dropChances.get(i)+DELIMITER+ench+DELIMITER+enchantLvls.get(i);
						}
						ITrait tLivingE = new LIVINGENTITY();
						String pLinvingE = 1+DELIMITER+pPotion2+DELIMITER+pEqui;
						tLivingE.loadParams(0, pLinvingE.split(DELIMITER));
						traits.add(tLivingE);
					}
					String nameEntity = (idEntityType == 0)?"":EntityType.fromId(idEntityType).getKey().toString();
					String pEntity = 1+DELIMITER+nameEntity+DELIMITER+onlyIfDeadOld+DELIMITER+cleanEntityOld+DELIMITER+false+DELIMITER+false+DELIMITER+0.5+DELIMITER+200+DELIMITER+0+DELIMITER+0+DELIMITER+traits.size();
					for(ITrait t : traits) pEntity += DELIMITER+t.getId()+DELIMITER+t.getParams();
					popEntity.loadParams(0, pEntity.split(DELIMITER));
				}
			}
			sqlSave();
		}else if(versions == 7){
			typePop = Integer.valueOf(params[1]);
			idEntityType = Integer.valueOf(params[2]);
			nbPop.load(params[3]);
			coordR.setCoord(params[4], Integer.valueOf(params[5]), Integer.valueOf(params[6]), Integer.valueOf(params[7]), Integer.valueOf(params[8]), Boolean.valueOf(params[9]));
			boolean onPlayer = Boolean.valueOf(params[10]);
			if(onPlayer) coordR.setOnPlayer();
			power = Float.valueOf(params[11]);
			fire = Boolean.valueOf(params[12]);
			breakBlocks = Boolean.valueOf(params[13]);
			onlyIfDeadOld = Boolean.valueOf(params[14]);
			if(typePop == EXPLOSION){
				popEntity = getPopEntity(typePop);
				popEntity.loadParams(0, new String[]{1+"", power+"", fire+"", breakBlocks+""});
			}else if(typePop == MOBSPAWNER){
				popEntity = getPopEntity(typePop);
				popEntity.loadParams(0, new String[]{1+"", EntityType.fromId(idEntityType).getKey().toString()});
			}else if(typePop == ENTITY){
				popEntity = getPopEntity(typePop);
				List<ITrait> traits = new ArrayList<ITrait>();
				int size = Integer.valueOf(params[15]);
				int index = 16;
				if(size > 0){
					List<Integer> types = new ArrayList<Integer>();
					List<Integer> idMats = new ArrayList<Integer>();
					List<Byte> dataMats = new ArrayList<Byte>();
					List<Float> dropChances = new ArrayList<Float>();
					List<String> enchants = new ArrayList<String>();
					List<Integer> enchantLvls = new ArrayList<Integer>();
					List<Integer> effects = new ArrayList<Integer>();
					List<Integer> durations = new ArrayList<Integer>();
					List<Integer> amplifiers = new ArrayList<Integer>();
					boolean equipment = false;
					boolean potion = false;
					for(int i = 0 ; i < size ; i++){
						types.add(Integer.valueOf(params[index++]));
						idMats.add(Integer.valueOf(params[index++]));
						dataMats.add(Byte.valueOf(params[index++]));
						dropChances.add(Float.valueOf(params[index++]));
						enchants.add(params[index++]);
						enchantLvls.add(Integer.valueOf(params[index++]));
						effects.add(Integer.valueOf(params[index++]));
						durations.add(Integer.valueOf(params[index++]));
						amplifiers.add(Integer.valueOf(params[index++]));
						if(idMats.get(i) > 0) equipment = true;
						if(effects.get(i) > 0) potion = true;
					}
					if(potion || equipment){
						int nbPo = 0;
						String pPotion = "";
						for(int i = 0 ; i < size ; i++){
							int idpo = effects.get(i);
							if(idpo > 0){
								nbPo++;
								pPotion += DELIMITER+PotionEffectType.getById(effects.get(i)).getName()+DELIMITER+durations.get(i)+DELIMITER+amplifiers.get(i);
							}
						}
						String pPotion2 = nbPo + pPotion;
						String pEqui = size+"";
						for(int i = 0 ; i < size ; i++){
							String ench = (enchantLvls.get(i) == 0)? "" : Enchantment.getByKey(NamespacedKey.minecraft(enchants.get(i))).getKey().toString();
							pEqui += DELIMITER+types.get(i)+DELIMITER+idMats.get(i)+DELIMITER+dataMats.get(i)+DELIMITER+dropChances.get(i)+DELIMITER+ench+DELIMITER+enchantLvls.get(i);
						}
						ITrait tLivingE = new LIVINGENTITY();
						String pLinvingE = 1+DELIMITER+pPotion2+DELIMITER+pEqui;
						tLivingE.loadParams(0, pLinvingE.split(DELIMITER));
						traits.add(tLivingE);
					}
					String nameEntity = (idEntityType == 0)?"":EntityType.fromId(idEntityType).getKey().toString();
					String pEntity = 1+DELIMITER+nameEntity+DELIMITER+onlyIfDeadOld+DELIMITER+cleanEntityOld+DELIMITER+false+DELIMITER+false+DELIMITER+0.5+DELIMITER+200+DELIMITER+0+DELIMITER+0+DELIMITER+traits.size();
					for(ITrait t : traits) pEntity += DELIMITER+t.getId()+DELIMITER+t.getParams();
					popEntity.loadParams(0, pEntity.split(DELIMITER));
				}
			}
			sqlSave();
		}else if(versions == 8){
			typePop = Integer.valueOf(params[1]);
			nbPop.load(params[2]);
			coordR.setCoord(params[3], Integer.valueOf(params[4]), Integer.valueOf(params[5]), Integer.valueOf(params[6]), Integer.valueOf(params[7]), Boolean.valueOf(params[8]));
			boolean onPlayer = Boolean.valueOf(params[9]);
			if(onPlayer) coordR.setOnPlayer();
			popEntity = getPopEntity(typePop);
			if(popEntity != null) popEntity.loadParams(10, params);
			sqlSave();
		}else if(versions == 9){
			typePop = Integer.valueOf(params[1]);
			nbPop.load(params[2]);
			int index = coordR.loadParams(3, params);
			popEntity = getPopEntity(typePop);
			if(popEntity != null) popEntity.loadParams(index, params);
		}
	}

	public IPopEntity getPopEntity(int type){
		if(popEntity != null && popEntity.getId() == type) return popEntity;
		else return newPopEntity(type);
	}
	public IPopEntity newPopEntity(int type){
		if(type == ENTITY) return new ENTITY();
		else if(type == MOBSPAWNER) return new MOBSPAWNER();
		else if(type == EXPLOSION) return new EXPLOSION();
		return null;
	}

	public void loadTraits(String nameEntity, List<ITrait> traits){
		TraitsEntity te = getTraitsEntity(nameEntity);
		if(te != null){
			try{ for(Class<? extends ITrait> c : te.classTraitsEntity) 
				traits.add(c.getDeclaredConstructor(ActionPopEntity.class).newInstance(this));
			}catch (Exception e){ Plugin.log("error popentity "+nameEntity); }
		}		
	}
	private ITrait getNewTrait(String nameEntity, int idTrait){
		TraitsEntity te = getTraitsEntity(nameEntity);
		if(te != null){
			int index = te.idTraitsEntity.indexOf(idTrait);
			if(index >= 0) try{ return te.classTraitsEntity.get(index).getDeclaredConstructor(ActionPopEntity.class).newInstance(this); }catch (Exception e){ }
		}
		return null;
	}
	private static void initTraitsClass(List<Integer> idTraits, List<Class<? extends ITrait>> traits, Class refclass){
		if(refclass != Entity.class){
			getTrait(idTraits, traits, refclass);
			Class[] cs = refclass.getInterfaces();
			for(int i = 0 ; i < cs.length ; i++) initTraitsClass(idTraits, traits, cs[i]);
		}
	}
	private static void getTrait(List<Integer> idTraits, List<Class<? extends ITrait>> traits, Class refclass){
		if(refclass == Creeper.class && !idTraits.contains(CREEPER)){ idTraits.add(CREEPER); traits.add(CREEPER.class); }
		else if(refclass == Creature.class && !idTraits.contains(CREATURE)){ idTraits.add(CREATURE); traits.add(CREATURE.class); }
		else if(refclass == LivingEntity.class && !idTraits.contains(LIVINGENTITY)){ idTraits.add(LIVINGENTITY); traits.add(LIVINGENTITY.class); }
		else if(refclass == Damageable.class && !idTraits.contains(DAMAGEABLE)){ idTraits.add(DAMAGEABLE); traits.add(DAMAGEABLE.class); }
		else if(refclass == Fireball.class && !idTraits.contains(FIREBALL)){ idTraits.add(FIREBALL); traits.add(FIREBALL.class); }
		else if(refclass == Ageable.class && !idTraits.contains(AGEABLE)){ idTraits.add(AGEABLE); traits.add(AGEABLE.class); }
		else if(refclass == Slime.class && !idTraits.contains(SLIME)){ idTraits.add(SLIME); traits.add(SLIME.class); }
		else if(refclass == Tameable.class && !idTraits.contains(TAMEABLE)){ idTraits.add(TAMEABLE); traits.add(TAMEABLE.class); }
		else if(refclass == Zombie.class && !idTraits.contains(ZOMBIE)){ idTraits.add(ZOMBIE); traits.add(ZOMBIE.class); }
		else if(refclass == Explosive.class && !idTraits.contains(EXPLOSIVE)){ idTraits.add(EXPLOSIVE); traits.add(EXPLOSIVE.class); }
		else if(refclass == Colorable.class && !idTraits.contains(COLOR)){ idTraits.add(COLOR); traits.add(COLOR.class); }
		else if(refclass == PigZombie.class && !idTraits.contains(PIGZOMBIE)){ idTraits.add(PIGZOMBIE); traits.add(PIGZOMBIE.class); }
		else if(refclass == Horse.class && !idTraits.contains(HORSE)){ idTraits.add(HORSE); traits.add(HORSE.class); }
		else if(refclass == Sheep.class && !idTraits.contains(SHEEP)){ idTraits.add(SHEEP); traits.add(SHEEP.class); }
		else if(refclass == ExperienceOrb.class && !idTraits.contains(EXPERIENCEORB)){ idTraits.add(EXPERIENCEORB); traits.add(EXPERIENCEORB.class); }
		else if(refclass == Projectile.class && !idTraits.contains(PROJECTILE)){ idTraits.add(PROJECTILE); traits.add(PROJECTILE.class); }
		else if(refclass == Ocelot.class && !idTraits.contains(CAT)){ idTraits.add(CAT); traits.add(CAT.class); }
		else if(refclass == TNTPrimed.class && !idTraits.contains(TNTPRIMED)){ idTraits.add(TNTPRIMED); traits.add(TNTPRIMED.class); }
		else if(refclass == Skeleton.class && !idTraits.contains(SKELETON)){ idTraits.add(SKELETON); traits.add(SKELETON.class); }
		else if(refclass == Villager.class && !idTraits.contains(VILLAGER)){ idTraits.add(VILLAGER); traits.add(VILLAGER.class); }
		else if(refclass == Wolf.class && !idTraits.contains(WOLF)){ idTraits.add(WOLF); traits.add(WOLF.class); }
		else if(refclass == Guardian.class && !idTraits.contains(GUARDIAN)){ idTraits.add(GUARDIAN); traits.add(GUARDIAN.class); }
		else if(refclass == Rabbit.class && !idTraits.contains(RABBIT)){ idTraits.add(RABBIT); traits.add(RABBIT.class); }
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StatePopEntity(this, mc, driver); }
	// ------------------ StatePopPlayer ------------------
	class StatePopEntity extends AbsMechaStateEntity{
		private List<LivingEntity> pops = new ArrayList<LivingEntity>();
		private IntegerData nbPop = new IntegerData();
		private MechaCoordRelative coordR = new MechaCoordRelative();
		private IPopEntityState state;
		public StatePopEntity(Mechanism m, MechaControler mc, IMechaDriver driver){
			super(m, mc, driver);
			state = popEntity.getPopEntityState(); 
		}
		@Override
		public void cloneData() {
			super.cloneData();
			nbPop.clone(this, ActionPopEntity.this.nbPop);
			coordR.clone(this, ActionPopEntity.this.coordR);
			state.cloneData(this);
		}
		@Override
		public void start() {
			//super.start();
			isLaunched = false;
			if((!coordR.isNo() || coordR.getWorld() != null)){
				if(state.goPop(this)){
					for(int i = 0 ; i < nbPop.get() ; i++){
						Location loc = coordR.getLocationRandomRadius(driver);
						if(loc == null) return;
						state.goPop(this, loc);
					}
				}else return; // no pops
			}
			sendMessage();
			if(!isLaunched) launch();
		}
		private boolean isDeadPops(){ return isDead(pops); }
		@Override
		public void stop() {
			super.stop();
			state.stop(this);
		}
	}
	// --------------------------- ENTITY ---------------------------
	class ENTITY implements IPopEntity{
		private String nameEntityType = "";
		private EntityType et;
		private boolean onlyIfDead = true, cleanEntity = true;
		private StringData displayName = new StringData();
		private boolean isLaunchEntity = false;
		private boolean commonPop = false;
		private boolean bypassSpawnProtect = true;
		private MechaVelocity velocity = new MechaVelocity();
		private MechaDirectionRelative directR = new MechaDirectionRelative();
		private List<ITrait> traits = new ArrayList<ITrait>();
		@Override
		public int getId() { return ENTITY; }
		public ITrait getTrait(int id){ for(ITrait t : traits) if(t.getId() == id) return t; return null;
		}
		@Override
		public String getParams() {
			StringBuilder params = new StringBuilder().append(traits.size());
			for(ITrait t : traits) params.append(DELIMITER).append(t.getId()).append(DELIMITER).append(t.getParams());
			return new StringBuilder("7").append(DELIMITER).append(nameEntityType).append(DELIMITER).append(onlyIfDead).append(DELIMITER)
					.append(cleanEntity).append(DELIMITER).append(displayName).append(DELIMITER).append(velocity.getParams())
					.append(DELIMITER).append(isLaunchEntity).append(DELIMITER).append(commonPop).append(DELIMITER).append(bypassSpawnProtect)
					.append(DELIMITER).append(directR.getParams()).append(DELIMITER).append(params).toString();
		}
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			if(version < 4){
				nameEntityType = params[index++];
				onlyIfDead = Boolean.valueOf(params[index++]);
				cleanEntity = Boolean.valueOf(params[index++]);
				if(version >= 2) displayName.load(params[index++]);
				boolean direction = Boolean.valueOf(params[index++]);
				boolean dirPlayer = Boolean.valueOf(params[index++]);
				index = velocity.loadParams(index, params);
				float yaw = Float.valueOf(params[index++]);
				float pitch = Float.valueOf(params[index++]);
				traits.clear();
				loadTraits(nameEntityType, traits);
				int size = Integer.valueOf(params[index++]);
				for(int i = 0 ; i < size ; i++){
					ITrait t = getTrait(Integer.valueOf(params[index++]));
					if(t == null) break;
					index = t.loadParams(index, params);
				}
				if(version <= 2 && coordR.isNo()) commonPop = true;
				if(!nameEntityType.isEmpty()) et = EntityType.valueOf(nameEntityType);
				if(yaw < 0) yaw = 0; else if(yaw > 360) yaw = 360;
				if(pitch < -90) pitch = -90; else if(pitch > 90) pitch = 90;
				directR.setUseDirect(direction);
				directR.setDirection(yaw, pitch, dirPlayer, dirPlayer);
				directR.setOnPlayer();
				sqlSave();
			}else if(version >= 4){
				nameEntityType = params[index++];
				onlyIfDead = Boolean.valueOf(params[index++]);
				cleanEntity = Boolean.valueOf(params[index++]);
				displayName.load(params[index++]);
				index = velocity.loadParams(index, params);
				if(version >= 5) isLaunchEntity = Boolean.valueOf(params[index++]);
				commonPop = common;
				if(version >= 6) commonPop = Boolean.valueOf(params[index++]);
				if(version >= 7) bypassSpawnProtect = Boolean.valueOf(params[index++]);
				index = directR.loadParams(index, params);
				traits.clear();
				loadTraits(nameEntityType, traits);
				int size = Integer.valueOf(params[index++]);
				for(int i = 0 ; i < size ; i++){
					ITrait t = getTrait(Integer.valueOf(params[index++]));
					if(t == null) break;
					index = t.loadParams(index, params);
				}
				if(!nameEntityType.isEmpty()) et = EntityType.valueOf(nameEntityType);
			}
		}
		@Override
		public IPopEntityState getPopEntityState() {
			return new IPopEntityState() {
				private StringData displayName = new StringData();
				private MechaVelocity velocity = new MechaVelocity();
				private MechaDirectionRelative directR = new MechaDirectionRelative();
				private List<ITrait> traits = new ArrayList<ITrait>();
				@Override
				public void cloneData(StatePopEntity spp) {
					displayName.clone(spp, ENTITY.this.displayName);
					velocity.clone(spp, ENTITY.this.velocity);
					directR.clone(spp, ENTITY.this.directR);
					traits.clear();
					for(ITrait t : ENTITY.this.traits) traits.add(t.clone(spp));
				}
				@Override
				public boolean goPop(StatePopEntity spp) {
					if(commonPop) spp.setActive(true);
					if(onlyIfDead){
						if(commonPop) return isDead(ActionPopEntity.this.pops);
						else return spp.isDeadPops();
					}
					return true;
				}
				@Override
				public void goPop(StatePopEntity spp, Location loc) {
					isLaunched = isLaunchEntity;
					if(et != null){
						ActionPopEntity.bypassSpawnProtect = bypassSpawnProtect;
						Entity entity = loc.getWorld().spawnEntity(loc, et);
						ActionPopEntity.bypassSpawnProtect = false;
						for(ITrait t : traits) t.goTrait(spp.driver, loc, entity);
						if(entity instanceof LivingEntity && (onlyIfDead || cleanEntity)){
							if(commonPop) ActionPopEntity.this.pops.add((LivingEntity)entity);
							else spp.pops.add((LivingEntity)entity);
							spp.setActive(true);
						}
						entity.setCustomName(displayName.get());
						if(directR.useDirection()){
							Location direct = directR.getDirectionVelocity(spp.driver, loc);
				            entity.setVelocity(velocity.getVelocity(direct.getYaw(), direct.getPitch()));
						}
						if(isLaunchEntity) launch(entity);
					}
				}				
				@Override
				public void stop(StatePopEntity spp) {
					if(cleanEntity){
						if(commonPop){ if(!hasCurrentStatesActive()) cleanEntity(ActionPopEntity.this.pops); }
						else cleanEntity(spp.pops);
					}
				}
			};
		}
	}
	// --------------------------- MOBSPAWNER ---------------------------
	class MOBSPAWNER implements IPopEntity{
		public String nameEntityType = "";
		@Override
		public int getId() { return MOBSPAWNER; }
		@Override
		public String getParams() { return 1+DELIMITER+nameEntityType+DELIMITER+1; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			nameEntityType = params[index++];
		}
		@Override
		public IPopEntityState getPopEntityState() {
			return new IPopEntityState() {
				@Override
				public void cloneData(StatePopEntity spp) { }
				@Override
				public boolean goPop(StatePopEntity spp) { return true; }
				@Override
				public void goPop(StatePopEntity spp, Location loc) {
					if(!nameEntityType.isEmpty()){
						EntityType e = EntityType.valueOf(nameEntityType);
						if(e != null){
							Block b = loc.getWorld().getBlockAt(loc);
							if(b.getType() != Material.SPAWNER) b.setType(Material.SPAWNER);
							((CreatureSpawner)b.getState()).setSpawnedType(e);
						}
					}
				}
				@Override
				public void stop(StatePopEntity spp) { }
			};
		}
	}
	// --------------------------- EXPLOSION ---------------------------
	class EXPLOSION implements IPopEntity{
		public float power = 0;
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
			power = Float.valueOf(params[index++]);
			fire = Boolean.valueOf(params[index++]);
			breakBlocks = Boolean.valueOf(params[index++]);
		}
		@Override
		public IPopEntityState getPopEntityState() {
			return new IPopEntityState() {
				@Override
				public void cloneData(StatePopEntity spp) { }
				@Override
				public boolean goPop(StatePopEntity spp) { return true; }
				@Override
				public void goPop(StatePopEntity spp, Location loc) {
					loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), power, fire, breakBlocks);
				}
				@Override
				public void stop(StatePopEntity spp) { }
			};
		}
	}
	// *************************** DAMAGEABLE ***************************
	class DAMAGEABLE implements ITrait{
		public DoubleData health = new DoubleData(), maxHealth = new DoubleData();
		public DAMAGEABLE() { }
		@Override
		public int getId() { return DAMAGEABLE; }
		@Override
		public String getParams() { return 1+DELIMITER+health+DELIMITER+maxHealth; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			health.load(params[index++]);
			maxHealth.load(params[index++]);
			if(maxHealth.get() < 0) maxHealth.set(0d);
			if(maxHealth.get() != 0 && health.get() > maxHealth.get()) health.set(maxHealth.get());
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) {
			DAMAGEABLE dmg = new DAMAGEABLE();
			dmg.health.clone(spp, health);
			dmg.maxHealth.clone(spp, maxHealth);
			if(dmg.maxHealth.get() < 0) dmg.maxHealth.set(0d);
			if(dmg.maxHealth.get() != 0 && dmg.health.get() > dmg.maxHealth.get()) dmg.health.set(dmg.maxHealth.get());
			return dmg;
		}
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			Damageable d = (Damageable)e;
			if(maxHealth.get() > 0) d.setMaxHealth(maxHealth.get());
			if(health.get() > 0){
				if(health.get() > d.getMaxHealth()) d.setHealth(d.getMaxHealth());
				else d.setHealth(health.get());
			}
		}
	}
	// *************************** CREATURE ***************************
	class CREATURE implements ITrait{
		public boolean target = true;
		public CREATURE() { }
		@Override
		public int getId() { return CREATURE; }
		@Override
		public String getParams() { return 1+DELIMITER+target; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			target = Boolean.valueOf(params[index++]);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) { CREATURE c = new CREATURE(); c.target = target; return c; }
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			if(target && driver.hasEntity() && driver.getEntity() instanceof LivingEntity) ((Creature)e).setTarget((LivingEntity) driver.getEntity());
		}
	}
	// *************************** CREEPER ***************************
	class CREEPER implements ITrait{
		public boolean powered;
		public CREEPER() { }
		@Override
		public int getId() { return CREEPER; }
		@Override
		public String getParams() { return 1+DELIMITER+powered; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			powered = Boolean.valueOf(params[index++]);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) { CREEPER c = new CREEPER(); c.powered = powered; return c; }
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			((Creeper)e).setPowered(powered);
		}
	}
	// *************************** FIREBALL ***************************
	class FIREBALL implements ITrait{
		private MechaDirectionRelative directR = new MechaDirectionRelative();
		public FIREBALL() { }
		@Override
		public int getId() { return FIREBALL; }
		@Override
		public String getParams() { return 3+DELIMITER+directR.getParams(); }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			if(version < 3){
				boolean onPlayer = Boolean.valueOf(params[index++]);
				int X = Integer.valueOf(params[index++]);
				int Y = Integer.valueOf(params[index++]);
				int Z = Integer.valueOf(params[index++]);
				boolean onLocation = !onPlayer;
				if(onPlayer) directR.setOnLocationOnPlayer();
				else directR.setOnLocation(coordR.getWorldName(), X, Y, Z);
				directR.setDirection(0, 0, true, true);
				if(version == 1) return index;
				onLocation = Boolean.valueOf(params[index++]);
				boolean directionPlayer = Boolean.valueOf(params[index++]);
				if(onLocation) directR.setOnLocation(coordR.getWorldName(), X, Y, Z);
				else if(directionPlayer) directR.setOnPlayer();				
			}else if(version == 3) index = directR.loadParams(index, params);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) { FIREBALL fb = new FIREBALL(); fb.directR.clone(spp, directR); return fb; }
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
            Location direct = directR.getDirectionVelocity(driver, loc);
            ((Fireball) e).setDirection(MechaVelocity.convertVelocity(direct.getYaw(), direct.getPitch()));
		}
	}
	// *************************** AGEABLE ***************************
	class AGEABLE implements ITrait{
		public boolean baby, adult, setage, agelock;
		public IntegerData age = new IntegerData();
		public AGEABLE() { }
		@Override
		public int getId() { return AGEABLE; }
		@Override
		public String getParams() { return 1+DELIMITER+baby+DELIMITER+adult+DELIMITER+setage+DELIMITER+agelock+DELIMITER+age; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			baby = Boolean.valueOf(params[index++]);
			adult = Boolean.valueOf(params[index++]);
			setage = Boolean.valueOf(params[index++]);
			agelock = Boolean.valueOf(params[index++]);
			age.load(params[index++]);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) {
			AGEABLE a = new AGEABLE();
			a.baby = baby;
			a.adult = adult;
			a.setage = setage;
			a.agelock = agelock;
			a.age.clone(spp, age);
			return a;
		}
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			Ageable a = ((Ageable)e);
			if(baby) a.setBaby(); else if(adult) a.setAdult(); else if(setage) a.setAge(age.get());
			a.setAgeLock(agelock);
		}
	}
	// *************************** SLIME ***************************
	class SLIME implements ITrait{
		public IntegerData size = new IntegerData();
		public SLIME() { }
		@Override
		public int getId() { return SLIME; }
		@Override
		public String getParams() { return 1+DELIMITER+size; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			size.load(params[index++]);
			if(size.get() < 0) size.set(0);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) {
			SLIME s = new SLIME();
			s.size.clone(spp, size);
			if(s.size.get() < 0) s.size.set(0);
			return s;
		}
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			if(size.get() > 0) ((Slime)e).setSize(size.get());
		}
	}
	// *************************** TAMEABLE ***************************
	class TAMEABLE implements ITrait{
		public boolean playerTamer, isTamed;
		public TAMEABLE() { }
		@Override
		public int getId() { return TAMEABLE; }
		@Override
		public String getParams() { return 1+DELIMITER+playerTamer+DELIMITER+isTamed; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			playerTamer = Boolean.valueOf(params[index++]);
			isTamed = Boolean.valueOf(params[index++]);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) {
			TAMEABLE t = new TAMEABLE();
			t.playerTamer = playerTamer;
			t.isTamed = isTamed;
			return t;
		}
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			if(playerTamer && driver.hasEntity() && driver.getEntity() instanceof AnimalTamer) ((Tameable)e).setOwner((AnimalTamer) driver.getEntity());
			else if(isTamed) ((Tameable)e).setTamed(isTamed);
		}
	}
	// *************************** ZOMBIE ***************************
	class ZOMBIE implements ITrait{
		public boolean natural, baby, villager;
		public ZOMBIE() { }
		@Override
		public int getId() { return ZOMBIE; }
		@Override
		public String getParams() { return 2+DELIMITER+natural+DELIMITER+baby+DELIMITER+villager; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			if(version == 2) natural = Boolean.valueOf(params[index++]);
			baby = Boolean.valueOf(params[index++]);
			villager = Boolean.valueOf(params[index++]);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) {
			ZOMBIE z = new ZOMBIE();
			z.natural = natural;
			z.baby = baby;
			z.villager = villager;
			return z;
		}
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			if(natural){
				if(baby) ((Zombie)e).setBaby(baby);
				if(villager) ((Zombie)e).setVillager(villager);
			}else{
				((Zombie)e).setBaby(baby);
				((Zombie)e).setVillager(villager);
			}
		}
	}
	// *************************** EXPLOSIVE ***************************
	class EXPLOSIVE implements ITrait{
		public boolean incendiary;
		public FloatData radius = new FloatData();
		public EXPLOSIVE() { }
		@Override
		public int getId() { return EXPLOSIVE; }
		@Override
		public String getParams() { return 1+DELIMITER+incendiary+DELIMITER+radius; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			incendiary = Boolean.valueOf(params[index++]);
			radius.load(params[index++]);
			if(radius.get() < 0) radius.set(0f);
			return index;
		}@Override
		public ITrait clone(StatePopEntity spp) {
			EXPLOSIVE e = new EXPLOSIVE();
			e.incendiary = incendiary;
			e.radius.clone(spp, radius);
			if(e.radius.get() < 0) e.radius.set(0f);
			return e;
		}
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			((Explosive)e).setIsIncendiary(incendiary);
			((Explosive)e).setYield(radius.get());
		}
	}
	// *************************** COLOR ***************************
	class COLOR implements ITrait{
		public String color = "";
		public boolean randColor;
		private DyeColor dyeColor; 
		public COLOR() { }
		@Override
		public int getId() { return COLOR; }
		@Override
		public String getParams() { return 1+DELIMITER+color+DELIMITER+randColor; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			color = params[index++];
			randColor = Boolean.valueOf(params[index++]);
			if(!color.isEmpty()) dyeColor = DyeColor.valueOf(color);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) {
			COLOR c = new COLOR();
			c.color = color;
			c.randColor = randColor;
			c.dyeColor = dyeColor;
			return c;
		}
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			if(randColor) ((Colorable)e).setColor(DyeColor.values()[new Random().nextInt(DyeColor.values().length)]);
			else if(dyeColor != null) ((Colorable)e).setColor(dyeColor);
		}
	}
	// *************************** PIGZOMBIE ***************************
	class PIGZOMBIE implements ITrait{
		public boolean angry;
		public IntegerData level = new IntegerData();
		public PIGZOMBIE() { }
		@Override
		public int getId() { return PIGZOMBIE; }
		@Override
		public String getParams() { return 1+DELIMITER+angry+DELIMITER+level; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			angry = Boolean.valueOf(params[index++]);
			level.load(params[index++]);
			if(level.get() < 0) level.set(0);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) {
			PIGZOMBIE pz = new PIGZOMBIE();
			pz.angry = angry;
			pz.level.clone(spp, level);
			if(pz.level.get() < 0) pz.level.set(0);
			return pz;
		}
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			((PigZombie)e).setAngry(angry);
			if(angry) ((PigZombie)e).setAnger(level.get());
		}
	}
	// *************************** HORSE ***************************
	class HORSE implements ITrait{
		public boolean carryingChest, randColor, randStyle, randVariant;
		public IntegerData domestication = new IntegerData(), maxDomestication = new IntegerData();
		public String color = "", style = "", variant = "";
		public DoubleData jumpStrength = new DoubleData();
		private Horse.Color horseColor; Horse.Style horseStyle; Horse.Variant horseVariant;
		public HORSE() { }
		@Override
		public int getId() { return HORSE; }
		@Override
		public String getParams() { return new StringBuilder("1").append(DELIMITER).append(carryingChest).append(DELIMITER).append(domestication)
				.append(DELIMITER).append(maxDomestication).append(DELIMITER).append(color).append(DELIMITER).append(style).append(DELIMITER)
				.append(variant).append(DELIMITER).append(jumpStrength).append(DELIMITER).append(randColor).append(DELIMITER).append(randStyle)
				.append(DELIMITER).append(randVariant).toString(); }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			carryingChest = Boolean.valueOf(params[index++]);
			domestication.load(params[index++]);
			maxDomestication.load(params[index++]);
			color = params[index++];
			style = params[index++];
			variant = params[index++];
			jumpStrength.load(params[index++]);
			randColor = Boolean.valueOf(params[index++]);
			randStyle = Boolean.valueOf(params[index++]);
			randVariant = Boolean.valueOf(params[index++]);
			if(!color.isEmpty()) horseColor = Horse.Color.valueOf(color);
			if(!style.isEmpty()) horseStyle = Horse.Style.valueOf(style);
			if(!variant.isEmpty()) horseVariant = Horse.Variant.valueOf(variant);
			if(maxDomestication.get() <= 0) maxDomestication.set(1);
			if(domestication.get() < 0) domestication.set(0);
			else if(domestication.get() > maxDomestication.get()) domestication.set(maxDomestication.get());
			if(jumpStrength.get() > 2) jumpStrength.set(2d);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) {
			HORSE h = new HORSE();
			h.carryingChest = carryingChest;
			h.randColor = randColor;
			h.randStyle = randStyle;
			h.randVariant = randVariant;
			h.domestication.clone(spp, domestication);
			h.maxDomestication.clone(spp, maxDomestication);
			h.color = color;
			h.style = style;
			h.variant = variant;
			h.horseColor = horseColor;
			h.horseStyle = horseStyle;
			h.horseVariant = horseVariant;
			h.jumpStrength.clone(spp, jumpStrength);
			if(h.maxDomestication.get() <= 0) h.maxDomestication.set(1);
			if(h.domestication.get() < 0) h.domestication.set(0);
			else if(h.domestication.get() > h.maxDomestication.get()) h.domestication.set(h.maxDomestication.get());
			if(h.jumpStrength.get() > 2) h.jumpStrength.set(2d);
			return h;
		}
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			Horse h = ((Horse)e);
			h.setCarryingChest(carryingChest);
			h.setMaxDomestication(maxDomestication.get());
			h.setDomestication(domestication.get());
			h.setJumpStrength(jumpStrength.get());
			Random r = new Random();
			if(randColor) h.setColor(Horse.Color.values()[r.nextInt(Horse.Color.values().length)]); else if(horseColor != null) h.setColor(horseColor);
			if(randStyle) h.setStyle(Horse.Style.values()[r.nextInt(Horse.Style.values().length)]); else if(horseStyle != null) h.setStyle(horseStyle);
			if(randVariant) h.setVariant(Horse.Variant.values()[r.nextInt(Horse.Variant.values().length)]); else if(horseVariant != null) h.setVariant(horseVariant);
		}
	}
	// *************************** SHEEP ***************************
	class SHEEP implements ITrait{
		public boolean sheared;
		public SHEEP() { }
		@Override
		public int getId() { return SHEEP; }
		@Override
		public String getParams() { return 1+DELIMITER+sheared; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			sheared = Boolean.valueOf(params[index++]);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) { SHEEP s = new SHEEP(); s.sheared = sheared; return s; }
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) { ((Sheep)e).setSheared(sheared); }
	}
	// *************************** EXPERIENCEORB ***************************
	class EXPERIENCEORB implements ITrait{
		public IntegerData xp = new IntegerData();
		public EXPERIENCEORB() { }
		@Override
		public int getId() { return EXPERIENCEORB; }
		@Override
		public String getParams() { return 1+DELIMITER+xp; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			xp.load(params[index++]);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) { EXPERIENCEORB eo = new EXPERIENCEORB(); eo.xp.clone(spp, xp); return eo; }
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			((ExperienceOrb)e).setExperience(xp.get());
		}
	}
	// *************************** PROJECTILE ***************************
	class PROJECTILE implements ITrait{
		public boolean bounce, playerShooter;
		public PROJECTILE() { }
		@Override
		public int getId() { return PROJECTILE; }
		@Override
		public String getParams() { return 1+DELIMITER+bounce+DELIMITER+playerShooter; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			bounce = Boolean.valueOf(params[index++]);
			playerShooter = Boolean.valueOf(params[index++]);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) {
			PROJECTILE p = new PROJECTILE();
			p.bounce = bounce;
			p.playerShooter = playerShooter;
			return p;
		}
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			((Projectile)e).setBounce(bounce);
			if(playerShooter && driver.hasEntity() && driver.getEntity() instanceof ProjectileSource) ((Projectile)e).setShooter((ProjectileSource)driver.getEntity());
		}
	}
	// *************************** OCELOT ***************************
	class CAT implements ITrait{
		public String nameType = "";
		public boolean sitting, randType;
		private Type ocelotType;
		public CAT() { }
		@Override
		public int getId() { return CAT; }
		@Override
		public String getParams() { return 1+DELIMITER+nameType+DELIMITER+sitting+DELIMITER+randType; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			nameType = params[index++];
			sitting = Boolean.valueOf(params[index++]);
			randType = Boolean.valueOf(params[index++]);
			if(!nameType.isEmpty()) ocelotType = Type.valueOf(nameType);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) {
			CAT o = new CAT();
			o.nameType = nameType;
			o.sitting = sitting;
			o.randType = randType;
			o.ocelotType = ocelotType;
			return o;
		}
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			if(randType) ((Cat)e).setCatType(Type.values()[new Random().nextInt(Type.values().length)]);
			else if(ocelotType != null) ((Cat)e).setCatType(ocelotType);
			((Cat)e).setSitting(sitting);
		}
	}
	// *************************** TNTPRIMED ***************************
	class TNTPRIMED implements ITrait{
		public IntegerData fuseTicks = new IntegerData();
		public TNTPRIMED() { }
		@Override
		public int getId() { return TNTPRIMED; }
		@Override
		public String getParams() { return 1+DELIMITER+fuseTicks; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			fuseTicks.load(params[index++]);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) { TNTPRIMED tp = new TNTPRIMED(); tp.fuseTicks.clone(spp, fuseTicks); return tp; }
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			((TNTPrimed)e).setFuseTicks(fuseTicks.get());
		}
	}
	// *************************** SKELETON ***************************
	class SKELETON implements ITrait{
		public String nameType = "";
		public boolean randType;
		private SkeletonType skeletonType;
		public SKELETON() { }
		@Override
		public int getId() { return SKELETON; }
		@Override
		public String getParams() { return 1+DELIMITER+nameType+DELIMITER+randType; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			nameType = params[index++];
			randType = Boolean.valueOf(params[index++]);
			if(!nameType.isEmpty()) skeletonType = SkeletonType.valueOf(nameType);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) {
			SKELETON s = new SKELETON();
			s.nameType = nameType;
			s.randType = randType;
			s.skeletonType = skeletonType;
			return s;
		}
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			if(randType) ((Skeleton)e).setSkeletonType(SkeletonType.values()[new Random().nextInt(SkeletonType.values().length)]);
			else if(skeletonType != null) ((Skeleton)e).setSkeletonType(skeletonType);
		}
	}
	// *************************** VILLAGER ***************************
	class VILLAGER implements ITrait{
		public String nameType = "";
		public boolean randType;
		private Profession prof;
		public VILLAGER() { }
		@Override
		public int getId() { return VILLAGER; }
		@Override
		public String getParams() { return 1+DELIMITER+nameType+DELIMITER+randType; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			nameType = params[index++];
			randType = Boolean.valueOf(params[index++]);
			if(!nameType.isEmpty()) prof = Profession.valueOf(nameType);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) {
			VILLAGER v = new VILLAGER();
			v.nameType = nameType;
			v.randType = randType;
			v.prof = prof;
			return v;
		}
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			if(randType) ((Villager)e).setProfession(Profession.values()[new Random().nextInt(Profession.values().length)]);
			else if(prof != null) ((Villager)e).setProfession(prof);	
		}
	}
	// *************************** WOLF ***************************
	class WOLF implements ITrait{
		public String collarColor = "";
		public boolean randColor, angry, sitting;
		private DyeColor dyeColor;
		public WOLF() { }
		@Override
		public int getId() { return WOLF; }
		@Override
		public String getParams() { return 1+DELIMITER+collarColor+DELIMITER+randColor+DELIMITER+angry+DELIMITER+sitting; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			collarColor = params[index++];
			randColor = Boolean.valueOf(params[index++]);
			angry = Boolean.valueOf(params[index++]);
			sitting = Boolean.valueOf(params[index++]);
			if(!collarColor.isEmpty()) dyeColor = DyeColor.valueOf(collarColor);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) {
			WOLF w = new WOLF();
			w.collarColor = collarColor;
			w.randColor = randColor;
			w.angry = angry;
			w.sitting = sitting;
			w.dyeColor = dyeColor;
			return w;
		}
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			if(randColor) ((Wolf)e).setCollarColor(DyeColor.values()[new Random().nextInt(DyeColor.values().length)]);
			else if(dyeColor != null) ((Wolf)e).setCollarColor(dyeColor);
			((Wolf)e).setAngry(angry);
			((Wolf)e).setSitting(sitting);
		}
	}
	// *************************** GUARDIAN ***************************
	class GUARDIAN implements ITrait{
		public boolean shouldBeElder;
		public GUARDIAN() { }
		@Override
		public int getId() { return GUARDIAN; }
		@Override
		public String getParams() { return 1+DELIMITER+shouldBeElder; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			shouldBeElder = Boolean.valueOf(params[index++]);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) { GUARDIAN g = new GUARDIAN(); g.shouldBeElder = shouldBeElder; return g; }
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			((Guardian)e).setElder(shouldBeElder);
		}
	}
	// *************************** RABBIT ***************************
	class RABBIT implements ITrait{
		private String nameType = "";
		private boolean randType;
		private Rabbit.Type rabbitType;
		public RABBIT() { }
		@Override
		public int getId() { return RABBIT; }
		@Override
		public String getParams() { return 1+DELIMITER+nameType+DELIMITER+randType; }
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			nameType = params[index++];
			randType = Boolean.valueOf(params[index++]);
			if(!nameType.isEmpty()) rabbitType = Rabbit.Type.valueOf(nameType);
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) { RABBIT r = new RABBIT(); r.nameType = nameType; r.randType = randType; r. rabbitType = rabbitType; return r; }
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			if(randType) ((Rabbit)e).setRabbitType(Rabbit.Type.values()[new Random().nextInt(Rabbit.Type.values().length)]);
			else if(rabbitType != null) ((Rabbit)e).setRabbitType(rabbitType);
		}
	}
	// *************************** LIVINGENTITY ***************************
	class LIVINGENTITY implements ITrait{
		public List<MechaPotionEffect> effects = new ArrayList<MechaPotionEffect>();
		private MechaRandom mRand = new MechaRandom<MechaPotionEffect>();
		public EquipmentData[] equiDatas = new EquipmentData[]{new Helmet(), new Chestplate(), new Leggings(), new Boots(), new ItemInHand(),};
		public LIVINGENTITY() { }
		@Override
		public int getId() { return LIVINGENTITY; }
		@Override
		public String getParams() {
			StringBuilder params = new StringBuilder().append(effects.size());
			for(MechaPotionEffect mpe : effects) params.append(DELIMITER).append(mpe.getParams());
			params.append(DELIMITER).append(equiDatas.length);
			for(EquipmentData equi : equiDatas) params.append(DELIMITER).append(equi.type).append(DELIMITER).append(equi.idMat).append(DELIMITER).append(equi.dataMat).append(DELIMITER).append(equi.dropChance).append(DELIMITER).append(equi.enchant).append(DELIMITER).append(equi.enchantLvl).toString();
			return 2+DELIMITER+mRand.getParams()+DELIMITER+params;
		}
		@Override
		public int loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			effects.clear();
			mRand.clear();
			if(version == 1){
				int size = Integer.valueOf(params[index++]);
				for(int i = 0 ; i < size ; i++){
					MechaPotionEffect mpe = new MechaPotionEffect(mRand);
					mpe.setNameEffect(params[index++]);
					mpe.setDuration(Integer.valueOf(params[index++]));
					mpe.setAmplifier(Integer.valueOf(params[index++]));
					mRand.add(mpe);
					effects.add(mpe);
				}
				mRand.setIteration(effects.size());
			}else if(version == 2){
				index = mRand.load(index, params);
				int size = Integer.valueOf(params[index++]);
				for(int i = 0 ; i < size ; i++){
					MechaPotionEffect mpe = new MechaPotionEffect(mRand);
					index = mpe.load(index, params);
					mRand.add(mpe);
					effects.add(mpe);
				}
			}
			int size = Integer.valueOf(params[index++]);
			for(int i = 0 ; i < size ; i++){
				equiDatas[i].type = Integer.valueOf(params[index++]);
				equiDatas[i].idMat.load(params[index++]);
				equiDatas[i].dataMat.load(params[index++]);
				equiDatas[i].dropChance.load(params[index++]);
				equiDatas[i].enchant = params[index++];
				equiDatas[i].enchantLvl.load(params[index++]);
			}
			return index;
		}
		@Override
		public ITrait clone(StatePopEntity spp) {
			LIVINGENTITY le = new LIVINGENTITY();
			for(MechaPotionEffect mpe : effects) le.effects.add(mpe.clone(spp));
			le.mRand.clone(spp, mRand);
			for(MechaPotionEffect mpe : le.effects) le.mRand.add(mpe);
			for(int i = 0 ; i < equiDatas.length ; i++) le.equiDatas[i].clone(spp, equiDatas[i]);
			return le;
		}
		@Override
		public void goTrait(IMechaDriver driver, Location loc, Entity e) {
			LivingEntity le = (LivingEntity) e;
			List<MechaPotionEffect> list = mRand.getRandomList();
			for(MechaPotionEffect mpe : list){
				if(mpe.getEffectType() != null) le.addPotionEffect(mpe.getPotionEffect());
			}
			EntityEquipment equi = le.getEquipment();	
			for(EquipmentData eq : equiDatas) eq.set(le, equi);
		}
	}
	abstract class EquipmentData{
		public int type = 0;
		public StringData idMat = new StringData();
		public IntegerData dataMat = new IntegerData();
		public FloatData dropChance = new FloatData(0.185f);
		public String enchant = "";
		public IntegerData enchantLvl = new IntegerData();
		public EquipmentData() {}
		public abstract void set(LivingEntity le, EntityEquipment equi);
		protected ItemStack addItem(String mat, String enchant){
			ItemStack is = new ItemStack(Material.getMaterial(mat), 1, dataMat.get().shortValue());
			if(!enchant.isEmpty() && enchantLvl.get() > 0){
				Enchantment en = Enchantment.getByName(enchant);
				if(en.canEnchantItem(is) && enchantLvl.get() >= en.getStartLevel() && enchantLvl.get() <= en.getMaxLevel())
					is.addEnchantment(en, enchantLvl.get());
			}
			/*if(mat == 397){
				SkullMeta meta = (SkullMeta)is.getItemMeta();
			    meta.setOwner("Diwaly");
			    is.setItemMeta(meta);
			}*/
			return is;
		}
		public void clone(StatePopEntity spp, EquipmentData ed){
			type = ed.type;
			idMat.clone(spp, ed.idMat);
			dataMat.clone(spp, ed.dataMat);
			dropChance.clone(spp, ed.dropChance);
			enchant = ed.enchant;
			enchantLvl.clone(spp, ed.enchantLvl);
		}
	}
	
	class Helmet extends EquipmentData{
		public Helmet() { type = HELMET; }
		@Override
		public void set(LivingEntity le, EntityEquipment equi) {
			if(Material.getMaterial(idMat.get()) != Material.AIR){
				equi.setHelmetDropChance(dropChance.get());
				equi.setHelmet(addItem(idMat.get(), enchant)); }
		}
	}
	class Chestplate extends EquipmentData{
		public Chestplate() { type = CHESPLATE; }
		@Override
		public void set(LivingEntity le, EntityEquipment equi) {
			if(Material.getMaterial(idMat.get()) != Material.AIR){
				equi.setChestplateDropChance(dropChance.get());
				equi.setChestplate(addItem(idMat.get(), enchant)); }
		}
	}
	class Leggings extends EquipmentData{
		public Leggings() { type = LEGGINGS; }
		@Override
		public void set(LivingEntity le, EntityEquipment equi) {
			if(Material.getMaterial(idMat.get()) != Material.AIR){
				equi.setLeggingsDropChance(dropChance.get());
				equi.setLeggings(addItem(idMat.get(), enchant)); }
		}
	}
	class Boots extends EquipmentData{
		public Boots() { type = BOOTS; }
		@Override
		public void set(LivingEntity le, EntityEquipment equi) {
			if(Material.getMaterial(idMat.get()) != Material.AIR){
				equi.setBootsDropChance(dropChance.get());
				equi.setBoots(addItem(idMat.get(), enchant)); }
		}
	}
	class ItemInHand extends EquipmentData{
		public ItemInHand() { type = ITEMINHAND; }
		@Override
		public void set(LivingEntity le, EntityEquipment equi) {
			if(Material.getMaterial(idMat.get()) != Material.AIR){
				equi.setItemInHandDropChance(dropChance.get());
				equi.setItemInHand(addItem(idMat.get(), enchant)); }
		}
	}
	// ------------------ CmdPopEntity ------------------
	class CmdPopEntity extends AbsCYMCommand{
		public CmdPopEntity() { super(id); }
		@Override
		public void initChilds() { }
		@Override
		public void initActions() { addAction(new ENTITYDATA());  addAction(new MOBSPAWNERDATA()); addAction(new EXPLOSIONDATA()); addAction(new DAMAGEABLEDATA()); addAction(new LIVINGENTITYDATA()); addAction(new EXPLOSIVEDATA()); addAction(new COLORDATA()); addAction(new OCELOTDATA()); addAction(new SKELETONDATA()); addAction(new VILLAGERDATA()); addAction(new HORSEDATA()); addAction(new RABBITDATA()); }
		
		abstract class AbsCmdActionEffect extends AbsCYMCommandAction{
			private List<String> list;
			protected boolean initList, isCurrentEffect;
			private String params = "";
			public abstract List<String> getList();
			@Override
			public void initSend(Player p) { 
				isCurrentEffect = popEntity.getId() == getId();
				if(isCurrentEffect) params = popEntity.getParams();
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
				initList = readBool();
			}
			@Override
			public void receive(Player p) {
				sendCmdGui(p, this);
			}
		}
		class ENTITYDATA extends AbsCmdActionEffect{
			private List<List<Integer>> entityIdTraits;
			@Override
			public int getId() { return ENTITY; }
			@Override
			public AbsCYMCommandAction clone() { return new ENTITYDATA(); }
			@Override
			public List<String> getList() {
				List<String> list = new ArrayList<String>();
				entityIdTraits = new ArrayList<List<Integer>>();
				for(TraitsEntity te : traitsEntity){
					list.add(te.name);
					entityIdTraits.add(new ArrayList<Integer>(te.idTraitsEntity));
				}
				return list;
			}
			@Override
			public void sendWrite() throws IOException {
				super.sendWrite();
				if(initList){
					write(entityIdTraits.size());
					for(List<Integer> idt : entityIdTraits) writeListInt(idt);
				}
			}
		}
		class MOBSPAWNERDATA extends AbsCmdActionEffect{
			@Override
			public int getId() { return MOBSPAWNER; }
			@Override
			public AbsCYMCommandAction clone() { return new MOBSPAWNERDATA(); }
			@Override
			public List<String> getList() {
				List<String> list = new ArrayList<String>();
				for(TraitsEntity te : traitsEntity) list.add(te.name);
				return list;
			}
		}
		class EXPLOSIONDATA extends AbsCmdActionEffect{
			@Override
			public int getId() { return EXPLOSION; }
			@Override
			public AbsCYMCommandAction clone() { return new EXPLOSIONDATA(); }
			@Override
			public List<String> getList() {
				List<String> list = new ArrayList<String>();
				return list;
			}
		}
		
		abstract class AbsCmdDefaultTrait extends AbsCYMCommandAction{
			private String nameEntity = "";
			private String params = "";
			private DAMAGEABLE d;
			public abstract void initParams(Entity e, ITrait t);
			@Override
			public void initSend(Player p) {
				if(!nameEntity.isEmpty()){
					if(popEntity.getId() == ENTITY){
						ITrait t = getNewTrait(nameEntity, getId());
						if(t != null){
							World w = Bukkit.getWorlds().get(0);
							Entity e = w.spawnEntity(new Location(w, 0, 0, 0), EntityType.valueOf(nameEntity));
							initParams(e, t);
							params = t.getParams();
							e.remove();
						}
					}
				}
			}
			@Override
			public void sendWrite() throws IOException { write(params); }
			@Override
			public void receiveRead() throws IOException { nameEntity = readStr(); }
			@Override
			public void receive(Player p) { sendCmdGui(p, this); }
		}
		class DAMAGEABLEDATA extends AbsCmdDefaultTrait{
			@Override
			public int getId() { return DAMAGEABLE; }
			@Override
			public AbsCYMCommandAction clone() { return new DAMAGEABLEDATA(); }
			@Override
			public void initParams(Entity e, ITrait t) {
				((DAMAGEABLE)t).health.load(((Damageable)e).getHealth()+"");
				((DAMAGEABLE)t).maxHealth.load(((Damageable)e).getMaxHealth()+"");
			}
		}
		class EXPLOSIVEDATA extends AbsCmdDefaultTrait{
			@Override
			public int getId() { return EXPLOSIVE; }
			@Override
			public AbsCYMCommandAction clone() { return new EXPLOSIVEDATA(); }
			@Override
			public void initParams(Entity e, ITrait t) {
				((EXPLOSIVE)t).incendiary = ((Explosive)e).isIncendiary();
				((EXPLOSIVE)t).radius.load(((Explosive)e).getYield()+"");
			}
		}
		
		abstract class AbsCmdListTrait extends AbsCYMCommandAction{
			private List<String> list = new ArrayList<String>();
			public abstract List<String> getList();
			@Override
			public void initSend(Player p) { 
				list = getList();
			}
			@Override
			public void sendWrite() throws IOException { writeListStr(list); }
			@Override
			public void receiveRead() throws IOException { }
			@Override
			public void receive(Player p) {
				sendCmdGui(p, this);
			}
		}
		class COLORDATA extends AbsCmdListTrait{
			private List<String> list = new ArrayList<String>();
			@Override
			public int getId() { return COLOR; }
			@Override
			public AbsCYMCommandAction clone() { return new COLORDATA(); }
			@Override
			public List<String> getList() {
				List<String> list = new ArrayList<String>();
				for(DyeColor c : DyeColor.values()) list.add(c.name());
				return list;
			}
		}
		class OCELOTDATA extends AbsCmdListTrait{
			private List<String> list = new ArrayList<String>();
			@Override
			public int getId() { return CAT; }
			@Override
			public AbsCYMCommandAction clone() { return new OCELOTDATA(); }
			@Override
			public List<String> getList() {
				List<String> list = new ArrayList<String>();
				for(Type t : Type.values()) list.add(t.name());
				return list;
			}
		}
		class SKELETONDATA extends AbsCmdListTrait{
			private List<String> list = new ArrayList<String>();
			@Override
			public int getId() { return SKELETON; }
			@Override
			public AbsCYMCommandAction clone() { return new SKELETONDATA(); }
			@Override
			public List<String> getList() {
				List<String> list = new ArrayList<String>();
				for(SkeletonType t : SkeletonType.values()) list.add(t.name());
				return list;
			}
		}
		class VILLAGERDATA extends AbsCmdListTrait{
			private List<String> list = new ArrayList<String>();
			@Override
			public int getId() { return VILLAGER; }
			@Override
			public AbsCYMCommandAction clone() { return new VILLAGERDATA(); }
			@Override
			public List<String> getList() {
				List<String> list = new ArrayList<String>();
				for(Profession t : Profession.values()) list.add(t.getKey().toString());
				return list;
			}
		}
		
		class HORSEDATA extends AbsCYMCommandAction{
			private List<String> color = new ArrayList<String>();
			private List<String> style = new ArrayList<String>();
			private List<String> variant = new ArrayList<String>();
			@Override
			public int getId() { return HORSE; }
			@Override
			public AbsCYMCommandAction clone() { return new HORSEDATA(); }
			@Override
			public void initSend(Player p) { 
				for(Horse.Color c : Horse.Color.values()) color.add(c.name());
				for(Horse.Style s : Horse.Style.values()) style.add(s.name());
				for(Horse.Variant v : Horse.Variant.values()) variant.add(v.name());
			}
			@Override
			public void sendWrite() throws IOException { writeListStr(color); writeListStr(style); writeListStr(variant); }
			@Override
			public void receiveRead() throws IOException { }
			@Override
			public void receive(Player p) {
				sendCmdGui(p, this);
			}
		}
		
		class RABBITDATA extends AbsCmdListTrait{
			private List<String> list = new ArrayList<String>();
			@Override
			public int getId() { return RABBIT; }
			@Override
			public AbsCYMCommandAction clone() { return new RABBITDATA(); }
			@Override
			public List<String> getList() {
				List<String> list = new ArrayList<String>();
				for(Rabbit.Type t : Rabbit.Type.values()) list.add(t.name());
				return list;
			}
		}
		
		class LIVINGENTITYDATA extends AbsCYMCommandAction{
			private List<String> effectPotion = new ArrayList<String>();
			private List<String> helmet = new ArrayList<String>();
			private List<String> chestplate = new ArrayList<String>();
			private List<String> leggings = new ArrayList<String>();
			private List<String> boots = new ArrayList<String>();
			private List<String> itemInhand = new ArrayList<String>();
			@Override
			public int getId() { return LIVINGENTITY; }
			@Override
			public AbsCYMCommandAction clone() { return new LIVINGENTITYDATA(); }
			@Override
			public void initSend(Player p) {
				for(PotionEffectType pe : PotionEffectType.values()) if(pe != null) effectPotion.add(pe.getName());
				Map<String, List<String>> enchants = new HashMap<String, List<String>>();
				for(EnchantmentTarget et : EnchantmentTarget.values()) enchants.put(et.name(), new ArrayList<String>());
				for(Enchantment e : Enchantment.values()){
					EnchantmentTarget et = e.getItemTarget();
					if(et == null) et = EnchantmentTarget.valueOf("ALL");
					List<String> list = enchants.get(et.name());
					if(list != null) list.add(e.getName());
				}
				for(Entry<String, List<String>>entry : enchants.entrySet()){
					String typeEnch = entry.getKey();
					if(typeEnch.equals("ARMOR")){
						helmet.addAll(entry.getValue()); chestplate.addAll(entry.getValue()); leggings.addAll(entry.getValue()); boots.addAll(entry.getValue());
					}else if(typeEnch.equals("BOW") || typeEnch.equals("WEAPON") || typeEnch.equals("TOOL")){
						itemInhand.addAll(entry.getValue());
					}else if(typeEnch.equals("ARMOR_HEAD")){
						helmet.addAll(entry.getValue());
					}else if(typeEnch.equals("ARMOR_TORSO")){
						chestplate.addAll(entry.getValue());
					}else if(typeEnch.equals("ARMOR_FEET")){
						boots.addAll(entry.getValue());
					}else if(typeEnch.equals("ARMOR_LEGS")){
						leggings.addAll(entry.getValue());
					}else{
						helmet.addAll(entry.getValue()); chestplate.addAll(entry.getValue()); leggings.addAll(entry.getValue()); boots.addAll(entry.getValue());
						itemInhand.addAll(entry.getValue());
					}
				}
			}
			@Override
			public void sendWrite() throws IOException { writeListStr(effectPotion); writeListStr(helmet); writeListStr(chestplate); writeListStr(leggings); writeListStr(boots); writeListStr(itemInhand); }
			@Override
			public void receiveRead() throws IOException { }
			@Override
			public void receive(Player p) {
				sendCmdGui(p, this);
			}
		}
	}
	// --------------------------- TRAIT ---------------------------
	public interface ITrait{
		public int getId();
		public String getParams();
		public int loadParams(int index, String[] params);
		public ITrait clone(StatePopEntity spp);
		public void goTrait(IMechaDriver driver, Location loc, Entity e);
	}
	// --------------------------- POPENTITY ---------------------------
	public interface IPopEntity {
		public int getId();
		public String getParams();
		public void loadParams(int index, String[] params);
		public IPopEntityState getPopEntityState();
	}
	public interface IPopEntityState {
		public void cloneData(StatePopEntity spp);
		public boolean goPop(StatePopEntity spp);
		public void goPop(StatePopEntity spp, Location loc);
		public void stop(StatePopEntity spp);
	}
}
class TraitsEntity{
	public String name = "";
	public List<Class<? extends ActionPopEntity.ITrait>> classTraitsEntity = new ArrayList<Class<? extends ActionPopEntity.ITrait>>();
	public List<Integer> idTraitsEntity = new ArrayList<Integer>();
}
