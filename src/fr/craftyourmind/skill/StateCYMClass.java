package fr.craftyourmind.skill;

import java.util.ArrayList;
import java.util.List;

import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.sql.QuestSQLManager;

public class StateCYMClass extends StateSkillManager{

	private CYMClass con;
	private List<StateCYMSkill> stateCYMSkills = new ArrayList<StateCYMSkill>();
	
	public StateCYMClass(CYMClass con, QuestPlayer qp) {
		super(con, qp);
		this.con = con;
		qp.addClass(this);
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
			int i = 0;
			for(CYMSkill ms : con.getMekaboxs()){
				if(ms.isActivated()){
					StateCYMSkill sms = null;
					for(StateCYMSkill s : stateCYMSkills){
						if(s.getId() == ms.getId()){ sms = s; break; }
					}
					if(sms == null){
						sms = ms.getStateOrCreate(qp);
						addCYMSkill(sms);
					}
					sms.activate();
					i++;
				}
			}
			return true;
		}else return false;
	}
	@Override
	public boolean deactivate(){
		if(!isActivated()) return false;
		super.deactivate();
		for(StateCYMSkill sms : stateCYMSkills)
			sms.deactivate();
		return true;
	}
	
	@Override
	public void clean() {
		super.clean();
		qp.removeClass(this);
		stateCYMSkills.clear();
	}
	
	@Override
	public void erase() {
		for(StateCYMSkill sms : stateCYMSkills.toArray(new StateCYMSkill[0])) sms.erase();
		super.erase();
	}
	
	@Override
	protected void setLevelOnly(int level) {
		super.setLevelOnly(level);
		for(StateCYMSkill sms : stateCYMSkills){
			if(sms.getContainer().levelClassActivated > getLevel()) sms.deactivate();
			else sms.activate();
		}
	}
	
	public CYMClass getContainer(){ return con; }
	
	public List<StateCYMSkill> getStateCYMSkills(){
		return stateCYMSkills;
	}
	public void addCYMSkill(StateCYMSkill sms){ if(!stateCYMSkills.contains(sms)) stateCYMSkills.add(sms); sms.smc = this; }
	public void removeCYMSkill(StateCYMSkill sms){ stateCYMSkills.remove(sms); }
	public StateCYMSkill getStateCYMSkill(int idskill){
		for(StateCYMSkill sms : stateCYMSkills) if(sms.getId() == idskill) return sms;
		return null;
	}
	
	public boolean canActivateSkill(StateCYMSkill sms) {
		if(sms.getContainer().levelClassActivated > getLevel()) return false;
		if(con.limitSkill == 0) return true;
		int i = 0;
		for(StateCYMSkill s : stateCYMSkills){
			if(s.isActivated()) i++;
			if(i >= con.limitSkill) return false;
		}
		return true;
	}
	
	public void updateAll() {
		cloneData();
		updateLevel();
		updateLinks();
		updateNodes();
		checkLinks();
	}
}