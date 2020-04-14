package fr.craftyourmind.quest.mecha;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class ActionQuestBook extends Mechanism{

	public IntegerData idnpc = new IntegerData();
	public StringData displayName = new StringData();
	public StringData lore = new StringData();
	public boolean onlyPlayer, delete;
	public boolean singleCopy = true;
	
	@Override
	public int getType() { return MechaType.ACTQUESTBOOK; }
	@Override
	public String getParams() {
		return new StringBuilder().append(idnpc).append(DELIMITER).append(displayName).append(DELIMITER).append(lore).append(DELIMITER).append(onlyPlayer).append(DELIMITER).append(delete).append(DELIMITER).append(singleCopy).toString();
	}
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 5){
			idnpc.load(params[0]);
			displayName.load(params[1]);
			lore.load(params[2]);
			onlyPlayer = Boolean.valueOf(params[3]);
			delete = Boolean.valueOf(params[4]);
			sqlSave();
		}else if(params.length == 6){
			idnpc.load(params[0]);
			displayName.load(params[1]);
			lore.load(params[2]);
			onlyPlayer = Boolean.valueOf(params[3]);
			delete = Boolean.valueOf(params[4]);
			singleCopy = Boolean.valueOf(params[5]);
		}
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateBook(this, mc, driver); }
	// ------------------ StateBook ------------------
	class StateBook extends AbsMechaStateEntity{

		public IntegerData idnpc = new IntegerData();
		public StringData displayName = new StringData();
		public StringData lore = new StringData();
		
		public StateBook(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void cloneData() {
			super.cloneData();
			idnpc.clone(this, ActionQuestBook.this.idnpc);
			displayName.clone(this, ActionQuestBook.this.displayName);
			lore.clone(this, ActionQuestBook.this.lore);
		}
		@Override
		public void start() {
			Player p = driver.getPlayer();
			if(p != null){
				ItemStack[] contents = p.getInventory().getContents();
				if(delete){
					for(int i = 0 ; i < contents.length ; i++){
						if(contents[i] != null){
							ItemStack is = contents[i];
							if(is.getType() == Material.WRITTEN_BOOK){
								BookMeta bm = (BookMeta)is.getItemMeta();
								if(bm.getAuthor().equals("quest book")){
									int idn = Integer.valueOf(bm.getPage(1));
									if(idn == idnpc.get()) contents[i] = null;
								}
							}
						}
					}
				}else{
					String dn = displayName.get();
					if(singleCopy){
						for(int i = 0 ; i < contents.length ; i++){
							if(contents[i] != null){
								ItemStack is = contents[i];
								if(is.getType() == Material.WRITTEN_BOOK){
									BookMeta bm = (BookMeta)is.getItemMeta();
									if(bm.getAuthor().equals("quest book") && bm.getDisplayName().equals(dn)){
										//p.sendMessage("Your are already this book !");
										return;
									}
								}
							}
						}
					}
					boolean add = false;
					for(int i = 0 ; i < contents.length ; i++){
						if(contents[i] == null){
							ItemStack is = contents[i] = new ItemStack(Material.WRITABLE_BOOK);
							BookMeta bm = (BookMeta)is.getItemMeta();
							bm.setDisplayName(dn);
							bm.setAuthor("quest book");
							List<String> lores = new ArrayList<String>(); lores.add(lore.get());
							if(onlyPlayer){
								bm.setPages(idnpc.get()+"", p.getName());
								lores.add("For "+p.getName());
							}else bm.setPages(idnpc.get()+"");
							bm.setLore(lores);
							is.setType(Material.WRITTEN_BOOK);
							is.setItemMeta(bm);
							add = true;
							break;
						}
					}
					if(!add) p.sendMessage("Inventory is full !");
				}
				p.getInventory().setContents(contents);
			}
			launchMessage();
		}
	}
}