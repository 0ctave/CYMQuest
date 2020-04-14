package fr.craftyourmind.quest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import fr.craftyourmind.manager.PeriodicCheck;
import fr.craftyourmind.manager.packet.DataAlert;
import fr.craftyourmind.manager.util.IChecker;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;
import fr.craftyourmind.quest.mecha.IMechaDriver;
import fr.craftyourmind.quest.mecha.Mechanism;
import fr.craftyourmind.quest.sql.QuestSQLManager;

public abstract class AbsObjective {

	static final protected String DELIMITER = QuestTools.DELIMITER;
	
	static final public int GATHER = 0;
	static final public int LOCATION = 1;
	static final public int TALK = 2;
	static final public int KILL = 3;
	static final public int BLOCKEVENT = 4;
	static final public int TIMER = 5;
	static final public int ANSWER = 6;
	static final public int CHOICE = 7;
	static final public int MONEY = 8;
	static final public int MEKA = 9;
	
	static final public String STRGATHER = "quest.objective.gather";
	static final public String STRLOCATION = "quest.gui.location";
	static final public String STRTALK = "quest.objective.talk";
	static final public String STRKILL = "quest.gui.kill";
	static final public String STRBLOCKEVENT = "quest.gui.blockevent";
	static final public String STRTIMER = "quest.gui.timer";
	static final public String STRANSWER = "quest.objective.answer";
	static final public String STRCHOICE = "quest.objective.choice";
	static final public String STRMONEY = "Money";
	static final public String STRMEKA = "Meka";
	
	public int id;
	public String descriptive;
	public String success;
	public String alertTitle = "quest.objective.success";
	public String alertItem = Material.BOOK.getKey().toString();
	private boolean finishQuest = false;
	
	public Mechanism mechaTerminate;
	
	public int index = 0;
	public Quest q;
	public boolean currentCommon = false;
	
	public AbsObjective(Quest q) { this.q = q; }
	
	public abstract IStateObj getState(StateQuestPlayer sqp);
	
	public void addCheckerPeriodic(IStateObj so){ PeriodicCheck.add(so); }
	public void removeCheckerPeriodic(IStateObj so){ PeriodicCheck.remove(so); }
	
	public void sqlCreate(){ QuestSQLManager.create(this); }
	public void sqlSave() { QuestSQLManager.save(this); }
	public void sqlDelete() { QuestSQLManager.delete(this); }
	
	public void sqlClean(QuestPlayer qp){ QuestSQLManager.clean(this, qp); }
	
	public void sqlTerminate(QuestPlayer qp){ QuestSQLManager.terminate(this, qp); }
	
	public abstract int getType();
	public abstract String getStrType();
	public abstract String getParams();
	public abstract String getParamsGUI();
	
	public String getIdItem() { return alertItem; }
	
	public byte getDataItem() { return 0; }
	
	public boolean isFinishQuest(){ return finishQuest; }
	public void setFinishQuest(boolean finish){ finishQuest = finish; }
	
	protected abstract void loadParams(String[] params);
	public void loadParams(String params) { loadParams(params.split(DELIMITER)); }
	
	protected void alertSuccess(QuestPlayer qp){ alertSuccess(qp, alertTitle); }
	protected void alertSuccess(QuestPlayer qp, String title){
		if(!success.isEmpty() && qp.getPlayer() != null)
			new DataAlert(title, success, alertItem).send(qp.getPlayer());
	}
	@Override
	public boolean equals(Object obj) {
		if(this.getClass() != obj.getClass()) return false;
		AbsObjective o = (AbsObjective)obj;
		return this.id == o.id;
	}
	
	public static AbsObjective newObjective(Quest q, int type){
		if(type == GATHER)  return new ObjectiveGather(q);
		else if (type == LOCATION) return new ObjectiveLocation(q);
		else if (type == TALK) return new ObjectiveTalk(q);
		else if (type == KILL) return new ObjectiveKill(q);
		else if (type == BLOCKEVENT) return new ObjectiveBlockEvent(q);
		else if (type == TIMER) return new ObjectiveTimer(q);
		else if (type == ANSWER) return new ObjectiveAnswer(q);
		else if (type == CHOICE) return new ObjectiveChoice(q);
		else if (type == MONEY) return new ObjectiveMoney(q);
		else if (type == MEKA) return new ObjectiveMeka(q);
		Plugin.log("type : "+type);
		return null;
	}

