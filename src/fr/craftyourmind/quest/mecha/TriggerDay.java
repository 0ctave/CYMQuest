package fr.craftyourmind.quest.mecha;

import org.bukkit.Bukkit;
import org.bukkit.World;
import fr.craftyourmind.quest.Plugin;

public class TriggerDay extends Mechanism{

	public String world = "world";
	public World w;
	private IntegerData tickBegin = new IntegerData();
	private IntegerData tickEnd = new IntegerData();
	
	private int idTask = 0;
	private long prefulltime = 0;
	
	public TriggerDay() { common = true; }
	@Override
	public boolean isMechaStoppable(){ return true; }
	
	private boolean isInInterval(int begin, int end, long time){
		if(begin > end){
			end += 24000;
			if(time >= 0 && time < begin) time += 24000;
		}
		return time >= begin && time < end;
	}
	private void stopTimer(){
		Bukkit.getScheduler().cancelTask(idTask);
		idTask = 0;
	}
	@Override
	public int getType() { return MechaType.TRIDAY; }
	@Override
	public String getParams() { return world+DELIMITER+tickBegin+DELIMITER+tickEnd; }
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		world = params[0];
		tickBegin.load(params[1]);
		tickEnd.load(params[2]);
		w = Bukkit.getWorld(world);
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateDay(this, mc, driver); }
	// ------------------ StateDay ------------------
	class StateDay extends AbsMechaStateEntitySave implements Runnable{

		private IntegerData tickBegin = new IntegerData();
		private IntegerData tickEnd = new IntegerData();
		
		public StateDay(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void cloneData() {
			super.cloneData();
			tickBegin.clone(this, TriggerDay.this.tickBegin);
			tickEnd.clone(this, TriggerDay.this.tickEnd);
		}
		@Override
		public void start() {
			super.start();
			if(w == null) w = Bukkit.getWorld(world);
			if(w != null){ if(idTask == 0) go(); }
		}
		@Override
		public boolean check() { return true; }
		@Override
		public void stop() {
			super.stop();
			if(!hasCurrentStatesActive()) stopTimer();
		}
		
		private void go(){
			idTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Plugin.it, this, 0, 1000);
		}
		@Override
		public void run() {
			long time = w.getTime();
			long fulltime = w.getFullTime();
			if(tickBegin.get() >= 0 && tickEnd.get() == 0){
				if(isInInterval(tickBegin.get(), tickBegin.get()+1000, time)){
					if(hasCurrentStatesActive()) getStateActive().checker();
					if(!permanent) stopTimer();
				}
			}else if(isInInterval(tickBegin.get(), tickEnd.get(), time)){
				if(hasCurrentStatesActive()) getStateActive().checker();
				if(!permanent) stopTimer();
			}
			prefulltime = fulltime;
		}
	}
}