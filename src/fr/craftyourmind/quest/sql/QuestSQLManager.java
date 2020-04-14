package fr.craftyourmind.quest.sql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;

import fr.craftyourmind.manager.CYMPlayer;
import fr.craftyourmind.manager.CYMReputation;
import fr.craftyourmind.manager.sql.AbsSQLCYMCnx;
import fr.craftyourmind.manager.sql.CYMMySQL;
import fr.craftyourmind.manager.sql.CYMSQLite;
import fr.craftyourmind.quest.AbsObjective;
import fr.craftyourmind.quest.AbsObjective.IStateObj;
import fr.craftyourmind.quest.CatBox;
import fr.craftyourmind.quest.EventParadise;
import fr.craftyourmind.quest.ICatBox;
import fr.craftyourmind.quest.IMekaBox;
import fr.craftyourmind.quest.MekaBox;
import fr.craftyourmind.quest.Plugin;
import fr.craftyourmind.quest.Quest;
import fr.craftyourmind.quest.QuestTag;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;
import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.AbsReward;
import fr.craftyourmind.quest.mecha.AbsMechaContainer;
import fr.craftyourmind.quest.mecha.AbsMechaStateEntity;
import fr.craftyourmind.quest.mecha.IMechaDriver;
import fr.craftyourmind.quest.mecha.Mechanism;
import fr.craftyourmind.quest.mecha.Mechanism.ChildLink;
import fr.craftyourmind.quest.mecha.ToolBox.TOOLMEKABOX;
import fr.craftyourmind.skill.CYMClass;
import fr.craftyourmind.skill.CYMLevel;
import fr.craftyourmind.skill.CYMSkill;
import fr.craftyourmind.skill.CYMTier;
import fr.craftyourmind.skill.StateCYMClass;
import fr.craftyourmind.skill.StateCYMSkill;

public class QuestSQLManager {
	
	static private String name_db;
	static private String base;
	static private String login;
	static private String pass;
	static private String host;

	private static final List<AbsQuestSQL> queries = new ArrayList<AbsQuestSQL>();
	private static final List<AbsQuestSQL> addqueries = Collections.synchronizedList(new ArrayList<AbsQuestSQL>());
	
	public static AbsSQLCYMCnx cymcnx;
	
	private static Thread thread;

