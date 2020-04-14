package fr.craftyourmind.quest;

import org.bukkit.Material;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class ObjectiveMeka extends AbsObjective{

	public String idIcon = "";
	public byte data = 0;
	public boolean defaultState = false;
	
	public ObjectiveMeka(Quest q) { super(q); }
	@Override
	public IStateObj getState(StateQuestPlayer sqp) { return new StateMeka(sqp); }
	@Override
	public int getType() { return MEKA; }
	@Override
	public String getStrType() { return STRMEKA; }
	@Override
	public String getParams() {
		return new StringBuilder("0").append(DELIMITER).append(idIcon).append(DELIMITER).append(data).append(DELIMITER).append(defaultState).toString();
	}
	@Override
	public String getParamsGUI() {
		Material mat = Material.getMaterial(idIcon);
		if(mat == null) return ""; else return mat.getKey().toString();
	}
	@Override
	protected void loadParams(String[] params) {
		int version = Integer.valueOf(params[0]);
		idIcon = params[1];
		data = Byte.valueOf(params[2]);
		defaultState = Boolean.valueOf(params[3]);
		if(Material.getMaterial(idIcon) == Material.AIR) idIcon = Material.REDSTONE_TORCH.getKey().toString();
		alertItem = idIcon;
	}
	@Override
	public String getIdItem() { return idIcon; }
	@Override
	public byte getDataItem() { return data; }
	
	// ---------------- STATEMEKA ----------------
	public class StateMeka extends StateObjPlayer{
		
		private boolean state = false;
		
		public StateMeka(StateQuestPlayer sqp) { super(sqp); }

		public boolean state(){ return state; }
		
		public void setState(boolean b){
			state = b;
			if(!b) setTerminate(false);
		}
		@Override
		public boolean check() {
			if(state){
				sendMessagesSuccess();
				terminate();
			}
			return state;
		}
		@Override
		public void begin() {
			state = defaultState;
			checker();
		}
		@Override
		public void finish() { }
		@Override
		public void clean() { }
	}
}