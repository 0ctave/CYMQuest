package fr.craftyourmind.quest.mecha;

import java.util.HashMap;
import java.util.Map;

import fr.craftyourmind.skill.CYMClass;
import fr.craftyourmind.skill.CYMSkill;
import fr.craftyourmind.skill.StateCYMClass;
import fr.craftyourmind.skill.StateCYMSkill;
import fr.craftyourmind.skill.StateSkillManager;

public class ActionSkill extends AbsMechaList{

	private static final int XP = 1;
	private static final int LEVEL = 2;
	private static final int ACTIVATE = 3;
	private static final int INIT = 0;
	private static final int ADD = 1;
	private static final int EQUAL = 2;
	private static final int INFERIOR = 3;
	private static final int SUPERIOR = 4;
	private static Map<Integer, Class<? extends IMechaList>> params = new HashMap<Integer, Class<? extends IMechaList>>();
	static{
		params.put(XP, XP.class);
		params.put(LEVEL, LEVEL.class);
		params.put(ACTIVATE, ACTIVATE.class);
	}
	
	private int idskill, idclass;
	
	public ActionSkill() { }
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) {
		return new StateSkill(this, mc, driver);
	}
	@Override
	public int getType() { return MechaType.ACTSKILL; }
	@Override
	public Map<Integer, Class<? extends IMechaList>> getMechaParam() { return params; }
	@Override
	protected String getStringParams() {
		return 0+DELIMITER+idskill+DELIMITER+idclass;
	}
	@Override
	protected int loadParams(int index, String[] params) {
		int version = Integer.valueOf(params[index++]);
		idskill = Integer.valueOf(params[index++]);
		idclass = Integer.valueOf(params[index++]);
		return index;
	}
	@Override
	public String getParamsGUI() {
		StringBuilder sb = new StringBuilder().append(CYMClass.getCYMClass().size());
		for(CYMClass mc : CYMClass.getCYMClass()){
			sb.append(DELIMITER).append(mc.getId()).append(DELIMITER).append(mc.getName()).append(DELIMITER).append(mc.getMekaboxs().size());
			for(CYMSkill ms : mc.getMekaboxs())
				sb.append(DELIMITER).append(ms.getId()).append(DELIMITER).append(ms.getName());
		}
		return sb.toString();
	}
	// ------------------ StateSkill ------------------
	class StateSkill extends AbsMechaStateEntityList{

		public StateSkill(Mechanism m, MechaControler mc, IMechaDriver driver) {
			super(m, mc, driver);
		}
	}
	// -------------- XP --------------
	class XP implements IMechaList{
		private int action;
		private IntegerData xp = new IntegerData();
		@Override
		public int getId() { return XP; }
		@Override
		public String getParams() {
			return 0+DELIMITER+action+DELIMITER+xp;
		}
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			action = Integer.valueOf(params[index++]);
			xp.load(params[index++]);
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateSkill>() {
				private IntegerData xp = new IntegerData();
				private StateSkillManager ssm;
				@Override
				public void cloneData(StateSkill s) {
					xp.clone(s, XP.this.xp);
					ssm = null;
					StateCYMClass smc = s.qp.getCYMClass(idclass);
					if(smc != null){
						ssm = smc;
						if(idskill != 0){
							StateCYMSkill sms = smc.getStateCYMSkill(idskill);
							if(sms != null) ssm = sms;
						}
					}
				}
				@Override
				public void start(StateSkill s) {
					if(ssm != null){
						if(action == INIT){
							ssm.changeXp(xp.get());
							s.launchMessage();
						}else if(action == ADD){
							ssm.changeXp(ssm.getXP() + xp.get());
							s.launchMessage();
						}else if(action == EQUAL){
							if(ssm.getXP() == xp.get()) s.launchMessage();
						}else if(action == INFERIOR){
							if(ssm.getXP() < xp.get()) s.launchMessage();
						}else if(action == SUPERIOR){
							if(ssm.getXP() > xp.get()) s.launchMessage();
						}
					}
				}
				@Override
				public void stop(StateSkill s) { }
			};
		}
	}
	// -------------- LEVEL --------------
	class LEVEL implements IMechaList{
		private int action;
		private IntegerData level = new IntegerData(1);
		@Override
		public int getId() { return LEVEL; }
		@Override
		public String getParams() {
			return 0+DELIMITER+action+DELIMITER+level;
		}
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			action = Integer.valueOf(params[index++]);
			level.load(params[index++]);
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateSkill>() {
				private IntegerData level = new IntegerData();
				private StateSkillManager ssm;
				@Override
				public void cloneData(StateSkill s) {
					level.clone(s, LEVEL.this.level);
					StateCYMClass smc = s.qp.getCYMClass(idclass);
					if(smc != null){
						ssm = smc;
						if(idskill != 0){
							StateCYMSkill sms = smc.getStateCYMSkill(idskill);
							if(sms != null) ssm = sms;							
						}
					}
				}
				@Override
				public void start(StateSkill s) {
					if(ssm != null){
						if(action == INIT){
							ssm.changeLevel(level.get());
							s.launchMessage();
						}else if(action == ADD){
							ssm.changeLevel(ssm.getLevel() + level.get());
							s.launchMessage();
						}else if(action == EQUAL){
							if(ssm.getLevel() == level.get()) s.launchMessage();
						}else if(action == INFERIOR){
							if(ssm.getLevel() < level.get()) s.launchMessage();
						}else if(action == SUPERIOR){
							if(ssm.getLevel() > level.get()) s.launchMessage();
						}
					}
				}
				@Override
				public void stop(StateSkill s) { }
			};
		}
	}
	// -------------- ACTIVATE --------------
	class ACTIVATE implements IMechaList{
		private static final int ACTIVATE = 0;
		private static final int DEACTIVATE = 1;
		private static final int ISACTIVATE = 2;
		private int action;
		@Override
		public int getId() { return ActionSkill.ACTIVATE; }
		@Override
		public String getParams() {
			return 1+DELIMITER+action;
		}
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			if(version == 0){
				boolean activate = Boolean.valueOf(params[index++]);
				action = activate ? ACTIVATE : DEACTIVATE;
			}else if(version == 1)
				action = Integer.valueOf(params[index++]);
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateSkill>() {
				@Override
				public void cloneData(StateSkill s) { }
				@Override
				public void start(StateSkill s) {
					boolean found = false;
					StateCYMClass smc = s.qp.getCYMClass(idclass);
					if(smc != null){
						AbsStateContainer sc = smc;
						if(idskill != 0) sc = smc.getStateCYMSkill(idskill);
						if(sc != null){
							found = true;
							if(action == ACTIVATE){
								sc.activate();
								s.launchMessage();
							}else if(action == DEACTIVATE){
								sc.deactivate();
								s.launchMessage();
							}else if(action == ISACTIVATE && sc.isActivated()){
								s.launchMessage();
							}
						}
					}
					if(!found && action != ISACTIVATE){
						CYMClass mc = CYMClass.getCYMClass(idclass);
						if(mc != null){
							AbsMechaContainer amc = mc;
							if(idskill != 0) amc = mc.getMekabox(idskill);
							if(amc != null){
								if(action == ACTIVATE) amc.activate(s.qp); else if(action == DEACTIVATE) amc.deactivate(s.qp);
								s.launchMessage();
							}
						}
					}
				}
				@Override
				public void stop(StateSkill s) { }
			};
		}
	}
}