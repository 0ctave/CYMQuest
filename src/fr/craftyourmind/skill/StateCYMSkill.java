package fr.craftyourmind.skill;

import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.sql.QuestSQLManager;

public class StateCYMSkill extends StateSkillManager{
	
	private CYMSkill con;
	protected StateCYMClass smc;
	
	public StateCYMSkill(CYMSkill con, QuestPlayer qp) {
		super(con, qp);
		this.con = con;
	}
	@Override
	public void createStateSql(){
		QuestSQLManager.createState(this, qp);
	}
	@Override
	public void updateStateSql() {
		QuestSQLManager.updateState(this, qp);
	}
	@Override
	public void deleteStateSql() {
		QuestSQLManager.deleteState(this, qp);
	}
	@Override
	public boolean activate() {
		if(super.activate()){
			con.getEnter().start(driver);
			return true;
		}else return false;
	}
	@Override
	public boolean deactivate(){
		if(!isActivated()) return false;
		super.deactivate();
		return true;
	}
	
	@Override
	public boolean canActivate() {
		if(!super.canActivate()) return false;
		if(smc == null){
			smc = con.getCatbox().getStateOrCreate(qp);
			smc.addCYMSkill(this);
		}
		return smc.canActivateSkill(this);
	}
	
	@Override
	public void clean() {
		super.clean();
		if(smc != null) smc.removeCYMSkill(this);
		smc = null;
	}
	
	public CYMSkill getContainer(){ return con; }
	
	public void updateAll() {
		cloneData();
		updateLevel();
		updateLinks();
		canActivate();
		updateNodes();
		checkLinks();
	}
}