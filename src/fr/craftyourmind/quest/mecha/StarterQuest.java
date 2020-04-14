package fr.craftyourmind.quest.mecha;

import fr.craftyourmind.quest.AbsObjective;
import fr.craftyourmind.quest.ObjectiveMeka.StateMeka;
import fr.craftyourmind.quest.Quest;
import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.AbsObjective.IStateObj;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class StarterQuest extends Mechanism{

	public static final int ACCEPT = 0;
	public static final int GIVE = 1;
	public static final int DECLINE = 2;
	public static final int OBJTERMINATE = 3;
	public static final int OBJON = 4;
	public static final int OBJOFF = 5;
	
	public int idQuest = 0;
	public int action = 0;
	public int idObj = 0;
	
	private Quest q;
	
	public StarterQuest() {}
	@Override
	public void init() {
		if(q == null) q = Quest.get(idQuest);
		if(q != null){
			if(q.mechaAccept != null && q.mechaAccept.id == id) q.mechaAccept = null;
			if(q.mechaGive != null && q.mechaGive.id == id) q.mechaGive = null;
			if(q.mechaDecline != null && q.mechaDecline.id == id) q.mechaDecline = null;
			for (AbsObjective obj : q.getObjs())
				if(obj.mechaTerminate != null && obj.mechaTerminate.id == id) obj.mechaTerminate = null;
			
			if(action == ACCEPT) q.mechaAccept = this;
			else if(action == GIVE) q.mechaGive = this;
			else if(action == DECLINE) q.mechaDecline = this;
			else if(action == OBJTERMINATE)
				for (AbsObjective obj : q.getObjs())
					if(obj.id == idObj) obj.mechaTerminate = this;
		}
	}	
	@Override
	public void start() { start(getContainer().getDriver(QuestPlayer.nobody)); }
	@Override
	public int getType() { return MechaType.STAQUEST; }
	@Override
	public String getParams() { return idQuest+DELIMITER+action+DELIMITER+idObj; }
	@Override
	public String getParamsGUI() {
		String param = "";
		if(q != null)
			for(AbsObjective obj : q.getObjs())
				param += obj.id+DELIMITER+obj.descriptive+DELIMITER+(obj.getType() == AbsObjective.MEKA)+DELIMITER;
		return param;
	}
	@Override
	protected void loadParams(String[] params) {
		idQuest = Integer.valueOf(params[0]);
		action = Integer.valueOf(params[1]);
		idObj = Integer.valueOf(params[2]);
		q = Quest.get(idQuest);
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateQuest(this, mc, driver); }
	// ------------------ StateQuest ------------------
	class StateQuest extends AbsMechaStateEntity{

		public StateQuest(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void start(){
			if((action == OBJON || action == OBJOFF) && driver.isPlayer()){
				StateQuestPlayer sqp = q.getState(driver.getQuestPlayer());
				for(IStateObj so : sqp.objs){
					if(so.getId() == idObj){
						StateMeka sm =  (StateMeka)so;
						boolean state = action == OBJON;
						if(state != sm.state()){
							sm.setState(state);
							sm.checker();
						}
						break;
					}
				}
			}
			launchMessage();
		}
	}
}