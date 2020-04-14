package fr.craftyourmind.quest.packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.craftyourmind.manager.server.network.ByteBuffer;
import fr.craftyourmind.manager.util.CYMData;
import fr.craftyourmind.quest.event.QuestRewEvent;
import fr.craftyourmind.quest.event.QuestRewOpenEvent;

public class DataQuestRew extends CYMData{

	public static final int OPEN = 0;
	public static final int REWARD = 1;
	public static final int CREATEREW = 2;
	public static final int SAVEREW = 3;
	public static final int DELETEREW = 4;
	
	public int action, npc, idQ, idR, type;
	public String descriptive="";
	public int amount;
	public String param;
	public String paramGUI;
	
	public List<Integer> idTypes = new ArrayList<Integer>();
	public List<String>  nameTypes = new ArrayList<String>();
	public List<Integer> idRews = new ArrayList<Integer>();
	public List<String> nameRews = new ArrayList<String>();
	
	public DataQuestRew() { }
	
	public void readPacketData(ByteBuffer input) throws IOException {
		action = input.readInt();
		if(action == OPEN){
			npc = input.readInt();
			idQ = input.readInt();
		}else if(action == REWARD || action == DELETEREW){
			npc = input.readInt();
			idQ = input.readInt();
			idR = input.readInt();
		}else{
			npc = input.readInt();
			idQ =input.readInt();
			type = input.readInt();
			idR = input.readInt();
			descriptive = input.readStr();
			amount = input.readInt();
			param = input.readStr();
		}
	}

	public void writePacketData(ByteBuffer output) throws IOException {
		output.writeInt(action);
		if(action == OPEN){
			output.writeList(idTypes, nameTypes);
			output.writeList(idRews, nameRews);
		}else{
			output.writeInt(npc);
			output.writeInt(idQ);
			output.writeInt(type);
			output.writeInt(idR);
			output.writeStr(descriptive);
			output.writeInt(amount);
			output.writeStr(param);
			output.writeStr(paramGUI);
		}
	}

	@Override
	public void callEvent() {
		if(action == OPEN){
			callEvent(new QuestRewOpenEvent(this));
			send();
		}else if(action == REWARD){
			callEvent(new QuestRewEvent(this));
			send();
		}else if(action == CREATEREW){
			callEvent(new QuestRewEvent(this));
		}else{
			callEvent(new QuestRewEvent(this));
			action = OPEN;
			callEvent(new QuestRewOpenEvent(this));
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
