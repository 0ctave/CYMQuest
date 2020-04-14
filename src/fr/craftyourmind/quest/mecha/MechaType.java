package fr.craftyourmind.quest.mecha;

import java.util.Arrays;
import java.util.List;

public class MechaType {

	// ---- mecha type ----
	public static final int STAQUEST = 0;
	public static final int STAEVENT = 1;
	public static final int TRIINVENTORY = 3;
	public static final int TRILOCATION = 4;
	public static final int ACTPOPENTITY = 5;
	public static final int ACTINVENTORY = 6;
	public static final int ACTTELEPORT = 7;
	public static final int ACTCOMMAND = 8;
	public static final int TRITIMER = 9;
	public static final int ACTBUILDBLOCK = 10;
	public static final int ACTEFFECT = 11;
	public static final int TRIEVENTBLOCK = 12;
	public static final int TRIKILL = 13;
	public static final int ACTCHEST = 14;
	public static final int ACTQUEST = 15;
	public static final int ACTREPUTATION = 16;
	public static final int TRIDAY = 17;
	public static final int ACTSENDCLAN = 18;
	public static final int TOOEFFECTS = 19;
	public static final int TOORANDMECHA = 20;
	public static final int ACTQUESTBOOK = 21;
	public static final int TRIUSEITEM = 22;
	public static final int STAMEKABOX = 23;
	public static final int TOOMEKABOX = 24;
	public static final int TOOSELECT = 25;
	public static final int ACTPLAYER = 26;
	public static final int TOOSTOP = 27;
	public static final int TRIDAMAGE = 28;
	public static final int TOOPARAM = 29;
	public static final int STASKILL = 30;
	public static final int STALEVEL = 31;
	public static final int STATIER = 32;
	public static final int ACTSKILL = 33;
	public static final int TOOTEXT = 34;
	
	public static final String STRSTAQUEST = "quest.gui.quest";
	public static final String STRSTAEVENT = "quest.gui.event";
	public static final String STRTRIINVENTORY = "quest.mecha.inventory";
	public static final String STRTRILOCATION = "quest.gui.location";
	public static final String STRACTINVENTORY = "quest.mecha.inventory";
	public static final String STRACTPOPENTITY = "quest.gui.popentity";
	public static final String STRACTCOMMAND = "quest.gui.command";
	public static final String STRTRITIMER = "quest.gui.timer";
	public static final String STRACTBUILDBLOCK = "quest.mecha.buildblock";
	public static final String STRACTEFFECT = "quest.gui.effect";
	public static final String STRTRIEVENTBLOCK = "quest.gui.blockevent";
	public static final String STRTRIKILL = "quest.gui.kill";
	public static final String STRACTCHEST = "Chest";
	public static final String STRACTQUEST = "Quest";
	public static final String STRACTREPUTE = "Reputation";
	public static final String STRTRIDAY = "Day";
	public static final String STRACTSENDCLAN = "Send Clan";
	public static final String STRACTTELEPORT = "Teleport";
	public static final String STRTOOEFFECTS = "FX Effects";
	public static final String STRTOORANDMECHA = "Rand. Mecha";
	public static final String STRACTQUESTBOOK = "Quest book";
	public static final String STRTRIUSEITEM = "Use item";
	public static final String STRSTAMEKABOX = "Mekabox";
	public static final String STRTOOMEKABOX = "Mekabox";
	public static final String STRTOOSELECT = "Selector";
	public static final String STRACTPLAYER = "Entity";
	public static final String STRTOOSTOP = "Stop mecha";
	public static final String STRTRIDAMAGE = "Damage";
	public static final String STRTOOPARAM = "Parameters";
	public static final String STRSTASKILL = "Skill";
	public static final String STRSTALEVEL = "Level";
	public static final String STRSTATIER = "Tier";
	public static final String STRACTSKILL = "Skill";
	public static final String STRTOOTEXT = "Text screen";
	
	public int id = 0;
	public String name = "";
	public List<Integer> typedriversPermit;
	public Class<? extends Mechanism> cls;
	public boolean display = true;
	public MechaCat category;

	public MechaType(int id, String name, Class<? extends Mechanism> cls, boolean display, MechaCat category, Integer... typedriversPermit) {
		this.id = id;
		this.name = name; 
		this.typedriversPermit = Arrays.asList(typedriversPermit);
		this.cls = cls;
		this.display = display;
		this.category = category;
	}
}
