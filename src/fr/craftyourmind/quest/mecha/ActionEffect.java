package fr.craftyourmind.quest.mecha;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ActionEffect extends Mechanism{

	public int idEffectType; // old
	public String nameEffectType = ""; // old
	public int duration; // old
	public int amplifier; // old
	public boolean delete = false;
	
	public List<MechaPotionEffect> effects = new ArrayList<MechaPotionEffect>();
	private MechaRandom<MechaPotionEffect> mRand = new MechaRandom<MechaPotionEffect>();
	
	public ActionEffect() {}
	@Override
	public int getType() { return MechaType.ACTEFFECT; }
	@Override
	public String getParams() {
		StringBuilder params = new StringBuilder().append(effects.size());
		for(MechaPotionEffect mpe : effects) params.append(DELIMITER).append(mpe.getParams());
		return new StringBuilder("5").append(DELIMITER).append(delete).append(DELIMITER).append(mRand.getParams()).append(DELIMITER).append(params).toString();
	}
	@Override
	public String getParamsGUI() {
		int length = PotionEffectType.values().length;
		StringBuilder params = new StringBuilder();
		for(PotionEffectType pet : PotionEffectType.values()){
			if(pet == null) length--;
			else params.append(pet.getName()).append(DELIMITER);
		}
		return length+DELIMITER+params.toString();
	}
	@Override
	protected void loadParams(String[] params) {
		int version = Integer.valueOf(params[0]);
		if(version == 3){
			nameEffectType = params[1];
			duration = Integer.valueOf(params[2]);
			amplifier = Integer.valueOf(params[3]);
			delete = Boolean.valueOf(params[4]);
			effects.clear();
			mRand.clear();
			if(!nameEffectType.isEmpty()){
				MechaPotionEffect mpe = new MechaPotionEffect(nameEffectType, duration, amplifier, mRand);
				effects.add(mpe);
				mRand.add(mpe);
				mRand.setIteration(1);
			}
			sqlSave();
		}else if(version == 4) loadVersion4(1, params);
		else if(version == 5) loadVersion5(1, params);
	}
	private void loadVersion4(int index, String[] params){
		delete = Boolean.valueOf(params[index++]);
		effects.clear();
		mRand.clear();
		int size = Integer.valueOf(params[index++]);
		for(int i = 0 ; i < size ; i++){
			MechaPotionEffect mpe = new MechaPotionEffect(mRand);
			mpe.setNameEffect(params[index++]);
			mpe.setDuration(Integer.valueOf(params[index++]));
			mpe.setAmplifier(Integer.valueOf(params[index++]));
			effects.add(mpe);
			mRand.add(mpe);
		}
		mRand.setIteration(effects.size());
		sqlSave();
	}
	private void loadVersion5(int index, String[] params){
		delete = Boolean.valueOf(params[index++]);
		index = mRand.load(index, params);
		effects.clear();
		mRand.clear();
		int size = Integer.valueOf(params[index++]);
		for(int i = 0 ; i < size ; i++){
			MechaPotionEffect mpe = new MechaPotionEffect(mRand);
			index = mpe.load(index, params);
			mRand.add(mpe);
			effects.add(mpe);
		}
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateEffect(this, mc, driver); }
	// ------------------ StateEffect ------------------
	class StateEffect extends AbsMechaStateEntity{

		private List<MechaPotionEffect> effects = new ArrayList<MechaPotionEffect>();
		private MechaRandom<MechaPotionEffect> mRand = new MechaRandom<MechaPotionEffect>();
		
		public StateEffect(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void cloneData() {
			super.cloneData();
			effects.clear(); mRand.clear();
			mRand.clone(this, ActionEffect.this.mRand);
			for(MechaPotionEffect mpe : ActionEffect.this.effects) effects.add(mpe.clone(this));
			for(MechaPotionEffect mpe : effects) mRand.add(mpe);
		}
		@Override
		public void start() {
			Entity e = driver.getEntity();
			if(e != null && e instanceof LivingEntity){
				LivingEntity le = (LivingEntity)e;
				if(effects.isEmpty()){
					if(delete){
						for(PotionEffect pe : le.getActivePotionEffects())
							le.removePotionEffect(pe.getType());
					}
				}else{
					List<MechaPotionEffect> list = mRand.getRandomList();
					for(MechaPotionEffect ep : list){
						if(ep.getEffectType() != null){
							le.removePotionEffect(ep.getEffectType());
							if(!delete)
								le.addPotionEffect(ep.getPotionEffect());
						}
					}
				}
			}
			launchMessage();
		}
	}
}