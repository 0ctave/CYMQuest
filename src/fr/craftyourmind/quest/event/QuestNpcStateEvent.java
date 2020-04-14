package fr.craftyourmind.quest.event;

import fr.craftyourmind.manager.util.CYMEvent;
import fr.craftyourmind.quest.packet.DataQuestNpcState;

public class QuestNpcStateEvent extends CYMEvent{

	public DataQuestNpcState data;
	
	public QuestNpcStateEvent(DataQuestNpcState data) {
		this.data = data;
	}

}
