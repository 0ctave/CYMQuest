package fr.craftyourmind.quest.mecha;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import fr.craftyourmind.quest.mecha.MechaRandom.RandomData;

class MechaPotionEffect implements RandomElement{
	
	private String nameEffect = "";
	private IntegerData duration = new IntegerData(), amplifier = new IntegerData();
	private PotionEffectType pet;
	private RandomData rdata;
	
	public MechaPotionEffect(MechaRandom mr) { this.rdata = mr.newRandomData(); }
	public MechaPotionEffect(RandomData rdata) { this.rdata = rdata; }
	public MechaPotionEffect(String nameEffect, int duration, int amplifier, MechaRandom mr) {
		setNameEffect(nameEffect); this.duration.set(duration); this.amplifier.set(amplifier); this.rdata = mr.newRandomData();
	}
	
	public void setNameEffect(String name){ nameEffect = name; pet = PotionEffectType.getByName(name); }
	public String getNameEffect(){ return nameEffect; }
	
	public void setDuration(int d){ duration.set(d); }
	public int getDuration(){ return duration.get(); }
	
	public void setAmplifier(int a){ amplifier.set(a); }
	public int getAmplifier(){ return amplifier.get(); }
	
	public PotionEffectType getEffectType(){ return pet; }
	public PotionEffect getPotionEffect() { return new PotionEffect(pet, duration.get()*20, amplifier.get()); }
	@Override
	public void setRandomData(RandomData rd) { rdata = rd; }
	@Override
	public RandomData getRandomData() { return rdata; }
	
	public String getParams() {
		return new StringBuilder(getNameEffect()).append(Mechanism.DELIMITER).append(duration).append(Mechanism.DELIMITER).append(amplifier).append(Mechanism.DELIMITER).append(rdata.getParams()).toString();
	}
	
	public int load(int index, String[] params){
		setNameEffect(params[index++]);
		duration.load(params[index++]);
		amplifier.load(params[index++]);
		index = rdata.load(index, params);
		return index;
	}
	public MechaPotionEffect clone(AbsMechaStateEntity mse) {
		MechaPotionEffect mpe = new MechaPotionEffect(rdata.clone(mse));
		mpe.setNameEffect(nameEffect);
		mpe.duration.clone(mse, duration);
		mpe.amplifier.clone(mse, amplifier);
		return mpe;
	}
}