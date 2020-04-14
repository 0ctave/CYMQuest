package fr.craftyourmind.quest.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import fr.craftyourmind.quest.Quest;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;
import fr.craftyourmind.quest.QuestPlayer;

public class SQLQuest extends AbsQuestSQL{
	
	protected static int autoincrement;
	
	private Quest q;
	private QuestPlayer qp;
	private StateQuestPlayer sqp;
	private String params = "";
	
	public SQLQuest() {}
	public SQLQuest(int action, Quest q) {
		this.action = action;
		this.q = q;
		if(action != DELETE) params = q.getParamsCon();
	}
	public SQLQuest(int action, Quest q, QuestPlayer qp) {
		this.action = action;
		this.q = q;
		this.qp = qp;
	}
	public SQLQuest(int action, Quest q, QuestPlayer qp, StateQuestPlayer sqp) {
		this.action = action;
		this.q = q;
		this.qp = qp;
		this.sqp = sqp;
		if(action == ACCEPT || action == UPDATESTATE) params = sqp.getParams();
	}
	@Override
	protected void runNext() throws SQLException {
		if(action == ACCEPT) accept();
		else if(action == DECLINE) decline();
		else if(action == TERMINATE) terminate();
		else if(action == REPEAT) repeat();
		else if(action == QUESTCHILD) questchild();
		else if(action == UPDATESTATE) updateParams();
	}
	@Override
	protected AbsQuestSQL initID() {
		q.id = autoQuestID++; return this;
	}
	@Override
	protected void updateID() throws SQLException {
		autoQuestID = getId(T_QUEST);
	}
	@Override
	protected void create() throws SQLException{
		q.id = getId(T_QUEST);
		String sql = updateRow.init(T_QUEST).add("id", q.id).add("idNPC", q.npc).add("idRepute", ((q.repute == null)?0:q.repute.id)).add("idEvent", ((q.getEvent() == null)?0:q.getEvent().id))
				.add("parent", ((q.parent == null)?0:q.parent.id)).add("idClass", q.cymClass).add("repeatable", q.repeatable).add("repeateTimeAccept", q.repeateTimeAccept).add("repeateTimeGive", q.repeateTimeGive).add("repeateTime", q.getRepeateTime()).add("levelMin", q.levelMin).add("levelMax", q.levelMax)
				.add("reputeMin", q.reputeMin).add("reputeMax", q.reputeMax).add("objInTheOrder", q.objInTheOrder).add("common", q.common).add("playersMax", q.playersMax).add("title", q.title.getStr())
				.add("introTxt", q.introTxt).add("fullTxt", q.fullTxt).add("successTxt", q.successTxt).add("loseTxt", q.loseTxt).add("displayIconNPC", q.displayIconNPC).add("params", params)
				.add("icons", q.strIcons).add("nameParamClass", q.nameParamClass).add("classMin", q.classMin).add("classMax", q.classMax).sqlInsertInto();
		create(T_QUEST, q.title.getStr(), sql);
	}
	@Override
	protected void save() throws SQLException{
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_QUEST).add("idNPC", q.npc).add("idRepute", ((q.repute == null)?0:q.repute.id)).add("idEvent", ((q.getEvent() == null)?0:q.getEvent().id))
		.add("parent", ((q.parent == null)?0:q.parent.id)).add("idClass", q.cymClass).add("repeatable", q.repeatable).add("repeateTimeAccept", q.repeateTimeAccept).add("repeateTimeGive", q.repeateTimeGive).add("repeateTime", q.getRepeateTime()).add("levelMin", q.levelMin).add("levelMax", q.levelMax)
		.add("reputeMin", q.reputeMin).add("reputeMax", q.reputeMax).add("objInTheOrder", q.objInTheOrder).add("common", q.common).add("playersMax", q.playersMax).add("title", q.title.getStr())
		.add("introTxt", q.introTxt).add("fullTxt", q.fullTxt).add("successTxt", q.successTxt).add("loseTxt", q.loseTxt).add("displayIconNPC", q.displayIconNPC).add("params", params)
		.add("icons", q.strIcons).add("nameParamClass", q.nameParamClass).add("classMin", q.classMin).add("classMax", q.classMax).sqlWhere("id", q.id).sqlUpdate());
	}
	@Override
	protected void delete() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_QUEST).sqlWhere("id", q.id).sqlDelete());
		totalDelete();
	}

	private void accept() throws SQLException{
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_QSTATEPLAYER).add("idPlayer", qp.getId()).add("idQuest", q.id).add("beginning", true).add("terminate", false).add("begintime", System.currentTimeMillis())
				.add("questchild", 0).add("params", params).sqlInsertInto());
	}

	private void terminate() throws SQLException{
		checkState();
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_QSTATEPLAYER).add("beginning", false).add("terminate", true).sqlWhere("idPlayer", qp.getId()).sqlWhere("idQuest", q.id).sqlUpdate());
	}	
	
	private void questchild() throws SQLException{
		if(checkState())
			terminate();
		
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_QSTATEPLAYER).add("questchild", sqp.idQuestChild).sqlWhere("idPlayer", qp.getId()).sqlWhere("idQuest", q.id).sqlUpdate());
	}

	private void decline() throws SQLException{
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_QSTATEPLAYER).sqlWhere("idPlayer", qp.getId()).sqlWhere("idQuest", q.id).sqlDelete());
	}
	
	private boolean checkState() throws SQLException{
		Statement state = cnx.createStatement();
		ResultSet rs = state.executeQuery("SELECT * FROM "+AbsQuestSQL.T_QSTATEPLAYER+" WHERE idQuest = "+q.id + " AND `idPlayer` = "+qp.getId()+";");
		if(!rs.next()){
			accept();
			return true;
		}
		return false;
	}
	
	private void updateParams() throws SQLException{
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_QSTATEPLAYER).add("params", params).sqlWhere("idPlayer", qp.getId()).sqlWhere("idQuest", q.id).sqlUpdate());
	}	
	
	private void repeat() throws SQLException{
		decline();
	}
	
	private void totalDelete() throws SQLException{ 
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_QSTATEPLAYER).sqlWhere("idQuest", q.id).sqlDelete());
	}
}