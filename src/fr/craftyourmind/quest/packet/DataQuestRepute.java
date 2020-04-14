package fr.craftyourmind.quest.packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.craftyourmind.manager.CYMReputation;
import fr.craftyourmind.manager.server.network.ByteBuffer;
import fr.craftyourmind.manager.util.CYMData;
import fr.craftyourmind.manager.util.ReputeData;
import fr.craftyourmind.quest.QuestPlayer;

public class DataQuestRepute extends CYMData{

	public static final int OPEN = 0;
	public static final int CREATE = 1;
	public static final int SAVE = 2;
	public static final int DELETE = 3;
	public int action;
	public int idRep;
	public String nameRep;
	public String descriptiveRep;
	
	public boolean edit = false;
	public List<Integer> ids;
	public List<String> names;
	public List<String> descriptives;
	public List<Integer> pts;
	public List<Integer> params;
	
	public DataQuestRepute() {}
	
	public void readPacketData(ByteBuffer input) throws IOException {
		action = input.readInt();
		if(action != OPEN){
			idRep = input.readInt();
			if(action != DELETE){
				nameRep = input.readStr();
				descriptiveRep = input.readStr();
			}
		}
	}

	public void writePacketData(ByteBuffer output) throws IOException {
		if(action == OPEN){
			output.writeBoolean(edit);
			output.writeList(ids, names);
			output.writeListStr( descriptives);
			output.writeListInt(pts);
			output.writeListInt(params);
		}
	}

	@Override
	public void callEvent() {
		if(action == OPEN){
			ids = new ArrayList<Integer>();
			names = new ArrayList<String>();
			descriptives = new ArrayList<String>();
			pts = new ArrayList<Integer>();
			params = new ArrayList<Integer>();
			QuestPlayer qp = QuestPlayer.get(player);
			Map<CYMReputation, ReputeData> map = qp.getReputations();
			if(qp.hasClan()){
				map = new HashMap<CYMReputation, ReputeData>(map);
				map.putAll(qp.getClan().getReputations());
			}
			if(player.hasPermission("cymquest.reputation")){
				edit = true;
				map = new HashMap<CYMReputation, ReputeData>(map);
				for(CYMReputation rep : CYMReputation.get())
					if(!map.containsKey(rep)) map.put(rep, null);
			}
			for(Entry<CYMReputation, ReputeData> entry : map.entrySet()){
				ids.add(entry.getKey().id);
				names.add(entry.getKey().name);
				descriptives.add(entry.getKey().descriptive);
				if(entry.getValue() == null){
					pts.add(0);
					params.add(0);
				}else{
					pts.add(entry.getValue().points);
					params.add(entry.getValue().param);
				}
			}
			send();
		}else{
			if(player.hasPermission("cymquest.reputation")){
				if(action == CREATE){
					CYMReputation rep = new CYMReputation(nameRep);
					rep.descriptive = descriptiveRep;
					rep.create();
				}else if(action == SAVE){
					CYMReputation rep = CYMReputation.getById(idRep);
					if(rep != null){
						rep.name = nameRep;
						rep.descriptive = descriptiveRep;
						rep.save();
					}
				}else if(action == DELETE){
					CYMReputation rep = CYMReputation.getById(idRep);
					if(rep != null) rep.delete();
				}
			}
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
