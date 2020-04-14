package fr.craftyourmind.quest.mecha;

import java.util.ArrayList;
import java.util.List;

public class MechaCat {

	// ---- category ----
	public static final int STARTER = 0;
	public static final int TRIGGER = 1;
	public static final int ACTION = 2;
	public static final int TOOLS = 3;

	private static List<MechaCat> cats = new ArrayList<MechaCat>();
	
	public int id = 0;
	public String name = "";
	private List<MechaType> types = new ArrayList<MechaType>();
	private List<Integer> idTypes = new ArrayList<Integer>();
	private List<String> strTypes = new ArrayList<String>();
	
	public MechaCat(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public static void add(MechaCat mc){ cats.add(mc); }
	
	public static MechaCat get(int id){
		for(MechaCat c : cats) if(c.id == id) return c;
		return null;
	}

	public void add(MechaType type) { 
		types.add(type);
		idTypes.add(type.id);
		strTypes.add(type.name);
	}
	
	public List<MechaType> getTypeMechaPermits(int typedriver){
		List<MechaType> permittypes = new ArrayList<MechaType>();
		for(MechaType mt : types) if(mt.display && mt.typedriversPermit.contains(typedriver)) permittypes.add(mt);
		return permittypes;
	}
	
}
