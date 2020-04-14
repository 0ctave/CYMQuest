package fr.craftyourmind.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.craftyourmind.skill.StateCYMSkill;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import fr.craftyourmind.manager.CYMClan;
import fr.craftyourmind.manager.CYMPlayer;
import fr.craftyourmind.manager.CYMReputation;
import fr.craftyourmind.manager.util.ReputeData;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;
import fr.craftyourmind.skill.CYMClass;
import fr.craftyourmind.skill.SkillInventory;
import fr.craftyourmind.skill.StateCYMClass;

public class QuestPlayer implements MetadataValue{

	private CYMPlayer mp;
	private Player player;
	
	public static List<QuestPlayer> list = new ArrayList<QuestPlayer>();
	public static QuestPlayer nobody;
	
	public static QuestPlayer get(Entity e){
		List<MetadataValue> metas = e.getMetadata("questPlayer");
		if(metas.isEmpty()) return null;
		return (QuestPlayer) metas.get(0);
	}
	
	public static QuestPlayer get(Player player){
		List<MetadataValue> metas = player.getMetadata("questPlayer");
		if(metas.isEmpty()){
			QuestPlayer qp = getOrCreate(player.getName());
			player.setMetadata("questPlayer", qp);
			qp.player = player;
			return qp;
		}
		return (QuestPlayer) metas.get(0);
	}
	
	public static QuestPlayer get(String name){
		for(QuestPlayer qp : list) if(qp.mp.name.equalsIgnoreCase(name)) return qp;
		return null;
	}
	
	public static QuestPlayer getOrCreate(String name){
		QuestPlayer qp = get(name);
		if(qp == null) return new QuestPlayer(CYMPlayer.getOrCreate(name));
		return qp;
	}
	
	public static QuestPlayer get(int idPlayer){
		for(QuestPlayer qp : list) if(qp.mp.id == idPlayer) return qp;
		return null;
	}
	
	public static void newNobody(){
		nobody = new QuestPlayer(CYMPlayer.nobody);
	}
	
	//private List<Quest> questsCurrent = new ArrayList<Quest>();
	//private List<Quest> questsFinished = new ArrayList<Quest>();
	private TagQuests questsCurrent = new TagQuests();
	private TagQuests questsFinished = new TagQuests();
	public int npcselect = 0;
	public boolean spy = false;
	private List<StateCYMClass> cymclasses = new ArrayList<StateCYMClass>();
	private StateCYMClass PLAYER;
	public Inventory skillInventory = SkillInventory.createInventory();
	public boolean skillBar;
	public ItemStack[] contentsBeforeOpenSkillInv; // bug cancel event inventory
	public QuestPlayer openOtherInv; // open other player inventory skill
	public ItemStack[] hotbarCache = new ItemStack[SkillInventory.lenghtHotbar];

	// Lors du chargement
	public QuestPlayer(CYMPlayer mp) {
		this.mp = mp;
		list.add(this);
	}
	
	public int getId(){
		return mp.id;
	}
	
	public CYMPlayer getCYMPlayer(){
		return mp;
	}
	
	public String getName(){
		return mp.name;
	}
	
	public int getLevel(){
		if(player == null) return 0;
		return player.getLevel();
	}
	
	public void sendMessage(String msg){
		if(player == null){
			player = getPlayer();
			if(player != null)
				player.sendMessage(msg);
		}else player.sendMessage(msg);
	}
	
	public Player getPlayer(){
		// if(player == null) player = mp.getPlayer();
		return player;
	}
	
	public void setPlayer(Player p){
		this.player = p;
	}
	
	public boolean isNobody(){ return mp.isNobody(); }
	
	public void addRepute(CYMReputation repute, int points, int param){
		mp.addRepute(repute, points, param);
	}
	
	public void initRepute(CYMReputation repute, int points, int param){
		mp.initRepute(repute, points, param);
	}
	
	public int getReputePts(CYMReputation rep){
		return mp.getReputePts(rep);
	}
	
	public int getReputeParam(CYMReputation rep){
		return mp.getReputeParam(rep);
	}
	
	public Map<CYMReputation, ReputeData> getReputations(){
		return mp.getReputations();
	}
	
	public CYMClan getClan(){
		return mp.getClan();
	}
	
	public boolean hasClan(){
		return mp.hasClan();
	}
	
	public boolean useModQuest(){
		return mp.useMod;
	}
	
	public void addQuestCurrent(Quest q){ questsCurrent.add(q); }
	public void removeQuestCurrent(Quest q){ questsCurrent.remove(q); }
	public List<Quest> getQuestsCurrent(){ return questsCurrent.get(); }
	public List<Quest> getQuestsCurrent(int tag) {return questsCurrent.get(tag); }
	public String getQuestCurrentTags(){ return questsCurrent.getTags(); }
	
