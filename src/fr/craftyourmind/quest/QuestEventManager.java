package fr.craftyourmind.quest;

import java.util.ArrayList;
import java.util.List;

import fr.craftyourmind.manager.util.INPCEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import fr.craftyourmind.quest.AbsObjective.IStateObj;
import fr.craftyourmind.quest.AbsReward.IStateRew;
import fr.craftyourmind.quest.ObjectiveAnswer.StateAnswer;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;
import fr.craftyourmind.quest.command.CmdChunkPosition;
import fr.craftyourmind.quest.event.QuestEditEvent;
import fr.craftyourmind.quest.event.QuestEvent;
import fr.craftyourmind.quest.event.QuestEventParadiseEditEvent;
import fr.craftyourmind.quest.event.QuestEventParadiseEvent;
import fr.craftyourmind.quest.event.QuestMechaEvent;
import fr.craftyourmind.quest.event.QuestModScreenEvent;
import fr.craftyourmind.quest.event.QuestNpcStateEvent;
import fr.craftyourmind.quest.event.QuestObjActionEvent;
import fr.craftyourmind.quest.event.QuestObjEvent;
import fr.craftyourmind.quest.event.QuestObjOpenEvent;
import fr.craftyourmind.quest.event.QuestRewEvent;
import fr.craftyourmind.quest.event.QuestRewOpenEvent;
import fr.craftyourmind.quest.event.QuestScreenEvent;
import fr.craftyourmind.quest.mecha.AbsMechaContainer;
import fr.craftyourmind.quest.mecha.ActionChest;
import fr.craftyourmind.quest.mecha.ActionInventory;
import fr.craftyourmind.quest.mecha.ActionPopEntity;
import fr.craftyourmind.quest.mecha.IMechaContainer;
import fr.craftyourmind.quest.mecha.MechaCat;
import fr.craftyourmind.quest.mecha.MechaDriverEntity;
import fr.craftyourmind.quest.mecha.MechaType;
import fr.craftyourmind.quest.mecha.Mechanism;
import fr.craftyourmind.quest.mecha.Mechanism.ChildLink;
import fr.craftyourmind.quest.packet.DataQuest;
import fr.craftyourmind.quest.packet.DataQuestEdit;
import fr.craftyourmind.quest.packet.DataQuestEventParadise;
import fr.craftyourmind.quest.packet.DataQuestEventParadiseEdit;
import fr.craftyourmind.quest.packet.DataQuestMecha;
import fr.craftyourmind.quest.packet.DataQuestModScreen;
import fr.craftyourmind.quest.packet.DataQuestNpcState;
import fr.craftyourmind.quest.packet.DataQuestObj;
import fr.craftyourmind.quest.packet.DataQuestObjAction;
import fr.craftyourmind.quest.packet.DataQuestRew;
import fr.craftyourmind.quest.packet.DataQuestScreen;
import fr.craftyourmind.skill.CYMClass;
import fr.craftyourmind.skill.SkillInventory;
import fr.craftyourmind.manager.CYMManager;
import fr.craftyourmind.manager.CYMNPC;
import fr.craftyourmind.manager.CYMReputation;
import fr.craftyourmind.manager.event.CYMLoginEvent;

public class QuestEventManager implements Listener, INPCEvent {
	
	public QuestEventManager(){
		Bukkit.getPluginManager().registerEvents(this, Plugin.it);
		CYMManager.addNpcEvent(this);
	}

