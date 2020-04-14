package fr.craftyourmind.quest.event;

import fr.craftyourmind.quest.packet.DataQuestModScreen;
import fr.craftyourmind.manager.util.CYMEvent;

public class QuestModScreenEvent extends CYMEvent{

	public DataQuestModScreen data;
	
	public QuestModScreenEvent(DataQuestModScreen data) {
		this.data = data;
	}

	
}
