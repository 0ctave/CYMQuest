package fr.craftyourmind.quest.mecha;

import org.bukkit.entity.Entity;
import fr.craftyourmind.manager.util.IChecker;
import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.mecha.MechaControler.Mode;

public abstract class AbsMechaStateEntity implements IChecker, IMechaParamManager{
	protected IMechaDriver driver;
	public QuestPlayer qp;
	private boolean active = false;
	private boolean isStart = false;
	private Mechanism m;
	private MechaControler mc;
	private StringData message = new StringData();
	private AbsMechaStateEntity stateExternalTool;
	
	public AbsMechaStateEntity(Mechanism m, MechaControler mc, IMechaDriver driver) {
		this.m = m;
		this.mc = mc;
		this.driver = driver;
		qp = driver.getQuestPlayer();
		mc.addState(this);
		m.getContainer().addConParams(driver);
		
		if(m.getExternalTool() != null){
			MechaControler mcExternalTool = driver.getControler(m.getExternalTool());
			if(mcExternalTool.getStates().isEmpty())
				stateExternalTool = m.getExternalTool().initState(mcExternalTool, driver);
			else stateExternalTool = mcExternalTool.getStates().get(0);
		}
	}
	
	public Mode getMode(){ return Mode.ONE; }
	
	public void start(){
		isStart = true;
		active = true;
	}
	public void stop(){
		isStart = false;
		active = false;
		mc.stop(this);
	}
	
	public boolean checker(){
		if(check()){
			if(isStart && m.common){
				for(MechaControler mc : m.getControllers().toArray(new MechaControler[0])){
					for(AbsMechaStateEntity mse : mc.getStates().toArray(new AbsMechaStateEntity[0]))
						if(mse.isActive()) mse.afterCheckOK();
				}
				if(m.single) launch();
			}else
				afterCheckOK();
			return true;
		}
		return false;
	}
	public boolean check() { return false; }
	
	public void launchMessage(){
		sendMessage();
		launch();
	}
	
	protected void afterCheckOK(){
		sendMessage();
		if(!m.permanent) stop();
		if(!m.single) launch();
	}

	public void cloneData(){ message.clone(this, m.message); }
	@Override
	public MechaParam getMechaParam(String param){ return driver.getMechaParam(m.getContainer(), param); }
	
	public int getId() { return m.id; }
	
	public Mechanism getMechanism(){ return m; }

	public MechaControler getController(){ return mc; }
	
	public Entity getEntity(){ return driver.getEntity(); }
	
	public void launch() {
		mc.launch();
		if(stateExternalTool != null)
			stateExternalTool.launchMessage();
	}
	
	public void sendMessage(){ if(!message.toString().isEmpty()) driver.sendMessage(message.get()); }
	
	public boolean isActive(){ return active; }
	
	public void setActive(boolean b){ active = b; }
	
	public void sqlStart() { driver.sqlStart(this); }
	
	public void sqlStop() { driver.sqlStop(this); }
}