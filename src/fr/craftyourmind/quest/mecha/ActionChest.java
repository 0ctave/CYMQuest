package fr.craftyourmind.quest.mecha;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Deprecated
public class ActionChest extends Mechanism{

	public static final int CHEST = 0;
	public static final int DISPENSER = 1;
	public static final int DROPPER = 2;
	public static final int HOPPER = 3;
	
	public static List<ActionChest> protectExploChests = new ArrayList<ActionChest>();
	
	private MechaCoordRelative coordR = new MechaCoordRelative();
	public boolean delete = false;
	public boolean protectExplo = false;
	
	public List<InventoryStack> invItemsPermanent = new ArrayList<InventoryStack>();
	public List<InventoryStack> invItemsRate = new ArrayList<InventoryStack>();
	public int nbItems = 1;
	
	public int typeCon = 0;
	
	private int version = 4;
	
	public ActionChest() {}	
	@Override
	public boolean isMechaStoppable(){ return true; }
	
	public void protectectExplo(IMechaDriver driver){
		if(protectExplo){
			if(!protectExploChests.contains(this))
				protectExploChests.add(this);
		}
	}

	@Override
	public int getType() { return MechaType.ACTCHEST; }
	@Override
	public String getParams() {
		List<InventoryStack> invItemstmp = new ArrayList<InventoryStack>();
		invItemstmp.addAll(invItemsPermanent);
		invItemstmp.addAll(invItemsRate);
		String size = invItemstmp.size()+"";
		String invs = "";
		for(InventoryStack invSt: invItemstmp)
			invs += invSt.item.getType().getKey().toString()+DELIMITER+invSt.item.getAmount()+DELIMITER+invSt.item.getData().getData()+DELIMITER+invSt.rate+DELIMITER+invSt.randomAmount+DELIMITER+invSt.unique+DELIMITER+invSt.permanent+DELIMITER+invSt.enchant+DELIMITER+invSt.enchantLvl+DELIMITER;
		return 4+DELIMITER+coordR.getParams()+DELIMITER+delete+DELIMITER+protectExplo+DELIMITER+typeCon+DELIMITER+nbItems+DELIMITER+size+DELIMITER+invs;
	}
	@Override
	public String getParamsGUI() {
		List<InventoryStack> invItemstmp = new ArrayList<InventoryStack>();
		invItemstmp.addAll(invItemsPermanent);
		invItemstmp.addAll(invItemsRate);
		String params = "";
		for(InventoryStack invSt : invItemstmp){
			Material mat = Material.getMaterial(invSt.item.getType().getKey().toString());
			if(mat != null) params += mat.getKey().toString()+DELIMITER;
		}
		return params;
	}
	@Override
	protected void loadParams(String[] params) {
		try{
			version = Integer.valueOf(params[0]);
		}catch (Exception e){version = 0;}
		if(version == 0){ // old versions
			version = 3;
			coordR.setCoord(params[0], Integer.valueOf(params[1]), Integer.valueOf(params[2]), Integer.valueOf(params[3]), 0, false);
			delete = Boolean.valueOf(params[4]);
			int size = Integer.valueOf(params[5]);
			int nbparams = 0;
			if(size > 0) nbparams = (params.length-6)/size;
			if(nbparams == 3){
				invItemsRate.clear();
				List<Integer> amounts = new ArrayList<Integer>();
				List<String> ids = new ArrayList<String>();
				List<Byte> datas = new ArrayList<Byte>();
				for (int i = 0 ; i < size ; i++) amounts.add(Integer.valueOf(params[i+6]));
				for (int i = 0 ; i < size ; i++) ids.add(params[i+6+size]);
				for (int i = 0 ; i < size ; i++) datas.add(Byte.valueOf(params[i+6+size+size]));
				for (int i = 0 ; i < size ; i++){
					InventoryStack invSt = new InventoryStack();
					invSt.item = new ItemStack(Material.getMaterial(ids.get(i)), amounts.get(i), (short) 0, datas.get(i));
					invItemsRate.add(invSt);
				}
			}else if(nbparams > 0){
				nbItems = Integer.valueOf(params[6]);
				invItemsPermanent.clear();
				invItemsRate.clear();
				int index = 7;
				for (int i = 0 ; i < size ; i++){
					InventoryStack invSt = new InventoryStack();
					invSt.item = new ItemStack(Material.getMaterial(params[index++]), Integer.valueOf(params[index++]), (short) 0, Byte.valueOf(params[index++]));
					invSt.rate = Double.valueOf(params[index++]);
					invSt.ratetmp = (int) (invSt.rate*100);
					invSt.randomAmount = Boolean.valueOf(params[index++]);
					invSt.unique = Boolean.valueOf(params[index++]);
					invSt.permanent = Boolean.valueOf(params[index++]);
					if(invSt.item.getAmount() <= 0) invSt.item.setAmount(1);
					if(invSt.permanent) invItemsPermanent.add(invSt);
					else invItemsRate.add(invSt);
				}
			}
			sqlSave();
		}else if(version == 1){
			coordR.setCoord(params[1], Integer.valueOf(params[2]), Integer.valueOf(params[3]), Integer.valueOf(params[4]), 0, false);
			delete = Boolean.valueOf(params[5]);
			typeCon = Integer.valueOf(params[6]);
			nbItems = Integer.valueOf(params[7]);
			int size = Integer.valueOf(params[8]);
			invItemsPermanent.clear();
			invItemsRate.clear();
			int index = 9;
			for (int i = 0 ; i < size ; i++){
				InventoryStack invSt = new InventoryStack();
				invSt.item = new ItemStack(Material.getMaterial(params[index++]), Integer.valueOf(params[index++]), (short) 0, Byte.valueOf(params[index++]));
				invSt.rate = Double.valueOf(params[index++]);
				invSt.ratetmp = (int) (invSt.rate*100);
				invSt.randomAmount = Boolean.valueOf(params[index++]);
				invSt.unique = Boolean.valueOf(params[index++]);
				invSt.permanent = Boolean.valueOf(params[index++]);
				if(invSt.item.getAmount() <= 0) invSt.item.setAmount(1);
				if(invSt.permanent) invItemsPermanent.add(invSt);
				else invItemsRate.add(invSt);
			}
			version = 3;
			sqlSave();
		}else if(version == 2){ // new version
			coordR.setCoord(params[1], Integer.valueOf(params[2]), Integer.valueOf(params[3]), Integer.valueOf(params[4]), 0, false);
			delete = Boolean.valueOf(params[5]);
			protectExplo = Boolean.valueOf(params[6]);
			typeCon = Integer.valueOf(params[7]);
			nbItems = Integer.valueOf(params[8]);
			int size = Integer.valueOf(params[9]);
			invItemsPermanent.clear();
			invItemsRate.clear();
			int index = 10;
			for (int i = 0 ; i < size ; i++){
				InventoryStack invSt = new InventoryStack();
				invSt.item = new ItemStack(Material.getMaterial(params[index++]), Integer.valueOf(params[index++]), (short) 0, Byte.valueOf(params[index++]));
				invSt.rate = Double.valueOf(params[index++]);
				invSt.ratetmp = (int) (invSt.rate*100);
				invSt.randomAmount = Boolean.valueOf(params[index++]);
				invSt.unique = Boolean.valueOf(params[index++]);
				invSt.permanent = Boolean.valueOf(params[index++]);
				if(invSt.item.getAmount() <= 0) invSt.item.setAmount(1);
				if(invSt.permanent) invItemsPermanent.add(invSt);
				else invItemsRate.add(invSt);
			}
			version = 3;
			sqlSave();
		}else if(version == 3){ // new version
			coordR.setCoord(params[1], Integer.valueOf(params[2]), Integer.valueOf(params[3]), Integer.valueOf(params[4]), 0, false);
			delete = Boolean.valueOf(params[5]);
			protectExplo = Boolean.valueOf(params[6]);
			typeCon = Integer.valueOf(params[7]);
			nbItems = Integer.valueOf(params[8]);
			int size = Integer.valueOf(params[9]);
			invItemsPermanent.clear();
			invItemsRate.clear();
			int index = 10;
			for (int i = 0 ; i < size ; i++){
				InventoryStack invSt = new InventoryStack();
				invSt.item = new ItemStack(Material.getMaterial(params[index++]), Integer.valueOf(params[index++]), (short) 0, Byte.valueOf(params[index++]));
				invSt.rate = Double.valueOf(params[index++]);
				invSt.ratetmp = (int) (invSt.rate*100);
				invSt.randomAmount = Boolean.valueOf(params[index++]);
				invSt.unique = Boolean.valueOf(params[index++]);
				invSt.permanent = Boolean.valueOf(params[index++]);
				invSt.enchant = params[index++];
				invSt.enchantLvl = Integer.valueOf(params[index++]);
				if(invSt.item.getAmount() <= 0) invSt.item.setAmount(1);
				if(invSt.permanent) invItemsPermanent.add(invSt);
				else invItemsRate.add(invSt);
			}
			version = 4;
			sqlSave();
		}else if(version ==4){ // new version
			int index = coordR.loadParams(1, params);
			delete = Boolean.valueOf(params[index++]);
			protectExplo = Boolean.valueOf(params[index++]);
			typeCon = Integer.valueOf(params[index++]);
			nbItems = Integer.valueOf(params[index++]);
			int size = Integer.valueOf(params[index++]);
			invItemsPermanent.clear();
			invItemsRate.clear();
			for (int i = 0 ; i < size ; i++){
				InventoryStack invSt = new InventoryStack();
				invSt.item = new ItemStack(Material.getMaterial(params[index++]), Integer.valueOf(params[index++]), (short) 0, Byte.valueOf(params[index++]));
				invSt.rate = Double.valueOf(params[index++]);
				invSt.ratetmp = (int) (invSt.rate*100);
				invSt.randomAmount = Boolean.valueOf(params[index++]);
				invSt.unique = Boolean.valueOf(params[index++]);
				invSt.permanent = Boolean.valueOf(params[index++]);
				invSt.enchant = params[index++];
				invSt.enchantLvl = Integer.valueOf(params[index++]);
				if(invSt.item.getAmount() <= 0) invSt.item.setAmount(1);
				if(invSt.permanent) invItemsPermanent.add(invSt);
				else invItemsRate.add(invSt);
			}
		}
	}

