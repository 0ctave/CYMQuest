package fr.craftyourmind.quest.packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.craftyourmind.manager.server.network.ByteBuffer;
import fr.craftyourmind.manager.util.CYMData;
import fr.craftyourmind.quest.event.QuestEvent;

public class DataQuest extends CYMData{

	public static final int QUEST = 0;
	public static final int QUESTMOD = 1;
	public static final int PLAYERQUEST = 2;
	public static final int PLAYEREND = 3;
	public static final int DECLINE = 4;
	public static final int ACCEPT = 5;
	public static final int GIVEREWARD = 6;
	public static final int EVENT = 7;
	
	public int action;
	
	public int npc;
	public int idQ;
	public int idE;
	public boolean edit;
    public int idPlayer;
	
	public String title = "";
	public String introTxt = "";
	public String fullTxt = "";
	public String successTxt = "";
	public String loseTxt = "";
	
	public boolean success = false;
	public boolean beginning = false;
	public boolean terminate = false;

	public String objIds = "";
	public String rewIds = "";
	public String objTypes = "";
	public String rewTypes = "";
	public String objTxt = "";
	public String rewTxt = "";
	public String objItems = "";
	public String objItemsData = "";
	public String rewItems = "";
	public String rewItemsData = "";
	public List<Boolean> objFinishs = new ArrayList<Boolean>();
	public List<String> objParams = new ArrayList<String>();
	
	
	public DataQuest() { }

	public void readPacketData(ByteBuffer input) throws IOException {
		action = input.readInt();
		npc = input.readInt();
		idQ = input.readInt();
		edit = input.readBoolean();
		idPlayer = input.readInt();
	}

	public void writePacketData(ByteBuffer output) throws IOException {
		output.writeInt(npc);
		output.writeInt(idQ);
		output.writeInt(idE);
		output.writeStr(title);
		output.writeStr(introTxt);
		output.writeStr(fullTxt);
		output.writeStr(successTxt);
		output.writeStr(loseTxt);
		output.writeBoolean(success);
		output.writeBoolean(beginning);
		output.writeBoolean(terminate);
		output.writeStr(objIds);
		output.writeStr(rewIds);
		output.writeStr(objTypes);
		output.writeStr(rewTypes);
		output.writeStr(objTxt);
		output.writeStr(rewTxt);
		output.writeStr(objItems);
		output.writeStr(objItemsData);
		output.writeStr(rewItems);
		output.writeStr(rewItemsData);
		output.writeListBool(objFinishs);
		output.writeListStr(objParams);
	}

	@Override
	public void callEvent() {
		callEvent(new QuestEvent(this));
		send();
	}

	@Override
	public void send() {
		if(action == GIVEREWARD)
			new DataQuestScreen(DataQuestScreen.NPC, npc).callEvent(player);
		else
			super.send();
	}

	private static int typedata = 0;
	public int getTypedata() {
		return typedata;
	}
	public void setTypedata(int typedata) {
		this.typedata = typedata;
	}
}
