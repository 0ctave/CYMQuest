package fr.craftyourmind.quest.event;

import fr.craftyourmind.quest.packet.DataQuestObjAction;
import fr.craftyourmind.manager.util.CYMEvent;

public class QuestObjActionEvent extends CYMEvent{

	public DataQuestObjAction data;
	
	public QuestObjActionEvent(DataQuestObjAction data) {
		this.data = data;
	}

}
