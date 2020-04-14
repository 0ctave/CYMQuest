package fr.craftyourmind.quest.sql;

import java.sql.SQLException;
import java.sql.Statement;

import fr.craftyourmind.quest.Quest;
import fr.craftyourmind.quest.QuestTag;

public class SQLQuestTag extends AbsQuestSQL{

	private QuestTag qt;
	private Quest q;
	
	
	public SQLQuestTag(int action, QuestTag qt) {
		this.action = action;
		this.qt = qt;
	}
	
	public SQLQuestTag(int action, QuestTag qt, Quest q) {
		this.action = action;
		this.qt = qt;
		this.q = q;
	}
	
	@Override
	protected AbsQuestSQL initID() {
		qt.id = autoTagID++; return this;
	}

	@Override
	protected void updateID() throws SQLException {
		autoTagID = getId(T_TAG);
	}

	@Override
	protected void runNext() throws SQLException {
		if(action == CREATESTATE) createState();
		else if(action == DELETESTATE) deleteState();
	}
	
	@Override
	protected void create() throws SQLException {
		qt.id = getId(T_TAG);
		String sql = updateRow.init(T_TAG).add("id", qt.id).add("name", qt.name).add("hidden", qt.hidden).sqlInsertInto();
		create(T_TAG, qt.name, sql);
	}

	@Override
	protected void save() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_TAG).add("name", qt.name).add("hidden", qt.hidden).sqlWhere("id", qt.id).sqlUpdate());
	}

	@Override
	protected void delete() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_TAG).sqlWhere("id", qt.id).sqlDelete());
		totalDelete();
	}

	private void createState() throws SQLException{
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_STATETAG).add("idTag", qt.id).add("idQuest", q.id).sqlInsertInto());
	}
	
	private void deleteState() throws SQLException{
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_STATETAG).sqlWhere("idTag", qt.id).sqlWhere("idQuest", q.id).sqlDelete());
	}
	
	private void totalDelete() throws SQLException{ 
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_STATETAG).sqlWhere("idTag", qt.id).sqlDelete());
	}
}
