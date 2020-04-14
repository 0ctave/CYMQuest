package fr.craftyourmind.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import fr.craftyourmind.manager.CYMReputation;
import fr.craftyourmind.manager.packet.DataAlert;
import fr.craftyourmind.quest.AbsObjective.IStateObj;
import fr.craftyourmind.quest.AbsReward.IStateRew;
import fr.craftyourmind.quest.mecha.AbsMechaContainer;
import fr.craftyourmind.quest.mecha.AbsStateContainer;
import fr.craftyourmind.quest.mecha.IMechaDriver;
import fr.craftyourmind.quest.mecha.IMechaParamSave;
import fr.craftyourmind.quest.mecha.MechaDriverPlayer;
import fr.craftyourmind.quest.mecha.MechaParam;
import fr.craftyourmind.quest.mecha.Mechanism;
import fr.craftyourmind.quest.sql.QuestSQLManager;
import fr.craftyourmind.skill.StateCYMClass;

public class Quest extends AbsMechaContainer{
	
	public static Map<Integer, List<Quest>> listNPC = new HashMap<Integer, List<Quest>>();
	
	public Map<QuestPlayer, StateQuestPlayer> states = new HashMap<QuestPlayer, StateQuestPlayer>();
	
	public int id;
	public int npc;
	public CYMReputation repute;
	public Quest parent;
	public int cymClass;
	public String nameParamClass = "";
	public boolean repeatable = false;
	public boolean repeateTimeAccept = true;
	public boolean repeateTimeGive = false;
	public long repeateTime = 0; // jj:hh:mm:ss
	public int levelMin = 0;
	public int levelMax = 0;
	public int reputeMin = 0;
	public int reputeMax = 0;
	public float classMin = 0;
	public float classMax = 0;
	public boolean objInTheOrder = false;
	public boolean common = true;
	public int playersMax = 0;
	public MechaParam title = new MechaParam(true, "");
	public String introTxt = "";
	public String fullTxt = "";
	public String successTxt = "";
	public String loseTxt = "";
	public boolean displayIconNPC = true;
	
	private EventParadise eventParadise;
	
	private List<AbsObjective> objectives = new ArrayList<AbsObjective>();
	private List<AbsReward> rewards = new ArrayList<AbsReward>();
	
	private QuestRepeatTime timeChecker;
	private MechaParam nbBeginning = new MechaParam(true, "0");
	
	public Mechanism mechaAccept;
	public Mechanism mechaGive;
	public Mechanism mechaDecline;
	
	private QuestIcon[] icons = new QuestIcon[QuestIcon.size];
	public String strIcons = "", normalIcon = "", takenIcon = "", successIcon = "", repeatableIcon = "", reputeIcon = "", skillIcon = "";
	
	public List<QuestTag> tags = new ArrayList<QuestTag>();
	
	private IMechaParamSave mps = new IMechaParamSave() { @Override public void save() { Quest.this.save(); } };
	
	public Quest(){ timeChecker = new QuestRepeatTime(this); }
	
	public StateQuestPlayer getState(QuestPlayer qp){
		StateQuestPlayer sqp = states.get(qp);
		if(sqp == null) sqp = getNewState(qp);
		return sqp;
	}
	
	public StateQuestPlayer getNewState(QuestPlayer qp){
		StateQuestPlayer sqp = new StateQuestPlayer(qp);
		states.put(qp, sqp);
		addState(qp, sqp);
		return sqp;
	}
	// -------- ACCEPT -------- 
	public StateQuestPlayer accept(QuestPlayer qp){
		StateQuestPlayer sqp = getState(qp);
		if(hasAcces(qp, sqp) && !sqp.isBeginning() && !sqp.isTerminate()) sqp.accept();
		return sqp;
	}
	// -------- DECLINE -------- 
	public StateQuestPlayer decline(QuestPlayer qp){
		StateQuestPlayer sqp = states.get(qp);
		if(sqp != null) sqp.decline();
		return sqp;
	}
	// -------- GIVEREWARD -------- 
	public StateQuestPlayer giveReward(QuestPlayer qp){
		StateQuestPlayer sqp = states.get(qp);
		if(sqp != null && !sqp.isTerminate()) sqp.giveReward();
		return sqp;
	}
	
	public void create(){
		List<Quest> lq = listNPC.get(npc);
		if(lq == null){
			lq = new ArrayList<Quest>();
			listNPC.put(npc, lq);
		}
		lq.add(this);
		QuestSQLManager.create(this);
	}
	
