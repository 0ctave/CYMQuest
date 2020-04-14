package fr.craftyourmind.quest.mecha;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import fr.craftyourmind.manager.command.ICYMCommandData;
import fr.craftyourmind.quest.IQuestSort;
import fr.craftyourmind.quest.Plugin;
import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.QuestSort;
import fr.craftyourmind.quest.QuestTools;
import fr.craftyourmind.quest.packet.DataQuestMecha;
import fr.craftyourmind.quest.sql.QuestSQLManager;

public abstract class Mechanism {

	public static final String DELIMITER = QuestTools.DELIMITER;

	private static Map<Integer, MechaType> types = new HashMap<Integer, MechaType>();
	
	private static final List<ICYMCommandData> cmdsData = Collections.synchronizedList(new ArrayList<ICYMCommandData>());
	
	public static void add(int category, int type, String name, Class<? extends Mechanism> cls){ // old, volcano
		MechaCat cat = MechaCat.get(category);
		if(cat != null) add(cat, type, name, cls);
		else Plugin.log("- cat null "+category+" "+type+" "+name);
	}
	
	public static void add(MechaCat category, int type, String name, Class<? extends Mechanism> cls){
		add(category, type, name, true, cls, AbsMechaContainer.QUEST, AbsMechaContainer.EVENT, AbsMechaContainer.MEKABOX, AbsMechaContainer.SKILL, AbsMechaContainer.CLASS);
	}
	
	public static void add(MechaCat category, int type, String name, boolean display, Class<? extends Mechanism> cls){
		add(category, type, name, display, cls, AbsMechaContainer.QUEST, AbsMechaContainer.EVENT, AbsMechaContainer.MEKABOX, AbsMechaContainer.SKILL, AbsMechaContainer.CLASS);
	}
	
	public static void add(MechaCat category, int type, String name, Class<? extends Mechanism> cls, Integer... typedriversPermit){
		add(category, type, name, true, cls, typedriversPermit);
	}
	
	public static void add(MechaCat category, int type, String name, boolean display, Class<? extends Mechanism> cls, Integer... typedriversPermit){
		MechaType mt = new MechaType(type, name, cls, display, category, typedriversPermit);
		category.add(mt);
		types.put(type, mt);
	}
	
	public static ICYMCommandData getCmdData(int id){
		synchronized (cmdsData) { for(ICYMCommandData cmd : cmdsData){ if(cmd.getId() == id) return cmd; } }
		return null;
	}
	
	public int id = 0;
	public int typeContainer = 0;
	public int idContainer = 0;
	public int category = 0;
	public boolean common = false;
	public boolean permanent = false;
	public boolean single = false;
	public String name = "";
	public StringData message = new StringData();
	public String params = "";
	protected ICYMCommandData cymcmd;

	private IMechaContainer mechaCon;
	protected Mechanism externalTool;
	
	public List<Mechanism> launchers = new ArrayList<Mechanism>();
	public List<ChildLink> childLinks = new ArrayList<ChildLink>();
	
	private List<MechaControler> controllers = new ArrayList<MechaControler>();
	
	public Mechanism() { }
	public Mechanism(int typeDriver, int idDriver, int category) {
		this.typeContainer = typeDriver;
		this.idContainer = idDriver;
		this.category = category;
	}
	
	public void init(){ getCommandData(); }
	public String getName() { return name; }
	public void start(){ }
	public void start(IMechaDriver driver){
		driver.getControler(this).start();
	}

	public void cleanControllers(){
		for(MechaControler mc : controllers.toArray(new MechaControler[0])) mc.clean();
		controllers.clear();
	}
	
	public void stopControllers(){
		for(MechaControler mc : controllers.toArray(new MechaControler[0])) mc.stop();
	}
	
	public void launch(Entity e) {
		MechaDriver.newDriver(getContainer(), e).getControler(this).launch();
	}
	
	public void launch(IMechaDriver driver) {
		driver.getControler(this).launch();
	}
	
