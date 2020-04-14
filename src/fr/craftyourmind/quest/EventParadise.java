package fr.craftyourmind.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import fr.craftyourmind.quest.AbsObjective.IStateObj;
import fr.craftyourmind.quest.AbsObjective.StateObjPlayer;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;
import fr.craftyourmind.quest.mecha.AbsMechaContainer;
import fr.craftyourmind.quest.mecha.AbsStateContainer;
import fr.craftyourmind.quest.mecha.IMechaDriver;
import fr.craftyourmind.quest.mecha.MechaDriverPlayer;
import fr.craftyourmind.quest.mecha.MechaParam;
import fr.craftyourmind.quest.mecha.Mechanism;
import fr.craftyourmind.quest.packet.DataQuestEventParadise;
import fr.craftyourmind.quest.packet.DataQuestScreen;
import fr.craftyourmind.quest.sql.QuestSQLManager;

public class EventParadise extends AbsMechaContainer implements Runnable{
	
	public static final int PARADISE = 0;
	public static final int TEAMPLAY = 1;
	public static final int MINUS = 2;
	public static final String STRPARADISE = "quest.event.paradise";
	public static final String STRTEAMPLAY = "quest.event.teamplay";
	public static final String STRMINUS = "Minus";
	public static final int FLOWCLASSIC = 0;
	public static final int FLOWRANDOM = 1;
	
	private static Map<Integer, List<EventParadise>> list = new HashMap<Integer, List<EventParadise>>();
	
	public List<Quest> quests = new ArrayList<Quest>();
	protected List<StateEventPlayer> players = new ArrayList<StateEventPlayer>();
	public List<StateQuestPlayer> questPlayers = new ArrayList<StateQuestPlayer>();
	
	public int id = 0;
	public MechaParam name = new MechaParam(true, "");
	public int flow = 0;

	public boolean preparation = false;
	public boolean firstFinish = false;
	public boolean openQuest = false;
	public boolean showObjCompleted = false;

	public int noticeTimer = 30;
	public String noticeMessage = "";
	public String beginMessage = "";
	public int generalTimer = 900; 		// 15 min
	public int nextEventTimer = 120; 	// 2 min
	public boolean isEphemeral = false;
	
	protected World world;
	public int x = 0;
	public int z = 0;
	public int radius = 0;
	
	public String msgFirstFinish = "finished !";
	public String msgNextEvent = "Next in";
	public String msgQueue = "Add in queue ...";
	public String msgObjCompleted= " has completed objective ";
	public String msgNotEnoughPeople = "Not enough people.";
	public String msgTooMany = "Sorry, you are too many.";
	
	protected int idScheduleNotice = 0;
	protected int idScheduleGeneral = 0;
	protected int idScheduleBetween = 0;
	
	public int minPlayers = 0;
	public int maxPlayers = 0;
	public int nextEventWin = 0;
	public int nextEventLose = 0;
	public boolean autoAccept = false;
	public boolean end = true;
	private int slot = 0;
	
	protected Quest currentQuest;
	protected boolean hasFirstFinish = false;
	protected boolean eventWin = false;
	protected Map<Quest, List<QuestPlayer>> autoAcceptQuests = new HashMap<Quest, List<QuestPlayer>>();
	
	public Mechanism mechaStart;
	public Mechanism mechaBegin;
	public Mechanism mechaFinish;
	public Mechanism mechaStop;
	public Mechanism mechaStartUnitary;
	public Mechanism mechaStopUnitary;
	
	public EventParadise() {}
	public EventParadise(int id) { this.id = id; }

	public void addList(){
		List<EventParadise> le = list.get(slot);
		if(le == null){
			le = new ArrayList<EventParadise>();
			list.put(slot, le);
		}
		if(!le.contains(this)) le.add(this);
	}
	public void removeList(){
		List<EventParadise> le = list.get(slot);
		if(le != null) le.remove(this);
	}
	
