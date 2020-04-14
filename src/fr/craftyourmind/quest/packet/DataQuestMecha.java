package fr.craftyourmind.quest.packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import fr.craftyourmind.manager.command.ICYMCommandData;
import fr.craftyourmind.manager.server.network.ByteBuffer;
import fr.craftyourmind.manager.util.CYMData;
import fr.craftyourmind.quest.event.QuestMechaEvent;
import fr.craftyourmind.quest.mecha.Mechanism;

public class DataQuestMecha extends CYMData{

	public static final int OPEN = 0;
	public static final int OPENMECHA = 1;
	public static final int SELECT = 2;
	public static final int CREATE = 3;
	public static final int SAVE = 4;
	public static final int DELETE = 5;
	public static final int LINK = 6;
	public static final int DELLINK = 7;
	public static final int CLONE = 8;
	public static final int COMMANDGUI = 9;
	public static final int UPLINK = 10;
	public static final int DOWNLINK = 11;
	public static final int MOVELINK = 12;
	
	public int action = 0;
	public int idM = 0;
	public int typeMecha = 0;
	public int typeDriver = 0;
	public int idDriver = 0;
	public int category = -1;
	public boolean common, permanent, single;
	public String nameMecha = "";
	public String message = "";
	public String params = "";
	public String paramsGUI = "";
	
	public int idmecha = 0;
	public int idchild = 0;
	public int slot = 0;
	
	public boolean links;
	
	public List<Integer> idMechas = new ArrayList<Integer>();
	public List<String> nameMechas = new ArrayList<String>();
	public List<Integer> catMechas = new ArrayList<Integer>();
	
	public List<Integer> idparents = new ArrayList<Integer>();
	public List<Integer> idchilds = new ArrayList<Integer>();
	public List<String> namelinks = new ArrayList<String>();
	public List<Integer> slots = new ArrayList<Integer>();
	public List<Integer> orders = new ArrayList<Integer>();
	
	public List<Integer> idTypes = new ArrayList<Integer>();
	public List<String> nameTypes = new ArrayList<String>();
	
	private ICYMCommandData cmdData;
	
	public DataQuestMecha() { }
	
	public void sendCommandGui(Player p, ICYMCommandData cmdData){
		action = COMMANDGUI; this.cmdData = cmdData; send(p);
	}
	@Override
	public void readPacketData(ByteBuffer input) throws IOException {
		action = input.readInt();
		if(action == OPEN){
			typeDriver = input.readInt();;
			idDriver = input.readInt();
		}else if(action == OPENMECHA){
			typeDriver = input.readInt();;
			idDriver = input.readInt();
			category = input.readInt();
		}else if(action == SELECT){
			idM = input.readInt();
			typeDriver = input.readInt();
			idDriver = input.readInt();
		}else if(action == CLONE){
			idM = input.readInt();
			typeDriver = input.readInt();
			idDriver = input.readInt();
			category = input.readInt();
			links = input.readBoolean();
		}else if(action == DELETE){
			idM = input.readInt();
			typeDriver = input.readInt();
			idDriver = input.readInt();
			category = input.readInt();
		}else if(action == SAVE || action == CREATE){
			typeMecha = input.readInt();
			idM = input.readInt();
			typeDriver = input.readInt();
			idDriver = input.readInt();
			category = input.readInt();
			common = input.readBoolean();
			permanent = input.readBoolean();
			single = input.readBoolean();
			nameMecha = input.readStr();
			message = input.readStr();
			params = input.readStr();
		}else if(action == LINK || action == DELLINK || action == UPLINK || action == DOWNLINK || action == MOVELINK){
			typeDriver = input.readInt();
			idDriver = input.readInt();
			idmecha = input.readInt();
			idchild = input.readInt();
			slot = input.readInt();
		}else if(action == COMMANDGUI){
			int id = input.readInt();
			cmdData = Mechanism.getCmdData(id);
			if(cmdData != null)
				cmdData = cmdData.readPacketData(input);
		}
	}
	@Override
	public void writePacketData(ByteBuffer output) throws IOException {
		output.writeInt(action);
		if(action == OPEN){
			output.writeList(idMechas, nameMechas);
			output.writeListInt(catMechas);
			output.writeListInt(idparents);
			output.writeListInt(idchilds);
			output.writeListStr(namelinks);
			output.writeListInt(slots);
			output.writeListInt(orders);
		}else if(action == OPENMECHA){
			output.writeInt(category);
			output.writeList(idTypes, nameTypes);
			output.writeList(idMechas, nameMechas);
		}else if(action == SELECT){
			output.writeInt(typeMecha);
			output.writeInt(idM);
			output.writeInt(typeDriver);
			output.writeInt(idDriver);
			output.writeInt(category);
			output.writeBoolean(common);
			output.writeBoolean(permanent);
			output.writeBoolean(single);
			output.writeStr(nameMecha);
			output.writeStr(message);
			output.writeStr(params);
			output.writeStr(paramsGUI);
		}else if(action == COMMANDGUI)
			cmdData.writePacketData(output);
	}
	@Override
	public void callEvent() {
		if(action == COMMANDGUI){ if(cmdData != null) cmdData.receive(player); }
		else{
			callEvent(new QuestMechaEvent(this));
			if(action == OPEN || action == OPENMECHA || action == SELECT)
				send();
			else if(action == LINK || action == DELLINK || action == UPLINK || action == DOWNLINK || action == MOVELINK){
				action = OPEN;
				callEvent();
			}else if(action != CREATE){
				action = OPENMECHA;
				callEvent();
			}
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