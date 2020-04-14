package fr.craftyourmind.quest;

import org.bukkit.Material;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class RewardExp extends AbsReward{
	
	public int level = 1;
	
	public RewardExp(Quest q) {
		super(q);
		idItem = Material.EXPERIENCE_BOTTLE.getKey().toString();
	}
	@Override
	public IStateRew getState(StateQuestPlayer sqp) { return new StateExp(sqp); }
	@Override
	public int getType() { return EXPERIENCE; }
	@Override
	public String getStrType() { return STREXPERIENCE; }
	@Override
	public String getParams() { return ""+level; }
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		level = Integer.valueOf(params[0]);
	}
	// ---------------- STATEXP ----------------
	class StateExp extends StateRewPlayer{

		public StateExp(StateQuestPlayer sqp) { super(sqp); }
		@Override
		public void give() {
			if(sqp.qp.getPlayer() != null){
				sqp.qp.getPlayer().giveExp(amount);
				messageGive(sqp.qp);
			}
		}
	}
}