	public static Map<Integer, List<EventParadise>> get() { return list; }
	public static EventParadise get(int idE) {
		for(Entry<Integer, List<EventParadise>> entry : list.entrySet()){
			for(EventParadise ep : entry.getValue())
				if(ep.id == idE) return ep;
		}
		return null;
	}
	public static EventParadise get(String name) {
		for(Entry<Integer, List<EventParadise>> entry : list.entrySet()){
			for(EventParadise ep : entry.getValue())
				if(ep.name.getStr().equalsIgnoreCase(name)) return ep;
		}
		return null;
	}
	public static List<EventParadise> gett(int slot) {
		if(slot == 0){
			List<EventParadise> le = new ArrayList<EventParadise>();
			for(Entry<Integer, List<EventParadise>> entry : list.entrySet())
				le.addAll(entry.getValue());
			return le;
		}else{
			List<EventParadise> le = list.get(slot);
			if(le == null) return new ArrayList<EventParadise>();
			return le;
		}
	}
	
	public static EventParadise get(int slot, int idE) {
		List<EventParadise> le = list.get(slot);
		if(le == null) return null;
		for(EventParadise ep : le) if(ep.id == idE)  return ep;
		return null;
	}
	
	public static List<Integer> getSlots(){
		List<Integer> slots = new ArrayList<Integer>();
		slots.addAll(list.keySet());
		return slots;
	}
	@Override
	public void init(){ addParamSys("eventName", name, "Event name."); }
	@Override
	public String getName() { return name.getStr(); }
	
	public void setSlot(int slot){
		if(this.slot != slot){
			removeList();
			this.slot = slot;
			addList();
		}
	}
	public void sqlSetSlot(int slot){ this.slot = slot; }
	public int getSlot(){ return slot; }
	
	public void setWorld(String name){ world = Bukkit.getWorld(name); }
	public void setWorld(World w){ world = w; }
	public String getWorldName(){ return (world == null)?"":world.getName(); }
	public World getWorld(){ return world; }
	
	public boolean isEvent(){ return id != 0; }
	
	public void create(){ QuestSQLManager.create(this); addList(); }
	public void save(){ QuestSQLManager.save(this); }
	public void delete(){
		stopPlayers();
		stop();
		clearMechas();
		removeList();
		QuestSQLManager.delete(this);
	}
	
	public List<Player> getPlayersSelect(){
		List<Player> players = new ArrayList<Player>();
		if(world != null){
			if(radius > 0){
				for(Player p : world.getPlayers()){
					Location loc = p.getLocation();
					if(loc.getBlockX() > x-radius && loc.getBlockX() < x+radius && loc.getBlockZ() > z-radius && loc.getBlockZ() < z+radius)
						players.add(p);
				}
			}else
				players = world.getPlayers();
		}
		return players;
	}
	
	public void initPlayersAccess(){
		players.clear();
		getStates().clear();
		players = getPlayersAccess(currentQuest);
		for(StateEventPlayer sep : players) addState(sep.qp, sep);
	}
	
	public List<StateEventPlayer> getPlayersAccess(Quest q){
		List<StateEventPlayer> players = new ArrayList<StateEventPlayer>();
		if(q != null){
			for(Player p : getPlayersSelect()){
				QuestPlayer qp = QuestPlayer.get(p);
				if(qp.useModQuest() && q.hasAccesEvent(qp, q.states.get(qp)))
					players.add(new StateEventPlayer(qp));
			}
		}
		return players;
	}
	
	public Quest getNextQuest(){
		if(quests.isEmpty()) return null;
		if(flow == FLOWCLASSIC){
			Quest q = quests.get(0);
			quests.remove(0);
			quests.add(q);
			return q;
		}else if(flow == FLOWRANDOM){
			return quests.get(new Random().nextInt(quests.size()));
		}
		return null;
	}
	
	public void sendMessages(String msg){
		for(StateEventPlayer sep : players)
			sep.qp.sendMessage(msg);
	}
	
	public void stopPlayers(){
		if(currentQuest != null)
			for(StateEventPlayer sep : players)
				sendStop(sep.qp.getPlayer(), currentQuest);
	}
	
