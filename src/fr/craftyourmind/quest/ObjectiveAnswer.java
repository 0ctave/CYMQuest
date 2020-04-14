package fr.craftyourmind.quest;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import fr.craftyourmind.manager.packet.DataAlert;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class ObjectiveAnswer extends AbsObjective{

	public String question = "";
	public String answer = "";
	
	public ObjectiveAnswer(Quest q) {
		super(q);
		alertItem = Material.BOOK.getKey().toString();
		setFinishQuest(true);
	}
	@Override
	public IStateObj getState(StateQuestPlayer sqp) { return new StateAnswer(sqp); }
	@Override
	public int getType() { return ANSWER; }
	@Override
	public String getStrType() { return STRANSWER; }
	@Override
	public void setFinishQuest(boolean finish) { super.setFinishQuest(true); }
	@Override
	public String getParams() { return question+DELIMITER+answer; }
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		if(params.length > 0) question = params[0]; else question = "";
		if(params.length > 1) answer = params[1]; else answer = "";
	}
	// ---------------- STATEANSWER ----------------
	class StateAnswer extends StateObjPlayer{

		public String playerAnswer = "";
		
		public StateAnswer(StateQuestPlayer sqp) { super(sqp); }
		@Override
		public boolean check() {
			if(answer.equalsIgnoreCase(playerAnswer)){
				sendMessagesSuccess();
				terminate();
				return true;
			}else{
				if(!q.loseTxt.isEmpty()) sqp.qp.sendMessage(ChatColor.AQUA+q.loseTxt);
				new DataAlert("quest.objective.fail", q.loseTxt, alertItem).send(sqp.qp.getPlayer());
			}
			playerAnswer = "";
			return false;
		}
		@Override
		public void begin() { }
		@Override
		public void finish() { clean(); }
		@Override
		public void clean() { playerAnswer = ""; }
	}
}