package fr.craftyourmind.quest.mecha;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import fr.craftyourmind.manager.command.AbsCYMCommand;
import fr.craftyourmind.manager.command.ICYMCommandData;
import fr.craftyourmind.quest.CatBox;
import fr.craftyourmind.quest.ICatBox;
import fr.craftyourmind.quest.IMekaBox;
import fr.craftyourmind.quest.MekaBox;
import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.sql.QuestSQLManager;

public class ToolBox extends AbsMechaList {

	private static final int TOOLMEKABOX = 1;
	private static final int EXTERNAL = 2;
	private static Map<Integer, Class<? extends IMechaList>> params = new HashMap<Integer, Class<? extends IMechaList>>();
	static{
		params.put(TOOLMEKABOX, TOOLMEKABOX.class);
		params.put(EXTERNAL, EXTERNAL.class);
	}
	
	public ToolBox() { }
	@Override
	public int getType() { return MechaType.TOOMEKABOX; }	
	@Override
	public Map<Integer, Class<? extends IMechaList>> getMechaParam() { return params; }
	@Override
	protected String getStringParams() { return ""; }
	@Override
	protected int loadParams(int index, String[] params) { return index; }
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 3){
			String idType = params[0];
			String idCat = params[1];
			String idBox = params[2];
			getParam(TOOLMEKABOX).loadParams(0, new String[]{ "0", idType, idCat, idBox, "0" });
			sqlSave();
		}else
			super.loadParams(params);
	}
	@Override
	public void init() {
		super.init();
		((IListBox)getSelect()).init();
	}
	
	@Override
	public List<AbsMechaStateEntity> getStatesActives() {
		return ((IListBox)getSelect()).getStatesActives();
	}
	@Override
	public Mechanism same(Mechanism m) {
		return ((IListBox)getSelect()).same(m);
	}
	
	public void removeTool(){
		((IListBox)getSelect()).removeTool();
	}
	@Override
	public boolean isMechaStoppable(){ return ((IListBox)getSelect()).isMechaStoppable(); }
	@Override
	public ICYMCommandData newCommandData() { return new CmdBox(); }
	@Override
	public void cleanControllers() {
		super.cleanControllers();
		((IListBox)getSelect()).cleanControllers();
	}
	@Override
	public void sqlDelete() {
		super.sqlDelete();
		((IListBox)getSelect()).sqlDelete();
	}
	public interface IListBox extends IMechaList{
		public void init();
		public boolean isMechaStoppable();
		public List<AbsMechaStateEntity> getStatesActives();
		public Mechanism same(Mechanism m);
		public void removeTool();
		public void cleanControllers();
		public void sqlDelete();
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateBox(this, mc, driver); }
	// ------------------ StateBox ------------------
	class StateBox extends AbsMechaStateEntityList2{
		
		public StateBox(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
	}
	
	// -------------- MEKABOX --------------
	public class TOOLMEKABOX implements IListBox, IMechaContainer{
		public boolean isInit;
		private int idType, idCat, idBox;
		private IMekaBox mbox;
		private MechaContainer mcon = new MechaContainer();
		private Mechanism boxenter;
		private List<EXTERNAL> externals = new ArrayList<EXTERNAL>();
		private List<REPLACE> replaces = new ArrayList<REPLACE>();
		@Override
		public int getId() { return TOOLMEKABOX; }
		@Override
		public String getParams() {
			StringBuilder sb = new StringBuilder("0").append(DELIMITER).append(idType).append(DELIMITER).append(idCat).append(DELIMITER).append(idBox);
			sb.append(DELIMITER).append(replaces.size());
			for(REPLACE r : replaces) r.getParams(sb);
			return sb.toString();
		}
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			idType = Integer.valueOf(params[index++]);
			idCat = Integer.valueOf(params[index++]);
			idBox = Integer.valueOf(params[index++]);
			replaces.clear();
			int size = Integer.valueOf(params[index++]);
			for(int i = 0 ; i < size ; i++){
				REPLACE r = new REPLACE();
				index = r.loadParams(index, params);
				replaces.add(r);
			}
			if(isInit) initMekaBox();
		}
		@Override
		public String getParamsGUI() {
			StringBuilder param = new StringBuilder();
			List<ICatBox> list = CatBox.get(idType);
			param.append(list.size());
			for(ICatBox cat : list)
				param.append(DELIMITER).append(cat.getId()).append(DELIMITER).append(cat.getName()).append(DELIMITER).append(cat.getOrder());
			ICatBox<IMekaBox> cat = CatBox.get(idType, idCat);
			if(cat == null ) param.append(DELIMITER).append(0);
			else{ param.append(DELIMITER).append(cat.getMekaboxs().size());
				for(IMekaBox mb : cat.getMekaboxs())
					param.append(DELIMITER).append(mb.getId()).append(DELIMITER).append(mb.getName()).append(DELIMITER).append(mb.getOrder());
			}
			return param.toString();
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateBox>() {
				private MechaControler mcboxenter;
				@Override
				public void cloneData(StateBox s) { }
				@Override
				public void start(StateBox s) {
					if(boxenter != null){
						s.sendMessage();
						if(mcboxenter == null ) mcboxenter = s.driver.getControler(boxenter);
						mcboxenter.start();
					}
					s.stop();
				}
				@Override
				public void stop(StateBox s) { }
			};
		}
		@Override
		public void init() {
			isInit = true;
			if(mbox == null || mbox.getId() != idBox) mbox = MekaBox.get(idType, idCat, idBox);
			initMekaBox();
			QuestSQLManager.updateStateMecha(this);
		}
		
		public void initMekaBox(){
			List<MechaControler> mcparents = new ArrayList<MechaControler>();
			List<AbsMechaStateEntity> currentStates = new ArrayList<AbsMechaStateEntity>();
			for(MechaControler mc : getControllers()) mcparents.addAll(mc.getParents());
			currentStates.addAll(getStatesActives());
			
			clearMechas();
			if(mbox != null){
				if(mbox.getEnter() != null){
					boxenter = mbox.getEnter().clone(this, false);
					for(Mechanism m : mbox.getMechas())
						if(m.launchers.isEmpty()) m.clone(this, false);
				}
				if(mbox.getExit() != null){
					StarterBox boxexit = (StarterBox) getMecha(mbox.getExit().getId());
					if(boxexit != null) boxexit.setToolExit(ToolBox.this);
				}
				mbox.addTool(this);
			}
			
			for(MechaControler mc : mcparents) mc.link(ToolBox.this);

			for(AbsMechaStateEntity state : currentStates){
				Mechanism same = same(state.getMechanism());
				if(same != null) same.start(state.driver);
			}
			
			for(Mechanism m : getContainer().getMechas()){
				if(m.getType() == getType()){
					ToolBox tb = (ToolBox) m;
					if(tb.getSelect().getId() == EXTERNAL){
						EXTERNAL ex = (EXTERNAL) tb.getSelect();
						if(ex.idToolbox == ToolBox.this.id)
							externals.add(ex);
					}
				}
			}
			for(EXTERNAL ex : externals) ex.initExternal(ToolBox.this.id, this);
			for(REPLACE re : replaces) re.initReplace(this);
		}
		@Override
		public List<AbsMechaStateEntity> getStatesActives() {
			List<AbsMechaStateEntity> states = new ArrayList<AbsMechaStateEntity>();
			for(Mechanism m : getMechas()) states.addAll(m.getStatesActives());
			return states;
		}
		@Override
		public Mechanism same(Mechanism m) {
			for(Mechanism mecha : getMechas()){
				Mechanism same = mecha.same(m);
				if(same != null) return same;
			}
			return null;
		}
		
		public void removeTool(){
			if(mbox == null) mbox = MekaBox.get(idBox);
			if(mbox != null) mbox.removeTool(this);
		}
		@Override
		public boolean isMechaStoppable(){ return true; }
		@Override
		public void cleanControllers() {
			for(Mechanism m : getMechas()){
				m.cleanControllers();
				if(m.getType() == MechaType.TOOMEKABOX) ((ToolBox)m).removeTool();
			}
		}
		@Override
		public void sqlDelete() {
			removeTool();
		}
		public IMechaContainer getContainer() { return ToolBox.this.getContainer(); }
		@Override
		public String getName() { return ToolBox.this.getName(); }
		@Override
		public void setId(int id) { }
		@Override
		public String getParamsCon(){ return "0"; }
		@Override
		public int loadParamsCon(String[] params){ return 1; }
		@Override
		public void loadParamsCon(String params){ }
		@Override
		public List<Mechanism> getMechas() { return mcon.getMechas(); }
		@Override
		public int getTypeContainer() { return AbsMechaContainer.TOOLBOX; }
		@Override
		public void removeMecha(Mechanism m) { mcon.removeMecha(m); }
		@Override
		public void addMecha(Mechanism m) { mcon.addMecha(m); }
		@Override
		public Mechanism getMecha(int id) { return mcon.getMecha(id); }
		@Override
		public Mechanism getMecha(String name) { return mcon.getMecha(name); }
		@Override
		public void clearMechas(){
			ToolBox.this.cleanControllers();
			getMechas().clear();
		}
		@Override
		public IMechaDriver getDriver(QuestPlayer qp) { return null; }
		@Override
		public IMechaDriver newDriver(QuestPlayer qp) { return null; }
		@Override
		public IMechaDriver newDriverGuest(QuestPlayer qp) { return null; }
		@Override
		public IMechaDriver getDriverGuest(QuestPlayer qp) { return null; }
		@Override
		public IMechaDriver newDriver(Entity e) { return null; }
		@Override
		public Map<String, MechaParam> getMechaParams() { return mbox == null ? getContainer().getMechaParams() : mbox.getMechaParams(); }
		@Override
		public MechaParam getMechaParam(String param) { return mbox == null ? getContainer().getMechaParam(param) : mbox.getMechaParam(param); }
		@Override
		public List<String> getNameMechaParams() { return mbox == null ? getContainer().getNameMechaParams() : mbox.getNameMechaParams(); }
		@Override
		public void addConParams(IMechaDriver driver) { driver.addConParams(this); }
		@Override
		public AbsStateContainer getState(QuestPlayer qp) { return null; }
		@Override
		public AbsStateContainer getStateOrCreate(QuestPlayer qp) { return null; }
		@Override
		public AbsStateContainer newStateContainer(QuestPlayer qp) { return null; }
		@Override
		public AbsStateContainer createStateContainer(QuestPlayer qp) { return null; }
		@Override
		public AbsStateContainer loadStateContainer(QuestPlayer qp) { return null; }
		@Override
		public void removeState(QuestPlayer qp) { }
		@Override
		public boolean isActivated() { return false; }
		@Override
		public void updateConParams(Map<String, MechaParam> addmps, List<String> removemps){ }
		@Override
		public IMechaParamSave getMechaParamSave() { return null; }
		@Override
		public String getMechaParamPlayers(String name) { return ""; }
		@Override
		public void updateMechaParamPlayer(String name, QuestPlayer qp, String value) { }
		@Override
		public void updateMechaParamAll(String name, String value) { }
	}
	// -------------- EXTERNAL --------------
	class EXTERNAL implements IListBox{
		private int idToolbox, idExternal;
		private TOOLMEKABOX toolbox;
		private Mechanism external;
		private boolean cancelLinkIn, cancelLinkOut;
		@Override
		public int getId() { return EXTERNAL; }
		@Override
		public String getParams() { return "0"+DELIMITER+idToolbox+DELIMITER+idExternal+DELIMITER+cancelLinkIn+DELIMITER+cancelLinkOut; }
		@Override
		public String getParamsGUI() {
			StringBuilder sb = new StringBuilder();
			int size = 0;
			for(Mechanism m : getContainer().getMechas()){
				if(m.getType() == getType()){
					ToolBox tb = (ToolBox) m;
					if(tb.getSelect().getId() == TOOLMEKABOX){
						sb.append(DELIMITER).append(tb.getId()).append(DELIMITER).append(tb.getName());
						size++;
					}
				}
			}
			sb.insert(0, size);
			StringBuilder sb2 = new StringBuilder();
			int size2 = 0;
			if(toolbox != null){
				for(Mechanism m : toolbox.getMechas()){
					sb2.append(DELIMITER).append(m.getId()).append(DELIMITER).append(m.getName());
					size2++;
				}
			}
			sb2.insert(0, size2);
			return sb.append(DELIMITER).append(sb2).toString();
		}
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			idToolbox = Integer.valueOf(params[index++]);
			idExternal = Integer.valueOf(params[index++]);
			cancelLinkIn = Boolean.valueOf(params[index++]);
			cancelLinkOut = Boolean.valueOf(params[index++]);
			this.toolbox = null;
			this.external = null;
			for(Mechanism m : getContainer().getMechas()){
				if(m.getId() == idToolbox && m.getType() == getType()){
					ToolBox tb = (ToolBox) m;
					if(tb.getSelect().getId() == TOOLMEKABOX){
						toolbox = (TOOLMEKABOX) tb.getSelect();
						if(toolbox.isInit) toolbox.initMekaBox();
					}
				}
			}
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateBox>() {
				private MechaControler mcExternal;
				@Override
				public void cloneData(StateBox s) { }
				@Override
				public void start(StateBox s) {
					if(mcExternal == null && external != null) mcExternal = s.driver.getControler(external);
					mcExternal.start();
				}
				@Override
				public void stop(StateBox s) {
					if(mcExternal == null && external != null) mcExternal = s.driver.getControler(external);
					mcExternal.stop();
				}
			};
		}
		public void initExternal(int idToolbox, TOOLMEKABOX toolbox) {
			this.toolbox = null;
			this.external = null;
			if(this.idToolbox == idToolbox){
				this.toolbox = toolbox;
				for(Mechanism m : toolbox.getMechas()){
					if(m.getId() == idExternal){
						this.external = m;
						this.external.externalTool = ToolBox.this;
						if(cancelLinkIn) this.external.delAllLinkIn(false);
						if(cancelLinkOut) this.external.delAllLinkOut(false);
						break;
					}
				}
			}
		}
		@Override
		public void init() { }
		@Override
		public boolean isMechaStoppable() { return external == null ? false :  external.isMechaStoppable(); }
		@Override
		public List<AbsMechaStateEntity> getStatesActives() { return new ArrayList<AbsMechaStateEntity>(); }
		@Override
		public Mechanism same(Mechanism m) { return null; }
		@Override
		public void removeTool() { }
		@Override
		public void cleanControllers() { }
		@Override
		public void sqlDelete() { }
	}
	// -------------- REPLACE --------------
	class REPLACE {
		private int idReplace, idBoxReplace;
		private boolean cancelLinkIn, cancelLinkOut;
		private Mechanism replace, boxReplace;

		public void initReplace(TOOLMEKABOX toolbox) {
			replace = null;
			boxReplace = null;
			for(Mechanism m : getContainer().getMechas())
				if(m.getId() == idReplace){ replace = m; break; }
			for(Mechanism m : toolbox.getMechas())
				if(m.getId() == idBoxReplace){ boxReplace = m; break; }
			if(replace != null && boxReplace != null){
				if(cancelLinkIn){ for(Mechanism parent : boxReplace.launchers) parent.delLink(replace, false); }
				else{ for(Mechanism parent : boxReplace.launchers) parent.link(replace, 0, false); }
				for(Mechanism parent : boxReplace.launchers.toArray(new Mechanism[0])) parent.delLink(boxReplace, false);
				if(cancelLinkOut){ for(ChildLink child : boxReplace.childLinks) replace.delLink(child.get(), false); }
				else{ for(ChildLink child : boxReplace.childLinks) replace.link(child.get(), 0, false); }
				for(ChildLink child : boxReplace.childLinks.toArray(new ChildLink[0])) boxReplace.delLink(child, false);
			}
		}
		
		public StringBuilder getParams(StringBuilder sb){
			return sb.append(DELIMITER).append(idReplace).append(DELIMITER).append(idBoxReplace)
					.append(DELIMITER).append(cancelLinkIn).append(DELIMITER).append(cancelLinkOut);
		}
		
		public int loadParams(int index, String[] params){
			idReplace = Integer.valueOf(params[index++]);
			idBoxReplace = Integer.valueOf(params[index++]);
			cancelLinkIn = Boolean.valueOf(params[index++]);
			cancelLinkOut = Boolean.valueOf(params[index++]);
			return index;
		}
	}
	// --------------------- CmdBox ---------------------
	private static final int LISTCAT = 0;
	private static final int LISTMEKA = 1;
	private static final int LISTMEKABOXREPLACE = 2;
	private static final int COPYMEKABOX = 3;
	class CmdBox extends AbsCYMCommand{

		public CmdBox() { super(id); }
		@Override
		public void initActions() { addAction(new LISTCAT(), new LISTMEKA(), new LISTMEKABOXREPLACE(), new COPYMEKABOX()); }
		@Override
		public void initChilds() { }
		
		class LISTCAT extends AbsCYMCommandAction{
			private int idType;
			private List<Integer> idCatbox = new ArrayList<Integer>();
			private List<String> nameCatbox = new ArrayList<String>();
			private List<Integer> orders = new ArrayList<Integer>();
			@Override
			public int getId() { return LISTCAT; }
			@Override
			public AbsCYMCommandAction clone() { return new LISTCAT(); }
			@Override
			public void initSend(Player p) {
				for(ICatBox cat : CatBox.get(idType)){
					idCatbox.add(cat.getId());
					nameCatbox.add(cat.getName());
					orders.add(cat.getOrder());
				}
			}
			@Override
			public void sendWrite() throws IOException { writeList(idCatbox, nameCatbox); writeListInt(orders); }
			@Override
			public void receiveRead() throws IOException { idType = readInt(); }
			@Override
			public void receive(Player p) { sendCmdGui(p, this); }
		}
		
		class LISTMEKA extends AbsCYMCommandAction{
			private int idType, idCat;
			private List<Integer> idMekabox = new ArrayList<Integer>();
			private List<String> nameMekabox = new ArrayList<String>();
			private List<Integer> orders = new ArrayList<Integer>();
			@Override
			public int getId() { return LISTMEKA; }
			@Override
			public AbsCYMCommandAction clone() { return new LISTMEKA(); }
			@Override
			public void initSend(Player p) {
				ICatBox<IMekaBox> cat = CatBox.get(idType, idCat);
				if(cat != null){
					for(IMekaBox mb : cat.getMekaboxs()){
						idMekabox.add(mb.getId());
						nameMekabox.add(mb.getName());
						orders.add(mb.getOrder());
					}
				}
			}
			@Override
			public void sendWrite() throws IOException { writeList(idMekabox, nameMekabox); writeListInt(orders); }
			@Override
			public void receiveRead() throws IOException { idType = readInt(); idCat = readInt(); }
			@Override
			public void receive(Player p) { sendCmdGui(p, this); }
		}
		
		class LISTMEKABOXREPLACE extends AbsCYMCommandAction{
			public boolean emptyReplace, emptyBoxReplace;
			private String replaceMekas = "", replaceBoxMekas = "";
			@Override
			public int getId() { return LISTMEKABOXREPLACE; }
			@Override
			public AbsCYMCommandAction clone() { return new LISTMEKABOXREPLACE(); }
			@Override
			public void initSend(Player p) {
				if(getSelect().getId() == TOOLMEKABOX){
					TOOLMEKABOX tmb = (TOOLMEKABOX) getSelect();
					if(emptyReplace){
						StringBuilder sb1 = new StringBuilder().append(getContainer().getMechas().size());
						for(Mechanism m : getContainer().getMechas())
							sb1.append(DELIMITER).append(m.getId()).append(DELIMITER).append(m.getName());
						replaceMekas = sb1.toString();
					}
					if(emptyBoxReplace){
						StringBuilder sb2 = new StringBuilder().append(tmb.getMechas().size());
						for(Mechanism m : tmb.getMechas())
							sb2.append(DELIMITER).append(m.getId()).append(DELIMITER).append(m.getName());
						replaceBoxMekas = sb2.toString();
					}
				}
			}
			@Override
			public void sendWrite() throws IOException { write(emptyReplace); write(replaceMekas); write(emptyBoxReplace); write(replaceBoxMekas); }
			@Override
			public void receiveRead() throws IOException { emptyReplace = readBool(); emptyBoxReplace = readBool(); }
			@Override
			public void receive(Player p) { sendCmdGui(p, this); }
		}
		
		class COPYMEKABOX extends AbsCYMCommandAction{
			private int idCopy;
			private boolean copy, listMeka, links;
			private String copyBox = "";
			@Override
			public int getId() { return COPYMEKABOX; }
			@Override
			public AbsCYMCommandAction clone() { return new COPYMEKABOX(); }
			@Override
			public void initSend(Player p) {
				if(getSelect().getId() == TOOLMEKABOX){
					TOOLMEKABOX tmb = (TOOLMEKABOX) getSelect();
					if(listMeka){
						StringBuilder sb2 = new StringBuilder().append(tmb.getMechas().size());
						for(Mechanism m : tmb.getMechas())
							sb2.append(DELIMITER).append(m.getId()).append(DELIMITER).append(m.getName());
						copyBox = sb2.toString();
					}
				}
			}
			@Override
			public void sendWrite() throws IOException { write(copyBox); }
			@Override
			public void receiveRead() throws IOException {
				copy = readBool();
				if(copy){
					idCopy = readInt(); links = readBool();
				}else
					listMeka = readBool();
			}
			@Override
			public void receive(Player p) {
				if(copy && p.hasPermission("cymquest.questedit")){
					if(getSelect().getId() == TOOLMEKABOX){
						TOOLMEKABOX tmb = (TOOLMEKABOX) getSelect();
						Mechanism m = tmb.getMecha(idCopy);
						if(m != null){
							m.cloneCloseRename(new HashMap<String, Mechanism>(), getContainer(), true, links);
						}
					}
				}else if(listMeka) sendCmdGui(p, this);
			}
		}
	}
}