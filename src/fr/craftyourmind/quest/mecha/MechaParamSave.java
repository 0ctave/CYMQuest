package fr.craftyourmind.quest.mecha;

public class MechaParamSave extends MechaParam{
	
	private IMechaParamSave mps;
	
	public MechaParamSave(boolean lock, String param, IMechaParamSave mps) {
		super(lock, param); 
		this.mps = mps;
	}
	
	@Override
	public void setIntUnlock(int i) {
		super.setIntUnlock(i);
		mps.save();
	}
	
	public void setIntNoSave(int i) {
		setIntOnly(i);
	}
	
	@Override
	public void setStrUnlock(String str) {
		super.setStrUnlock(str);
		mps.save();
	}
	
	public void setStrNoSave(String str){
		setStrOnly(str);
	}
	
	@Override
	public void setDoubleUnlock(double d) {
		super.setDoubleUnlock(d);
		mps.save();
	}
	
	public void setDoubleNoSave(double d){
		setDoubleOnly(d);
	}
	
	@Override
	public void setFloatUnlock(float f) {
		super.setFloatUnlock(f);
		mps.save();
	}
	
	public void setFloatNoSave(float f){
		setFloatOnly(f);
	}
	
	@Override
	public void setMechaParamSave(IMechaParamSave mps) { this.mps = mps; }
	
	@Override
	public IMechaParamSave getMechaParamSave() { return mps; }
	
	@Override
	public boolean isSave() { return true; }
}