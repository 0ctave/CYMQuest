package fr.craftyourmind.quest.mecha;

import java.util.Map;

public abstract class AbsMechaList extends Mechanism{
	
	private IMechaList select;
	
	public abstract Map<Integer, Class<? extends IMechaList>> getMechaParam();
	@Override
	public String getParams() {
		String params = getStringParams();
		if(!params.isEmpty()) params += DELIMITER;
		return 0+DELIMITER+params+select.getId()+DELIMITER+select.getParams();
	}
	
	protected abstract String getStringParams();
	@Override
	public String getParamsGUI() { return select.getParamsGUI(); }
	@Override
	protected void loadParams(String[] params) {
		int version = Integer.valueOf(params[0]);
		int index = loadParams(1, params);
		select = getParam(Integer.valueOf(params[index++]));
		select.loadParams(index, params);
	}
	/**
	 * @return index
	 */
	protected abstract int loadParams(int index, String[] params);

	public IMechaList getSelect(){ return select; }
	
	public IMechaList getParam(int type){
		if(select != null && select.getId() == type) return select;
		return newParam(type);
	}
	public IMechaList newParam(int type){
		try{ select = getMechaParam().get(type).getDeclaredConstructor(this.getClass()).newInstance(this);
		}catch (Exception e){ }
		return select;
	}
	
	public interface IMechaList{
		public int getId();
		public String getParams();
		public String getParamsGUI();
		public void loadParams(int index, String[] params);
		public IMechaStateList getStateList();
	}
	
	public interface IMechaStateList<S>{
		public void cloneData(S s);
		public void start(S s);
		public void stop(S s);
	}
	
	abstract class AbsMechaStateEntityList extends AbsMechaStateEntity{
		protected IMechaStateList state;
		public AbsMechaStateEntityList(Mechanism m, MechaControler mc, IMechaDriver driver) {
			super(m, mc, driver);
			state = select.getStateList();
		}
		@Override
		public void cloneData() { super.cloneData(); state.cloneData(this); }
		@Override
		public void start() { super.start(); state.start(this); stop(); }
		@Override
		public void stop() { super.stop(); state.stop(this); }
	}
	
	abstract class AbsMechaStateEntityList2 extends AbsMechaStateEntityList{
		public AbsMechaStateEntityList2(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void start() { state.start(this); }
	}
}