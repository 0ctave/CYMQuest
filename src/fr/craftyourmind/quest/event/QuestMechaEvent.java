package fr.craftyourmind.quest.event;

import fr.craftyourmind.manager.util.CYMEvent;
import fr.craftyourmind.quest.packet.DataQuestMecha;

public class QuestMechaEvent extends CYMEvent{

	public DataQuestMecha data;
	
	public QuestMechaEvent(DataQuestMecha data) {
		this.data = data;
	}
}
