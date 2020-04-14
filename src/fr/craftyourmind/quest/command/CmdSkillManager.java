package fr.craftyourmind.quest.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import fr.craftyourmind.manager.command.CmdGuiChild;
import fr.craftyourmind.manager.command.CmdGuiEnter;
import fr.craftyourmind.manager.command.CmdGuiMain;
import fr.craftyourmind.skill.ISkillManager;
import fr.craftyourmind.skill.CYMClass;
import fr.craftyourmind.skill.CYMLevel;
import fr.craftyourmind.skill.CYMSkill;
import fr.craftyourmind.skill.CYMTier;
import fr.craftyourmind.skill.SkillManager;

public class CmdSkillManager extends CmdGuiMain{

	private static final int SKILL = 0;
	private static final int CLASS = 1;
	private static final int LEVEL = 2;
	private static final int TIER = 3;
	
	private static final int LEVELDATA = 1;
	private static final int TIERDATA = 2;
	private static final int LISTDATA = 3;
	
	private static CmdSkillManager cmdSkill;
	
	public static void sendOpenSkill(Player p) {
		cmdSkill.sendOpenMain(p, SKILL);
	}
	public static void sendOpenSkill(Player p, int idskill, int idclass) {
		cmdSkill.sendOpenMain(p, SKILL, idskill, idclass);
	}
	public static void sendOpenClass(Player p) {
		cmdSkill.sendOpenMain(p, CLASS);
	}
	public static void sendOpenClass(Player p, int idclass) {
		cmdSkill.sendOpenMain(p, CLASS, idclass, 0);
	}
	
	public CmdSkillManager() {
		super(CmdQuestManager.CMDSKILLS);
		cmdSkill = this;
		permission = "cymquest.skilledit";
	}
	@Override
	public void initChilds() { addChild(new CmdSkill()); addChild(new CmdClass()); addChild(new CmdLevel()); addChild(new CmdTier()); }
	@Override
	public void initActions() { super.initActions(); addAction(new LEVELDATA()); addAction(new TIERDATA()); addAction(new LISTDATA()); }
	// ---- LEVELDATA ----
	class LEVELDATA extends AbsCYMCommandAction{
		private List<Integer> ids = new ArrayList<Integer>();
		private List<String> names = new ArrayList<String>();
		@Override
		public int getId() { return LEVELDATA; }
		@Override
		public AbsCYMCommandAction clone() { return new LEVELDATA(); }
		@Override
		public void initSend(Player p) {
			for(CYMLevel lvl : CYMLevel.get()){
				ids.add(lvl.getId()); names.add(lvl.getName());
			}
		}
		@Override
		public void sendWrite() throws IOException { writeList(ids, names); }
		@Override
		public void receiveRead() throws IOException { }
		@Override
		public void receive(Player p) { send(p, this); }	
	}
	// ---- TIERDATA ----
	class TIERDATA extends AbsCYMCommandAction{
		private List<Integer> ids = new ArrayList<Integer>();
		private List<String> names = new ArrayList<String>();
		@Override
		public int getId() { return TIERDATA; }
		@Override
		public AbsCYMCommandAction clone() { return new TIERDATA(); }
		@Override
		public void initSend(Player p) {
			for(CYMTier tier : CYMTier.get()){
				ids.add(tier.getId()); names.add(tier.getName());
			}
		}
		@Override
		public void sendWrite() throws IOException { writeList(ids, names); }
		@Override
		public void receiveRead() throws IOException { }
		@Override
		public void receive(Player p) { send(p, this); }	
	}
	// ---- LISTDATA ----
	class LISTDATA extends AbsCYMCommandAction{
		private int type, idskill, idparent;
		private List<Integer> ids = new ArrayList<Integer>();
		private List<String> names = new ArrayList<String>();
		@Override
		public int getId() { return LISTDATA; }
		@Override
		public AbsCYMCommandAction clone() { return new LISTDATA(); }
		@Override
		public void initSend(Player p) {
			ISkillManager sm = SkillManager.get(type, idskill, idparent);
			if(sm != null){
				for(ISkillManager link : sm.getAllLinks()){
					ids.add(link.getId()); names.add(link.getName());
				}
			}
		}
		@Override
		public void sendWrite() throws IOException { writeList(ids, names); }
		@Override
		public void receiveRead() throws IOException { type = readInt(); idskill = readInt(); idparent = readInt(); }
		@Override
		public void receive(Player p) { send(p, this); }	
	}
	// **************** SKILL ****************
	class CmdSkill extends CmdGuiChild{

