package fr.craftyourmind.quest.event;

import fr.craftyourmind.manager.util.CYMEvent;
import fr.craftyourmind.quest.packet.DataQuestEventParadiseEdit;

public class QuestEventParadiseEditEvent extends CYMEvent{

	public DataQuestEventParadiseEdit data;
	
	public QuestEventParadiseEditEvent(DataQuestEventParadiseEdit data) {
		this.data = data;
	}
}