	public String getIdMat(){
		if(typeCon == CHEST) return Material.CHEST.getKey().toString();
		if(typeCon == DISPENSER) return Material.DISPENSER.getKey().toString();
		if(typeCon == DROPPER) return Material.DROPPER.getKey().toString();
		return Material.AIR.getKey().toString();
	}
	
	class InventoryStack implements Comparable<InventoryStack>{
		public ItemStack item;
		public double rate = 0;
		public boolean randomAmount = false;
		public boolean unique = true;
		public boolean permanent = true;
		public String enchant = "";
		public int enchantLvl = 0;
		
		public int ratetmp;
		
		public ItemStack getItem(){
			if(enchant.equals("") && enchantLvl > 0){
				Enchantment en = Enchantment.getByKey(NamespacedKey.minecraft(enchant));
				if(en.canEnchantItem(item) && enchantLvl >= en.getStartLevel() && enchantLvl <= en.getMaxLevel())
					item.addEnchantment(en, enchantLvl);
			}
			if(randomAmount){
				ItemStack itemtmp = new ItemStack(item);
				itemtmp.setAmount(new Random().nextInt(item.getAmount())+1);
				return itemtmp;
			}else
				return item;
		}

		@Override
		public int compareTo(InventoryStack o) {
			if(ratetmp < o.ratetmp) return -1;
			if(ratetmp > o.ratetmp) return 1;
			return 0;
			
		}
	}

