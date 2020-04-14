package fr.craftyourmind.quest;

import java.util.ArrayList;
import java.util.List;

import fr.craftyourmind.quest.mecha.AbsMechaContainer;
import fr.craftyourmind.quest.mecha.AbsStateContainer;
import fr.craftyourmind.quest.mecha.MechaCat;
import fr.craftyourmind.quest.mecha.MechaParam;
import fr.craftyourmind.quest.mecha.MechaType;
import fr.craftyourmind.quest.mecha.Mechanism;
import fr.craftyourmind.quest.mecha.StarterBox;
import fr.craftyourmind.quest.mecha.ToolBox.TOOLMEKABOX;
import fr.craftyourmind.quest.sql.QuestSQLManager;

public class MekaBox<C extends ICatBox, S extends AbsStateContainer> extends AbsMechaContainer<S> implements IMekaBox<C>, IQuestSort{
	
	// --- TYPE ---
	public static final int BOX = 0;
	public static final int SKILLBOX = 1;
	
	private static List<IMekaBox> list = new ArrayList<IMekaBox>();
	public static void add(IMekaBox box){ list.add(box); }
	public static void remove(IMekaBox box){ list.remove(box); }
	
	private int id;
	protected MechaParam name = new MechaParam(true, "");
	private C cat;
	private int type = BOX;
	public QuestSort questSort;
	
	private StarterBox enterBox;
	private StarterBox exitBox;
	private List<TOOLMEKABOX> listTool = new ArrayList<TOOLMEKABOX>();
	
	public MekaBox(int type, String name) { setName(name); this.type = type; questSort = new QuestSort(this); }
	
	public static IMekaBox get(int idB) {
		for(IMekaBox box : list) if(box.getId() == idB) return box;
		return null;
	}
	
	public static IMekaBox get(int type, int idcat, int idBox) {
		ICatBox cat = CatBox.get(type, idcat);
		if(cat != null) return cat.getMekabox(idBox);
		return get(idBox);
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
	@Override
	public int getCatId(){ return cat.getId(); }
	public C getCatbox(){ return cat; }
	public void setCat(C cat){
		if(this.cat != null){
			if(this.cat.getId() == cat.getId()) return;
			questSort.deleteOrder();
			this.cat.removeMekabox(this);
			this.cat = cat;
			cat.addMekabox(this);
			questSort.createOrder(getSortList().size());
			save();
		}else{
			this.cat = cat;
			cat.addMekabox(this);
		}
	}
	public void setCat(int idCat) {
		if(this.cat != null && this.cat.getId() == idCat) return;
		C cat = (C) CatBox.get(getType(), idCat);
		if(cat != null) setCat(cat);
	}
	
	public void addEnter(StarterBox enterBox){ this.enterBox = enterBox; }

	public void addExit(StarterBox exitBox){ this.exitBox = exitBox; }
	
	public StarterBox getEnter(){ return enterBox; }

	public StarterBox getExit(){ return exitBox; }	
	
	public void create(){
		QuestSQLManager.create(this);
	}
	public void save(){
		QuestSQLManager.save(this);
	}
	public void delete(){
		clearMechas();
		QuestSQLManager.delete(this);
		cat.removeMekabox(this);
		remove(this);
		questSort.deleteOrder();
	}
	@Override
	public int getTypeContainer() { return MEKABOX; }
	@Override
	public void clearMechas(){
		for(TOOLMEKABOX tb : listTool.toArray(new TOOLMEKABOX[0])) tb.clearMechas();
		for(Mechanism m : getMechas()) if(m.getType() == MechaType.STAMEKABOX) ((StarterBox) m).save = true;
		super.clearMechas();
	}

	public static IMekaBox newMekaboxSql(int type, String name, int cat, int idbox){
		IMekaBox box = MekaBox.newMekabox(type, name, cat);
		box.setId(idbox);
		return box;
	}
	
	public static IMekaBox newMekaboxCmd(int type, String name, int cat){
		IMekaBox mbox = MekaBox.newMekabox(type, name, cat);
		if(mbox != null){
			mbox.create();
			mbox.initStarterCreate();
		}
		return mbox;
	}
	
	public static IMekaBox newMekabox(int type, String name, int cat) {
		IMekaBox box = null;
		if(type == BOX){
			box = new MekaBox(BOX, name);
			box.setCat(cat);
			list.add(box);
		}
		return box;
	}
	
	public void initStarterCreate() {
		initParam();
		createStarter();
	}
	protected void createStarter() {
		enterBox = (StarterBox) Mechanism.newMechanism(MechaType.STAMEKABOX, this, MechaCat.STARTER);
		enterBox.save = true;
		enterBox.setEnter(this);
		enterBox.sqlCreate();
		enterBox.save = false;
		exitBox = (StarterBox) Mechanism.newMechanism(MechaType.STAMEKABOX, this, MechaCat.STARTER);
		exitBox.save = true;
		exitBox.setExit(this);
		exitBox.sqlCreate();
		exitBox.save = false;
	}
	@Override
	public void init() {
		initParam();
		initStarterBox();
	}
	protected void initStarterBox(){
		for(Mechanism m : getMechas()) if(m.getType() == MechaType.STAMEKABOX) ((StarterBox) m).initStarter(this);
	}
	private void initParam(){ addParamSys("boxName", name, "Mekabox name."); }
	
	public void updateTool() {
		TOOLMEKABOX[] tmplist = listTool.toArray(new TOOLMEKABOX[0]);
		listTool.clear();
		for(TOOLMEKABOX tb : tmplist) tb.initMekaBox();
	}

	public void addTool(TOOLMEKABOX toolBox) { listTool.add(toolBox); }

	public void removeTool(TOOLMEKABOX toolBox) { listTool.remove(toolBox); }
	@Override
	public S newStateContainer(QuestPlayer qp) { return null; }
	@Override
	public QuestSort getSort() { return questSort; }
	@Override
	public int getIdSort() { return getId(); }
	@Override
	public int getOrder(){ return questSort.getOrder(); }
	@Override
	public void setOrder(int order){ questSort.setOrder(order); }
	@Override
	public List<? extends IQuestSort> getSortList() { return cat.getMekaboxs(); }
}