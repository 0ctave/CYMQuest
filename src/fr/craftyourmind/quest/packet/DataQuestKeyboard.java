package fr.craftyourmind.quest.packet;

import java.io.IOException;

import org.bukkit.entity.Player;
import fr.craftyourmind.manager.server.network.ByteBuffer;
import fr.craftyourmind.manager.util.CYMData;
import fr.craftyourmind.quest.QuestKeyboard;

public class DataQuestKeyboard extends CYMData{

	public static final int OPEN = 0;
	public static final int CONFIG = 1;
	public static final int SAVE = 2;
	
	public int action;
	public int playerScreen;
	public String playerScreenKey;
	public int eventManager;
	public String eventManagerKey;
	public int skill;
	public String skillKey;
	public int skillBar;
	public String skillBarKey;
	
	public DataQuestKeyboard() {}
	@Override
	public void readPacketData(ByteBuffer input) throws IOException {
		action = input.readInt();
		if(action == SAVE){
			playerScreen = input.readInt();
			playerScreenKey = input.readStr();
			eventManager = input.readInt();
			eventManagerKey = input.readStr();
			skill = input.readInt();
			skillKey = input.readStr();
			skillBar = input.readInt();
			skillBarKey = input.readStr();
		}
	}
	@Override
	public void writePacketData(ByteBuffer output) throws IOException {
		output.writeInt(action);
		output.writeInt(playerScreen);
		output.writeStr(playerScreenKey);
		output.writeInt(eventManager);
		output.writeStr(eventManagerKey);
		output.writeInt(skill);
		output.writeStr(skillKey);
		output.writeInt(skillBar);
		output.writeStr(skillBarKey);
	}
	@Override
	public void callEvent() {
		QuestKeyboard.recieve(this);
	}

	public boolean isOPEN() {
		return action == OPEN;
	}

	public void sendCONFIG(Player p, int playerScreen, String playerScreenKey, int eventManager, String eventManagerKey, int skill, String skillKey, int skillBar, String skillBarKey) {
		action = CONFIG; this.player = p;
		this.playerScreen = playerScreen; this.playerScreenKey = playerScreenKey;
		this.eventManager = eventManager; this.eventManagerKey = eventManagerKey;
		this.skill = skill; this.skillKey = skillKey; this.skillBar = skillBar; this.skillBarKey = skillBarKey;
		send();
	}
	
	public boolean isSAVE() {
		return action == SAVE;
	}
	
	public void sendOPEN() {
		action = OPEN; send();
	}
	
	private static int typedata = 0;
	public int getTypedata() {
		return typedata;
	}
	public void setTypedata(int typedata) {
		this.typedata = typedata;
	}
}