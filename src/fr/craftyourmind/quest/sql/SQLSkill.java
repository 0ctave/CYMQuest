package fr.craftyourmind.quest.sql;

import java.sql.SQLException;
import java.sql.Statement;

import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.skill.CYMSkill;
import fr.craftyourmind.skill.StateCYMSkill;

public class SQLSkill extends AbsQuestSQL{

	private CYMSkill ms;
	private String params = "", desc = "", linkChilds = "", levels = "", tiers = "", nodeChilds = "", stateparams = "";
	private QuestPlayer qp;
	private StateCYMSkill stateSkill;
	
	public SQLSkill(int action, CYMSkill ms) {
		this.action = action;
		this.ms = ms;
		if(action != DELETE){
			params = ms.getParamsCon();
			desc = ms.getDescriptives();
			linkChilds = ms.getLinkChildsStr();
			levels = ms.getLevels();
			tiers = ms.getTiers();
			nodeChilds = ms.getNodeChildsStr();
		}
	}
	public SQLSkill(int action, StateCYMSkill stateSkill, QuestPlayer qp) {
		this.action = action;
		this.stateSkill = stateSkill;
		this.qp = qp;
		stateparams = stateSkill.getParams();
	}
	@Override
	protected void runNext() throws SQLException {
		if(action == CREATESTATE) createState();
		else if(action == UPDATESTATE) updateState();
		else if(action == DELETESTATE) deleteState();
	}
	@Override
	protected AbsQuestSQL initID() {
		ms.setId(autoSkillID++); return this;
	}
	@Override
	protected void updateID() throws SQLException {
		autoSkillID = getId(T_SKILL);
	}
	@Override
	protected void create() throws SQLException {
		ms.setId(getId(T_SKILL));
		String sql = updateRow.init(T_SKILL).add("id", ms.getId()).add("idclass", ms.getCatId()).add("name", ms.getName()).add("levelClassActivated", ms.levelClassActivated).add("activate", ms.isActivated()).add("keepEnableOnLink", ms.isKeepEnableOnLink()).add("showPlayer", ms.isShowPlayer())
				.add("activationPlayer", ms.isActivationPlayer()).add("showMessage", ms.isShowMessage()).add("descriptives", desc).add("linkChilds", linkChilds).add("levels", levels).add("tiers", tiers)
				.add("nodeChilds", nodeChilds).add("limitNode", ms.getLimitNode()).add("syncNodeParents", ms.isSyncNodeParents()).add("params", params).add("listOrder", ms.getOrder())
				.sqlInsertInto();
		create(T_SKILL, ms.getName(), sql);
	}
	@Override
	protected void save() throws SQLException {
		Statement state = cnx.createStatement();
		String sql = updateRow.init(T_SKILL).add("idclass", ms.getCatId()).add("name", ms.getName()).add("levelClassActivated", ms.levelClassActivated).add("activate", ms.isActivated()).add("keepEnableOnLink", ms.isKeepEnableOnLink()).add("showPlayer", ms.isShowPlayer())
				.add("activationPlayer", ms.isActivationPlayer()).add("showMessage", ms.isShowMessage()).add("descriptives", desc).add("linkChilds", linkChilds).add("levels", levels).add("tiers", tiers)
				.add("nodeChilds", nodeChilds).add("limitNode", ms.getLimitNode()).add("syncNodeParents", ms.isSyncNodeParents()).add("params", params).add("listOrder", ms.getOrder())
				.sqlWhere("id", ms.getId()).sqlUpdate();
		state.executeUpdate(sql);
	}
	@Override
	protected void delete() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_SKILL).sqlWhere("id", ms.getId()).sqlDelete());
		totalDelete();
	}
	
	private void createState() throws SQLException{
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_STATESKILL).add("idPlayer", qp.getId()).add("idSkill", stateSkill.getId()).add("activate", stateSkill.isActivated()).add("level", stateSkill.getLevel()).add("xp", stateSkill.getXP()).add("params", stateparams).sqlInsertInto());
	}
	
	private void updateState() throws SQLException{
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_STATESKILL).add("activate", stateSkill.isActivated()).add("level", stateSkill.getLevel()).add("xp", stateSkill.getXP()).add("params", stateparams).sqlWhere("idPlayer", qp.getId()).sqlWhere("idSkill", stateSkill.getId()).sqlUpdate());
	}
	
	private void deleteState() throws SQLException{
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_STATESKILL).sqlWhere("idPlayer", qp.getId()).sqlWhere("idSkill", stateSkill.getId()).sqlDelete());
	}
	
	private void totalDelete() throws SQLException{
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_STATESKILL).sqlWhere("idSkill", ms.getId()).sqlDelete());
	}
}