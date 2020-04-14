package fr.craftyourmind.quest.event;

import fr.craftyourmind.quest.packet.DataQuestObj;
import fr.craftyourmind.manager.util.CYMEvent;

public class QuestObjEvent extends CYMEvent{

	public DataQuestObj data;
	
	public QuestObjEvent(DataQuestObj data) {
		this.data = data;
	}

}
