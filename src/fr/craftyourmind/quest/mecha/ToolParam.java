package fr.craftyourmind.quest.mecha;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import fr.craftyourmind.manager.CYMManager;
import fr.craftyourmind.manager.command.AbsCYMCommand;
import fr.craftyourmind.manager.command.ICYMCommandData;
import fr.craftyourmind.quest.CatBox;
import fr.craftyourmind.quest.EventParadise;
import fr.craftyourmind.quest.ICatBox;
import fr.craftyourmind.quest.IMekaBox;
import fr.craftyourmind.quest.MekaBox;
import fr.craftyourmind.quest.Quest;

public class ToolParam extends AbsMechaList{

	private static final int CALCULE = 1;
	private static final int IMPORT = 2;
	private static Map<Integer, Class<? extends IMechaList>> params = new HashMap<Integer, Class<? extends IMechaList>>();
	static{
		params.put(CALCULE, CALCULE.class);
		params.put(IMPORT, IMPORT.class);
	}
	public ToolParam() { }
	@Override
	public int getType() { return MechaType.TOOPARAM; }
	@Override
	public ICYMCommandData newCommandData() { return new CmdParam(); }
	@Override
	public Map<Integer, Class<? extends IMechaList>> getMechaParam() { return params; }
	@Override
	protected String getStringParams() { return ""; }
	@Override
	protected int loadParams(int index, String[] params) { return index; }
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateParam(this, mc, driver); }
	// ------------------ StateParam ------------------
	class StateParam extends AbsMechaStateEntityList2{
		
		public StateParam(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }

	}
	// -------------- CALCULE --------------
	class CALCULE implements IMechaList{
		private static final int INIT = 0;
		private static final int PLUS = 1;
		private static final int MINUS = 2;
		private static final int TIMES = 3;
		private static final int DIVISION = 4;
		private static final int EQUAL = 5;
		private static final int INFERIOROREQUAL = 6;
		private static final int INFERIOR = 7;
		private static final int SUPERIOROREQUAL = 8;
		private static final int SUPERIOR = 9;
		private static final int INITSTR = 10;
		private static final int PLUSSTR = 11;
		private int action = 0;
		private StringData paramAStr = new StringData();
		private StringData paramBStr = new StringData();
		private StringData paramResultStr = new StringData();
		private DoubleData paramA = new DoubleData();
		private DoubleData paramB = new DoubleData();
		private DoubleData paramResult = new DoubleData();
		@Override
		public int getId() { return CALCULE; }
		@Override
		public String getParams() { return new StringBuilder("0").append(DELIMITER).append(action).append(DELIMITER).append(paramAStr).append(DELIMITER).append(paramBStr).append(DELIMITER).append(paramResultStr).append(DELIMITER).append(paramA).append(DELIMITER).append(paramB).append(DELIMITER).append(paramResult).toString(); }
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			action = Integer.valueOf(params[index++]);
			paramAStr.load(params[index++]);
			paramBStr.load(params[index++]);
			paramResultStr.load(params[index++]);
			paramA.load(params[index++]);
			paramB.load(params[index++]);
			paramResult.load(params[index++]);
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateParam>() {
				private StringData paramAStr = new StringData(), paramBStr = new StringData(), paramResultStr = new StringData();
				private DoubleData paramA = new DoubleData(), paramB = new DoubleData(), paramResult = new DoubleData();
				@Override
				public void cloneData(StateParam s) {
					paramA.clone(s, CALCULE.this.paramA);
					paramB.clone(s, CALCULE.this.paramB);
					paramResult.clone(s, CALCULE.this.paramResult);
					paramAStr.clone(s, CALCULE.this.paramAStr);
					paramBStr.clone(s, CALCULE.this.paramBStr);
					paramResultStr.clone(s, CALCULE.this.paramResultStr);
				}
				@Override
				public void start(StateParam s) {
					if(action <= DIVISION){
						if(action == INIT) paramA.set(paramB.get());
						else if(action == PLUS) paramResult.set(paramA.get() + paramB.get());
						else if(action == MINUS) paramResult.set(paramA.get() - paramB.get());
						else if(action == TIMES) paramResult.set(paramA.get() * paramB.get());
						else if(action == DIVISION) paramResult.set(paramA.get() / paramB.get());
						s.launchMessage();
					}else if(action <= SUPERIOR){
						if(action == EQUAL){ if(paramA.get().equals(paramB.get())) s.launchMessage(); }
						else if(action == INFERIOROREQUAL){ if(paramA.get() <= paramB.get()) s.launchMessage(); }
						else if(action == INFERIOR){ if(paramA.get() < paramB.get()) s.launchMessage(); }
						else if(action == SUPERIOROREQUAL){ if(paramA.get() >= paramB.get()) s.launchMessage(); }
						else if(action == SUPERIOR){ if(paramA.get() > paramB.get()) s.launchMessage(); }
					}else{
						if(action == INITSTR) paramAStr.set(paramBStr.get());
						else if(action == PLUSSTR) paramResultStr.set(paramAStr.get() + paramBStr.get());
						s.launchMessage();
					}
				}
				@Override
				public void stop(StateParam s) { }
			};
		}
	}
	// -------------- IMPORT --------------
	class IMPORT implements IMechaList{
		private int container, select, element;
		private String param = "";
		IMechaContainer con = null;
		@Override
		public int getId() { return IMPORT; }
		@Override
		public String getParams() { return new StringBuilder("0").append(DELIMITER).append(param).append(DELIMITER)
				.append(container).append(DELIMITER) .append(select).append(DELIMITER).append(element).toString(); }
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			param = params[index++];
			container = Integer.valueOf(params[index++]);
			select = Integer.valueOf(params[index++]);
			element = Integer.valueOf(params[index++]);
			getContainerParam();
		}
		private void getContainerParam(){
			if(container == AbsMechaContainer.QUEST) con = Quest.get(select, element);
			else if(container == AbsMechaContainer.EVENT) con = EventParadise.get(select, element);
			else if(container == AbsMechaContainer.MEKABOX){
				if(element == 0) con = CatBox.get(MekaBox.BOX, select);
				else con = MekaBox.get(MekaBox.BOX, select, element);
			}else if(container == AbsMechaContainer.CLASS){
				if(element == 0) con = CatBox.get(MekaBox.SKILLBOX, select);
				else con = MekaBox.get(MekaBox.SKILLBOX, select, element);
			}
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateParam>() {
				@Override
				public void cloneData(StateParam s) { }
				@Override
				public void start(StateParam s) {
					if(s.driver.isPlayer()){
						if(con == null) getContainerParam();
						if(con != null){
							s.sqlStart(); s.setActive(true);
							IMechaDriver d = con.getDriver(s.qp);
							if(d != null){
								MechaParam mp = d.getMechaParam(param);
								if(mp != null) s.driver.addConParams(getContainer(), name, mp);
							}else{
								MechaParam mp = con.getMechaParam(param);
								if(mp != null) s.driver.addConParams(getContainer(), name, mp);
							}
							s.launchMessage();
						}
					}
				}
				@Override
				public void stop(StateParam s) { s.sqlStop(); }
			};
		}
	}
	private static final int CONTAINER = 1;
	private static final int SELECT = 2;
	private static final int ELEMENT = 3;
	private static final int PARAM = 4;
	class CmdParam extends AbsCYMCommand{

		public CmdParam() { super(id); }
		@Override
		public void initActions() { addAction(new CONTAINER()); addAction(new SELECT()); addAction(new ELEMENT()); addAction(new PARAM()); }
		@Override
		public void initChilds() { }
		
		class CONTAINER extends AbsCYMCommandAction{
			private List<Integer> idCon = new ArrayList<Integer>();
			private List<String> nameCon = new ArrayList<String>();
			@Override
			public int getId() { return CONTAINER; }
			@Override
			public AbsCYMCommandAction clone() { return new CONTAINER(); }
			@Override
			public void initSend(Player p) {
				idCon.add(AbsMechaContainer.QUEST); idCon.add(AbsMechaContainer.EVENT); idCon.add(AbsMechaContainer.CLASS); // idCon.add(AbsMechaContainer.SKILL); // idCon.add(AbsMechaContainer.MEKABOX);
				nameCon.add("Quest"); nameCon.add("Event"); nameCon.add("Class"); // nameCon.add("Skill"); // nameCon.add("Box"); 
			}
			@Override
			public void sendWrite() throws IOException { writeList(idCon, nameCon); }
			@Override
			public void receiveRead() throws IOException { }
			@Override
			public void receive(Player p) { sendCmdGui(p, this); }
		}
		
		class SELECT extends AbsCYMCommandAction{
			private int idcon;
			private List<Integer> idSelect = new ArrayList<Integer>();
			private List<String> nameSelect = new ArrayList<String>();
			private List<Integer> orders = new ArrayList<Integer>();
			@Override
			public int getId() { return SELECT; }
			@Override
			public AbsCYMCommandAction clone() { return new SELECT(); }
			@Override
			public void initSend(Player p) {
				if(idcon == AbsMechaContainer.QUEST){
					idSelect.addAll(Quest.listNPC.keySet());
					for(Entry<Integer, String> npc : CYMManager.getIdNameNPC().entrySet()){
						if(idSelect.contains(npc.getKey())) nameSelect.add(npc.getValue());
					}
				}else if(idcon == AbsMechaContainer.EVENT){
					idSelect.addAll(EventParadise.getSlots());
					for(Integer i : idSelect) nameSelect.add(i+"");
					
				}else if(idcon == AbsMechaContainer.MEKABOX){
					for(ICatBox cb : CatBox.get(MekaBox.BOX)){
						idSelect.add(cb.getId());
						nameSelect.add(cb.getName());
					}
				}else if(idcon == AbsMechaContainer.CLASS){
					for(ICatBox cb : CatBox.get(MekaBox.SKILLBOX)){
						idSelect.add(cb.getId());
						nameSelect.add(cb.getName());
						orders.add(cb.getOrder());
					}
				}
			}
			@Override
			public void sendWrite() throws IOException { writeList(idSelect, nameSelect); writeListInt(orders); }
			@Override
			public void receiveRead() throws IOException { idcon = readInt(); }
			@Override
			public void receive(Player p) { sendCmdGui(p, this); }
		}
		
		class ELEMENT extends AbsCYMCommandAction{
			private int idcon, idselect;
			private List<Integer> idElement = new ArrayList<Integer>();
			private List<String> nameElement = new ArrayList<String>();
			private List<Integer> orders = new ArrayList<Integer>();
			@Override
			public int getId() { return ELEMENT; }
			@Override
			public AbsCYMCommandAction clone() { return new ELEMENT(); }
			@Override
			public void initSend(Player p) {
				if(idcon == AbsMechaContainer.QUEST){
					List<Quest> lq = Quest.listNPC.get(idselect);
					if(lq != null){
						for(Quest q : lq){
							idElement.add(q.id);
							nameElement.add(q.title.toString());
						}
					}
				}else if(idcon == AbsMechaContainer.EVENT){
					for(EventParadise e : EventParadise.gett(idselect)){
						idElement.add(e.id);
						nameElement.add(e.name.toString());
					}
				}else if(idcon == AbsMechaContainer.MEKABOX){
					ICatBox<IMekaBox> cb = CatBox.get(MekaBox.BOX, idselect);
					if(cb != null){
						for(IMekaBox mb : cb.getMekaboxs()){
							idElement.add(mb.getId());
							nameElement.add(mb.getName());
						}
					}
				}else if(idcon == AbsMechaContainer.CLASS){
					ICatBox<IMekaBox> cb = CatBox.get(MekaBox.SKILLBOX, idselect);
					if(cb != null){
						for(IMekaBox mb : cb.getMekaboxs()){
							idElement.add(mb.getId());
							nameElement.add(mb.getName());
							orders.add(mb.getOrder());
						}
					}
				}
			}
			@Override
			public void sendWrite() throws IOException { writeList(idElement, nameElement); writeListInt(orders); }
			@Override
			public void receiveRead() throws IOException {
				idcon = readInt();
				idselect = readInt();
			}
			@Override
			public void receive(Player p) { sendCmdGui(p, this); }
		}
		
		class PARAM extends AbsCYMCommandAction{
			private int idcon, idselect, idelement;
			private List<String> nameParam = new ArrayList<String>();
			@Override
			public int getId() { return PARAM; }
			@Override
			public AbsCYMCommandAction clone() { return new PARAM(); }
			@Override
			public void initSend(Player p) {
				if(idcon == AbsMechaContainer.QUEST){
					Quest q = Quest.get(idselect, idelement);
					if(q != null) nameParam.addAll(q.getNameMechaParams());
					
				}else if(idcon == AbsMechaContainer.EVENT){
					EventParadise e = EventParadise.get(idselect, idelement);
					if(e != null) nameParam.addAll(e.getNameMechaParams());
					
				}else if(idcon == AbsMechaContainer.MEKABOX){
					ICatBox cb = CatBox.get(MekaBox.BOX, idselect);
					if(cb != null){
						IMekaBox mb = cb.getMekabox(idelement);
						if(mb != null) nameParam.addAll(mb.getNameMechaParams());
					}
				}else if(idcon == AbsMechaContainer.CLASS){
					ICatBox cb = CatBox.get(MekaBox.SKILLBOX, idselect);
					if(cb != null){
						if(idelement == 0) nameParam.addAll(cb.getNameMechaParams());
						else{
							IMekaBox mb = cb.getMekabox(idelement);
							if(mb != null) nameParam.addAll(mb.getNameMechaParams());
						}
					}
				}
			}
			@Override
			public void sendWrite() throws IOException { writeListStr(nameParam); }
			@Override
			public void receiveRead() throws IOException {
				idcon = readInt();
				idselect = readInt();
				idelement = readInt();
			}
			@Override
			public void receive(Player p) { sendCmdGui(p, this); }
		}
	}
}