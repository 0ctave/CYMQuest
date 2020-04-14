package fr.craftyourmind.quest.packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.craftyourmind.manager.server.network.ByteBuffer;
import fr.craftyourmind.manager.util.CYMData;
import fr.craftyourmind.quest.event.QuestScreenEvent;

public class DataQuestScreen extends CYMData{

	public static final int NPC = 0;
	public static final int EVENT = 1;
	
	public int action = 0;
	public int npc;
	public int idQ;
	public String textNpc = "";
	public List<Integer> listid = new ArrayList<Integer>();
	public List<Integer> listnpc = new ArrayList<Integer>();
	public List<String> listTitle = new ArrayList<String>();
	
	public DataQuestScreen() { }
	public DataQuestScreen(int action, int npc) {
		this.action = action;
		this.npc = npc;
	}
	public DataQuestScreen(int action, int npc, int idQ) {
		this.action = action;
		this.npc = npc;
		this.idQ = idQ;
	}

	public void readPacketData(ByteBuffer input) throws IOException {
		// TODO Auto-generated method stub
		
	}

	public void writePacketData(ByteBuffer output) throws IOException {
		output.writeInt(action);
		output.writeInt(npc);
		output.writeStr(textNpc);
		output.writeList(listid, listTitle);
		output.writeListInt(listnpc);
	}
	
	@Override
	public void callEvent() {
		callEvent(new QuestScreenEvent(this));
		send();
	}

	private static int typedata = 0;
	public int getTypedata() {
		return typedata;
	}
	public void setTypedata(int typedata) {
		this.typedata = typedata;
	}
}
