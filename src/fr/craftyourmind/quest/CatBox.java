package fr.craftyourmind.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.craftyourmind.quest.mecha.AbsMechaContainer;
import fr.craftyourmind.quest.mecha.AbsStateContainer;
import fr.craftyourmind.quest.mecha.MechaParam;
import fr.craftyourmind.quest.sql.QuestSQLManager;

public class CatBox<M extends IMekaBox, S extends AbsStateContainer> extends AbsMechaContainer<S> implements ICatBox<M>{
	
	private static Map<Integer, List<ICatBox>> list = new HashMap<Integer, List<ICatBox>>();
	static{
		list.put(MekaBox.BOX, new ArrayList<ICatBox>());
		list.put(MekaBox.SKILLBOX, new ArrayList<ICatBox>());
	}
	public static Map<Integer, List<ICatBox>> get(){ return list; }
	public static List<ICatBox> get(int type){ return list.get(type); }
	public static ICatBox get(int type, int idCat) { for(ICatBox cat : get(type)) if(cat.getId() == idCat) return cat; return getCatDefault(type); }
	public static void add(int type, ICatBox cat){ list.get(type).add(cat); }
	public static void remove(int type, ICatBox cat){ list.get(type).remove(cat); }
	private static ICatBox CATBOXDEFAULT = newCatbox(MekaBox.BOX, "default");
	//static{ CATBOXDEFAULT.setOrder(1); }
	private int id;
	protected MechaParam name = new MechaParam(true, "");
	private int type;
	public QuestSort questSort;
	private List<M> mekaboxs = new ArrayList<M>();
	
	public CatBox(int type, String name) {
		this.type = type;
		this.name.setStrUnlock(name);
		questSort = new QuestSort(this);
	}
	@Override
	public int getId() { return id; }
	@Override
	public void setId(int id) { this.id = id; }
	@Override
	public int getType(){ return type; }
	@Override
	public String getName(){ return name.getStr(); }
	public void setName(String name){ this.name.setStrUnlock(name); }
	
	public void addMekabox(M box){ mekaboxs.add(box); }
	public void removeMekabox(M box){ mekaboxs.remove(box); }
	public List<M> getMekaboxs(){ return mekaboxs; }
	public M getMekabox(int id){ for(M mb : mekaboxs) if(mb.getId() == id) return mb; return null; }
	
	public void create(){
		QuestSQLManager.create(this);
	}
	public void save(){
		QuestSQLManager.save(this);
	}
	public void delete(){
		if(this == CATBOXDEFAULT) return;
		questSort.deleteOrder();
		QuestSQLManager.delete(this);
		for(IMekaBox mb : mekaboxs.toArray(new IMekaBox[0])){ mb.setCat(CATBOXDEFAULT); mb.save(); }
		mekaboxs.clear();
		remove(getType(), this);
	}

	public static ICatBox getCatDefault(int type){ return get(type).get(0); }
	
	public static ICatBox newCatboxSql(int type, int idcat, String name) {
		ICatBox cat = newCatbox(type, name);
		if(cat != null) cat.setId(idcat);
		return cat;
	}
	public static ICatBox newCatboxCmd(int type, String name) {
		ICatBox cat = newCatbox(type, name);
		if(cat != null) cat.create();
		return cat;
	}
	public static ICatBox newCatbox(int type, String name) {
		ICatBox cat = null;
		if(type == MekaBox.BOX){
			cat = new CatBox(type, name);
			add(type, cat);
		}
		return cat;
	}
	@Override
	public int getTypeContainer() { return -1; }
	@Override
	public void init() { }
	@Override
	public QuestSort getSort() { return questSort; }
	@Override
	public int getIdSort() { return getId(); }
	@Override
	public int getOrder(){ return questSort.getOrder(); }
	@Override
	public void setOrder(int order){ questSort.setOrder(order); }
	@Override
	public List<? extends IQuestSort> getSortList() { return list.get(type); }
}