		public CmdSkill() { super(CmdSkillManager.this, SKILL); }
		@Override
		public void initChilds() { }
		@Override
		public void initSendOpen(List<Integer> idlists, List<String> namelists, List<Integer> orderlists, int... ids) {
			for(CYMClass mc : CYMClass.getCYMClass()){ idlists.add(mc.getId()); namelists.add(mc.getName()); orderlists.add(mc.getOrder()); }
		}
		@Override
		public void initSendOpenChild(List<Integer> idchilds, List<String> namechilds, List<Integer> orderlists, int... ids) {
			CYMClass mc = CYMClass.getCYMClass(ids[1]);
			if(mc != null){
				for(CYMSkill ms : mc.getMekaboxs()){ idchilds.add(ms.getId()); namechilds.add(ms.getName()); orderlists.add(ms.getOrder()); }
			}
		}
		@Override
		public ICmdData getCmdData() {
			return new ICmdData() {
				private int idskill, idclass, limitNode, levelClassActivated, order;
				private String name = "", descriptives = "", linkParents = "", linkChilds = "", levels = "", tiers = "", nodeParents = "", nodeChilds = "", params = "";
				private boolean activated, keepEnableOnLink, showPlayer, activationPlayer, syncNodeParents, showMessage;
				@Override
				public void initSend(int... ids) {
					CYMSkill ms = CYMSkill.getSkill(ids[0], ids[1]);
					if(ms != null){
						idskill = ms.getId(); idclass = ms.getCatId(); name = ms.getName(); levelClassActivated = ms.levelClassActivated; activated = ms.isActivated(); keepEnableOnLink = ms.isKeepEnableOnLink(); showPlayer = ms.isShowPlayer();
						activationPlayer = ms.isActivationPlayer(); showMessage = ms.isShowMessage(); descriptives = ms.getDescriptives(); linkParents = ms.getLinkParentsStr(); linkChilds = ms.getLinkChildsStr(); levels = ms.getLevels();
						tiers = ms.getTiers(); nodeParents = ms.getNodeParentsStr(); nodeChilds = ms.getNodeChildsStr(); limitNode = ms.getLimitNode(); syncNodeParents = ms.isSyncNodeParents();
						params = ms.getParamsCon(); order = ms.getOrder();
					}
				}
				@Override
				public void sendWrite(AbsCYMCommandAction cmd) throws IOException {
					cmd.write(idskill); cmd.write(idclass); cmd.write(name); cmd.write(levelClassActivated); cmd.write(activated); cmd.write(keepEnableOnLink); cmd.write(showPlayer);
					cmd.write(activationPlayer); cmd.write(showMessage); cmd.write(descriptives); cmd.write(linkParents); cmd.write(linkChilds); cmd.write(levels);
					cmd.write(tiers); cmd.write(nodeParents); cmd.write(nodeChilds); cmd.write(limitNode); cmd.write(syncNodeParents);
					cmd.write(params); cmd.write(order);
				}
				@Override
				public void receiveRead(AbsCYMCommandAction cmd) throws IOException {
					idskill = cmd.readInt(); idclass = cmd.readInt(); name = cmd.readStr(); levelClassActivated = cmd.readInt(); activated = cmd.readBool(); keepEnableOnLink = cmd.readBool(); showPlayer = cmd.readBool();
					activationPlayer = cmd.readBool(); showMessage = cmd.readBool(); descriptives = cmd.readStr(); linkParents = cmd.readStr(); linkChilds = cmd.readStr(); levels = cmd.readStr();
					tiers = cmd.readStr(); nodeParents = cmd.readStr(); nodeChilds = cmd.readStr(); limitNode = cmd.readInt(); syncNodeParents = cmd.readBool();
					params = cmd.readStr(); order = cmd.readInt();
				}
				@Override
				public int[] receive() {
					CYMSkill ms = null;
					if(idskill == 0){
						ms = CYMSkill.newCYMSkillCmd(name, idclass);
						ms.questSort.createOrder(order);
					}else ms = CYMSkill.getSkill(idskill, idclass);
					boolean initActivate = activated != ms.isActivated();
					boolean updateLinks = false;
					boolean updateLevel = false;
					boolean updateLevelClassActivated = false;
					boolean updateNodes = false;
					if(!initActivate){
						updateLinks = !linkParents.equals(ms.getLinkParentsStr()) || !linkChilds.equals(ms.getSkillManager().strLinkChilds);
						updateLevel = !levels.equals(ms.getSkillManager().strLevels) || !tiers.equals(ms.getSkillManager().strTiers);
						updateLevelClassActivated = levelClassActivated != ms.levelClassActivated;
						updateNodes = !nodeParents.equals(ms.getNodeParentsStr()) || !nodeChilds.equals(ms.getSkillManager().strNodeChilds);
					}
					ms.setName(name); ms.setCat(idclass); ms.levelClassActivated = levelClassActivated; ms.setActivate(activated);
					ms.setKeepEnableOnLink(keepEnableOnLink); ms.setShowPlayer(showPlayer); ms.setActivationPlayer(activationPlayer); ms.setShowMessage(showMessage); 
					ms.setDescriptives(descriptives); ms.setLinkParents(linkParents); ms.setLinkChilds(linkChilds); ms.setLevels(levels); ms.setTiers(tiers);
					ms.setNodeParents(nodeParents); ms.setNodeChilds(nodeChilds); ms.setLimitNode(limitNode); ms.setSyncNodeParents(syncNodeParents); 
					ms.loadParamsCon(params); ms.setOrder(order);
					ms.save();
					if(initActivate) ms.initActivate();
					else{
						if(updateLinks) ms.updateLinks();
						if(updateLevel) ms.updateLevel();
						if(updateLevelClassActivated) ms.updateLevelClassActivated();
						if(updateNodes) ms.updateNodes();
						if(updateLinks || updateLevel) ms.checkLinks();
					}
					ms.updateCloneData();
					return new int[]{ ms.getId(), ms.getCatId()};
				}
			};
		}
		@Override
		public void receiveDelete(int... ids) {
			CYMSkill ms = CYMSkill.getSkill(ids[0], ids[1]);
			if(ms != null) ms.delete();
		}
		@Override
		public void receiveSort(boolean order, int... ids) {
			CYMSkill ms = CYMSkill.getSkill(ids[0], ids[1]);
			if(ms != null){
				if(order) ms.questSort.upOrder(); else ms.questSort.downOrder();
			}
		}
	}
	// **************** CLASS ****************
	class CmdClass extends CmdGuiEnter{

