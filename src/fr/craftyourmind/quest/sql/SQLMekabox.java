package fr.craftyourmind.quest.sql;

import java.sql.SQLException;
import java.sql.Statement;

import fr.craftyourmind.quest.MekaBox;

public class SQLMekabox extends AbsQuestSQL{
	
	private MekaBox box;
	private String params = "";
	
	public SQLMekabox(int action, MekaBox box) {
		this.action = action;
		this.box = box;
		if(action != DELETE) params = box.getParamsCon();
	}
	@Override
	protected AbsQuestSQL initID() {
		box.setId(autoBoxID++); return this;
	}
	@Override
	protected void updateID() throws SQLException {
		autoBoxID = getId(T_MEKABOX);
	}
	@Override
	protected void create() throws SQLException {
		box.setId(getId(T_MEKABOX));
		String sql = updateRow.init(T_MEKABOX).add("id", box.getId()).add("name", box.getName()).add("type", box.getType()).add("idCat", box.getCatbox().getId()).add("param", params).add("listOrder", box.getOrder())
				.sqlInsertInto();
		create(T_MEKABOX, box.getName(), sql);
	}
	@Override
	protected void save() throws SQLException {
		Statement state = cnx.createStatement();
		String sql = updateRow.init(T_MEKABOX).add("name", box.getName()).add("type", box.getType()).add("idCat", box.getCatbox().getId()).add("param", params).add("listOrder", box.getOrder())
				.sqlWhere("id", box.getId()).sqlUpdate();
		state.executeUpdate(sql);
	}
	@Override
	protected void delete() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_MEKABOX).sqlWhere("id", box.getId()).sqlDelete());
	}
}