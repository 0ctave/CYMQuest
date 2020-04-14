package fr.craftyourmind.quest.mecha;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

@Deprecated
public class ActionTeleport extends Mechanism{

	public static final int NORTH = 180;
	public static final int SOUTH = 0;
	public static final int EAST = -90;
	public static final int WEST = 90;
	
	private MechaCoordRelative coordR = new MechaCoordRelative();
	private String yawName = "NO";
	private int yaw = SOUTH;
	private boolean direction = true;

	@Override
	public int getType() { return MechaType.ACTTELEPORT; }
	@Override
	public String getParams() {
		return 2+DELIMITER+coordR.getParams()+DELIMITER+yawName;
	}
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 4){
			coordR.setCoord(params[0], Integer.valueOf(params[1]), Integer.valueOf(params[2]), Integer.valueOf(params[3]), 0, false);
			sqlSave();
		}else if(params.length == 5){
			coordR.setCoord(params[0], Integer.valueOf(params[1]), Integer.valueOf(params[2]), Integer.valueOf(params[3]), 0, false);
			yawName = params[4];
			if(yawName.equals("SOUTH")) yaw = SOUTH;
			else if(yawName.equals("NORTH")) yaw = NORTH;
			else if(yawName.equals("EAST")) yaw = EAST;
			else if(yawName.equals("WEST")) yaw = WEST;
			sqlSave();
		}else{
			int version = Integer.valueOf(params[0]);
			int index = coordR.loadParams(1, params);
			yawName = params[index];
			direction = true;
			if(yawName.equals("SOUTH")) yaw = SOUTH;
			else if(yawName.equals("NORTH")) yaw = NORTH;
			else if(yawName.equals("EAST")) yaw = EAST;
			else if(yawName.equals("WEST")) yaw = WEST;
			else if(yawName.equals("NO")) direction = false;
		}
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateTP(this, mc, driver); }
	// ------------------ StateTP ------------------
	class StateTP extends AbsMechaStateEntity{

		public StateTP(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void start() {
			Entity e = driver.getEntity();
			if(e != null){
				Location loc = coordR.getLocationRandomRadius(driver);
				if(loc != null){
					if(!direction){
						yaw = (int) e.getLocation().getYaw();
						loc.setPitch(e.getLocation().getPitch());
					}
					loc.setYaw(yaw);
					e.teleport(loc);
					launchMessage();
				}
			}
		}
	}
}