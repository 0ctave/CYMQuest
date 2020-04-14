package fr.craftyourmind.quest.mecha;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class MechaData<T> {

	protected static final char charParam = '#';
	protected static final Pattern pattern = Pattern.compile(charParam+"\\w+");
	
	protected String dataStr = "";
	protected T data;
	protected String nameparam = "";
	protected MechaParam param;
	
	public MechaData(T data) {
		this.data = data;
		dataStr = data+"";
	}
	
	public void set(T data){
		dataStr = data+"";
		if(param == null) this.data = data;
		else setValueParam(data);		
	}
	
	public T get(){
		if(param == null) return data;
		return getValueParam();
	}
	
	public boolean hasParam(){ return param != null; }
	
	protected abstract void setValueParam(T data);
	protected abstract T getValueParam();
	public abstract void valueOf(String data);
	@Override
	public String toString() { return dataStr; }

	public void load(String dataStr){
		this.dataStr = dataStr;
		nameparam = "";
		param = null;
		Matcher matcher = pattern.matcher(dataStr);
		if(matcher.find()) nameparam = matcher.group().substring(1);
		else valueOf(dataStr);
	}
	
	public void clone(IMechaParamManager mpm, MechaData<T> clone) {
		dataStr = clone.dataStr;
		data = clone.data;
		nameparam = clone.nameparam;
		param = clone.param;
		if(!nameparam.isEmpty() && mpm != null) param = mpm.getMechaParam(nameparam);
	}
}