		public CmdClass() { super(CmdSkillManager.this, CLASS); }
		@Override
		public void initChilds() { }
		@Override
		public void initSendOpen(List<Integer> idlists, List<String> namelists, List<Integer> orderlists,	int... ids) {
			for(CYMClass mc : CYMClass.getCYMClass()){ idlists.add(mc.getId()); namelists.add(mc.getName()); orderlists.add(mc.getOrder()); }
		}
		@Override
		public ICmdData getCmdData() {
			return new ICmdData() {
				private int idclass, limitSkill, limitNode, order;
				private String name = "", descriptives = "", linkParents = "", linkChilds = "", levels = "", tiers = "", nodeParents = "", nodeChilds = "", params = "";
				private boolean activated, keepEnableOnLink, showPlayer, activationPlayer, syncNodeParents, showMessage;
				@Override
				public void initSend(int... ids) {
					CYMClass mc = CYMClass.getCYMClass(ids[0]);
					if(mc != null){
						idclass = mc.getId(); name = mc.getName(); limitSkill = mc.limitSkill; activated = mc.isActivated(); keepEnableOnLink = mc.isKeepEnableOnLink(); showPlayer = mc.isShowPlayer();
						activationPlayer = mc.isActivationPlayer(); showMessage = mc.isShowMessage(); descriptives = mc.getDescriptives(); linkParents = mc.getLinkParentsStr(); linkChilds = mc.getLinkChildsStr(); levels = mc.getLevels();
						tiers = mc.getTiers(); nodeParents = mc.getNodeParentsStr(); nodeChilds = mc.getNodeChildsStr(); limitNode = mc.getLimitNode(); syncNodeParents = mc.isSyncNodeParents();
						params = mc.getParamsCon(); order = mc.getOrder();
					}
				}
				@Override
				public void sendWrite(AbsCYMCommandAction cmd) throws IOException {
					cmd.write(idclass); cmd.write(name); cmd.write(limitSkill); cmd.write(activated); cmd.write(keepEnableOnLink); cmd.write(showPlayer);
					cmd.write(activationPlayer); cmd.write(showMessage); cmd.write(descriptives); cmd.write(linkParents); cmd.write(linkChilds); cmd.write(levels);
					cmd.write(tiers); cmd.write(nodeParents); cmd.write(nodeChilds); cmd.write(limitNode); cmd.write(syncNodeParents);
					cmd.write(params); cmd.write(order);
				}
				@Override
				public void receiveRead(AbsCYMCommandAction cmd) throws IOException {
					idclass = cmd.readInt(); name = cmd.readStr(); limitSkill = cmd.readInt(); activated = cmd.readBool(); keepEnableOnLink = cmd.readBool(); showPlayer = cmd.readBool();
					activationPlayer = cmd.readBool(); showMessage = cmd.readBool(); descriptives = cmd.readStr(); linkParents = cmd.readStr(); linkChilds = cmd.readStr(); levels = cmd.readStr();
					tiers = cmd.readStr(); nodeParents = cmd.readStr(); nodeChilds = cmd.readStr(); limitNode = cmd.readInt(); syncNodeParents = cmd.readBool();
					params = cmd.readStr(); order = cmd.readInt();
				}
				@Override
				public int[] receive() {
					CYMClass mc = null;
					if(idclass == 0){
						mc = CYMClass.newCYMClassCmd(name);
						mc.questSort.createOrder(order);
					}else mc = CYMClass.getCYMClass(idclass);
					boolean initActivate = activated != mc.isActivated();
					boolean updateLinks = false;
					boolean updateLevel = false;
					boolean updateLimitSkill = false;
					boolean updateNodes = false;
					if(!initActivate){
						updateLinks = !linkParents.equals(mc.getLinkParentsStr()) || !linkChilds.equals(mc.getSkillManager().strLinkChilds);
						updateLevel = !levels.equals(mc.getSkillManager().strLevels) || !tiers.equals(mc.getSkillManager().strTiers);
						updateLimitSkill = limitSkill != mc.limitSkill;
						updateNodes = !nodeParents.equals(mc.getNodeParentsStr()) || !nodeChilds.equals(mc.getSkillManager().strNodeChilds);
					}
					mc.setName(name); mc.setActivate(activated); mc.limitSkill = limitSkill;
					mc.setKeepEnableOnLink(keepEnableOnLink); mc.setShowPlayer(showPlayer); mc.setActivationPlayer(activationPlayer); mc.setShowMessage(showMessage);
					mc.setDescriptives(descriptives); mc.setLinkParents(linkParents); mc.setLinkChilds(linkChilds); mc.setLevels(levels); mc.setTiers(tiers);
					mc.setNodeParents(nodeParents); mc.setNodeChilds(nodeChilds); mc.setLimitNode(limitNode); mc.setSyncNodeParents(syncNodeParents);
					mc.loadParamsCon(params); mc.setOrder(order);
					mc.save();
					if(initActivate) mc.initActivate();
					else{
						if(updateLinks) mc.updateLinks();
						if(updateLevel) mc.updateLevel();
						if(updateLimitSkill) mc.updateLimitSkill();
						if(updateNodes) mc.updateNodes();
						if(updateLinks || updateLevel) mc.checkLinks();
					}
					mc.updateCloneData();
					return new int[]{ mc.getId() };
				}
			};
		}
		@Override
		public void receiveDelete(int... ids) {
			CYMClass mc = CYMClass.getCYMClass(ids[0]);
			if(mc != null) mc.delete();
		}
		@Override
		public void receiveSort(boolean order, int... ids) {
			CYMClass mc = CYMClass.getCYMClass(ids[0]);
			if(mc != null){
				if(order) mc.questSort.upOrder(); else mc.questSort.downOrder();
			}
		}
		// -------------- NODES -------------
		public void initSendOpenNodes(List<String> listnodes) {
			for(CYMClass mc : CYMClass.getCYMClass())
				listnodes.add(mc.getNodeChildsStr());
		}
		@Override
		public OPEN getOpen() { return new OPENNODE(); }
		class OPENNODE extends OPEN{
			private List<String> listnodes = new ArrayList<String>();
			@Override
			public OPEN clone() { return new OPENNODE(); }
			@Override
			public void initSend(Player p) {
				super.initSend(p);
				initSendOpenNodes(listnodes);
			}
			@Override
			public void sendWrite() throws IOException {
				writeListStr(listnodes);
				super.sendWrite();
			}
		}
	}
	// **************** LEVEL ****************
	class CmdLevel extends CmdGuiEnter{

