package fr.craftyourmind.skill;

import java.util.ArrayList;
import java.util.List;

import fr.craftyourmind.quest.sql.QuestSQLManager;

public class CYMLevel implements Comparable<CYMLevel>{

	private static List<CYMLevel> levels = new ArrayList<CYMLevel>();
	
	public static List<CYMLevel> get() { return levels; }
	public static CYMLevel get(int id){
		for(CYMLevel lvl : levels) if(lvl.getId() == id) return lvl ; return null;
	}
	
	public static CYMLevel newCYMLevelSql() {
		CYMLevel mt = new CYMLevel();
		levels.add(mt);
		return mt;
	}
	
	public static CYMLevel newCYMLevelCmd() {
		CYMLevel mt = new CYMLevel();
		levels.add(mt);
		mt.create();
		return mt;
	}
	
	private int id;
	private String name = "";
	public int lvlBegin, lvlEnd, baseXP;
	public float coefMulti = 1.2f;
	public List<ISkillManager> uses = new ArrayList<ISkillManager>();
	
	public void setId(int id) { this.id = id; }	
	public int getId() { return id; }
	
	public String getName(){ return name; }
	public void setName(String name){ this.name = name; }
	
	public void create() { QuestSQLManager.create(this); }
	
	public void save() { QuestSQLManager.save(this); }
	
	public void delete() { QuestSQLManager.delete(this); }
	@Override
	public int compareTo(CYMLevel o) {
		return lvlBegin > o.lvlBegin ? 1 : -1;
	}
	public int getBaseXp(int level) {
		if(level >= lvlEnd ) return 0;
		if(level == lvlBegin ) return baseXP;
		float result = (coefMulti * baseXP) - baseXP;
		return (int) (baseXP + (result * (level - lvlBegin)));
	}
	public void updateState() {
		for(ISkillManager sm : uses) sm.updateLevel();
	}
	public void add(ISkillManager sm) {
		if(!uses.contains(sm)) uses.add(sm);
	}
	public void remove(ISkillManager sm) {
		uses.remove(sm);
	}
}