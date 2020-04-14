package fr.craftyourmind.quest.mecha;

public class FloatData extends MechaData<Float>{
	
	public FloatData() { super(0f); }
	public FloatData(float f) { super(f); }
	@Override
	protected void setValueParam(Float data) { param.setFloat(data); }
	@Override
	protected Float getValueParam() { return param.getFloat(); }
	@Override
	public void valueOf(String dataStr){ this.data = Float.valueOf(dataStr); }
}