	public void run() {
		if(idScheduleNotice > 0){ // Notice
			idScheduleNotice = 0; 
			if(queueOK()) startGeneral();
		}else if(idScheduleGeneral > 0){ // General
			idScheduleGeneral = 0;
			finish();
			stop();
			nextEvent();
		}else if(idScheduleBetween > 0){ // Between
			idScheduleBetween = 0;
			start();
		}
	}
	// ----------------------------- START -----------------------------
	public void start(){ //Plugin.log("start");
		eventWin = false;
		stopPlayers();
		stop();
		players.clear();
		getStates().clear();
		currentQuest = getNextQuest();
		if(mechaStartUnitary != null) mechaStartUnitary.start();
		List<StateEventPlayer> access = getPlayersAccess(currentQuest);
		if(start(currentQuest, access)){
			if(preparation) goNotice(); else startGeneral();
			if(autoAccept)
				for(StateEventPlayer sep : access)
					 accept(currentQuest, sep.qp, currentQuest.getState(sep.qp));
		}else{
			nextEvent(false);
			stopPlayers();
		}
		save();
	}
	protected boolean start(Quest q, List<StateEventPlayer> access){
		if(q != null){
			for(StateEventPlayer sep : access){ 
				if(!noticeMessage.isEmpty()) sep.qp.sendMessage(ChatColor.DARK_PURPLE+"["+q.title+"] "+ChatColor.LIGHT_PURPLE+noticeMessage);
				sendNotice(sep.qp.getPlayer(), q);
				sep.start();
			}
			players.addAll(access);
			for(StateEventPlayer sep : players) addState(sep.qp, sep);
			if(access.size() > 0)
				return true;
		}
		return false;
	}
	
