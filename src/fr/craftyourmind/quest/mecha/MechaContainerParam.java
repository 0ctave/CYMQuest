package fr.craftyourmind.quest.mecha;

public class MechaContainerParam {
	
	protected boolean isSystem = false;
	protected boolean isCommon = false;
	protected boolean isSave = false;
	protected String name = "";
	protected String descriptive = "";
	private String paramdefault = "";
	private MechaParam mechaParamCommon;
	
	public MechaContainerParam(String name) { this.name = name; }
	
	public MechaParam getMechaParam() {
		if(!isCommon){
			if(isSave) return new MechaParamSave(isSystem, paramdefault, null);
			else return new MechaParam(isSystem, paramdefault);
		}
		if(mechaParamCommon == null){
			if(isSave) mechaParamCommon = new MechaParamSave(isSystem, paramdefault, null);
			else mechaParamCommon = new MechaParam(isSystem, paramdefault);
		}
		mechaParamCommon.setSystem(isSystem); mechaParamCommon.setCommon(isCommon);
		return mechaParamCommon;
	}

	public void setSystem(boolean b) { isSystem = b; }

	public void setCommon(boolean b) { isCommon = b; }

	public void setParam(MechaParam param) { this.mechaParamCommon = param; }
	
	public String getParamDefault(){
		if(mechaParamCommon == null) return paramdefault;
		return mechaParamCommon.getStr();
	}

	public void setParamDefault(String paramdefault) {
		this.paramdefault = paramdefault;
		if(mechaParamCommon != null) mechaParamCommon.setStr(paramdefault);
	}

	public void setDescriptive(String descr) { this.descriptive = descr; }
}