	public abstract AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver);
	public AbsMechaStateEntity initState(MechaControler mc, IMechaDriver driver){
		AbsMechaStateEntity mse = newState(mc, driver);
		mse.cloneData();
		return mse;
	}
	public boolean isMechaStoppable(){ return false; }
	public void addController(MechaControler mc){ controllers.add(mc); }
	public void removeController(MechaControler mc){ controllers.remove(mc); }
	public List<MechaControler> getControllers(){ return controllers; }
	public boolean hasCurrentStatesActive(){ for(MechaControler mc : controllers) if(mc.hasCurrentStatesActive()) return true; return false; }
	public AbsMechaStateEntity getStateActive(){ for(MechaControler mc : controllers) if(mc.hasCurrentStatesActive()) return mc.getStateActive(); return null; }
	public List<AbsMechaStateEntity> getStatesActives(){
		List<AbsMechaStateEntity> states = new ArrayList<AbsMechaStateEntity>();
		for(MechaControler mc : controllers){
			AbsMechaStateEntity state = mc.getStateActive();
			if(state != null) states.add(state);
		}
		return states;
	}
	public void updateData(){ for(MechaControler mc : controllers) mc.updateData(); }
	
	public abstract int getType();
	public abstract String getParams();
	public abstract String getParamsGUI();
	protected abstract void loadParams(String[] params);
	public void loadParams(String params) {
		String[] str = params.split(DELIMITER);
		loadParams(str);
		updateData();
	}
	
	public ICYMCommandData newCommandData() { return null; }
	public ICYMCommandData getCommandData() {
		if(cymcmd == null){
			cymcmd = newCommandData();
			if(cymcmd != null){
				cymcmd.init();
				cmdsData.add(cymcmd);
			}
		}
		return cymcmd;
	}
	public void sendCmdGui(Player p, ICYMCommandData cmdData){ cmdData.initSend(p); new DataQuestMecha().sendCommandGui(p, cmdData); }
	
	public void sqlCreate(){
		QuestSQLManager.create(this);
	}
	public void sqlSave() {
		QuestSQLManager.save(this);
	}
	public void sqlDelete() {
		if(cymcmd != null) cmdsData.remove(cymcmd);
		cleanControllers();
		QuestSQLManager.delete(this);
		if(mechaCon == null) mechaCon = AbsMechaContainer.get(typeContainer, idContainer);
		if(mechaCon != null) mechaCon.removeMecha(this);
		for(ChildLink cl : childLinks.toArray(new ChildLink[0])) 
			delLink(cl.child);
		for(Mechanism m : launchers.toArray(new Mechanism[0])) 
			m.delLink(this);
	}
	public void link(Mechanism child, int slot) {
		link(child, slot, true);
	}
	public void link(Mechanism child, int slot, boolean save) {
		if(!childLinks.contains(child)){
			ChildLink cl = new ChildLink(child, slot);
			childLinks.add(cl);
			cl.questSort.createOrder(cl.getSortList().size());
			child.launchers.add(this);
			if(save) QuestSQLManager.link(this, cl);
			for(MechaControler mc : controllers) mc.link(child);
		}
	}

	public void delLink(Mechanism child) {
		delLink(child, true);
	}
	public void delLink(Mechanism child, boolean save) {
		delLink(getLink(child), save);
	}
	public void delLink(ChildLink child, boolean save) {
		if(child != null){
			child.questSort.deleteOrder();
			childLinks.remove(child);
			child.get().launchers.remove(this);
			if(save) QuestSQLManager.delLink(this, child);
			//if(child.launchers.isEmpty()) child.stop();
			for(MechaControler mc : controllers) mc.delLink(child.get());
		}
	}
	public void delAllLinkIn(boolean save){
		for(Mechanism m : launchers.toArray(new Mechanism[0]))
			m.delLink(this, save);
	}
	public void delAllLinkOut(boolean save){
		for(ChildLink cl : childLinks.toArray(new ChildLink[0]))
			delLink(cl, save);
	}
	
	public ChildLink getLink(Mechanism m){
		ChildLink childLink = null;
		for(ChildLink cl : childLinks)
			if(cl.getId() == m.getId()){ childLink = cl; break; }
		return childLink;
	}
	
	public void upLink(ChildLink child) {
		if(child != null) child.questSort.upOrder();
	}

	public void downLink(ChildLink child) {
		if(child != null) child.questSort.downOrder();
	}

	public void moveLink(ChildLink child, int slot) {
		if(child != null){
			child.questSort.deleteOrder();
			child.slot = slot;
			child.questSort.createOrder(child.getSortList().size());
			child.save();
		}
	}
	
	public void addState(int idPlayer) {
		QuestPlayer qp = QuestPlayer.get(idPlayer);
		IMechaDriver driver = mechaCon.getDriver(qp);
		if(driver != null)
			start(driver);
	}
	
	public Mechanism getExternalTool(){ return externalTool; }
	
	public IMechaContainer getContainer(){ return mechaCon; }
	public int getTypeCon(){ return mechaCon.getTypeContainer(); }
	public int getIdCon(){ return mechaCon.getId(); }
	
	// ------------------- NEW INSTANCE -------------------
	public static Mechanism newMechanism(int type, int typeDriver, int idDriver, int category){
		return newMechanism(type, AbsMechaContainer.get(typeDriver, idDriver), category);
	}
	public static Mechanism newMechanism(int type, IMechaContainer mc, int category){
		Mechanism m = null;
		try {
			MechaType mt = types.get(type);
			m = mt.cls.newInstance();
			m.typeContainer = mc.getTypeContainer();
			m.idContainer = mc.getId();
			m.category = mt.category.id;
		} catch (Exception e) { Plugin.log("Mechanism type missing : "+type); }
		if(m != null){
			m.mechaCon = mc;
			if(mc != null) mc.addMecha(m);
		}
		return m;
	}
	
	public Mechanism clone(IMechaContainer con){
		return clone(con, name);
	}
	
	public Mechanism clone(IMechaContainer con, boolean save){
		return clone(con, name, save);
	}
	
	public Mechanism clone(IMechaContainer con, String name){
		return clone(con, name, true);
	}
	
	public Mechanism clone(IMechaContainer con, String name, boolean save){
		Mechanism m = con.getMecha(name);
		if(m == null) m = cloneMecha(con, name, save, true);
		return m;
	}
	
	public Mechanism cloneRename(IMechaContainer con, boolean links){
		Mechanism mechaClone = null;
		for(int i = 2 ; i < 100 ; i++){
			String name15 = name;
			if(i < 10){
				if(name15.length() > 14) name15 = name15.substring(0, 14);
			}else{
				if(name15.length() > 13) name15 = name15.substring(0, 13);
			}
			mechaClone = con.getMecha(name15+i);
			if(mechaClone == null){
				mechaClone = cloneMecha(con, name15+i, true, links);
				break;
			}
		}
		return mechaClone;
	}
	
	private Mechanism cloneMecha(IMechaContainer con, String name, boolean save, boolean links){
		Mechanism m = newMechanism(getType(), con, category);
		m.common = common;
		m.permanent = permanent;
		m.single = single;
		m.name = name;
		m.message.load(message.toString());
		m.loadParams(getParams());
		if(save) m.sqlCreate(); else m.id = id;
		m.init();
		if(links) for(ChildLink cl : childLinks) m.link(cl.child.clone(con, save), cl.slot, save);
		return m;
	}
	
	public Mechanism cloneCloseRename(Map<String, Mechanism> ref, IMechaContainer con, boolean save, boolean links){
		Mechanism mechaClone = ref.get(name);
		if(mechaClone == null){
			mechaClone = con.getMecha(name);
			if(mechaClone == null){
				mechaClone = cloneClose(ref, con, name, true, links);
			}else{
				for(int i = 2 ; i < 100 ; i++){
					String name15 = name;
					if(i < 10){
						if(name15.length() > 14) name15 = name15.substring(0, 14);
					}else{
						if(name15.length() > 13) name15 = name15.substring(0, 13);
					}
					name15 += i;
					mechaClone = ref.get(name15);
					if(mechaClone == null) mechaClone = con.getMecha(name15);
					if(mechaClone == null){
						mechaClone = cloneClose(ref, con, name15, save, links);
						break;
					}
				}
			}
		}		
		return mechaClone;
	}
	
	public Mechanism cloneClose(Map<String, Mechanism> ref, IMechaContainer con, String name, boolean save, boolean links){
		Mechanism m = newMechanism(getType(), con, category);
		ref.put(this.name, m);
		m.common = common;
		m.permanent = permanent;
		m.single = single;
		m.name = name;
		m.message.load(message.toString());
		m.loadParams(getParams());
		if(save) m.sqlCreate(); else m.id = id;
		m.init();
		if(links) for(ChildLink cl : childLinks) m.link(cl.child.cloneCloseRename(ref, con, save, links), cl.slot, save);
		return m;
	}
	
	public int getId() {
		return id;
	}
	@Override
	public boolean equals(Object obj) {
		if(getClass() == obj.getClass()){
			Mechanism m = (Mechanism) obj;
			return id == m.id;
		}else if(ChildLink.class == obj.getClass()){
			ChildLink cl = (ChildLink) obj;
			return id == cl.child.id;
		}
		return false;
	}
	
	public Mechanism same(Mechanism m) {
		if(getId() == m.getId() && getIdCon() == m.getIdCon() && getTypeCon() == m.getTypeCon()) return this; return null;
	}
	
	public ChildLink newChildLink(Mechanism child, int slot){
		return new ChildLink(child, slot);
	}
	private static int incrementLink = 0;
	public class ChildLink implements IQuestSort{
		private int idlink = 0;
		private Mechanism child;
		private int slot;
		public QuestSort questSort;
		public ChildLink(Mechanism child, int slot) {
			this.idlink = ++incrementLink; 
			this.child = child;
			this.slot = slot;
			questSort = new QuestSort(this);
		}
		
		public Mechanism get(){ return child; }
		
		public int getSlot(){ return slot; }

		public int getId(){ return child.id; }
		@Override
		public QuestSort getSort() { return questSort; }
		@Override
		public int getIdSort() { return idlink; }
		@Override
		public void setOrder(int order){ questSort.setOrder(order); }
		@Override
		public int getOrder(){ return questSort.getOrder(); }
		@Override
		public void save() { QuestSQLManager.linkUpdate(Mechanism.this, this); }
		@Override
		public List<? extends IQuestSort> getSortList() {
			List<ChildLink> list = new ArrayList<ChildLink>();
			for(Mechanism m : getContainer().getMechas())
				for(ChildLink cl : m.childLinks)
					if(cl.slot == slot) list.add(cl);
			return list;
		}
		@Override
		public boolean equals(Object obj) {
			if(getClass() == obj.getClass()){
				ChildLink cl = (ChildLink) obj;
				return child.id == cl.child.id;
			}else if(Mechanism.class == obj.getClass()){
				Mechanism m = (Mechanism) obj;
				return child.id == m.id;
			}
			return false;
		}
	}
}