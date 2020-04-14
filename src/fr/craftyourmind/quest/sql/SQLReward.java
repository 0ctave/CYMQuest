package fr.craftyourmind.quest.sql;

import java.sql.SQLException;
import java.sql.Statement;

import fr.craftyourmind.quest.AbsReward;

public class SQLReward extends AbsQuestSQL{

	protected String strType;
	protected AbsReward rew;
	private String params = "";
	
	public SQLReward(int action, AbsReward rew) {
		this.action = action;
		this.rew = rew;
		if(action != DELETE) params = rew.getParams();
	}
	@Override
	protected AbsQuestSQL initID() {
		rew.id = autoRewardID++; return this;
	}
	@Override
	protected void updateID() throws SQLException {
		autoRewardID = getId(T_REW);
	}
	@Override
	protected void create() throws SQLException {
		rew.id = getId(T_REW);		
		String sql = updateRow.init(T_REW).add("id", rew.id).add("idQuest", rew.q.id).add("type", rew.getType()).add("descriptive", rew.descriptive).add("amount", rew.amount).add("params", params).sqlInsertInto();
		create(T_REW, rew.descriptive, sql);
	}
	@Override
	protected void save() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_REW).add("descriptive", rew.descriptive).add("amount", rew.amount).add("params", params).sqlWhere("id", rew.id).sqlUpdate());
	}
	@Override
	protected void delete() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_REW).sqlWhere("id", rew.id).sqlDelete());
	}
}