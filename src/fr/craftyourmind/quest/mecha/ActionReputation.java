package fr.craftyourmind.quest.mecha;

import fr.craftyourmind.manager.CYMReputation;

public class ActionReputation extends Mechanism{

	public IntegerData idRepute = new IntegerData();
	public IntegerData points = new IntegerData();
	public IntegerData param = new IntegerData();
	public boolean initialize = false;
	public boolean add = false;
	public boolean superior = false;
	public boolean inferior = false;
	public boolean clanPlayer = false;

	@Override
	public int getType() { return MechaType.ACTREPUTATION; }
	@Override
	public String getParams() {
		return new StringBuilder(idRepute.toString()).append(DELIMITER).append(points).append(DELIMITER).append(param).append(DELIMITER).append(initialize)
				.append(DELIMITER).append(add).append(DELIMITER).append(superior).append(DELIMITER).append(inferior).append(DELIMITER).append(clanPlayer).toString();
	}
	@Override
	public String getParamsGUI() {
		StringBuilder paramgui = new StringBuilder().append(CYMReputation.get().size());
		for(CYMReputation rep : CYMReputation.get())
			paramgui.append(DELIMITER).append(rep.id).append(DELIMITER).append(rep.name);
		return paramgui.toString();
	}
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 7){
			idRepute.load(params[0]);
			points.load(params[1]);
			param.load(params[2]);
			initialize = Boolean.valueOf(params[3]);
			add = Boolean.valueOf(params[4]);
			superior = Boolean.valueOf(params[5]);
			inferior = Boolean.valueOf(params[6]);
			sqlSave();
		}else if(params.length == 8){
			idRepute.load(params[0]);
			points.load(params[1]);
			param.load(params[2]);
			initialize = Boolean.valueOf(params[3]);
			add = Boolean.valueOf(params[4]);
			superior = Boolean.valueOf(params[5]);
			inferior = Boolean.valueOf(params[6]);
			clanPlayer = Boolean.valueOf(params[7]);
		}
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateRepute(this, mc, driver); }
	// ------------------ StateRepute ------------------
	class StateRepute extends AbsMechaStateEntity{

		public CYMReputation repute;
		public IntegerData idRepute = new IntegerData();
		public IntegerData points = new IntegerData();
		public IntegerData param = new IntegerData();
		
		public StateRepute(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void cloneData() {
			super.cloneData();
			idRepute.clone(this, ActionReputation.this.idRepute);
			repute = CYMReputation.getById(idRepute.get());
			points.clone(this, ActionReputation.this.points);
			param.clone(this, ActionReputation.this.param);
		}
		@Override
		public void start() {
			if(!driver.isPlayer() || repute == null) return;
			
			if(initialize){
				if(clanPlayer){ if(driver.getQuestPlayer().hasClan()) driver.getQuestPlayer().getClan().initRepute(repute, points.get(), param.get());}
				else driver.getQuestPlayer().initRepute(repute, points.get(), param.get());
				launchMessage();
				
			}else if(add){
				if(clanPlayer){ if(driver.getQuestPlayer().hasClan()) driver.getQuestPlayer().getClan().addRepute(repute, points.get(), param.get());}
				else driver.getQuestPlayer().addRepute(repute, points.get(), param.get());
				launchMessage();
				
			}else if(superior){
				if(clanPlayer){
					if(driver.getQuestPlayer().hasClan() && driver.getQuestPlayer().getClan().getReputePts(repute) > points.get()) launchMessage();
				}else if(driver.getQuestPlayer().getReputePts(repute) > points.get()) launchMessage();
				
			}else if(inferior){
				if(clanPlayer){
					if(driver.getQuestPlayer().hasClan() && driver.getQuestPlayer().getClan().getReputePts(repute) < points.get()) launchMessage();
				}else if(driver.getQuestPlayer().getReputePts(repute) < points.get()) launchMessage();
			}
		}
	}
}