	public static List<Integer> getListTypeId() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(GATHER);
		list.add(LOCATION);
		list.add(TALK);
		list.add(KILL);
		list.add(BLOCKEVENT);
		list.add(TIMER);
		list.add(MEKA);
		list.add(ANSWER);
		list.add(CHOICE);
		list.add(MONEY);
		return list;
	}
	
	public static List<String> getListType() {
		List<String> list = new ArrayList<String>();
		list.add(STRGATHER);
		list.add(STRLOCATION);
		list.add(STRTALK);
		list.add(STRKILL);
		list.add(STRBLOCKEVENT);
		list.add(STRTIMER);
		list.add(STRMEKA);
		list.add(STRANSWER);
		list.add(STRCHOICE);
		list.add(STRMONEY);
		return list;
	}
	
	// ************************* STATEOBJPLAYER *************************
	abstract class StateObjPlayer implements IStateObj{
		private IMechaDriver driver;
		public StateQuestPlayer sqp;
		private boolean terminate;
		private boolean lock;
		private boolean afterCheckOK = false;
		public boolean initCommon = false;
		
		public StateObjPlayer(StateQuestPlayer sqp) {
			this.sqp = sqp;
			terminate = false;
			lock = false;
			//driver = new MechaDriverPlayer(sqp.qp, sqp.getDriver().getContainer());
			driver = sqp.getDriver();
		}
		
		public int getId() { return id; }
		
		public boolean checker(){
			if(afterCheckOK) return true;
			if(!lock){
				if(check()){
					afterCheckOK();
					return true;
				}
			}
			return false;
		}
		
		public void tick(int nb){
			if(q.common && !currentCommon){
				currentCommon = true;
				Collection<StateQuestPlayer> sqps = q.states.values();
				for(StateQuestPlayer sqp : sqps.toArray(new StateQuestPlayer[0])){
					if(!this.sqp.equals(sqp) && sqp.isBeginning()){
						IStateObj so = sqp.objs.get(index);
						so.tick(nb);
					}
				}
				currentCommon = false;
			}
		}
		
		public void afterCheckOK(){
			afterCheckOK = true;
			if(q.objInTheOrder) nextObjLock(false);
			if(finishQuest && sqp.checkObjSuccessNoMsg()){
				q.messageSuccess(sqp.qp);
				sqp.giveReward();
			}
			// common
			if(q.common && !currentCommon){
				currentCommon = true;
				Collection<StateQuestPlayer> sqps = q.states.values();
				for(StateQuestPlayer sqp : sqps.toArray(new StateQuestPlayer[0])){
					if(!this.sqp.equals(sqp) && sqp.isBeginning()){
						IStateObj so = sqp.objs.get(index);
						so.afterCheckOK();
					}
				}
				currentCommon = false;
			}
			afterCheckOK = false;
		}
		
		public void afterCheckKO(){
			sqlClean(sqp.qp);
			setTerminate(false);
			if(q.objInTheOrder) nextObjLock(true);
			// common
			if(q.common && !currentCommon){
				currentCommon = true;
				Collection<StateQuestPlayer> sqps = q.states.values();
				for(StateQuestPlayer sqp : sqps.toArray(new StateQuestPlayer[0])){
					if(!this.sqp.equals(sqp) && sqp.isBeginning()){
						IStateObj so = sqp.objs.get(index);
						so.afterCheckKO();
					}
				}
				currentCommon = false;
			}
			//driver.cleanControllers();
		}
		
		public void terminate(){
			terminate = true;
			sqlTerminate(sqp.qp);
			if(q.getEvent() != null && !currentCommon) q.getEvent().terminateObj(this);
			if(q.common && !currentCommon){
				currentCommon = true;
				Collection<StateQuestPlayer> sqps = q.states.values();
				for(StateQuestPlayer sqp : sqps.toArray(new StateQuestPlayer[0])){
					if(!this.sqp.equals(sqp) && sqp.isBeginning()){
						IStateObj so = sqp.objs.get(index);
						so.afterCheckOK();
						so.sendMessagesSuccess();
						so.terminate();
					}
				}
				currentCommon = false;
			}
			if(mechaTerminate != null && !initCommon) mechaTerminate.start(driver);
		}
		
		public boolean isTerminate(){ return terminate; }
		
		public void setTerminate(boolean terminate) { this.terminate = terminate; }
		
		public int getIndex() { return index; }
		
		public boolean exist(Entity player){ 
			if(sqp.qp.getPlayer() == null) return false;
			return sqp.qp.getPlayer().getEntityId() == player.getEntityId();
		}
		
		public void nextObjLock(boolean lock){
			if(index+1 < sqp.objs.size()){
				IStateObj so = sqp.objs.get(index+1);
				boolean preLock = so.isLock();
				so.setLock(lock);
				if(!so.isLock() && preLock) so.begin();
				else if(so.isLock() && !preLock && !so.isTerminate()) so.clean();
				so.checker();
			}
		}
		
		public void setLock(boolean lock) { this.lock = lock; }
		
		public boolean isLock(){ return lock; }
		
		public String getDescriptive(){ return descriptive; }
		
		protected void messageSuccess(QuestPlayer qp){
			if(!success.isEmpty()) qp.sendMessage(ChatColor.AQUA+success);
		}
		
		public void sendMessagesSuccess() {
			messageSuccess(sqp.qp);
			alertSuccess(sqp.qp);
		}
		
		public String getMessageGui() { return ""; }
		
		public int getTick() { return 0; }
		
		public void initTick(int action) { }
		@Override
		public void setInitCommon(boolean b) { initCommon = b; }
		@Override
		public void cleanMechas() {
			//driver.cleanControllers();
		}
		@Override
		public boolean equals(Object obj) {
			if(getClass() != obj.getClass()) return false;
			StateObjPlayer o = (StateObjPlayer) obj;
			return id == o.getId() && sqp.qp.getId() == o.sqp.qp.getId();
		}
	}
	// ************************* ISTATEOBJ *************************
	public interface IStateObj extends IChecker{
		/**
		 * Gestionnaire du check().
		 */
		public abstract boolean checker();
		/**
		 * Test si l'objectif est remplie.
		 */
		public abstract boolean check();
		public abstract int getId();
		/**
		 * Action de debut de quête.
		 */
		public abstract void begin();
		/**
		 * Action de fin de quête réussi.
		 */
		public abstract void finish();
		/**
		 * Nettoie l'objectif annulé
		 */
		public abstract void clean();
		/**
		 * Termine l'objectif réussis.
		 */
		public void terminate();
		public abstract boolean isTerminate();
		public abstract void setTerminate(boolean terminate);
		public abstract int getIndex();
		public abstract boolean isLock();
		public abstract void setLock(boolean lock);
		public abstract void sendMessagesSuccess();
		public abstract void afterCheckOK();
		public abstract void afterCheckKO();
		public abstract void nextObjLock(boolean lock);
		public abstract void tick(int diff);
		public abstract String getMessageGui();
		public abstract int getTick();
		public abstract void initTick(int action);
		public abstract void cleanMechas();
		public abstract void setInitCommon(boolean b);
	}
}