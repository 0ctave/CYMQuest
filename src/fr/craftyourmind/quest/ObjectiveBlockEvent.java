package fr.craftyourmind.quest;

import org.bukkit.Material;
import fr.craftyourmind.manager.CYMChecker;
import fr.craftyourmind.manager.CYMChecker.ICheckerBlockEvent;
import fr.craftyourmind.manager.checker.BlockEvent;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class ObjectiveBlockEvent extends AbsObjective{

	public static final int BREAK = 0;
	public static final int PLACE = 1;
	public static final int INTERACT = 2;
	
	public int type = 0;
	public String idMat = "minecraft:air";
	public byte data = 0;
	
	public String world = "";
	public int x = 0;
	public int y = 0;
	public int z = 0;
	
	public boolean cancelled = false;
	public int amount = 1;
	
	public ObjectiveBlockEvent(Quest q) { super(q); }
	@Override
	public IStateObj getState(StateQuestPlayer sqp) {
		StateBlockEvent sbe = new StateBlockEvent(sqp);
		return sbe;
	}
	@Override
	public int getType() { return BLOCKEVENT; }
	@Override
	public String getStrType() { return STRBLOCKEVENT; }
	@Override
	public String getParams() {
		return new StringBuilder().append(type).append(DELIMITER).append(idMat).append(DELIMITER).append(data).append(DELIMITER).append(world).append(DELIMITER)
				.append(x).append(DELIMITER).append(y).append(DELIMITER).append(z).append(DELIMITER).append(cancelled).append(DELIMITER).append(amount).toString();
	}
	@Override
	public String getParamsGUI() {
		Material mat = Material.getMaterial(idMat);
		if(mat == null) return ""; else return mat.getKey().toString();
	}
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 7){ // old version
			type = Integer.valueOf(params[0]);
			idMat = params[1];
			data = Byte.valueOf(params[2]);
			world = params[3];
			x = Integer.valueOf(params[4]);
			y = Integer.valueOf(params[5]);
			z = Integer.valueOf(params[6]);
			sqlSave();
		}else if(params.length == 8){ // old version
			type = Integer.valueOf(params[0]);
			idMat = params[1];
			data = Byte.valueOf(params[2]);
			world = params[3];
			x = Integer.valueOf(params[4]);
			y = Integer.valueOf(params[5]);
			z = Integer.valueOf(params[6]);
			cancelled = Boolean.valueOf(params[7]);
			sqlSave();
		}else if(params.length == 9){
			type = Integer.valueOf(params[0]);
			idMat = params[1];
			data = Byte.valueOf(params[2]);
			world = params[3];
			x = Integer.valueOf(params[4]);
			y = Integer.valueOf(params[5]);
			z = Integer.valueOf(params[6]);
			cancelled = Boolean.valueOf(params[7]);
			amount = Integer.valueOf(params[8]);
		}
	}
	@Override
	public String getIdItem() { return idMat; }
	
	// ---------------- STATEBLOCKEVENT ----------------
	class StateBlockEvent extends StateObjPlayer implements ICheckerBlockEvent{

		private int currentAmount = 0;
		private BlockEvent be;
		
		public StateBlockEvent(StateQuestPlayer sqp) { super(sqp); }
		@Override
		public boolean check() {
			if(cancelled || idMat == "minecaft:air") return true;
			if(currentAmount >= amount){
				terminate();
				sendMessagesSuccess();
				return true;
			}
			return false;
		}
		@Override
		public void begin() {
			if(!isTerminate()) be = CYMChecker.startBlockEvent(sqp.qp.getCYMPlayer(), this);
		}
		@Override
		public void terminate() {
			super.terminate();
			if(be != null) be.stop();
		}
		@Override
		public void finish() { clean(); }
		@Override
		public void clean() {
			if(be != null) be.stop();
			if(isTerminate()) sqlClean(sqp.qp);
		}
		@Override
		public String getMessageGui() { return currentAmount+"/"+amount+" "; }
		@Override
		public void tick(int nb) {
			super.tick(nb);
			currentAmount += nb;
		}
		@Override
		public int getTick() { return currentAmount; }
		@Override
		public void initTick(int nbcurrent) { this.currentAmount = nbcurrent; }
		@Override
		public void event() {
			if(!isLock()){
				tick(1);
				checker();
			}
		}
		@Override
		public boolean getCancelled() { return cancelled; }
		@Override
		public String getWorld() { return world; }
		@Override
		public int getX() { return x; }
		@Override
		public int getY() { return y; }
		@Override
		public int getZ() { return z; }
		@Override
		public int getData() { return data; }
		@Override
		public String getIdMat() { return idMat; }
		@Override
		public int getTypeEvent() { return type; }		
	}
}