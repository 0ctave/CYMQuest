package fr.craftyourmind.quest.sql;

import java.sql.SQLException;
import java.sql.Statement;

import fr.craftyourmind.quest.mecha.AbsMechaStateEntity;
import fr.craftyourmind.quest.mecha.Mechanism;
import fr.craftyourmind.quest.mecha.Mechanism.ChildLink;

public class SQLMechanism extends AbsQuestSQL{

	public Mechanism m;
	public ChildLink child;
	public AbsMechaStateEntity smp;
	private String params = "";
	
	private int idPlayer, idMecha, typeDriver, idDriver;
	
	public SQLMechanism(int action, Mechanism m) {
		this.action = action;
		this.m = m;
		if(action != DELETE) params = m.getParams();
	}
	
	public SQLMechanism(int action, Mechanism m, ChildLink child) {
		this.action = action;
		this.m = m;
		this.child = child;
	}

	public SQLMechanism(int action, AbsMechaStateEntity smp) {
		this.action = action;
		this.smp = smp;
	}
	
	public SQLMechanism(int action, int idPlayer, int idMecha, int typeDriver, int idDriver) {
		this.action = action;
		this.idPlayer = idPlayer;
		this.idMecha = idMecha;
		this.typeDriver = typeDriver;
		this.idDriver = idDriver;
	}
	@Override
	protected void runNext() throws SQLException {
		if(action == LINK) link();
		else if(action == UPDATESTATE) updateLink();
		else if(action == DELLINK) delLink();
		else if(action == START) start();
		else if(action == STOP) stop();
	}	
	@Override
	protected AbsQuestSQL initID() {
		m.id = autoMechaID++; return this;
	}
	@Override
	protected void updateID() throws SQLException {
		autoMechaID = getId(T_MECHANISM);
	}
	@Override
	protected void create() throws SQLException {
		m.id = getId(T_MECHANISM);
		String sql = updateRow.init(T_MECHANISM).add("id", m.id).add("typeDriver", m.typeContainer).add("idDriver", m.idContainer).add("category", m.category).add("type", m.getType()).add("common", m.common)
				.add("permanent", m.permanent).add("single", m.single).add("name", m.name).add("message", m.message.toString()).add("params", params).sqlInsertInto();
		create(T_MECHANISM, m.name, sql);
	}
	@Override
	protected void save() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_MECHANISM).add("typeDriver", m.typeContainer).add("idDriver", m.idContainer).add("category", m.category).add("type", m.getType()).add("common", m.common)
				.add("permanent", m.permanent).add("single", m.single).add("name", m.name).add("message", m.message.toString()).add("params", params).sqlWhere("id", m.id).sqlUpdate());
	}
	@Override
	protected void delete() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_MECHANISM).sqlWhere("id", m.id).sqlDelete());
		totalDelete();
	}

	private void link() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_MECHA).add("idLauncher", m.id).add("idLaunch", child.getId()).add("slot", child.getSlot()).add("listOrder", child.getOrder()).sqlInsertInto());
	}
	
	private void updateLink() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_MECHA).add("slot", child.getSlot()).add("listOrder", child.getOrder()).sqlWhere("idLauncher", m.id).sqlWhere("idLaunch", child.getId()).sqlUpdate());
	}
	
	private void delLink() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_MECHA).sqlWhere("idLauncher", m.id).sqlWhere("idLaunch", child.getId()).sqlDelete());
	}
	
	private void start() throws SQLException {
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_STATEMECHA).add("idPlayer", smp.qp.getId()).add("idMecha", smp.getId()).add("typeDriver", smp.getMechanism().typeContainer).add("idDriver", smp.getMechanism().idContainer).sqlInsertIgnoreInto());
	}
	
	private void stop() throws SQLException {
		Statement state = cnx.createStatement();
		if(smp == null)
			state.executeUpdate(updateRow.init(T_STATEMECHA).sqlWhere("idPlayer", idPlayer).sqlWhere("idMecha", idMecha).sqlWhere("typeDriver", typeDriver).sqlWhere("idDriver", idDriver).sqlDelete());
		else
			state.executeUpdate(updateRow.init(T_STATEMECHA).sqlWhere("idPlayer", smp.qp.getId()).sqlWhere("idMecha", smp.getId()).sqlWhere("typeDriver", smp.getMechanism().typeContainer).sqlWhere("idDriver", smp.getMechanism().idContainer).sqlDelete());
	}
	
	private void totalDelete() throws SQLException{ 
		Statement state = cnx.createStatement();
		state.executeUpdate(updateRow.init(T_STATEMECHA).sqlWhere("idMecha", m.id).sqlDelete());
	}
}