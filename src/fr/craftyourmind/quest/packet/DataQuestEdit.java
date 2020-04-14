package fr.craftyourmind.quest.packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.craftyourmind.manager.server.network.ByteBuffer;
import fr.craftyourmind.manager.util.CYMData;
import fr.craftyourmind.quest.Quest;
import fr.craftyourmind.quest.QuestTag;
import fr.craftyourmind.quest.packet.DataQuestModScreen;
import fr.craftyourmind.quest.event.QuestEditEvent;
import fr.craftyourmind.skill.CYMClass;

public class DataQuestEdit extends CYMData{

	public static final int OPEN = 0;
	public static final int CREATEQUEST = 1;
	public static final int SAVEQUEST = 2;
	public static final int DELETEQUEST = 3;
	public static final int CLASSPARAMS = 4;
	public static final int TAGLIST = 5;
	public static final int TAGUPDATE = 6;
	public static final int TAGDEL = 7;
	public static final int TAGADDQUEST = 8;
	public static final int TAGDELQUEST = 9;
	
	public List<Integer> idRepute = new ArrayList<Integer>();
	public List<String> nameRepute = new ArrayList<String>();

	public int npc;
	public int newnpc;
	public int idQ;
	public String title = "";
	public String introTxt = "";
	public String fullTxt = "";
	public String successTxt = "";
	public String loseTxt = "";
	public int levelMin = 0;
	public int levelMax = 0;
	public int reputeMin = 0;
	public int reputeMax = 0;
	public boolean objInTheOrder = false;
	public boolean common = false;
	public int playersMax = 0;
	public int repute = 0;
	public boolean repeatable = false;
	public boolean repeateTimeAccept;
	public boolean repeateTimeGive;
	public long repeateTime = 0;
	public int parent = 0;
	public int idEvent = 0;
	public boolean displayIconNPC = true;
	public String params = "";
	public String icons = "";
	public String allIcons = "";
	public int cymClass;
	public String nameParamclass = "";
	public float classMin = 0;
	public float classMax = 0;
	public List<Integer> idClasses = new ArrayList<Integer>();
	public List<String> nameClasses = new ArrayList<String>();
	public List<String> allParams = new ArrayList<String>();
	public String nameTag = "";
	public int idTag;
	public boolean hidden;
	public String taglist = "";
	public String tags = "0";

	public int action;

	public DataQuestEdit() { }
	
	public void readPacketData(ByteBuffer input) throws IOException {
		action = input.readInt();
		if(action == CLASSPARAMS){
			cymClass = input.readInt();
		}else if(action == TAGLIST){
			npc = input.readInt();
			idQ = input.readInt();
		}else if(action == TAGUPDATE){
			idTag = input.readInt();
			nameTag = input.readStr();
			hidden = input.readBoolean();
			npc = input.readInt();
			idQ = input.readInt();
		}else if(action == TAGDEL || action == TAGADDQUEST || action == TAGDELQUEST){
			idTag = input.readInt();
			npc = input.readInt();
			idQ = input.readInt();
		}else{
			npc = input.readInt();
			idQ = input.readInt();
			if(action != OPEN && action != DELETEQUEST){
				newnpc = input.readInt();
				title = input.readStr();
				introTxt = input.readStr();
				fullTxt = input.readStr();
				successTxt = input.readStr();
				loseTxt = input.readStr();
				levelMin = input.readInt();
				levelMax = input.readInt();
				reputeMin = input.readInt();
				reputeMax = input.readInt();
				objInTheOrder = input.readBoolean();
				common = input.readBoolean();
				playersMax = input.readInt();
				repute = input.readInt();
				repeatable = input.readBoolean();
				repeateTimeAccept = input.readBoolean();
				repeateTimeGive = input.readBoolean();
				repeateTime = input.readLong();
				parent = input.readInt();
				idEvent = input.readInt();
				displayIconNPC = input.readBoolean();
				params = input.readStr();
				icons = input.readStr();
				cymClass = input.readInt();
				nameParamclass = input.readStr();
				classMin = input.readFloat();
				classMax = input.readFloat();
			}
		}
	}

	public void writePacketData(ByteBuffer output) throws IOException {
		output.writeInt(action);
		if(action == CLASSPARAMS){
			output.writeListStr(allParams);
		}else if(action == TAGLIST){
			output.writeStr(taglist);
			output.writeStr(tags);
		}else{
			output.writeList(idRepute, nameRepute);
			output.writeInt(npc);
			output.writeInt(idQ);
			output.writeStr(title);
			output.writeStr(introTxt);
			output.writeStr(fullTxt);
			output.writeStr(successTxt);
			output.writeStr(loseTxt);
			output.writeInt(levelMin);
			output.writeInt(levelMax);
			output.writeInt(reputeMin);
			output.writeInt(reputeMax);
			output.writeBoolean(objInTheOrder);
			output.writeBoolean(common);
			output.writeInt(playersMax);
			output.writeInt(repute);
			output.writeBoolean(repeatable);
			output.writeBoolean(repeateTimeAccept);
			output.writeBoolean(repeateTimeGive);
			output.writeLong(repeateTime);
			output.writeInt(parent);
			output.writeInt(idEvent);
			output.writeBoolean(displayIconNPC);
			output.writeStr(params);
			output.writeStr(icons);
			output.writeStr(allIcons);
			output.writeInt(cymClass);
			output.writeStr(nameParamclass);
			output.writeListStr(allParams);
			output.writeFloat(classMin);
			output.writeFloat(classMax);
			output.writeList(idClasses, nameClasses);
		}
	}

	@Override
	public void callEvent() {
		if(action >= CLASSPARAMS){
			if(getPlayer().hasPermission("cymquest.questedit")){
				if(action == CLASSPARAMS){
					CYMClass mc = CYMClass.getCYMClass(cymClass);
					if(mc != null) allParams = mc.getNameMechaParams();
					send();
				}else if(action == TAGLIST){
					taglist = QuestTag.getTags();
					Quest q = Quest.get(npc, idQ);
					if(q != null) tags = q.getTags();
					send();
				}else if(action == TAGUPDATE){
					action = TAGLIST;
					QuestTag.update(idTag, nameTag, hidden);
					callEvent();
				}else if(action == TAGDEL){
					action = TAGLIST;
					QuestTag.remove(idTag);
					callEvent();
				}else if(action == TAGADDQUEST || action == TAGDELQUEST){
					QuestTag qt = QuestTag.get(idTag);
					if(qt != null){
						Quest q = Quest.get(npc, idQ);
						if(q != null){
							if(action == TAGADDQUEST) q.addTag(qt);
							else q.removeTag(qt);
						}
					}
					action = TAGLIST;
					callEvent();
				}
			}
		}else{
			callEvent(new QuestEditEvent(this));
			if(action == OPEN) send();
			else if(action == SAVEQUEST || action == CREATEQUEST ){
				action = OPEN;
				callEvent(new QuestEditEvent(this));
				send();
			}else if(action == DELETEQUEST){
				new DataQuestModScreen(DataQuestModScreen.OPENQUESTMOD, npc).callEvent(getPlayer());
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