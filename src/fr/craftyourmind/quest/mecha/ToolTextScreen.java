package fr.craftyourmind.quest.mecha;

import org.bukkit.Bukkit;
import fr.craftyourmind.manager.CYMChecker.ICheckerTextScreen;
import fr.craftyourmind.manager.CYMChecker;
import fr.craftyourmind.manager.CYMTextScreen;
import fr.craftyourmind.manager.checker.TextScreen;
import fr.craftyourmind.quest.Plugin;
import fr.craftyourmind.quest.QuestTextScreen;

public class ToolTextScreen extends Mechanism{

	private QuestTextScreen ts = new QuestTextScreen();
	
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) {
		return new StateTextScreen(this, mc, driver);
	}
	@Override
	public int getType() { return MechaType.TOOTEXT; }
	@Override
	public boolean isMechaStoppable() { return true; }
	@Override
	public String getParams() {
		return "0"+DELIMITER+ts.getParams();
	}
	@Override
	public String getParamsGUI() {
		StringBuilder params = new StringBuilder();
		int size = 0;
		for(Mechanism m : getContainer().getMechas()){
			if(m.isMechaStoppable()){
				params.append(DELIMITER).append(m.id).append(DELIMITER).append(m.name);
				size++;
			}
		}
		return params.insert(0, size).toString();
	}
	@Override
	protected void loadParams(String[] params) {
		int index = 0;
		int version = Integer.valueOf(params[index++]);
		ts.load(index, params);
	}
	// ------------------ StateTextScreen ------------------
	class StateTextScreen extends AbsMechaStateEntitySave implements ICheckerTextScreen{

		private QuestTextScreen ts = new QuestTextScreen();
		private TextScreen cts;
		
		public StateTextScreen(Mechanism m, MechaControler mc, IMechaDriver driver) {
			super(m, mc, driver);
		}
		@Override
		public void cloneData() {
			super.cloneData();
			ts.clone(this, ToolTextScreen.this.ts);
		}
		@Override
		public void start() {
			super.start();
			if(driver.isPlayer()){
				if(cts == null) cts = CYMChecker.startTextScreen(qp.getCYMPlayer(), this);
				else cts.start();
				launchMessage();
				if(ts.timer.get() > 0){
					 Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.it, new Runnable() {
						@Override
						public void run() { stop(); }
					}, ts.timer.get()*20);
				}
			}
		}
		@Override
		public void stop() {
			super.stop();
			if(cts != null) cts.stop();
		}
		@Override
		public CYMTextScreen getTextScreen() { return ts; }
	}
}