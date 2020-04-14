package fr.craftyourmind.quest.mecha;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import fr.craftyourmind.manager.CYMChecker;
import fr.craftyourmind.manager.CYMChecker.ICheckerInventory;
import fr.craftyourmind.manager.checker.Inventory;

public class TriggerInventory extends Mechanism{

	public IntegerData amount = new IntegerData();
	public StringData idMat = new StringData();
	public IntegerData data = new IntegerData();
	public StringData displayName = new StringData();
	
	public TriggerInventory() {}
	@Override
	public boolean isMechaStoppable(){ return true; }
	@Override
	public int getType() { return MechaType.TRIINVENTORY; }
	@Override
	public String getParams() {
		return 2+DELIMITER+amount+DELIMITER+displayName+DELIMITER+idMat+DELIMITER+data;
	}
	@Override
	public String getParamsGUI() {
		Material mat = Material.getMaterial(idMat.get());
		if(mat == null) return ""; else return mat.getKey().toString();
	}
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 3){
			amount.load(params[0]);
			idMat.load(params[1]);
			data.load(params[2]);
			sqlSave();
		}else{
			int version = Integer.valueOf(params[0]);
			amount.load(params[1]);
			displayName.load(params[2]);
			idMat.load(params[3]);
			data.load(params[4]);
		}
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateInv(this, mc, driver); }
	// ------------------ StateInv ------------------
	class StateInv extends AbsMechaStateEntitySave implements ICheckerInventory{
		private IntegerData amount = new IntegerData();
		private StringData idMat = new StringData();
		private IntegerData data = new IntegerData();
		private StringData displayName = new StringData();
		private Material mat;
		private int nb = 0;
		private Inventory inv;
		
		public StateInv(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void cloneData() {
			super.cloneData();
			amount.clone(this, TriggerInventory.this.amount);
			idMat.clone(this, TriggerInventory.this.idMat);
			data.clone(this, TriggerInventory.this.data);
			displayName.clone(this, TriggerInventory.this.displayName);
			mat = Material.getMaterial(idMat.get());
		}
		@Override
		public void start() {
			super.start();
			if(driver.isPlayer()){
				if(inv != null) inv.stop();
				inv = CYMChecker.startInventory(qp.getCYMPlayer(), this);
				checker();
			}else stop();
		}
		@Override
		public boolean check() {
			if(mat != null && qp.getPlayer() != null ){
				nb = 0;
				for(ItemStack is : qp.getPlayer().getInventory().getContents()){ 
					if(is != null && is.getType() == mat && (data.get() == -1 || is.getData().getData() == data.get())){
						String dn = displayName.get();
						if(dn.isEmpty() || (is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().equalsIgnoreCase(dn)))
							nb += is.getAmount();
					}
				}
				if(nb >= amount.get()) return true;
			}
			return false;
		}
		@Override
		public void stop() {
			super.stop();
			if(inv != null) inv.stop();
		}
	}
}