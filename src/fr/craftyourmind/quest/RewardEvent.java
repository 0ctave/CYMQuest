package fr.craftyourmind.quest;

import org.bukkit.ChatColor;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;
import org.bukkit.Material;

public class RewardEvent extends AbsReward{

	public static int START = 0;
	public static int STOP = 1;
	public static int TOGGLE = 2;
	
	public int idEvent = 0;
	public int type = 0;
	
	public RewardEvent(Quest q) {
		super(q);
		idItem = Material.CLOCK.getKey().toString();
	}
	@Override
	public IStateRew getState(StateQuestPlayer sqp) { return new StateEvent(sqp); }
	@Override
	public int getType() { return EVENT; }
	@Override
	public String getStrType() { return STREVENT; }
	@Override
	public String getParams() { return idEvent+DELIMITER+type; }
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		idEvent = Integer.valueOf(params[0]);
		type = Integer.valueOf(params[1]);
	}
	// ---------------- STATEEVENT ----------------
	class StateEvent extends StateRewPlayer{

		public StateEvent(StateQuestPlayer sqp) { super(sqp); }
		@Override
		public void give() {
			EventParadise event = EventParadise.get(idEvent);
			if(event != null){
				boolean isStart = true;
				if(event.idScheduleNotice == 0 && event.idScheduleGeneral == 0 && event.idScheduleBetween == 0) isStart = false;
				if(type == START){
					if(!isStart){
						event.stopPlayers();
						event.start();
						sqp.qp.sendMessage(ChatColor.GRAY+"Start "+event.name);
					}else
						sqp.qp.sendMessage(ChatColor.GRAY+event.name.getStr()+" is already started.");
				}else if(type == STOP){
					if(isStart){
						event.stopPlayers();
						event.stop();
						sqp.qp.sendMessage(ChatColor.GRAY+"Stop "+event.name);
					}else
						sqp.qp.sendMessage(ChatColor.GRAY+event.name.getStr()+" is already stopped.");
				}else if(type == TOGGLE){
					if(isStart){
						event.stopPlayers();
						event.stop();
						sqp.qp.sendMessage(ChatColor.GRAY+"Stop "+event.name);
					}else{
						event.stopPlayers();
						event.start();
						sqp.qp.sendMessage(ChatColor.GRAY+"Start "+event.name);
					}
				}
			}
		}
	}
}