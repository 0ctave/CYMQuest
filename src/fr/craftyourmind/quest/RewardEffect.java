package fr.craftyourmind.quest;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class RewardEffect extends AbsReward{

	public int idEffectType;
	public int duration;
	public int amplifier;
	
	public RewardEffect(Quest q) {
		super(q);
		idItem = Material.MAGMA_CREAM.getKey().toString();
	}
	@Override
	public IStateRew getState(StateQuestPlayer sqp) { return new StateEffects(sqp); }
	@Override
	public int getType() { return EFFECT; }
	@Override
	public String getStrType() { return STREFFECT; }
	@Override
	public String getParams() { return idEffectType+DELIMITER+duration+DELIMITER+amplifier; }
	@Override
	public String getParamsGUI() {
		int length = PotionEffectType.values().length;
		StringBuilder params = new StringBuilder();
		for(PotionEffectType pet : PotionEffectType.values())
			if(pet == null) length--;
			else params.append(pet.getId()).append(DELIMITER).append(pet.getName()).append(DELIMITER);
		return length+DELIMITER+params.toString();
	}
	@Override
	protected void loadParams(String[] params) {
		idEffectType = Integer.valueOf(params[0]);
		duration = Integer.valueOf(params[1]);
		amplifier = Integer.valueOf(params[2]); 
	}
	// ---------------- STATEEFFECTS ----------------
	class StateEffects extends StateRewPlayer{

		public StateEffects(StateQuestPlayer sqp) { super(sqp); }
		@Override
		public void give() {
			PotionEffectType pet = PotionEffectType.getById(idEffectType);
			if(pet != null && sqp.qp.getPlayer() != null)
				sqp.qp.getPlayer().addPotionEffect(new PotionEffect(pet, duration*20, amplifier));
		}
	}
}