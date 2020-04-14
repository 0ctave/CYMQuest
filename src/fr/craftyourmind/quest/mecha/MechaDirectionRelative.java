package fr.craftyourmind.quest.mecha;

import org.bukkit.Location;

public class MechaDirectionRelative {

	private static final int NO = 0;
	private static final int PLAYER = 1;
	private static final int LOCATION = 2;
	private static final int RELATIVE = 3;
	
	private static final float NORTH = 180;
	private static final float NORTHEAST = 225;
	private static final float EAST = 270;
	private static final float SOUTHEAST = 315;
	private static final float SOUTH = 0;
	private static final float SOUTHWEST = 45;
	private static final float WEST = 90;
	private static final float NORTHWEST = 135;
	
	private int relative;
	private FloatData yaw = new FloatData(), pitch = new FloatData();
	private boolean useDirection, dirYawRelative, dirPitchRelative;
	private MechaCoordRelative coordR = new MechaCoordRelative();
	
	public String getParams(){
		return new StringBuilder("0").append(Mechanism.DELIMITER).append(useDirection).append(Mechanism.DELIMITER).append(relative).append(Mechanism.DELIMITER).append(yaw).append(Mechanism.DELIMITER).append(pitch).append(Mechanism.DELIMITER).append(dirYawRelative).append(Mechanism.DELIMITER).append(dirPitchRelative).append(((relative == LOCATION)?Mechanism.DELIMITER+coordR.getParams():"")).toString();
	}
	
	public int loadParams(int index, String[] params){
		int version = Integer.valueOf(params[index++]);
		setUseDirect(Boolean.valueOf(params[index++]));
		setRelative(Integer.valueOf(params[index++]));
		setDirection(params[index++], params[index++], Boolean.valueOf(params[index++]), Boolean.valueOf(params[index++]));
		if(relative == LOCATION) index = coordR.loadParams(index, params);
		return index;
	}

	public void setDirection(float yaw, float pitch, boolean dirYawRelative, boolean dirPitchRelative) {
		this.yaw.set(yaw);
		this.pitch.set(pitch);
		this.dirYawRelative = dirYawRelative;
		this.dirPitchRelative = dirPitchRelative;
		if(this.yaw.get() < 0) this.yaw.set(0f); else if(this.yaw.get() > 360) this.yaw.set(360f);
		if(this.pitch.get() < -90) this.pitch.set(-90f); else if(this.pitch.get() > 90) this.pitch.set(90f);
	}
	
	public void setDirection(String yaw, String pitch, boolean dirYawRelative, boolean dirPitchRelative) {
		this.yaw.load(yaw);
		this.pitch.load(pitch);
		this.dirYawRelative = dirYawRelative;
		this.dirPitchRelative = dirPitchRelative;
		if(this.yaw.get() < 0) this.yaw.set(0f); else if(this.yaw.get() > 360) this.yaw.set(360f);
		if(this.pitch.get() < -90) this.pitch.set(-90f); else if(this.pitch.get() > 90) this.pitch.set(90f);
	}
	
	public void setUseDirect(boolean direct) { this.useDirection = direct; }
	
	public boolean useDirection(){ return useDirection; }
	
	public void setRelative(int relative) { this.relative = relative; }
	
	public void setOnNo() { this.relative = NO; }
	
	public void setOnPlayer() { this.relative = PLAYER; }
	
	public void setOnLocation() { this.relative = LOCATION; }
	
	public void setOnLocation(String w, int x, int y, int z) { coordR.setCoord(w, x, y, z, 0, false); }
	
	public void setOnLocationOnPlayer() { this.relative = LOCATION; coordR.setOnPlayer(); }
	
	public void setOnRelative() { this.relative = RELATIVE; }

	public boolean isNo(){ return relative == NO; }
	public boolean isPlayer(){ return relative == PLAYER; }
	public boolean isLocation(){ return relative == LOCATION; }
	public boolean isRelative(){ return relative == RELATIVE; }
	
	public boolean isYawRelative(){ return dirYawRelative; }
	public boolean isPitchRelative(){ return dirPitchRelative; }
	
	public Location getDirection(IMechaDriver driver) { return getDirection(driver, null); }
	
