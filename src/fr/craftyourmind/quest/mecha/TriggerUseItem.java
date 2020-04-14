package fr.craftyourmind.quest.mecha;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import fr.craftyourmind.manager.CYMChecker;
import fr.craftyourmind.manager.CYMChecker.ICheckerUseItem;
import fr.craftyourmind.manager.checker.UseItem;

public class TriggerUseItem extends Mechanism{
	private static final int NONECLICK = 0;
	private static final int LEFTCLICK = 1;
	private static final int MIDDLECLICK = 2;
	private static final int RIGHTCLICK = 3;
	public StringData iditem = new StringData();
	public IntegerData data = new IntegerData();
	public StringData displayName = new StringData();
	public int actionClick;
	public boolean slotSkill, cancelMcEvent;
	
	@Override
	public boolean isMechaStoppable(){ return true; }
	@Override
	public int getType() { return MechaType.TRIUSEITEM; }
	@Override
	public String getParams() {
		return new StringBuilder("3").append(DELIMITER).append(iditem).append(DELIMITER).append(displayName).append(DELIMITER).append(data).append(DELIMITER)
			.append(actionClick).append(DELIMITER).append(slotSkill).append(DELIMITER).append(cancelMcEvent).toString();
	}
	@Override
	public String getParamsGUI() {
		Material mat = Material.getMaterial(iditem.get());
		if(mat == null) return ""; else return mat.getKey().toString();
	}
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 1){
			iditem.load(params[0]);
			sqlSave();
		}else{
			int index = 0;
			int version = Integer.valueOf(params[index++]);
			iditem.load(params[index++]);
			displayName.load(params[index++]);
			data.load(params[index++]);
			actionClick = RIGHTCLICK;
			if(version == 3){
				actionClick = Integer.valueOf(params[index++]);
				slotSkill = Boolean.valueOf(params[index++]);
				cancelMcEvent = Boolean.valueOf(params[index++]);
			}
		}
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateUseItem(this, mc, driver); }
	// ------------------ StateUseItem ------------------
	class StateUseItem extends AbsMechaStateEntitySave implements ICheckerUseItem{
		
		private StringData iditem = new StringData();
		private IntegerData data = new IntegerData();
		private StringData displayName = new StringData();
		private int slotSelected = 0;
		private UseItem ui;
		
		public StateUseItem(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void cloneData() {
			super.cloneData();
			iditem.clone(this, TriggerUseItem.this.iditem);
			data.clone(this, TriggerUseItem.this.data);
			displayName.clone(this, TriggerUseItem.this.displayName);
		}
		@Override
		public void start() {
			super.start();
			if(driver.isPlayer()){
				if(ui != null) ui.stop();
				ui = CYMChecker.startUseItem(qp.getCYMPlayer(), this);
			}else stop();
		}
		@Override
		public boolean check() {
			String dn = displayName.get();
			ItemStack is = qp.getPlayer().getItemInHand();
			if(slotSkill) is = qp.getPlayer().getInventory().getContents()[slotSelected];
			if(is != null && is.getType().getKey().toString() == iditem.get() && (data.get() == -1 || is.getData().getData() == data.get())){
				if(dn.isEmpty() || (is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().equalsIgnoreCase(dn)))
					return true;
			}
			return false;
		}
		@Override
		public void stop() {
			super.stop();
			if(ui != null) ui.stop();
		}
		@Override
		public String getIdItem() { return iditem.get(); }
		@Override
		public int getIdData() { return data.get(); }
		@Override
		public String getNameItem() { return displayName.get(); }
		@Override
		public int getActionClick() { return actionClick; }
		@Override
		public boolean isCancelMcEvent() { return cancelMcEvent; }
		@Override
		public boolean isSlotSkill() { return slotSkill; }
		@Override
		public void setSlotSelected(int slotSelected) { this.slotSelected = slotSelected; }
	}
}