	public void save(){
		QuestSQLManager.save(this);
	}
	
	public void delete(){
		clearMechas();
		setEvent(null);
		List<Quest> lq = listNPC.get(npc);
		lq.remove(this);
		if(lq.size() == 0)
			listNPC.remove(npc);
		cleanAll();
		QuestSQLManager.delete(this);
		for(AbsObjective o : objectives) o.sqlDelete();
		for(AbsReward r : rewards) r.sqlDelete();
	}
	@Override
	public String getName() { return title.getStr(); }
	@Override
	public void init(){
		addParamSys("currentPlayer", nbBeginning, "Current player who began.");
		addParamSys("questTitle", title, "Quest title.");
	}
	@Override
	public IMechaParamSave getMechaParamSave() { return mps; }
	
	// -------- HAS ACCESS -------- 
	public boolean hasAcces(QuestPlayer qp){
		return hasAcces(qp, states.get(qp));
	}
	
	public boolean hasAcces(QuestPlayer qp, StateQuestPlayer sqp){
		if(eventParadise != null && eventParadise.isEvent()) return false;
		return hasAccesEvent(qp, sqp);
	}
	
	public boolean hasAccesEvent(QuestPlayer qp, StateQuestPlayer sqp){
		if(qp.getPlayer() == null || !qp.getPlayer().hasPermission("cymquest.quest")) return false;
		if(sqp != null){
			if(sqp.terminate) return false;
			if(sqp.beginning) return true;
		}
		
		if(playersMax > 0 && playersMax <= nbBeginning.getInt()) return false;
		
		if(parent != null ){
			StateQuestPlayer psqp = parent.states.get(qp);
			if(psqp == null) return false;
			else{
				if(psqp.beginning) return false;
				else if(psqp.terminate && psqp.idQuestChild != 0 && psqp.idQuestChild != id)
					return false;
			}
		}
		if(repute != null){
			if(reputeMin == 0 && reputeMax == 0);
			else{
				int pts = 0;
				if(qp.hasClan()) pts = qp.getClan().getReputePts(repute);
				else pts = qp.getReputePts(repute);
				if(pts >= reputeMin && reputeMax == 0);
				else if(reputeMin == 0 && pts < reputeMax);
				else if(pts >= reputeMin && pts < reputeMax);
				else return false;
			}
		}
		
		if(cymClass > 0){
			if(classMin == 0 && classMax == 0);
			else{
				StateCYMClass smc = qp.getCYMClass(cymClass);
				if(smc == null) return false;
				else{
					if(nameParamClass.isEmpty()) return false;
					MechaParam mp = smc.getMechaParam(nameParamClass);
					if(mp != null){
						if(mp.getDouble() >= classMin && classMax == 0);
						else if(classMin == 0 && mp.getDouble() < classMax);
						else if(mp.getDouble() >= classMin && mp.getDouble() < classMax);
						else return false;
					}
				}
			}
		}
		
		if(levelMin == 0 && levelMax == 0) return true;
		else if(qp.getLevel() >= levelMin && levelMax == 0) return true;
		else if(levelMin == 0 && qp.getLevel() < levelMax) return true;
		else if(qp.getLevel() >= levelMin && qp.getLevel() < levelMax) return true;
		
		return false;
	}
	
	public void addTimer(long time, StateQuestPlayer sqp){
		timeChecker.add(time, sqp);
	}
	
	public void setRepeatable(boolean b){
		if(repeatable != b) cleanAll(); // init
		repeatable = b;
	}
	
	public void setRepeateTime(String time){
		try{
			String[] t = time.split(":");
			int sec = Integer.valueOf(t[3]);
			int min = Integer.valueOf(t[2]);
			int hour = Integer.valueOf(t[1]);
			int day = Integer.valueOf(t[0]);
			if(sec < 0) sec = 0; else if(sec > 59) sec = 59;
			if(min < 0) min = 0; else if(min > 59) min = 59;
			if(hour < 0) hour = 0; else if(hour > 23) hour = 23;
			if(day < 0) day = 0; else if(day > 364) day = 364;
			repeateTime = ((day * 86400) + (hour * 3600) + (min * 60) + sec ) * 1000;
		}catch (Exception e) {
			repeateTime = 0;
		}
	}
	