	// ***************************** OPEN *****************************
	public void open(Quest q, QuestPlayer qp, StateQuestPlayer sqp){ //Plugin.log("open");
		if(idScheduleBetween == 0 && (idScheduleNotice != 0 || (idScheduleGeneral != 0 && openQuest)) && q.hasAccesEvent(qp, sqp))
			new DataQuestScreen(DataQuestScreen.EVENT, q.npc, q.id).callEvent(qp.getPlayer());
	}
	// ***************************** ACCEPT *****************************
	public boolean accept(Quest q, QuestPlayer qp, StateQuestPlayer sqp) { //Plugin.log("accept");
		if(idScheduleBetween == 0 && q.hasAccesEvent(qp, sqp) && !sqp.isBeginning()){
			sqp.accept();
			if(!questPlayers.contains(sqp)) questPlayers.add(sqp);
			if(preparation && idScheduleNotice != 0){
				for(IStateObj so : sqp.objs) so.setLock(true);
				qp.sendMessage(ChatColor.DARK_PURPLE+"["+q.title+"] "+ChatColor.LIGHT_PURPLE+msgQueue);
			}
			return true;
		}
		return false;
	}
	// ***************************** DECLINE *****************************
	public void decline(Quest q, QuestPlayer qp, StateQuestPlayer sqp) { //Plugin.log("decline");
		if(sqp != null){
			questPlayers.remove(sqp);
			sqp.decline();}
	}
	// ***************************** GIVEREWARD *****************************
	public void giveReward(Quest q, QuestPlayer qp, StateQuestPlayer sqp) { //Plugin.log("giveReward");
		if(sqp != null) give(sqp);
	}
	private boolean give(StateQuestPlayer sqp){ //Plugin.log("give");
		if(sqp.isBeginning() && sqp.checkObjSuccessNoMsg() && !sqp.isTerminate()){
			Quest q = sqp.getQuest();
			if(!q.successTxt.isEmpty()){
				q.messageSuccess(sqp.qp);
				q.alertSuccess(sqp.qp);
			}
			sqp.giveReward();
			return true;
		} return false;
	}
	// ----------------------------- QUEUEOK -----------------------------
	public boolean queueOK(){
		return queueOK(questPlayers, currentQuest);
	}
	public boolean queueOK(List<StateQuestPlayer> list, Quest q){
		if(list.isEmpty() || q == null){
			stopPlayers();
			stop();
			nextEvent(false);
			return false;
		}else if(minPlayers != 0){
			if(list.size() < minPlayers){
				if(q.playersMax != 0 && q.playersMax < minPlayers) return true;
				sendNotEnough(list);
				stopPlayers();
				stop();
				nextEvent(false);
				return false;
			}
		}else if(maxPlayers != 0){
			Random rand = new Random();
			while(list.size() > maxPlayers){
				int index = rand.nextInt(list.size());
				StateQuestPlayer sqp = list.get(index);
				sqp.decline();
				sqp.qp.sendMessage(ChatColor.DARK_PURPLE+"["+q.title+"] "+ChatColor.LIGHT_PURPLE+msgTooMany);
				sendStop(sqp.qp.getPlayer(), q);
				list.remove(index);
			}
		}
		return true;
	}
	protected void sendNotEnough(List<StateQuestPlayer> list){
		for(StateQuestPlayer sqp : list)
			sqp.qp.sendMessage(ChatColor.DARK_PURPLE+"["+sqp.getQuest().title+"] "+ChatColor.LIGHT_PURPLE+msgNotEnoughPeople);
	}
	// ----------------------------- STARTBEGINNING -----------------------------
	public void startGeneral(){ //Plugin.log("startGeneral");
		stopPlayers();
		if(preparation){ 
			for(StateQuestPlayer sqp : questPlayers){
				if(!sqp.objs.isEmpty()) sqp.objs.get(0).setLock(false);
				if(!sqp.getQuest().objInTheOrder)
					for(IStateObj so : sqp.objs)
						 so.setLock(false);
				StateEventPlayer sep = getState(sqp.qp);
				if(sep.qp != null) sep.begin();
				sqp.qp.sendMessage(ChatColor.DARK_PURPLE+"["+currentQuest.title+"] "+ChatColor.LIGHT_PURPLE+beginMessage);
				sendBeginning(sqp.qp.getPlayer(), sqp.getQuest());
			}
		}else{
			for(StateEventPlayer sep : players){
				sep.begin();
				sep.qp.sendMessage(ChatColor.DARK_PURPLE+"["+currentQuest.title+"] "+ChatColor.LIGHT_PURPLE+beginMessage);
				sendBeginning(sep.qp.getPlayer(), currentQuest);
			}
		}		
		goGeneral();
	}
	// ----------------------------- FINISH -----------------------------
	public void finish(){ //Plugin.log("finish");
		stopPlayers();
		for(StateQuestPlayer sqp : questPlayers)
			if(finish(currentQuest, sqp, false))
				eventWin = true;
		for(StateEventPlayer sep : players) sep.finish();
	}
	public boolean finish(Quest q, StateQuestPlayer sqp, boolean fail){
		if(fail || (!give(sqp) && !sqp.isTerminate())){
			if(!q.repeatable && !q.loseTxt.isEmpty()){ 
				q.messageLose(sqp.qp);
				q.alertLose(sqp.qp);}
			return false;}
		return true;
	}
	// ----------------------------- NEXTEVENT -----------------------------
	public void nextEvent(){
		nextEvent(true);
	}
	public void nextEvent(boolean nextEvent){ //Plugin.log("nextEvent");
		if(nextEventTimer > 0){
			if(eventWin && nextEventWin != 0 && nextEvent){
				EventParadise ep = get(nextEventWin);
				if(ep != null){
					ep.stop();
					ep.stopPlayers();
					ep.eventWin = false;
					ep.nextEvent(false);
				}else stop();
			}else if(!eventWin && nextEventLose != 0 && nextEvent){
				EventParadise ep = get(nextEventLose);
				if(ep != null) {
					ep.stop();
					ep.stopPlayers();
					ep.eventWin = false;
					ep.nextEvent(false);
				}else stop();
			}else{
				initPlayersAccess();
				goBetween();
				sendMessages(ChatColor.LIGHT_PURPLE+"["+name+"] "+msgNextEvent+" "+nextEventTimer/60+" min "+nextEventTimer%60+" sec.");
			}
		}else stop();
	}
	// ----------------------------- STOP -----------------------------
	public void stop(){ //Plugin.log("stop");
		Bukkit.getScheduler().cancelTask(idScheduleNotice);
		Bukkit.getScheduler().cancelTask(idScheduleGeneral);
		Bukkit.getScheduler().cancelTask(idScheduleBetween);
		idScheduleNotice = 0;
		idScheduleGeneral = 0;
		idScheduleBetween = 0;
		questPlayers.clear();
		if(currentQuest != null) currentQuest.cleanAll();
		autoAcceptQuests.clear();
		for(StateEventPlayer sep : players)
			sep.stop();
		if(mechaStopUnitary != null) mechaStopUnitary.start();
		save();
	}
	
