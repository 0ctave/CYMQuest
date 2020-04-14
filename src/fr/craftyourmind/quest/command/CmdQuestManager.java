package fr.craftyourmind.quest.command;

import fr.craftyourmind.manager.command.AbsCYMCommand;
import fr.craftyourmind.skill.GUISkillScreen;

public class CmdQuestManager {

	//public static final int CMDPARTICLE = 1;
	public static final int CMDMEKABOX = 2;
	public static final int CMDCHUNKPOSITION = 4;
	public static final int CMDSKILLS = 5;
	public static final int CMDGUISKILLS = 6;
	//public static final int CMDTEXTSCREEN = 7;
	public static final int CMDQUEST = 8;
	
	public static void init() {
		AbsCYMCommand.add(new CmdChunkPosition());
		AbsCYMCommand.add(new CmdMekaBox());
		AbsCYMCommand.add(new CmdSkillManager());
		AbsCYMCommand.add(new GUISkillScreen());
		AbsCYMCommand.add(new CmdQuest());
	}
	
}
