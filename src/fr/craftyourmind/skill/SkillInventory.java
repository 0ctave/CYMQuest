package fr.craftyourmind.skill;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import fr.craftyourmind.quest.Plugin;
import fr.craftyourmind.quest.QuestPlayer;

public class SkillInventory {

	public static final String titleInventory = ChatColor.DARK_RED+""+ChatColor.BOLD+"Skills Inventory";
	private static final String skillbarFolder = "skillinventory";
	private static final String skillInventorySection = "skill_inventory";
	private static final String hotbarSection = "hotbar";
	private static final String barMode = "bar_mode";
	public static final int lenghtHotbar = 9;
	
	public static Inventory createInventory() {
		return Bukkit.createInventory(null, 54, SkillInventory.titleInventory);
	}
	
	public static void open(Player p) {
		QuestPlayer qp = QuestPlayer.get(p);
		p.openInventory(getInventory(qp)); 
		qp.contentsBeforeOpenSkillInv = p.getInventory().getContents();
	}
	
	public static void open(QuestPlayer qpSource, QuestPlayer qpTarget) {
		if(qpSource != qpTarget) qpSource.openOtherInv= qpTarget;
		qpSource.getPlayer().openInventory(getInventory(qpTarget));
	}
	
	public static Inventory getInventory(QuestPlayer qp) {
		if(qp.getPlayer() == null || !qp.getPlayer().hasPlayedBefore())
			load(qp);
		if(qp.skillBar && qp.getPlayer() != null){
			ItemStack[] skillContents = qp.skillInventory.getContents();
			ItemStack[] contents = qp.getPlayer().getInventory().getContents();
			for(int i = 0 ; i < lenghtHotbar ; i++)
				if(skillContents[i] == null) qp.hotbarCache[i] = contents[i]; // update
		}
		return qp.skillInventory;
	}
	
	public static void sendLogin(QuestPlayer qp) {
		GUISkillScreen.sendBarMode(qp.getPlayer(), qp.skillBar);
	}
	
	public static void changeBarMode(QuestPlayer qp, boolean skillBar) {
		qp.skillBar = skillBar;
		if(skillBar) changeToSkillBar(qp); else restoreHotBar(qp);
	}
	
	private static void changeToSkillBar(QuestPlayer qp) {
		saveHotBar(qp);
		ItemStack[] skillContents = qp.skillInventory.getContents();
		ItemStack[] contents = qp.getPlayer().getInventory().getContents();
		for(int i = 0 ; i < lenghtHotbar ; i++)
			if(skillContents[i] != null) contents[i] = skillContents[i];
		qp.getPlayer().getInventory().setContents(contents);
	}
	
