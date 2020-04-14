package fr.craftyourmind.quest.sql;

import java.sql.SQLException;
import java.sql.Statement;

import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.skill.CYMClass;
import fr.craftyourmind.skill.StateCYMClass;

public class SQLClass extends AbsQuestSQL{

	private CYMClass mc;
	private String params = "", desc = "", linkChilds = "", levels = "", tiers = "", nodeChilds = "", stateparams = "";
	private QuestPlayer qp;
	private StateCYMClass stateClass;
	
	public SQLClass(int action, CYMClass mc) {
		this.action = action;
		this.mc = mc;
		if(action != DELETE) {
			params = mc.getParamsCon();
			desc = mc.getDescriptives();
			linkChilds = mc.getLinkChildsStr();
			levels = mc.getLevels();
			tiers = mc.getTiers();
			nodeChilds = mc.getNodeChildsStr();
		}
	}
	public SQLClass(int action, StateCYMClass stateClass, QuestPlayer qp) {
		this.action = action;
		this.qp = qp;
		this.stateClass = stateClass;
		stateparams = stateClass.getParams();
	}
	@Override
	protected void runNext() throws SQLException {
		if(action == CREATESTATE) createState();
		else if(action == UPDATESTATE) updateState();
		else if(action == DELETESTATE) deleteState();
	}
	@Override
	protected AbsQuestSQL initID() {
		mc.setId(autoClassID++); return this;
	}
	@Override
	protected void updateID() throws SQLException {
		autoClassID = getId(T_CLASS);
	}
	@Override
	protected void create() throws SQLException {
		mc.setId(getId(T_CLASS));
		String sql = updateRow.init(T_CLASS).add("id", mc.getId()).add("name", mc.getName()).add("limitSkill", mc.limitSkill).add("activate", mc.isActivated()).add("keepEnableOnLink", mc.isKeepEnableOnLink()).add("showPlayer", mc.isShowPlayer())
				.add("activationPlayer", mc.isActivationPlayer()).add("showMessage", mc.isShowMessage()).add("descriptives", desc).add("linkChilds", linkChilds).add("levels", levels).add("tiers", tiers)
				.add("nodeChilds", nodeChilds).add("limitNode", mc.getLimitNode()).add("syncNodeParents", mc.isSyncNodeParents()).add("params", params).add("listOrder", mc.getOrder())
				.sqlInsertInto();
		create(T_CLASS, mc.getName(), sql);
	}
	@Override
	protected void save() throws SQLException {
		Statement state = cnx.createStatement();
		String sql = updateRow.init(T_CLASS).add("name", mc.getName()).add("limitSkill", mc.limitSkill).add("activate", mc.isActivated()).add("keepEnableOnLink", mc.isKeepEnableOnLink()).add("showPlayer", mc.isShowPlayer())
				.add("activationPlayer", mc.isActivationPlayer()).add("showMessage", mc.isShowMessage()).add("descriptives", desc).add("linkChilds", linkChilds).add("levels", levels).add("tiers", tiers)
				.add("nodeChilds", nodeChilds).add("limitNode", mc.getLimitNode()).add("syncNodeParents", mc.isSyncNodeParents()).add("params", params).add("listOrder", mc.getOrder())
				.sqlWhere("id", mc.getId()).sqlUpdate();
		state.executeUpdate(sql);
	}
	@Override
	protected void delete() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_CLASS).sqlWhere("id", mc.getId()).sqlDelete());
		totalDelete();
	}
	
	private void createState() throws SQLException{
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_STATECLASS).add("idPlayer", qp.getId()).add("idClass", stateClass.getId()).add("activate", stateClass.isActivated()).add("level", stateClass.getLevel()).add("xp", stateClass.getXP()).add("params", stateparams).sqlInsertInto());
	}
	
	private void updateState() throws SQLException{
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_STATECLASS).add("activate", stateClass.isActivated()).add("level", stateClass.getLevel()).add("xp", stateClass.getXP()).add("params", stateparams).sqlWhere("idPlayer", qp.getId()).sqlWhere("idClass", stateClass.getId()).sqlUpdate());
	}
	
	private void deleteState() throws SQLException{
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_STATECLASS).sqlWhere("idPlayer", qp.getId()).sqlWhere("idClass", stateClass.getId()).sqlDelete());
	}
	
	private void totalDelete() throws SQLException{ 
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_STATECLASS).sqlWhere("idClass", mc.getId()).sqlDelete());
	}
}