	public void questAccept(StateQuestPlayer sqp) {
		
	}
	
	public void questDecline(StateQuestPlayer sqp) {
		
	}
	// ----------------------------- GIVE REWARD -----------------------------
	public void questGiveReward(StateQuestPlayer sqp) { //Plugin.log("questGiveReward");
		if(firstFinish && !hasFirstFinish && idScheduleBetween == 0 && idScheduleNotice == 0 && idScheduleGeneral != 0){ // Event non terminer
			hasFirstFinish = true;
			for(StateQuestPlayer sqpOther : questPlayers)
				sqpOther.qp.sendMessage(ChatColor.DARK_PURPLE+"["+sqp.getQuest().title+"] "+sqp.qp.getName()+" "+msgFirstFinish);
			finish();
			eventWin = true;
			stop();
			nextEvent();
			hasFirstFinish = false;
		}
		questGiveRewardAutoAccept(sqp);
	}
	
	public void questGiveRewardAutoAccept(StateQuestPlayer sqp) {
		if(autoAccept && sqp.getQuest().hasAccesEvent(sqp.qp, sqp) && idScheduleBetween == 0 && idScheduleNotice == 0 && idScheduleGeneral != 0){
			if(autoAccept){
				List<QuestPlayer> qpAuto = autoAcceptQuests.get(sqp.getQuest());
				if(sqp.getQuest().states.size() == 0){
					if(qpAuto != null)
						for(QuestPlayer qp : qpAuto) sqp.getQuest().getNewState(qp).accept();
					sqp.getQuest().getNewState(sqp.qp).accept();
					autoAcceptQuests.remove(sqp.getQuest());
				}else{
					if(qpAuto == null) {
						qpAuto = new ArrayList<QuestPlayer>();
						autoAcceptQuests.put(sqp.getQuest(), qpAuto);
					}
					qpAuto.add(sqp.qp);
				}
			}else
				sqp.getQuest().getNewState(sqp.qp).accept();
		}
	}
	
	public void terminateObj(StateObjPlayer sop) {
		if(showObjCompleted)
			for(StateQuestPlayer sqp : questPlayers)
				sqp.qp.sendMessage(ChatColor.DARK_PURPLE+sqp.qp.getName()+msgObjCompleted+"\""+sop.getDescriptive()+"\"");
	}
	
	public void quit(QuestPlayer qp){
		if(currentQuest != null)
			quit(qp, currentQuest);
	}
	
	protected void quit(QuestPlayer qp, Quest q){
		StateQuestPlayer sqp = q.states.get(qp);
		if(sqp != null){
			questPlayers.remove(sqp);
			sqp.decline();
		}
	}
	
	private void disable() {
		stopPlayers();
		for(StateQuestPlayer sqp : questPlayers)
			sqp.decline();
	}
	
	public Boolean isStarted() {
		return idScheduleGeneral != 0 || idScheduleBetween != 0 || idScheduleNotice != 0;
	}
	
	private boolean statePreparation = true;
	private boolean stateUnderway = false;
	public void initPreparation(){
		statePreparation = true;
		stateUnderway = false;
	}
	public void initUnderway(){
		statePreparation = false;
		stateUnderway = true;
	}
	public boolean isStatePreparation(){ return statePreparation;}
	public boolean isStateUnderway(){ return stateUnderway;}
	
