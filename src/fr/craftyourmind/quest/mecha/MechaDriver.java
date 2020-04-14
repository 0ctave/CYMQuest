package fr.craftyourmind.quest.mecha;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import fr.craftyourmind.quest.QuestPlayer;

public abstract class MechaDriver implements IMechaDriver{

	private Entity entitySource;
	private Location loc;
	private Location direction;
	private IMechaContainer con;
	private List<MechaControler> controllers = new ArrayList<MechaControler>();
	private List<ContainerParams> conparams = new ArrayList<ContainerParams>();
	
	public MechaDriver(IMechaContainer con) {
		this.con = con;
		addConParams(con);
	}
	public MechaDriver(IMechaContainer con, IMechaParamSave mps) {
		this.con = con;
		addConParams(con, mps);
	}
	@Override
	public void addController(MechaControler mc){ controllers.add(mc); }
	@Override
	public void removeController(MechaControler mc){ controllers.remove(mc); }
	@Override
	public MechaControler getControler(Mechanism m) {
		for(MechaControler mc : controllers) if(mc.getId() == m.getId() && mc.getIdCon() == m.getIdCon() && mc.getTypeCon() == m.getTypeCon()) return mc;
		return new MechaControler(m, this);
	}
	@Override
	public IMechaContainer getContainer() { return con; }
	@Override
	public void addConParams(IMechaContainer con){
		addConParams(con, null);
	}
	@Override
	public void addConParams(IMechaContainer con, IMechaParamSave mps){
		for(ContainerParams cp : conparams) if(cp.con == con) return;
		conparams.add(new ContainerParams(con, mps));
	}
	@Override
	public void addConParams(IMechaContainer con, String name, MechaParam mp) {
		for(ContainerParams cp : conparams) if(cp.con == con) cp.addMechaParams(name, mp);
	}
	
	public void updateConParams(IMechaContainer con, Map<String, MechaParam> addmps, List<String> removemps){
		for(ContainerParams cp : conparams) if(cp.con == con) cp.updateConParams(addmps, removemps);
	}
	
	@Override
	public MechaParam getMechaParam(String param){
		for(ContainerParams cp : conparams){
			MechaParam mp = cp.getMechaParam(param);
			if(mp != null) return mp;
		}
		return null;
	}
	@Override
	public MechaParam getMechaParam(IMechaContainer con, String param){
		for(ContainerParams cp : conparams){
			if(cp.con == con){
				MechaParam mp = cp.getMechaParam(param);
				if(mp != null) return mp;
			}
		}
		return getMechaParam(param);
	}
	@Override
	public Map<String, MechaParam> getMechaParams(IMechaContainer con){
		for(ContainerParams cp : conparams) if(cp.con == con) return cp.getMechaParams();
		return null;
	}
	@Override
	public Map<String, MechaParam> getSaveMechaParams(IMechaContainer con){
		for(ContainerParams cp : conparams) if(cp.con == con) return cp.getSaveMechaParams();
		return null;
	}
	@Override
	public boolean hasCurrentStatesActive() {
		 for(MechaControler mc : controllers) if(mc.hasCurrentStatesActive()) return true; return false;
	}
	@Override
	public void cleanControllers() {
		for(MechaControler mc : controllers.toArray(new MechaControler[0])) mc.clean();
		controllers.clear();
	}
	@Override
	public Entity getEntitySource() { return entitySource; }
	@Override
	public Location getLocation() { return loc == null ? null : new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()); }
	@Override
	public Location getDirection() { return direction; }
	@Override
	public void setEntitySource(Entity e) { entitySource = e; }
	@Override
	public void setLocation(Location l) { loc = l; }
	@Override
	public void setDirection(Location d) { direction = d; }
	@Override
	public boolean hasEntitySource() { return entitySource != null; }
	@Override
	public boolean hasLocation() { return loc != null; }
	@Override
	public boolean hasDirection() { return direction != null; }

	public static IMechaDriver newDriver(IMechaContainer mcon, Entity e) {
		if(e.getType() == EntityType.PLAYER) return mcon.newDriver(QuestPlayer.get((Player)e));
		else return mcon.newDriver(e);
	}
	
	public static IMechaDriver newDriverGuest(IMechaContainer mcon, Entity e) {
		if(e.getType() == EntityType.PLAYER) return mcon.newDriverGuest(QuestPlayer.get((Player)e));
		else return mcon.newDriver(e);
	}
	
	class ContainerParams{
		private IMechaContainer con;
		private IMechaParamSave mps;
		private Map<String, MechaParam> mechaParams = new HashMap<String, MechaParam>();
		private Map<String, MechaParam> saveMechaParams = new HashMap<String, MechaParam>();
		public ContainerParams(IMechaContainer con, IMechaParamSave mps) {
			this.con = con;
			this.mps = mps;
			for(Entry<String, MechaParam> entry : con.getMechaParams().entrySet())
				iniMechaParams(entry.getKey(), entry.getValue());
		}
		public void updateConParams(Map<String, MechaParam> addmps, List<String> removemps) {
			for(String name : removemps) mechaParams.remove(name);
			for(String name : removemps) saveMechaParams.remove(name);
			for(Entry<String, MechaParam> entry : addmps.entrySet())
				iniMechaParams(entry.getKey(), entry.getValue());
			if(mps != null) mps.save();
		}
		private void iniMechaParams(String name, MechaParam mp) {
			saveMechaParams.remove(name);
			if(mp.isSave() && !mp.isSystem()){
				if(mp.isCommon()){
					if(mp.getMechaParamSave() == null) mp.setMechaParamSave(con.getMechaParamSave());
				}else{
					saveMechaParams.put(name, mp);
					if(mp.getMechaParamSave() == null) mp.setMechaParamSave(mps);
				}
			}
			mechaParams.put(name, mp);
		}
		public void addMechaParams(String name, MechaParam mp) {
			iniMechaParams(name, mp);
		}
		public MechaParam getMechaParam(String param){
			return mechaParams.get(param);
		}
		public Map<String, MechaParam> getMechaParams(){
			return mechaParams;
		}
		public Map<String, MechaParam> getSaveMechaParams(){
			return saveMechaParams;
		}
	}
}