	public String getRepeateTime() {
		long sec = TimeUnit.MILLISECONDS.toSeconds(repeateTime);
		long min = TimeUnit.MILLISECONDS.toMinutes(repeateTime);
		long hour = TimeUnit.MILLISECONDS.toHours(repeateTime);
		long day = TimeUnit.MILLISECONDS.toDays(repeateTime);
		sec = sec - TimeUnit.MINUTES.toSeconds(min);
		min = min - TimeUnit.HOURS.toMinutes(hour);
		hour = hour - TimeUnit.DAYS.toHours(day);
		return day+":"+hour+":"+min+":"+sec;
	}
	
	private void setDefaultIcons(){
		icons = QuestIcon.defaultIcons();
		strIcons = getIcons();
		QuestIcon.setQuestIcons(this, icons);
	}
	
	public void setIcons(String str){
		strIcons = str;
		if(str == null || str.isEmpty())
			setDefaultIcons();
		else{
			String[] icons = str.split(DELIMITER);
			int index = 0;
			int version = Integer.valueOf(icons[index++]);
			int size = Integer.valueOf(icons[index++]);
			if(size == 0) setDefaultIcons();
			else{
				this.icons = new QuestIcon[size];
				for(int i = 0 ; i < size ; i++) this.icons[i] = QuestIcon.getIcon(icons[index++]);
				this.icons = QuestIcon.setQuestIcons(this, this.icons);
				if(size != QuestIcon.size) strIcons = getIcons(); // update
			}
		}
	}
	
	public String getIcons(){
		StringBuilder sb = new StringBuilder("0");
		sb.append(DELIMITER).append(icons.length);
		for(QuestIcon qi : icons) sb.append(DELIMITER).append(qi.name);
		return sb.toString();
	}
	
	public void addTag(QuestTag qt) {
		if(!tags.contains(qt)){
			tags.add(qt);
			qt.tag(this);
			for(StateQuestPlayer sqp : states.values()) sqp.getQuestPlayer().addQuest(qt, sqp);
		}
	}
	public void removeTag(QuestTag qt) {
		tags.remove(qt);
		qt.detag(this);
		for(StateQuestPlayer sqp : states.values()) sqp.getQuestPlayer().removeQuest(qt, sqp);
	}
	public String getTags(){
		StringBuilder sb = new StringBuilder().append(tags.size());
		for(QuestTag qt : tags) sb.append(DELIMITER).append(qt.id);
		return sb.toString();
	}
	public String getTagsNoHidden(){
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for(QuestTag qt : tags){
			if(!qt.hidden){
				sb.append(DELIMITER).append(qt.id);
				i++;
			}
		}
		return sb.insert(0, i).toString();
	}
	
	public void messageSuccess(QuestPlayer qp){
		if(!successTxt.isEmpty()) qp.sendMessage(ChatColor.DARK_AQUA+successTxt);
	}
	public void alertSuccess(QuestPlayer qp){
		if(!successTxt.isEmpty()) new DataAlert("quest.success", successTxt, Material.CLOCK.getKey().toString()).send(qp.getPlayer());
	}
	public void messageLose(QuestPlayer qp){
		if(!loseTxt.isEmpty()) qp.sendMessage(ChatColor.DARK_AQUA+loseTxt);
	}
	public void alertLose(QuestPlayer qp){
		if(!loseTxt.isEmpty()) new DataAlert("quest.fail", loseTxt, Material.SADDLE.getKey().toString()).send(qp.getPlayer());
	}
	
	public void giveRewardForAll(){
		for(StateQuestPlayer sqp : states.values().toArray(new StateQuestPlayer[0]))
			sqp.giveReward();		
	}
	
	public void cleanAll(){
		timeChecker.remove();
		for(StateQuestPlayer sqp : states.values().toArray(new StateQuestPlayer[0]))
			sqp.decline();
	}
	
	public static Quest get(int id){
		if(id > 0){
			for(Entry<Integer, List<Quest>> lq : listNPC.entrySet()){
				for(Quest q : lq.getValue())
					if(q.id == id) return q;
			}
		}
		return null;
	}
	
	public static Quest get(int npc, int id){
		List<Quest> lq = listNPC.get(npc);
		if(lq != null){
			for(Quest q : lq)
				if(q.id == id) return q;
		}
		return null;
	}
	
	public static Quest get(int npc, String name){
		List<Quest> lq = listNPC.get(npc);
		if(lq != null){
			for(Quest q : lq)
				if(q.title.getStr().equals(name)) return q;
		}
		return null;
	}
	
