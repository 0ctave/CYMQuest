package fr.craftyourmind.quest;

import org.bukkit.*;
import org.bukkit.entity.Player;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class RewardTeleport extends AbsReward{

	public static final int NORTH = 180;
	public static final int SOUTH = 0;
	public static final int EAST = -90;
	public static final int WEST = 90;
	
	public String world = "";
	public int x = 0;
	public int y = 0;
	public int z = 0;
	private World w;
	public String yawName = "SOUTH";
	public int yaw = SOUTH;
	private boolean direction = true;
	
	public RewardTeleport(Quest q) {
		super(q);
		idItem = Material.ENDER_PEARL.getKey().toString(); // ender perle
	}
	@Override
	public IStateRew getState(StateQuestPlayer sqp) { return new StateTeleport(sqp); }
	@Override
	public int getType() { return TELEPORT; }
	@Override
	public String getStrType() { return STRTELEPORT; }
	@Override
	protected void messageGive(QuestPlayer qP) {
		qP.getPlayer().sendMessage(ChatColor.GRAY+descriptive);
	}
	@Override
	public String getParams() {
		return new StringBuilder(world).append(DELIMITER).append(x).append(DELIMITER).append(y).append(DELIMITER).append(z).append(DELIMITER).append(yawName).toString();
	}
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 4){
			world = params[0];
			x = Integer.valueOf(params[1]);
			y = Integer.valueOf(params[2]);
			z = Integer.valueOf(params[3]);
			w = Bukkit.getWorld(world);
			sqlSave();
		}else if(params.length == 5){
			world = params[0];
			x = Integer.valueOf(params[1]);
			y = Integer.valueOf(params[2]);
			z = Integer.valueOf(params[3]);
			yawName = params[4];
			w = Bukkit.getWorld(world);
			direction = true;
			if(yawName.equals("SOUTH")) yaw = SOUTH;
			else if(yawName.equals("NORTH")) yaw = NORTH;
			else if(yawName.equals("EAST")) yaw = EAST;
			else if(yawName.equals("WEST")) yaw = WEST;
			else if(yawName.equals("NO")) direction = false;
		}
	}
	// ---------------- STATETELEPORT ----------------
	class StateTeleport extends StateRewPlayer{

		public StateTeleport(StateQuestPlayer sqp) { super(sqp); }
		@Override
		public void give() {
			Player p = sqp.qp.getPlayer();
			if(w == null) w = Bukkit.getWorld(world);
			if(w != null && p != null){
				Location loc = new Location(w, x, y, z);
				if(!direction){
					yaw = (int) p.getLocation().getYaw();
					loc.setPitch(p.getLocation().getPitch());
				}
				loc.setYaw(yaw);
				p.teleport(loc);
				messageGive(sqp.qp);
			}
		}
	}
}