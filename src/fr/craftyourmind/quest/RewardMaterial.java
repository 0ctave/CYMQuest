package fr.craftyourmind.quest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class RewardMaterial extends AbsReward{

	public String idMat = "minecraft:air";
	public byte data = 0;
	public String displayName = "";
	
	public RewardMaterial(Quest q) { super(q); }
	@Override
	public IStateRew getState(StateQuestPlayer sqp) { return new StateMaterial(sqp); }
	@Override
	public int getType() { return MATERIAL; }
	@Override
	public String getStrType() { return STRMATERIAL; }
	@Override
	public String getParams() { return new StringBuilder("2").append(DELIMITER).append(idMat).append(DELIMITER).append(displayName).append(DELIMITER).append(data).toString(); }
	@Override
	public String getParamsGUI() {
		Material mat = Material.getMaterial(idMat);
		if(mat == null) return ""; else return mat.getKey().toString();
	}
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 2){
			idMat = params[0];
			data = Byte.valueOf(params[1]);
		}else{
			int version = Integer.valueOf(params[0]);
			idMat = params[1];
			displayName = params[2];
			data = Byte.valueOf(params[3]);
		}
	}
	@Override
	public String getIdItem() { return idMat; }
	@Override
	public byte getDataItem() { return data; }
	
	// ---------------- STATEMATERIAL ----------------
	class StateMaterial extends StateRewPlayer{

		public StateMaterial(StateQuestPlayer sqp) { super(sqp); }
		@Override
		public void give() {
			Material m = Material.matchMaterial(idMat);


			if(m != null && sqp.qp.getPlayer() != null){

				int nb = amount;
				ItemStack[] contents = sqp.qp.getPlayer().getInventory().getContents();
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
								//contents[i] = new ItemStack(m, nb, plop, data);
								ItemStack is = new ItemStack(m, nb);
								contents[i] = is;

								ItemMeta im = contents[i].getItemMeta();
								im.setDisplayName(displayName);
								contents[i].setItemMeta(im);
								break;
							}else if(nb > m.getMaxStackSize()){
								//contents[i] = new ItemStack(m, m.getMaxStackSize(), plop, data);

								ItemStack is = new ItemStack(m, m.getMaxStackSize());
								contents[i] = is;

								ItemMeta im = contents[i].getItemMeta();
								im.setDisplayName(displayName);
								contents[i].setItemMeta(im);
								nb -= m.getMaxStackSize();
							}
						}
					}
				}
				sqp.qp.getPlayer().getInventory().setContents(contents);
				messageGive(sqp.qp);
			}
		}
	}
}