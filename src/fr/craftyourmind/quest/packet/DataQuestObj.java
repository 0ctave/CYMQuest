package fr.craftyourmind.quest.packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.craftyourmind.manager.server.network.ByteBuffer;
import fr.craftyourmind.manager.util.CYMData;
import fr.craftyourmind.quest.event.QuestObjEvent;
import fr.craftyourmind.quest.event.QuestObjOpenEvent;

public class DataQuestObj extends CYMData{

	public static final int OPEN = 0;
	public static final int OBJECTIVE = 1;
	public static final int CREATEOBJ = 2;
	public static final int SAVEOBJ = 3;
	public static final int DELETEOBJ = 4;
	
	public List<Integer> idTypes = new ArrayList<Integer>();
	public List<String> nameTypes = new ArrayList<String>();
	
	public List<Integer> idObjs = new ArrayList<Integer>();
	public List<String> nameObjs = new ArrayList<String>();
	
	public int action;
	public int npc;
	public int idQ;
	public int idO;
	public int type;
	
	public String descriptive;
	public String success;
	public boolean finishQuest;
	public String param;
	public String paramGUI;

	public DataQuestObj() { }
	
	public void readPacketData(ByteBuffer input) throws IOException {
		action = input.readInt();
		if(action == OPEN){
			npc = input.readInt();
			idQ = input.readInt();
		}else if(action == OBJECTIVE || action == DELETEOBJ){
			npc = input.readInt();
			idQ = input.readInt();
			idO = input.readInt();
		}else{
			npc = input.readInt();
			idQ =input.readInt();
			type = input.readInt();
			idO = input.readInt();
			descriptive = input.readStr();
			success = input.readStr();
			finishQuest = input.readBoolean();
			param = input.readStr();
		}
	}

	public void writePacketData(ByteBuffer output) throws IOException {
		output.writeInt(action);
		if(action == OPEN){
			output.writeList(idTypes, nameTypes);
			output.writeList(idObjs, nameObjs);
		}else{
			output.writeInt(npc);
			output.writeInt(idQ);
			output.writeInt(type);
			output.writeInt(idO);
			output.writeStr(descriptive);
			output.writeStr(success);
			output.writeBoolean(finishQuest);
			output.writeStr(param);
			output.writeStr(paramGUI);
		}
	}

	@Override
	public void callEvent() {
		if(action == OPEN){
			callEvent(new QuestObjOpenEvent(this));
			send();
		}else if(action == OBJECTIVE){
			callEvent(new QuestObjEvent(this));
			send();
		}else if(action == CREATEOBJ){
			callEvent(new QuestObjEvent(this));
		}else{
			callEvent(new QuestObjEvent(this));
			action = OPEN;
			callEvent(new QuestObjOpenEvent(this));
			send();
		}
	}

	private static int typedata = 0;
	public int getTypedata() {
		return typedata;
	}
	public void setTypedata(int typedata) {
		this.typedata = typedata;
	}
}
