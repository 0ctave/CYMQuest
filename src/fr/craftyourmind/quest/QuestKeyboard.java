package fr.craftyourmind.quest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import fr.craftyourmind.quest.packet.DataQuestKeyboard;

public class QuestKeyboard {

	private static int defaultPlayerScreen = 78;
	private static String defaultPlayerScreenKey = "N";
	private static int defaultEventManager = 66;
	private static String defaultEventManagerKey = "B";
	private static int defaultSkill = 77;
	private static String defaultSkillKey = ",";
	private static int defaultSkillBar = 67;
	private static String defaultSkillBarKey = "C";

	private static Map<Player, QuestKeyboard> keys = new HashMap<Player, QuestKeyboard>();
	
	private int playerScreen;
	private String playerScreenKey;
	private int eventManager;
	private String eventManagerKey;
	private int skill;
	private String skillKey;
	private int skillBar;
	private String skillBarKey;
	
	public static void recieve(DataQuestKeyboard data) {
		if(data.isOPEN()){
			QuestKeyboard key = getKeyboard(data.getPlayer());
			data.playerScreen = key.playerScreen;
			data.playerScreenKey = key.playerScreenKey;
			data.eventManager = key.eventManager;
			data.eventManagerKey = key.eventManagerKey;
			data.skill = key.skill;
			data.skillKey = key.skillKey;
			data.skillBar = key.skillBar;
			data.skillBarKey = key.skillBarKey;
			data.send();
		}else if(data.isSAVE()){
			QuestKeyboard keys = getKeyboard(data.getPlayer());
			keys.playerScreen = data.playerScreen;
			keys.playerScreenKey = data.playerScreenKey;
			keys.eventManager = data.eventManager;
			keys.eventManagerKey = data.eventManagerKey;
			keys.skill = data.skill;
			keys.skillKey = data.skillKey;
			keys.skillBar = data.skillBar;
			keys.skillBarKey = data.skillBarKey;
			save(data.getPlayer(), keys);
			data.sendOPEN();
		}
	}

	public static void sendCONFIG(Player p) {
		QuestKeyboard key = getKeyboard(p);
		new DataQuestKeyboard().sendCONFIG(p, key.playerScreen, key.playerScreenKey, key.eventManager, key.eventManagerKey, key.skill, key.skillKey, key.skillBar, key.skillBarKey);
	}
	
	private static QuestKeyboard getKeyboard(Player p) {
		QuestKeyboard kb = keys.get(p);
		if(kb == null){
			kb = load(p);
			keys.put(p, kb);
		}
		return kb;
	}

	private static void save(Player p, QuestKeyboard keys) {
		try {
			File d = Plugin.it.getDataFolder();
			d.mkdir();
			File d2 = new File(d, "keyboards");
			d2.mkdir();
			File f = new File(d2, p.getName()+".yml");
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
			if(f.createNewFile()) load(p);
			else{
				conf.set("playerScreen", keys.playerScreen);
				conf.set("playerScreen_key", keys.playerScreenKey);
				conf.set("eventManager", keys.eventManager);
				conf.set("eventManager_key", keys.eventManagerKey);
				conf.set("skill", keys.skill);
				conf.set("skill_Key", keys.skillKey);
				conf.set("skill_bar", keys.skillBar);
				conf.set("skill_bar_Key", keys.skillBarKey);
				conf.save(f);
			}
		} catch (IOException e) {
			Plugin.log(e.getMessage());
		}
	}
	
	private static QuestKeyboard load(Player p) {
		try {
			File d = Plugin.it.getDataFolder();
			d.mkdir();
			File d2 = new File(d, "keyboards");
			d2.mkdir();
			File f = new File(d2, p.getName()+".yml");
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
			if(f.createNewFile()){
				conf.set("playerScreen", defaultPlayerScreen);
				conf.set("playerScreen_key", defaultPlayerScreenKey);
				conf.set("eventManager", defaultEventManager);
				conf.set("eventManager_key", defaultEventManagerKey);
				conf.set("skill", defaultSkill);
				conf.set("skill_Key", defaultSkillKey);
				conf.set("skill_bar", defaultSkillBar);
				conf.set("skill_bar_Key", defaultSkillBarKey);
				conf.save(f);
				return getDefault();
			}else{
				QuestKeyboard kb = new QuestKeyboard();
				kb.playerScreen = conf.getInt("playerScreen", defaultPlayerScreen);
				kb.playerScreenKey = conf.getString("playerScreen_key", defaultPlayerScreenKey);
				kb.eventManager = conf.getInt("eventManager", defaultEventManager);
				kb.eventManagerKey = conf.getString("eventManager_key", defaultEventManagerKey);
				kb.skill = conf.getInt("skill", defaultSkill);
				kb.skillKey = conf.getString("skill_Key", defaultSkillKey);
				kb.skillBar = conf.getInt("skill_bar", defaultSkillBar);
				kb.skillBarKey = conf.getString("skill_bar_Key", defaultSkillBarKey);
				return kb;
			}
		} catch (IOException e) {
			Plugin.log(e.getMessage());
			return getDefault();
		}
	}
	
	private static QuestKeyboard getDefault(){
		QuestKeyboard kb = new QuestKeyboard();
		kb.playerScreen = defaultPlayerScreen;
		kb.playerScreenKey = defaultPlayerScreenKey;
		kb.eventManager = defaultEventManager;
		kb.eventManagerKey = defaultEventManagerKey;
		kb.skill = defaultSkill;
		kb.skillKey = defaultSkillKey;
		kb.skillBar = defaultSkillBar;
		kb.skillBarKey = defaultSkillBarKey;
		return kb;
	}
	
	public static void loadDefault() {
		try {
			File d = Plugin.it.getDataFolder();
			d.mkdir();
			File f = new File(d, "keyboards.yml");
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
			if(f.createNewFile()){
				conf.set("playerScreen", defaultPlayerScreen);
				conf.set("playerScreen_key", defaultPlayerScreenKey);
				conf.set("eventManager", defaultEventManager);
				conf.set("eventManager_key", defaultEventManagerKey);
				conf.set("skill", defaultSkill);
				conf.set("skill_Key", defaultSkillKey);
				conf.set("skill_bar", defaultSkillBar);
				conf.set("skill_bar_Key", defaultSkillBarKey);
				conf.save(f);
			}else{
				defaultPlayerScreen = conf.getInt("playerScreen", defaultPlayerScreen);
				defaultPlayerScreenKey = conf.getString("playerScreen_key", defaultPlayerScreenKey);
				defaultEventManager = conf.getInt("eventManager", defaultEventManager);
				defaultEventManagerKey = conf.getString("eventManager_key", defaultEventManagerKey);
				defaultSkill = conf.getInt("skill", defaultSkill);
				defaultSkillKey = conf.getString("skill_Key", defaultSkillKey);
				defaultSkillBar = conf.getInt("skill_bar", defaultSkillBar);
				defaultSkillBarKey = conf.getString("skill_bar_Key", defaultSkillBarKey);
			}
		} catch (IOException e) {
			Plugin.log("Error load default keyboards");
		}
	}
}