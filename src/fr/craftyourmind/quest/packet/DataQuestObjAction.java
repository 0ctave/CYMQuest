package fr.craftyourmind.quest.packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.craftyourmind.manager.server.network.ByteBuffer;
import fr.craftyourmind.manager.util.CYMData;
import fr.craftyourmind.quest.event.QuestObjActionEvent;

public class DataQuestObjAction extends CYMData{

	public static final int CHOICE = 0;
	public static final int ANSWER = 1;
	
	public int action;
	
	public int npc;
	public int idQ;
	public List<Integer> rewards;
	public int idO;
	public String response;
	
	public DataQuestObjAction() {}

	public void readPacketData(ByteBuffer input) throws IOException {
		action = input.readInt();
		npc = input.readInt();
		idQ = input.readInt();
		if(action == CHOICE){
			rewards = new ArrayList<Integer>();
			input.readListInt(rewards);
		}else if(action == ANSWER){
			idO = input.readInt();
			response = input.readStr();
		}
	}

	public void writePacketData(ByteBuffer output) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void callEvent() {
		callEvent(new QuestObjActionEvent(this));
	}

	private static int typedata = 0;
	public int getTypedata() {
		return typedata;
	}
	public void setTypedata(int typedata) {
		this.typedata = typedata;
	}
}
