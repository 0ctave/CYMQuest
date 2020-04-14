package fr.craftyourmind.quest.event;

import fr.craftyourmind.quest.packet.DataQuestEdit;
import fr.craftyourmind.manager.util.CYMEvent;

public class QuestEditEvent extends CYMEvent{
	
	public DataQuestEdit data;
	
	public QuestEditEvent(DataQuestEdit data) {
		this.data = data;
	}
	
}