	public Location getDirection(IMechaDriver driver, Location locRef) {
		 float dir = yaw.get();
		 float ptch = pitch.get();
         Location direction = new Location(null, 0d, 0d, 0d);
         Location loc = null;
         
         if(relative == NO){
        	 
         }else  if(relative == PLAYER){
        	 if(driver.hasEntity()) loc = driver.getEntity().getLocation();
        	 
         }else  if(relative == LOCATION){
    		 if(coordR.isOnPlayer() && locRef != null) loc = locRef;
    		 else if(driver.hasEntity()) loc = driver.getEntity().getLocation();
        	 Location locRelative = coordR.getLocationRandomRadius(driver);
        	 if(loc != null && locRelative != null)
        	 	loc.setDirection(locRelative.toVector().subtract(loc.toVector()));
        	 
         }else  if(relative == RELATIVE){
        	 if(driver.hasDirection()) loc = driver.getDirection();
         }         
         if(loc != null){
        	 if(dirYawRelative) dir = yaw.get() + loc.getYaw();
    		 if(dirPitchRelative) ptch = pitch.get() + loc.getPitch();
         }
         if(dir < 0) dir += 360;
         direction.setYaw(dir);
         direction.setPitch(ptch);
         return direction;
	}
	
	public Location getDirectionVelocity(IMechaDriver driver, Location locRef) {
		Location loc = getDirection(driver, locRef);
		loc.setYaw(- loc.getYaw() - 90);
		if(dirPitchRelative) loc.setPitch(pitch.get() * 2 - loc.getPitch());
		return loc;
	}
	
	public Location getDirectionVelocity(IMechaDriver driver) { return getDirectionVelocity(driver, null); }

	public void setSouth() { yaw.set(SOUTH); }

	public void setNorth() { yaw.set(NORTH); }

	public void setEast() { yaw.set(EAST); }

	public void setWest() { yaw.set(WEST); }

	public String getCardinalName() { return getCardinalName(yaw.get()); }
	
	public String getCardinalName(float yaw) {
		if(yaw > NORTHWEST && yaw <= NORTHEAST) return "NORTH";
		else if(yaw > NORTHEAST && yaw <= SOUTHEAST) return "EAST";
		else if(yaw > SOUTHEAST || yaw <= SOUTHWEST) return "SOUTH";
		else if(yaw > SOUTHWEST && yaw <= NORTHWEST) return "WEST";
		return "";
	}
	
	public String getCardinalNorthSouth() { return getCardinalNorthSouth(yaw.get()); }
	
	public String getCardinalNorthSouth(float yaw) {
		if(yaw > WEST && yaw <= EAST) return "NORTH";
		else return "SOUTH";
	}
	
	public String getCardinalEastWest() { return getCardinalEastWest(yaw.get()); }
	
	public String getCardinalEastWest(float yaw) {
		if(yaw > SOUTH && yaw <= NORTH) return "WEST";
		else return "EAST";
	}
	
	public String getCardinalUpDown() { return getCardinalUpDown(pitch.get()); }
	
	public String getCardinalUpDown(float pitch) {
		if(pitch < 0) return "DOWN";
		else return "UP";
	}

	public void setCardinalNorthSouth(String facex) {
		if(facex.equals("NORTH")){
			if(getCardinalEastWest().equals(WEST)) yaw.set(NORTHWEST);
			else yaw.set(NORTHEAST);
		}else{
			if(getCardinalEastWest().equals(WEST)) yaw.set(SOUTHWEST);
			else yaw.set(SOUTHEAST);
		}
	}

	public void setCardinalUpDown(String facey) {
		if(facey.equals("UP")) pitch.set(0f);
		else pitch.set(-1f);
	}

	public void setCardinalEastWest(String facez) {
		if(facez.equals("EAST")){
			if(getCardinalNorthSouth().equals("NORTH")) yaw.set(NORTHEAST);
			else yaw.set(SOUTHEAST);
		}else {
			if(getCardinalNorthSouth().equals("NORTH")) yaw.set(NORTHWEST);
			else yaw.set(SOUTHWEST);
		}
	}
	
	public void clone(AbsMechaStateEntity mse, MechaDirectionRelative mdr){
		useDirection = mdr.useDirection;
		relative = mdr.relative;
		yaw.clone(mse, mdr.yaw);
		pitch.clone(mse, mdr.pitch);
		dirYawRelative = mdr.dirYawRelative;
		dirPitchRelative = mdr.dirPitchRelative;
	}
}