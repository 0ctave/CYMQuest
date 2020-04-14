package fr.craftyourmind.quest.event;

import fr.craftyourmind.quest.packet.DataQuestRew;
import fr.craftyourmind.manager.util.CYMEvent;

public class QuestRewOpenEvent extends CYMEvent{

	public DataQuestRew data;
	
	public QuestRewOpenEvent(DataQuestRew data) {
		this.data = data;
	}
	
}
