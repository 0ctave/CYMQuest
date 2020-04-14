package fr.craftyourmind.quest.event;

import fr.craftyourmind.quest.packet.DataQuest;
import fr.craftyourmind.manager.util.CYMEvent;

public class QuestEvent extends CYMEvent{

	public DataQuest data;
	
	public QuestEvent(DataQuest data) {
		this.data = data;
	}
	
}
