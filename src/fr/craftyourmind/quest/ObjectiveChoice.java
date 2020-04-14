package fr.craftyourmind.quest;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class ObjectiveChoice extends AbsObjective{

	public int parentIdChoice = 0;
	public int idReward = 0;
	public String coreMessage = "";
	public byte dataItem;
	public List<Integer> rewards = new ArrayList<Integer>();
	
	public ObjectiveChoice(Quest q) {
		super(q);
		alertItem = Material.PAPER.getKey().toString();
	}
	@Override
	public IStateObj getState(StateQuestPlayer sqp) { return new StateChoice(sqp); }
	@Override
	public int getType() { return CHOICE; }
	@Override
	public String getStrType() { return STRCHOICE; }
	@Override
	public String getParams() {
		StringBuilder rews = new StringBuilder().append(rewards.size());
		for(Integer r : rewards) rews.append(DELIMITER).append(r);
		return new StringBuilder("4").append(DELIMITER).append(parentIdChoice).append(DELIMITER).append(rews).append(DELIMITER).append(coreMessage).append(DELIMITER).append(alertItem).append(DELIMITER).append(dataItem).toString();
	}
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 2 || params.length == 3){
			parentIdChoice = Integer.valueOf(params[0]);
			idReward = Integer.valueOf(params[1]);
			if(params.length == 3) coreMessage = params[2];
			rewards.add(idReward);
			sqlSave();
		}else if(params.length == 4){
			parentIdChoice = Integer.valueOf(params[0]);
			idReward = Integer.valueOf(params[1]);
			coreMessage = params[2];
			alertItem = params[3];
			rewards.add(idReward);
			sqlSave();
		}else if(params.length > 4){
			rewards.clear();
			int v = Integer.valueOf(params[0]);
			if(v == 3){
				parentIdChoice = Integer.valueOf(params[1]);
				int size = Integer.valueOf(params[2]);
				int index = 3;
				for(int i = 0 ; i < size ; i++) rewards.add(Integer.valueOf(params[index++]));
				coreMessage = params[index++];
				alertItem = params[index++];
				sqlSave();
			}else if(v == 4){
				parentIdChoice = Integer.valueOf(params[1]);
				int size = Integer.valueOf(params[2]);
				int index = 3;
				for(int i = 0 ; i < size ; i++) rewards.add(Integer.valueOf(params[index++]));
				coreMessage = params[index++];
				alertItem = params[index++];
				dataItem = Byte.valueOf(params[index++]);
			}
		}
	}
	// ---------------- STATECHOICE ----------------
	class StateChoice extends StateObjPlayer{
		public StateChoice(StateQuestPlayer sqp) { super(sqp); }
		@Override
		public boolean check() { return false; }
		@Override
		public void begin() { }
		@Override
		public void finish() { }
		@Override
		public void clean() { }
	}
}