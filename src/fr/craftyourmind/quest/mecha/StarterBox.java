package fr.craftyourmind.quest.mecha;

import fr.craftyourmind.quest.MekaBox;

public class StarterBox extends Mechanism{
	
	private static final int ENTER = 0;
	private static final int EXIT = 1;
	
	private int idBox, action;
	private ToolBox toolBox;
	private MekaBox mbox;
	public boolean save = false;
	
	public StarterBox() { }
	
	public void setToolExit(ToolBox toolBox) {
		this.toolBox = toolBox;
		cleanControllers();
	}

	public void setEnter(MekaBox mbox){
		name = "enter";
		action = ENTER;
		idBox = mbox.getId();
		this.mbox = mbox;
	}
	
	public void setExit(MekaBox mbox){
		name = "exit";
		action = EXIT;
		idBox = mbox.getId();
		this.mbox = mbox;
	}
	
	public void initStarter(MekaBox mbox) {
		this.mbox = mbox;
		if(action == ENTER) mbox.addEnter(this); else mbox.addExit(this);
	}
	@Override
	public int getType() { return MechaType.STAMEKABOX; }
	@Override
	public String getParams() { return idBox+DELIMITER+action; }
	@Override
	public String getParamsGUI() { return mbox.getName(); }
	@Override
	protected void loadParams(String[] params) {
		idBox = Integer.valueOf(params[0]);
		action = Integer.valueOf(params[1]);
	}
	@Override
	public void sqlCreate() {
		if(save) super.sqlCreate();
		else getContainer().removeMecha(this);
	}
	@Override
	public void sqlDelete() { if(save) super.sqlDelete(); }
	@Override
	public Mechanism cloneRename(IMechaContainer con, boolean links) { return null; }
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateBox(this, mc, driver); }
	// ------------------ StateBox ------------------
	class StateBox extends AbsMechaStateEntity{

		private MechaControler mctoolbox;
		
		public StateBox(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void start() {
			if(action == ENTER) launchMessage();
			else if(toolBox != null && action == EXIT){
				sendMessage();
				if(mctoolbox == null) mctoolbox = driver.getControler(toolBox);
				mctoolbox.launch();
			}
		}
	}
}