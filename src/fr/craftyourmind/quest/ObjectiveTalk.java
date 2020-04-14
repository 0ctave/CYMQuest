package fr.craftyourmind.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import fr.craftyourmind.manager.CYMManager;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class ObjectiveTalk extends AbsObjective{

	private static List<StateTalk> listCheck = new ArrayList<StateTalk>();
	
	public int idNPC = 0;
	
	public ObjectiveTalk(Quest q) {
		super(q);
		alertItem = Material.OAK_SIGN.getKey().toString();
	}
	@Override
	public IStateObj getState(StateQuestPlayer sqp) { return new StateTalk(sqp); }
	@Override
	public int getType() { return TALK; }
	@Override
	public String getStrType() { return STRTALK; }
	@Override
	public String getParams() { return ""+idNPC; }
	@Override
	public String getParamsGUI() {
		String id = "";
		String name = "";
		for(Entry<Integer, String> npc : CYMManager.getIdNameNPC().entrySet()){
			id += npc.getKey()+DELIMITER;
			name += npc.getValue()+DELIMITER;
		}
		return id+":"+name;
	}
	@Override
	protected void loadParams(String[] params) { idNPC = Integer.valueOf(params[0]);	}
	
	public static void add(StateTalk st){ listCheck.add(st); }
	
	public static void remove(StateTalk st){ listCheck.remove(st); }
	
	public static void check(Player player, int idNPC){
		List<StateTalk> list = new ArrayList<StateTalk>(listCheck);
		for(StateTalk st : list){
			if(!st.isLock() && st.exist(player)){ 
				st.setIdNPC(idNPC);
				st.checker();
			}
		}
	}
	// ---------------- STATETALK ----------------
	class StateTalk extends StateObjPlayer{

		private int idNPCCheck = -1;
		
		public StateTalk(StateQuestPlayer sqp) { super(sqp); }
		
		public void setIdNPC(int idNPC){ idNPCCheck = idNPC; }
		
		public int getIdNPC(){ return idNPCCheck; }
		@Override
		public boolean check() {
			if(idNPC == getIdNPC()){
				sendMessagesSuccess();
				terminate();
				return true;
			}
			return false;
		}
		@Override
		public void begin() {
			if(!isTerminate()) add(this);
		}
		@Override
		public void terminate() {
			super.terminate();
			setIdNPC(-1);
			remove(this);
		}
		@Override
		public void finish() { clean(); }
		@Override
		public void clean() {
			remove(this);
			if(isTerminate()) sqlClean(sqp.qp);
		}
	}
}