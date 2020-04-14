package fr.craftyourmind.quest.packet;

import java.io.IOException;

import fr.craftyourmind.manager.server.network.ByteBuffer;
import fr.craftyourmind.manager.util.CYMData;
import fr.craftyourmind.quest.event.QuestEventParadiseEvent;

public class DataQuestEventParadise extends CYMData{

	public static final int START = 0;
	public static final int NOTICE = 1;
	public static final int OPEN = 2;
	public static final int ACCEPT = 3;
	public static final int STOP = 4;
	public static final int DECLINE = 5;
	public static final int GIVEREWARD = 6;
	public static final int BEGINNING = 7;
	
	public int action;
	public int idE;
	public int npc;
	public int idQ;

	public int timer;
	public String name;
	
	public DataQuestEventParadise() {}
	public DataQuestEventParadise(int action, int idE, int npc, int idQ) {
		this.action = action;
		this.idE = idE;
		this.npc = npc;
		this.idQ = idQ;
	}
	
	public DataQuestEventParadise(int action, int idE, int npc, int idQ, int timer, String name) {
		this.action = action;
		this.idE = idE;
		this.npc = npc;
		this.idQ = idQ;
		this.timer = timer;
		this.name = name;
	}

	public void readPacketData(ByteBuffer input) throws IOException {
		action = input.readInt();
		idE = input.readInt();
		npc = input.readInt();
		idQ = input.readInt();
	}

	public void writePacketData(ByteBuffer output) throws IOException {
		output.writeInt(action);
		output.writeInt(idE);
		output.writeInt(npc);
		output.writeInt(idQ);
		if(action == NOTICE || action == BEGINNING){
			output.writeInt(timer);
			output.writeStr(name);
		}
	}

	@Override
	public void callEvent() {
		callEvent(new QuestEventParadiseEvent(this));
	}

	private static int typedata = 0;
	public int getTypedata() {
		return typedata;
	}
	public void setTypedata(int typedata) {
		this.typedata = typedata;
	}
}
