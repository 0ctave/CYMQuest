package fr.craftyourmind.quest;

import org.bukkit.Material;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class RewardQuest extends AbsReward{

	public int idQuest;
	public boolean autoAccept = false;
	
	public RewardQuest(Quest q) {
		super(q);
		idItem = Material.WRITABLE_BOOK.getKey().toString();
	}
	@Override
	public IStateRew getState(StateQuestPlayer sqp) { return new StateQuest(sqp); }
	@Override
	public int getType() { return QUEST; }
	@Override
	public String getStrType() { return STRQUEST; }
	@Override
	public String getParams() { return idQuest+DELIMITER+autoAccept; }
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 1){
			idQuest = Integer.valueOf(params[0]);
			sqlSave();
		}else if(params.length == 2){
			idQuest = Integer.valueOf(params[0]);
			autoAccept = Boolean.valueOf(params[1]);
		}
	}
	// ---------------- STATEQUEST ----------------
	class StateQuest extends StateRewPlayer{

		public StateQuest(StateQuestPlayer sqp) { super(sqp); }
		@Override
		public void give() {
			sqp.linkQuestChild(idQuest);
			Quest q = Quest.get(idQuest);
			if(q != null && autoAccept) q.accept(sqp.qp);
			messageGive(sqp.qp);
		}
	}
}