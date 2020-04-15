package fr.craftyourmind.quest;

import fr.craftyourmind.manager.CYMChecker;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import fr.craftyourmind.manager.CYMChecker;
import fr.craftyourmind.manager.CYMChecker.ICheckerKill;
import fr.craftyourmind.manager.checker.Kill;
import fr.craftyourmind.manager.packet.DataAlert;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class ObjectiveKill extends AbsObjective{

	public static final int ENTITY = 0;
	public static final int PLAYER = 1;
	public static final int SURVIVE = 2;
	
	public int type = 0;
	public String targetT = "";
	public String targetName = "";
	public int num = 1;
	public boolean targetType = true; // true = target type ; false = target name
	
	public ObjectiveKill(Quest q) {
		super(q);
		alertItem = Material.STONE_SWORD.getKey().toString();
	}
	@Override
	public IStateObj getState(StateQuestPlayer sqp) { return new StateKill(sqp); }
	@Override
	public int getType() { return KILL; }
	@Override
	public String getStrType() { return STRKILL; }
	@Override
	public String getParams() {
		return new StringBuilder("2").append(DELIMITER).append(type).append(DELIMITER).append(targetT).append(DELIMITER).append(targetName).append(DELIMITER)
				.append(num).append(DELIMITER).append(targetType).toString();
	}
	@Override
	public String getParamsGUI() {
		String param = "";
		for(EntityType e : EntityType.values()){
			if(e.isSpawnable() && e.isAlive()) param += e.getKey().toString()+DELIMITER;
		}
		return param;
	}
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 3){
			type = Integer.valueOf(params[0]);
			targetT = params[1];
			num = Integer.valueOf(params[2]);
			sqlSave();
		}else{
			int version = Integer.valueOf(params[0]);
			if(version == 2){
				type = Integer.valueOf(params[1]);
				targetT = params[2];
				targetName = params[3];
				num = Integer.valueOf(params[4]);
				targetType = Boolean.valueOf(params[5]);
				if(type == PLAYER) targetType = false;
			}
		}
	}
	// ---------------- STATEKILL ----------------
	class StateKill extends StateObjPlayer implements CYMChecker.ICheckerKill {

		public int current = 0;
		public LivingEntity killer;	
		public LivingEntity victim;
		private Kill k;
		
		public StateKill(StateQuestPlayer sqp) { super(sqp); }
		@Override
		public boolean check() {
			boolean kill = false;
			if(victim == null) return type == SURVIVE;
			
			if(type == ENTITY){
				if(targetType){ if(!targetT.isEmpty() && victim.getType() == EntityType.valueOf(targetT.split(":")[1].toUpperCase())) kill = true;
				}else if(victim.getCustomName() != null && victim.getCustomName().equalsIgnoreCase(targetName)) kill = true;
				
			}else if(type == PLAYER){
				if(victim.getType() == EntityType.PLAYER){
					if(targetName.isEmpty() && sqp.getDriver().getEntity() != victim) kill = true;
					else if(((HumanEntity)victim).getName().equalsIgnoreCase(targetName)) kill = true;
				}
			}else if(type == SURVIVE && sqp.getDriver().getEntity() == victim) kill = true;
			
			killer = null;
			victim = null;
			if(kill){
				tick(1);
				if(current >= num){
					if(type == SURVIVE){
						messageSuccess(sqp.qp);
						sqp.getQuest().messageLose(sqp.qp);
						if(!success.isEmpty())
							new DataAlert("quest.objective.fail", success, alertItem).send(sqp.qp.getPlayer());
						sqp.decline();
						return false;
					}
					terminate();
					sendMessagesSuccess();
					return true;
				}
			}
			if(type == SURVIVE) return true;
			return false;
		}
		@Override
		public void begin() {
			if(!isTerminate())
				k = CYMChecker.startKill(sqp.qp.getCYMPlayer(), this);
		}
		@Override
		public void tick(int nb) {
			super.tick(nb);
			current += nb;
			String nameNotice = (targetType) ? targetT.split(":")[1].toUpperCase() : targetName;
			if(type == SURVIVE) nameNotice = "";
			sqp.qp.sendMessage(new StringBuilder().append(ChatColor.GRAY).append("+++ ").append("KILL").append(" ")
					.append(nameNotice).append(" : ").append(current).append("/").append(num).append(" +++").toString());
		}
		@Override
		public void terminate() {
			super.terminate();
			if(k != null) k.stop();
		}
		@Override
		public void finish() { clean(); }
		@Override
		public void clean() {
			if(k != null) k.stop();
			if(isTerminate()) sqlClean(sqp.qp);
		}
		@Override
		public String getMessageGui() { return current+"/"+num+" "; }
		@Override
		public int getTick() { return current; }
		@Override
		public void initTick(int current) { this.current = current; }
		@Override
		public void setKiller(LivingEntity killer) { this.killer = killer; }
		@Override
		public void setVictim(LivingEntity victim) { this.victim = victim; }
	}
}