	// ***************************** MECHANISM *****************************
	@EventHandler
	public void onQuestMechaEvent(QuestMechaEvent event) {
		DataQuestMecha data = event.data;
		if(data.getPlayer().hasPermission("cymquest.mechanism")){
			if(data.action == DataQuestMecha.OPEN){
				IMechaContainer mc = AbsMechaContainer.get(data.typeDriver, data.idDriver);
				if(mc != null){
					for(Mechanism m : mc.getMechas()){
						data.idMechas.add(m.id);
						data.nameMechas.add(m.name);
						data.catMechas.add(m.category);
						for(ChildLink cl : m.childLinks){
							data.idparents.add(m.id);
							data.idchilds.add(cl.get().id);
							data.namelinks.add(m.name+" -> "+cl.get().name);
							data.slots.add(cl.getSlot());
							data.orders.add(cl.questSort.getOrder());
						}
					}
				}
			}else if(data.action == DataQuestMecha.OPENMECHA){
				IMechaContainer mc = AbsMechaContainer.get(data.typeDriver, data.idDriver);
				if(mc != null){
					MechaCat cat = MechaCat.get(data.category);
					if(cat != null){
						List<MechaType> mtypes = cat.getTypeMechaPermits(mc.getTypeContainer());
						for(MechaType mt : mtypes){
							data.idTypes.add(mt.id);
							data.nameTypes.add(mt.name);
						}
						for(Mechanism m : mc.getMechas()){
							if(m.category == data.category){
								data.idMechas.add(m.id);
								data.nameMechas.add(m.name);
							}
						}
					}
				}
			}else if(data.action == DataQuestMecha.SELECT){
				IMechaContainer mc = AbsMechaContainer.get(data.typeDriver, data.idDriver);
				Mechanism m = null;
				if(mc != null) m = mc.getMecha(data.idM);
				if(m == null) data.setCancelled(true);
				else{
					data.typeMecha = m.getType();
					data.category = m.category;
					data.common = m.common;
					data.permanent = m.permanent;
					data.single = m.single;
					data.nameMecha = m.name;
					data.message = m.message.toString();
					data.params = m.getParams();
					data.paramsGUI = m.getParamsGUI();
				}
			}else if(data.action == DataQuestMecha.CREATE || data.action == DataQuestMecha.SAVE){
				Mechanism m = null;
				if(data.action == DataQuestMecha.CREATE)
					m = Mechanism.newMechanism(data.typeMecha, data.typeDriver, data.idDriver, data.category);
				else if(data.action == DataQuestMecha.SAVE){
					IMechaContainer mc = AbsMechaContainer.get(data.typeDriver, data.idDriver);
					if(mc != null) m = mc.getMecha(data.idM);
				}
				if(m != null){
					m.common = data.common;
					m.permanent = data.permanent;
					m.single = data.single;
					m.name = data.nameMecha;
					m.message.load(data.message);
					m.loadParams(data.params);
					if(data.action == DataQuestMecha.CREATE) m.sqlCreate();
					m.init();
					m.sqlSave();
				}
			}else if(data.action == DataQuestMecha.CLONE){
				IMechaContainer mc = AbsMechaContainer.get(data.typeDriver, data.idDriver);
				if(mc != null){
					Mechanism m = mc.getMecha(data.idM);
					if(m != null) m.cloneRename(mc, data.links);
				}
			}else if(data.action == DataQuestMecha.DELETE){
				IMechaContainer mc = AbsMechaContainer.get(data.typeDriver, data.idDriver);
				if(mc != null){
					Mechanism m = mc.getMecha(data.idM);
					if(m != null) m.sqlDelete();
				}
			}else if(data.action == DataQuestMecha.LINK){
				IMechaContainer mc = AbsMechaContainer.get(data.typeDriver, data.idDriver);
				if(mc != null){
					Mechanism mecha = mc.getMecha(data.idmecha);
					if(mecha != null){
						Mechanism child = mc.getMecha(data.idchild);
						if(child != null) mecha.link(child, data.slot);
					}
				}
			}else if(data.action == DataQuestMecha.DELLINK){
				IMechaContainer mc = AbsMechaContainer.get(data.typeDriver, data.idDriver);
				if(mc != null){
					Mechanism mecha = mc.getMecha(data.idmecha);
					if(mecha != null){
						Mechanism child = mc.getMecha(data.idchild);
						if(child != null) mecha.delLink(child);
					}
				}
			}else if(data.action == DataQuestMecha.UPLINK){
				IMechaContainer mc = AbsMechaContainer.get(data.typeDriver, data.idDriver);
				if(mc != null){
					Mechanism mecha = mc.getMecha(data.idmecha);
					if(mecha != null){
						Mechanism child = mc.getMecha(data.idchild);
						if(child != null) mecha.upLink(mecha.getLink(child));
					}
				}
			}else if(data.action == DataQuestMecha.DOWNLINK){
				IMechaContainer mc = AbsMechaContainer.get(data.typeDriver, data.idDriver);
				if(mc != null){
					Mechanism mecha = mc.getMecha(data.idmecha);
					if(mecha != null){
						Mechanism child = mc.getMecha(data.idchild);
						if(child != null) mecha.downLink(mecha.getLink(child));
					}
				}
			}else if(data.action == DataQuestMecha.MOVELINK){
				IMechaContainer mc = AbsMechaContainer.get(data.typeDriver, data.idDriver);
				if(mc != null){
					Mechanism mecha = mc.getMecha(data.idmecha);
					if(mecha != null){
						Mechanism child = mc.getMecha(data.idchild);
						if(child != null) mecha.moveLink(mecha.getLink(child), data.slot);
					}
				}
			}
		}
	}
	
