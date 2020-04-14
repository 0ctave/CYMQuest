package fr.craftyourmind.quest.mecha;

import fr.craftyourmind.skill.ISkillManager;
import fr.craftyourmind.skill.SkillManager;

public class StarterSkill extends Mechanism{

	private static final int ACTIVATE = 0;
	private static final int DEACTIVATE = 1;
	
	private int type, idskill, idparent;
	private int action;
	private ISkillManager con;
	
	@Override
	public void init() {
		if(con != null){
			if(con.getStarterActivate() != null && con.getStarterActivate().id == id) con.setStarterActivate(null);
			if(con.getStarterDeactivate() != null && con.getStarterDeactivate().id == id) con.setStarterDeactivate(null);
			if(action == ACTIVATE) con.setStarterActivate(this);
			else if(action == DEACTIVATE) con.setStarterDeactivate(this);
		}
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) {
		return new StateSkill(this, mc, driver);
	}
	@Override
	public int getType() {
		return MechaType.STASKILL;
	}
	@Override
	public String getParams() {
		return 0+DELIMITER+type+DELIMITER+idskill+DELIMITER+idparent+DELIMITER+action;
	}
	@Override
	public String getParamsGUI() {
		return "";
	}
	@Override
	protected void loadParams(String[] params) {
		int version = Integer.valueOf(params[0]);
		type = Integer.valueOf(params[1]);
		idskill = Integer.valueOf(params[2]);
		idparent = Integer.valueOf(params[3]);
		action = Integer.valueOf(params[4]);
		con = SkillManager.get(type, idskill, idparent);
	}
	// ------------------ StateSkill ------------------
	class StateSkill extends AbsMechaStateEntity{

		public StateSkill(Mechanism m, MechaControler mc, IMechaDriver driver) {
			super(m, mc, driver);
		}
		@Override
		public void start() {
			launchMessage();
		}
	}
}