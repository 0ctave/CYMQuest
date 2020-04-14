package fr.craftyourmind.quest;

import java.util.ArrayList;
import java.util.List;

import fr.craftyourmind.quest.sql.QuestSQLManager;

public class QuestTag{

	private static List<QuestTag> list = new ArrayList<QuestTag>();
	
	public int id;
	public String name = "";
	public boolean hidden = true;
	public List<Quest> quests = new ArrayList<Quest>();
	
	public QuestTag(int id) {
		this.id = id;
		list.add(this);
	}
	
	public void tag(Quest q) {
		quests.add(q);
		QuestSQLManager.createState(this, q);
	}
	
	public void detag(Quest q) {
		quests.remove(q);
		QuestSQLManager.deleteState(this, q);
	}	
	
	public void create(){
		QuestSQLManager.create(this);
	}
	
	public void save(){
		QuestSQLManager.save(this);
	}
	
	public void delete(){
		list.remove(this);
		for(Quest q : quests) q.tags.remove(this);
		QuestSQLManager.delete(this);
	}
	
	public static String getTags(){
		StringBuilder sb = new StringBuilder().append(list.size());
		for(QuestTag qt : list)
			sb.append(QuestTools.DELIMITER).append(qt.id)
			.append(QuestTools.DELIMITER).append(qt.name)
			.append(QuestTools.DELIMITER).append(qt.hidden);
		return sb.toString();
	}

	public static void update(int idTag, String nameTag, boolean hidden) {
		if(idTag == 0){
			QuestTag qt = new QuestTag(0);
			qt.name = nameTag;
			qt.hidden = hidden;
			qt.create();
		}else{
			QuestTag qt = get(idTag);
			if(qt != null){
				qt.name = nameTag;
				qt.hidden = hidden;
				qt.save();
			}
		}
	}

	public static void remove(int idTag) {
		QuestTag qt = get(idTag);
		if(qt != null) qt.delete();
	}
	
	public static QuestTag get(int id){
		for(QuestTag qt : list) if(qt.id == id) return qt;
		return null;
	}
}