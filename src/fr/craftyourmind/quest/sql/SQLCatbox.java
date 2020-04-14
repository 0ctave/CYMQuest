package fr.craftyourmind.quest.sql;

import java.sql.SQLException;
import java.sql.Statement;

import fr.craftyourmind.quest.CatBox;

public class SQLCatbox extends AbsQuestSQL{
	
	private CatBox cat;
	
	public SQLCatbox(int action, CatBox cat) {
		this.action = action;
		this.cat = cat;
	}
	@Override
	protected AbsQuestSQL initID() {
		cat.setId(autoCatboxID++); return this;
	}
	@Override
	protected void updateID() throws SQLException {
		autoCatboxID = getId(T_CATBOX);
	}
	@Override
	protected void create() throws SQLException {
		cat.setId(getId(T_CATBOX));
		String sql = updateRow.init(T_CATBOX).add("id", cat.getId()).add("name", cat.getName()).add("type", cat.getType()).add("listOrder", cat.getOrder())
				.sqlInsertInto();
		create(T_CATBOX, cat.getName(), sql);
	}
	@Override
	protected void save() throws SQLException {
		Statement state = cnx.createStatement();
		String sql = updateRow.init(T_CATBOX).add("name", cat.getName()).add("type", cat.getType()).add("listOrder", cat.getOrder())
				.sqlWhere("id", cat.getId()).sqlUpdate();
		state.executeUpdate(sql);
	}
	@Override
	protected void delete() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_CATBOX).sqlWhere("id", cat.getId()).sqlDelete());
	}
}