package fr.craftyourmind.quest.event;

import fr.craftyourmind.quest.packet.DataQuestObj;
import fr.craftyourmind.manager.util.CYMEvent;

public class QuestObjOpenEvent extends CYMEvent{

	public DataQuestObj data;
	
	public QuestObjOpenEvent(DataQuestObj data) {
		this.data = data;
	}
	
}
