package fr.craftyourmind.quest;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;
import fr.craftyourmind.quest.sql.QuestSQLManager;

public abstract class AbsReward {

	static final protected String DELIMITER = QuestTools.DELIMITER;
	
	static final public int MATERIAL = 0;
	static final public int EXPERIENCE = 2;
	static final public int REPUTATION = 3;
	static final public int QUEST = 4;
	static final public int TELEPORT = 5;
	static final public int EFFECT = 6;
	static final public int EVENT = 7;
	static final public int COMMAND = 8;
	
	static final public String STRMATERIAL = "quest.reward.material";
	static final public String STREXPERIENCE = "quest.reward.experience";
	static final public String STRREPUTATION = "quest.reward.reputation";
	static final public String STRQUEST = "quest.gui.quest";
	static final public String STRTELEPORT = "quest.reward.teleportation";
	static final public String STREFFECT = "quest.gui.effect";
	static final public String STREVENT = "quest.gui.event";
	static final public String STRCOMMAND = "quest.gui.command";
	
	public int id;
	public String descriptive = "";
	public int amount = 0;
	public Quest q;
	public String idItem = "";
	
	public AbsReward(Quest q){ this.q = q; }
	
	public abstract IStateRew getState(StateQuestPlayer sqp);	
	
	public void sqlCreate(){ QuestSQLManager.create(this); }
	public void sqlSave() { QuestSQLManager.save(this); }
	public void sqlDelete() { QuestSQLManager.delete(this); }
	
	public abstract int getType();
	public abstract String getStrType();
	public abstract String getParams();
	public abstract String getParamsGUI();
	
	public String getIdItem() { return idItem; }
	public byte getDataItem() { return 0; }
	
	protected abstract void loadParams(String[] params);
	public void loadParams(String params) {
		String[] str = params.split(DELIMITER);
		loadParams(str);
	}
	
	protected void messageGive(QuestPlayer qp){
		if(!descriptive.isEmpty()) qp.sendMessage(ChatColor.GRAY+"Receive "+descriptive);
	}
	@Override
	public boolean equals(Object obj) {
		if(this.getClass() != obj.getClass()) return false;
		AbsReward o = (AbsReward)obj;
		return this.id == o.id;
	}
	
	public static AbsReward newReward(Quest q, int type){
		if(type == MATERIAL) return new RewardMaterial(q);
		else if (type == EXPERIENCE) return new RewardExp(q);
		else if (type == REPUTATION) return new RewardRepute(q);
		else if (type == QUEST) return new RewardQuest(q);
		else if (type == TELEPORT) return new RewardTeleport(q);
		else if (type == EFFECT) return new RewardEffect(q);
		else if (type == EVENT) return new RewardEvent(q);
		else if (type == COMMAND) return new RewardCommand(q);
		return null;
	}

	public static List<Integer> getListTypeId() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(MATERIAL);
		list.add(EFFECT);
		list.add(EXPERIENCE);
		list.add(REPUTATION);
		list.add(TELEPORT);
		list.add(COMMAND);
		list.add(QUEST);
		list.add(EVENT);
		return list;
	}
	
	public static List<String> getListType() {
		List<String> list = new ArrayList<String>();
		list.add(STRMATERIAL);
		list.add(STREFFECT);
		list.add(STREXPERIENCE);
		list.add(STRREPUTATION);
		list.add(STRTELEPORT);
		list.add(STRCOMMAND);
		list.add(STRQUEST);
		list.add(STREVENT);
		return list;
	}
	// ************************* STATEREWPLAYER *************************
	abstract class StateRewPlayer implements IStateRew{
		public boolean disable = false;
		public StateQuestPlayer sqp;
		public StateRewPlayer(StateQuestPlayer sqp) { this.sqp = sqp; }
		public int getId() { return id; }
		public boolean isDisable() { return disable; }
		public void setDisable(boolean disable) { this.disable = disable; }
	}
	// ************************* ISTATEREW *************************
	interface IStateRew{
		public abstract int getId();
		public abstract void give();
		public abstract boolean isDisable();
		public abstract void setDisable(boolean disable);
	}
}