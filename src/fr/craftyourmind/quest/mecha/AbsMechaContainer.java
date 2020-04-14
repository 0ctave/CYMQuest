package fr.craftyourmind.quest.mecha;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import fr.craftyourmind.quest.EventParadise;
import fr.craftyourmind.quest.MekaBox;
import fr.craftyourmind.quest.Quest;
import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.QuestTools;
import fr.craftyourmind.skill.CYMClass;
import fr.craftyourmind.skill.CYMSkill;

public abstract class AbsMechaContainer<S extends AbsStateContainer> implements IMechaContainer{

	// ---- type driver container ----
	public static final int QUEST = 0;
	public static final int EVENT = 1;
	public static final int MEKABOX = 2;
	public static final int TOOLBOX = 3;
	public static final int CLASS = 4;
	public static final int SKILL = 5;
	public static final int LEVEL = 6;
	public static final int TIER = 7;
	
	protected static final String DELIMITER = QuestTools.DELIMITER;
	
	private Map<QuestPlayer, S> states = new HashMap<QuestPlayer, S>();
	private MechaContainer mcon = new MechaContainer(this);
	private boolean activated;
	private IMechaParamSave mps = new IMechaParamSave() { @Override public void save() { } };
	
	public void activate(QuestPlayer qp){
		activate(getStateOrCreate(qp));
	}
	
	public void activate(S sc){
		sc.activate();
	}
	
	public void deactivate(QuestPlayer qp){
		S sc = states.get(qp);
		if(sc != null) deactivate(sc);
	}
	
	public void deactivate(S sc){
		sc.deactivate();
	}
	@Override
	public boolean isActivated() {
		return activated;
	}
	
	public void setActivate(boolean activated) {
		this.activated = activated;
	}
	
	public void initActivate(){
		if(activated) for(S state : states.values()) state.activate();
		else for(S state : states.values()) state.deactivate();
	}
	
	public void updateCloneData(){
		for(S state : states.values()) state.cloneData();
	}
	
	public void initParamSys(){ }
	
	public void addState(QuestPlayer qp, S sc){
		states.put(qp, sc);
	}
	
	public Collection<S> getStates(){
		return states.values();
	}
	@Override
	public S getStateOrCreate(QuestPlayer qp){
		S sc = states.get(qp);
		if(sc == null) sc = createStateContainer(qp);
		return sc;
	}
	@Override
	public S getState(QuestPlayer qp){
		return states.get(qp);
	}
	
	@Override
	public S createStateContainer(QuestPlayer qp) {
		S sc = newStateContainer(qp);
		sc.cloneData();
		sc.createStateSql();
		states.put(qp, sc);
		return sc;
	}
	@Override
	public S loadStateContainer(QuestPlayer qp) {
		S sc = newStateContainer(qp);
		states.put(qp, sc);
		return sc;
	}
	@Override
	public S newStateContainer(QuestPlayer qp) {
		return null;
	}
	
	public void cleanStates(){
		for(S state : states.values()) state.clean();
	}
	public void clearStates(){
		states.clear();
	}
	public void removeState(QuestPlayer qp){
		states.remove(qp);
	}
	public void erase(QuestPlayer qp){
		S s = getState(qp);
		if(s != null) s.erase();
	}
	@Override
	public String getParamsCon(){
		return new StringBuilder("1").append(DELIMITER).append(mcon.getParams()).toString();
	}
	@Override
	public int loadParamsCon(String[] params){
		if(params[0].isEmpty()) return 0;
		int version = Integer.valueOf(params[0]);
		if(version == 0) return 1;
		return mcon.loadParams(1, params);
	}
	@Override
	public void loadParamsCon(String params){
		if(params != null){
			String[] str = params.split(DELIMITER);
			loadParamsCon(str);
		}
	}
	
	public void addParamSys(String name, MechaParam param, String desc) {
		addParamSys(name, param, desc, true, true);
	}
	
	public void addParamSys(String name, MechaParam param, String desc, boolean sys, boolean common) {
		param.setSystem(sys); param.setCommon(common);
		MechaContainerParam mcp = new MechaContainerParam(name);
		mcp.setParam(param);
		mcp.setDescriptive(desc);
		mcp.setSystem(sys);
		mcp.setCommon(common);
		mcon.addParamSys(mcp);
	}
	@Override
	public List<Mechanism> getMechas() { return mcon.getMechas(); }
	@Override
	public void removeMecha(Mechanism m) { mcon.removeMecha(m); }
	@Override
	public void addMecha(Mechanism m) { mcon.addMecha(m); }
	@Override
	public Mechanism getMecha(int id) { return mcon.getMecha(id); }
	@Override
	public Mechanism getMecha(String name) { return mcon.getMecha(name); }
	@Override
	public void clearMechas(){ mcon.clearMechas(); }
	@Override
	public IMechaDriver getDriver(QuestPlayer qp) { S s = getState(qp); return s == null ? null : s.driver; }
	@Override
	public IMechaDriver newDriver(QuestPlayer qp) { return getStateOrCreate(qp).driver; }
	@Override
	public IMechaDriver newDriverGuest(QuestPlayer qp) { return mcon.newDriverGuest(qp, this); }
	@Override
	public IMechaDriver getDriverGuest(QuestPlayer qp) { return mcon.getDriverGuest(qp); }
	@Override
	public IMechaDriver newDriver(Entity e) { return mcon.newDriver(e, this); }
	@Override
	public Map<String, MechaParam> getMechaParams() { return mcon.getMechaParams(); }
	@Override
	public MechaParam getMechaParam(String param) { return mcon.getMechaParam(param); }
	@Override
	public List<String> getNameMechaParams() { return mcon.getNameMechaParams(); }
	@Override
	public void addConParams(IMechaDriver driver) { }
	@Override
	public void updateConParams(Map<String, MechaParam> addmps, List<String> removemps) {
		for(S s : getStates()) s.getDriver().updateConParams(this, addmps, removemps);
	}
	@Override
	public IMechaParamSave getMechaParamSave() { return mps; }
	@Override
	public String getMechaParamPlayers(String name) {
		StringBuilder sb = new StringBuilder();
		sb.append(getStates().size());
		for(S s : getStates()){
			MechaParam mp = s.getMechaParam(name);
			sb.append(DELIMITER).append(s.qp.getId()).append(DELIMITER).append(s.qp.getName()).append(DELIMITER).append(mp == null ? "" : mp.getStr());
		}
		return sb.append(DELIMITER).append(0).toString();
	}
	
	@Override
	public void updateMechaParamPlayer(String name, QuestPlayer qp, String value) {
		S s = getState(qp);
		if(s != null){
			MechaParam mp  = s.getMechaParam(name);
			if(mp != null && !mp.isSystem()) mp.setStr(value);
		}
	}
	@Override
	public void updateMechaParamAll(String name, String value) {
		for(S s : getStates()){
			MechaParam mp  = s.getMechaParam(name);
			if(mp != null && !mp.isSystem()) mp.setStr(value);
		}
	}
	
	public static IMechaContainer get(int typeContainer, int idContainer, int... ids){
		if(typeContainer == QUEST) return Quest.get(idContainer);
		else if (typeContainer == EVENT) return EventParadise.get(idContainer);
		else if (typeContainer == MEKABOX) return MekaBox.get(idContainer);
		else if (typeContainer == SKILL){
			if(ids.length == 1) return CYMSkill.getSkill(idContainer, ids[0]);
			else return CYMSkill.get(idContainer);
		}else if (typeContainer == CLASS) return CYMClass.getCYMClass(idContainer);
		return null;
	}
}