package fr.craftyourmind.quest.sql;

import java.sql.SQLException;
import java.sql.Statement;

import fr.craftyourmind.skill.CYMLevel;

public class SQLLevel extends AbsQuestSQL{

	private CYMLevel level;
	
	public SQLLevel(int action, CYMLevel level) {
		this.action = action;
		this.level = level;
	}
	@Override
	protected AbsQuestSQL initID() {
		level.setId(autoLevelID++); return this;
	}
	@Override
	protected void updateID() throws SQLException {
		autoLevelID = getId(T_LEVEL);
	}
	@Override
	protected void create() throws SQLException {
		level.setId(getId(T_LEVEL));
		String sql = updateRow.init(T_LEVEL).add("id", level.getId()).add("name", level.getName()).add("lvlBegin", level.lvlBegin).add("lvlEnd", level.lvlEnd).add("baseXP", level.baseXP).add("coefMulti", level.coefMulti)
				.sqlInsertInto();
		create(T_LEVEL, level.getName(), sql);
	}
	@Override
	protected void save() throws SQLException {
		Statement state = cnx.createStatement();
		String sql = updateRow.init(T_LEVEL).add("name", level.getName()).add("lvlBegin", level.lvlBegin).add("lvlEnd", level.lvlEnd).add("baseXP", level.baseXP).add("coefMulti", level.coefMulti)
				.sqlWhere("id", level.getId()).sqlUpdate();
		state.executeUpdate(sql);
	}
	@Override
	protected void delete() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_LEVEL).sqlWhere("id", level.getId()).sqlDelete());
	}
}