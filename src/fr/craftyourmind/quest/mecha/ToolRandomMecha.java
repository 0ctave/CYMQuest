package fr.craftyourmind.quest.mecha;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ToolRandomMecha extends Mechanism{

	public IntegerData nbIteration = new IntegerData(1);
	public IntegerData nbMin = new IntegerData(), nbMax = new IntegerData();
	public List<RandomMecha> mechaPermanent = new ArrayList<RandomMecha>();
	public List<RandomMecha> mechaRate = new ArrayList<RandomMecha>();
	
	private int attempt, nbLaunched;
	private final int maxAttempt = 5;
	
	@Override
	public int getType() { return MechaType.TOORANDMECHA; }
	@Override
	public String getParams() {
		List<RandomMecha> mechaList = new ArrayList<RandomMecha>();
		mechaList.addAll(mechaPermanent);
		mechaList.addAll(mechaRate);
		StringBuilder params = new StringBuilder().append(mechaList.size());
		for(RandomMecha rm : mechaList) params.append(DELIMITER).append(rm.idMecha).append(DELIMITER).append(rm.permanent).append(DELIMITER).append(rm.unique).append(DELIMITER).append(rm.rate);
		return 2+DELIMITER+nbIteration+DELIMITER+nbMin+DELIMITER+nbMax+DELIMITER+params;
	}
	@Override
	public String getParamsGUI() {
		StringBuilder params = new StringBuilder();
		int size = getContainer().getMechas().size();
		for(Mechanism m : getContainer().getMechas()){
			if(m.name.isEmpty()) size--;
			else params.append(DELIMITER).append(m.id).append(DELIMITER).append(m.name);
		}
		return size+params.toString();
	}
	@Override
	protected void loadParams(String[] params) {
		int version = Integer.valueOf(params[0]);
		if(version == 1){
			nbIteration.load(params[1]);
			int size = Integer.valueOf(params[2]);
			mechaPermanent.clear();
			mechaRate.clear();
			int index = 3;
			for(int i = 0; i < size ; i++){
				RandomMecha rm = new RandomMecha();
				rm.idMecha = Integer.valueOf(params[index++]);
				rm.permanent = Boolean.valueOf(params[index++]);
				rm.unique = Boolean.valueOf(params[index++]);
				rm.rate.load(params[index++]);
				if(rm.permanent) mechaPermanent.add(rm);
				else mechaRate.add(rm);
				rm.m = getContainer().getMecha(rm.idMecha);
			}
			sqlSave();
		}else if(version == 2){
			nbIteration.load(params[1]);
			nbMin.load(params[2]);
			nbMax.load(params[3]);
			int size = Integer.valueOf(params[4]);
			mechaPermanent.clear();
			mechaRate.clear();
			int index = 5;
			for(int i = 0; i < size ; i++){
				RandomMecha rm = new RandomMecha();
				rm.idMecha = Integer.valueOf(params[index++]);
				rm.permanent = Boolean.valueOf(params[index++]);
				rm.unique = Boolean.valueOf(params[index++]);
				rm.rate.load(params[index++]);
				if(rm.permanent) mechaPermanent.add(rm);
				else mechaRate.add(rm);
				rm.m = getContainer().getMecha(rm.idMecha);
			}
		}
	}
	
	class RandomMecha{
		public int idMecha;
		public Mechanism m;
		public boolean permanent, unique;
		public FloatData rate = new FloatData();
		public int ratetmp;
		public Mechanism getMecha(){ if(m == null) m = getContainer().getMecha(idMecha); return m; }
	}
	
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateRand(this, mc, driver); }
	// ------------------ StateRand ------------------
	class StateRand extends AbsMechaStateEntity{

		public IntegerData nbIteration = new IntegerData(1);
		public IntegerData nbMin = new IntegerData(), nbMax = new IntegerData();
		
		public StateRand(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void cloneData() {
			super.cloneData();
			nbIteration.clone(this, ToolRandomMecha.this.nbIteration);
			nbMin.clone(this, ToolRandomMecha.this.nbMin);
			nbMax.clone(this, ToolRandomMecha.this.nbMax);
		}
		@Override
		public void start() {
			attempt = 0;
			nbLaunched = 0;
			if(nbMin.get() > 0){
				while(nbLaunched < nbMin.get() && attempt < maxAttempt){
					List<Mechanism> launchMechas = getRandMecha();
					for(Mechanism m : launchMechas) m.start(driver);
					attempt++;
				}
			}else{
				List<Mechanism> launchMechas = getRandMecha();
				for(Mechanism m : launchMechas) m.start(driver);
			}
			launchMessage();
		}
		private List<Mechanism> getRandMecha(){
			List<Mechanism> launchMechas = new ArrayList<Mechanism>();
			Random rand = new Random();
			int nbItetmp = nbIteration.get();
			
			List<RandomMecha> mechaPermanenttmp = new ArrayList<RandomMecha>(mechaPermanent);
			int size = mechaPermanenttmp.size();
			for(int i = 0 ; i < size ; i++){
				if(nbItetmp <= 0 || mechaPermanenttmp.isEmpty()) break;
				int nbrand = rand.nextInt(mechaPermanenttmp.size());
				RandomMecha rm = mechaPermanenttmp.get(nbrand);
				Mechanism m = rm.getMecha();
				if(m != null){
					launchMechas.add(m);
					nbLaunched++;
					if(nbMax.get() > 0 && nbLaunched >= nbMax.get()){
						attempt = maxAttempt;
						return launchMechas;
					}
				}
				if(rm.unique) mechaPermanenttmp.remove(rm);
				nbItetmp--;
			}
			
			List<RandomMecha> mechaRatetmp = new ArrayList<RandomMecha>(mechaRate);
			for(int i = 0 ; i < nbItetmp ; i++){
				int pre = 0;
				for(RandomMecha rm : mechaRatetmp){
					rm.ratetmp = (int) ((rm.rate.get()*100)+pre);
					pre = rm.ratetmp;
				}
				if(mechaRatetmp.isEmpty()) break;
				int raterand = rand.nextInt(10000);
				int index = 0;
				boolean unique = false;
				for(RandomMecha rm : mechaRatetmp){
					if(raterand < rm.ratetmp){
						Mechanism m = rm.getMecha();
						if(m != null){
							launchMechas.add(m);
							nbLaunched++;
							if(nbMax.get() > 0 && nbLaunched >= nbMax.get()){
								attempt = maxAttempt;
								return launchMechas;
							}
						}
						unique = rm.unique;
						break;
					}
					index++;
				}
				if(unique) mechaRatetmp.remove(index);						
			}
			return launchMechas;
		}
	}
}