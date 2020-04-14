package fr.craftyourmind.quest.mecha;

public class IntegerData extends MechaData<Integer>{
	
	public IntegerData() { super(0); }
	public IntegerData(int i) { super(i); }
	@Override
	protected void setValueParam(Integer data) { param.setInt(data); }
	@Override
	protected Integer getValueParam() { return param.getInt(); }
	@Override
	public void valueOf(String dataStr){ this.data = Integer.valueOf(dataStr); }
}