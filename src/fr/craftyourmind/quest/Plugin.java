package fr.craftyourmind.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import fr.craftyourmind.skill.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import fr.craftyourmind.manager.CYMNPC;
import fr.craftyourmind.quest.command.CmdMekaBox;
import fr.craftyourmind.quest.command.CmdQuestManager;
import fr.craftyourmind.quest.command.CmdSkillManager;
import fr.craftyourmind.quest.mecha.AbsMechaContainer;
import fr.craftyourmind.quest.mecha.ActionBuildBlock;
import fr.craftyourmind.quest.mecha.ActionChest;
import fr.craftyourmind.quest.mecha.ActionCommand;
import fr.craftyourmind.quest.mecha.ActionEffect;
import fr.craftyourmind.quest.mecha.ActionInventory;
import fr.craftyourmind.quest.mecha.ActionPlayer;
import fr.craftyourmind.quest.mecha.ActionPopEntity;
import fr.craftyourmind.quest.mecha.ActionQuest;
import fr.craftyourmind.quest.mecha.ActionQuestBook;
import fr.craftyourmind.quest.mecha.ActionReputation;
import fr.craftyourmind.quest.mecha.ActionSendClan;
import fr.craftyourmind.quest.mecha.ActionSkill;
import fr.craftyourmind.quest.mecha.ActionTeleport;
import fr.craftyourmind.quest.mecha.MechaCat;
import fr.craftyourmind.quest.mecha.MechaDriverEntity;
import fr.craftyourmind.quest.mecha.MechaType;
import fr.craftyourmind.quest.mecha.Mechanism;
import fr.craftyourmind.quest.mecha.StarterBox;
import fr.craftyourmind.quest.mecha.StarterEvent;
import fr.craftyourmind.quest.mecha.StarterLevel;
import fr.craftyourmind.quest.mecha.StarterQuest;
import fr.craftyourmind.quest.mecha.StarterSkill;
import fr.craftyourmind.quest.mecha.StarterTier;
import fr.craftyourmind.quest.mecha.ToolBox;
import fr.craftyourmind.quest.mecha.ToolEffects;
import fr.craftyourmind.quest.mecha.ToolMechaStop;
import fr.craftyourmind.quest.mecha.ToolParam;
import fr.craftyourmind.quest.mecha.ToolRandomMecha;
import fr.craftyourmind.quest.mecha.ToolSelector;
import fr.craftyourmind.quest.mecha.ToolTextScreen;
import fr.craftyourmind.quest.mecha.TriggerDamage;
import fr.craftyourmind.quest.mecha.TriggerDay;
import fr.craftyourmind.quest.mecha.TriggerEventBlock;
import fr.craftyourmind.quest.mecha.TriggerInventory;
import fr.craftyourmind.quest.mecha.TriggerKill;
import fr.craftyourmind.quest.mecha.TriggerLocation;
import fr.craftyourmind.quest.mecha.TriggerTimer;
import fr.craftyourmind.quest.mecha.TriggerUseItem;
import fr.craftyourmind.quest.packet.DataQuest;
import fr.craftyourmind.quest.packet.DataQuestEdit;
import fr.craftyourmind.quest.packet.DataQuestEventParadise;
import fr.craftyourmind.quest.packet.DataQuestEventParadiseEdit;
import fr.craftyourmind.quest.packet.DataQuestKeyboard;
import fr.craftyourmind.quest.packet.DataQuestMecha;
import fr.craftyourmind.quest.packet.DataQuestModScreen;
import fr.craftyourmind.quest.packet.DataQuestNpcState;
import fr.craftyourmind.quest.packet.DataQuestObj;
import fr.craftyourmind.quest.packet.DataQuestObjAction;
import fr.craftyourmind.quest.packet.DataQuestRepute;
import fr.craftyourmind.quest.packet.DataQuestRew;
import fr.craftyourmind.quest.packet.DataQuestScreen;
import fr.craftyourmind.quest.sql.QuestSQLManager;
import fr.craftyourmind.manager.util.CYMHandlerUtil;

public class Plugin extends JavaPlugin{

	static public Plugin it;
	static private Logger log;
	
	@Override
	public void onDisable() {
		EventParadise.onDisable();
		QuestPlayer.onPluginDisableEvent();
		MechaDriverEntity.onPluginDisableEvent();
	}
	