	// ***************************** QUEST EVENT PARADISE *****************************
	@EventHandler
	public void onQuestEventParadiseEvent(QuestEventParadiseEvent event) {
		DataQuestEventParadise data = event.data;
		EventParadise ep = EventParadise.get(data.idE);
		if(ep != null){
			if(data.action == DataQuestEventParadise.START){
				if(data.getPlayer().hasPermission("cymquest.event")){
					 data.getPlayer().sendMessage(ChatColor.DARK_RED+"Start event "+ep.name);
					 ep.stopPlayers();
					 ep.start();
				}
			}else if(data.action == DataQuestEventParadise.STOP){
				if(data.getPlayer().hasPermission("cymquest.event")){
					ep.stopPlayers();
					ep.stop();
				}
			}else{
				Quest q = Quest.get(data.npc, data.idQ);
				if(q!= null){
					QuestPlayer qp = QuestPlayer.get(data.getPlayer());
					if(data.action == DataQuestEventParadise.OPEN){
						ep.open(q, qp, q.states.get(qp));
						
					}else if(data.action == DataQuestEventParadise.ACCEPT){
						ep.accept(q, qp, q.getState(qp)); // retourne forcement une instance
						
					}else if(data.action == DataQuestEventParadise.DECLINE){
						ep.decline(q, qp, q.states.get(qp));
						
					}else if(data.action == DataQuestEventParadise.GIVEREWARD){
						ep.giveReward(q, qp, q.states.get(qp));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onQuestEventParadiseEditEvent(QuestEventParadiseEditEvent event) {
		DataQuestEventParadiseEdit data = event.data;
		if(data.getPlayer().hasPermission("cymquest.event")){
			if(data.action == DataQuestEventParadiseEdit.OPEN){
				List<Integer> ids = new ArrayList<Integer>();
				List<String> names = new ArrayList<String>();
				List<Boolean> started = new ArrayList<Boolean>();
				for(EventParadise ep : EventParadise.gett(data.slot)){
					ids.add(ep.id);
					names.add(ep.name.getStr());
					started.add(ep.isStarted());
				}
				data.ids = ids;
				data.names = names;
				data.started = started;
				data.idsType = EventParadise.getListTypeId();
				data.namesType = EventParadise.getListType();
				
			}else if(data.action == DataQuestEventParadiseEdit.SELECT){
				data.ep = EventParadise.get(data.idE);
				if(data.ep == null) data.setCancelled(true);
			}else if(data.action == DataQuestEventParadiseEdit.DELETE){
				EventParadise ep = EventParadise.get(data.idE);
				if(ep != null) ep.delete();
			}else if(data.action == DataQuestEventParadiseEdit.SAVE){
				data.ep.save();
			}else if(data.action == DataQuestEventParadiseEdit.CREATE){
				data.ep.create();
				data.ep.init();
			}
		}
	}
	
	// ***************************** QUEST SCREEN *****************************
	@EventHandler
	public void onQuestScreenEvent(QuestScreenEvent event) {
		DataQuestScreen data = event.data;
		QuestPlayer qP = QuestPlayer.get(data.getPlayer());
		if(qP != null){
			List<Integer> listid = new ArrayList<Integer>();
			List<Integer> listnpc = new ArrayList<Integer>();
			List<String> listTitle = new ArrayList<String>();
			List<Quest> lq = null;
			if(data.action == DataQuestScreen.NPC)
				lq = Quest.listNPC.get(data.npc);
			else if(data.action == DataQuestScreen.EVENT){
				Quest q = Quest.get(data.npc, data.idQ);
				if(q != null){
					listid.add(q.id);
					listnpc.add(q.npc);
					listTitle.add(q.title.getStr());
				}
			}
			if(lq != null){
				for(Quest q : lq){
					if(q.hasAcces(qP)){
						listid.add(q.id);
						listnpc.add(q.npc);
						listTitle.add(q.title.getStr());
					}
				}
			}
			if(listTitle.size() != 1)
				data.textNpc = CYMNPC.getText(data.npc);
			if(listTitle.size() == 0 && data.textNpc.isEmpty())
				data.setCancelled(true);
			data.listid = listid;
			data.listnpc = listnpc;
			data.listTitle = listTitle;
		}		
	}
	
	// ***************************** QUEST *****************************
	@EventHandler
	public void onQuestEvent(QuestEvent event) {
		DataQuest data = event.data;
		QuestPlayer qp = null;
		if(data.edit && data.getPlayer().hasPermission("cymquest.questedit")){
			qp = QuestPlayer.get(data.idPlayer);
		}else{
			qp = QuestPlayer.get(data.getPlayer());
			data.edit = false;
		}
		if(qp != null){
			Quest q = null;
			if (data.action == DataQuest.QUEST){
				q = Quest.get(data.npc, data.idQ);
				if(q == null || !q.hasAcces(qp)) return ;
				
			}else{
				q = Quest.get(data.npc, data.idQ);
				if(q == null) return ;
			}

			if(data.action == DataQuest.DECLINE) q.decline(qp);
			else if(data.action == DataQuest.ACCEPT) q.accept(qp);

			data.idE = (q.getEvent() == null)? 0 : q.getEvent().id;
			data.title = q.title.getStr();
			data.introTxt = q.introTxt;
			data.fullTxt = q.fullTxt;
			data.successTxt = q.successTxt;
			data.loseTxt = q.loseTxt;
			StateQuestPlayer sqp = q.states.get(qp);
			if(sqp != null){
				if(sqp.isBeginning()){
					boolean successQ = true;
					for(IStateObj o : sqp.objs){
						boolean successO = true;
						if(!o.isTerminate())
							if(!o.checker())
								successO = false;
						data.objFinishs.add(successO);
						if(!successO) successQ = false;
					}
					data.success = successQ;
					if(data.action == DataQuest.GIVEREWARD && successQ){
						q.messageSuccess(qp);
						sqp.giveReward();
						data.success = false;
					}
				}
				data.beginning = sqp.isBeginning();
				data.terminate = sqp.isTerminate();
			}
			
			StringBuilder objIds = new StringBuilder();
			StringBuilder objTypes = new StringBuilder();
			StringBuilder objTxt = new StringBuilder();
			StringBuilder objItems = new StringBuilder();
			StringBuilder objItemsData = new StringBuilder();
			String messageGui = "";
			for(AbsObjective obj : q.getObjs()){
				if(sqp != null) messageGui = sqp.objs.get(obj.index).getMessageGui();
				else messageGui = "";
				objIds.append(obj.id).append(QuestTools.DELIMITER);
				objTypes.append(obj.getType()).append(QuestTools.DELIMITER);
				objTxt.append((obj.descriptive.isEmpty())?" ":messageGui+obj.descriptive).append(QuestTools.DELIMITER);
				objItems.append(obj.getIdItem()).append(QuestTools.DELIMITER);
				objItemsData.append(obj.getDataItem()).append(QuestTools.DELIMITER);
				data.objParams.add(obj.getParams());
			}
			
			StringBuilder rewIds = new StringBuilder();
			StringBuilder rewTypes = new StringBuilder();
			StringBuilder rewTxt = new StringBuilder();
			StringBuilder rewItems = new StringBuilder();
			StringBuilder rewItemsData = new StringBuilder();
			for(AbsReward rew : q.getRewards()){
				rewIds.append(rew.id).append(QuestTools.DELIMITER);
				rewTypes.append(rew.getType()).append(QuestTools.DELIMITER);
				rewTxt.append((rew.descriptive.isEmpty())?" ":rew.descriptive).append(QuestTools.DELIMITER);
				rewItems.append(rew.getIdItem()).append(QuestTools.DELIMITER);
				rewItemsData.append(rew.getDataItem()).append(QuestTools.DELIMITER);
			}
			
			data.objIds = objIds.toString();
			data.rewIds = rewIds.toString();
			data.objTypes = objTypes.toString();
			data.rewTypes = rewTypes.toString();
			data.objTxt = objTxt.toString();
			data.rewTxt = rewTxt.toString();
			data.objItems = objItems.toString();
			data.objItemsData = objItemsData.toString();
			data.rewItems = rewItems.toString();
			data.rewItemsData = rewItemsData.toString();
		}
	}
	
	// ***************************** QUEST OBJECTIVE ACTION *****************************
	@EventHandler
	public void onQuestObjActionEvent(QuestObjActionEvent event) {
		DataQuestObjAction data = event.data;
		QuestPlayer qp = QuestPlayer.get(data.getPlayer());
		if(qp != null){
			Quest q = Quest.get(data.npc, data.idQ);
			if(q == null) return;
				if(data.action == DataQuestObjAction.CHOICE){ // --- CHOICE ---
					StateQuestPlayer sqp = q.getState(qp);
					for(IStateRew rew : sqp.rews)
						if(!data.rewards.contains(rew.getId())) rew.setDisable(true);
					if(q.hasAcces(qp, sqp)) sqp.accept();
					sqp.giveReward();
					sqp.getQuest().messageSuccess(qp);
						
			}else if(data.action == DataQuestObjAction.ANSWER){ // --- ANSWER ---
				StateQuestPlayer sqp = q.states.get(qp);
				if(sqp == null) sqp = q.getNewState(qp);
				for(IStateObj so : sqp.objs){
					if(so.getId() == data.idO){
						StateAnswer sa = (StateAnswer) so;
						sa.playerAnswer = data.response;
						sa.checker();
					}
				}	
			}
		}
	}
	
	// ***************************** QUEST MODIFICATION SCREEN *****************************
	@EventHandler
	public void onQuestModScreenEvent(QuestModScreenEvent event) {
		DataQuestModScreen data = event.data;
		QuestPlayer qp = null;
		if(data.edit && data.getPlayer().hasPermission("cymquest.questedit")){
			qp = QuestPlayer.get(data.idPlayer);
			data.namePlayer = qp.getName();
		}else{
			qp = QuestPlayer.get(data.getPlayer());
			data.edit = false;
		}
		if(qp != null){
			List<Quest> lq = null;
			if(data.action == DataQuestModScreen.OPENQUESTMOD){
				lq = Quest.listNPC.get(data.npc);
			}else if(data.action == DataQuestModScreen.OPENQUESTEVENT){
				EventParadise ep = EventParadise.get(data.event);
				if(ep != null) lq = EventParadise.get(data.event).quests;
			}else if(data.action == DataQuestModScreen.OPENQUESTPLAYER){
				if(data.menuTag) data.listQuestTags = qp.getQuestCurrentTags();
				lq = qp.getQuestsCurrent(data.tag);
			}else if(data.action == DataQuestModScreen.OPENQUESTEND){
				if(data.menuTag) data.listQuestTags = qp.getQuestFinishedTags();
				lq = qp.getQuestsFinished(data.tag);
			}
			if(lq != null){
				for(Quest q : lq){
					data.listid.add(q.id);
					data.listnpc.add(q.npc);
					data.listTitle.add(q.title.getStr());
				}
			}
		}		
	}
	
	@EventHandler
	public void onQuestEditEvent(QuestEditEvent event) {
		DataQuestEdit data = event.data;
		if(data.getPlayer().hasPermission("cymquest.questedit")){
			QuestPlayer qP = QuestPlayer.get(data.getPlayer());
			if(qP != null){
				// SELECT QUEST
				if(data.action == DataQuestEdit.OPEN){
					Quest q = Quest.get(data.npc, data.idQ);
					if(q != null){
						List<Integer> idsRep = new ArrayList<Integer>();
						List<String> namesRep = new ArrayList<String>();
						for(CYMReputation rep : CYMReputation.get()){
							idsRep.add(rep.id);
							namesRep.add(rep.name);
						}
						data.idRepute = idsRep;
						data.nameRepute = namesRep;
						data.npc = q.npc;
						data.title = q.title.getStr();
						data.introTxt = q.introTxt;
						data.fullTxt = q.fullTxt;
						data.successTxt = q.successTxt;
						data.loseTxt = q.loseTxt;
						data.levelMin = q.levelMin;
						data.levelMax = q.levelMax;
						data.reputeMin = q.reputeMin;
						data.reputeMax = q.reputeMax;
						data.objInTheOrder = q.objInTheOrder;
						data.common = q.common;
						data.playersMax = q.playersMax;
						if(q.repute != null) data.repute = q.repute.id;
						data.repeatable = q.repeatable;
						data.repeateTimeAccept = q.repeateTimeAccept;
						data.repeateTimeGive = q.repeateTimeGive;
						data.repeateTime = q.repeateTime;
						if(q.parent != null) data.parent = q.parent.id;
						if(q.getEvent() != null) data.idEvent = q.getEvent().id;
						data.displayIconNPC = q.displayIconNPC;
						data.params = q.getParamsCon();
						data.icons = q.strIcons;
						data.allIcons = QuestIcon.getAllIcons();
						data.classMin = q.classMin;
						data.classMax = q.classMax;
						data.cymClass = q.cymClass;
						data.nameParamclass = q.nameParamClass;
						for(CYMClass mc : CYMClass.getCYMClass()){
							data.idClasses.add(mc.getId());
							data.nameClasses.add(mc.getName());
						}
						if(q.cymClass > 0){
							CYMClass mc = CYMClass.getCYMClass(q.cymClass);
							if(mc != null) data.allParams = mc.getNameMechaParams();
						}
					}
				// CREATE ET SAVE QUEST
				}else if(data.action == DataQuestEdit.SAVEQUEST || data.action == DataQuestEdit.CREATEQUEST){
					Quest q = null;
					if(data.action == DataQuestEdit.SAVEQUEST){
						q = Quest.get(data.npc, data.idQ);
						if(q == null) return;
						if(data.npc != data.newnpc){
							Quest.listNPC.get(data.npc).remove(q);
							if(Quest.listNPC.containsKey(data.newnpc)) Quest.listNPC.get(data.newnpc).add(q);
							else{
								List<Quest> list = new ArrayList<Quest>();
								list.add(q);
								Quest.listNPC.put(data.newnpc, list);
							}
						}
					}else if(data.action == DataQuestEdit.CREATEQUEST){
						q = new Quest();
					}
					q.npc = data.newnpc;
					q.title.setStrUnlock(data.title);
					q.introTxt = data.introTxt;
					q.fullTxt = data.fullTxt;
					q.successTxt = data.successTxt;
					q.loseTxt = data.loseTxt;
					q.levelMin = data.levelMin;
					q.levelMax = data.levelMax;
					q.reputeMin = data.reputeMin;
					q.reputeMax = data.reputeMax;
					q.objInTheOrder = data.objInTheOrder;
					q.common = data.common;
					q.playersMax = data.playersMax;
					q.repute = CYMReputation.getById(data.repute);
					q.setRepeatable(data.repeatable);
					q.repeateTime = data.repeateTime;
					q.repeateTimeAccept = data.repeateTimeAccept;
					q.repeateTimeGive = data.repeateTimeGive;
					q.parent = Quest.get(data.parent);
					q.setEvent(EventParadise.get(data.idEvent));
					q.displayIconNPC = data.displayIconNPC;
					q.loadParamsCon(data.params);
					q.setIcons(data.icons);
					q.classMin = data.classMin;
					q.classMax = data.classMax;
					q.cymClass = data.cymClass;
					q.nameParamClass = data.nameParamclass;
					if(data.action == DataQuestEdit.SAVEQUEST)
						q.save();
					else if(data.action == DataQuestEdit.CREATEQUEST){
						q.create();
						q.init();
						data.idQ = q.id;
					}
				// DELETE QUEST
				}else if(data.action == DataQuestEdit.DELETEQUEST){
					Quest q = Quest.get(data.npc, data.idQ);
					if(q != null){
						q.delete();
					}
				}
			}
		}
	}
	// ***************************** OBJECTIVE *****************************
	@EventHandler
	public void onQuestObjOpenEvent(QuestObjOpenEvent event) {
		DataQuestObj data = event.data;
		QuestPlayer qP = QuestPlayer.get(data.getPlayer());
		if(qP != null){			
			data.idTypes = AbsObjective.getListTypeId();
			data.nameTypes = AbsObjective.getListType();
			List<Integer> idObjs = new ArrayList<Integer>();
			List<String> nameObjs = new ArrayList<String>();
			Quest q = Quest.get(data.npc, data.idQ);
			if(q != null){
				for(AbsObjective obj : q.getObjs()){
					idObjs.add(obj.id);
					nameObjs.add(obj.getStrType());
				}
				data.idObjs = idObjs;
				data.nameObjs = nameObjs;
			}
		}		
	}
	
	@EventHandler
	public void onQuestObjEvent(QuestObjEvent event) {
		DataQuestObj data = event.data;
		if(data.getPlayer().hasPermission("cymquest.questedit")){
			QuestPlayer qP = QuestPlayer.get(data.getPlayer());
			if(qP != null){
				// SELECT OBJECTIVE
				if(data.action == DataQuestObj.OBJECTIVE){
					Quest q = Quest.get(data.npc, data.idQ);
					if(q != null){
						AbsObjective obj = q.getObjective(data.idO);
						if(obj != null){
							data.type = obj.getType();
							data.descriptive = obj.descriptive;
							data.success = obj.success;
							data.finishQuest = obj.isFinishQuest();
							data.param = obj.getParams();
							data.paramGUI = obj.getParamsGUI();
						}
					}
				// CREATE ET SAVE OBJECTIVE
				}else if(data.action == DataQuestObj.SAVEOBJ || data.action == DataQuestObj.CREATEOBJ){
					Quest q = Quest.get(data.npc, data.idQ);
					AbsObjective obj = null;
					if(q == null) return;
					if(data.action == DataQuestObj.SAVEOBJ){
						obj = q.getObjective(data.idO);
						if(obj == null) return;
					}else if(data.action == DataQuestObj.CREATEOBJ)
						obj = AbsObjective.newObjective(q, data.type);
					
					obj.descriptive = data.descriptive;
					obj.success = data.success;
					obj.setFinishQuest(data.finishQuest);
					obj.loadParams(data.param);
					
					if(data.action == DataQuestObj.SAVEOBJ)
						obj.sqlSave();
					else if(data.action == DataQuestObj.CREATEOBJ){
						obj.sqlCreate();
						q.setObjective(obj);
					}
					// DELETE OBJECTIVE
				}else if(data.action == DataQuestObj.DELETEOBJ){
					Quest q = Quest.get(data.npc, data.idQ);
					if(q != null){
						AbsObjective obj = q.getObjective(data.idO);
						if(obj != null){
							obj.sqlDelete();
							q.delObjective(obj);
						}
					}	
				}
			}
		}
	}
	// ***************************** REWARD *****************************
	@EventHandler
	public void onQuestRewOpenEvent(QuestRewOpenEvent event) {
		DataQuestRew data = event.data;
		QuestPlayer qP = QuestPlayer.get(data.getPlayer());
		if(qP != null){			
			data.idTypes = AbsReward.getListTypeId();
			data.nameTypes = AbsReward.getListType();
			List<Integer> idRews = new ArrayList<Integer>();
			List<String> nameRews = new ArrayList<String>();
			Quest q = Quest.get(data.npc, data.idQ);
			if(q != null){
				for(AbsReward rew : q.getRewards()){
					idRews.add(rew.id);
					nameRews.add(rew.getStrType());
				}
				data.idRews = idRews;
				data.nameRews = nameRews;
			}
		}		
	}
	
	@EventHandler
	public void onQuestRewEvent(QuestRewEvent event) {
		DataQuestRew data = event.data;
		if(data.getPlayer().hasPermission("cymquest.questedit")){
			QuestPlayer qP = QuestPlayer.get(data.getPlayer());
			if(qP != null){
				// SELECT REWARD
				if(data.action == DataQuestRew.REWARD){
					Quest q = Quest.get(data.npc, data.idQ);
					if(q != null){
						AbsReward rew = q.getReward(data.idR);
						if(rew != null){
							data.type = rew.getType();
							data.descriptive = rew.descriptive;
							data.amount = rew.amount;
							data.param = rew.getParams();
							data.paramGUI = rew.getParamsGUI();
						}
					}
				// CREATE ET SAVE REWARD
				}else if(data.action == DataQuestRew.SAVEREW || data.action == DataQuestRew.CREATEREW){
					Quest q = Quest.get(data.npc, data.idQ);
					AbsReward rew = null;
					if(q == null) return;
					if(data.action == DataQuestRew.SAVEREW){
						rew = q.getReward(data.idR);
						if(rew == null) return;
					}else if(data.action == DataQuestRew.CREATEREW)
						rew = AbsReward.newReward(q, data.type);
					
					rew.descriptive = data.descriptive;
					rew.amount = data.amount;
					rew.loadParams(data.param);
					
					if(data.action == DataQuestRew.SAVEREW)
						rew.sqlSave();
					else if(data.action == DataQuestRew.CREATEREW){
						rew.sqlCreate();
						q.setReward(rew);
					}
				// DELETE REWARD
				}else if(data.action == DataQuestRew.DELETEREW){
					Quest q = Quest.get(data.npc, data.idQ);
					if(q != null){
						AbsReward rew = q.getReward(data.idR);
						if(rew != null){
							rew.sqlDelete();
							q.delReward(rew);
						}
					}	
				}
			}
		}
	}
	
	// ***************************** NPC *****************************
	@Override
	public void onNPCRightClick(Player p, int npc) {
		QuestPlayer qp = QuestPlayer.get(p);
		if(qp.useModQuest()){
			qp.npcselect = npc;
			ObjectiveTalk.check(p, npc);
			if(p.isSneaking() && qp.getPlayer().hasPermission("cymquest.questedit"))
				new DataQuestModScreen(DataQuestModScreen.OPENQUESTMOD, npc).callEvent(p);
			else if(qp.getPlayer().hasPermission("cymquest.quest")){
				new DataQuestScreen(DataQuestScreen.NPC, npc).callEvent(p);
			}
		}
	}
	
	@Override
	public void onNPCDeathEvent(int npc, LivingEntity entity) {
		
	}
	
	@EventHandler
	public void onQuestNpcStateEvent(QuestNpcStateEvent event){
		DataQuestNpcState data = event.data;
		if(data.isDrawIcon()){
			data.nameIcon = QuestIcon.none;
			List<Quest> lq = Quest.listNPC.get(data.npc);
			if(lq != null){
				QuestPlayer qp = QuestPlayer.get(data.getPlayer());
				if(qp != null){
					for(Quest q : lq){
						if(q.displayIconNPC){
							StateQuestPlayer sqp = q.states.get(qp);
							if(q.hasAcces(qp, sqp)){
								data.questDispo = true;
								data.nameIcon = q.normalIcon;
								if(q.repeatable) data.nameIcon = q.repeatableIcon;
								if(q.repute != null) data.nameIcon = q.reputeIcon;
								if(q.cymClass > 0) data.nameIcon = q.skillIcon;
								if(sqp != null){
									if(sqp.isBeginning()) data.nameIcon = q.takenIcon;
									if(sqp.checkObjSuccessNoMsg()){
										data.nameIcon = q.successIcon;
										break;
									}
								}
							}
						}
					}
				}
			}
		}else if(data.isUpdateIcon())
			data.icon = QuestIcon.getIcon(data.nameIcon);
	}
	
	// ***************************** PLAYER *****************************
	@EventHandler
	public void onPlayerLoginEvent(PlayerLoginEvent event) {
		QuestPlayer qp = QuestPlayer.get(event.getPlayer()); // init
		qp.setPlayer(event.getPlayer());
		SkillInventory.load(qp);
	}

	@EventHandler
	public void onCYMLoginEvent(CYMLoginEvent event) {
		Player p = event.data.getPlayer();
		QuestPlayer qp = QuestPlayer.get(p);
		qp.setPlayer(p);
		EventMinus.onPlayerChangedWorldEvent(qp, null, p.getWorld());
		QuestKeyboard.sendCONFIG(p);
		qp.onCYMLoginEvent();
		SkillInventory.sendLogin(qp);
	}
	
	@EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		Player p = event.getPlayer();
		EventParadise.onPlayerQuit(p);
		CmdChunkPosition.onPlayerQuit(p);
		SkillInventory.onPlayerQuitEvent(event);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Block b = event.getBlock();
		CYMNPC npc = CYMNPC.get(b);
		if(npc != null && npc.clickon(b.getLocation())){
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.GRAY+"It's a quest block !");
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null){
			Block b = event.getClickedBlock();
			CYMNPC npc = CYMNPC.get(b);
			if(npc != null && npc.clickon(b.getLocation())){
				ObjectiveTalk.check(event.getPlayer(), npc.id);
				if(event.getPlayer().isSneaking() && event.getPlayer().hasPermission("cymquest.questedit"))
					new DataQuestModScreen(DataQuestModScreen.OPENQUESTMOD, npc.id).callEvent(event.getPlayer());
				else if(event.getPlayer().hasPermission("cymquest.quest"))
					new DataQuestScreen(DataQuestScreen.NPC, npc.id).callEvent(event.getPlayer());	
			}
			
		}
		if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
			ItemStack is = event.getItem();
			if(is != null){
				ItemMeta im = is.getItemMeta();
				if(im instanceof BookMeta){
					BookMeta bm = (BookMeta)im;
					if(bm.hasAuthor() && bm.getAuthor().equals("quest book")){
						try{
							if(bm.getPages().size() == 2){
								if(!bm.getPage(2).equalsIgnoreCase(event.getPlayer().getName())){
									event.getPlayer().sendMessage(ChatColor.GRAY+"This isn't your book.");
									Plugin.log("This isn't your book "+event.getPlayer().getName());
									return;
								}
							}
							int idnpc = Integer.valueOf(bm.getPage(1));
							CYMNPC npc = CYMNPC.get(idnpc);
							if(npc == null) npc = CYMNPC.getBlock(idnpc);
							if(npc != null){
								ObjectiveTalk.check(event.getPlayer(), npc.id);
								if(event.getPlayer().isSneaking() && event.getPlayer().hasPermission("cymquest.questedit"))
									new DataQuestModScreen(DataQuestModScreen.OPENQUESTMOD, npc.id).callEvent(event.getPlayer());
								else if(event.getPlayer().hasPermission("cymquest.quest"))
									new DataQuestScreen(DataQuestScreen.NPC, npc.id).callEvent(event.getPlayer());
							}else event.getPlayer().sendMessage(ChatColor.GRAY+"Error open npc item quest "+idnpc+".");
						}catch(Exception e){
							event.getPlayer().sendMessage(ChatColor.GRAY+"Error open npc item quest.");
							Plugin.log("error open npc item quest "+event.getPlayer().getName());
						}
					}
					
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
		QuestPlayer qp = QuestPlayer.get(event.getPlayer());
		if(qp.useModQuest())
			EventMinus.onPlayerChangedWorldEvent(qp, event.getFrom(), event.getPlayer().getWorld());
	}
	
	@EventHandler
	public void onEntityExplodeEvent(EntityExplodeEvent event) {
		ActionChest.onEntityExplodeEvent(event);
		ActionInventory.onEntityExplodeEvent(event);
	}
	
	@EventHandler
	public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		SkillInventory.onPlayerRespawnEvent(event);
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		MechaDriverEntity.onEntityDeath(event);
		SkillInventory.onEntityDeath(event);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
		 if (event.isCancelled() && ActionPopEntity.bypassSpawnProtect)
             event.setCancelled(false);
	}	
	
	@EventHandler
	public void onInventoryCloseEvent(InventoryCloseEvent event) {
		SkillInventory.onInventoryCloseEvent(event);
	}
	
	@EventHandler
	public void onInventoryClickEvent(InventoryClickEvent event) {
		SkillInventory.onInventoryClickEvent(event);
	}
	
	@EventHandler
	public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
		SkillInventory.onPlayerDropItemEvent(event);
	}
	
	/*@EventHandler
	public void onPluginEnableEvent(PluginEnableEvent event) {
		if(event.getPlugin() == Plugin.it) MechaDriverEntity.onPluginEnableEvent();
	}
	
	@EventHandler
	public void onPluginDisableEvent(PluginDisableEvent event) {
		if(event.getPlugin() == Plugin.it){
			QuestPlayer.onPluginDisableEvent();
			MechaDriverEntity.onPluginDisableEvent();
		}
	}*/
	
}
