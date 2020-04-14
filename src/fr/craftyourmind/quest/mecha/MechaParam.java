package fr.craftyourmind.quest.mecha;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MechaParam {
	
	private final static DecimalFormat df = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.ENGLISH));
	
	private boolean lock = false;
	private int i = 0;
	private String str = "";
	private double d = 0;
	private float f = 0;
	private boolean system, common;
	
	public MechaParam(boolean lock, String param) {
		this.lock = lock;
		setStrOnly(param);
	}
	
	public int getInt(){ return i; }
	public String getStr(){ return str; }
	public double getDouble(){ return d; }
	public float getFloat(){ return f; }
	
	public void setInt(int i){
		if(lock) return;
		setIntUnlock(i);
	}
	public void setIntUnlock(int i){
		setIntOnly(i);
	}
	protected void setIntOnly(int i){
		this.i = i;
		this.str = i+"";
		this.d = i;
		this.f = i;
	}
	
	public void setStr(String str){
		if(lock) return;
		setStrUnlock(str);
	}
	public void setStrUnlock(String str){
		setStrOnly(str);
	}
	protected void setStrOnly(String str){
		this.str = str;
		try{ d = Double.valueOf(str);
			f = Float.valueOf(str); 
			i = Integer.valueOf(str);			
		}catch(Exception e){}
	}
	
	public void setDouble(double d){
		if(lock) return;
		setDoubleUnlock(d);
	}
	public void setDoubleUnlock(double d){
		setDoubleOnly(d);
	}
	protected void setDoubleOnly(double d){
		this.i = (int) d;
		this.str = df.format(d);
		this.d = d;
		this.f = (float) d;
	}
	
	public void setFloat(float f){
		if(lock) return;
		setFloatUnlock(f);
	}
	public void setFloatUnlock(float f){
		setFloatOnly(f);
	}
	protected void setFloatOnly(float f){
		this.i = (int) f;
		this.str = df.format(f);
		this.d = f;
		this.f = f;
	}
	@Override
	public String toString() { return str; }

	public void setSystem(boolean b){ system = b; }
	public void setCommon(boolean b){ common = b; }
	public boolean isSystem() { return system; }
	public boolean isCommon() { return common; }
	public boolean isSave() { return false; }
	public void setMechaParamSave(IMechaParamSave mps) { }
	public IMechaParamSave getMechaParamSave() { return null; }
}