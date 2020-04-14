package fr.craftyourmind.quest.mecha;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import fr.craftyourmind.quest.QuestTools;

public class MechaCoordRelative {

	private static final int NO = 0;
	private static final int PLAYER = 1;
	private static final int PLAYERSOURCE = 2;
	private static final int LOCATION = 3;
	
	private String w = "";
	private World world;
	private FloatData x = new FloatData(), y = new FloatData(), z = new FloatData();
	private IntegerData radius = new IntegerData();
	private boolean ground;
	private Location loc;
	private int relative;
	
	public void setCoord(String w, float x, float y, float z, int radius, boolean ground){
		this.w = w;
		world = Bukkit.getWorld(w);
		this.x.set(x); this.y.set(y); this.z.set(z);
		loc = new Location(world, this.x.get(), this.y.get(), this.z.get());
		this.radius.set(radius);
		this.ground = ground;
	}
	
	public void setCoord(String w, String x, String y, String z, String radius, boolean ground){
		this.w = w;
		world = Bukkit.getWorld(w);
		this.x.load(x); this.y.load(y); this.z.load(z);
		loc = new Location(world, this.x.get(), this.y.get(), this.z.get());
		this.radius.load(radius);
		this.ground = ground;
	}
	
	public String getWorldName(){ return (world == null)?w:world.getName(); }
	public World getWorld(){ return world; }
	public float getX(){ return x.get(); }
	public float getY(){ return y.get(); }
	public float getZ(){ return z.get(); }
	public int getRadius(){ return radius.get(); }
	public boolean isOnGround(){ return ground; }
	public String getParams(){
		return new StringBuilder(getWorldName()).append(Mechanism.DELIMITER).append(x).append(Mechanism.DELIMITER).append(y).append(Mechanism.DELIMITER).append(z).append(Mechanism.DELIMITER).append(radius).append(Mechanism.DELIMITER).append(isOnGround()).append(Mechanism.DELIMITER).append(getRelative()).toString();
	}
	public int loadParams(int index, String[] params){
		setCoord(params[index++], params[index++], params[index++], params[index++], params[index++], Boolean.valueOf(params[index++]));
		setRelative(Integer.valueOf(params[index++]));
		return index;
	}
	
	public void setRelative(int relative) { this.relative = relative; }
	public int getRelative() { return relative; }
	
	public void setOnNo() { this.relative = NO; }
	public void setOnPlayer() { this.relative = PLAYER; }
	public void setOnPlayerSource() { this.relative = PLAYERSOURCE; }
	public void setOnLocation() { this.relative = LOCATION; }

	public boolean isNo(){ return relative == NO; }
	public boolean isOnPlayer(){ return relative == PLAYER; }
	public boolean isOnPlayerSource(){ return relative == PLAYERSOURCE; }
	public boolean isOnLocation(){ return relative == LOCATION; }

	public Location getLocationRelative(IMechaDriver driver) {
		if(relative == NO){ return loc;
		}else if(relative == PLAYER){
			if(driver.hasEntity()) return driver.getEntity().getLocation().add(x.get(), y.get(), z.get());
		}else if(relative == PLAYERSOURCE){
			if(driver.hasEntitySource()) return driver.getEntitySource().getLocation().add(x.get(), y.get(), z.get());
		}else if(relative == LOCATION){
			if(driver.hasLocation()) return driver.getLocation().add(x.get(), y.get(), z.get());
		}
		return null;
	}
	
	public Location getLocationRandomRadius(IMechaDriver driver){
		Location locR = getLocationRelative(driver);
		if(locR == null) return null;
		return QuestTools.getLocation(locR.getWorld(), locR.getX(), locR.getY(), locR.getZ(), radius.get(), isOnGround());
	}
	
	public void clone(AbsMechaStateEntity mse, MechaCoordRelative mcr){
		w = mcr.w;
		world = mcr.world;
		x.clone(mse, mcr.x);
		y.clone(mse, mcr.y);
		z.clone(mse, mcr.z);
		loc = new Location(world, x.get(), y.get(), z.get());
		radius.clone(mse, mcr.radius);
		ground = mcr.ground;
		relative = mcr.relative;
	}
}