		public CmdLevel() { super(CmdSkillManager.this, LEVEL); }
		@Override
		public void initChilds() { }
		@Override
		public void initSendOpen(List<Integer> idlists, List<String> namelists, List<Integer> orderlists, int... ids) {
			for(CYMLevel ml : CYMLevel.get()){ idlists.add(ml.getId()); namelists.add(ml.getName()); }
		}
		@Override
		public ICmdData getCmdData() {
			return new ICmdData() {
				private int idlevel, lvlBegin, lvlEnd, baseXP;
				private String name = "";
				private float coefMulti = 1.5f;
				@Override
				public void initSend(int... ids) {
					CYMLevel lvl = CYMLevel.get(ids[0]);
					if(lvl != null){
						idlevel = lvl.getId();
						name = lvl.getName();
						lvlBegin = lvl.lvlBegin;
						lvlEnd = lvl.lvlEnd;
						baseXP = lvl.baseXP;
						coefMulti = lvl.coefMulti;
					}
				}
				@Override
				public void sendWrite(AbsCYMCommandAction cmd) throws IOException {
					cmd.write(idlevel); cmd.write(name); cmd.write(lvlBegin);
					cmd.write(lvlEnd); cmd.write(baseXP); cmd.write(coefMulti);
				}
				@Override
				public void receiveRead(AbsCYMCommandAction cmd) throws IOException {
					idlevel = cmd.readInt();
					name = cmd.readStr();
					lvlBegin = cmd.readInt();
					lvlEnd = cmd.readInt();
					baseXP = cmd.readInt();
					coefMulti = cmd.readFloat();
				}
				@Override
				public int[] receive() {
					CYMLevel lvl = null;
					if(idlevel == 0) lvl = CYMLevel.newCYMLevelCmd();
					else lvl = CYMLevel.get(idlevel);
					lvl.setName(name); lvl.lvlBegin = lvlBegin;
					lvl.lvlEnd = lvlEnd; lvl.baseXP = baseXP; lvl.coefMulti = coefMulti;
					lvl.save();
					lvl.updateState();
					return new int[]{ lvl.getId() };
				}
			};
		}
		@Override
		public void receiveDelete(int... ids) {
			CYMLevel ml = CYMLevel.get(ids[0]);
			if(ml != null) ml.delete();
		}
		@Override
		public void receiveSort(boolean order, int... ids) {
			
		}
	}
	// **************** TIER ****************
	class CmdTier extends CmdGuiEnter{