	@Override
	public void onEnable() {
		it = this;
		log = getLogger();
		log("Loading ...");
		
		MechaDriverEntity.onPluginEnableEvent();
		
		MechaCat starter = new MechaCat(MechaCat.STARTER, "quest.mecha.starterB");
		MechaCat.add(starter);
		MechaCat trigger = new MechaCat(MechaCat.TRIGGER, "quest.mecha.triggerB");
		MechaCat.add(trigger);
		MechaCat action = new MechaCat(MechaCat.ACTION, "quest.mecha.actionB");
		MechaCat.add(action);
		MechaCat tools = new MechaCat(MechaCat.TOOLS, "Tools");
		MechaCat.add(tools);
		
		Mechanism.add(starter, MechaType.STAQUEST, MechaType.STRSTAQUEST, StarterQuest.class, AbsMechaContainer.QUEST, AbsMechaContainer.EVENT);
		Mechanism.add(starter, MechaType.STAEVENT, MechaType.STRSTAEVENT, StarterEvent.class, AbsMechaContainer.EVENT);
		Mechanism.add(starter, MechaType.STAMEKABOX, MechaType.STRSTAMEKABOX, StarterBox.class, AbsMechaContainer.MEKABOX, AbsMechaContainer.SKILL);
		Mechanism.add(starter, MechaType.STASKILL, MechaType.STRSTASKILL, StarterSkill.class, AbsMechaContainer.SKILL, AbsMechaContainer.CLASS);
		Mechanism.add(starter, MechaType.STALEVEL, MechaType.STRSTALEVEL, StarterLevel.class, AbsMechaContainer.SKILL, AbsMechaContainer.CLASS);
		Mechanism.add(starter, MechaType.STATIER, MechaType.STRSTATIER, StarterTier.class, AbsMechaContainer.SKILL, AbsMechaContainer.CLASS);
		
		Mechanism.add(trigger, MechaType.TRIINVENTORY, MechaType.STRTRIINVENTORY, TriggerInventory.class);
		Mechanism.add(trigger, MechaType.TRILOCATION, MechaType.STRTRILOCATION, TriggerLocation.class);
		Mechanism.add(trigger, MechaType.TRIUSEITEM, MechaType.STRTRIUSEITEM, TriggerUseItem.class);
		Mechanism.add(trigger, MechaType.TRITIMER, MechaType.STRTRITIMER, TriggerTimer.class);
		Mechanism.add(trigger, MechaType.TRIEVENTBLOCK, MechaType.STRTRIEVENTBLOCK, TriggerEventBlock.class);
		Mechanism.add(trigger, MechaType.TRIKILL, MechaType.STRTRIKILL, TriggerKill.class);
		Mechanism.add(trigger, MechaType.TRIDAMAGE, MechaType.STRTRIDAMAGE, TriggerDamage.class);
		Mechanism.add(trigger, MechaType.TRIDAY, MechaType.STRTRIDAY, TriggerDay.class);
		
		//Mechanism.add(action, MechaType.ACTINVENTORYOLD, MechaType.STRACTINVENTORY, false, ActionInventoryOld.class);
		Mechanism.add(action, MechaType.ACTINVENTORY, MechaType.STRACTINVENTORY, ActionInventory.class);
		Mechanism.add(action, MechaType.ACTPLAYER, MechaType.STRACTPLAYER, ActionPlayer.class);
		Mechanism.add(action, MechaType.ACTPOPENTITY, MechaType.STRACTPOPENTITY, ActionPopEntity.class);
		Mechanism.add(action, MechaType.ACTEFFECT, MechaType.STRACTEFFECT, ActionEffect.class);
		Mechanism.add(action, MechaType.ACTSKILL, MechaType.STRACTSKILL, ActionSkill.class);
		Mechanism.add(action, MechaType.ACTQUEST, MechaType.STRACTQUEST, ActionQuest.class);
		Mechanism.add(action, MechaType.ACTREPUTATION, MechaType.STRACTREPUTE, ActionReputation.class);
		Mechanism.add(action, MechaType.ACTQUESTBOOK, MechaType.STRACTQUESTBOOK, ActionQuestBook.class);
		Mechanism.add(action, MechaType.ACTTELEPORT, MechaType.STRACTTELEPORT, false, ActionTeleport.class); // old
		
		Mechanism.add(tools, MechaType.ACTCOMMAND, MechaType.STRACTCOMMAND, ActionCommand.class);
		Mechanism.add(tools, MechaType.TOOEFFECTS, MechaType.STRTOOEFFECTS, ToolEffects.class);
		Mechanism.add(tools, MechaType.ACTBUILDBLOCK, MechaType.STRACTBUILDBLOCK, ActionBuildBlock.class);
		Mechanism.add(tools, MechaType.TOOSELECT, MechaType.STRTOOSELECT, ToolSelector.class);
		Mechanism.add(tools, MechaType.TOOMEKABOX, MechaType.STRTOOMEKABOX, ToolBox.class);
		Mechanism.add(tools, MechaType.TOOPARAM, MechaType.STRTOOPARAM, ToolParam.class);
		Mechanism.add(tools, MechaType.TOOTEXT, MechaType.STRTOOTEXT, ToolTextScreen.class);
		Mechanism.add(tools, MechaType.TOOSTOP, MechaType.STRTOOSTOP, ToolMechaStop.class);
		Mechanism.add(tools, MechaType.TOORANDMECHA, MechaType.STRTOORANDMECHA, ToolRandomMecha.class);
		Mechanism.add(tools, MechaType.ACTSENDCLAN, MechaType.STRACTSENDCLAN, ActionSendClan.class);
		Mechanism.add(tools, MechaType.ACTCHEST, MechaType.STRACTCHEST, false, ActionChest.class); // old
		
        CYMHandlerUtil.addData(DataQuest.class, fr.craftyourmind.quest.client.packet.DataQuest.class);
        CYMHandlerUtil.addData(DataQuestEdit.class, fr.craftyourmind.quest.client.packet.DataQuestEdit.class);
        CYMHandlerUtil.addData(DataQuestModScreen.class, fr.craftyourmind.quest.client.packet.DataQuestModScreen.class);
        CYMHandlerUtil.addData(DataQuestObj.class, fr.craftyourmind.quest.client.packet.DataQuestObj.class);
        CYMHandlerUtil.addData(DataQuestObjAction.class, fr.craftyourmind.quest.client.packet.DataQuestObjAction.class);
        CYMHandlerUtil.addData(DataQuestRew.class, fr.craftyourmind.quest.client.packet.DataQuestRew.class);
        CYMHandlerUtil.addData(DataQuestScreen.class, fr.craftyourmind.quest.client.packet.DataQuestScreen.class);
        CYMHandlerUtil.addData(DataQuestNpcState.class, fr.craftyourmind.quest.client.packet.DataQuestNpcState.class);
        CYMHandlerUtil.addData(DataQuestEventParadise.class, fr.craftyourmind.quest.client.packet.DataQuestEventParadise.class);
        CYMHandlerUtil.addData(DataQuestEventParadiseEdit.class, fr.craftyourmind.quest.client.packet.DataQuestEventParadiseEdit.class);
        CYMHandlerUtil.addData(DataQuestRepute.class, fr.craftyourmind.quest.client.packet.DataQuestRepute.class);
        CYMHandlerUtil.addData(DataQuestMecha.class, fr.craftyourmind.quest.client.packet.DataQuestMecha.class);
        CYMHandlerUtil.addData(DataQuestKeyboard.class, fr.craftyourmind.quest.client.packet.DataQuestKeyboard.class);
        
        QuestKeyboard.loadDefault();
        CmdQuestManager.init();
        QuestIcon.loadDefault();
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run(){ new QuestEventManager(); QuestSQLManager.init(); QuestSQLManager.load(); }}, 1);	  
        
        logDescription();
	    
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,	String label, String[] args) {
		
    	if(label.equalsIgnoreCase("quest") || label.equalsIgnoreCase("cymquest")){
    		QuestPlayer qp = null;
    		if(sender instanceof Player){
        		Player player = (Player)sender;
        		qp = QuestPlayer.get(player);
        		if(!qp.useModQuest()) return false;
        	}
    		
    		try{ // --- load ---
				/*if(args.length == 1 && args[0].equalsIgnoreCase("load") && sender.hasPermission("cymquest.load")){
					QuestSQLManager.init();
					QuestSQLManager.load();
					sender.sendMessage(ChatColor.GRAY+"Loading quests ...");
					return true;*/
				// --- npc ---
				if(args.length == 1 && args[0].equalsIgnoreCase("npc") && sender.hasPermission("cymquest.questedit") && sender instanceof Player){
					Player player = (Player)sender;
					int npc = qp.npcselect;
					new DataQuestModScreen(DataQuestModScreen.OPENQUESTMOD, npc).callEvent(player); // open GUI
					player.sendMessage(ChatColor.GRAY+"Quests open ...");
					return true;
					
				// --- event ---
				}else if(args.length == 1 && args[0].equalsIgnoreCase("event")  && sender.hasPermission("cymquest.event") && sender instanceof Player){
					Player player = (Player)sender;
					new DataQuestEventParadiseEdit().callEventOPEN(player);
					sender.sendMessage(ChatColor.GRAY+"Events open ...");
					return true;
				}else if(args.length == 2 && args[0].equalsIgnoreCase("event")  && sender.hasPermission("cymquest.event") && sender instanceof Player){
					Player player = (Player)sender;
					new DataQuestEventParadiseEdit().callEventOPEN(player, Integer.valueOf(args[1]));
					sender.sendMessage(ChatColor.GRAY+"Events open ...");
					return true;				
				// --- start event ---
				}else if(args.length >= 2 && args[0].equalsIgnoreCase("start")  && sender.hasPermission("cymquest.event")){
					String name = args[1];
		    		for(int i = 2 ; i < args.length ; i++) name += " "+args[i];
					EventParadise ep = EventParadise.get(name);
					if(ep != null){
						ep.stopPlayers();
						ep.start();
						sender.sendMessage(ChatColor.GRAY+"Start events open "+args[1]+" ...");
					}
					return true;
				// --- stop event ---
				}else if(args.length >= 2 && args[0].equalsIgnoreCase("stop")  && sender.hasPermission("cymquest.event")){
					String name = args[1];
		    		for(int i = 2 ; i < args.length ; i++) name += " "+args[i];
					EventParadise ep = EventParadise.get(name);
					if(ep != null){
						ep.stopPlayers();
						ep.stop();
						sender.sendMessage(ChatColor.GRAY+"Stop events "+args[1]+" ...");
					}
					return true;
					
				// --- add ---
				}else if(args.length == 3 && args[0].equalsIgnoreCase("add")  && sender.hasPermission("cymquest.add") && sender instanceof Player){
					Player player = (Player)sender;
					Block block = player.getTargetBlock(ToolSelector.airTransparents, 10);
					if(block.getType() != Material.AIR){
						int idnpc = Integer.valueOf(args[1]);
						String name = args[2];
						CYMNPC npc = CYMNPC.get(idnpc);
						CYMNPC npcB = CYMNPC.getBlock(idnpc);
						if(npc == null && npcB == null){
							new CYMNPC(true, idnpc, name, block.getLocation()).createBlockNpc();
							player.sendMessage(ChatColor.GRAY+"Add npc block id:"+idnpc+" world:"+block.getWorld().getName()+" x:"+block.getX()+" y:"+block.getY()+" z:"+block.getZ()+".");
						}else
							player.sendMessage(ChatColor.GRAY+"Npc id:"+idnpc+" exist.");
					}else
						player.sendMessage(ChatColor.GRAY+"No block found.");
					return true;
					
				// --- move ---
				}else if(args.length >= 2 && args[0].equalsIgnoreCase("move")  && sender.hasPermission("cymquest.move") && sender instanceof Player){
					Player player = (Player)sender;
					Block block = player.getTargetBlock(ToolSelector.airTransparents, 10);
					if(block.getType() != Material.AIR){
						int idnpc = Integer.valueOf(args[1]);
						CYMNPC npc = CYMNPC.get(idnpc);
						CYMNPC npcB = CYMNPC.getBlock(idnpc);
						if(npc == null && npcB == null){
							player.sendMessage(ChatColor.GRAY+"Npc id:"+idnpc+" doesn't exist.");
						}else{
							if(args.length == 3 && args[2].equalsIgnoreCase("toNpc")){
								if(npcB != null && npcB.isBlock){
									npcB.blockToNpc();
									player.sendMessage(ChatColor.GRAY+"Block Npc id:"+idnpc+" move on real npc.");
								}else if(npc != null && !npc.isBlock)
									player.sendMessage(ChatColor.GRAY+"Npc id:"+idnpc+" is already a real npc.");
								else player.sendMessage(ChatColor.GRAY+"Npc id:"+idnpc+" bad format.");
							}else if(args.length == 2){
								if(npcB != null && npcB.isBlock){
									npcB.moveNpcBlock(block);
									player.sendMessage(ChatColor.GRAY+"Npc id:"+idnpc+" move on "+npcB.world.getName()+" x:"+npcB.x+" y:"+npcB.y+" z:"+npcB.z);
								}else if(npc != null && !npc.isBlock){
									npc.npcToBlock(block);
									player.sendMessage(ChatColor.GRAY+"Real Npc id:"+idnpc+" move on "+npc.world.getName()+" x:"+npc.x+" y:"+npc.y+" z:"+npc.z);
								}else player.sendMessage(ChatColor.GRAY+"Npc id:"+idnpc+" bad format.");
							}else return false;
						}
					}else player.sendMessage(ChatColor.GRAY+"No block found.");
					return true;
					
				// --- remove ---
				}else if(args.length == 2 && args[0].equalsIgnoreCase("remove")  && sender.hasPermission("cymquest.remove") && sender instanceof Player){
					Player player = (Player)sender;
					int idnpc = Integer.valueOf(args[1]);
					CYMNPC npc = CYMNPC.getBlock(idnpc);
					if(npc == null)
						player.sendMessage(ChatColor.GRAY+"Npc id:"+idnpc+" doesn't exist.");
					else if(npc.isBlock){
						npc.removeBlockNpcMeta();
						npc.delete();
						player.sendMessage(ChatColor.GRAY+"Remove npc block id:"+idnpc+".");
					}else
						player.sendMessage(ChatColor.GRAY+"Npc id:"+idnpc+" isn't block.");
					return true;
					
				// --- help ---
				}else if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help")) || (args.length == 2 && args[0].equalsIgnoreCase("help") && args[1].equalsIgnoreCase("1"))){
					sender.sendMessage(ChatColor.GREEN+"**** HELP MINICRAFT QUEST 1/2 ****");
					String tiret = ChatColor.GREEN+"-"+ChatColor.GRAY;
					String slash = ChatColor.GREEN+"/"+ChatColor.GRAY;
					String etoile = ChatColor.GREEN+"*"+ChatColor.GRAY;
					//if(sender.hasPermission("cymquest.load")) sender.sendMessage(ChatColor.GRAY+"/quest load - Initialisation and loading cymcraft quest.");
					if(sender.hasPermission("cymquest.questedit")) sender.sendMessage(slash+"quest npc "+tiret+" Open editor gui selected npc.");
					if(sender.hasPermission("cymquest.questedit")) sender.sendMessage(slash+"quest open <idNPC> [<player>] "+tiret+" Open editor gui npc or quest gui for a player.");
					if(sender.hasPermission("cymquest.questedit")) sender.sendMessage(slash+"quest accept|decline|validate <idNPC> <idQuest> [<player>] "+tiret+" Manage the quest for a player.");
					if(sender.hasPermission("cymquest.questedit")) sender.sendMessage(slash+"quest reset <player> [<idQuest>] "+tiret+" Reset quest player or all quests.");
					if(sender.hasPermission("cymquest.questedit")) sender.sendMessage(slash+"quest edit <player> "+tiret+" Open quests historic gui.");
					//if(sender.isOp()) sender.sendMessage(ChatColor.GRAY+"/quest spy [<player>] - Ignores some mechanisms.");
					if(sender.hasPermission("cymquest.quest")) sender.sendMessage(etoile+" right click on npc "+tiret+" open npc quests.");
					if(sender.hasPermission("cymquest.questedit")) sender.sendMessage(etoile+" right click + 'sneak' on npc "+tiret+" open npc quests edit.");
					if(sender.hasPermission("cymquest.quest")) sender.sendMessage(etoile+" key N "+tiret+" Open current quests.");
					if(sender.hasPermission("cymquest.quest")) sender.sendMessage(etoile+" key B "+tiret+" Open current events.");
					if(sender.hasPermission("cymquest.quest")) sender.sendMessage(etoile+" key , "+tiret+" Open skills gui.");
					if(sender.hasPermission("cymquest.quest")) sender.sendMessage(etoile+" key C "+tiret+" Toggle skill bar.");
					return true;
				}else if(args.length == 2 && args[0].equalsIgnoreCase("help") && args[1].equalsIgnoreCase("2")){
					String tiret = ChatColor.GREEN+"-"+ChatColor.GRAY;
					String slash = ChatColor.GREEN+"/"+ChatColor.GRAY;
					sender.sendMessage(ChatColor.GREEN+"**** HELP MINICRAFT QUEST 2/2 ****");
					if(sender.hasPermission("cymquest.add")) sender.sendMessage(slash+"quest add <idNPC> <name> "+tiret+" Add target block as npc for to open quests.");
					if(sender.hasPermission("cymquest.move")) sender.sendMessage(slash+"quest move <idNPC> [toNpc] "+tiret+" Move npc block or real npc on target block or on real npc with same id.");
					if(sender.hasPermission("cymquest.remove")) sender.sendMessage(slash+"quest remove <idNPC> "+tiret+" Remove npc block.");
					if(sender.hasPermission("cymquest.questedit")) sender.sendMessage(slash+"quest event [slot] "+tiret+" Open event gui.");
					if(sender.hasPermission("cymquest.questedit")) sender.sendMessage(slash+"quest start <event> "+tiret+" Start event.");
					if(sender.hasPermission("cymquest.questedit")) sender.sendMessage(slash+"quest stop <event> "+tiret+" Stop event.");
					if(sender.hasPermission("cymquest.questedit")) sender.sendMessage(slash+"quest mekabox|meka|box [<category>] "+tiret+" Open mekabox gui.");
					if(sender.hasPermission("cymquest.questedit")) sender.sendMessage(slash+"quest book <idNPC> <title> "+tiret+" Add quest book.");
					if(sender.hasPermission("cymquest.questedit")) sender.sendMessage(slash+"quest icons "+tiret+" Load quest icon files.");
					return true;
				// --- open with id ---
				}else if(args.length == 2 && args[0].equalsIgnoreCase("open")  && sender.hasPermission("cymquest.questedit") && sender instanceof Player){
					Player player = (Player)sender;
					int npc = Integer.valueOf(args[1]);
					qp.npcselect = npc;
					new DataQuestModScreen(DataQuestModScreen.OPENQUESTMOD, npc).callEvent(player); // open GUI
					sender.sendMessage(ChatColor.GRAY+"Quest open ...");
					return true;
					
				}else if(args.length == 3 && args[0].equalsIgnoreCase("open") && sender.hasPermission("cymquest.questedit")){
					QuestPlayer qplayer = QuestPlayer.get(args[2]);
					if(qplayer != null){
						int npc = Integer.valueOf(args[1]);
						qplayer.npcselect = npc;
						new DataQuestScreen(DataQuestScreen.NPC, npc).callEvent(qplayer.getPlayer()); // open GUI
						qplayer.sendMessage(ChatColor.GRAY+"Quest open ...");
					}
					return true;
					
				// --- reset ---
				}else if(args.length >= 2 && args[0].equalsIgnoreCase("reset")  && sender.hasPermission("cymquest.questedit")){
					QuestPlayer qplayer = QuestPlayer.get(args[1]);
					if(qplayer != null){
						if(args.length == 2){
							for(Quest q : qplayer.getQuestsCurrent().toArray(new Quest[0])) q.decline(qplayer);
							for(Quest q : qplayer.getQuestsFinished().toArray(new Quest[0])) q.decline(qplayer);
							sender.sendMessage(ChatColor.GRAY+"Reset Quest ...");
						}else if(args.length == 3){
							Quest q = Quest.get(Integer.valueOf(args[2]));
							if(q != null){
								q.decline(qplayer);
								sender.sendMessage(ChatColor.GRAY+"Reset Quest ...");
							}else sender.sendMessage(ChatColor.GRAY+"Quest not found ...");
						}
					}else sender.sendMessage(ChatColor.GRAY+args[1]+" not found ...");
					return true;
					
				// --- accept - decline - validate ---
				}else if(args.length == 4 && (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("decline") || args[0].equalsIgnoreCase("validate")) && sender.hasPermission("cymquest.questedit")){
					int idnpc = Integer.valueOf(args[1]);
					int idquest = Integer.valueOf(args[2]);
					qp = QuestPlayer.get(args[3]);
					Quest q = Quest.get(idnpc, idquest);
					if(qp != null){
						if(q != null){
							if(args[0].equalsIgnoreCase("accept")){
								q.accept(qp);
								sender.sendMessage(ChatColor.GRAY+"Quest "+q.title+" id "+idquest+" on npc "+idnpc+" accept for "+qp.getName()+" ...");
							}else if(args[0].equalsIgnoreCase("decline")){
								q.decline(qp);
								sender.sendMessage(ChatColor.GRAY+"Quest "+q.title+" id "+idquest+" on npc "+idnpc+" decline for "+qp.getName()+" ...");
							}else if(args[0].equalsIgnoreCase("validate")){
								q.giveReward(qp);
								sender.sendMessage(ChatColor.GRAY+"Quest "+q.title+" id "+idquest+" on npc "+idnpc+" give reward for "+qp.getName()+" ...");
							}
						}else sender.sendMessage(ChatColor.GRAY+"Quest "+args[2]+" on npc "+args[1]+" not found ...");
					}else sender.sendMessage(ChatColor.GRAY+args[3]+" not found ...");
					return true;
					
				}else if(args.length == 3 && (args[0].equalsIgnoreCase("decline") || args[0].equalsIgnoreCase("validate")) && sender.hasPermission("cymquest.questedit")){
					int idnpc = Integer.valueOf(args[1]);
					int idquest = Integer.valueOf(args[2]);
					Quest q = Quest.get(idnpc, idquest);
					if(q != null){
						if(args[0].equalsIgnoreCase("accept")){
							sender.sendMessage(ChatColor.GRAY+"Quest "+q.title+" id "+idquest+" on npc "+idnpc+" can't to be accepted for all players ...");
						}else if(args[0].equalsIgnoreCase("decline")){
							q.cleanAll();
							sender.sendMessage(ChatColor.GRAY+"Quest "+q.title+" id "+idquest+" on npc "+idnpc+" is cleaned for all players ...");
						}else if(args[0].equalsIgnoreCase("validate")){
							q.giveRewardForAll();
							sender.sendMessage(ChatColor.GRAY+"Quest "+q.title+" id "+idquest+" on npc "+idnpc+" is gived reward for all players ...");
						}
					}else sender.sendMessage(ChatColor.GRAY+"Quest "+args[2]+" on npc "+args[1]+" not found ...");
					return true;
					
				// --- Enable / Disable Spy ---
				}else if(args.length == 1 && args[0].equalsIgnoreCase("spy")  && sender.isOp() && sender instanceof Player){
					if(qp.spy){
						qp.spy = false;
						qp.sendMessage(ChatColor.RED+"Disable spy");
					}else{
						qp.spy = true;
						qp.sendMessage(ChatColor.GREEN+"Enable spy");
					}
					return true;
				}else if(args.length == 2 && args[0].equalsIgnoreCase("spy")  && sender.isOp()){
					qp = QuestPlayer.get(args[1]);
					if(qp != null){
						if(qp.spy){
							qp.spy = false;
							qp.sendMessage(ChatColor.RED+"Disable spy");
						}else{
							qp.spy = true;
							qp.sendMessage(ChatColor.GREEN+"Enable spy");
						}
					}else sender.sendMessage(ChatColor.GRAY+args[1]+" not found ...");
					return true;
					
				// --- npc item ---
				}else if(args.length >= 2 && args[0].equalsIgnoreCase("book")  && sender.isOp() && sender instanceof Player){
					Player player = (Player)sender;
					int idnpc = Integer.valueOf(args[1]);
					CYMNPC npc = CYMNPC.get(idnpc);
					if(npc == null) npc = CYMNPC.getBlock(idnpc);
					if(npc != null){
						String title = "";
						for(int i = 2 ; i < args.length ; i++) title += args[i]+" ";
						ItemStack[] contents = player.getInventory().getContents();
						for(int i = 0 ; i < contents.length ; i++){
							if(contents[i] == null){
								ItemStack is = contents[i] = new ItemStack(Material.WRITABLE_BOOK);
								is.setType(Material.WRITABLE_BOOK);
								BookMeta bm = (BookMeta)is.getItemMeta();
								bm.setDisplayName(title);
								bm.setAuthor("quest book");
								bm.setPages(idnpc+"");
								is.setType(Material.WRITTEN_BOOK);
								is.setItemMeta(bm);
								player.setItemInHand(is);
								sender.sendMessage(ChatColor.GRAY+"add item npc "+idnpc+" ...");
								player.getInventory().setContents(contents);
								return true;
							}
						}
						player.sendMessage("Inventory is full !");
					}else sender.sendMessage(ChatColor.GRAY+"npc "+idnpc+" doesn't exist ...");
					return true;
					
				// --- mekabox ---
				}else if(args.length >= 1 && (args[0].equalsIgnoreCase("mekabox") || args[0].equalsIgnoreCase("box") || args[0].equalsIgnoreCase("meka")) && sender.hasPermission("cymquest.questedit") && sender instanceof Player){
					Player player = (Player)sender;
					if(args.length == 1) CmdMekaBox.sendOpen(player);
					else{
						String nameCat = args[1];
						for(int i = 2 ; i < args.length ; i++) nameCat += " "+args[i];
						int idcat = 0;
						for(ICatBox cat : CatBox.get(MekaBox.BOX)){
							if(nameCat.equalsIgnoreCase(cat.getName())){
								idcat = cat.getId();
								break;
							}
						}
						CmdMekaBox.sendOpen(player, idcat);
					}
					player.sendMessage(ChatColor.GRAY+"Mekabox open ...");
					return true;
				// --- icons ---
				}else if(args.length == 1 && args[0].equalsIgnoreCase("icons") && sender.hasPermission("cymquest.questedit")){
					QuestIcon.loadDefault();
					sender.sendMessage(ChatColor.GRAY+"Load icons ...");
					return true;
				// --- Edit quests player ---
				}else if(args.length == 2 && args[0].equalsIgnoreCase("edit") && sender.hasPermission("cymquest.questedit") && sender instanceof Player){
					Player player = (Player)sender;
					qp = QuestPlayer.get(args[1]);
					if(qp != null){
						new DataQuestModScreen().sendEditQuests(player, qp.getId());
						sender.sendMessage(ChatColor.GRAY+"Edit quests "+qp.getName()+" ...");
					}else sender.sendMessage(ChatColor.GRAY+args[1]+" not found ...");
					return true;
				}
    		}catch (Exception e) {
				sender.sendMessage(ChatColor.GRAY+"Error command cym quest.");
				Plugin.log("Error command cym quest !");
				e.printStackTrace();
			}
    	// ************** SKILLS **************
    	}else if(label.equalsIgnoreCase("skill") || label.equalsIgnoreCase("cymskill")){
    		QuestPlayer qp = null;
    		if(sender instanceof Player){
        		Player player = (Player)sender;
        		qp = QuestPlayer.get(player);
        		if(!qp.useModQuest()) return false;
        	}
    		try{
    			// --- skills ---
    			if(args.length == 0 && sender.hasPermission("cymquest.skill") && sender instanceof Player){
    				GUISkillScreen.open(qp.getPlayer());
    				qp.sendMessage(ChatColor.GRAY+"Skills open ...");
    				return true;
    			// --- skills HELP ---
    			}else if(args.length == 1 && args[0].equalsIgnoreCase("help") && sender.hasPermission("cymquest.skill")){
    				sender.sendMessage(ChatColor.GREEN+"**** HELP MINICRAFT SKILL ****");
					if(sender.hasPermission("cymquest.skill")) sender.sendMessage(ChatColor.GRAY+"/skill - Open skills gui.");
					if(sender.hasPermission("cymquest.skill")) sender.sendMessage(ChatColor.GRAY+"*key ',' - Open skills gui.");
					if(sender.hasPermission("cymquest.skilledit")) sender.sendMessage(ChatColor.GRAY+"/skilledit [<class> [<skill>]] - Open skills edit gui.");
					if(sender.hasPermission("cymquest.skilledit")) sender.sendMessage(ChatColor.GRAY+"/classedit [<class>] - Open classes edit gui.");
					if(sender.hasPermission("cymquest.skilledit")) sender.sendMessage(ChatColor.GRAY+"/skill info <player> [<class>] - Show player classes/skills.");
					if(sender.hasPermission("cymquest.skilledit")) sender.sendMessage(ChatColor.GRAY+"/skill activate|deactivate <player> <class> [<skill>]");
					if(sender.hasPermission("cymquest.skilledit")) sender.sendMessage(ChatColor.GRAY+"/skill level|levelforce add|init <amount> <player> <class> [<skill>]");
					if(sender.hasPermission("cymquest.skilledit")) sender.sendMessage(ChatColor.GRAY+"/skill xp|xpforce add|init <amount> <player> <class> [<skill>]");
					if(sender.hasPermission("cymquest.skilledit")) sender.sendMessage(ChatColor.GRAY+"* force : add xp/level on class/skill deactivated.");
					if(sender.hasPermission("cymquest.skilledit")) sender.sendMessage(ChatColor.GRAY+"/skill erase <player> <class> [<skill>] - Erase state player.");
					if(sender.hasPermission("cymquest.skill")) sender.sendMessage(ChatColor.GRAY+"/skill inventory"+(sender.hasPermission("cymquest.skilledit") ? " [<player>]" : "")+" - Open skill inventory player.");
        			return true;
        		// --- info ---
        		}else if(args.length >= 2 && args[0].equalsIgnoreCase("info") && sender.hasPermission("cymquest.skilledit")){
    				boolean activate = args[0].equalsIgnoreCase("activate");
    				qp = QuestPlayer.get(args[1]);
    				if(qp != null){
    					List<StateSkillManager> list = new ArrayList<StateSkillManager>();
    					if(args.length == 3){
    						CYMClass mc = CYMClass.get(args[2]);
            				if(mc != null){
            					StateCYMClass smc = qp.getCYMClass(mc.getId());
            					if(smc != null){
            						list.addAll(smc.getStateCYMSkills());
            						sender.sendMessage(ChatColor.GREEN+"**** INFO "+qp.getName()+" skills ****");
            						sender.sendMessage(ChatColor.GREEN+"*** "+ChatColor.GRAY+"Class "+smc.getName()+" "+(smc.isActivated() ? "activated" : "deactivated")+" level : "+smc.getLevel()+"  xp : "+smc.getXP()+"  tier : "+smc.getTier());
            					}else sender.sendMessage(ChatColor.GRAY+"Class "+args[2]+" for player "+args[1]+" not found ...");
            				}else sender.sendMessage(ChatColor.GRAY+"Class "+args[2]+" not found ...");
    					}else{
    						sender.sendMessage(ChatColor.GREEN+"**** INFO "+qp.getName()+" classes ****");
    						list.addAll(qp.getCYMClasses());
    					}
    					for(StateSkillManager ssm : list)
    						sender.sendMessage(ChatColor.GREEN+"* "+ChatColor.GRAY+ssm.getName()+" "+(ssm.isActivated() ? "activated" : "deactivated")+" level : "+ssm.getLevel()+"  xp : "+ssm.getXP()+"  tier : "+ssm.getTier());
    					
    				}else sender.sendMessage(ChatColor.GRAY+args[1]+" not found ...");
    				return true;
        		// --- activate ---
        		}else if(args.length >= 3 && (args[0].equalsIgnoreCase("activate") || args[0].equalsIgnoreCase("deactivate")) && sender.hasPermission("cymquest.skilledit")){
    				boolean activate = args[0].equalsIgnoreCase("activate");
    				qp = QuestPlayer.get(args[1]);
    				if(qp != null){
        				CYMClass mc = CYMClass.get(args[2]);
        				if(mc != null){
        					if(args.length == 4){
        						CYMSkill ms = CYMSkill.get(mc, args[3]);
        						if(ms != null){
        							if(activate) ms.activate(qp); else ms.deactivate(qp);
        							sender.sendMessage(ChatColor.GRAY+"Skill "+args[3]+" "+(activate ? "activated" : "deactivated")+" ...");
        						}else sender.sendMessage(ChatColor.GRAY+"Skill "+args[3]+" not found ...");
        					}else{
        						if(activate) mc.activate(qp); else mc.deactivate(qp);
    		    				sender.sendMessage(ChatColor.GRAY+"Class "+args[2]+" "+(activate ? "activated" : "deactivated")+" ...");
        					}
        				}else sender.sendMessage(ChatColor.GRAY+"Class "+args[2]+" not found ...");
    				}else sender.sendMessage(ChatColor.GRAY+args[1]+" not found ...");
    				return true;
    			// --- level ---
	    		}else if(args.length >= 5 && (args[0].equalsIgnoreCase("level") || args[0].equalsIgnoreCase("levelforce")) && sender.hasPermission("cymquest.skilledit")){
	    			boolean force = args[0].equalsIgnoreCase("levelforce");
					boolean add = args[1].equalsIgnoreCase("add");
					boolean init = args[1].equalsIgnoreCase("init");
					int amount = Integer.valueOf(args[2]);
					qp = QuestPlayer.get(args[3]);
					if(qp != null){
	    				CYMClass mc = CYMClass.get(args[4]);
	    				if(mc != null){
	    					if(args.length == 6){
								CYMSkill ms = CYMSkill.get(mc, args[5]);
	    						if(ms != null){
	    							StateCYMSkill sms = qp.getCYMSkill(mc.getId(), ms.getId());
	    							if(sms != null){
	    								if(add) sms.changeLevel(sms.getLevel() + amount, force);
	    								else if(init) sms.changeLevel(amount, force);
	    								sender.sendMessage(ChatColor.GRAY+"Skill "+args[5]+" "+(add ? "add" : "init")+" level of "+amount+" on player "+args[3]+" ...");
	    							}else sender.sendMessage(ChatColor.GRAY+"Skill "+args[5]+" for player "+args[3]+" not found ...");
	    						}else sender.sendMessage(ChatColor.GRAY+"Skill "+args[5]+" not found ...");
	    					}else{
	    						StateCYMClass smc = qp.getCYMClass(mc.getId());
    							if(smc != null){
    								if(add) smc.changeLevel(smc.getLevel() + amount, force);
    								else if(init) smc.changeLevel(amount, force);
    								sender.sendMessage(ChatColor.GRAY+"Class "+args[4]+" "+(add ? "add" : "init")+" level of "+amount+" on player "+args[3]+" ...");
    							}else sender.sendMessage(ChatColor.GRAY+"Class "+args[4]+" for player "+args[3]+" not found ...");
	    					}
	    				}else sender.sendMessage(ChatColor.GRAY+"Class "+args[4]+" not found ...");
					}else sender.sendMessage(ChatColor.GRAY+args[3]+" not found ...");
					return true;
				// --- xp ---
	    		}else if(args.length >= 5 && (args[0].equalsIgnoreCase("xp") || args[0].equalsIgnoreCase("xpforce")) && sender.hasPermission("cymquest.skilledit")){
	    			boolean force = args[0].equalsIgnoreCase("xpforce");
					boolean add = args[1].equalsIgnoreCase("add");
					boolean init = args[1].equalsIgnoreCase("init");
					int amount = Integer.valueOf(args[2]);
					qp = QuestPlayer.get(args[3]);
					if(qp != null){
	    				CYMClass mc = CYMClass.get(args[4]);
	    				if(mc != null){
	    					if(args.length == 6){
	    						CYMSkill ms = CYMSkill.get(mc, args[5]);
	    						if(ms != null){
	    							StateCYMSkill sms = qp.getCYMSkill(mc.getId(), ms.getId());
	    							if(sms != null){
	    								if(add) sms.changeXp(sms.getXP() + amount, force);
	    								else if(init) sms.changeXp(amount, force);
	    								sender.sendMessage(ChatColor.GRAY+"Skill "+args[5]+" "+(add ? "add" : "init")+" xp of "+amount+" on player "+args[3]+" ...");
	    							}else sender.sendMessage(ChatColor.GRAY+"Skill "+args[5]+" for player "+args[3]+" not found ...");
	    						}else sender.sendMessage(ChatColor.GRAY+"Skill "+args[5]+" not found ...");
	    					}else{
	    						StateCYMClass smc = qp.getCYMClass(mc.getId());
    							if(smc != null){
    								if(add) smc.changeXp(smc.getXP() + amount, force);
    								else if(init) smc.changeXp(amount, force);
    								sender.sendMessage(ChatColor.GRAY+"Class "+args[4]+" "+(add ? "add" : "init")+" xp of "+amount+" on player "+args[3]+" ...");
    							}else sender.sendMessage(ChatColor.GRAY+"Class "+args[4]+" for player "+args[3]+" not found ...");
	    					}
	    				}else sender.sendMessage(ChatColor.GRAY+"Class "+args[4]+" not found ...");
					}else sender.sendMessage(ChatColor.GRAY+args[3]+" not found ...");
					return true;
    			// --- erase ---
	    		}else if(args.length >= 3 && args[0].equalsIgnoreCase("erase") && sender.hasPermission("cymquest.skilledit")){
					qp = QuestPlayer.get(args[1]);
					if(qp != null){
						CYMClass mc = CYMClass.get(args[2]);
        				if(mc != null){
        					StateCYMClass smc = qp.getCYMClass(mc.getId());
        					if(smc != null){
        						if(args.length == 4){
        							CYMSkill ms = CYMSkill.get(mc, args[3]);
        							if(ms != null){
        								StateCYMSkill sms = qp.getCYMSkill(mc.getId(), ms.getId());
    	    							if(sms != null){
    	    								sms.erase();
    	    								sender.sendMessage(ChatColor.GRAY+"Skill "+args[3]+" erased ...");
    	    							}else sender.sendMessage(ChatColor.GRAY+"Skill "+args[3]+" for player "+args[1]+" not found ...");
        							}else sender.sendMessage(ChatColor.GRAY+"Skill "+args[3]+" not found ...");
        						}else{
        							smc.erase();
        							sender.sendMessage(ChatColor.GRAY+"Class "+args[2]+" erased ...");		
        						}
        					}else sender.sendMessage(ChatColor.GRAY+"Class "+args[2]+" for player "+args[1]+" not found ...");
        				}else sender.sendMessage(ChatColor.GRAY+"Class "+args[2]+" not found ...");						
					}else sender.sendMessage(ChatColor.GRAY+args[1]+" not found ...");
					return true;
				// --- inventory ---
	    		}else if(args.length >= 1 && args[0].equalsIgnoreCase("inventory") && sender.hasPermission("cymquest.skill")){
	    			QuestPlayer qpSender = QuestPlayer.get((Player)sender);
	    			if(args.length == 2 && sender.hasPermission("cymquest.skilledit")) qp = QuestPlayer.get(args[1]);
	    			else qp = qpSender;
					if(qp != null){
						SkillInventory.open(qpSender, qp);
						sender.sendMessage(ChatColor.GRAY+"Open inventory "+qp.getName()+" ...");
					}else sender.sendMessage(ChatColor.GRAY+args[1]+" not found ...");
					return true;
	    		}
	    	}catch (Exception e) {
				sender.sendMessage(ChatColor.GRAY+"Error command cym skill.");
				Plugin.log("Error command cym skill !");
				e.printStackTrace();
			}
    		// --- skills edit ---
    	}else if((label.equalsIgnoreCase("skilledit") || label.equalsIgnoreCase("cymskilledit")) && sender.hasPermission("cymquest.skilledit") && sender instanceof Player){
    		Player player = (Player)sender;
    		QuestPlayer qp = QuestPlayer.get(player);
    		if(!qp.useModQuest()) return false;
    		try{
    			if(args.length >= 1){
    				CYMClass mc = CYMClass.get(args[0]);
    				if(mc != null){
    					if(args.length == 2){
    						CYMSkill ms = CYMSkill.get(mc, args[1]);
    						if(ms != null){
    							CmdSkillManager.sendOpenSkill(player, ms.getId(), mc.getId());
    		    				player.sendMessage(ChatColor.GRAY+"Skills open ...");
    						}else player.sendMessage(ChatColor.GRAY+"Skill "+args[1]+" not found ...");
    					}else{
		    				CmdSkillManager.sendOpenSkill(player, 0, mc.getId());
		    				player.sendMessage(ChatColor.GRAY+"Skills open ...");
    					}
    				}else player.sendMessage(ChatColor.GRAY+"Class "+args[0]+" not found ...");
    			}else{
    				CmdSkillManager.sendOpenSkill(player);
    				player.sendMessage(ChatColor.GRAY+"Skills open ...");
    			}
    			return true;
    		}catch (Exception e) {
				sender.sendMessage(ChatColor.GRAY+"Error command cym skill.");
				Plugin.log("Error command cym skill edit !");
				e.printStackTrace();
			}
    		// --- classes edit ---
    	}else if((label.equalsIgnoreCase("classedit") || label.equalsIgnoreCase("cymclassedit")) && sender.hasPermission("cymquest.skilledit") && sender instanceof Player){
    		Player player = (Player)sender;
    		QuestPlayer qp = QuestPlayer.get(player);
    		if(!qp.useModQuest()) return false;
    		try{
    			if(args.length >= 1){
    				CYMClass mc = CYMClass.get(args[0]);
    				if(mc != null){
	    				CmdSkillManager.sendOpenClass(player, mc.getId());
	    				player.sendMessage(ChatColor.GRAY+"Classes open ...");
    				}else player.sendMessage(ChatColor.GRAY+"Class "+args[0]+" not found ...");
    			}else{
    				CmdSkillManager.sendOpenClass(player);
    				player.sendMessage(ChatColor.GRAY+"Classes open ...");
    			}
    			return true;
    		}catch (Exception e) {
				sender.sendMessage(ChatColor.GRAY+"Error command cym class.");
				Plugin.log("Error command cym class edit !");
				e.printStackTrace();
			}
    	}
		return false;
	}

	static private void logDescription(){
		PluginDescriptionFile pdfFile = it.getDescription();
        log( pdfFile.getName() +" "+pdfFile.getVersion() + " is enabled!" );
	}
	static public void log(String msg){
		log.info(msg);
	}
	
	static public void log(String msg, Location loc){
		log(msg+" "+loc.getWorld().getName()+" x:"+loc.getX()+" y:"+loc.getY()+" z:"+loc.getZ());
	}
}