	public void addQuestFinished(Quest q){ questsFinished.add(q); }
	public void removeQuestFinished(Quest q){ questsFinished.remove(q); }
	public List<Quest> getQuestsFinished(){ return questsFinished.get(); }
	public List<Quest> getQuestsFinished(int tag){ return questsFinished.get(tag); }
	public String getQuestFinishedTags(){ return questsFinished.getTags(); }
	
	public void addQuest(QuestTag qt, StateQuestPlayer sqp){
		if(sqp.isBeginning()) questsCurrent.add(qt, sqp.getQuest());
		else if(sqp.isTerminate()) questsFinished.add(qt, sqp.getQuest());
	}
	
	public void removeQuest(QuestTag qt, StateQuestPlayer sqp){
		if(sqp.isBeginning()) questsCurrent.remove(qt, sqp.getQuest());
		else if(sqp.isTerminate()) questsFinished.remove(qt, sqp.getQuest());
	}
	
	public void addClass(StateCYMClass smc){
		cymclasses.add(smc);
	}
	public void removeClass(StateCYMClass smc){
		cymclasses.remove(smc);
	}
	
	public void checkClassPlayer(){
		if(PLAYER == null){
			for(StateCYMClass smc : cymclasses){
				if(smc.getId() == CYMClass.classPlayer.getId()){
					PLAYER = smc;
					return;
				}
			}
		}
		if(!hasClassPlayer()) CYMClass.classPlayer.activate(PLAYER = CYMClass.classPlayer.getStateOrCreate(this));
	}
	public boolean hasClassPlayer(){ return PLAYER != null; }
	
	public StateCYMClass getClassPlayer(){ return PLAYER;}
	
	public List<StateCYMClass> getCYMClasses(){ return cymclasses; }
	
	public StateCYMClass getCYMClass(int idclass){
		for(StateCYMClass smc : cymclasses) if(smc.getId() == idclass) return smc;
		return null;
	}
	
	public StateCYMSkill getCYMSkill(int idclass, int idskill){
		StateCYMClass smc = getCYMClass(idclass);
		if(smc != null) return smc.getStateCYMSkill(idskill);
		return null;
	}
	
	public void onCYMLoginEvent() {
		for(StateCYMClass smc : cymclasses){
			smc.updateAll();
			for(StateCYMSkill sms : smc.getStateCYMSkills())
				sms.updateAll();
		}
		checkClassPlayer();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this.getClass() != obj.getClass())
			return false;
		QuestPlayer o = (QuestPlayer)obj;
		return this.mp.id == o.mp.id;
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

	public static void onPluginDisableEvent() {
		for(QuestPlayer qp : list) if(qp.player != null) qp.player.removeMetadata("questPlayer", Plugin.it);
	}
	// -------------- TagQuests --------------
	class TagQuests{
		private List<Quest> quests = new ArrayList<Quest>();
		private Map<QuestTag, List<Quest>> tagQuests = new HashMap<QuestTag, List<Quest>>();
		private List<Quest> questsNotTag = new ArrayList<Quest>();
		
		public List<Quest> get() { return quests; }
		public List<Quest> get(int tag) {
			if(tag == 0) return questsNotTag;
			for(Entry<QuestTag, List<Quest>> entry : tagQuests.entrySet())
				if(entry.getKey().id == tag) return entry.getValue();
			return new ArrayList<Quest>();
		}
		public String getTags() {
			int size = tagQuests.size();
			StringBuilder sb = new StringBuilder();
			if(!questsNotTag.isEmpty()){
				sb.append(QuestTools.DELIMITER).append(0).append(QuestTools.DELIMITER).append("No tag");
				size++;
			}
			for(QuestTag qt : tagQuests.keySet()) sb.append(QuestTools.DELIMITER).append(qt.id).append(QuestTools.DELIMITER).append(qt.name);
			return sb.insert(0, size).toString();
		}
		public void add(Quest q) {
			quests.add(q);
			if(q.tags.isEmpty()) questsNotTag.add(q);
			for(QuestTag qt : q.tags){
				List<Quest> list = tagQuests.get(qt);
				if(list == null) tagQuests.put(qt, list = new ArrayList<Quest>());
				list.add(q);
			}
		}
		public void remove(Quest q) {
			quests.remove(q);
			questsNotTag.remove(q);
			for(QuestTag qt : q.tags){
				List<Quest> list = tagQuests.get(qt);
				if(list != null){
					list.remove(q);
					if(list.isEmpty()) tagQuests.remove(qt);
				}
			}
		}
		public void add(QuestTag qt, Quest q) {
			questsNotTag.remove(q);
			List<Quest> list = tagQuests.get(qt);
			if(list != null) list.add(q);
		}
		public void remove(QuestTag qt, Quest q) {
			if(q.tags.isEmpty() && !questsNotTag.contains(q)) questsNotTag.add(q);
			List<Quest> list = tagQuests.get(qt);
			if(list != null) list.remove(q);
		}		
	}
}