package fr.craftyourmind.skill;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import fr.craftyourmind.quest.CatBox;
import fr.craftyourmind.quest.IMekaBox;
import fr.craftyourmind.quest.MekaBox;
import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.mecha.IMechaDriver;
import fr.craftyourmind.quest.mecha.IMechaParamSave;
import fr.craftyourmind.quest.mecha.Mechanism;
import fr.craftyourmind.quest.mecha.StarterTier;
import fr.craftyourmind.quest.sql.QuestSQLManager;
import fr.craftyourmind.skill.SkillManager.Descriptive;

public class CYMClass extends CatBox<CYMSkill, StateCYMClass> implements ISkillManager{

	private static List<CYMClass> classes = new ArrayList<CYMClass>();
	public static final String classPlayerName = "Player";
	public static CYMClass classPlayer;
	
	public static void initPlayerClass(){
		classPlayer = get(classPlayerName);
		if(classPlayer == null){
			classPlayer = newCYMClassCmd(classPlayerName);
			classPlayer.setActivate(true);
			classPlayer.setShowPlayer(false);
			classPlayer.save();
		}
		for(StateCYMClass smc : classPlayer.getStates()) smc.getQuestPlayer().checkClassPlayer();
	}
	
	public static List<CYMClass> getCYMClass(){ return classes; }
	public static CYMClass get(String name){
		for(CYMClass mc : classes) if(name.equalsIgnoreCase(mc.name.getStr())) return mc; return null;
	}
	public static CYMClass getCYMClass(int idclass){
		for(CYMClass mc : classes) if(mc.getId() == idclass) return mc; return null;
	}
	
	public static CYMClass newCYMClassSql(String name){
		return newCYMClass(name);
	}
	
	public static CYMClass newCYMClassCmd(String name){
		CYMClass mc = newCYMClass(name);
		mc.initParamSys();
		mc.create();
		return mc;
	}
	
	public static CYMClass newCYMClass(String name){
		CYMClass mc = new CYMClass(name);
		classes.add(mc);
		add(MekaBox.SKILLBOX, mc);
		return mc;
	}
	
	public int limitSkill;
	private SkillManager skillManager;
	private IMechaParamSave mps = new IMechaParamSave() { @Override public void save() { CYMClass.this.save(); } };
	
	public CYMClass(String name) {
		super(MekaBox.SKILLBOX, name);
		skillManager = new SkillManager(this);
	}
	@Override
	public void init() {
		skillManager.init();
	}
	@Override
	public void initParamSys() {
		skillManager.initParamSys();
		addParamSys("className", name, "Class name.");
		super.initParamSys();
	}
	@Override
	public void setName(String name) {
		if(classPlayer != this) super.setName(name);
	}
	@Override
	public IMechaDriver getDriver(QuestPlayer qp) {
		if(classPlayer == this) return qp.getClassPlayer().getDriver();
		StateCYMClass smc = qp.getCYMClass(getId());
		return smc == null ? null : smc.getDriver();
	}
	@Override
	public IMechaParamSave getMechaParamSave() { return mps; }
	@Override
	public ISkillManager getLink(int idlink) { return getCYMClass(idlink); }
	@Override
	public List<? extends ISkillManager> getAllLinks() { return classes; }
	@Override
	public StateCYMClass newStateContainer(QuestPlayer qp) { return new StateCYMClass(this, qp); }
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
		if(this == classPlayer) return;
		clearMechas();
		classes.remove(this);
		for(IMekaBox mb : getMekaboxs().toArray(new IMekaBox[0])){ mb.setCat(classPlayer); mb.save(); }
		getMekaboxs().clear();
		remove(getType(), this);
		cleanStates();
		clearStates();
		questSort.deleteOrder();
		QuestSQLManager.delete(this);
	}
	@Override
	public void addMekabox(CYMSkill box) {
		super.addMekabox(box);
		for(StateCYMSkill sms : box.getStates()){
			for(StateCYMClass smc : sms.getQuestPlayer().getCYMClasses()){
				if(smc.getId() == getId()) smc.addCYMSkill(sms);
			}
		}
	}
	@Override
	public void removeMekabox(CYMSkill box) {
		super.removeMekabox(box);
		box.cleanStates();
		box.setActivate(false);
	}
	
	@Override
	public StateCYMClass getStateOrCreate(QuestPlayer qp) {
		StateCYMClass smc = super.getStateOrCreate(qp);
		smc.updateAll();
		return smc;
	}

	@Override
	public void updateCloneData() {
		for(StateCYMClass smc : getStates()){
			Player p = smc.getQuestPlayer().getPlayer();
			if(p != null && p.isOnline())
				smc.cloneData();
		}
	}
	
	public void updateLimitSkill(){
		for(StateCYMClass smc : getStates()){
			Player p = smc.getQuestPlayer().getPlayer();
			if(p != null && p.isOnline()){
				for(StateCYMSkill sms : smc.getStateCYMSkills()){
					smc.canActivateSkill(sms);
				}
			}
		}
	}
	
	public void updateLinks() {
		for(StateCYMClass smc : getStates()){
			Player p = smc.getQuestPlayer().getPlayer();
			if(p != null && p.isOnline())
				smc.updateLinks();
		}
	}
	
	public void checkLinks() {
		for(StateCYMClass smc : getStates()){
			Player p = smc.getQuestPlayer().getPlayer();
			if(p != null && p.isOnline())
				smc.checkLinks();
		}
	}
	
	public void updateNodes() {
		for(StateCYMClass smc : getStates()){
			Player p = smc.getQuestPlayer().getPlayer();
			if(p != null && p.isOnline())
				smc.updateNodes();
		}
	}
	
	public SkillManager getSkillManager(){ return skillManager; }
	@Override
	public int getTypeContainer() { return CLASS; }
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
		for(StateCYMClass smc : getStates()){
			Player p = smc.getQuestPlayer().getPlayer();
			if(p != null && p.isOnline())
				smc.updateLevel();
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
		for(StateCYMClass smc : getStates()){
			Player p = smc.getQuestPlayer().getPlayer();
			if(p != null && p.isOnline())
				smc.updateTier();
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