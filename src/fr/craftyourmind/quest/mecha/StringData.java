package fr.craftyourmind.quest.mecha;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

public class StringData extends MechaData<String>{
	
	private Map<String, MechaParam> params = new HashMap<String, MechaParam>();
	
	public StringData() { super(""); }
	public StringData(String str) { super(str); }
	@Override
	protected void setValueParam(String data) { param.setStr(data); }
	@Override
	protected String getValueParam() {
		StringBuilder sb = new StringBuilder(data);
		for(Entry<String, MechaParam> entry : params.entrySet()){
			if(entry.getValue() != null){
				//sb.replace(0, sb.length(), Pattern.compile(charParam+entry.getKey()).matcher(sb).replaceAll(entry.getValue().getStr()));
				String strParam = charParam+entry.getKey();
				int indexEnd = 0;
				int indexBegin = sb.indexOf(strParam, indexEnd);
				while (indexBegin > -1){
					indexEnd = indexBegin+strParam.length();
					if(indexEnd < sb.length()){
						if(!sb.substring(indexEnd, indexEnd+1).matches("\\w+"))
							sb.replace(indexBegin, indexEnd, entry.getValue().getStr());
						indexBegin = sb.indexOf(strParam, indexEnd+1);
					}else{
						sb.replace(indexBegin, indexEnd, entry.getValue().getStr());
						indexBegin = -1;
					}
				}
			}
		}		
		return sb.toString();
	}
	@Override
	public void valueOf(String dataStr){ this.data = dataStr; }
	@Override
	public void load(String dataStr){
		params.clear();
		this.dataStr = dataStr;
		Matcher matcher = pattern.matcher(dataStr);
		while (matcher.find()) params.put(matcher.group().substring(1), null);
		valueOf(dataStr);
	}
	
	public void clone(IMechaParamManager mpm, StringData clone) {
		dataStr = clone.dataStr;
		data = clone.data;
		if(mpm != null){
			for(String entry : clone.params.keySet()){
				MechaParam p = mpm.getMechaParam(entry);
				params.put(entry, p);
				if(p != null) param = p;
			}
		}
	}
}