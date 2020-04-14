package fr.craftyourmind.quest.mecha;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.MetadataValue;
import fr.craftyourmind.manager.CYMChecker;
import fr.craftyourmind.manager.CYMChecker.ICheckerEntity;
import fr.craftyourmind.quest.Plugin;
import fr.craftyourmind.quest.QuestPlayer;

public class MechaDriverEntity extends MechaDriver {

	private Entity entity;
	
	public MechaDriverEntity(Entity e, IMechaContainer con) {
		super(con);
		this.entity = e;
		List<MetadataValue> metas = e.getMetadata("driverEntity");
		if(metas.isEmpty()) e.setMetadata("driverEntity", new MetaEntity(this));
		else ((MetaEntity)metas.get(0)).add(this);
	}
	@Override
	public QuestPlayer getQuestPlayer() {
		return null;
	}
	@Override
	public Player getPlayer() {
		return null;
	}
	@Override
	public Entity getEntity() {
		//if(entity.isDead() || !entity.isValid()) return null;
		return entity;
	}
	@Override
	public ICheckerEntity getChecker() {
		return CYMChecker.getOrNewCheckerEntityMeta(entity);
	}
	@Override
	public boolean isPlayer() {
		return false;
	}
	@Override
	public boolean isEntity() {
		return true;
	}
	@Override
	public boolean hasPlayer() {
		return false;
	}
	@Override
	public boolean hasEntity() {
		//return !entity.isDead() && entity.isValid();
		return true;
	}
	@Override
	public void sqlStart(AbsMechaStateEntity smp) { }
	@Override
	public void sqlStop(AbsMechaStateEntity smp) { }
	@Override
	public void sendMessage(String msg) { }
	@Override
	public String getNameEntity() {
		if(entity.getType() == EntityType.PLAYER) return ((HumanEntity)entity).getName();
		else if(entity.getCustomName() == null) return entity.getType().getKey().toString().toLowerCase();
		return entity.getCustomName();
	}
	
	public static void onEntityDeath(EntityDeathEvent event) {
		onEntityDeath(event.getEntity());
	}
	
	public static void onEntityDeath(Entity e) {
		List<MetadataValue> metas = e.getMetadata("driverEntity");
		if(!metas.isEmpty()) ((MetaEntity)metas.get(0)).cleanStateMechas();
	}

	public static void onPluginEnableEvent() {
		for(World w : Bukkit.getWorlds()) for(LivingEntity e : w.getLivingEntities()) e.removeMetadata("driverEntity", Plugin.it);
	}

	public static void onPluginDisableEvent() {
		for(World w : Bukkit.getWorlds()){
			for(LivingEntity e : w.getLivingEntities()){
				List<MetadataValue> metas = e.getMetadata("driverEntity");
				if(!metas.isEmpty()){
					((MetaEntity)metas.get(0)).cleanStateMechas();
					e.remove();
				}
			}
		}
	}
}

class MetaEntity implements MetadataValue{
	
	private List<IMechaDriver> drivers = new ArrayList<IMechaDriver>();
	
	public MetaEntity(MechaDriverEntity d) { add(d); }

	public void add(MechaDriverEntity d) { drivers.add(d); }
	
	public void cleanStateMechas() {
		for(IMechaDriver d : drivers) d.cleanControllers();
		drivers.clear();
	}
	@Override
	public boolean asBoolean() { return false; }
	@Override
	public byte asByte() { return 0; }
	@Override
	public double asDouble() { return 0; }
	@Override
	public float asFloat() { return 0; }
	@Override
	public int asInt() { return 0; }
	@Override
	public long asLong() { return 0; }
	@Override
	public short asShort() { return 0; }
	@Override
	public String asString() { return ""; }
	@Override
	public org.bukkit.plugin.Plugin getOwningPlugin() { return Plugin.it; }
	@Override
	public void invalidate() { }
	@Override
	public Object value() { return this; }
}