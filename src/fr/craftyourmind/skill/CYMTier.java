package fr.craftyourmind.skill;

import java.util.ArrayList;
import java.util.List;

import fr.craftyourmind.quest.mecha.MechaParam;
import fr.craftyourmind.quest.sql.QuestSQLManager;

public class CYMTier implements Comparable<CYMTier>{

	private static List<CYMTier> tiers = new ArrayList<CYMTier>();
	
	public static List<CYMTier> get() { return tiers; }
	public static CYMTier get(int id){
		for(CYMTier mt : tiers) if(mt.getId() == id) return mt ; return null;
	}
	
	public static CYMTier newCYMTierSql() {
		CYMTier mt = new CYMTier();
		tiers.add(mt);
		return mt;
	}
	public static CYMTier newCYMTierCmd() {
		CYMTier mt = new CYMTier();
		tiers.add(mt);
		mt.create();
		return mt;
	}
	
	private int id;
	private MechaParam name = new MechaParam(true, "");
	public int limit;
	public List<ISkillManager> uses = new ArrayList<ISkillManager>();
	
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public String getName(){ return name.getStr(); }
	public void setName(String name){ this.name.setStrUnlock(name); }	
	
	public void create() { QuestSQLManager.create(this); }
	
	public void save() { QuestSQLManager.save(this); }
	
	public void delete() { QuestSQLManager.delete(this); }
	@Override
	public int compareTo(CYMTier o) {
		return limit > o.limit ? 1 : -1;
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