package fr.craftyourmind.quest.mecha;

public class DoubleData extends MechaData<Double>{

	public DoubleData() { super(0d); }
	public DoubleData(double d) { super(d); }
	@Override
	protected void setValueParam(Double data) { param.setDouble(data); }
	@Override
	protected Double getValueParam() { return param.getDouble(); }
	@Override
	public void valueOf(String dataStr){ this.data = Double.valueOf(dataStr); }	
}