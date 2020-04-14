package fr.craftyourmind.quest.mecha;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Deprecated
public class ActionInventoryOld extends Mechanism{

	public int amount = 0;
	public String idMat = "minecraft:air";
	public byte data = 0;
	public boolean give = true;
	public boolean pickup = false;
	public String displayName = "";
	
	public ActionInventoryOld() {}

	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return null; }
	@Override
	public void start(IMechaDriver driver) {
		Player p = driver.getQuestPlayer().getPlayer();
		Material m = Material.getMaterial(idMat);
		if(p != null && m != null){
			if(pickup){ // ------- PICK UP
				int nb = 0;
				int prenb = 0;
				ItemStack[] contents = p.getInventory().getContents();
				for(int i = 0 ; i < contents.length ; i++){
					if(contents[i] != null && contents[i].getType() == m && (data == -1 || contents[i].getData().getData() == data)){
						if(displayName.isEmpty() || (contents[i].hasItemMeta() && contents[i].getItemMeta().hasDisplayName() && contents[i].getItemMeta().getDisplayName().equalsIgnoreCase(displayName))){
							nb += contents[i].getAmount();
							if(nb < amount){
								contents[i] = null;
							}else if(contents[i].getAmount() == amount){
								contents[i] = null;
								break;
							}else if(nb >= amount){
								contents[i].setAmount(contents[i].getAmount() - amount + prenb);
								break;
							}
							prenb = nb;
						}
					}
				}
				p.getInventory().setContents(contents);
				
			}else{ // ------- GIVE
				int nb = amount;
				ItemStack[] contents = p.getInventory().getContents();
				for(int i = 0 ; i < contents.length ; i++){
					if(contents[i] != null && contents[i].getType() == m && contents[i].getData().getData() == data && contents[i].getAmount() < m.getMaxStackSize()){
						if(displayName.isEmpty() || (contents[i].hasItemMeta() && contents[i].getItemMeta().hasDisplayName() && contents[i].getItemMeta().getDisplayName().equalsIgnoreCase(displayName))){
							int diff = m.getMaxStackSize() - contents[i].getAmount();
							if(nb <= diff){
								contents[i].setAmount(contents[i].getAmount()+nb); nb = 0; break;
							}else if(nb > diff){
								contents[i].setAmount(m.getMaxStackSize());
								nb -= diff;
							}
						}
					}
				}
				if(nb > 0){
					for(int i = 0 ; i < contents.length ; i++){
						if(contents[i] == null){ short plop = 0;
							if(nb <= m.getMaxStackSize()){
								contents[i] = new ItemStack(m, nb, plop, data);
								ItemMeta im = contents[i].getItemMeta();
								im.setDisplayName(displayName);
								contents[i].setItemMeta(im);
								break;
							}else if(nb > m.getMaxStackSize()){
								contents[i] = new ItemStack(m, m.getMaxStackSize(), plop, data);
								ItemMeta im = contents[i].getItemMeta();
								im.setDisplayName(displayName);
								contents[i].setItemMeta(im);
								nb -= m.getMaxStackSize();
							}
						}
					}
				}
				p.getInventory().setContents(contents);
			}
		}
		//sendMessages(driver);
		//launch(driver);
	}

	@Override
	public int getType() {
		return MechaType.ACTINVENTORY;
	}

	@Override
	public String getParams() {
		return 2+DELIMITER+amount+DELIMITER+idMat+DELIMITER+data+DELIMITER+give+DELIMITER+displayName+DELIMITER+pickup;
	}

	@Override
	public String getParamsGUI() {
		Material mat = Material.getMaterial(idMat);
		if(mat == null) return ""; else return mat.getKey().toString();
	}

	@Override
	protected void loadParams(String[] params) {
		if(params.length == 5){
			amount = Integer.valueOf(params[0]);
			idMat = params[1];
			data = Byte.valueOf(params[2]);
			give = Boolean.valueOf(params[3]);
			pickup = Boolean.valueOf(params[4]);
			sqlSave();
		}else{
			int version = Integer.valueOf(params[0]);
			amount = Integer.valueOf(params[1]);
			idMat = params[2];
			data = Byte.valueOf(params[3]);
			give = Boolean.valueOf(params[4]);
			displayName = params[5];
			pickup = Boolean.valueOf(params[6]);
		}
	}
	
}
