package fr.craftyourmind.quest;

import org.bukkit.Material;
import fr.craftyourmind.manager.CYMReputation;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class RewardRepute extends AbsReward{

	public CYMReputation repute;
	public int param = 0;
	public boolean initialize = false;
	public boolean add = true;
	public boolean clanPlayer = false;
	
	public RewardRepute(Quest q) {
		super(q);
		idItem = Material.BOOK.getKey().toString();
	}
	@Override
	public IStateRew getState(StateQuestPlayer sqp) { return new StateRepute(sqp); }
	@Override
	public int getType() { return REPUTATION; }
	@Override
	public String getStrType() { return STRREPUTATION; }
	@Override
	public String getParams() {
		return new StringBuilder().append(getIdRepute()).append(DELIMITER).append(param).append(DELIMITER).append(initialize).append(DELIMITER).append(add).append(DELIMITER).append(clanPlayer).toString();
	}
	@Override
	public String getParamsGUI() {
		StringBuilder params = new StringBuilder().append(CYMReputation.get().size()).append(DELIMITER);
		for(CYMReputation rep : CYMReputation.get())
			params.append(rep.id).append(DELIMITER).append(rep.name).append(DELIMITER);
		return params.toString();
	}

	public int getIdRepute(){ return repute == null ? 0 : repute.id; }
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 2){
			repute = CYMReputation.getById(Integer.valueOf(params[0]));
			param = Integer.valueOf(params[1]);
			sqlSave();
		}else if(params.length == 5){
			repute = CYMReputation.getById(Integer.valueOf(params[0]));
			param = Integer.valueOf(params[1]);
			initialize = Boolean.valueOf(params[2]);
			add = Boolean.valueOf(params[3]);
			clanPlayer = Boolean.valueOf(params[4]);
		}
	}
	// ---------------- STATEREPUTE ----------------
	class StateRepute extends StateRewPlayer{

		public StateRepute(StateQuestPlayer sqp) { super(sqp); }
		@Override
		public void give() {
			if(sqp.qp.getCYMPlayer() == null) return;
			if(initialize){
				if(clanPlayer){ if(sqp.qp.hasClan()) sqp.qp.getClan().initRepute(repute, amount, param);}
				else sqp.qp.initRepute(repute, amount, param);
			}else if(add){
				if(clanPlayer){ if(sqp.qp.hasClan()) sqp.qp.getClan().addRepute(repute, amount, param);}
				else sqp.qp.addRepute(repute, amount, param);
			}
			messageGive(sqp.qp);
		}
	}
}