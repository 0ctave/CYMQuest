package fr.craftyourmind.quest.mecha;

import java.util.ArrayList;
import java.util.List;

import fr.craftyourmind.quest.mecha.Mechanism.ChildLink;

public class MechaControler {

	enum Mode{ ONE, MULTIPLE; } // ONE = one instance, never replaced - MULTIPLE = multiple instance, create new each start
	
	private IMechaDriver driver;
	private Mechanism m;
	
	private List<MechaControler> parents = new ArrayList<MechaControler>();
	private List<MechaControler> childs = new ArrayList<MechaControler>();
	
	private List<AbsMechaStateEntity> states = new ArrayList<AbsMechaStateEntity>();
	
	private Mode mode = Mode.ONE;
	
	public MechaControler(Mechanism m, IMechaDriver driver) {
		this.m = m;
		this.driver = driver;
		driver.addController(this);
		m.addController(this);
		linkChilds();
	}
	
	public void linkChilds(){
		for(ChildLink cl : m.childLinks) link(cl.get());
	}
	
	public void link(Mechanism child) {
		MechaControler mc = driver.getControler(child);
		childs.add(mc);
		mc.parents.add(this);
	}	
	
	public void delLinks(){
		for(MechaControler mc : parents) mc.childs.remove(this);
		for(MechaControler mc : childs) mc.parents.remove(this);
		parents.clear();
		childs.clear();
	}
	
	public void delLink(Mechanism child) {
		for(MechaControler mc : childs.toArray(new MechaControler[0])){
			if(mc.getId() == child.getId()){
				mc.parents.remove(this);
				childs.remove(mc);
			}
		}
	}
	
	public int getId() {
		return m.id;
	}

	public void start() {
		if(states.isEmpty()){
			AbsMechaStateEntity mse = m.initState(this, driver);
			mode = mse.getMode();
			mse.start();
			
		}else if(mode == Mode.ONE){
			states.get(0).start();
			
		}else if(mode == Mode.MULTIPLE){
			AbsMechaStateEntity mse = m.initState(this, driver);
			mse.start();
		}
	}
	
	public void stop(AbsMechaStateEntity mse) {
		if(mode == Mode.MULTIPLE) states.remove(mse);
	}
	
	public void stop(){
		for(AbsMechaStateEntity mse : states.toArray(new AbsMechaStateEntity[0])) mse.stop();
	}
	
	public void clean(){
		stop();
		states.clear();
		delLinks();
		driver.removeController(this);
		m.removeController(this);
	}
	
	public void launch() {
		for(MechaControler mc : childs.toArray(new MechaControler[0])) mc.start();
	}

	public List<AbsMechaStateEntity> getStates() {
		return states;
	}
	
	public void addState(AbsMechaStateEntity mse){
		states.add(mse);
	}

	public boolean hasCurrentStatesActive() {
		for(AbsMechaStateEntity mse : states) if(mse.isActive()) return true;
		return false;
	}
	
	public AbsMechaStateEntity getStateActive() {
		for(AbsMechaStateEntity mse : states) if(mse.isActive()) return mse;
		return null;
	}
	
	public void updateData(){
		boolean isActive = false;
		for(AbsMechaStateEntity mse : states.toArray(new AbsMechaStateEntity[0])){
			if(!isActive) isActive = mse.isActive();
			mse.stop();
		}
		for(AbsMechaStateEntity mse : states) mse.cloneData();
		if(isActive) start();
	}
	
	public int getTypeCon(){ return m.getTypeCon(); }
	public int getIdCon(){ return m.getIdCon(); }
	public IMechaDriver getDriver(){ return driver; }
	public List<MechaControler> getParents(){ return parents; }
}