package fr.craftyourmind.quest.sql;

import java.sql.SQLException;
import java.sql.Statement;

import fr.craftyourmind.quest.AbsObjective;
import fr.craftyourmind.quest.QuestPlayer;

public class SQLObjective extends AbsQuestSQL{

	protected AbsObjective obj;
	protected QuestPlayer qP;
	private String params = "";
	
	public SQLObjective(int action, AbsObjective obj){
		this.action = action;
		this.obj = obj;
		if(action != DELETE) params = obj.getParams();
	}
	
	public SQLObjective(int action, AbsObjective obj, QuestPlayer qP) {
		this(action, obj);
		this.qP = qP;
	}
	@Override
	protected void runNext() throws SQLException {
		if(action == TERMINATE)terminate();
		else if(action == CLEAN)clean();
	}
	@Override
	protected AbsQuestSQL initID() {
		obj.id = autoObjectiveID++; return this;
	}
	@Override
	protected void updateID() throws SQLException {
		autoObjectiveID = getId(T_OBJ);
	}
	@Override
	protected void create()  throws SQLException{
		obj.id = getId(T_OBJ);		
		String sql = updateRow.init(T_OBJ).add("id", obj.id).add("idQuest", obj.q.id).add("type", obj.getType()).add("descriptive", obj.descriptive).add("success", obj.success).add("finishQuest", obj.isFinishQuest()).add("params", params).sqlInsertInto();
		create(T_OBJ, obj.descriptive, sql);
	}
	@Override
	protected void save() throws SQLException{
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_OBJ).add("descriptive", obj.descriptive).add("success", obj.success).add("finishQuest", obj.isFinishQuest()).add("params", params).sqlWhere("id", obj.id).sqlUpdate());
	}
	@Override
	protected void delete() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_OBJ).sqlWhere("id", obj.id).sqlDelete());
		totalClean();
	}
	
	protected void terminate() throws SQLException { 
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_OBJTERM).add("idPlayer", qP.getId()).add("idObjective", obj.id).sqlInsertIgnoreInto());
	}
	
	protected void clean() throws SQLException { 
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_OBJTERM).sqlWhere("idPlayer", qP.getId()).sqlWhere("idObjective", obj.id).sqlDelete());
	}
	
	protected void totalClean() throws SQLException { 
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_OBJTERM).sqlWhere("idObjective", obj.id).sqlDelete());
	}
}