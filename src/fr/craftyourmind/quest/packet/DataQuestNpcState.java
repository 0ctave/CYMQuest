package fr.craftyourmind.quest.packet;

import java.io.IOException;

import fr.craftyourmind.manager.server.network.ByteBuffer;
import fr.craftyourmind.manager.util.CYMData;
import fr.craftyourmind.quest.QuestIcon;
import fr.craftyourmind.quest.QuestIcon.MIDrawingQuads;
import fr.craftyourmind.quest.event.QuestNpcStateEvent;

public class DataQuestNpcState extends CYMData{

	private static final int DRAWICON = 0;
	private static final int UPDATEICON = 1;
	
	private int action = 0;
	public int npc;
	public boolean questDispo = false;
	public String nameIcon = "";
	public QuestIcon icon;
	
	public DataQuestNpcState() { }
	
	public void readPacketData(ByteBuffer input) throws IOException {
		action = input.readInt();
		npc = input.readInt();
		if(isUpdateIcon()) nameIcon = input.readStr();
	}

	public void writePacketData(ByteBuffer output) throws IOException {
		output.writeInt(action);
		if(isDrawIcon()){
			output.writeInt(npc);
			output.writeBoolean(questDispo);
			output.writeStr(nameIcon);
		}else if(isUpdateIcon()){
			output.writeInt(npc);
			output.writeStr(nameIcon);
			output.writeBoolean(icon.visibleAllDirection);
			output.writeInt(icon.quads.size());
			for (MIDrawingQuads dq : icon.quads) {
				writeListFloat(output, dq.color);
				writeListFloat(output, dq.vertex1);
				writeListFloat(output, dq.vertex2);
				writeListFloat(output, dq.vertex3);
				writeListFloat(output, dq.vertex4);
			}
		}
	}

	@Override
	public void callEvent() {
		callEvent(new QuestNpcStateEvent(this));
		send();
	}

	private static int typedata = 0;
	public int getTypedata() {
		return typedata;
	}
	public void setTypedata(int typedata) {
		this.typedata = typedata;
	}
	
	public boolean isDrawIcon(){ return action == DRAWICON; }
	
	public boolean isUpdateIcon(){ return action == UPDATEICON; }
}