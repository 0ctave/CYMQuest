package fr.craftyourmind.quest;

import fr.craftyourmind.manager.CYMManager;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;
import org.bukkit.Material;

public class ObjectiveMoney extends AbsObjective{

	public double amount = 0;
	
	public ObjectiveMoney(Quest q) {
		super(q);
		alertItem = Material.EMERALD.getKey().toString();
	}
	@Override
	public IStateObj getState(StateQuestPlayer sqp) { return new StateMoney(sqp); }
	@Override
	public int getType() { return MONEY; }
	@Override
	public String getStrType() { return STRMONEY; }
	@Override
	public String getParams() { return amount+""; }
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		amount = Double.valueOf(params[0]);
	}
	// ---------------- STATEMONEY ----------------
	class StateMoney extends StateObjPlayer{

		public StateMoney(StateQuestPlayer sqp) {  super(sqp); }
		@Override
		public boolean check() {
			if(CYMManager.hasMoney(sqp.qp.getName(), amount)){
				sendMessagesSuccess();
				return true;
			}
			return false;
		}
		@Override
		public void begin() { checker(); }
		@Override
		public void finish() { }
		@Override
		public void clean() { }
	}
}