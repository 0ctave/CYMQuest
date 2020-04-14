package fr.craftyourmind.quest.event;

import fr.craftyourmind.quest.packet.DataQuestRew;
import fr.craftyourmind.manager.util.CYMEvent;

public class QuestRewEvent extends CYMEvent{

	public DataQuestRew data;
	
	public QuestRewEvent(DataQuestRew data) {
		this.data = data;
	}

}
