package fr.craftyourmind.quest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class QuestIcon {

	private static final String nameFolder = "icons";
	private static Map<String, QuestIcon> icons = new HashMap<String, QuestIcon>();
	private static List<QuestIcon> defaultIcons = new ArrayList<QuestIcon>();
	private static String allIcons = "";

	public static final int size = 6;
	public static final String none = "none";
	public static final String normal = "normal";
	public static final String taken = "taken";
	public static final String success = "success";
	public static final String repeatable = "repeatable";
	public static final String repute = "repute";
	public static final String cymclass = "class";
	
	private static QuestIcon qiNormal, qiTaken, qiSuccess, qiRepeatable, qiRepute, qiClass;
	
	public static QuestIcon getIcon(String nameIcon){
		QuestIcon qi = icons.get(nameIcon);
		if(qi == null) qi = new QuestIcon();
		 return qi;
	}
	
	public String name = none;
	public boolean visibleAllDirection = true;
	public List<MIDrawingQuads> quads = new ArrayList<MIDrawingQuads>();
	public QuestIcon() { }
	public QuestIcon(String name) { this.name = name; }
	public MIDrawingQuads newDrawingsQuads(){
		return new MIDrawingQuads();
	}
	
	public class MIDrawingQuads{
		public Float[] color = new Float[4], vertex1 = new Float[3], vertex2 = new Float[3], vertex3 = new Float[3], vertex4 = new Float[3];
	}

	public static void loadDefault() {
		try{
			File d = Plugin.it.getDataFolder();
			d.mkdir();
			File iconFolder = new File(d, nameFolder);
			iconFolder.mkdir();
			
			QuestTools.copyFile(nameFolder, normal+".yml");
			QuestTools.copyFile(nameFolder, normal+"3D.yml");
			QuestTools.copyFile(nameFolder, taken+".yml");
			QuestTools.copyFile(nameFolder, success+".yml");
			QuestTools.copyFile(nameFolder, success+"3D.yml");
			QuestTools.copyFile(nameFolder, repeatable+".yml");
			QuestTools.copyFile(nameFolder, repeatable+"3D.yml");
			QuestTools.copyFile(nameFolder, repute+".yml");
			QuestTools.copyFile(nameFolder, repute+"3D.yml");
			QuestTools.copyFile(nameFolder, cymclass+".yml");
			QuestTools.copyFile(nameFolder, cymclass+"3D.yml");
			QuestTools.copyFile(nameFolder, "skull.yml");
			
			icons.clear();
			defaultIcons.clear();
			allIcons = "0";
			for(File f : iconFolder.listFiles()){
				String name = f.getName().replaceFirst(".yml", "");
				QuestIcon qi = new QuestIcon(name);
				YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
				qi.visibleAllDirection = conf.getBoolean("visible_all_direction");
				int i = 1;
				ConfigurationSection confR = conf.getConfigurationSection("quad"+i);
				while(confR != null){
					MIDrawingQuads dq = qi.newDrawingsQuads(); 
					dq.color = confR.getFloatList("color").toArray(new Float[0]);
					dq.vertex1 = confR.getFloatList("vertex1").toArray(new Float[0]);
					dq.vertex2 = confR.getFloatList("vertex2").toArray(new Float[0]);
					dq.vertex3 = confR.getFloatList("vertex3").toArray(new Float[0]);
					dq.vertex4 = confR.getFloatList("vertex4").toArray(new Float[0]);
					qi.quads.add(dq);
					i++;
					confR = conf.getConfigurationSection("quad"+i);
				}
				icons.put(name, qi);
			}
			qiNormal = icons.get(normal);			
			qiTaken = icons.get(taken);			
			qiRepeatable = icons.get(repeatable);			
			qiRepute = icons.get(repute);			
			qiClass = icons.get(cymclass);
			qiSuccess = icons.get(success);			
			addDefaultIcons();
			
			allIcons = icons.size()+"";
			for(String name : icons.keySet()) allIcons += QuestTools.DELIMITER+name;
			
		} catch (Exception e) { 
			Plugin.log("Error load icons : "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static QuestIcon[] defaultIcons() {
		QuestIcon[] icons = new QuestIcon[defaultIcons.size()];
		int i = 0;
		for(QuestIcon qi : defaultIcons){
			icons[i] = qi;
			i++;
		}
		return icons;
	}
	
	public static String getAllIcons() {
		return allIcons;
	}
	
	private static void addDefaultIcons(){
		defaultIcons.add(qiNormal);
		defaultIcons.add(qiTaken);
		defaultIcons.add(qiSuccess);
		defaultIcons.add(qiRepeatable);
		defaultIcons.add(qiRepute);
		defaultIcons.add(qiClass);
	}
	
	public static QuestIcon[] setQuestIcons(Quest q, QuestIcon[] icons) {
		if(icons.length == 5){
			QuestIcon[] iconsUpdate = new QuestIcon[size];
			iconsUpdate[0] = icons[0];
			iconsUpdate[1] = qiTaken;
			iconsUpdate[2] = icons[1];
			iconsUpdate[3] = icons[2];
			iconsUpdate[4] = icons[3];
			iconsUpdate[5] = icons[4];
			icons = setQuestIcons(q, iconsUpdate);
		}else if(icons.length == size){
			q.normalIcon = icons[0].name;
			q.takenIcon = icons[1].name;
			q.successIcon = icons[2].name;
			q.repeatableIcon = icons[3].name;
			q.reputeIcon = icons[4].name;
			q.skillIcon = icons[5].name;
		}
		return icons;
	}
}