package fr.craftyourmind.quest.event;

import fr.craftyourmind.quest.packet.DataQuestScreen;
import fr.craftyourmind.manager.util.CYMEvent;

public class QuestScreenEvent extends CYMEvent{

	public DataQuestScreen data;
	
	public QuestScreenEvent(DataQuestScreen data) {
		this.data = data;
	}
}
