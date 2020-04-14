package fr.craftyourmind.skill;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import fr.craftyourmind.quest.MekaBox;
import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.mecha.IMechaDriver;
import fr.craftyourmind.quest.mecha.IMechaParamSave;
import fr.craftyourmind.quest.mecha.Mechanism;
import fr.craftyourmind.quest.mecha.StarterTier;
import fr.craftyourmind.quest.sql.QuestSQLManager;
import fr.craftyourmind.skill.SkillManager.Descriptive;

public class CYMSkill extends MekaBox<CYMClass, StateCYMSkill> implements ISkillManager{

	private static List<CYMSkill> skills = new ArrayList<CYMSkill>();
	private IMechaParamSave mps = new IMechaParamSave() { @Override public void save() { CYMSkill.this.save(); } };
	
	public static CYMSkill getSkill(int idskill, int idclass) {
		CYMClass mc = CYMClass.getCYMClass(idclass);
		if(mc != null) return mc.getMekabox(idskill);
		return get(idskill);
	}
	
	public static CYMSkill get(CYMClass mc, String name){
		for(CYMSkill ms : mc.getMekaboxs()) if(name.equalsIgnoreCase(ms.name.getStr())) return ms; return null;
	}
	
	public static CYMSkill get(String name){
		for(CYMSkill ms : skills) if(name.equalsIgnoreCase(ms.name.getStr())) return ms; return null;
	}
	
	public static CYMSkill get(int idskill) {
		for(CYMSkill ms : skills) if(ms.getId() == idskill) return ms; return null;
	}
	
	public static CYMSkill newCYMSkillSql(int idskill, String name, int idclass){
		CYMSkill ms = newCYMSkill(name, idclass);
		ms.setId(idskill);
		return ms;
	}
	
	public static CYMSkill newCYMSkillCmd(String name, int idclass){
		CYMSkill ms = newCYMSkill(name, idclass);
		ms.create();
		ms.createStarter();
		ms.initStarter();
		ms.initParamSys();
		return ms;
	}
	
	public static CYMSkill newCYMSkill(String name, int idclass){
		CYMSkill ms = new CYMSkill(name);
		ms.setCat(idclass);
		skills.add(ms);
		return ms;
	}
	
	public int levelClassActivated;
	private SkillManager skillManager;
	
	public CYMSkill(String name) {
		super(SKILLBOX, name);
		skillManager = new SkillManager(this);
	}
	@Override
	public void init() {
		skillManager.init();
	}
	public void initStarter(){ initStarterBox(); }
	@Override
	public void initParamSys() {
		skillManager.initParamSys();
		addParamSys("skillName", name, "Skill name.");
		super.initParamSys();
	}
	@Override
	public IMechaDriver getDriver(QuestPlayer qp) {
		StateCYMClass smc = qp.getCYMClass(getCatId());
		if(smc != null){
			StateCYMSkill sms = smc.getStateCYMSkill(getId());
			return sms == null ? null : sms.getDriver();
		}
		return null;
	}
	@Override
	public IMechaParamSave getMechaParamSave() { return mps; }
	@Override
	public ISkillManager getLink(int idlink) { return getCatbox().getMekabox(idlink); }
	@Override
	public List<? extends ISkillManager> getAllLinks() { return getCatbox().getMekaboxs(); }
	@Override
	public StateCYMSkill newStateContainer(QuestPlayer qp) { return new StateCYMSkill(this, qp); }
	@Override
	public void create() {
		QuestSQLManager.create(this);
	}
	@Override
	public void save() {
		QuestSQLManager.save(this);
	}
	@Override
	public void delete() {
		clearMechas();
		skills.remove(this);
		getCatbox().removeMekabox(this);
		clearStates();
		remove(this);
		questSort.deleteOrder();
		QuestSQLManager.delete(this);
	}
	@Override
	public StateCYMSkill getStateOrCreate(QuestPlayer qp) {
		StateCYMSkill sms = super.getStateOrCreate(qp);
		sms.updateAll();
		return sms;
	}
	
	@Override
	public void updateCloneData() {
		for(StateCYMSkill sms : getStates()){
			Player p = sms.getQuestPlayer().getPlayer();
			if(p != null && p.isOnline())
				sms.cloneData();
		}
	}
	
	public void updateLevelClassActivated(){
		for(StateCYMSkill sms : getStates()){
			Player p = sms.getQuestPlayer().getPlayer();
			if(p != null && p.isOnline())
				sms.canActivate();
		}
	}
	
	public void updateLinks() {
		for(StateCYMSkill sms : getStates()){
			Player p = sms.getQuestPlayer().getPlayer();
			if(p != null && p.isOnline())
				sms.updateLinks();
		}
	}
	
	public void checkLinks() {
		for(StateCYMSkill sms : getStates()){
			Player p = sms.getQuestPlayer().getPlayer();
			if(p != null && p.isOnline())
				sms.checkLinks();
		}
	}
	
	public void updateNodes() {
		for(StateCYMSkill sms : getStates()){
			Player p = sms.getQuestPlayer().getPlayer();
			if(p != null && p.isOnline())
				sms.updateNodes();
		}
	}
	
