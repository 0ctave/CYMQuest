package fr.craftyourmind.quest.mecha;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import fr.craftyourmind.quest.Plugin;
import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.QuestTools;

public class MechaContainer implements Runnable{

	public static final String DELIMITER = QuestTools.DELIMITER;
	
	private List<MechaContainerParam> conparamssys = new ArrayList<MechaContainerParam>();
	private List<MechaContainerParam> conparams = new ArrayList<MechaContainerParam>();
	private List<MechaContainerParam> conparamsUpdateAdd = new ArrayList<MechaContainerParam>();
	private List<MechaContainerParam> conparamsUpdateRemove = new ArrayList<MechaContainerParam>();
	
	private List<Mechanism> mechas = new ArrayList<Mechanism>();
	
	private List<IMechaDriver> players = new ArrayList<IMechaDriver>();
	private List<IMechaDriver> entities = new ArrayList<IMechaDriver>();
	private int idTask = 0;
	private IMechaContainer manager;
	private boolean updateParams;
	
	public MechaContainer() { }
	public MechaContainer(IMechaContainer mc) { manager = mc; }
	
	public List<Mechanism> getMechas() { return mechas; }

	public void removeMecha(Mechanism m) { mechas.remove(m); }

	public void addMecha(Mechanism m) { mechas.add(m); }

	public Mechanism getMecha(int id) {
		for(Mechanism m : mechas) if(m.id == id) return m; return null;
	}

	public Mechanism getMecha(String name) {
		for(Mechanism m : mechas) if(m.name.equals(name)) return m;
		return null;
	}
	
	public void clearMechas(){
		for(Mechanism m : mechas.toArray(new Mechanism[0])) m.sqlDelete();
		for(IMechaDriver d : players) d.cleanControllers(); players.clear();
		for(IMechaDriver d : entities) d.cleanControllers(); entities.clear();
		Bukkit.getScheduler().cancelTask(idTask);
		idTask = 0;
	}

	public IMechaDriver getDriverGuest(QuestPlayer qp) {
		for(IMechaDriver d : players) if(d.getPlayer() == qp.getPlayer()) return d;
		return null;
	}
	