	public void setEvent(EventParadise ep){
		if(ep == null){
			if(eventParadise != null) eventParadise.quests.remove(this);
			eventParadise = ep;
		}else if(eventParadise == null && ep.id != 0){
			ep.quests.add(this);
			eventParadise = ep;
		}else if(ep.id != 0 && eventParadise.id != ep.id){
			eventParadise.quests.remove(this);
			ep.quests.add(this);
			eventParadise = ep;
		}
	}
	
	public EventParadise getEvent(){
		return eventParadise;
	}
	
	public List<AbsObjective> getObjs(){
		return objectives;
	}
	
	public void setObjective(AbsObjective obj){
		obj.index = objectives.size();
		objectives.add(obj);
		for(StateQuestPlayer sqp : states.values())
			sqp.objs.add(obj.getState(sqp));
	}
	
	public void delObjective(AbsObjective obj){
		objectives.remove(obj);
		for(int i = 0 ; i < objectives.size() ; i++)
			objectives.get(i).index = i;
			
		for(StateQuestPlayer sqp : states.values()){
			for(int i = 0 ; i < sqp.objs.size() ; i++)
				if(sqp.objs.get(i).getId() == obj.id)
					sqp.objs.remove(i);		
		}
	}
	
	public AbsObjective getObjective(int idO) {
		for(AbsObjective obj : objectives)
			if( obj.id == idO) return obj;
		return null;
	}
	
	public List<AbsReward> getRewards(){
		return rewards;
	}
	
	public void setReward(AbsReward rew){
		rewards.add(rew);
		for(StateQuestPlayer sqp : states.values())
			sqp.rews.add(rew.getState(sqp));

	}
	
	public void delReward(AbsReward rew){
		rewards.remove(rew);
		for(StateQuestPlayer sqp : states.values()){
			for(int i = 0 ; i < sqp.rews.size() ; i++)
				if(sqp.rews.get(i).getId() == rew.id)
					sqp.rews.remove(i);	
		}
	}
	
	public AbsReward getReward(int idR) {
		for(AbsReward rew : rewards)
			if( rew.id == idR) return rew;
		return null;
	}
	
	public boolean equals(Object obj) {
		if(this.getClass() != obj.getClass())
			return false;
		Quest o = (Quest)obj;
		return this.id == o.id;
	}
	// ********************************** STATEQUESTPLAYER **********************************
	public class StateQuestPlayer extends AbsStateContainer{
		
		public QuestPlayer qp;
		private boolean beginning;
		private boolean terminate;
		public int idQuestChild = 0;
		public boolean isRepeat = false;
		
		public List<IStateObj> objs = new ArrayList<IStateObj>();
		public List<IStateRew> rews = new ArrayList<IStateRew>();
		
		private IMechaParamSave mps = new IMechaParamSave() { @Override public void save() { QuestSQLManager.updateParams(Quest.this, StateQuestPlayer.this); } };
		