	public void goNotice(){
		initPreparation();
		idScheduleNotice = Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.it, this, noticeTimer*20);
	}
	public void goGeneral(){
		initUnderway();
		idScheduleGeneral = Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.it, this, generalTimer*20);
	}
	public void goBetween(){
		initPreparation();
		idScheduleBetween = Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.it, this, nextEventTimer*20);
	}
	
	public void sendStop(Player p, Quest q){
		new DataQuestEventParadise(DataQuestEventParadise.STOP, id, q.npc, q.id).send(p);
	}
	
	public void sendNotice(Player p, Quest q){
		new DataQuestEventParadise(DataQuestEventParadise.NOTICE, id, q.npc, q.id, noticeTimer, name.getStr()).send(p);
	}
	
	public void sendBeginning(Player p, Quest q){
		new DataQuestEventParadise(DataQuestEventParadise.BEGINNING, id, q.npc, q.id, generalTimer, name.getStr()).send(p);
	}
	
	public static void onPlayerQuit(Player p) {
		for(Entry<Integer, List<EventParadise>> entry : list.entrySet()){
			for(EventParadise ep : entry.getValue()) ep.quit(QuestPlayer.get(p));
		}
	}
	
	public static void onDisable() {
		for(Entry<Integer, List<EventParadise>> entry : list.entrySet()){
			for(EventParadise ep : entry.getValue()) ep.disable();
		}
	}
	public static EventParadise newEvent(int type){
		if (type == PARADISE) return new EventParadise();
		else if (type == TEAMPLAY) return new EventTeamPlay();
		else if (type == MINUS)	 return new EventMinus();
		Plugin.log("error type : "+type);
		return null;
	}

	public static List<Integer> getListTypeId() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(PARADISE);
		list.add(TEAMPLAY);
		list.add(MINUS);
		return list;
	}
	
	public static List<String> getListType() {
		List<String> list = new ArrayList<String>();
		list.add(STRPARADISE);
		list.add(STRTEAMPLAY);
		list.add(STRMINUS);
		return list;
	}
	
	public boolean equals(Object obj) {
		if(this.getClass() != obj.getClass()) return false;
		EventParadise o = (EventParadise)obj;
		return this.id == o.id;
	}
	// ************************** STATE **************************
	public class StateEventPlayer extends AbsStateContainer{
		
		private IMechaDriver driver;
		public QuestPlayer qp;
		
		public StateEventPlayer(QuestPlayer qp) {
			super(EventParadise.this, qp);
			this.qp = qp;
			driver = new MechaDriverPlayer(qp, EventParadise.this);
		}

		public void start() { if(mechaStart != null) mechaStart.start(driver); }
		
		public void begin() { if(mechaBegin != null) mechaBegin.start(driver); }

		public void finish() {
			if(mechaFinish != null) mechaFinish.start(driver);
			driver.cleanControllers();
		}
		
		public void stop() {
			if(mechaStop != null) mechaStop.start(driver);
			driver.cleanControllers();
		}
		
		public int getId() { return id; }
		@Override
		public boolean equals(Object obj) {
			if(this.getClass() != obj.getClass()) return false;
			StateEventPlayer o = (StateEventPlayer) obj;
			return id == o.getId() && qp.getId() == o.qp.getId();
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

	public int getType(){ return PARADISE; }
	@Override
	public int getTypeContainer() { return EVENT; }
	@Override
	public int getId() { return id; }
	@Override
	public void setId(int id) { this.id = id; }
	@Override
	public IMechaDriver getDriver(QuestPlayer qp) {
		StateEventPlayer sep = getState(qp);
		if(sep == null) return null;
		else return sep.driver;
	}
	
	public StateEventPlayer getState(QuestPlayer qp) {
		for(StateEventPlayer sep : players)
			if(sep.qp.getId() == qp.getId()) return sep;
		return null;
	}
	@Override
	public IMechaDriver newDriver(QuestPlayer qp) {
		StateEventPlayer sep = getState(qp);
		if(sep == null){
			sep = new StateEventPlayer(qp);
			players.add(sep);
			addState(qp, sep);
		}
		return sep.driver;
	}
	@Override
	public StateEventPlayer newStateContainer(QuestPlayer qp) {
		StateEventPlayer sep = getState(qp);
		if(sep != null) return sep;
		return new StateEventPlayer(qp);
	}
}