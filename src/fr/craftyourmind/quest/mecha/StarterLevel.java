package fr.craftyourmind.quest.mecha;

import fr.craftyourmind.skill.ISkillManager;
import fr.craftyourmind.skill.SkillManager;

public class StarterLevel extends Mechanism{

	private static final int EACHLEVEL = 0;
	
	private int type, idskill, idparent;
	private int action;
	private ISkillManager con;
	
	@Override
	public void init() {
		if(con != null){
			if(action == EACHLEVEL) con.setStarterEachLevel(this);
		}
	}
	
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) {
		return new StateLevel(this, mc, driver);
	}
	@Override
	public int getType() {
		return MechaType.STALEVEL;
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
	// ------------------ StateLevel ------------------
	class StateLevel extends AbsMechaStateEntity{

		public StateLevel(Mechanism m, MechaControler mc, IMechaDriver driver) {
			super(m, mc, driver);
		}
		@Override
		public void start() {
			launchMessage();
		}
	}
}
