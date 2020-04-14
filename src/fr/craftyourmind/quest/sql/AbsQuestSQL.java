package fr.craftyourmind.quest.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import fr.craftyourmind.manager.sql.AbsSQLCYMCnx.AbsSQLUpdateRow;
import fr.craftyourmind.quest.Plugin;

public abstract class AbsQuestSQL {

	static final int CREATE = 0;
	static final int SAVE = 1;
	static final int DELETE = 2;
	static final int ACCEPT = 3;
	static final int DECLINE = 4;
	static final int TERMINATE = 5;
	static final int CLEAN = 6;
	static final int REPEAT = 9;
	static final int QUESTCHILD = 10;
	static final int LINK = 11;
	static final int DELLINK = 12;
	static final int START = 13;
	static final int STOP = 14;
	static final int CREATESTATE = 15;
	static final int UPDATESTATE = 16;
	static final int DELETESTATE = 17;
	
	static public String prefix;
	static public String T_QUEST;
	static public String T_QSTATEPLAYER;
	static public String T_OBJ;
	static public String T_OBJTERM;
	static public String T_REW;
	static public String T_EVENT;
	static public String T_MECHANISM;
	static public String T_MECHA;
	static public String T_STATEMECHA;
	static public String T_MEKABOX;
	static public String T_CATBOX;
	static public String T_SKILL;
	static public String T_STATESKILL;
	static public String T_CLASS;
	static public String T_STATECLASS;
	static public String T_LEVEL;
	static public String T_TIER;
	static public String T_TAG;
	static public String T_STATETAG;
	
	public static int autoQuestID;
	public static int autoObjectiveID;
	public static int autoRewardID;
	public static int autoEventID;
	public static int autoMechaID;
	public static int autoBoxID;
	public static int autoCatboxID;
	public static int autoSkillID;
	public static int autoClassID;
	public static int autoLevelID;
	public static int autoTierID;
	public static int autoTagID;
	
	protected AbsSQLUpdateRow updateRow;
	protected int action;
	protected int compteur = 0;
	protected Connection cnx;
	protected boolean errorCreate = false;
	
	public static void init(Connection cnx) throws SQLException{
		autoQuestID = getId(cnx, T_QUEST);
		autoObjectiveID = getId(cnx, T_OBJ);
		autoRewardID = getId(cnx, T_REW);
		autoEventID = getId(cnx, T_EVENT);
		autoMechaID = getId(cnx, T_MECHANISM);
		autoBoxID = getId(cnx, T_MEKABOX);
		autoCatboxID = getId(cnx, T_CATBOX);
		autoSkillID = getId(cnx, T_SKILL);
		autoClassID = getId(cnx, T_CLASS);
		autoLevelID = getId(cnx, T_LEVEL);
		autoTierID = getId(cnx, T_TIER);
	}
	
	protected void go(){ // MAJ Async
		QuestSQLManager.addQuery(this);
	}
	
	public void run() throws SQLException{
		try{
			this.updateRow = QuestSQLManager.cymcnx.getUpdateRow();
			if(action == CREATE){
				create();
				if(errorCreate) updateID(); 
			}else if(action == SAVE) save();
			else if(action == DELETE) delete();
			else runNext();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected void runNext() throws SQLException {}
	
	protected static int getId(Connection cnx, String table) throws SQLException{
		Statement state = cnx.createStatement();
		return QuestSQLManager.cymcnx.getAutoIncrement(state, table);
	}
	
	protected int getId(String table) throws SQLException{
		return getId(cnx, table);
	}
	
	protected void create(String table, String name, String sql) throws SQLException {
		Statement state = cnx.createStatement();
		try{
			state.executeUpdate(sql);
		} catch (SQLException e) {
			if(compteur < 10 && updateRow.isErrorPrimaryKey(e)){
				Plugin.log("Error id "+table+" "+name+" : "+compteur);
				compteur++;
				errorCreate = true;
				create();
			}else{
				Plugin.log("Error create "+table+" "+name+" : "+e.getErrorCode());
				compteur = 0;
				e.printStackTrace();
			}
		}
	}
	protected abstract AbsQuestSQL initID();
	protected abstract void updateID() throws SQLException;
	protected abstract void create()throws SQLException;
	protected abstract void save()throws SQLException;
	protected abstract void delete()throws SQLException;
}