package fr.craftyourmind.skill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import fr.craftyourmind.quest.QuestTools;
import fr.craftyourmind.quest.mecha.AbsMechaContainer;
import fr.craftyourmind.quest.mecha.MechaParam;
import fr.craftyourmind.quest.mecha.Mechanism;
import fr.craftyourmind.quest.mecha.StarterTier;
import fr.craftyourmind.quest.mecha.StringData;

public class SkillManager {

	public static final String DELIMITER = QuestTools.DELIMITER;
	
	private boolean keepEnableOnLink = true;
	private boolean showPlayer = true;
	private boolean activationPlayer = true;

	private List<Descriptive> descriptives = new ArrayList<Descriptive>();
	private List<ISkillManager> linkParents = new ArrayList<ISkillManager>();
	private List<ISkillManager> linkChilds = new ArrayList<ISkillManager>();
	public String strLinkChilds = "";
	private List<CYMLevel> levels = new ArrayList<CYMLevel>();
	public String strLevels = "";
	private List<CYMTier> tiers = new ArrayList<CYMTier>();
	public String strTiers = "";
	private List<ISkillManager> nodeParents = new ArrayList<ISkillManager>();
	private List<ISkillManager> nodeChilds = new ArrayList<ISkillManager>();
	public String strNodeChilds = "";
	private int limitNode = 1;
	private boolean syncNodeParents;
	private boolean showMessage = true;
	
	private MechaParam level = new MechaParam(true, "1");
	private MechaParam xp = new MechaParam(true, "0");
	
	private Mechanism staActivate, staDeactivate, staEachLevel, staEachTier;
	private List<StarterTier> staTiers = new ArrayList<StarterTier>();
	private ISkillManager manager;
	
	public SkillManager(ISkillManager manager) { this.manager = manager; }
	
	public void init() {
		setLinkChilds(strLinkChilds);
		setNodeChilds(strNodeChilds);
	}
	
	public void initParamSys() {
		manager.addParamSys("xp", xp, "Player xp.");
		manager.addParamSys("level", level, "Player level.");
	}
	
	public void activate(){ }
	
	public void deactivate(){ }
	
	public boolean isKeepEnableOnLink(){ return keepEnableOnLink; }
	public void setKeepEnableOnLink(boolean b){ keepEnableOnLink = b; }
	
	public boolean isShowPlayer(){ return showPlayer; }
	public void setShowPlayer(boolean b){ showPlayer = b; }
	
	public boolean isActivationPlayer(){ return activationPlayer; }
	public void setActivationPlayer(boolean b){ activationPlayer = b; }
	
	public boolean isShowMessage(){ return showMessage; }
	public void setShowMessage(boolean b){ showMessage = b; }
	
	// --------------------- Descriptives ---------------------
	public String getDescriptives(){
		StringBuilder sb = new StringBuilder("0").append(DELIMITER).append(descriptives.size());
		for(Descriptive d : descriptives) sb.append(DELIMITER).append(d.name).append(DELIMITER).append(d.descriptive).append(DELIMITER).append(d.idIcon).append(DELIMITER).append(d.dataIcon);
		return sb.toString();
	}
	
	public void setDescriptives(String str){
		String[] desc = str.split(DELIMITER);
		int index = 0;
		int version = Integer.valueOf(desc[index++]);
		int size = Integer.valueOf(desc[index++]);
		descriptives.clear();
		for(int i = 0 ; i < size ; i++){
			Descriptive d = new Descriptive();
			d.name = desc[index++];
			d.descriptive.load(desc[index++]);
			d.idIcon = Integer.valueOf(desc[index++]);
			d.dataIcon = Integer.valueOf(desc[index++]);
			descriptives.add(d);
		}
	}
	
	public List<Descriptive> getDescriptivesList(){
		return descriptives;
	}
	// --------------------- LINK ---------------------
	public String getLinkParentsStr(){
		return getStrings(linkParents);
	}
	public void setLinkParents(String str){
		for(ISkillManager sm : linkParents) sm.removeLinkChild(manager);
		setStrings(str, linkParents);
		for(ISkillManager sm : linkParents) sm.addLinkChild(manager);
	}
	public void addLinkChild(ISkillManager sm){ linkChilds.add(sm); }
	public void removeLinkChild(ISkillManager sm){ linkChilds.remove(sm); }
	public List<ISkillManager> getLinkChilds(){
		return linkChilds;
	}
	
	public String getLinkChildsStr(){
		return getStrings(linkChilds);
	}
	public void setLinkChilds(String str){
		strLinkChilds = str;
		for(ISkillManager sm : linkChilds) sm.removeLinkParent(manager);
		setStrings(str, linkChilds);
		for(ISkillManager sm : linkChilds) sm.addLinkParent(manager);
	}
	public void addLinkParent(ISkillManager sm){ linkParents.add(sm); }
	public void removeLinkParent(ISkillManager sm){ linkParents.remove(sm); }
	public List<ISkillManager> getLinkParents(){
		return linkParents;
	}
	// --------------------- LEVEL ---------------------
	public int getLevelLimit() {
		if(levels.isEmpty()) return 0;
		return levels.get(levels.size()-1).lvlEnd;
	}
	
	public CYMLevel getLevel(int lvl) {
		ListIterator<CYMLevel> it = levels.listIterator(levels.size());
		while(it.hasPrevious()){
			CYMLevel ml = it.previous();
			if(lvl >= ml.lvlBegin) return ml;
		}
		return levels.isEmpty() ? null : levels.get(0);
	}
	
	public String getLevels(){
		String l = levels.size()+"";
		for(CYMLevel ml : levels) l += DELIMITER+ml.getId();
		return l;
	}
	