		public CmdTier() { super(CmdSkillManager.this, TIER); }
		@Override
		public void initChilds() { }
		@Override
		public void initSendOpen(List<Integer> idlists, List<String> namelists, List<Integer> orderlists, int... ids) {
			for(CYMTier mt : CYMTier.get()){ idlists.add(mt.getId()); namelists.add(mt.getName()); }
		}
		@Override
		public ICmdData getCmdData() {
			return new ICmdData() {
				private int idtier;
				private String name = "";
				private int limit;
				@Override
				public void initSend(int... ids) {
					CYMTier tier = CYMTier.get(ids[0]);
					if(tier != null){
						idtier = tier.getId();
						name = tier.getName();
						limit = tier.limit;
					}
				}
				@Override
				public void sendWrite(AbsCYMCommandAction cmd) throws IOException {
					cmd.write(idtier); cmd.write(name); cmd.write(limit);
				}
				@Override
				public void receiveRead(AbsCYMCommandAction cmd) throws IOException {
					idtier = cmd.readInt(); name = cmd.readStr(); limit = cmd.readInt();
				}
				@Override
				public int[] receive() {
					CYMTier tier = null;
					if(idtier == 0) tier = CYMTier.newCYMTierCmd();
					else tier = CYMTier.get(idtier);
					tier.setName(name); tier.limit = limit;
					tier.save();
					tier.updateState();
					return new int[]{ tier.getId() };
				}
			};
		}
		@Override
		public void receiveDelete(int... ids) {
			CYMTier mt = CYMTier.get(ids[0]);
			if(mt != null) mt.delete();
		}
		@Override
		public void receiveSort(boolean order, int... ids) {
			
		}
	}
}