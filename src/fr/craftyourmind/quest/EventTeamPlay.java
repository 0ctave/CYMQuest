package fr.craftyourmind.quest;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import fr.craftyourmind.quest.AbsObjective.IStateObj;
import fr.craftyourmind.quest.AbsObjective.StateObjPlayer;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class EventTeamPlay extends EventParadise{
	
	public List<Team> teams = new ArrayList<Team>();
	
	@Override
	public void initPlayersAccess() {
		players.clear();
		getStates().clear();
		for(Team t : teams)
			players.addAll(getPlayersAccess(t.quest));
		for(StateEventPlayer sep : players) addState(sep.qp, sep);
	}
	@Override
	public void stopPlayers() {
		for(Team t : teams){
			for(StateEventPlayer sep : t.players)
				sendStop(sep.qp.getPlayer(), t.quest);
		}
	}
	// ----------------------------- START -----------------------------
	@Override
	public void start(){ //Plugin.log("start tp");
		eventWin = false;
		stopPlayers();
		players.clear();
		getStates().clear();
		stop();
		if(mechaStartUnitary != null) mechaStartUnitary.start();
		boolean start = true;
		for(Team t : teams){
			t.players.clear();
			t.players = getPlayersAccess(t.quest);
			if(!start(t.quest, t.players))
				start = false;
		}
		if(start){
			if(preparation) goNotice(); else startGeneral();
			if(autoAccept)
				for(Team t : teams)
					for(StateEventPlayer sep : t.players)
						 accept(t.quest, sep.qp, t.quest.getState(sep.qp));
		}else{
			nextEvent(false);
			stopPlayers();
		}
		save();
	}
	// ***************************** OPEN *****************************
	@Override
	public void open(Quest q, QuestPlayer qp, StateQuestPlayer sqp) {
		super.open(q, qp, sqp);
	}
	// ***************************** ACCEPT *****************************
	@Override
	public boolean accept(Quest q, QuestPlayer qp, StateQuestPlayer sqp) { //Plugin.log("accept tp");
		for(Team t : teams)
			if(t.questPlayers.contains(sqp)) return false;
		if(super.accept(q, qp, sqp)){
			for(Team t : teams){
				if(t.quest.id == q.id){
					t.questPlayers.add(sqp);
					return true;
				}
			}
		} return false;
	}
	// ***************************** DECLINE *****************************
	@Override
	public void decline(Quest q, QuestPlayer qp, StateQuestPlayer sqp) { //Plugin.log("decline tp");
		super.decline(q, qp, sqp);
		if(sqp != null){
			for(Team t : teams){
				if(t.quest.id == q.id)
					t.questPlayers.remove(sqp);
			}
		}
	}
	// ----------------------------- QUEUEOK -----------------------------
	@Override
	public boolean queueOK(){
		for(Team t : teams)
			if(!queueOK(t.questPlayers, t.quest))
				return false;
		return true;
	}
	
	@Override
	protected void sendNotEnough(List<StateQuestPlayer> list) {
		for(Team t : teams)
			super.sendNotEnough(t.questPlayers);
	}
	// ----------------------------- STARTBEGINNING -----------------------------
	@Override
	public void startGeneral(){ //Plugin.log("startBeginning tp");
		stopPlayers();
		if(preparation){
			for(Team t : teams){
				for(StateQuestPlayer sqp : t.questPlayers){
					if(!sqp.objs.isEmpty()) sqp.objs.get(0).setLock(false);
					if(!sqp.getQuest().objInTheOrder)
						for(IStateObj so : sqp.objs)
							 so.setLock(false);	
					sqp.qp.sendMessage(ChatColor.DARK_PURPLE+"["+t.quest.title+"] "+ChatColor.LIGHT_PURPLE+beginMessage);
					sendBeginning(sqp.qp.getPlayer(), sqp.getQuest());
				}
			}
		}else{
			for(Team t : teams){
				for(StateEventPlayer sep : t.players){
					sep.qp.sendMessage(ChatColor.DARK_PURPLE+"["+t.quest.title+"] "+ChatColor.LIGHT_PURPLE+beginMessage);
					sendBeginning(sep.qp.getPlayer(), t.quest);
				}
			}
		}
		goGeneral();
	}
	// ----------------------------- FINISH -----------------------------
	@Override
	public void finish(){ //Plugin.log("finish tp");
		stopPlayers();
		boolean onFinish = false;
		boolean win = false;
		for(Team t : teams){
			if(t.onFinish){ onFinish = true;
				if(finish(t.questPlayers, t.quest, false)) win = true;
			}else if(onFinish && !win && t.win)
				finish(t.questPlayers, t.quest, true);
			else if(onFinish && win && t.lose)
				finish(t.questPlayers, t.quest, true);
			else
				finish(t.questPlayers, t.quest, false);
		}
		if(win) eventWin = true;
		for(StateEventPlayer sep : players) sep.finish();
	}
	public boolean finish(List<StateQuestPlayer> lsqp, Quest q, boolean fail){
		boolean finish = false;
		for(StateQuestPlayer sqp : lsqp)
			if(finish(q, sqp, fail)) finish = true;
		return finish;
	}
	// ----------------------------- STOP -----------------------------
	@Override
	public void stop() {
		super.stop();
		for(Team t : teams){
			t.questPlayers.clear();
			t.quest.cleanAll();
		}
	}
	@Override
	public void questDecline(StateQuestPlayer sqp) { //Plugin.log("questDecline");
		if(idScheduleGeneral != 0){
			for(Team t : teams){
				if(t.quest.id == sqp.getQuest().id){
					t.questPlayers.remove(sqp);
					if((t.onFinish || t.win || t.lose) && t.questPlayers.size() == 0){
						finish();
						stop();
						nextEvent();
					}
				}
			}
		}
	}	
	@Override
	public void terminateObj(StateObjPlayer sop) {
		if(showObjCompleted)
			for(Team t : teams)
				for(StateQuestPlayer sqp : t.questPlayers)
					sqp.qp.sendMessage(ChatColor.DARK_PURPLE+sqp.qp.getName()+msgObjCompleted+"\""+sop.getDescriptive()+"\"");
	}
	@Override
	public void quit(QuestPlayer qp) {
		for(Team t : teams)
			quit(qp, t.quest);
	}
	@Override
	public String getParamsCon() {
		int version = 0;
		StringBuilder params = new StringBuilder().append(version).append(DELIMITER).append(teams.size());
		for(Team t : teams)
			params.append(DELIMITER).append(t.name).append(DELIMITER).append(((t.quest==null)?0:t.quest.id)).append(DELIMITER)
			.append(t.onFinish).append(DELIMITER).append(t.win).append(DELIMITER).append(t.lose);
		return super.getParamsCon() + DELIMITER + params;
	}
	@Override
	public int loadParamsCon(String[] params) {
		teams.clear();
		int index = super.loadParamsCon(params);
		try{
			int version = Integer.valueOf(params[index++]);
			int size = Integer.valueOf(params[index++]);
			for(int i = 0 ; i < size ; i++){
				Team t = new Team();
				t.name = params[index++];
				t.quest = Quest.get(Integer.valueOf(params[index++]));
				t.onFinish = Boolean.valueOf(params[index++]);
				t.win = Boolean.valueOf(params[index++]);
				t.lose = Boolean.valueOf(params[index++]);
				
				if(t.onFinish && t.quest != null) teams.add(0, t);
				else if(t.quest != null) teams.add(t);
			}
		}catch (Exception e) {}
		return index;
	}
	@Override
	public int getType() {
		return TEAMPLAY;
	}
	private class Team{
		
		public boolean onFinish = false;
		public boolean win = false;
		public boolean lose = false;
		
		public String name = "";
		public List<StateEventPlayer> players = new ArrayList<StateEventPlayer>();
		public List<StateQuestPlayer> questPlayers = new ArrayList<StateQuestPlayer>();
		public Quest quest;
	}
}