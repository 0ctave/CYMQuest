package fr.craftyourmind.quest.mecha;

import java.util.Map;
import java.util.Map.Entry;

import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.QuestTools;

public abstract class AbsStateContainer implements IMechaParamManager {
	public static final String DELIMITER = QuestTools.DELIMITER;
	protected IMechaContainer con;
	protected QuestPlayer qp;
	protected IMechaDriver driver;
	private boolean activated;
	protected IMechaParamSave mps = new IMechaParamSave() {
		@Override public void save() { updateStateSql(); } };
	public AbsStateContainer(IMechaContainer con, QuestPlayer qp) {
		this.con = con;
		this.qp = qp;
		//con.addState(qp, this);
		driver = new MechaDriverPlayer(qp, con, mps);
	}
	public abstract void cloneData();
	public boolean activate(){
		//if(activated || !con.isActivated()) return false;
		activated = true;
		updateStateSql();
		cloneData();
		return true;
	}
	public boolean deactivate(){
		if(!activated) return false;
		activated = false;
		updateStateSql();
		driver.cleanControllers();
		return true;
	}
	public boolean isActivated() {
		return activated;
	}
	public void setActivate(boolean activated) {
		this.activated = activated;
	}
	public int getId() {
		return con.getId();
	}
	public String getName() {
		return con.getName();
	}
	public IMechaDriver getDriver(){
		return driver;
	}
	public QuestPlayer getQuestPlayer(){
		return qp;
	}
	public abstract void createStateSql();
	public abstract void updateStateSql();
	public abstract void deleteStateSql();
	
	public void clean(){
		deactivate();
	}
	
	public void erase(){
		clean();
		deleteStateSql();
		con.removeState(qp);
	}
	
	@Override
	public MechaParam getMechaParam(String param){
		return driver.getMechaParam(con, param);
	}
	
	public String getParams() {
		StringBuilder sb = new StringBuilder("0").append(DELIMITER);
		Map<String, MechaParam> conparams = driver.getSaveMechaParams(con);
		sb.append(conparams.size());
		for(Entry<String, MechaParam> entry : conparams.entrySet())
			sb.append(DELIMITER).append(entry.getKey()).append(DELIMITER).append(entry.getValue());
		
		return sb.toString();
	}
	public void setParams(String str){
		if(str != null && !str.isEmpty()){
			Map<String, MechaParam> conparams = driver.getMechaParams(con);
			String[] params = str.split(DELIMITER);
			int index = 0;
			int version = Integer.valueOf(params[index++]);
			int size = Integer.valueOf(params[index++]);
			for(int i = 0 ; i < size ; i++){
				String param = params[index++];
				String value = index < params.length ? params[index++] : ""; // last value empty
				//String value = params[index++];
				MechaParam mp = conparams.get(param);
				if(mp != null) mp.setStrOnly(value);
			}
		}
	}
	public boolean canActivate() {
		return true;
	}
}