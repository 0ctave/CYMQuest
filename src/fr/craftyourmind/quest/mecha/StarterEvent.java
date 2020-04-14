package fr.craftyourmind.quest.mecha;

import fr.craftyourmind.quest.EventParadise;
import fr.craftyourmind.quest.QuestPlayer;

public class StarterEvent extends Mechanism{

	public static final int START = 0;
	public static final int STOP = 1;
	public static final int BEGIN = 2;
	public static final int FINISH = 3;
	public static final int STARTUNITARY = 4;
	public static final int STOPUNITARY = 5;
	public static final int INITPREPARATION = 6;
	public static final int INITUNDERWAY = 7;
	public static final int IFPREPARATION = 8;
	public static final int IFUNDERWAY= 9;
	
	public int idEvent = 0;
	public int action = 0;
	public EventParadise ep;
	
	public StarterEvent() {}
	@Override
	public void init() {
		if(ep == null) ep = EventParadise.get(idEvent);
		if(ep != null){
			if(ep.mechaStart != null && ep.mechaStart.id == id) ep.mechaStart = null;
			if(ep.mechaStop != null && ep.mechaStop.id == id) ep.mechaStop = null;
			if(ep.mechaBegin != null && ep.mechaBegin.id == id) ep.mechaBegin = null;
			if(ep.mechaFinish != null && ep.mechaFinish.id == id) ep.mechaFinish = null;
			if(ep.mechaStartUnitary != null && ep.mechaStartUnitary.id == id) ep.mechaStartUnitary = null;
			if(ep.mechaStopUnitary != null && ep.mechaStopUnitary.id == id) ep.mechaStopUnitary = null;
			
			if(action == START) ep.mechaStart = this;
			else if(action == STOP) ep.mechaStop = this;
			else if(action == BEGIN) ep.mechaBegin = this;
			else if(action == FINISH) ep.mechaFinish = this;
			else if(action == STARTUNITARY){ 
				ep.mechaStartUnitary = this;
				if(ep.isStarted()) start();
			}else if(action == STOPUNITARY) ep.mechaStopUnitary = this;
		}
	}
	@Override
	public void start() { start(getContainer().newDriver(QuestPlayer.nobody)); }
	@Override
	public int getType() { return MechaType.STAEVENT; }
	@Override
	public String getParams() { return idEvent+DELIMITER+action; }
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		idEvent = Integer.valueOf(params[0]);
		action = Integer.valueOf(params[1]);
		ep = EventParadise.get(idEvent);
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateEvent(this, mc, driver); }
	// ------------------ StateEvent ------------------
	class StateEvent extends AbsMechaStateEntity{

		public StateEvent(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void start() {
			if(ep != null){
				if(action == INITPREPARATION){
					ep.initPreparation();
					launchMessage();
				}else if(action == INITUNDERWAY){
					ep.initUnderway();
					launchMessage();
				}else if(action == IFPREPARATION){
					if(ep.isStatePreparation()) launchMessage();
				}else if(action == IFUNDERWAY){
					if(ep.isStateUnderway()) launchMessage();
				}else launchMessage();
			}
		}
	}
}