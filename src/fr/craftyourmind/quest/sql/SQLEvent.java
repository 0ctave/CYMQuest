package fr.craftyourmind.quest.sql;

import java.sql.SQLException;
import java.sql.Statement;

import fr.craftyourmind.quest.EventParadise;

public class SQLEvent extends AbsQuestSQL{

	private EventParadise event;
	private String params = "";
	
	public SQLEvent(int action, EventParadise event) {
		this.action = action;
		this.event = event;
		if(action != DELETE) params = event.getParamsCon();
	}
	@Override
	protected AbsQuestSQL initID() {
		event.id = autoEventID++; return this;
	}
	@Override
	protected void updateID() throws SQLException {
		autoEventID = getId(T_EVENT);
	}
	@Override
	protected void create() throws SQLException {
		event.id = getId(T_EVENT);
		String sql = updateRow.init(T_EVENT).add("id", event.id).add("type", event.getType()).add("name", event.name.getStr()).add("flow", ((event.flow == 0)?false:true)).add("preparation", event.preparation).add("firstFinish", event.firstFinish)
				.add("openQuest", event.openQuest).add("showObjCompleted", event.showObjCompleted).add("startTimer", event.noticeTimer).add("startMessage", event.noticeMessage).add("beginMessage", event.beginMessage).add("generalTimer", event.generalTimer)
				.add("nextEventTimer", event.nextEventTimer).add("world", event.getWorldName()).add("x", event.x).add("z", event.z).add("radius", event.radius).add("params", params).add("minPlayers", event.minPlayers)
				.add("maxPlayers", event.maxPlayers).add("nextEventWin", event.nextEventWin).add("nextEventLose", event.nextEventLose).add("autoAccept", event.autoAccept).add("isStarted", ((event.isEphemeral)?false:event.isStarted())).add("slot", event.getSlot())
				.sqlInsertInto();
		create(T_EVENT, event.name.getStr(), sql);
	}
	@Override
	protected void save() throws SQLException {
		Statement state = cnx.createStatement();
		String sql = updateRow.init(T_EVENT).add("name", event.name.getStr()).add("flow", ((event.flow == 0)?false:true)).add("preparation", event.preparation).add("firstFinish", event.firstFinish)
				.add("openQuest", event.openQuest).add("showObjCompleted", event.showObjCompleted).add("startTimer", event.noticeTimer).add("startMessage", event.noticeMessage).add("beginMessage", event.beginMessage).add("generalTimer", event.generalTimer)
				.add("nextEventTimer", event.nextEventTimer).add("world", event.getWorldName()).add("x", event.x).add("z", event.z).add("radius", event.radius).add("params", params).add("minPlayers", event.minPlayers)
				.add("maxPlayers", event.maxPlayers).add("nextEventWin", event.nextEventWin).add("nextEventLose", event.nextEventLose).add("autoAccept", event.autoAccept).add("isStarted", ((event.isEphemeral)?false:event.isStarted())).add("slot", event.getSlot())
				.sqlWhere("id", event.id).sqlUpdate();
		state.executeUpdate(sql);
	}
	@Override
	protected void delete() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_EVENT).sqlWhere("id", event.id).sqlDelete());
	}
}