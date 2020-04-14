package fr.craftyourmind.quest.mecha;

import org.bukkit.Material;
import fr.craftyourmind.manager.CYMChecker;
import fr.craftyourmind.manager.CYMChecker.ICheckerBlockEvent;
import fr.craftyourmind.manager.checker.BlockEvent;

public class TriggerEventBlock extends Mechanism{

	public static final int BREAK = 0;
	public static final int PLACE = 1;
	public static final int INTERACT = 2;
	
	public int type = 0;
	public StringData idMat = new StringData();
	public IntegerData data = new IntegerData();
	
	public String world = "";
	public IntegerData x = new IntegerData();
	public IntegerData y = new IntegerData();
	public IntegerData z = new IntegerData();
	
	public boolean cancelled = false;
	public IntegerData amount = new IntegerData(1);
	
	public TriggerEventBlock() {}
	@Override
	public boolean isMechaStoppable(){ return true; }
	@Override
	public int getType() { return MechaType.TRIEVENTBLOCK; }
	@Override
	public String getParams() {
		return new StringBuilder().append(type).append(DELIMITER).append(idMat).append(DELIMITER).append(data).append(DELIMITER).append(world).append(DELIMITER).append(x).append(DELIMITER).append(y).append(DELIMITER).append(z).append(DELIMITER).append(cancelled).append(DELIMITER).append(amount).toString();
	}
	@Override
	public String getParamsGUI() {
		Material mat = Material.getMaterial(idMat.get());
		if(mat == null) return ""; else return mat.getKey().toString();
	}
	@Override
	protected void loadParams(String[] params) {
		type = Integer.valueOf(params[0]);
		idMat.load(params[1]);
		data.load(params[2]);
		world = params[3];
		x.load(params[4]);
		y.load(params[5]);
		z.load(params[6]);
		cancelled = Boolean.valueOf(params[7]);
		amount.load(params[8]);
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateBlockEvent(this, mc, driver); }
	// ------------------ StateBlockEvent ------------------
	class StateBlockEvent extends AbsMechaStateEntitySave implements ICheckerBlockEvent{

		private StringData idMat = new StringData();
		private IntegerData data = new IntegerData();
		private IntegerData x = new IntegerData();
		private IntegerData y = new IntegerData();
		private IntegerData z = new IntegerData();
		private IntegerData amount = new IntegerData(1);
		
		private int currentAmount = 0;
		private BlockEvent be;
		
		public StateBlockEvent(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void cloneData() {
			super.cloneData();
			idMat.clone(this, TriggerEventBlock.this.idMat);
			data.clone(this, TriggerEventBlock.this.data);
			x.clone(this, TriggerEventBlock.this.x);
			y.clone(this, TriggerEventBlock.this.y);
			z.clone(this, TriggerEventBlock.this.z);
			amount.clone(this, TriggerEventBlock.this.amount);
		}
		@Override
		public void start() {
			super.start();
			if(driver.isPlayer()){
				if(be != null) be.stop();
				be = CYMChecker.startBlockEvent(qp.getCYMPlayer(), this);
			}else stop();
		}
		@Override
		public boolean check() {
			if(cancelled || idMat.get() == "minecraft:air") return true;
			if(currentAmount >= amount.get()){ currentAmount = 0; return true; }
			return false;
		}
		@Override
		public void stop() {
			super.stop();
			if(be != null) be.stop();
		}
		@Override
		public void event() {
			currentAmount++;
			checker();
		}
		@Override
		public boolean getCancelled() { return cancelled; }
		@Override
		public int getData() { return data.get(); }
		@Override
		public String getIdMat() { return idMat.get(); }
		@Override
		public int getTypeEvent() { return type; }
		@Override
		public String getWorld() { return world; }
		@Override
		public int getX() { return x.get(); }
		@Override
		public int getY() { return y.get(); }
		@Override
		public int getZ() { return z.get(); }
	}
}