		public StateQuestPlayer(QuestPlayer qp) {
			super(Quest.this, qp);
			this.qp = qp;
			beginning = false;
			terminate = false;
			driver = new MechaDriverPlayer(qp, Quest.this, mps);
			for(AbsObjective obj : objectives){
				IStateObj so = obj.getState(this);
				objs.add(so);
			}
			for(AbsReward rew : rewards)
				rews.add(rew.getState(this));
		}
		// -------- ACCEPT -------- 
		public void accept(){
			beginning = true;
			terminate = false;
			nbBeginning.setIntUnlock(nbBeginning.getInt()+1);
			qp.addQuestCurrent(Quest.this);
			QuestSQLManager.accept(Quest.this, this, qp);
			
			// ---- objInTheOrder
			int i = 0;
			for(IStateObj so : objs){
				if(!so.isTerminate()){	
					if(i != 0 && objInTheOrder) so.setLock(true);
					if(!so.isLock()) so.begin();
				}
				i++;
			}
			// ---- common
			if(common){ 
				StateQuestPlayer sqpcommon = null;
				for(StateQuestPlayer sqp : states.values()){
					if(sqp.isBeginning() && !sqp.equals(this)){
						sqpcommon = sqp;
						break;
					}
				}
				if(sqpcommon != null){
					for(IStateObj so : objs){
						if(sqpcommon.objs.get(so.getIndex()).isTerminate()){
							AbsObjective o = objectives.get(so.getIndex());
							so.setInitCommon(true);
							o.currentCommon = true;
							so.afterCheckOK();
							so.terminate();
							o.currentCommon = false;
							so.setInitCommon(false);
						}
						so.initTick(sqpcommon.objs.get(so.getIndex()).getTick());
					}
				}
			}
			if(repeatable && repeateTimeAccept) timeChecker.add(this);
			if(eventParadise != null) eventParadise.questAccept(this);
			if(mechaAccept != null) mechaAccept.start(driver);
		}
		// -------- DECLINE -------- 
		public void decline(){
			if(beginning) nbBeginning.setIntUnlock(nbBeginning.getInt()-1);
			beginning = false;
			terminate = false;
			for (IStateObj so : objs){
				so.clean();
				so.cleanMechas();
			}
			qp.removeQuestCurrent(Quest.this);
			qp.removeQuestFinished(Quest.this);
			states.remove(qp);
			removeState(qp);
			QuestSQLManager.decline(Quest.this, qp);
			if(eventParadise!= null) eventParadise.questDecline(this);
			driver.cleanControllers();
			if(mechaDecline != null) mechaDecline.start(driver);
		}
		// -------- GIVEREWARD -------- 
		public void giveReward(){
			beginning = false;
			terminate = true;
			nbBeginning.setIntUnlock(nbBeginning.getInt()-1);
			for(IStateObj so : objs){
				so.finish();
				so.cleanMechas();
			}
			for(IStateRew sr : rews) if(!sr.isDisable()) sr.give();
			qp.removeQuestCurrent(Quest.this);
			if(repeatable && repeateTime == 0){ 
				terminate = false;
				states.remove(qp);
				removeState(qp);
				QuestSQLManager.repeat(Quest.this, qp);
			}else{
				qp.addQuestFinished(Quest.this);
				QuestSQLManager.terminate(Quest.this, qp);
			}
			if(repeatable && repeateTimeGive) timeChecker.add(this);
			if(eventParadise!= null) eventParadise.questGiveReward(this);
			driver.cleanControllers();
			if(mechaGive != null) mechaGive.start(driver);
		}
		// -------- CHECK SUCCESS -------- 
		public boolean checkObjSuccessWithMsg(){
			boolean success = checkObjSuccessNoMsg();
			if(success) messageSuccess(qp);
			return success;
		}
		
		public boolean checkObjSuccessNoMsg(){
			boolean success = true;
			for (IStateObj o : objs){
				if(!o.isTerminate())
					if(!o.checker())
						success = false;					
			}
			return success;
		}
		
		// -------- QUEST CHILD -------- 
		public void linkQuestChild(int idQuestChild){
			this.idQuestChild = idQuestChild;
			QuestSQLManager.questchild(Quest.this, this);
		}
		
		public Quest getQuest(){
			return Quest.this;
		}
		
		public boolean isBeginning() {
			return beginning;
		}
		public void setBeginning(boolean beginning) {
			this.beginning = beginning;
			nbBeginning.setIntUnlock(nbBeginning.getInt()+1);
		}
		public boolean isTerminate() {
			return terminate;
		}
		public void setTerminate(boolean terminate) {
			this.terminate = terminate;
		}
		
		public QuestPlayer getPlayer() { return qp; }
		
		public IMechaDriver getDriver(){ return driver; }
		
		@Override
		public boolean equals(Object obj) {
			if(obj.getClass() == this.getClass()){ 
				StateQuestPlayer sqp = (StateQuestPlayer) obj;
				return qp.getId() == sqp.qp.getId();
			}
			return false;
		}
		@Override
		public void cloneData() { }
		@Override
		public void createStateSql() { }
		@Override
		public void updateStateSql() { }
		@Override
		public void deleteStateSql() { }
	}
	@Override
	public int getId() { return id; }
	@Override
	public void setId(int id) { this.id = id; }
	@Override
	public int getTypeContainer() { return QUEST; }
	@Override
	public IMechaDriver newDriver(QuestPlayer qp) { return getState(qp).getDriver(); }
	@Override
	public IMechaDriver getDriver(QuestPlayer qp) {
		StateQuestPlayer sqp = states.get(qp);
		if(sqp == null) return null;
		else return sqp.getDriver();
	}
	@Override
	public StateQuestPlayer newStateContainer(QuestPlayer qp) {
		// TODO Auto-generated method stub
		return null;
	}
}