	public SkillManager getSkillManager(){ return skillManager; }
	@Override
	public int getTypeContainer() { return SKILL; }
	@Override
	public boolean isKeepEnableOnLink() { return skillManager.isKeepEnableOnLink(); }
	@Override
	public void setKeepEnableOnLink(boolean b) { skillManager.setKeepEnableOnLink(b); }
	@Override
	public boolean isShowPlayer() { return skillManager.isShowPlayer(); }
	@Override
	public void setShowPlayer(boolean b) { skillManager.setShowPlayer(b); }
	@Override
	public boolean isActivationPlayer() { return skillManager.isActivationPlayer(); }
	@Override
	public void setActivationPlayer(boolean b) { skillManager.setActivationPlayer(b); }
	@Override
	public boolean isShowMessage() { return skillManager.isShowMessage(); }
	@Override
	public void setShowMessage(boolean b) { skillManager.setShowMessage(b); }
	@Override
	public String getDescriptives() { return skillManager.getDescriptives(); }
	@Override
	public void setDescriptives(String str) { skillManager.setDescriptives(str); }
	@Override
	public List<Descriptive> getDescriptivesList(){ return skillManager.getDescriptivesList(); }
	@Override
	public String getLinkParentsStr() { return skillManager.getLinkParentsStr(); }
	@Override
	public void setLinkParents(String str) { skillManager.setLinkParents(str); }
	@Override
	public void addLinkParent(ISkillManager sm) { skillManager.addLinkParent(sm); }
	@Override
	public void removeLinkParent(ISkillManager sm) { skillManager.removeLinkParent(sm); }
	@Override
	public List<ISkillManager> getLinkParents() { return skillManager.getLinkParents(); }
	@Override
	public String getLinkChildsStr() { return skillManager.getLinkChildsStr(); }
	@Override
	public void setLinkChilds(String str) { skillManager.setLinkChilds(str); }
	@Override
	public void addLinkChild(ISkillManager sm) { skillManager.addLinkChild(sm); }
	@Override
	public void removeLinkChild(ISkillManager sm) { skillManager.removeLinkChild(sm); }
	@Override
	public List<ISkillManager> getLinkChilds() { return skillManager.getLinkChilds(); }
	@Override
	public int getLevelLimit() { return skillManager.getLevelLimit(); }
	@Override
	public CYMLevel getLevel(int lvl) { return skillManager.getLevel(lvl); }
	@Override
	public String getLevels() { return skillManager.getLevels(); }
	@Override
	public void setLevels(String str) { skillManager.setLevels(str); }
	@Override
	public void updateLevel() {
		for(StateCYMSkill sms : getStates()){
			Player p = sms.getQuestPlayer().getPlayer();
			if(p != null && p.isOnline())
				sms.updateLevel();
		}
	}
	@Override
	public int getTierLimit() { return skillManager.getTierLimit(); }
	@Override
	public List<CYMTier> getCYMTiers() { return skillManager.getCYMTiers(); }
	@Override
	public String getTiers() { return skillManager.getTiers(); }
	@Override
	public void setTiers(String str) { skillManager.setTiers(str); }
	@Override
	public int getIdTier(int level) { return skillManager.getIdTier(level); }
	@Override
	public int getTier(int level) { return skillManager.getTier(level); }
	@Override
	public void updateTier() {
		for(StateCYMSkill sms : getStates()){
			Player p = sms.getQuestPlayer().getPlayer();
			if(p != null && p.isOnline())
				sms.updateTier();
		}
	}
	@Override
	public String getNodeParentsStr() { return skillManager.getNodeParentsStr(); }
	@Override
	public void setNodeParents(String str) { skillManager.setNodeParents(str); }
	@Override
	public void addNodeParent(ISkillManager sm) { skillManager.addNodeParent(sm); }
	@Override
	public void removeNodeParent(ISkillManager sm) { skillManager.removeNodeParent(sm); }
	@Override
	public List<ISkillManager> getNodeParents() { return skillManager.getNodeParents(); }
	@Override
	public String getNodeChildsStr() { return skillManager.getNodeChildsStr(); }
	@Override
	public void setNodeChilds(String str) { skillManager.setNodeChilds(str); }
	@Override
	public void addNodeChild(ISkillManager sm) { skillManager.addNodeChild(sm); }
	@Override
	public void removeNodeChild(ISkillManager sm) { skillManager.removeNodeChild(sm); }
	@Override
	public List<ISkillManager> getNodeChilds() { return skillManager.getNodeChilds(); }
	@Override
	public int getLimitNode() { return skillManager.getLimitNode(); }
	@Override
	public void setLimitNode(int limit) { skillManager.setLimitNode(limit); };
	@Override
	public boolean isSyncNodeParents() { return skillManager.isSyncNodeParents(); }
	@Override
	public void setSyncNodeParents(boolean b) { skillManager.setSyncNodeParents(b); }
	@Override
	public Mechanism getStarterActivate() { return skillManager.getStarterActivate(); }
	@Override
	public void setStarterActivate(Mechanism m) { skillManager.setStarterActivate(m); }
	@Override
	public Mechanism getStarterDeactivate() { return skillManager.getStarterDeactivate(); }
	@Override
	public void setStarterDeactivate(Mechanism m) { skillManager.setStarterDeactivate(m); }
	@Override
	public Mechanism getStarterEachLevel() { return skillManager.getStarterEachLevel(); }
	@Override
	public void setStarterEachLevel(Mechanism m) { skillManager.setStarterEachLevel(m); }
	@Override
	public Mechanism getStarterEachTier() { return skillManager.getStarterEachTier(); }
	@Override
	public void setStarterEachTier(Mechanism m) { skillManager.setStarterEachTier(m); }
	@Override
	public List<StarterTier> getStarterSelectTier() { return skillManager.getStarterSelectTier(); }
	@Override
	public void setStarterSelectTier(StarterTier statier) { skillManager.setStarterSelectTier(statier); }
}