	public IMechaDriver newDriverGuest(QuestPlayer qp, IMechaContainer con) {
		for(IMechaDriver d : players) if(d.getPlayer() == qp.getPlayer()) return d;
		IMechaDriver driver = new MechaDriverPlayer(qp, con);
		players.add(driver);
		if(idTask == 0) idTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Plugin.it, this, 200, 200);
		return driver;
	}
	
	public IMechaDriver newDriver(Entity e, IMechaContainer con) {
		for(IMechaDriver d : entities) if(d.getEntity() == e) return d;
		IMechaDriver driver = new MechaDriverEntity(e, con);
		entities.add(driver);
		if(idTask == 0) idTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Plugin.it, this, 200, 200);
		return driver;
	}
	
	public Map<String, MechaParam> getMechaParams(){
		Map<String, MechaParam> paramDatas = new HashMap<String, MechaParam>();
		for(MechaContainerParam mcp : conparams) paramDatas.put(mcp.name, mcp.getMechaParam());
		return paramDatas;
	}
	
	public MechaParam getMechaParam(String param){
		for(MechaContainerParam mcp : conparams) if(mcp.name.equals(param)) return mcp.getMechaParam();
		return null;
	}
	
	public List<String> getNameMechaParams(){
		List<String> nameParams = new ArrayList<String>();
		for(MechaContainerParam mcp : conparams) nameParams.add(mcp.name);
		return nameParams;
	}
	
	@Override
	public void run() {
		for(IMechaDriver d : players.toArray(new IMechaDriver[0])){
			if(!d.hasCurrentStatesActive()){
				d.cleanControllers();
				players.remove(d);
			}
		}
		for(IMechaDriver d : entities.toArray(new IMechaDriver[0])){
			if(!d.hasCurrentStatesActive() || d.getEntity().isDead() || !d.getEntity().isValid()){
				d.cleanControllers();
				entities.remove(d);
			}
		}
		if(players.isEmpty() && entities.isEmpty()){
			Bukkit.getScheduler().cancelTask(idTask);
			idTask = 0;
		}
	}

	public StringBuilder getParams() {
		StringBuilder sb = new StringBuilder("2").append(DELIMITER).append(conparams.size());
		for(MechaContainerParam mcp : conparams)
			sb.append(DELIMITER).append(mcp.isSystem).append(DELIMITER).append(mcp.name).append(DELIMITER) .append(mcp.descriptive).append(DELIMITER)
			.append(mcp.getParamDefault()).append(DELIMITER).append(mcp.isCommon).append(DELIMITER).append(mcp.isSave);
		return sb;
	}

	public int loadParams(int index, String[] params) {
		conparamsUpdateAdd.clear();
		conparamsUpdateRemove.clear();
		updateParams = !conparams.isEmpty();
		conparamsUpdateRemove.addAll(conparams);
		conparamsUpdateRemove.removeAll(conparamssys);
		int version = Integer.valueOf(params[index++]);
		if(version == 1){
			int size = Integer.valueOf(params[index++]);
			MechaContainerParam[] mcps = conparams.toArray(new MechaContainerParam[0]);
			conparams.clear();
			for(int i = 0 ; i < size ; i++){
				boolean isSystem = Boolean.valueOf(params[index++]);
				if(isSystem) index += 4;
				else{
					String name = params[index++];
					MechaContainerParam mcp = null;
					for(MechaContainerParam mc : mcps) if(mc.name.equals(name)){ mcp = mc; conparamsUpdateRemove.remove(mcp); break; }
					if(mcp == null){
						mcp = new MechaContainerParam(name);
						conparamsUpdateAdd.add(mcp);
					}
					mcp.descriptive = params[index++];
					mcp.setParamDefault(params[index++]);
					mcp.isCommon = Boolean.valueOf(params[index++]);
					conparams.add(mcp);
					if(manager.getTypeContainer() == AbsMechaContainer.SKILL || manager.getTypeContainer() == AbsMechaContainer.CLASS) // update
						mcp.isSave = true;
				}
			}
		}else if(version == 2){
			int size = Integer.valueOf(params[index++]);
			MechaContainerParam[] mcps = conparams.toArray(new MechaContainerParam[0]);
			conparams.clear();
			for(int i = 0 ; i < size ; i++){
				boolean isSystem = Boolean.valueOf(params[index++]);
				if(isSystem) index += 5;
				else{
					String name = params[index++];
					MechaContainerParam mcp = null;
					for(MechaContainerParam mc : mcps){
						if(mc.name.equals(name)){ mcp = mc; conparamsUpdateRemove.remove(mcp); break; }
					}
					if(mcp == null){
						mcp = new MechaContainerParam(name);
						//conparamsUpdateAdd.add(mcp);
					}
					conparamsUpdateAdd.add(mcp);
					mcp.descriptive = params[index++];
					mcp.setParamDefault(params[index++]);
					mcp.isCommon = Boolean.valueOf(params[index++]);
					mcp.isSave = Boolean.valueOf(params[index++]);
					conparams.add(mcp);
				}
			}
		}
		updateOnAllState();
		conparams.addAll(0, conparamssys);
		return index;
	}

	public void addParamSys(MechaContainerParam param) {
		conparams.add(0, param);
		conparamssys.add(0, param);
	}
	
	private void updateOnAllState(){
		if(updateParams && manager != null && (!conparamsUpdateAdd.isEmpty() || !conparamsUpdateRemove.isEmpty())){
			Map<String, MechaParam> addmps = new HashMap<String, MechaParam>();
			List<String> removemps = new ArrayList<String>();
			//conparamsUpdateAdd.addAll(conparamssys);
			for(MechaContainerParam mcp : conparamsUpdateAdd) addmps.put(mcp.name, mcp.getMechaParam());
			for(MechaContainerParam mcp : conparamsUpdateRemove) removemps.add(mcp.name);
			manager.updateConParams(addmps, removemps);
		}
		conparamsUpdateAdd.clear();
		conparamsUpdateRemove.clear();
	}
}