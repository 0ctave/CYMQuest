package fr.craftyourmind.quest.mecha;

import fr.craftyourmind.skill.ISkillManager;
import fr.craftyourmind.skill.CYMTier;
import fr.craftyourmind.skill.SkillManager;

public class StarterTier extends Mechanism{

	private static final int EACHTIER = 0;
	private static final int SELECTTIER = 1;
	
	private int type, idskill, idparent;
	private int action;
	public int idtier;
	private ISkillManager con;
	
	@Override
	public void init() {
		if(con != null){
			if(con.getStarterEachTier() != null && con.getStarterEachTier().id == id) con.setStarterEachTier(null);
			con.setStarterSelectTier(this);
			if(action == EACHTIER) con.setStarterEachTier(this);
		}
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) {
		return new StateTier(this, mc, driver);
	}
	@Override
	public int getType() {
		return MechaType.STATIER;
	}
	@Override
	public String getParams() {
		return 0+DELIMITER+type+DELIMITER+idskill+DELIMITER+idparent+DELIMITER+action+DELIMITER+idtier;
	}
	@Override
	public String getParamsGUI() {
		StringBuilder params = new StringBuilder().append(con.getCYMTiers().size());
		for(CYMTier mt : con.getCYMTiers())
			params.append(DELIMITER).append(mt.getId()).append(DELIMITER).append(mt.getName());
		return params.toString();
	}
	@Override
	protected void loadParams(String[] params) {
		int version = Integer.valueOf(params[0]);
		type = Integer.valueOf(params[1]);
		idskill = Integer.valueOf(params[2]);
		idparent = Integer.valueOf(params[3]);
		action = Integer.valueOf(params[4]);
		idtier = Integer.valueOf(params[5]);
		con = SkillManager.get(type, idskill, idparent);
	}
	public int getIdTier(){ return idtier; }
	// ------------------ StateTier ------------------
	class StateTier extends AbsMechaStateEntity{

		public StateTier(Mechanism m, MechaControler mc, IMechaDriver driver) {
			super(m, mc, driver);
		}
		@Override
		public void start() {
			launchMessage();
		}
	}
}