	public static void onEntityExplodeEvent(EntityExplodeEvent event) {
		if(!protectExploChests.isEmpty()){
			Location loc = event.getLocation();
			for(ActionChest ac : protectExploChests){
				if(ac.coordR.getWorld() != null && ac.coordR.getWorld().getUID() == loc.getWorld().getUID()){
					String type = ac.getIdMat();
					for(Block b : event.blockList()){
						if(b.getType().getKey().toString() == type && b.getX() == ac.coordR.getX() && b.getY() == ac.coordR.getY() && b.getZ() == ac.coordR.getZ()){
							event.setCancelled(true);
							return;
						}
					}
				}
			}
		}
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateChest(this, mc, driver); }
	// ------------------ StateProtectExplo ------------------
	class StateChest extends AbsMechaStateEntitySave{

		public StateChest(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void start() {
			super.start();
			if(coordR.getWorld() != null){
				Location loc = coordR.getLocationRandomRadius(driver);
				if(loc != null){
					Block b = coordR.getWorld().getBlockAt(loc);
					if(b != null){
						
						protectectExplo(driver);
						
						Inventory invChest = null;
						if(typeCon == CHEST){
							if(b.getType() != Material.CHEST){
								b.setType(Material.AIR);
								b.setType(Material.CHEST);
							}
							Chest chest = (Chest) b.getState();
							invChest = chest.getBlockInventory();
						}else if(typeCon == DISPENSER){
							if(b.getType() != Material.DISPENSER){
								b.setType(Material.AIR);
								b.setType(Material.DISPENSER);
							}
							Dispenser disp = (Dispenser) b.getState();
							invChest = disp.getInventory();
						}else if(typeCon == DROPPER){
							if(b.getType() != Material.DROPPER){
								b.setType(Material.AIR);
								b.setType(Material.DROPPER);
							}
							Dropper drop = (Dropper) b.getState();
							invChest = drop.getInventory();
						}
						invChest.clear();
						if(delete) b.setType(Material.AIR);
						else{
							Random rand = new Random();
							int nbItemstmp = nbItems;
							
							List<InventoryStack> invItemsPermanenttmp = new ArrayList<InventoryStack>(invItemsPermanent);
							int size = invItemsPermanenttmp.size();
							for(int i = 0 ; i < size ; i++){
								if(nbItemstmp <= 0 || invItemsPermanenttmp.isEmpty()) break;
								int nbrand = rand.nextInt(invItemsPermanenttmp.size());
								InventoryStack invSt = invItemsPermanenttmp.get(nbrand);
								invChest.addItem(invSt.getItem());
								if(invSt.unique) invItemsPermanenttmp.remove(invSt);
								nbItemstmp--;
							}
							
							List<InventoryStack> invItemstmp = new ArrayList<InventoryStack>(invItemsRate);
							for(int i = 0 ; i < nbItemstmp ; i++){
								int pre = 0;
								for(InventoryStack invSt : invItemstmp){
									invSt.ratetmp = (int) ((invSt.rate*100)+pre);
									pre = invSt.ratetmp;
								}
								if(invItemstmp.isEmpty()) break;						
								int raterand = rand.nextInt(10000);
								int index = 0;
								boolean unique = false;
								for(InventoryStack invSt : invItemstmp){
									if(raterand < invSt.ratetmp){
										invChest.addItem(invSt.getItem());
										unique = invSt.unique;
										break;
									}
									index++;
								}
								if(unique) invItemstmp.remove(index);						
							}	
						}
					}
				}
			}
			launchMessage();
			stop();
		}
		@Override
		public void stop() {
			super.stop();
			if(protectExplo) setActive(true);
			if(!hasCurrentStatesActive()) protectExploChests.remove(ActionChest.this);
		}
	}
}