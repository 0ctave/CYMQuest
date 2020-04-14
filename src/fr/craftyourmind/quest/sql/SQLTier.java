package fr.craftyourmind.quest.sql;

import java.sql.SQLException;
import java.sql.Statement;

import fr.craftyourmind.skill.CYMTier;

public class SQLTier extends AbsQuestSQL{

	private CYMTier tier;
	
	public SQLTier(int action, CYMTier tier) {
		this.action = action;
		this.tier = tier;
	}
	@Override
	protected AbsQuestSQL initID() {
		tier.setId(autoTierID++); return this;
	}
	@Override
	protected void updateID() throws SQLException {
		autoTierID = getId(T_TIER);
	}
	@Override
	protected void create() throws SQLException {
		tier.setId(getId(T_TIER));
		String sql = updateRow.init(T_TIER).add("id", tier.getId()).add("name", tier.getName()).add("limitLevel", tier.limit)
				.sqlInsertInto();
		create(T_TIER, tier.getName(), sql);
	}
	@Override
	protected void save() throws SQLException {
		Statement state = cnx.createStatement();
		String sql = updateRow.init(T_TIER).add("id", tier.getId()).add("name", tier.getName()).add("limitLevel", tier.limit)
				.sqlWhere("id", tier.getId()).sqlUpdate();
		state.executeUpdate(sql);
	}
	@Override
	protected void delete() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_TIER).sqlWhere("id", tier.getId()).sqlDelete());
	}
}