	public void setLevels(String str){
		strLevels = str;
		int tmpsize = levels.size();
		String[] lvl = str.split(DELIMITER);
		int index = 0;
		int size = Integer.valueOf(lvl[index++]);
		for(CYMLevel ml : levels) ml.remove(manager);
		levels.clear();
		for(int i = 0 ; i < size ; i++){
			int idlevel = Integer.valueOf(lvl[index++]);
			CYMLevel ml = CYMLevel.get(idlevel);
			if(ml != null){
				levels.add(ml);
				ml.add(manager);
			}
		}
		Collections.sort(levels);
	}
	
	// --------------------- TIER ---------------------
	public int getTierLimit() {
		return tiers.size();
	}
	
	public List<CYMTier> getCYMTiers(){
		return tiers;
	}
	
	public int getIdTier(int level) {
		int idtier = 0;
		for(CYMTier mt : tiers){
			if(level < mt.limit) break;
			idtier = mt.getId();
		}
		return idtier;
	}
	
	public int getTier(int level) {
		int tier = 0;
		for(CYMTier mt : tiers){
			if(level < mt.limit) break;
			tier++;
		}
		return tier;
	}
	
	public String getTiers(){
		String t = tiers.size()+"";
		for(CYMTier mt : tiers) t += DELIMITER+mt.getId();
		return t;
	}
	
	public void setTiers(String str){
		strTiers = str;
		int tmpsize = tiers.size();
		String[] tr = str.split(DELIMITER);
		int index = 0;
		int size = Integer.valueOf(tr[index++]);
		for(CYMTier mt : tiers) mt.remove(manager);
		tiers.clear();
		for(int i = 0 ; i < size ; i++){
			int idtier = Integer.valueOf(tr[index++]);
			CYMTier mt = CYMTier.get(idtier);
			if(mt != null){
				tiers.add(mt);
				mt.add(manager);
			}
		}
		Collections.sort(tiers);
	}
	// --------------------- NODE ---------------------
	public String getNodeParentsStr(){
		return getStrings(nodeParents);
	}
	public void setNodeParents(String str){
		for(ISkillManager sm : nodeParents) sm.removeNodeChild(manager);
		setStrings(str, nodeParents);
		for(ISkillManager sm : nodeParents) sm.addNodeChild(manager);
	}
	public void addNodeChild(ISkillManager sm){ nodeChilds.add(sm); }
	public void removeNodeChild(ISkillManager sm){ nodeChilds.remove(sm); }
	public List<ISkillManager> getNodeChilds(){
		return nodeChilds;
	}
	
	public String getNodeChildsStr(){
		return getStrings(nodeChilds);
	}
	public void setNodeChilds(String str){
		strNodeChilds = str;
		for(ISkillManager sm : nodeChilds) sm.removeNodeParent(manager);
		setStrings(str, nodeChilds);
		for(ISkillManager sm : nodeChilds) sm.addNodeParent(manager);
	}
	public void addNodeParent(ISkillManager sm){ nodeParents.add(sm); }
	public void removeNodeParent(ISkillManager sm){ nodeParents.remove(sm); }
	public List<ISkillManager> getNodeParents(){
		return nodeParents;
	}
	
	public int getLimitNode(){
		return limitNode;
	}
	public void setLimitNode(int limit){
		limitNode = limit;
	}
	public boolean isSyncNodeParents(){
		return syncNodeParents;
	}
	public void setSyncNodeParents(boolean b){
		syncNodeParents = b;
	}
	// ------  ------
	private String getStrings(List<ISkillManager> list){
		String p = list.size()+"";
		for(ISkillManager sm : list) p += DELIMITER+sm.getId();
		return p;
	}
	
	private void setStrings(String str, List<ISkillManager> list){
		String[] prt = str.split(DELIMITER);
		int index = 0;
		int size = Integer.valueOf(prt[index++]);
		list.clear();
		for(int i = 0 ; i < size ; i++){
			int id = Integer.valueOf(prt[index++]);
			if(id != manager.getId()){
				ISkillManager sm = manager.getLink(id);
				if(sm != null) list.add(sm);
			}
		}
	}
	
	class Descriptive{
		private String name = "";
		public int idIcon, dataIcon;
		private StringData descriptive = new StringData();
		public StringData get(){ return descriptive; }
	}
	
	public static ISkillManager get(int type, int idskill) {
		return get(type, idskill, 0);
	}
	public static ISkillManager get(int type, int idskill, int idparent) {
		if(type == AbsMechaContainer.SKILL) return CYMSkill.getSkill(idskill, idparent);
		else if(type == AbsMechaContainer.CLASS) return CYMClass.getCYMClass(idskill);
		return null;
	}
	
	public Mechanism getStarterActivate() { return staActivate; }
	public void setStarterActivate(Mechanism m) { staActivate = m; }
	public Mechanism getStarterDeactivate() { return staDeactivate; }
	public void setStarterDeactivate(Mechanism m) { staDeactivate = m; }
	public Mechanism getStarterEachLevel() { return staEachLevel; }
	public void setStarterEachLevel(Mechanism m) { staEachLevel = m; }
	public Mechanism getStarterEachTier() { return staEachTier; }
	public void setStarterEachTier(Mechanism m) { staEachTier = m; }
	public List<StarterTier> getStarterSelectTier() { return staTiers; }
	public void setStarterSelectTier(StarterTier statier) {
		if(statier.getIdTier() == 0) staTiers.remove(statier);
		else if(!staTiers.contains(statier)) staTiers.add(statier);
	}
}