package fr.craftyourmind.quest.mecha;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import fr.craftyourmind.manager.CYMManager;

public class ActionCommand extends AbsMechaList{

	private static final int COMMAND = 1;
	private static final int TELLRAW = 2;
	private static final int TITLE = 3;
	private static final int SUBTITLE = 4;
	private static Map<Integer, Class<? extends IMechaList>> params = new HashMap<Integer, Class<? extends IMechaList>>();
	static{
		params.put(COMMAND, COMMAND.class);
		params.put(TELLRAW, TELLRAW.class);
		params.put(TITLE, TITLE.class);
		params.put(SUBTITLE, SUBTITLE.class);
	}
	
	private static final int NO = 0;
	private static final int PLAYER = 1;
	private static final int LOCATION = 2;
	
	public int npc = 0;
	public StringData cmd = new StringData();
	public boolean servercmd = true;
	public boolean playercmd = false;
	public boolean npccmd = false;
	public int relative;
	
	public ActionCommand() { }
	@Override
	public Map<Integer, Class<? extends IMechaList>> getMechaParam() { return params; }
	@Override
	public int getType() { return MechaType.ACTCOMMAND; }
	@Override
	public String getStringParams() {
		return new StringBuilder().append(4).append(DELIMITER).append(cmd).append(DELIMITER).append(npc).append(DELIMITER).append(servercmd).append(DELIMITER).append(playercmd).append(DELIMITER).append(npccmd).append(DELIMITER).append(relative).toString();
	}
	@Override
	public String getParamsGUI() {
		StringBuilder param = new StringBuilder();
		for(Entry<Integer, String> entry : CYMManager.getIdNameNPC().entrySet())
			param.append(entry.getKey()).append(DELIMITER).append(entry.getValue()).append(DELIMITER);
		return param.toString();
	}
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 2){
			cmd.load(params[0]);
			npc = Integer.valueOf(params[1]);
			newParam(COMMAND);
			sqlSave();
		}else if(params.length == 3){
			cmd.load(params[0]);
			npc = Integer.valueOf(params[1]);
			servercmd = Boolean.valueOf(params[2]);
			newParam(COMMAND);
			sqlSave();
		}else if(params.length == 5){
			cmd.load(params[0]);
			npc = Integer.valueOf(params[1]);
			servercmd = Boolean.valueOf(params[2]);
			playercmd = Boolean.valueOf(params[3]);
			npccmd = Boolean.valueOf(params[4]);
			newParam(COMMAND);
			sqlSave();
		}else{
			int version = Integer.valueOf(params[0]);
			if(version == 3){
				cmd.load(params[1]);
				npc = Integer.valueOf(params[2]);
				servercmd = Boolean.valueOf(params[3]);
				playercmd = Boolean.valueOf(params[4]);
				npccmd = Boolean.valueOf(params[5]);
				relative = Integer.valueOf(params[6]);
				newParam(COMMAND);
				sqlSave();
			}else
				super.loadParams(params);
		}
	}
	@Override
	protected int loadParams(int index, String[] params) {
		int version = Integer.valueOf(params[index++]);
		cmd.load(params[index++]);
		npc = Integer.valueOf(params[index++]);
		servercmd = Boolean.valueOf(params[index++]);
		playercmd = Boolean.valueOf(params[index++]);
		npccmd = Boolean.valueOf(params[index++]);
		relative = Integer.valueOf(params[index++]);
		return index;
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateCommand(this, mc, driver); }
	// ------------------ StateCommand ------------------
	class StateCommand extends AbsMechaStateEntityList{
		public StringData cmd = new StringData();
		public StateCommand(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void cloneData(){
			super.cloneData();
			cmd.clone(this, ActionCommand.this.cmd);
		}
		@Override
		public void start() {
			super.start();
			launchMessage();
		}
	}
	// -------------- COMMAND --------------
	class COMMAND implements IMechaList{
		@Override
		public int getId() { return COMMAND; }
		@Override
		public String getParams() { return 0+""; }
		@Override
		public String getParamsGUI() { return ""; }
		@Override
		public void loadParams(int index, String[] params) { }
		@Override
		public IMechaStateList getStateList() {
			return new IMechaStateList<StateCommand>() {
				@Override
				public void cloneData(StateCommand s) { }
				@Override
				public void start(StateCommand s) {
					IMechaDriver driver = s.driver;
					String cmd2 = s.cmd.get().replaceAll("\\$p", driver.getNameEntity());
					if(!(relative == NO)){
						Location loc = null;
						if(relative == PLAYER && driver.hasEntity())
							loc = driver.getEntity().getLocation();
						else if(relative == LOCATION && driver.hasLocation())
								loc = driver.getLocation();
						if(loc != null){
							cmd2 = cmd2.replaceAll("\\$w", loc.getWorld().getName());
							cmd2 = cmd2.replaceAll("\\$x", loc.getBlockX()+"");
							cmd2 = cmd2.replaceAll("\\$y", loc.getBlockY()+"");
							cmd2 = cmd2.replaceAll("\\$z", loc.getBlockZ()+"");
						}
					}
					if(servercmd) Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd2);
					else if(playercmd){
						if(driver.hasPlayer()) driver.getPlayer().performCommand(cmd2);
					}else if(npccmd){
						Entity npcE = CYMManager.getPlayerNPC(npc);
						if(npcE != null && npcE instanceof Player){
							((Player)npcE).performCommand(cmd2);
						}
					}
				}
				@Override
				public void stop(StateCommand s) { }
			};
		}
	}
	// -------------- CMDJSONTEXT --------------
	abstract class CMDJSONTEXT extends COMMAND{
		private List<JsonText> texts = new ArrayList<JsonText>();
		@Override
		public String getParams() {
			StringBuilder params = new StringBuilder().append(texts.size());
			for(JsonText jt : texts){
				params.append(DELIMITER).append(jt.text).append(DELIMITER).append(jt.color).append(DELIMITER).append(jt.formatings.size());
				for(String s : jt.formatings) params.append(DELIMITER).append(s);
			}
			return 0+DELIMITER+params;
		}
		@Override
		public void loadParams(int index, String[] params) {
			int version = Integer.valueOf(params[index++]);
			texts.clear();
			int size = Integer.valueOf(params[index++]);
			for(int i = 0 ; i < size ; i++){
				JsonText jt = new JsonText();
				jt.text = params[index++];
				jt.color = params[index++];
				int sizeForm = Integer.valueOf(params[index++]);
				for(int f = 0 ; f < sizeForm ; f++) jt.formatings.add(params[index++]);
				texts.add(jt);
			}
			StringBuilder line = new StringBuilder().append(getBegincmd()).append(" {text:'',extra:[");
			int i = 0;
			for(JsonText jt : texts){
				line.append("{text:'").append(jt.text).append("'");
				if(!jt.color.equals("none")) line.append(",color:").append(jt.color);
				for(String f : jt.formatings) line.append(",").append(f).append(":true");
				line.append("}");
				i++;
				if(i < texts.size()) line.append(",");
			}
			line.append("]}");
			cmd.load(line.toString());
		}
		public abstract String getBegincmd();
	}
	// -------------- JSONTEXT --------------
	class JsonText{
		private String text = "";
		private String color = "none";
		private List<String> formatings = new ArrayList<String>();
	}
	// -------------- TELLRAW --------------
	class TELLRAW extends CMDJSONTEXT{
		@Override
		public int getId() { return TELLRAW; }
		@Override
		public String getBegincmd() { return "tellraw $p"; }
	}
	// -------------- TITLE --------------
	class TITLE extends CMDJSONTEXT{
		@Override
		public int getId() { return TITLE; }
		@Override
		public String getBegincmd() { return "title $p title"; }
	}
	// -------------- SUBTITLE --------------
	class SUBTITLE extends CMDJSONTEXT{
		@Override
		public int getId() { return SUBTITLE; }
		@Override
		public String getBegincmd() { return "title $p subtitle"; }
	}
}