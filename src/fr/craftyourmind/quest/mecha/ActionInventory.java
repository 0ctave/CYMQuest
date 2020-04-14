package fr.craftyourmind.quest.mecha;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import fr.craftyourmind.manager.util.CYMMetadataValue;
import fr.craftyourmind.quest.Plugin;
import fr.craftyourmind.quest.mecha.MechaRandom.RandomData;
import fr.craftyourmind.skill.SkillInventory;

public class ActionInventory extends AbsMechaList{

	private static final int PLAYER = 1;
	private static final int CHEST = 2;
	private static final int DISPENSER = 3;
	private static final int DROPPER = 4;
	private static final int HOPPER = 5;
	private static final int DROPITEM = 6;
	
	private static Map<Integer, Class<? extends IMechaList>> params = new HashMap<Integer, Class<? extends IMechaList>>();
	static{
		params.put(PLAYER, PLAYER.class);
		params.put(CHEST, CHEST.class);
		params.put(DISPENSER, DISPENSER.class);
		params.put(DROPPER, DISPENSER.class);
		params.put(HOPPER, HOPPER.class);
		params.put(DROPITEM, DROPITEM.class);
	}
	
	private static final int GIVE = 1;
	private static final int PICKUP = 2;
	private static final int CLEAR = 3;
	
	public static List<ContainerBlock> protectExploChests = new ArrayList<ContainerBlock>();
	
	private MechaRandom<InventoryStack> mRand = new MechaRandom<InventoryStack>();
	private List<InventoryStack> invList = new ArrayList<InventoryStack>();
	private int action = GIVE;
	private boolean clearBefore, pickupBefore;
	
