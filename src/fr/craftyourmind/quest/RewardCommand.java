package fr.craftyourmind.quest;

import fr.craftyourmind.manager.CYMManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import fr.craftyourmind.manager.CYMManager;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class RewardCommand extends AbsReward{
	
	public String cmd = "";
	public boolean servercmd = true;
	public boolean playercmd = false;
	public boolean npccmd = false;
	
	public RewardCommand(Quest q) { super(q); }
	@Override
	public IStateRew getState(StateQuestPlayer sqp) { return new StateCommand(sqp); }
	@Override
	public int getType() { return COMMAND; }
	@Override
	public String getStrType() { return STRCOMMAND; }
	@Override
	public String getParams() {
		return new StringBuilder().append(idItem).append(DELIMITER).append(cmd).append(DELIMITER).append(servercmd).append(DELIMITER).append(playercmd).append(DELIMITER).append(npccmd).toString();
	}
	@Override
	public String getParamsGUI() {
		Material mat = Material.getMaterial(idItem);
		if(mat == null) return ""; else return mat.getKey().toString();
	}
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 2){
			idItem = params[0];
			cmd = params[1];
			sqlSave();
		}else if(params.length == 3){
			idItem = params[0];
			cmd = params[1];
			servercmd = Boolean.valueOf(params[2]);
			sqlSave();
		}else if(params.length == 5){
			idItem = params[0];
			cmd = params[1];
			servercmd = Boolean.valueOf(params[2]);
			playercmd = Boolean.valueOf(params[3]);
			npccmd = Boolean.valueOf(params[4]);
		}
	}
	// ---------------- STATECOMMAND ----------------
	class StateCommand extends StateRewPlayer{

		public StateCommand(StateQuestPlayer sqp) { super(sqp); }
		@Override
		public void give() {
			if(servercmd) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replaceAll("\\$p", sqp.getPlayer().getName()));
			else if(playercmd){
				if(sqp.getPlayer().getPlayer() != null) sqp.getPlayer().getPlayer().performCommand(cmd.replaceAll("\\$p", sqp.getPlayer().getName()));
			}else if(npccmd){
				Entity npc = CYMManager.getPlayerNPC(q.npc);
				if(npc != null && npc instanceof Player)
					((Player)npc).performCommand(cmd.replaceAll("\\$p", sqp.getPlayer().getName()));
			}
		}
	}
}