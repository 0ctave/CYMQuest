package fr.craftyourmind.quest.packet;

import java.io.IOException;
import java.util.List;

import org.bukkit.entity.Player;
import fr.craftyourmind.manager.server.network.ByteBuffer;
import fr.craftyourmind.manager.util.CYMData;
import fr.craftyourmind.quest.EventParadise;
import fr.craftyourmind.quest.event.QuestEventParadiseEditEvent;

public class DataQuestEventParadiseEdit extends CYMData{

	public static final int OPEN = 0;
	public static final int SELECT = 1;
	public static final int CREATE = 2;
	public static final int SAVE = 3;
	public static final int DELETE = 4;

	public int action, idE;
	public int slot = 0;
	public List<Integer> ids;
	public List<String> names;
	public List<Integer> idsType;
	public List<String> namesType;
	public List<Boolean> started;
	
	public EventParadise ep;
	
	public DataQuestEventParadiseEdit() {}
	public DataQuestEventParadiseEdit(int action) {
		this.action = action;
	}

	public void callEventOPEN(Player player){
		this.action = OPEN; callEvent(player);
	}
	public void callEventOPEN(Player player, int slot){
		this.action = OPEN; this.slot = slot; callEvent(player);
	}
	
	public void readPacketData(ByteBuffer input) throws IOException {
		action = input.readInt();
		if(action == OPEN) slot = input.readInt();
		else if(action == SELECT || action == DELETE){
			idE = input.readInt();
			slot = input.readInt();
		}else if(action == SAVE || action == CREATE){
			int type = input.readInt();
			if(action == CREATE){ 
				ep = EventParadise.newEvent(type);
				ep.id = input.readInt();
			}else if(action == SAVE) 
				ep = EventParadise.get(input.readInt());
			ep.name.setStrUnlock(input.readStr());
			ep.flow = input.readInt();
			ep.preparation = input.readBoolean();
			ep.firstFinish = input.readBoolean();
			ep.openQuest = input.readBoolean();
			ep.showObjCompleted = input.readBoolean();
			ep.noticeTimer = input.readInt();
			ep.noticeMessage = input.readStr();
			ep.beginMessage = input.readStr();
			ep.generalTimer = input.readInt();
			ep.nextEventTimer = input.readInt();
			ep.setWorld(input.readStr());
			ep.x = input.readInt();
			ep.z = input.readInt();
			ep.radius = input.readInt();
			ep.loadParamsCon(input.readStr());
			ep.minPlayers = input.readInt();
			ep.maxPlayers = input.readInt();
			ep.nextEventWin = input.readInt();
			ep.nextEventLose = input.readInt();
			ep.autoAccept = input.readBoolean();
			ep.setSlot(input.readInt());
			slot = ep.getSlot();
		}
	}

	public void writePacketData(ByteBuffer output) throws IOException {
		output.writeInt(action);
		if(action == OPEN){
			output.writeInt(slot);
			output.writeList(ids, names);
			output.writeList(idsType, namesType);
			output.writeListBool(started);
		}else if(action == SELECT){
			output.writeInt(ep.getType());
			output.writeInt(ep.id);
			output.writeStr(ep.name.getStr());
			output.writeInt(ep.flow);
			output.writeBoolean(ep.preparation);
			output.writeBoolean(ep.firstFinish);
			output.writeBoolean(ep.openQuest);
			output.writeBoolean(ep.showObjCompleted);
			output.writeInt(ep.noticeTimer);
			output.writeStr(ep.noticeMessage);
			output.writeStr(ep.beginMessage);
			output.writeInt(ep.generalTimer);
			output.writeInt(ep.nextEventTimer);
			output.writeStr(ep.getWorldName());
			output.writeInt(ep.x);
			output.writeInt(ep.z);
			output.writeInt(ep.radius);
			output.writeStr(ep.getParamsCon());
			output.writeInt(ep.minPlayers);
			output.writeInt(ep.maxPlayers);
			output.writeInt(ep.nextEventWin);
			output.writeInt(ep.nextEventLose);
			output.writeBoolean(ep.autoAccept);
			output.writeInt(ep.getSlot());
		}
	}

	@Override
	public void callEvent() {
		callEvent(new QuestEventParadiseEditEvent(this));
		if(action == OPEN || action == SELECT)
			send();
		else if(action != CREATE){
			action = OPEN;
			callEvent();
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