	@Override
	public Map<Integer, Class<? extends IMechaList>> getMechaParam() { return params; }
	@Override
	public boolean isMechaStoppable() { return true; }
	@Override
	public int getType() { return MechaType.ACTINVENTORY; }
	@Override
	protected String getStringParams() {
		StringBuilder params = new StringBuilder().append(invList.size());
		for(InventoryStack invSt: invList)
			params.append(DELIMITER).append(invSt.idItem).append(DELIMITER).append(invSt.amount).append(DELIMITER).append(invSt.data).append(DELIMITER).append(invSt.randomAmount).append(DELIMITER).append(invSt.enchant).append(DELIMITER).append(invSt.enchantLvl).append(DELIMITER).append(invSt.displayName).append(DELIMITER).append(invSt.getRandomData().getParams());
		return new StringBuilder("4").append(DELIMITER).append(action).append(DELIMITER).append(clearBefore).append(DELIMITER).append(pickupBefore).append(DELIMITER).append(mRand.getParams()).append(DELIMITER).append(params).toString();
	}
	@Override
	public String getParamsGUI() {
		StringBuilder params = new StringBuilder();
		for(InventoryStack invSt : invList)
			params.append(invSt.item.getType().getKey().toString()).append(DELIMITER);
		return params.toString();
	}
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 5){
			InventoryStack invSt = new InventoryStack(mRand);
			invSt.amount.load(params[0]);
			invSt.idItem.load(params[1]);
			invSt.data.load(params[2]);
			boolean give = Boolean.valueOf(params[3]);
			boolean pickup = Boolean.valueOf(params[4]);
			invSt.item = new ItemStack(Material.getMaterial(invSt.idItem.get()), invSt.amount.get(), (short) 0, invSt.data.get().byteValue());
			if(give) action = GIVE; else if(pickup) action = PICKUP;
			if(invSt.item.getAmount() <= 0) invSt.item.setAmount(1);
			invList.add(invSt);
			mRand.add(invSt);
			getParam(PLAYER);
			sqlSave();
		}else{
			int version = Integer.valueOf(params[0]);
			if(version == 2){
				InventoryStack invSt = new InventoryStack(mRand);
				invSt.amount.load(params[1]);
				invSt.idItem.load(params[2]);
				invSt.data.load(params[3]);
				boolean give = Boolean.valueOf(params[4]);
				String displayName = params[5];
				boolean pickup = Boolean.valueOf(params[6]);
				invSt.item = new ItemStack(Material.getMaterial(invSt.idItem.get()), invSt.amount.get(), (short) 0, invSt.data.get().byteValue());
				invSt.displayName.load(displayName);
				if(give) action = GIVE; else if(pickup) action = PICKUP;
				if(invSt.item.getAmount() <= 0) invSt.item.setAmount(1);
				invList.add(invSt);
				mRand.add(invSt);
				IMechaList p = getParam(PLAYER);
				sqlSave();
			}else
				super.loadParams(params);
		}		
	}
	@Override
	protected int loadParams(int index, String[] params) {
		int version = Integer.valueOf(params[index++]);
		if(version >= 3){
			action = Integer.valueOf(params[index++]);
			clearBefore = Boolean.valueOf(params[index++]);
			if(version == 4) pickupBefore = Boolean.valueOf(params[index++]);
			index = mRand.load(index, params);
			invList.clear();
			mRand.clear();
			int size = Integer.valueOf(params[index++]);
			for (int i = 0 ; i < size ; i++){
				InventoryStack invSt = new InventoryStack(mRand);
				invSt.idItem.load(params[index++]);
				invSt.amount.load(params[index++]);
				invSt.data.load(params[index++]);
				invSt.randomAmount = Boolean.valueOf(params[index++]);
				invSt.enchant.load(params[index++]);
				invSt.enchantLvl.load(params[index++]);
				invSt.displayName.load(params[index++]);
				index = invSt.getRandomData().load(index, params);
				invSt.item = new ItemStack(Material.getMaterial(invSt.idItem.get()), invSt.amount.get(), (short) 0, invSt.data.get().byteValue());
				if(invSt.item.getAmount() <= 0) invSt.item.setAmount(1);
				invList.add(invSt);
				mRand.add(invSt);
			}
		}
		return index;
	}	
	
	public static void onEntityExplodeEvent(EntityExplodeEvent event) {
		if(!protectExploChests.isEmpty()){
			List<Block> list = new ArrayList<Block>();
			for(Block b : event.blockList()){
				if(b.hasMetadata("protectectExplo")) list.add(b);
			}
			event.blockList().removeAll(list);
		}
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateInv(this, mc, driver); };
	// ------------------ StateProtectExplo ------------------
	class StateInv extends AbsMechaStateEntityList2{
		
		private MechaRandom<InventoryStack> mRand = new MechaRandom<InventoryStack>();
		private List<InventoryStack> invList = new ArrayList<InventoryStack>();
		
		public StateInv(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void cloneData() {
			super.cloneData();
			invList.clear(); mRand.clear();
			mRand.clone(this, ActionInventory.this.mRand);
			for(InventoryStack is : ActionInventory.this.invList) invList.add(is.clone(this));
			for(InventoryStack is : invList) mRand.add(is);
		}
		@Override
		public void start() { if(getSelect().getId() != DROPITEM) state.start(this); else super.start(); }
		
		public void startInv(Inventory inv, int invSize){
			if(action == GIVE){
				if(clearBefore) inv.clear();
				List<InventoryStack> invList = mRand.getRandomList();
				if(pickupBefore) pickup(inv, invList);
				for(InventoryStack is : invList)
					inv.addItem(is.getItem());

			}else if(action == PICKUP){
				List<InventoryStack> invList = mRand.getRandomList();
				pickup(inv, invList);
				
			}if(action == CLEAR) inv.clear();
		}
		private void pickup(Inventory inv, List<InventoryStack> invList){
			for(InventoryStack is : invList){
				ItemStack item = is.getItem();
				Material mat = item.getType();
				String displayName = is.displayName.get();
				int data = item.getData().getData();
				int amount = item.getAmount();
				int nb = 0;
				int prenb = 0;
				ItemStack[] contents = inv.getContents();
				for(int i = 0 ; i < contents.length ; i++){
					if(contents[i] != null && contents[i].getType() == mat && (data == -1 || contents[i].getData().getData() == data)){
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
				inv.setContents(contents);
			}
		}
	}
	// ************** InventoryStack **************
	class InventoryStack implements RandomElement{
		public StringData idItem = new StringData();
		public IntegerData data = new IntegerData();
		public IntegerData amount = new IntegerData(1);
		public ItemStack item;
		public boolean randomAmount = false;
		public StringData enchant = new StringData();
		public IntegerData enchantLvl = new IntegerData();
		public StringData displayName = new StringData();
		
		public int ratetmp;
		private RandomData rdata;
		
		public InventoryStack(MechaRandom mr) { rdata = mr.newRandomData(); }
		public InventoryStack(RandomData rdata) { this.rdata = rdata; }
		
		public ItemStack getItem(){
			if(!enchant.get().equals("") && enchantLvl.get() > 0){
				Enchantment en = Enchantment.getByKey(NamespacedKey.minecraft(enchant.get()));
				if(en.canEnchantItem(item) && enchantLvl.get() >= en.getStartLevel() && enchantLvl.get() <= en.getMaxLevel())
					item.addEnchantment(en, enchantLvl.get());
			}
			ItemStack itemtmp = new ItemStack(item);
			String dn = displayName.get();
			if(!dn.isEmpty()){
				ItemMeta im = itemtmp.getItemMeta();
				im.setDisplayName(dn);
				itemtmp.setItemMeta(im);
			}
			if(randomAmount){
				itemtmp.setAmount(new Random().nextInt(item.getAmount())+1);
				return itemtmp;
			}else
				return itemtmp;
		}
		@Override
		public RandomData getRandomData() { return rdata; }
		@Override
		public void setRandomData(RandomData rd) { rdata = rd; }

		public InventoryStack clone(AbsMechaStateEntity mse) {
			InventoryStack is = new InventoryStack(rdata.clone(mse));
			is.idItem.clone(mse, idItem);
			is.data.clone(mse, data);
			is.amount.clone(mse, amount);
			is.item = new ItemStack(Material.getMaterial(is.idItem.get()), is.amount.get(), (short) 0, is.data.get().byteValue());
			is.randomAmount = randomAmount;
			is.enchant.clone(mse, enchant);
			is.enchantLvl.clone(mse, enchantLvl);
			is.displayName.clone(mse, displayName);
			return is;
		}
	}
	// ************** ContainerBlock **************
	abstract class ContainerBlock implements IMechaList{
		private List<Block> blocks = new ArrayList<Block>();
		private MechaCoordRelative coordR = new MechaCoordRelative();
		private boolean delete = false;
		private boolean protectExplo = false;
		public abstract int getInvSize();
		public abstract Material getMatType();
		@Override
		public String getParams() { return 1+DELIMITER+delete+DELIMITER+protectExplo+DELIMITER+coordR.getParams(); }
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			delete = Boolean.valueOf(params[index++]);
			protectExplo = Boolean.valueOf(params[index++]);
			coordR.loadParams(index, params);
			if(protectExplo){
				if(!protectExploChests.contains(this)) protectExploChests.add(this);
			}else protectExploChests.remove(this);
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateInv>() {
				private MechaCoordRelative coordR = new MechaCoordRelative();
				@Override
				public void cloneData(StateInv s) { coordR.clone(s, ContainerBlock.this.coordR); }
				@Override
				public void start(StateInv s) {
					IMechaDriver driver = s.driver;
					Location loc = coordR.getLocationRandomRadius(driver);
					if(loc != null){
						Block b = loc.getWorld().getBlockAt(loc);
						if(b != null){
							protectectExplo(s, b);
							if(b.getType() != getMatType()){
								b.setType(Material.AIR);
								b.setType(getMatType());
							}
							InventoryHolder invHolder = (InventoryHolder) b.getState();
							Inventory inv = invHolder.getInventory();
							if(delete){
								b.removeMetadata("protectectExplo", Plugin.it);
								blocks.remove(b);
								b.setType(Material.AIR);
							}else s.startInv(inv, getInvSize());
						}
					}
					s.launchMessage();
				}
				@Override
				public void stop(StateInv s) {
					if(protectExplo){
						s.sqlStop();
						if(!hasCurrentStatesActive()){
							for(Block b : blocks) b.removeMetadata("protectectExplo", Plugin.it);
							protectExploChests.remove(this);
							blocks.clear();
						}
					}
				}
			};
		}
		public void protectectExplo(StateInv s, Block b){
			if(protectExplo){
				s.sqlStart();
				s.setActive(true);
				b.setMetadata("protectectExplo", new CYMMetadataValue());
				if(!blocks.contains(b)) blocks.add(b);
				if(!protectExploChests.contains(this)) protectExploChests.add(this);
			}
		}
	}
	// ************** PLAYER **************
	class PLAYER implements IMechaList{
		private static final int PLAYERINV = 0;
		private static final int SKILLINV = 1;
		private static final int BOTHINV = 2;
		private int inventoryType = 0;
		@Override
		public int getId() { return PLAYER; }
		@Override
		public String getParams() { return "0"+DELIMITER+inventoryType; }
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			if(index < params.length){
				int version = Integer.valueOf(params[index++]);
				inventoryType = Integer.valueOf(params[index++]);
			}
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateInv>() {
				@Override
				public void cloneData(StateInv s) { }
				@Override
				public void start(StateInv s) {
					IMechaDriver driver = s.driver;
					if(driver.isPlayer()){
						Player p = driver.getPlayer();
						if(p != null){
							if(inventoryType == PLAYERINV || inventoryType == BOTHINV)
								s.startInv(p.getInventory(), InventoryType.PLAYER.getDefaultSize());
						}
						if(inventoryType == SKILLINV || inventoryType == BOTHINV){
							s.startInv(SkillInventory.getInventory(driver.getQuestPlayer()), InventoryType.PLAYER.getDefaultSize());
							SkillInventory.save(s.qp);
						}
					}
					s.launchMessage();
				}
				@Override
				public void stop(StateInv s) { }
			};
		}
	}
	// ************** CHEST **************
	class CHEST extends ContainerBlock{
		@Override
		public int getId() { return CHEST; }
		@Override
		public int getInvSize() { return InventoryType.CHEST.getDefaultSize(); }
		@Override
		public Material getMatType() { return Material.CHEST; }
	}
	// ************** DISPENSER **************
	class DISPENSER extends ContainerBlock{
		@Override
		public int getId() { return DISPENSER; }
		@Override
		public int getInvSize() { return InventoryType.DISPENSER.getDefaultSize(); }
		@Override
		public Material getMatType() { return Material.DISPENSER; }
	}
	// ************** DROPPER **************
	class DROPPER extends ContainerBlock{
		@Override
		public int getId() { return DROPPER; }
		@Override
		public int getInvSize() { return InventoryType.DROPPER.getDefaultSize(); }
		@Override
		public Material getMatType() { return Material.DROPPER; }
	}
	// ************** HOPPER **************
	class HOPPER extends ContainerBlock{
		@Override
		public int getId() { return HOPPER; }
		@Override
		public int getInvSize() { return InventoryType.HOPPER.getDefaultSize(); }
		@Override
		public Material getMatType() { return Material.HOPPER; }
	}
	// ************** DROPITEM **************
	class DROPITEM implements IMechaList{
		private MechaCoordRelative coordR = new MechaCoordRelative();
		private boolean onlyIfNoExist = true, cleanItems = true, commonItem = false;
		private MechaVelocity velocity = new MechaVelocity();
		private MechaDirectionRelative directR = new MechaDirectionRelative();
		@Override
		public int getId() { return DROPITEM; }
		@Override
		public String getParams() { return 2+DELIMITER+coordR.getParams()+DELIMITER+cleanItems; }
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			index = coordR.loadParams(index, params);
			if(version == 2) cleanItems = Boolean.valueOf(params[index++]);
		}
		private boolean isDead(List<Item> items){
			boolean goPop = true;
			if(items.isEmpty()) goPop = true;
			else{ for(Entity e : items){ if(!e.isDead() && e.isValid()){ goPop = false; break; } }
				if(goPop) items.clear();
			} return goPop;
		}
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateInv>() {
				private MechaCoordRelative coordR = new MechaCoordRelative();
				private List<Item> items = new ArrayList<Item>();
				@Override
				public void cloneData(StateInv s) {
					coordR.clone(s, DROPITEM.this.coordR);
					velocity.clone(s, DROPITEM.this.velocity);
					directR.clone(s, DROPITEM.this.directR);
				}
				@Override
				public void start(StateInv s) {
					items.clear();
					Location loc = coordR.getLocationRandomRadius(s.driver);
					if(loc != null){
						List<InventoryStack> invList = s.mRand.getRandomList();
						for(InventoryStack is : invList)
							items.add(loc.getWorld().dropItem(loc, is.getItem()));
						if(!invList.isEmpty()){
							if(cleanItems) s.setActive(true);
							s.launchMessage();
						}
					}
				}
				@Override
				public void stop(StateInv s) {
					if(cleanItems) for(Item item : items) item.remove();
					items.clear();
				}
			};
		}
	}
}