	public static void addQuery(AbsQuestSQL query){
		addqueries.add(query);
		if(thread == null){
			thread = new Thread(){

				@Override
				public void run() {
					try {
						Connection cnx = getCnx();
						boolean run = true;
						while(run){
							synchronized (addqueries) {
								queries.addAll(addqueries);
								addqueries.clear();
							}
							for(AbsQuestSQL sql : queries){
								sql.cnx = cnx;
								sql.run();
							}
							queries.clear();
							synchronized (addqueries) {
								if(addqueries.isEmpty()) {
									run = false;
									thread = null;
								}
							}
						}
						cnx.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			};
			thread.start();
		}
	}
	
	static public void init(){
		try {
			File d = Plugin.it.getDataFolder();
			d.mkdir();
			File c = new File(d, "configSQL.yml");
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(c);
			if(c.createNewFile()){
				conf.set("base_type_(sqlite, mysql)", "sqlite");
				conf.set("prefix", "quest_");
				conf.createSection("SQLite");
				conf.set("name_db", "cymquest");
				conf.createSection("MySQL");
				conf.set("base", "minecraft");
				conf.set("login", "root");
				conf.set("pass", "");
				conf.set("host", "localhost");
				conf.save(c);
			}
			String base_type = conf.getString("base_type_(sqlite, mysql)");
			String prefix = conf.getString("prefix");
			name_db = conf.getString("name_db");
			base = conf.getString("base");
			login = conf.getString("login");
			pass = conf.getString("pass");
			host = conf.getString("host");
			AbsQuestSQL.T_QUEST = prefix+"quest";
			AbsQuestSQL.T_QSTATEPLAYER = prefix+"statequestplayer";
			AbsQuestSQL.T_OBJ = prefix+"objective";
			AbsQuestSQL.T_OBJTERM = prefix+"termobjplayer";
			AbsQuestSQL.T_REW = prefix+"reward";
			AbsQuestSQL.T_EVENT = prefix+"event";
			AbsQuestSQL.T_MECHANISM = prefix+"mechanism";
			AbsQuestSQL.T_MECHA = prefix+"mecha";
			AbsQuestSQL.T_STATEMECHA = prefix+"statemechaplayer";
			AbsQuestSQL.T_MEKABOX = prefix+"mekabox";
			AbsQuestSQL.T_CATBOX = prefix+"catbox";
			AbsQuestSQL.T_SKILL = prefix+"skill";
			AbsQuestSQL.T_STATESKILL = prefix+"stateskill";
			AbsQuestSQL.T_CLASS = prefix+"class";
			AbsQuestSQL.T_STATECLASS = prefix+"stateclass";
			AbsQuestSQL.T_LEVEL = prefix+"level";
			AbsQuestSQL.T_TIER = prefix+"tier";
			AbsQuestSQL.T_TAG = prefix+"tag";
			AbsQuestSQL.T_STATETAG = prefix+"statetag";
			
			if(base_type.equalsIgnoreCase("mysql"))
				cymcnx = new CYMMySQL(base, login, pass, host);
			else if(base_type.equalsIgnoreCase("sqlite"))
				cymcnx = new CYMSQLite(Plugin.it.getDataFolder().getPath()+File.separator, name_db);
			
			if(cymcnx == null){
				Plugin.log("Error type data base.");
				return;
			}
			
			//createBase();
			createTable();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static protected Connection getCnx() throws SQLException{
		return cymcnx.getCnx();
	}
	
	static public void load(){
		try {
			Plugin.log("Quest SQL Load ...");
			
			if(cymcnx == null){
				Plugin.log("Error type data base.");
				return;
			}
			
			Connection cnx = getCnx();
			Statement state1 = cnx.createStatement();
			Statement state2 = cnx.createStatement();
			Statement state3 = cnx.createStatement();
			Statement state4 = cnx.createStatement();
			Statement state5 = cnx.createStatement();
			Statement state6 = cnx.createStatement();
			Statement state7 = cnx.createStatement();
			Statement state8 = cnx.createStatement();
			Statement state9 = cnx.createStatement();
			Statement state10 = cnx.createStatement();
			Statement state11 = cnx.createStatement();
			Statement state12 = cnx.createStatement();
			Statement state13 = cnx.createStatement();
			Statement state14 = cnx.createStatement();
			Statement state15 = cnx.createStatement();
			Statement state16 = cnx.createStatement();
			Statement state17 = cnx.createStatement();
			Statement state18 = cnx.createStatement();
			Statement state19 = cnx.createStatement();
			Statement state20 = cnx.createStatement();
			
			AbsQuestSQL.init(cnx);
			
			List<Mechanism> mechaList = new ArrayList<Mechanism>();
			
			// TAG
			ResultSet rsTag = state20.executeQuery("select * FROM "+AbsQuestSQL.T_TAG);
			while(rsTag.next()){
				QuestTag qt = new QuestTag(rsTag.getInt("id"));
				qt.name = rsTag.getString("name");
				qt.hidden = rsTag.getBoolean("hidden");
			}
			// QUEST PLAYER
			QuestPlayer.newNobody();
			for(CYMPlayer mp : CYMPlayer.getList()){
				new QuestPlayer(mp);
			}
			// TIER
			ResultSet rsTier = state15.executeQuery("select * FROM "+AbsQuestSQL.T_TIER);
			while(rsTier.next()){
				CYMTier tier = CYMTier.newCYMTierSql();
				tier.setId(rsTier.getInt("id"));
				tier.setName(rsTier.getString("name"));
				tier.limit = rsTier.getInt("limitLevel");
			}
			// LEVEL
			ResultSet rsLvl = state14.executeQuery("select * FROM "+AbsQuestSQL.T_LEVEL);
			while(rsLvl.next()){
				CYMLevel level = CYMLevel.newCYMLevelSql();
				level.setId(rsLvl.getInt("id"));
				level.setName(rsLvl.getString("name"));
				level.lvlBegin = rsLvl.getInt("lvlBegin");
				level.lvlEnd = rsLvl.getInt("lvlEnd");
				level.baseXP = rsLvl.getInt("baseXP");
				level.coefMulti = rsLvl.getFloat("coefMulti");
			}
			// CLASS
			List<CYMClass> classes = new ArrayList<CYMClass>();
			ResultSet rsClass = state16.executeQuery("select * FROM "+AbsQuestSQL.T_CLASS);
			while(rsClass.next()){
				CYMClass mc = CYMClass.newCYMClassSql(rsClass.getString("name"));
				mc.setId(rsClass.getInt("id"));
				mc.limitSkill = rsClass.getInt("limitSkill");
				mc.setActivate(rsClass.getBoolean("activate"));
				mc.setKeepEnableOnLink(rsClass.getBoolean("keepEnableOnLink"));
				mc.setShowPlayer(rsClass.getBoolean("showPlayer"));
				mc.setActivationPlayer(rsClass.getBoolean("activationPlayer"));
				mc.setShowMessage(rsClass.getBoolean("showMessage"));
				mc.setDescriptives(rsClass.getString("descriptives"));
				mc.getSkillManager().strLinkChilds = rsClass.getString("linkChilds");
				mc.setLevels(rsClass.getString("levels"));
				mc.setTiers(rsClass.getString("tiers"));
				mc.getSkillManager().strNodeChilds = rsClass.getString("nodeChilds");
				mc.setLimitNode(rsClass.getInt("limitNode"));
				mc.setSyncNodeParents(rsClass.getBoolean("syncNodeParents"));
				mc.loadParamsCon(rsClass.getString("params"));
				mc.setOrder(rsClass.getInt("listOrder"));
				mc.initParamSys();
				// STATE CLASS
				ResultSet rsState = state18.executeQuery("SELECT * FROM "+AbsQuestSQL.T_STATECLASS+" WHERE idClass = "+mc.getId());
				while(rsState.next()){
					QuestPlayer qp = QuestPlayer.get(rsState.getInt("idPlayer"));
					if(qp != null){
						StateCYMClass smc = mc.loadStateContainer(qp);
						smc.setActivate(rsState.getBoolean("activate"));
						smc.setLevelNoSave(rsState.getInt("level"));
						smc.setXpNoSave(rsState.getInt("xp"));
						smc.setParams(rsState.getString("params"));
						if(smc.isActivated()) smc.cloneData();
					}
				}
				loadMechanism(mechaList, state9, AbsMechaContainer.CLASS, mc.getId());
				if(mc.getOrder() == 0){ // update
					mc.setOrder(mc.getSortList().size());
					mc.save();
				}
				classes.add(mc);
			}
			CYMClass.initPlayerClass();
			for(CYMClass mc : classes) mc.init();
			// SKILL
			List<CYMSkill> skills = new ArrayList<CYMSkill>();
			ResultSet rsSkill = state17.executeQuery("select * FROM "+AbsQuestSQL.T_SKILL);
			while(rsSkill.next()){
				CYMSkill ms = CYMSkill.newCYMSkillSql(rsSkill.getInt("id"), rsSkill.getString("name"), rsSkill.getInt("idclass"));
				ms.levelClassActivated = rsSkill.getInt("levelClassActivated");
				ms.setActivate(rsSkill.getBoolean("activate"));
				ms.setKeepEnableOnLink(rsSkill.getBoolean("keepEnableOnLink"));
				ms.setShowPlayer(rsSkill.getBoolean("showPlayer"));
				ms.setActivationPlayer(rsSkill.getBoolean("activationPlayer"));
				ms.setShowMessage(rsSkill.getBoolean("showMessage"));
				ms.setDescriptives(rsSkill.getString("descriptives"));
				ms.getSkillManager().strLinkChilds = rsSkill.getString("linkChilds");
				ms.setLevels(rsSkill.getString("levels"));
				ms.setTiers(rsSkill.getString("tiers"));
				ms.getSkillManager().strNodeChilds = rsSkill.getString("nodeChilds");
				ms.setLimitNode(rsSkill.getInt("limitNode"));
				ms.setSyncNodeParents(rsSkill.getBoolean("syncNodeParents"));
				ms.loadParamsCon(rsSkill.getString("params"));
				ms.setOrder(rsSkill.getInt("listOrder"));
				ms.initParamSys();
				// STATE SKILL
				ResultSet rsState = state19.executeQuery("SELECT * FROM "+AbsQuestSQL.T_STATESKILL+" WHERE idSkill = "+ms.getId());
				while(rsState.next()){
					QuestPlayer qp = QuestPlayer.get(rsState.getInt("idPlayer"));
					if(qp != null){
						StateCYMSkill sms = ms.loadStateContainer(qp);
						sms.setActivate(rsState.getBoolean("activate"));
						sms.setLevelNoSave(rsState.getInt("level"));
						sms.setXpNoSave(rsState.getInt("xp"));
						sms.setParams(rsState.getString("params"));
						if(sms.isActivated()) sms.cloneData();
						StateCYMClass smc = ms.getCatbox().getState(qp);
						if(smc != null) smc.addCYMSkill(sms);
					}
				}
				loadMechanism(mechaList, state9, AbsMechaContainer.SKILL, ms.getId());
				ms.initStarter();
				if(ms.getOrder() == 0){ // update
					ms.setOrder(ms.getSortList().size());
					ms.save();
				}
				skills.add(ms);
			}
			for(CYMSkill ms : skills) ms.init();
			// CATBOX
			ResultSet rsCat = state12.executeQuery("select * FROM "+AbsQuestSQL.T_CATBOX);
			while(rsCat.next()){
				ICatBox cb = CatBox.newCatboxSql(rsCat.getInt("type"), rsCat.getInt("id"), rsCat.getString("name"));
				cb.setOrder(rsCat.getInt("listOrder"));
				if(cb.getOrder() == 0){ // update
					cb.setOrder(cb.getSortList().size()-1);
					cb.save();
				}
			}
			// MEKABOX
			ResultSet rsBox = state13.executeQuery("select * FROM "+AbsQuestSQL.T_MEKABOX);
			while(rsBox.next()){
				IMekaBox box = MekaBox.newMekaboxSql(rsBox.getInt("type"), rsBox.getString("name"), rsBox.getInt("idCat"), rsBox.getInt("id"));
				box.loadParamsCon(rsBox.getString("param"));
				box.setOrder(rsBox.getInt("listOrder"));
				// MECHANISM
				loadMechanism(mechaList, state9, AbsMechaContainer.MEKABOX, box.getId());
				box.init();
				if(box.getOrder() == 0){ // update
					box.setOrder(box.getSortList().size());
					box.save();
				}
			}
			// EVENT
			ResultSet rsEvent = state1.executeQuery("select * FROM "+AbsQuestSQL.T_EVENT);
			while(rsEvent.next()){
				EventParadise ep = EventParadise.newEvent(rsEvent.getInt("type"));
				ep.id = rsEvent.getInt("id");
				ep.name.setStrUnlock(rsEvent.getString("name"));
				ep.flow = rsEvent.getInt("flow");
				ep.preparation = rsEvent.getBoolean("preparation");
				ep.firstFinish = rsEvent.getBoolean("firstFinish");
				ep.openQuest = rsEvent.getBoolean("openQuest");
				ep.showObjCompleted = rsEvent.getBoolean("showObjCompleted");
				ep.noticeTimer = rsEvent.getInt("startTimer");
				ep.noticeMessage = rsEvent.getString("startMessage");
				ep.beginMessage = rsEvent.getString("beginMessage");
				ep.generalTimer = rsEvent.getInt("generalTimer");
				ep.nextEventTimer = rsEvent.getInt("nextEventTimer");
				ep.setWorld(rsEvent.getString("world"));
				ep.x = rsEvent.getInt("x");
				ep.z = rsEvent.getInt("z");
				ep.radius = rsEvent.getInt("radius");
				ep.loadParamsCon(rsEvent.getString("params"));
				ep.minPlayers = rsEvent.getInt("minPlayers");
				ep.maxPlayers = rsEvent.getInt("maxPlayers");
				ep.nextEventWin = rsEvent.getInt("nextEventWin");
				ep.nextEventLose = rsEvent.getInt("nextEventLose");
				ep.autoAccept = rsEvent.getBoolean("autoAccept");
				ep.sqlSetSlot(rsEvent.getInt("slot"));
				ep.addList();
				// MECHANISM
				loadMechanism(mechaList, state9, AbsMechaContainer.EVENT, ep.id);
				ep.init();
				if(rsEvent.getBoolean("isStarted")) ep.start();
			}
			// QUEST
			Map<Integer, Quest> listParentQuest = new HashMap<Integer, Quest>();
			ResultSet rsQuest = state2.executeQuery("select * FROM "+AbsQuestSQL.T_QUEST);
			while(rsQuest.next()){
				Quest q = new Quest();
				q.id = rsQuest.getInt("id");
				q.npc = rsQuest.getInt("idNPC");
				q.repute = CYMReputation.getById(rsQuest.getInt("idRepute"));
				q.setEvent(EventParadise.get(rsQuest.getInt("idEvent")));
				q.repeatable = rsQuest.getBoolean("repeatable");
				q.setRepeateTime(rsQuest.getString("repeateTime"));
				q.levelMin = rsQuest.getInt("levelMin");
				q.levelMax = rsQuest.getInt("levelMax");
				q.reputeMin = rsQuest.getInt("reputeMin");
				q.reputeMax = rsQuest.getInt("reputeMax");
				q.objInTheOrder = rsQuest.getBoolean("objInTheOrder");
				q.common = rsQuest.getBoolean("common");
				q.playersMax = rsQuest.getInt("playersMax");
				q.title.setStrUnlock(rsQuest.getString("title"));
				q.introTxt = rsQuest.getString("introTxt");
				q.fullTxt = rsQuest.getString("fullTxt");
				q.successTxt = rsQuest.getString("successTxt");
				q.loseTxt = rsQuest.getString("loseTxt");
				q.displayIconNPC = rsQuest.getBoolean("displayIconNPC");
				q.loadParamsCon(rsQuest.getString("params"));
				q.setIcons(rsQuest.getString("icons"));
				q.cymClass = rsQuest.getInt("idClass");
				q.nameParamClass = rsQuest.getString("nameParamClass"); if(q.nameParamClass == null) q.nameParamClass = "";
				q.classMin = rsQuest.getFloat("classMin");
				q.classMax = rsQuest.getFloat("classMax");
				q.init();
				// OBJECTIVE
				ResultSet rsObj = state4.executeQuery("select * FROM "+AbsQuestSQL.T_OBJ+" WHERE idQuest = "+q.id);
				while(rsObj.next()){
					AbsObjective obj = AbsObjective.newObjective(q, rsObj.getInt("type"));
					obj.id = rsObj.getInt("id");
					obj.descriptive = rsObj.getString("descriptive");
					obj.success = rsObj.getString("success");
					obj.setFinishQuest(rsObj.getBoolean("finishQuest"));
					obj.loadParams(rsObj.getString("params"));
					q.setObjective(obj);					
				}
				// REWARD
				ResultSet rsRew = state6.executeQuery("select * FROM "+AbsQuestSQL.T_REW+" WHERE idQuest = "+q.id);
				while(rsRew.next()){
					AbsReward rew = AbsReward.newReward(q, rsRew.getInt("type"));
					rew.id = rsRew.getInt("id");
					rew.descriptive = rsRew.getString("descriptive");
					rew.amount = rsRew.getInt("amount");
					rew.loadParams(rsRew.getString("params"));
					q.setReward(rew);
				}
				// STATE TAG
				ResultSet rsStatetag = state9.executeQuery("SELECT * FROM "+AbsQuestSQL.T_STATETAG+" WHERE idQuest = "+q.id);
				while(rsStatetag.next()){
					QuestTag qt = QuestTag.get(rsStatetag.getInt("idTag"));
					if(qt != null){
						q.tags.add(qt);
						qt.quests.add(q);
					}
				}
				// QUEST PLAYER STATE
				ResultSet rsState = state3.executeQuery("SELECT * FROM "+AbsQuestSQL.T_QSTATEPLAYER+" WHERE idQuest = "+q.id);
				while(rsState.next()){
					QuestPlayer qp = QuestPlayer.get(rsState.getInt("idPlayer"));
					if(qp != null){
						StateQuestPlayer sqp = q.getNewState(qp);
						if(rsState.getBoolean("beginning")){
							sqp.setBeginning(true);
							qp.addQuestCurrent(q);
						}else if(rsState.getBoolean("terminate")){
							sqp.setTerminate(true);
							qp.addQuestFinished(q);
						}
						if(q.repeatable)
							q.addTimer(rsState.getLong("begintime") + q.repeateTime, sqp);
						
						sqp.idQuestChild = rsState.getInt("questchild");
						sqp.setParams(rsState.getString("params"));
						
						// OBJECTIVE TERMINATE
						for(IStateObj so : sqp.objs){
							ResultSet rsObjTerm = state5.executeQuery("SELECT * FROM `"+AbsQuestSQL.T_OBJTERM+"` where idPlayer = "+qp.getId()+" AND idObjective = "+so.getId());
							while(rsObjTerm.next()){
								so.setTerminate(true);
							}
						}
					}
				}
				// LIST QUEST NPC
				List<Quest> lq = Quest.listNPC.get(q.npc);
				if(lq == null){
					lq = new ArrayList<Quest>();
					Quest.listNPC.put(q.npc, lq);
				}
				lq.add(q);
				// BEGIN OBJECTIF
				for(StateQuestPlayer sqp : q.states.values()){
					if(!sqp.isTerminate()){
						int i = 0;
						for(IStateObj so : sqp.objs){
							if(!so.isTerminate()){	
								if(i != 0 && q.objInTheOrder) so.setLock(true);
								if(!so.isLock()) so.begin();
							}
							i++;
						}
					}
				}
				// MECHANISM
				loadMechanism(mechaList, state9, AbsMechaContainer.QUEST, q.id);
				listParentQuest.put(q.id, q);
			}
			// QUEST PARENT
			ResultSet rsQParent = state7.executeQuery("select * from "+AbsQuestSQL.T_QUEST+" WHERE parent != 0");
			while(rsQParent.next()){
				int id = rsQParent.getInt("id");
				int parent = rsQParent.getInt("parent");
				Quest q = listParentQuest.get(id);
				if(q != null){
					Quest qParent = listParentQuest.get(parent);
					if(q != null) q.parent = qParent;
				}
			}
			// QUEST EVENT LOAD PARAMS
			ResultSet rsParams = state8.executeQuery("select * from "+AbsQuestSQL.T_EVENT);
			while(rsParams.next()){
				EventParadise ep = EventParadise.get(rsParams.getInt("id"));
				if(ep != null) ep.loadParamsCon(rsParams.getString("params"));
			}
			// MECHA LINK
			for(Mechanism m : mechaList){
				ResultSet rsMecha = state10.executeQuery("select * from "+AbsQuestSQL.T_MECHA+" where idLauncher = "+m.id);
				while(rsMecha.next()){					
					Mechanism child = get(mechaList, rsMecha.getInt("idLaunch"));
					if(child != null){
						ChildLink cl = m.newChildLink(child, rsMecha.getInt("slot"));
						cl.setOrder(rsMecha.getInt("listOrder"));
						if(cl.getOrder() == 0){ // update
							cl.questSort.setOrder(cl.getSortList().size()+1);
							cl.save();
						}
						m.childLinks.add(cl);
						child.launchers.add(m);
					}
				}
			}
			// STATE MECHA
			for(Mechanism m : mechaList){
				ResultSet rsSM = state11.executeQuery("select * from "+AbsQuestSQL.T_STATEMECHA+" where idMecha = "+m.id);
				while(rsSM.next()){
					int idDriver = rsSM.getInt("idDriver");
					int typeDriver = rsSM.getInt("typeDriver");
					if(idDriver == 0 && typeDriver == 0) // update
						stop(rsSM.getInt("idPlayer"), rsSM.getInt("idMecha"), typeDriver, idDriver);
					if(typeDriver != AbsMechaContainer.TOOLBOX)
						m.addState(rsSM.getInt("idPlayer"));
				}
			}
			
			cnxLoad = cnx;
			for(Mechanism m : mechaList) m.init();
			cnxLoad = null;
			cnx.close();
			Plugin.log("Quest SQL Load Finish ! !");
		} catch (SQLException e) { e.printStackTrace(); }
	}

	private static Mechanism get(List<Mechanism> mechaList, int id){
		for(Mechanism m : mechaList)
			if(m.id == id) return m;
		return null;
	}
	
	private static void loadMechanism(List<Mechanism> mechaList, Statement state, int type, int id) throws SQLException{
		// MECHANISM
		ResultSet rsMechanism = state.executeQuery("select * from "+AbsQuestSQL.T_MECHANISM+" where typeDriver = "+type+" and idDriver = "+id);
		while(rsMechanism.next()){
			Mechanism m = Mechanism.newMechanism(rsMechanism.getInt("type"), rsMechanism.getInt("typeDriver"), rsMechanism.getInt("idDriver"), rsMechanism.getInt("category"));
			if(m != null){
				m.id = rsMechanism.getInt("id");
				m.common = rsMechanism.getBoolean("common");
				m.permanent = rsMechanism.getBoolean("permanent");
				m.single = rsMechanism.getBoolean("single");
				m.name = rsMechanism.getString("name");
				m.message.load(rsMechanism.getString("message"));
				m.loadParams(rsMechanism.getString("params"));
				mechaList.add(m);
			}
		}
	}
	
	private static Connection cnxLoad; // sqlite multiple connexion bug
	public static void updateStateMecha(TOOLMEKABOX toolBox){
		try{
			Connection cnx = null;
			Statement state = null;
			if(cnxLoad == null){
				cnx = getCnx();
				state = cnx.createStatement();
			}else
				state = cnxLoad.createStatement();
			
			ResultSet rs = state.executeQuery("select * from "+AbsQuestSQL.T_STATEMECHA+" where typeDriver = "+toolBox.getTypeContainer()+" and idDriver = "+toolBox.getId());
			while(rs.next()){
				Mechanism m = toolBox.getMecha(rs.getInt("idMecha"));
				if(m != null){
					QuestPlayer qp = QuestPlayer.get(rs.getInt("idPlayer"));
					IMechaDriver driver = toolBox.getContainer().getDriver(qp);
					if(driver != null)
						m.start(driver);
				}
			}
			if(cnx != null) cnx.close();
		} catch (SQLException e) { e.printStackTrace(); }
	}
	
	static private void createBase(){
		try {
			Connection cnx = DriverManager.getConnection(host, login, pass);
			Statement state = cnx.createStatement();
			state.executeUpdate("CREATE DATABASE IF NOT EXISTS "+base);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	static private void createTable(){
		Connection cnx = null;
		try {
			if(cymcnx == null){
				Plugin.log("Error type data base.");
				return;
			}
			cnx = getCnx();
			Statement state = cnx.createStatement();

			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_OBJ).addPK("id").addAI("id").addInt("id").addInt("idQuest").addInt("type").addVarC("descriptive", 100).addVarC("success", 100).addBool("finishQuest").addTxt("params").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_QUEST).addPK("id").addAI("id").addInt("id").addInt("idNPC").addInt("idRepute").addInt("idEvent").addInt("parent").addInt("idClass").addBool("repeatable").addBool("repeateTimeAccept").addDefaultValue("1").addBool("repeateTimeGive").addDefaultValue("0").addVarC("repeateTime", 11).addDefaultValue("00:00:00:00").addInt("levelMin").addInt("levelMax")
					.addInt("reputeMin").addInt("reputeMax").addBool("objInTheOrder").addBool("common").addInt("playersMax").addVarC("title", 150).addTxt("introTxt").addTxt("fullTxt").addTxt("successTxt").addTxt("loseTxt")
					.addBool("displayIconNPC").addDefaultValue("1").addTxt("params").addTxt("icons").addTxt("nameParamClass").addFloat("classMin").addFloat("classMax").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_REW).addPK("id").addAI("id").addInt("id").addInt("idQuest").addInt("type").addVarC("descriptive", 100).addInt("amount").addTxt("params").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_QSTATEPLAYER).addPK("idPlayer").addPK("idQuest").addInt("idPlayer").addInt("idQuest").addBool("beginning").addBool("terminate").addLong("begintime").addInt("questchild").addTxt("params").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_OBJTERM).addPK("idPlayer").addPK("idObjective").addInt("idPlayer").addInt("idObjective").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_EVENT).addPK("id").addAI("id").addInt("id").addInt("type").addVarC("name", 100).addBool("flow").addBool("preparation").addBool("openQuest").addBool("firstFinish").addBool("showObjCompleted")
					.addInt("startTimer").addTxt("startMessage").addTxt("beginMessage").addInt("generalTimer").addInt("nextEventTimer").addVarC("world", 100).addInt("x").addInt("z").addInt("radius").addTxt("params").addInt("minPlayers").addInt("maxPlayers")
					.addInt("nextEventWin").addInt("nextEventLose").addBool("autoAccept").addBool("isStarted").addInt("slot").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_MECHANISM).addPK("id").addAI("id").addInt("id").addInt("typeDriver").addInt("idDriver").addInt("category").addInt("type").addBool("common").addBool("permanent").addBool("single").addVarC("name", 100).addTxt("message").addTxt("params").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_MECHA).addPK("idLauncher").addPK("idLaunch").addInt("idLauncher").addInt("idLaunch").addInt("slot").addInt("listOrder").addDefaultValue("0").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_STATEMECHA).addPK("idPlayer").addPK("idMecha").addPK("typeDriver").addPK("idDriver").addInt("idPlayer").addInt("idMecha").addInt("typeDriver").addDefaultValue("0").addInt("idDriver").addDefaultValue("0").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_MEKABOX).addPK("id").addAI("id").addInt("id").addVarC("name", 100).addInt("type").addInt("idCat").addTxt("param").addInt("listOrder").addDefaultValue("0").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_CATBOX).addPK("id").addAI("id").addInt("id").addVarC("name", 100).addInt("type").addInt("listOrder").addDefaultValue("0").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_SKILL).addPK("id").addAI("id").addInt("id").addInt("idclass").addVarC("name", 100).addInt("levelClassActivated").addBool("activate").addBool("keepEnableOnLink").addBool("showPlayer").addBool("activationPlayer").addBool("showMessage").addDefaultValue("1").addTxt("descriptives").addTxt("linkChilds").addTxt("levels").addTxt("tiers")
					.addTxt("nodeChilds").addInt("limitNode").addBool("syncNodeParents").addTxt("params").addInt("listOrder").addDefaultValue("0").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_STATESKILL).addPK("idPlayer").addPK("idSkill").addInt("idPlayer").addInt("idSkill").addBool("activate").addInt("level").addInt("xp").addTxt("params").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_CLASS).addPK("id").addAI("id").addInt("id").addVarC("name", 100).addInt("limitSkill").addBool("activate").addBool("keepEnableOnLink").addBool("showPlayer").addBool("activationPlayer").addBool("showMessage").addDefaultValue("1").addTxt("descriptives").addTxt("linkChilds").addTxt("levels").addTxt("tiers")
					.addTxt("nodeChilds").addInt("limitNode").addBool("syncNodeParents").addTxt("params").addInt("listOrder").addDefaultValue("0").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_STATECLASS).addPK("idPlayer").addPK("idClass").addInt("idPlayer").addInt("idClass").addBool("activate").addInt("level").addInt("xp").addTxt("params").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_LEVEL).addPK("id").addAI("id").addInt("id").addVarC("name", 100).addInt("lvlBegin").addInt("lvlEnd").addInt("baseXP").addFloat("coefMulti").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_TIER).addPK("id").addAI("id").addInt("id").addVarC("name", 100).addInt("limitLevel").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_TAG).addPK("id").addAI("id").addInt("id").addVarC("name", 100).addBool("hidden").sqlCreate());
			state.executeUpdate(cymcnx.init(AbsQuestSQL.T_STATETAG).addPK("idTag").addPK("idQuest").addInt("idTag").addInt("idQuest").sqlCreate());
			
		} catch (SQLException e) {
			cymcnx.errorCreate(e);
		}
		try { cnx.close(); } catch (SQLException e) { }
	}
	
	public static void create(Quest q) {
		new SQLQuest(AbsQuestSQL.CREATE, q).initID().go();
	}
	public static void create(AbsObjective obj) {
		new SQLObjective(AbsQuestSQL.CREATE, obj).initID().go();
	}
	public static void create(AbsReward rew) {
		new SQLReward(AbsQuestSQL.CREATE, rew).initID().go();
	}
	
	static public void save(Quest q){
		new SQLQuest(AbsQuestSQL.SAVE, q).go();
	}
	public static void save(AbsObjective obj) {
		new SQLObjective(AbsQuestSQL.SAVE, obj).go();
	}
	public static void save(AbsReward rew) {
		new SQLReward(AbsQuestSQL.SAVE, rew).go();
	}
	
	public static void delete(Quest q) {
		new SQLQuest(AbsQuestSQL.DELETE, q).go();
	}
	public static void delete(AbsObjective obj) {
		new SQLObjective(AbsQuestSQL.DELETE, obj).go();
	}
	public static void delete(AbsReward rew) {
		new SQLReward(AbsQuestSQL.DELETE, rew).go();
	}	
	
	static public void accept(Quest q, StateQuestPlayer sqp, QuestPlayer qP){
		new SQLQuest(AbsQuestSQL.ACCEPT, q, qP, sqp).go();
	}
	public static void decline(Quest q, QuestPlayer qP) {
		new SQLQuest(AbsQuestSQL.DECLINE, q, qP).go();
	}
	public static void terminate(Quest q, QuestPlayer qP) {
		new SQLQuest(AbsQuestSQL.TERMINATE, q, qP).go();
	}
	public static void repeat(Quest q, QuestPlayer qP) {
		new SQLQuest(AbsQuestSQL.REPEAT, q, qP).go();
	}
	public static void questchild(Quest q, StateQuestPlayer sqp) {
		new SQLQuest(AbsQuestSQL.QUESTCHILD, q, sqp.qp, sqp).go();
	}
	public static void updateParams(Quest q, StateQuestPlayer sqp) {
		new SQLQuest(AbsQuestSQL.UPDATESTATE, q, sqp.qp, sqp).go();
	}
	
	public static void terminate(AbsObjective obj, QuestPlayer qP) {
		new SQLObjective(AbsQuestSQL.TERMINATE, obj, qP).go();
	}
	public static void clean(AbsObjective obj, QuestPlayer qP) {
		new SQLObjective(AbsQuestSQL.CLEAN, obj, qP).go();
	}

	public static void create(EventParadise ep) {
		new SQLEvent(AbsQuestSQL.CREATE, ep).initID().go();
	}
	public static void save(EventParadise ep) {
		new SQLEvent(AbsQuestSQL.SAVE, ep).go();
	}
	public static void delete(EventParadise ep) {
		new SQLEvent(AbsQuestSQL.DELETE, ep).go();
	}

	public static void create(Mechanism m) {
		new SQLMechanism(AbsQuestSQL.CREATE, m).initID().go();
	}

	public static void save(Mechanism m) {
		new SQLMechanism(AbsQuestSQL.SAVE, m).go();
	}

	public static void delete(Mechanism m) {
		new SQLMechanism(AbsQuestSQL.DELETE, m).go();
	}

	public static void link(Mechanism m, ChildLink child) {
		new SQLMechanism(AbsQuestSQL.LINK, m, child).go();
	}

	public static void delLink(Mechanism m, ChildLink child) {
		new SQLMechanism(AbsQuestSQL.DELLINK, m, child).go();
	}
	
	public static void linkUpdate(Mechanism m, ChildLink child) { 
		new SQLMechanism(AbsQuestSQL.UPDATESTATE, m, child).go();
	}

	public static void start(AbsMechaStateEntity s) {
		new SQLMechanism(AbsQuestSQL.START, s).go();
	}

	public static void stop(AbsMechaStateEntity s) {
		new SQLMechanism(AbsQuestSQL.STOP, s).go();
	}
	
	private static void stop(int idPlayer, int idMecha, int typeDriver, int idDriver) {
		new SQLMechanism(AbsQuestSQL.STOP, idPlayer, idMecha, typeDriver, idDriver).go();
	}

	public static void create(MekaBox box) {
		new SQLMekabox(AbsQuestSQL.CREATE, box).initID().go();
	}

	public static void save(MekaBox box) {
		new SQLMekabox(AbsQuestSQL.SAVE, box).go();
	}

	public static void delete(MekaBox box) {
		new SQLMekabox(AbsQuestSQL.DELETE, box).go();
	}

	public static void create(CatBox cat) {
		new SQLCatbox(AbsQuestSQL.CREATE, cat).initID().go();
	}

	public static void save(CatBox cat) {
		new SQLCatbox(AbsQuestSQL.SAVE, cat).go();
	}

	public static void delete(CatBox cat) {
		new SQLCatbox(AbsQuestSQL.DELETE, cat).go();
	}

	public static void create(CYMClass cls) {
		new SQLClass(AbsQuestSQL.CREATE, cls).initID().go();
	}

	public static void save(CYMClass cls) {
		new SQLClass(AbsQuestSQL.SAVE, cls).go();
	}

	public static void delete(CYMClass cls) {
		new SQLClass(AbsQuestSQL.DELETE, cls).go();
	}
	
	public static void create(CYMSkill skill) {
		new SQLSkill(AbsQuestSQL.CREATE, skill).initID().go();
	}

	public static void save(CYMSkill skill) {
		new SQLSkill(AbsQuestSQL.SAVE, skill).go();
	}

	public static void delete(CYMSkill skill) {
		new SQLSkill(AbsQuestSQL.DELETE, skill).go();
	}
	
	public static void create(CYMLevel level) {
		new SQLLevel(AbsQuestSQL.CREATE, level).initID().go();
	}

	public static void save(CYMLevel level) {
		new SQLLevel(AbsQuestSQL.SAVE, level).go();
	}

	public static void delete(CYMLevel level) {
		new SQLLevel(AbsQuestSQL.DELETE, level).go();
	}
	
	public static void create(CYMTier tier) {
		new SQLTier(AbsQuestSQL.CREATE, tier).initID().go();
	}

	public static void save(CYMTier tier) {
		new SQLTier(AbsQuestSQL.SAVE, tier).go();
	}

	public static void delete(CYMTier tier) {
		new SQLTier(AbsQuestSQL.DELETE, tier).go();
	}

	public static void createState(StateCYMClass stateClass, QuestPlayer qp) {
		new SQLClass(AbsQuestSQL.CREATESTATE, stateClass, qp).go();
	}
	
	public static void updateState(StateCYMClass stateClass, QuestPlayer qp) {
		new SQLClass(AbsQuestSQL.UPDATESTATE, stateClass, qp).go();
	}
	
	public static void deleteState(StateCYMClass stateClass, QuestPlayer qp) {
		new SQLClass(AbsQuestSQL.DELETESTATE, stateClass, qp).go();
	}
	
	public static void createState(StateCYMSkill stateSkill, QuestPlayer qp) {
		new SQLSkill(AbsQuestSQL.CREATESTATE, stateSkill, qp).go();
	}
	
	public static void updateState(StateCYMSkill stateSkill, QuestPlayer qp) {
		new SQLSkill(AbsQuestSQL.UPDATESTATE, stateSkill, qp).go();
	}
	
	public static void deleteState(StateCYMSkill stateSkill, QuestPlayer qp) {
		new SQLSkill(AbsQuestSQL.DELETESTATE, stateSkill, qp).go();
	}

	public static void create(QuestTag qt) {
		new SQLQuestTag(AbsQuestSQL.CREATE, qt).initID().go();
	}

	public static void save(QuestTag qt) {
		new SQLQuestTag(AbsQuestSQL.SAVE, qt).go();
	}

	public static void delete(QuestTag qt) {
		new SQLQuestTag(AbsQuestSQL.DELETE, qt).go();
	}
	
	public static void createState(QuestTag qt, Quest q) {
		new SQLQuestTag(AbsQuestSQL.CREATESTATE, qt, q).go();
	}
	
	public static void deleteState(QuestTag qt, Quest q) {
		new SQLQuestTag(AbsQuestSQL.DELETESTATE, qt, q).go();
	}
}