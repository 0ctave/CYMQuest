package fr.craftyourmind.quest.mecha;

import java.util.Random;

import org.bukkit.Bukkit;
import fr.craftyourmind.manager.CYMChecker;
import fr.craftyourmind.manager.CYMChecker.ICheckerTextScreen;
import fr.craftyourmind.manager.CYMTextScreen;
import fr.craftyourmind.manager.checker.TextScreen;
import fr.craftyourmind.quest.Plugin;
import fr.craftyourmind.quest.QuestTextScreen;
import fr.craftyourmind.quest.mecha.MechaControler.Mode;

public class TriggerTimer extends Mechanism implements Runnable{
	
	public IntegerData hour = new IntegerData();
	public IntegerData minute = new IntegerData();
	public IntegerData second = new IntegerData(20);
	public IntegerData tenth = new IntegerData();
	
	public IntegerData hourdelay = new IntegerData();
	public IntegerData minutedelay = new IntegerData();
	public IntegerData seconddelay = new IntegerData();
	public IntegerData tenthdelay = new IntegerData();
	
	public IntegerData nbTime = new IntegerData();
	public QuestTextScreen ts = new QuestTextScreen();
	
	private int nbTimeCom = 0;
	private int currentNbTime = 0;
	private int idTaskCommon = 0;
	
	public TriggerTimer() {
		ts.useTextScreen = false;
	}
	@Override
	public boolean isMechaStoppable(){ return true; }
	@Override
	public void run() {
		if(permanent && nbTimeCom > 0){
			currentNbTime++;
			if(currentNbTime >= nbTimeCom) stopCommon();
		}
		if(!permanent) idTaskCommon = 0;
		if(hasCurrentStatesActive()) ((StateTimer)getStateActive()).run();
	}
	private void stopCommon(){
		Bukkit.getScheduler().cancelTask(idTaskCommon);
		idTaskCommon = 0;
		currentNbTime = 0;
	}
	@Override
	public int getType() { return MechaType.TRITIMER; }
	@Override
	public String getParams() {
		return new StringBuilder("1").append(DELIMITER).append(hour).append(DELIMITER).append(minute).append(DELIMITER).append(second).append(DELIMITER).append(tenth).append(DELIMITER)
				.append(hourdelay).append(DELIMITER).append(minutedelay).append(DELIMITER).append(seconddelay).append(DELIMITER).append(tenthdelay).append(DELIMITER)
				.append(nbTime).append(DELIMITER).append(ts.getParams()).toString();
	}
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 3){
			hour.load(params[0]);
			minute.load(params[1]);
			second.load(params[2]);
			sqlSave();
		}else if(params.length == 6){
			hour.load(params[0]);
			minute.load(params[1]);
			second.load(params[2]);
			hourdelay.load(params[3]);
			minutedelay.load(params[4]);
			seconddelay.load(params[5]);
			sqlSave();
		}else if(params.length == 9){
			hour.load(params[0]);
			minute.load(params[1]);
			second.load(params[2]);
			tenth.load(params[3]);
			hourdelay.load(params[4]);
			minutedelay.load(params[5]);
			seconddelay.load(params[6]);
			tenthdelay.load(params[7]);
			nbTime.load(params[8]);
			sqlSave();
		}else if(params.length > 9){
			int index = 0;
			int version = Integer.valueOf(params[index++]);
			hour.load(params[index++]);
			minute.load(params[index++]);
			second.load(params[index++]);
			tenth.load(params[index++]);
			hourdelay.load(params[index++]);
			minutedelay.load(params[index++]);
			seconddelay.load(params[index++]);
			tenthdelay.load(params[index++]);
			nbTime.load(params[index++]);
			ts.load(index, params);
		}
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateTimer(this, mc, driver); }
	// ------------------ StateTimer ------------------
	class StateTimer extends AbsMechaStateEntitySave implements ICheckerTextScreen, Runnable{
		
		private IntegerData hour = new IntegerData();
		private IntegerData minute = new IntegerData();
		private IntegerData second = new IntegerData(20);
		private IntegerData tenth = new IntegerData();
		
		private IntegerData hourdelay = new IntegerData();
		private IntegerData minutedelay = new IntegerData();
		private IntegerData seconddelay = new IntegerData();
		private IntegerData tenthdelay = new IntegerData();
		
		private IntegerData nbTime = new IntegerData();
		private QuestTextScreen ts = new QuestTextScreen();
		private TextScreen cts;
		
		private int idTask = 0;
		private boolean state = false;
		private int currentNbTime = 0;
		
		public StateTimer(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void cloneData() {
			super.cloneData();
			hour.clone(this, TriggerTimer.this.hour);
			minute.clone(this, TriggerTimer.this.minute);
			second.clone(this, TriggerTimer.this.second);
			tenth.clone(this, TriggerTimer.this.tenth);
			hourdelay.clone(this, TriggerTimer.this.hourdelay);
			minutedelay.clone(this, TriggerTimer.this.minutedelay);
			seconddelay.clone(this, TriggerTimer.this.seconddelay);
			tenthdelay.clone(this, TriggerTimer.this.tenthdelay);
			nbTime.clone(this, TriggerTimer.this.nbTime);
			ts.clone(this, TriggerTimer.this.ts);
		}
		@Override
		public Mode getMode() { return Mode.MULTIPLE; }
		@Override
		public void start() {
			super.start();
			if(common){ if(idTaskCommon == 0) idTaskCommon = go(TriggerTimer.this, hour.get(), minute.get(), second.get(), tenth.get(), hourdelay.get(), minutedelay.get(), seconddelay.get(), tenthdelay.get(), nbTime.get());
			}else idTask = go(this, hour.get(), minute.get(), second.get(), tenth.get(), hourdelay.get(), minutedelay.get(), seconddelay.get(), tenthdelay.get(), nbTime.get());
		}
		private int go(Runnable run, int h, int m, int s, int t, int hd, int md, int sd, int td, int nb){
			nbTimeCom = nb;
			Random rand = new Random();
			hd = (hd == 0)?0:rand.nextInt(hd);
			md = (md == 0)?0:rand.nextInt(md);
			sd = (sd == 0)?0:rand.nextInt(sd);
			td = (td == 0)?0:rand.nextInt(td);
			long time = ((((h+hd)*60*60)+((m+md)*60)+(s+sd))*20)+((t+td)*2);
			if(ts.useTextScreen && driver.isPlayer()){
				ts.timer.set((int) time/20);
				if(cts == null) cts = CYMChecker.startTextScreen(qp.getCYMPlayer(), this);
				else cts.start();
			}
			if(permanent) return Bukkit.getScheduler().scheduleSyncRepeatingTask(Plugin.it, run, time, time);
			else return Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.it, run, time);
		}
		@Override
		public boolean check() { return state; }
		@Override
		public void run() {
			state = true;
			checker();
			if(permanent){
				if(nbTime.get() > 0){
					currentNbTime++;
					if(currentNbTime >= nbTime.get()) stop();
					else{
						if(cts != null) cts.start();
					}
				}else{
					if(cts != null) cts.start();
				}
			}
		}
		@Override
		public void stop() {
			super.stop();
			if(cts != null) cts.stop();
			Bukkit.getScheduler().cancelTask(idTask);
			if(common && !hasCurrentStatesActive()) stopCommon();
		}
		@Override
		public CYMTextScreen getTextScreen() { return ts; }
	}
}