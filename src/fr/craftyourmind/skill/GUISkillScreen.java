package fr.craftyourmind.skill;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import fr.craftyourmind.manager.command.AbsCYMCommand;
import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.command.CmdQuestManager;

public class GUISkillScreen extends AbsCYMCommand{

	private static final int OPEN = 0;
	private static final int OPENCLASS = 1;
	private static final int OPENSKILL = 2;
	private static final int ACTIVATE = 3;
	private static final int INVENTORY = 4;
	private static final int SKILLBAR = 5;
	
	private static GUISkillScreen guiskill;
	
	public static void open(Player p){ if(p.hasPermission("cymquest.skill")) guiskill.send(p, OPEN); }
	
	public static void sendBarMode(Player p, boolean barmode){ guiskill.send(p, guiskill.new SKILLBAR(barmode)); }
	
	public GUISkillScreen() {
		super(CmdQuestManager.CMDGUISKILLS);
		guiskill = this;
	}
	@Override
	public void initChilds() { }
	@Override
	public void initActions() {
		addAction(new OPEN(), new OPENCLASS(), new OPENSKILL(), new ACTIVATE(), new INVENTORY(), new SKILLBAR());
	}
	// ---- OPEN ----
	class OPEN extends AbsCYMCommandAction{
		private List<Integer> idclasses = new ArrayList<Integer>();
		private List<String> nameclasses = new ArrayList<String>();
		private List<Integer> orders = new ArrayList<Integer>();
		@Override
		public int getId() { return OPEN; }
		@Override
		public AbsCYMCommandAction clone() { return new OPEN(); }
		@Override
		public void initSend(Player p) {
			QuestPlayer qp = QuestPlayer.get(p);
			for(StateCYMClass smc : qp.getCYMClasses()){
				if(smc.getContainer().isShowPlayer()){
					idclasses.add(smc.getId());
					nameclasses.add(smc.getName());
					orders.add(smc.getOrder());
				}
			}
		}		
		@Override
		public void sendWrite() throws IOException {
			writeList(idclasses, nameclasses);
			writeListInt(orders);
		}
		@Override
		public void receiveRead() throws IOException { }
		@Override
		public void receive(Player p) {
			if(p.hasPermission("cymquest.skill")) send(p, this);
		}
	}
	// ---- OPENCLASS ----
	class OPENCLASS extends AbsCYMCommandAction{
		private int idclass;
		private boolean activated;
		private int level, xp, tier, levellimit, xplimit, tierlimit;
		private String descriptives = "";
		private List<Integer> idskills = new ArrayList<Integer>();
		private List<String> nameskills = new ArrayList<String>();
		private List<Integer> orders = new ArrayList<Integer>();
		@Override
		public int getId() { return OPENCLASS; }
		@Override
		public AbsCYMCommandAction clone() { return new OPENCLASS(); }
		@Override
		public void initSend(Player p) {
			QuestPlayer qp = QuestPlayer.get(p);
			StateCYMClass smc = qp.getCYMClass(idclass);
			if(smc != null && smc.getContainer().isShowPlayer()){
				StateSkillManager ssm = smc;
				activated = smc.isActivated();
				level = ssm.getLevel();
				xp = ssm.getXP();
				tier = ssm.getTier();
				levellimit = ssm.getLevelLimit();
				xplimit = ssm.getXPLimit();
				tierlimit = ssm.getTierLimit();
				descriptives = ssm.getDescriptives();
				for(StateCYMSkill sms : smc.getStateCYMSkills()){
					if(sms.getContainer().isShowPlayer()){
						idskills.add(sms.getId());
						nameskills.add(sms.getName());
						orders.add(sms.getOrder());
					}
				}
			}
		}
		@Override
		public void sendWrite() throws IOException {
			write(activated); write(level); write(xp); write(tier);
			write(levellimit); write(xplimit); write(tierlimit); write(descriptives);
			writeList(idskills, nameskills); writeListInt(orders);
		}
		@Override
		public void receiveRead() throws IOException { idclass = readInt(); }
		@Override
		public void receive(Player p) {
			if(p.hasPermission("cymquest.skill")) send(p, this);
		}
	}
	// ---- OPENSKILL ----
	class OPENSKILL extends AbsCYMCommandAction{
		private int idclass, idskill;
		private boolean activated;
		private int level, xp, tier, levellimit, xplimit, tierlimit;
		private String descriptives = "";
		@Override
		public int getId() { return OPENSKILL; }
		@Override
		public AbsCYMCommandAction clone() { return new OPENSKILL(); }
		@Override
		public void initSend(Player p) {
			QuestPlayer qp = QuestPlayer.get(p);
			StateCYMClass smc = qp.getCYMClass(idclass);
			if(smc != null){
				StateCYMSkill sms = smc.getStateCYMSkill(idskill);
				if(sms != null && sms.getContainer().isShowPlayer()){
					StateSkillManager ssm = sms;
					activated = sms.isActivated();
					level = ssm.getLevel();
					xp = ssm.getXP();
					tier = ssm.getTier();
					levellimit = ssm.getLevelLimit();
					xplimit = ssm.getXPLimit();
					tierlimit = ssm.getTierLimit();
					descriptives = ssm.getDescriptives();
				}
			}
		}
		@Override
		public void sendWrite() throws IOException {
			write(activated); write(level); write(xp); write(tier);
			write(levellimit); write(xplimit); write(tierlimit); write(descriptives);
		}
		@Override
		public void receiveRead() throws IOException { idclass = readInt(); idskill = readInt(); }
		@Override
		public void receive(Player p) {
			if(p.hasPermission("cymquest.skill")) send(p, this);
		}
	}
	// ---- ACTIVATE ----
	class ACTIVATE extends AbsCYMCommandAction{
		private int idclass, idskill;
		private boolean activated;
		@Override
		public int getId() { return ACTIVATE; }
		@Override
		public AbsCYMCommandAction clone() { return new ACTIVATE(); }
		@Override
		public void initSend(Player p) {
			QuestPlayer qp = QuestPlayer.get(p);
			StateCYMClass smc = qp.getCYMClass(idclass);
			if(smc != null){
				StateSkillManager ssm = smc;
				if(idskill != 0) ssm = smc.getStateCYMSkill(idskill);
				if(ssm != null && ssm.getSkillManager().isActivationPlayer()){
					if(ssm.isActivated()) ssm.deactivate(); else ssm.activate();
					activated = ssm.isActivated();
				}
			}
		}
		@Override
		public void sendWrite() throws IOException {
			write(activated);
		}
		@Override
		public void receiveRead() throws IOException { idclass = readInt(); idskill = readInt(); }
		@Override
		public void receive(Player p) {
			if(p.hasPermission("cymquest.skill")) send(p, this);
		}
	}
	// ---- INVENTORY ----
	class INVENTORY extends AbsCYMCommandAction{
		@Override
		public int getId() { return INVENTORY; }
		@Override
		public AbsCYMCommandAction clone() { return new INVENTORY(); }
		@Override
		public void initSend(Player p) { }
		@Override
		public void sendWrite() throws IOException { }
		@Override
		public void receiveRead() throws IOException { }
		@Override
		public void receive(Player p) {
			if(p.hasPermission("cymquest.skill")) SkillInventory.open(p);
		}
	}
	// ---- SKILLBAR ----
	class SKILLBAR extends AbsCYMCommandAction{
		private boolean skillBar;
		public SKILLBAR() { }
		public SKILLBAR(boolean skillBar) { this.skillBar = skillBar; }
		@Override
		public int getId() { return SKILLBAR; }
		@Override
		public AbsCYMCommandAction clone() { return new SKILLBAR(); }
		@Override
		public void initSend(Player p) { }
		@Override
		public void sendWrite() throws IOException { write(skillBar); }
		@Override
		public void receiveRead() throws IOException { skillBar = readBool(); }
		@Override
		public void receive(Player p) {
			if(p.hasPermission("cymquest.skill")) SkillInventory.changeBarMode(QuestPlayer.get(p), skillBar);
		}
	}
}