	private static void saveHotBar(QuestPlayer qp) {
		try{
			File f = getFile(qp);
			f.createNewFile();
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
			conf.set(barMode, qp.skillBar);
			ConfigurationSection confHot = conf.createSection(hotbarSection);
			ItemStack[] contents = qp.getPlayer().getInventory().getContents();
			for(int i = 0 ; i < lenghtHotbar ; i++){
				qp.hotbarCache[i] = contents[i];
				confHot.set("bar"+i, contents[i]);
			}
			conf.save(f);
		} catch (Exception e) { 
			Plugin.log("Error save hot bar "+qp.getName()+" : "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static void saveHotBarCache(QuestPlayer qp) {
		try{
			File f = getFile(qp);
			f.createNewFile();
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
			conf.set(barMode, qp.skillBar);
			ConfigurationSection confHot = conf.createSection(hotbarSection);
			for(int i = 0 ; i < lenghtHotbar ; i++)
				confHot.set("bar"+i, qp.hotbarCache[i]);
			conf.save(f);
		} catch (Exception e) { 
			Plugin.log("Error save hot bar cache "+qp.getName()+" : "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static void restoreHotBar(QuestPlayer qp) {
		try{
			File f = getFile(qp);
			if(!f.createNewFile()){
				YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
				conf.set(barMode, qp.skillBar);
				conf.save(f);
				ItemStack[] skillContents = qp.skillInventory.getContents();
				ItemStack[] contents = qp.getPlayer().getInventory().getContents();
				for(int i = 0 ; i < lenghtHotbar ; i++)
					if(skillContents[i] != null)
						contents[i] = qp.hotbarCache[i];
				qp.getPlayer().getInventory().setContents(contents);
			}
		} catch (Exception e) { 
			Plugin.log("Error restore hot bar "+qp.getName()+" : "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void save(QuestPlayer qp) {
		try{
			File f = getFile(qp);
			f.createNewFile();
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
			conf.set(barMode, qp.skillBar);
			ConfigurationSection confInv = conf.createSection(skillInventorySection);
			ItemStack[] skillContents = qp.skillInventory.getContents();
			for(int i = 0 ; i < skillContents.length ; i++)
				confInv.set("slot"+i, skillContents[i]);
			conf.save(f);
			if(qp.skillBar && qp.getPlayer() != null){
				ItemStack[] contents = qp.getPlayer().getInventory().getContents();
				for(int i = 0 ; i < lenghtHotbar ; i++)
					if(skillContents[i] == null) contents[i] = qp.hotbarCache[i];
					else contents[i] = skillContents[i];
				qp.getPlayer().getInventory().setContents(contents);
				
			}
		} catch (Exception e) { 
			Plugin.log("Error save skill inventory "+qp.getName()+" : "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static void load(final QuestPlayer qp) {
		try{
			File f = getFile(qp);
			if(f.createNewFile()){
				save(qp);
			}else{
				YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
				qp.skillBar = conf.getBoolean(barMode, qp.skillBar);
				ConfigurationSection confInv = conf.getConfigurationSection(skillInventorySection);
				if(confInv != null){
					ItemStack[] skillContents = qp.skillInventory.getContents();
					for(int i = 0 ; i < skillContents.length ; i++)
						skillContents[i] = confInv.getItemStack("slot"+i);
					qp.skillInventory.setContents(skillContents);
				}
				if(qp.skillBar && qp.getPlayer() != null){ // init hotbarTmp
					final ConfigurationSection confHot = conf.getConfigurationSection(hotbarSection);
					if(confHot != null){
						Bukkit.getScheduler().runTask(Plugin.it, new Runnable() { // bug on login
							@Override
							public void run() {
								ItemStack[] skillContents = qp.skillInventory.getContents();
								ItemStack[] contents = qp.getPlayer().getInventory().getContents();
								for(int i = 0 ; i < lenghtHotbar ; i++){
									qp.hotbarCache[i] = confHot.getItemStack("bar"+i); // update
									if(skillContents[i] == null) contents[i] = qp.hotbarCache[i];
									else contents[i] = skillContents[i];
								}
								qp.getPlayer().getInventory().setContents(contents);
							}
						});
					}
				}
			}
		} catch (Exception e) { 
			Plugin.log("Error load skill inventory "+qp.getName()+" : "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static File getFile(QuestPlayer qp){
		File dir = getSkillInventoryFolder();
		return new File(dir, qp.getName()+".yml");
	}
	
	public static File getSkillInventoryFolder(){
		File d = Plugin.it.getDataFolder();
		d.mkdir();
		File SkillInvFolder = new File(d, skillbarFolder);
		SkillInvFolder.mkdir();
		return SkillInvFolder;
	}

	public static void onInventoryCloseEvent(InventoryCloseEvent event) {
		if(titleInventory.equals(event.getInventory().getType().getDefaultTitle())){
			final QuestPlayer qp = QuestPlayer.get(event.getPlayer());
			if(qp.openOtherInv != null){ 
				save(qp.openOtherInv);
				qp.openOtherInv = null;
			}
			if(qp.contentsBeforeOpenSkillInv != null){ // bug replace client
				Bukkit.getScheduler().runTask(Plugin.it, new Runnable() {
					@Override
					public void run() {
						if(qp.contentsBeforeOpenSkillInv != null){
						ItemStack[] contents = qp.contentsBeforeOpenSkillInv;
						if(qp.getPlayer().hasPermission("cymquest.skilledit"))
							contents = qp.getPlayer().getInventory().getContents();
						if(qp.skillBar){
							ItemStack[] skillContents = qp.skillInventory.getContents();
							for(int i = 0 ; i < lenghtHotbar ; i++)
								if(skillContents[i] == null) contents[i] = qp.hotbarCache[i];
								else contents[i] = skillContents[i];
						}
							qp.getPlayer().getInventory().setContents(contents);
							qp.contentsBeforeOpenSkillInv = null;
							qp.getPlayer().updateInventory();
							save(qp);
							saveHotBarCache(qp);
						}
					}
				});
			}
		}
	}

	public static void onInventoryClickEvent(InventoryClickEvent event) {
		QuestPlayer qp = QuestPlayer.get(event.getWhoClicked());
		if(titleInventory.equals(event.getInventory().getType().getDefaultTitle())){
			boolean cancel = false;
			if(event.getWhoClicked().hasPermission("cymquest.skilledit")) cancel = event.getRawSlot() >= event.getView().getTopInventory().getSize() + event.getView().getBottomInventory().getSize() - lenghtHotbar;
			else cancel = event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize();
			if(cancel){
				event.setCancelled(true);
				event.setResult(Result.DENY);
				qp.getPlayer().updateInventory();
				if(event.getRawSlot() < 0) event.getView().setCursor(null);
				else if(event.getAction() == InventoryAction.PLACE_ALL || event.getAction() == InventoryAction.PLACE_ONE || event.getAction() == InventoryAction.PLACE_SOME){
					qp.skillInventory.addItem(event.getCursor());
					event.getView().setCursor(null);
				}
			}
		}
	}

	public static void onPlayerDropItemEvent(PlayerDropItemEvent event) {
		if(QuestPlayer.get(event.getPlayer()).skillBar)
			event.setCancelled(true);
	}
	
	public static void onEntityDeath(EntityDeathEvent event) {
		if(event.getEntityType() == EntityType.PLAYER){
			QuestPlayer qp = QuestPlayer.get(event.getEntity());
			if(qp != null){
				if(qp.skillBar){
					ItemStack[] skillContents = qp.skillInventory.getContents();
					for(int i = 0 ; i < lenghtHotbar ; i++)
						event.getDrops().remove(skillContents[i]);
					SkillInventory.changeBarMode(qp, false);
					ItemStack[] contents = qp.getPlayer().getInventory().getContents();
					for(int i = 0 ; i < lenghtHotbar ; i++)
						if(skillContents[i] != null) event.getDrops().add(contents[i]);
				}
			}
		}
	}

	public static void onPlayerQuitEvent(PlayerQuitEvent event) {
		updateHotbarCache(QuestPlayer.get(event.getPlayer()));
	}

	public static void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		updateHotbarCache(QuestPlayer.get(event.getPlayer()));
	}
	
	private static void updateHotbarCache(QuestPlayer qp){
		if(qp != null){
			if(qp.skillBar){
				ItemStack[] skillContents = qp.skillInventory.getContents();
				ItemStack[] contents = qp.getPlayer().getInventory().getContents();
				for(int i = 0 ; i < lenghtHotbar ; i++)
					if(skillContents[i] == null) qp.hotbarCache[i] = contents[i];
				saveHotBarCache(qp);
			}
		}
	}
}