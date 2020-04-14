package fr.craftyourmind.quest.mecha;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import fr.craftyourmind.manager.CYMChecker;
import fr.craftyourmind.manager.CYMChecker.ICheckerPosition;
import fr.craftyourmind.manager.PeriodicCheck;
import fr.craftyourmind.manager.checker.Position;
import fr.craftyourmind.manager.checker.PositionMeta;
import fr.craftyourmind.quest.QuestTools;
import fr.craftyourmind.quest.mecha.MechaControler.Mode;

public class TriggerLocation extends Mechanism{

	public MechaCoordRelative coordR = new MechaCoordRelative();
	public boolean outside = false;
	public IntegerData time = new IntegerData();
	public boolean notice = false;

	@Override
	public boolean isMechaStoppable(){ return true; }
	@Override
	public int getType() { return MechaType.TRILOCATION; }
	@Override
	public String getParams() {
		return 1+DELIMITER+coordR.getParams()+DELIMITER+outside+DELIMITER+time+DELIMITER+notice;
	}
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 8){
			coordR.setCoord(params[0], Integer.valueOf(params[1]), Integer.valueOf(params[2]), Integer.valueOf(params[3]), Integer.valueOf(params[4]), false);
			outside = Boolean.valueOf(params[5]);
			time.load(params[6]);
			notice = Boolean.valueOf(params[7]);
			sqlSave();
		}else{
			int version = Integer.valueOf(params[0]);
			int index = coordR.loadParams(1, params);
			outside = Boolean.valueOf(params[index++]);
			time.load(params[index++]);
			notice = Boolean.valueOf(params[index++]);
		}
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateLocation(this, mc, driver); }
	// ------------------ StateLocation ------------------
	class StateLocation extends AbsMechaStateEntitySave implements ICheckerPosition{

		public MechaCoordRelative coordR = new MechaCoordRelative();
		public IntegerData time = new IntegerData();
		
		private long timestamps = 0;
		private int currentTime = 0;
		private boolean isInsideNow = false; // no flood message
		
		private Position pos;
		private PositionMeta posM;
		private Location loc;
		
		public StateLocation(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void cloneData() {
			super.cloneData();
			coordR.clone(this, TriggerLocation.this.coordR);
			time.clone(this, TriggerLocation.this.time);
			currentTime = time.get();
		}
		@Override
		public Mode getMode() { return Mode.MULTIPLE; }
		@Override
		public void start() {
			super.start();
			loc = coordR.getLocationRelative(driver);
			if(driver.isPlayer()) pos = CYMChecker.startPosition(qp.getCYMPlayer(), this);
			else posM = CYMChecker.startPositionMeta(driver.getChecker(), this);
			if(driver.getEntity() != null) isInsideNow = QuestTools.insideLoc(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), driver.getEntity().getLocation(), coordR.getRadius());
		}
		@Override
		public boolean check() {
			if(driver.getEntity() != null){
				if(QuestTools.insideLoc(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), driver.getEntity().getLocation(), coordR.getRadius())){ // ------ Inside
					if(outside) {
						if(!isInsideNow) sendNotice(ChatColor.GRAY+"+++ OUTSIDE ZONE "+name+" +++");
						currentTime = time.get();
						isInsideNow = true;
						return false;
					}else{
						if(time.get() == 0){
							sendNotice(ChatColor.GRAY+"+++ ZONE "+name+" +++");
							PeriodicCheck.remove(this);
							isInsideNow = true;
							return true;
						}else if(currentTime == time.get()){
							timestamps = System.currentTimeMillis();
							sendNotice(ChatColor.GRAY+"+++ ZONE "+name+" "+currentTime+" sec +++");
							currentTime--;
						}else{
							long diff = System.currentTimeMillis() - timestamps;
							if(diff > 1000){
								timestamps = System.currentTimeMillis();
								if(currentTime < 6 || currentTime == 10 || currentTime == 20)
									sendNotice(ChatColor.GRAY+"+++ ZONE "+name+" "+currentTime+" sec +++");
								currentTime--;
								if(currentTime < 0){
									PeriodicCheck.remove(this);
									isInsideNow = true;
									return true;
								}
							}
						}
						isInsideNow = true;
						return false;
					}
				}else{ // ------ Outside
					if(outside){
						if(time.get() == 0){
							sendNotice(ChatColor.GRAY+"+++ ZONE "+name+" +++");
							PeriodicCheck.remove(this);
							isInsideNow = false;
							return true;
						}else if(currentTime == time.get()){
							timestamps = System.currentTimeMillis();
							sendNotice(ChatColor.GRAY+"+++ ZONE "+name+" "+currentTime+" sec +++");
							currentTime--;
						}else{
							long diff = System.currentTimeMillis() - timestamps;
							if(diff > 1000){
								timestamps = System.currentTimeMillis();
								if(currentTime < 6 || currentTime == 10 || currentTime == 20)
									sendNotice(ChatColor.GRAY+"+++ ZONE "+name+" "+currentTime+" sec +++");
								currentTime--;
								if(currentTime < 0){
									PeriodicCheck.remove(this);
									isInsideNow = false;
									return true;
								}
							}
						}
						isInsideNow = false;
						return false;
					}else{
						if(isInsideNow) sendNotice(ChatColor.GRAY+"+++ OUTSIDE ZONE "+name+" +++");
					}
					isInsideNow = false;
				}
			}
			return false;
		}
		
		private void sendNotice(String msg){ if(notice) driver.sendMessage(msg); }
		@Override
		public void stop() {
			super.stop();
			if(pos != null) pos.stop();
			else if(posM != null) posM.stop();
			PeriodicCheck.remove(this);
		}
		@Override
		public void inside() {
			currentTime = time.get();
			if(outside){ PeriodicCheck.remove(this); check(); }
			else{ PeriodicCheck.add(this); checker(); }
		}
		@Override
		public void outside() {
			currentTime = time.get();
			if(outside){ PeriodicCheck.add(this); checker(); }
			else{ PeriodicCheck.remove(this); check(); }
		}
		@Override
		public int getRadius() { return coordR.getRadius(); }
		@Override
		public String getWorld() { return loc.getWorld().getName(); }
		@Override
		public int getX() { return loc.getBlockX(); }
		@Override
		public int getY() { return loc.getBlockY(); }
		@Override
		public int getZ() { return loc.getBlockZ(); }
	}
}