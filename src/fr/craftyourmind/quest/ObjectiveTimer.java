package fr.craftyourmind.quest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class ObjectiveTimer extends AbsObjective{
	
	public int hour = 0;
	public int minute = 0;
	public int second = 20;
	
	public ObjectiveTimer(Quest q) {
		super(q);
		alertTitle = "quest.objective.fail";
		alertItem = Material.CLOCK.getKey().toString();
	}
	@Override
	public IStateObj getState(StateQuestPlayer sqp) { return new StateTimer(sqp); }
	@Override
	public int getType() { return TIMER; }
	@Override
	public String getStrType() { return STRTIMER; }
	@Override
	public String getParams() { return hour+DELIMITER+minute+DELIMITER+second; }
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		hour = Integer.valueOf(params[0]);
		minute = Integer.valueOf(params[1]);
		second = Integer.valueOf(params[2]);	
	}
	// ---------------- STATETIMER ----------------
	class StateTimer extends StateObjPlayer implements Runnable{

		private int idTask = 0;
		private boolean state = true;
		
		public StateTimer(StateQuestPlayer sqp) { super(sqp); }

		public void go(){
			idTask = Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.it, this, ((hour*60*60)+(minute*60)+second)*20);
		}
		
		public void run() {
			if(sqp.isBeginning() && !sqp.isTerminate() && !sqp.checkObjSuccessNoMsg()){
				sendMessagesSuccess();
				sqp.getQuest().messageLose(sqp.qp);
				sqp.decline();
				state = false;
			}
		}
		
		public void stop(){ Bukkit.getScheduler().cancelTask(idTask); }
		@Override
		public boolean check() { return state; }
		@Override
		public void begin() { if(!isTerminate()) go(); }
		@Override
		public void finish() { clean(); }
		@Override
		public void clean() { stop(); }
	}
}