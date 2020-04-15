package fr.craftyourmind.quest;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import fr.craftyourmind.manager.CYMChecker;
import fr.craftyourmind.manager.CYMChecker.ICheckerInventory;
import fr.craftyourmind.manager.checker.Inventory;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

import java.util.logging.Logger;

public class ObjectiveGather extends AbsObjective{
	
	public int amount = 0;
	public String idMat = "minecraft:air";
	public byte data = 0;
	public boolean pickup = true;
	public String displayName = "";
	
	private Material mat;
	
	public ObjectiveGather(Quest q) { super(q); }	
	@Override
	public IStateObj getState(StateQuestPlayer sqp) { return new StateGather(sqp); }
	@Override
	public int getType() { return GATHER; }
	@Override
	public String getStrType() { return STRGATHER; }
	@Override
	public String getParams() {
		return new StringBuilder("4").append(DELIMITER).append(amount).append(DELIMITER).append(idMat).append(DELIMITER).append(data).append(DELIMITER).append(displayName).append(DELIMITER).append(pickup).toString();
	}
	@Override
	public String getParamsGUI() {
		Material mat;
		if(idMat.split(":").length > 1)
			mat = Material.getMaterial(idMat.split(":")[1].toUpperCase());
		else
			mat = Material.getMaterial(idMat);
		if(mat == null) return ""; else return mat.getKey().toString();
	}
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 3){
			amount = Integer.valueOf(params[0]);
			idMat = params[1];
			data = Byte.valueOf(params[2]);
			sqlSave();
		}else if(params.length == 4){
			amount = Integer.valueOf(params[0]);
			idMat = params[1];
			data = Byte.valueOf(params[2]);
			pickup = Boolean.valueOf(params[3]);
			sqlSave();
		}else{
			int version = Integer.valueOf(params[0]);
			amount = Integer.valueOf(params[1]);
			idMat = params[2];
			data = Byte.valueOf(params[3]);
			displayName = params[4];
			pickup = Boolean.valueOf(params[5]);
		}
		if(idMat.split(":").length > 1)
			mat = Material.getMaterial(idMat.split(":")[1].toUpperCase());
		else
			mat = Material.getMaterial(idMat);

	}
	@Override
	public String getIdItem() { return idMat; }
	@Override
	public byte getDataItem() { return data; }
	// ---------------- STATEGATHER ----------------
	class StateGather extends StateObjPlayer implements ICheckerInventory{

		public int nb = 0;
		public int prenb = 0;
		public int nbcurrent = 0;
		private Inventory inv;
		
		public StateGather(StateQuestPlayer sqp) { super(sqp); }
		@Override
		public boolean check() {
			if(mat != null && sqp.qp.getPlayer() != null){
				nb = 0;
				for(ItemStack is : sqp.qp.getPlayer().getInventory().getContents()){
					if(is != null && is.getType() == mat){
						if(displayName.isEmpty() || (is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().equalsIgnoreCase(displayName)))
							nb += is.getAmount();
					}
				}
				int diff = nb - prenb;
				prenb = nb;
				if(diff != 0)  tick(diff);

				if(nbcurrent >= amount){
					if(!isTerminate()){
						sendMessagesSuccess();
						terminate();
					}
					return true;
				}else if(isTerminate()) afterCheckKO();
			}
			return false;
		}
		@Override
		public void tick(int nb) {
			super.tick(nb);
			nbcurrent += nb;
		}
		@Override
		public void begin() { inv = CYMChecker.startInventory(sqp.qp.getCYMPlayer(), this); }
		@Override
		public void terminate() { super.terminate(); }
		@Override
		public void finish() {
			Material m;
			if(idMat.split(":").length > 1)
				m = Material.getMaterial(idMat.split(":")[1].toUpperCase());
			else
				m = Material.getMaterial(idMat);
			if(pickup && m != null && sqp.qp.getPlayer() != null){
				nb = 0;
				prenb = 0;
				ItemStack[] contents = sqp.qp.getPlayer().getInventory().getContents();
				for(int i = 0 ; i < contents.length ; i++){
					ItemStack is = contents[i];
					if(is != null && is.getType() == m){
						if(displayName.isEmpty() || (is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().equalsIgnoreCase(displayName))){
							nb += is.getAmount();
							if(nb < amount){
								contents[i] = null;
							}else if(is.getAmount() == amount){
								contents[i] = null;
								break;
							}else if(nb >= amount){
								is.setAmount(is.getAmount() - amount + prenb);
								break;
							}
							prenb = nb;
						}
					}
				}
				sqp.qp.getPlayer().getInventory().setContents(contents);
			}
			if(inv != null) inv.stop();
			if(isTerminate()) sqlClean(sqp.qp);
		}
		@Override
		public void clean() {
			if(inv != null) inv.stop();
			if(isTerminate()) sqlClean(sqp.qp);
			tick(-nb);
		}
		@Override
		public String getMessageGui() { return nbcurrent+"/"+amount+" "; }
		@Override
		public int getTick() { return nbcurrent; }
		@Override
		public void initTick(int nbcurrent) { this.nbcurrent = nbcurrent; }
	}
}