package fr.craftyourmind.quest;

import fr.craftyourmind.manager.CYMChecker;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import fr.craftyourmind.manager.checker.Position;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class ObjectiveLocation extends AbsObjective{
	
	public String world = "";
	public int x = 0;
	public int y = 0;
	public int z = 0;
	public int radius = 0;
	public boolean stayinside = false;
	public int time = 0;
	
	public ObjectiveLocation(Quest q) {
		super(q);
		alertItem = Material.COMPASS.getKey().toString();
	}
	@Override
	public IStateObj getState(StateQuestPlayer sqp) { return new StateLocation(sqp); }
	@Override
	public int getType() { return LOCATION; }
	@Override
	public String getStrType() { return STRLOCATION; }
	@Override
	public String getParams() {
		return new StringBuilder(world).append(DELIMITER).append(x).append(DELIMITER).append(y).append(DELIMITER).append(z).append(DELIMITER).append(radius).append(DELIMITER).append(stayinside).append(DELIMITER).append(time).toString();
	}
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		world = params[0];
		x = Integer.valueOf(params[1]);
		y = Integer.valueOf(params[2]);
		z = Integer.valueOf(params[3]);
		radius = Integer.valueOf(params[4]);
		stayinside = Boolean.valueOf(params[5]);
		time = Integer.valueOf(params[6]);
	}
	// ---------------- STATELOCATION ----------------
	class StateLocation extends StateObjPlayer implements CYMChecker.ICheckerPosition {
		
		private long timestamps = 0;
		private int currentTime = time;
		private boolean isInsideNow = false; // no flood message
		private Position pos;
		
		public StateLocation(StateQuestPlayer sqp) {
			super(sqp);
			timestamps = System.currentTimeMillis();
		}
		@Override
		public boolean check() {
			if(sqp.qp.getPlayer() != null){
				if(QuestTools.insideLoc(world, x, y, z, sqp.qp.getPlayer().getLocation(), radius)){ // ------ Inside
					if(stayinside) {
						currentTime = time;
						if(!isInsideNow) sqp.qp.sendMessage(ChatColor.GRAY+"+++ ZONE "+descriptive+" +++");
						isInsideNow = true;
						return true;
					}else{
						if(time == 0){
							terminate();
							sendMessagesSuccess();
							isInsideNow = true;
							return true;
							
						}else if(currentTime == time){
							timestamps = System.currentTimeMillis();
							sqp.qp.sendMessage(ChatColor.GRAY+"+++ ZONE "+descriptive+" "+currentTime+" sec +++");
							currentTime--;
						}else{
							long diff = System.currentTimeMillis() - timestamps;
							if(diff > 1000){
								timestamps = System.currentTimeMillis();
								if(currentTime < 6 || currentTime == 10 || currentTime == 20)
									sqp.qp.sendMessage(ChatColor.GRAY+"+++ ZONE "+descriptive+" "+currentTime+" sec +++");
								currentTime--;
								if(currentTime < 0){
									terminate();
									sendMessagesSuccess();
									isInsideNow = true;
									return true;
								}
							}
						}
						isInsideNow = true;
						return false;
					}
				}else{ // ------ Outside
					if(stayinside){
						if(time == 0){
							if(isInsideNow) sqp.qp.sendMessage(ChatColor.GRAY+"+++ OUTSIDE ZONE "+descriptive+" +++");
							removeCheckerPeriodic(this);
							isInsideNow = false;
						}else if(currentTime == time){
							timestamps = System.currentTimeMillis();
							sqp.qp.sendMessage(ChatColor.GRAY+"+++ OUTSIDE ZONE "+descriptive+" "+currentTime+" sec +++");
							currentTime--;
						}else{
							long diff = System.currentTimeMillis() - timestamps;
							if(diff > 1000){
								timestamps = System.currentTimeMillis();
								if(currentTime < 6 || currentTime == 10 || currentTime == 20)
									sqp.qp.sendMessage(ChatColor.GRAY+"+++ OUTSIDE ZONE "+descriptive+" "+currentTime+" sec +++");
								currentTime--;
								if(currentTime < 0){
									sqp.decline();
									messageSuccess(sqp.qp);
									alertSuccess(sqp.qp, "quest.objective.fail");
									sqp.getQuest().messageLose(sqp.qp);
									isInsideNow = false;
								}
							}
						}
						isInsideNow = false;
						return false;
					}else{
						if(isInsideNow) sqp.qp.sendMessage(ChatColor.GRAY+"+++ OUTSIDE ZONE "+descriptive+" +++");
					}
					isInsideNow = false;
				}
			}
			currentTime = time;
			return false;
		}
		@Override
		public void begin() {
			if(!isTerminate())
				pos = CYMChecker.startPosition(sqp.qp.getCYMPlayer(), this);
			if(sqp.qp.getPlayer() != null) isInsideNow = QuestTools.insideLoc(world, x, y, z, sqp.qp.getPlayer().getLocation(), radius);
		}
		@Override
		public void terminate() {
			super.terminate();
			if(pos != null) pos.stop();
			removeCheckerPeriodic(this);
		}
		@Override
		public void finish() { clean(); }
		@Override
		public void clean() {
			if(pos != null) pos.stop();
			removeCheckerPeriodic(this);
			if(isTerminate())
				sqlClean(sqp.qp);
		}
		@Override
		public void inside() {
			if(!isInsideNow) currentTime = time;
			if(stayinside){ removeCheckerPeriodic(this); check(); }
			else addCheckerPeriodic(this);
		}
		@Override
		public void outside() {
			if(isInsideNow) currentTime = time;
			if(stayinside) addCheckerPeriodic(this);
			else{ removeCheckerPeriodic(this); check(); }
		}
		@Override
		public int getRadius() { return radius; }
		@Override
		public String getWorld() { return world; }
		@Override
		public int getX() { return x; }
		@Override
		public int getY() { return y; }
		@Override
		public int getZ() { return z; }
	}
}