package fr.craftyourmind.quest.event;

import fr.craftyourmind.manager.util.CYMEvent;
import fr.craftyourmind.quest.packet.DataQuestEventParadise;

public class QuestEventParadiseEvent extends CYMEvent{

	public DataQuestEventParadise data;
	
	public QuestEventParadiseEvent(DataQuestEventParadise data) {
		this.data = data;
	}
}
