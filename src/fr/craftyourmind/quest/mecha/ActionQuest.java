package fr.craftyourmind.quest.mecha;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import fr.craftyourmind.quest.Plugin;
import fr.craftyourmind.quest.Quest;
import fr.craftyourmind.quest.packet.DataQuestEventParadise;

public class ActionQuest extends Mechanism{

	public static final int ACCEPT = 1;
	public static final int GIVE = 2;
	public static final int DECLINE = 3;
	public static final int STOP = 4;
	public static final int NOTICE = 5;
	public static final int BEGINNING = 6;

	public int action = 0;
	public IntegerData npc = new IntegerData();
	public IntegerData idQuest = new IntegerData();
	public int actionPropose = 0;
	public IntegerData timer = new IntegerData(); // second
	public boolean openQuest = true;
	
	@Override
	public boolean isMechaStoppable(){ return true; }
	@Override
	public int getType() { return MechaType.ACTQUEST; }
	@Override
	public String getParams() {
		return new StringBuilder().append(action).append(DELIMITER).append(npc).append(DELIMITER).append(idQuest).append(DELIMITER).append(actionPropose).append(DELIMITER).append(timer).append(DELIMITER).append(openQuest).toString();
	}
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		action = Integer.valueOf(params[0]);
		npc.load(params[1]);
		idQuest.load(params[2]);
		actionPropose = Integer.valueOf(params[3]);
		timer.load(params[4]);
		openQuest = Boolean.valueOf(params[5]);
	}

	public void sendStop(Quest q, Player p){
		new DataQuestEventParadise(DataQuestEventParadise.STOP, q.getEvent().id, q.npc, q.id).send(p);
	}
	
	public void sendNotice(Quest q, Player p, int time){
		new DataQuestEventParadise(DataQuestEventParadise.NOTICE, q.getEvent().id, q.npc, q.id, time, q.title.getStr()).send(p);
	}
	
	public void sendBeginning(Quest q, Player p, int time){
		new DataQuestEventParadise(DataQuestEventParadise.BEGINNING, q.getEvent().id, q.npc, q.id, time, q.title.getStr()).send(p);
	}
	
	public long timerCommon;
	public void initTimerCommon(int timer){ timerCommon = System.currentTimeMillis() + timer * 1000;}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateActionQuest(this, mc, driver); }
	// ------------------ StateActionQuest ------------------
	class StateActionQuest extends AbsMechaStateEntitySave implements Runnable{

		private int idTask = 0;
		private boolean isAlreadyPropose = false;
		private IntegerData npc = new IntegerData();
		private IntegerData idQuest = new IntegerData();
		private IntegerData timer = new IntegerData();
		private Quest q;
		
		public StateActionQuest(Mechanism m, MechaControler mc, IMechaDriver driver) {  super(m, mc, driver); }
		@Override
		public void cloneData() {
			super.cloneData();
			npc.clone(this, ActionQuest.this.npc);
			idQuest.clone(this, ActionQuest.this.idQuest);
			timer.clone(this, ActionQuest.this.timer);
			q = Quest.get(npc.get(), idQuest.get());
		}
		@Override
		public void start() {
			if(q != null && driver.isPlayer() && driver.hasPlayer()){
				super.start();
				if(action > 0){
					if(q.getEvent() == null){
						if(action == ACCEPT) q.accept(driver.getQuestPlayer());
						else if(action == GIVE) q.giveReward(driver.getQuestPlayer());
						else if(action == DECLINE) q.decline(driver.getQuestPlayer());
					}else{
						if(action == ACCEPT) q.getEvent().accept(q, driver.getQuestPlayer(), q.getState(driver.getQuestPlayer()));
						else if(action == GIVE) q.getEvent().giveReward(q, driver.getQuestPlayer(), q.getState(driver.getQuestPlayer()));
						else if(action == DECLINE) q.getEvent().decline(q, driver.getQuestPlayer(), q.getState(driver.getQuestPlayer()));
					}
				}
				if(actionPropose > 0 && q.getEvent() != null){
					if(actionPropose == STOP) stop();
					else{
						if(!isAlreadyPropose){
							isAlreadyPropose = true;
							q.getEvent().openQuest = openQuest;
							if(timer.get() > 0){
								goTimer();
								return;
							}
							if(actionPropose == NOTICE) sendNotice(q, driver.getPlayer(), timer.get());
							else if(actionPropose == BEGINNING) sendBeginning(q, driver.getPlayer(), timer.get());
						}else{
							stop();
							return;
						}
					}
				}else stop();
			}
			launchMessage();			
		}
		
		private void goTimer(){
			int time = timer.get();
			if(time > 0){
				if(common){
					if(timerCommon == 0) initTimerCommon(timer.get());
					long currentTime = System.currentTimeMillis();
					time = (int) ((timerCommon - currentTime) / 1000);
					if(time <= 0){
						time = timer.get();
						initTimerCommon(timer.get());
					}
				}
				idTask = Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.it, this, time * 20);
			}
			if(actionPropose == NOTICE) sendNotice(q, driver.getPlayer(), time);
			else if(actionPropose == BEGINNING) sendBeginning(q, driver.getPlayer(), time);
		}
		@Override
		public void run() {
			checker();
			if(permanent) start();
		}
		@Override
		public boolean check() {
			isAlreadyPropose = false;
			sendStop(q, qp.getPlayer());
			timerCommon = 0;
			return true;
		}		
		@Override
		public void stop() {
			super.stop();
			isAlreadyPropose = false;
			if(q != null && q.getEvent() != null && driver.isPlayer() && driver.hasPlayer()) sendStop(q, qp.getPlayer());
			Bukkit.getScheduler().cancelTask(idTask);
			idTask = 0;
		}
	}
}