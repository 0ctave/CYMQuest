package fr.craftyourmind.quest.mecha;

import java.util.ArrayList;
import java.util.List;

public class ToolMechaStop extends Mechanism{

	private static final int PLAYER = 0;
	private static final int ALL = 1;
	
	private int typeStop;
	private List<Integer> idStops = new ArrayList<Integer>();
	private List<Mechanism> mechaStops = new ArrayList<Mechanism>();
	
	@Override
	public int getType() { return MechaType.TOOSTOP; }
	@Override
	public String getParams() {
		String params = DELIMITER+idStops.size();
		for(Integer idm : idStops) params += DELIMITER+idm;
		return 0+DELIMITER+typeStop+params;
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
		typeStop = Integer.valueOf(params[index++]);
		int size = Integer.valueOf(params[index++]);
		idStops.clear();
		for(int i = 0 ; i < size ; i++)
			idStops.add(Integer.valueOf(params[index++]));
		init();
	}
	@Override
	public void init() {
		super.init();
		mechaStops.clear();
		for(Integer idm : idStops.toArray(new Integer[0])){
			Mechanism m =  getContainer().getMecha(idm);
			//if(m != null && m.isMechaStoppable()) mechaStops.add(m);
			//else idStops.remove(idm);
			if(m != null) mechaStops.add(m);
		}
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateStop(this, mc, driver); }
	// ------------------ StateStop ------------------
	class StateStop extends AbsMechaStateEntity{

		public StateStop(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void start() {
			if(typeStop == PLAYER){
				for(Mechanism m : mechaStops) driver.getControler(m).stop();
				
			}else if(typeStop == ALL) for(Mechanism m : mechaStops) m.stopControllers();
			launchMessage();
		}
	}
}