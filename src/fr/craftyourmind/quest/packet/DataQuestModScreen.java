package fr.craftyourmind.quest.packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import fr.craftyourmind.manager.server.network.ByteBuffer;
import fr.craftyourmind.manager.util.CYMData;
import fr.craftyourmind.quest.event.QuestModScreenEvent;

public class DataQuestModScreen extends CYMData{

	public static final int OPENQUESTMOD = 0;
	public static final int OPENQUESTPLAYER = 1;
	public static final int OPENQUESTEND = 2;
	public static final int OPENQUESTEVENT = 3;
	
	public int action, npc, event, tag, idPlayer;
	public boolean menuTag, edit;
	public List<Integer> listid = new ArrayList<Integer>();
	public List<Integer> listnpc = new ArrayList<Integer>();
	public List<String> listTitle = new ArrayList<String>();
	public String listQuestTags = "", namePlayer = "";
	
	public DataQuestModScreen() {}
	public DataQuestModScreen(int action, int npc) {
		this.action = action;
		this.npc = npc;
	}
	
	public void sendEditQuests(Player p, int id) {
		action = OPENQUESTPLAYER;
		edit = true;
		idPlayer = id;
		menuTag = true;
		callEvent(p);
	}
	
	public void readPacketData(ByteBuffer input) throws IOException {
		action = input.readInt();
		if(action == OPENQUESTMOD)
			npc = input.readInt();
		else if(action == OPENQUESTEVENT)
			event = input.readInt();
		else if(action == OPENQUESTPLAYER || action == OPENQUESTEND){
			edit = input.readBoolean();
			idPlayer = input.readInt();
			namePlayer = input.readStr();
			menuTag = input.readBoolean();
			tag = input.readInt();
		}
	}

	public void writePacketData(ByteBuffer output) throws IOException {
		output.writeInt(action);
		output.writeInt(npc);
		
		output.writeList(listid, listTitle);
		output.writeListInt(listnpc);
		
		if(action == OPENQUESTPLAYER || action == OPENQUESTEND){
			output.writeBoolean(edit);
			output.writeInt(idPlayer);
			output.writeStr(namePlayer);
			output.writeBoolean(menuTag);
			output.writeInt(tag);
			output.writeStr(listQuestTags);
		}
	}

	@Override
	public void callEvent() {
		callEvent(new QuestModScreenEvent(this));
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