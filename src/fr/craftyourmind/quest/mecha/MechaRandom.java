package fr.craftyourmind.quest.mecha;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.craftyourmind.quest.mecha.MechaRandom.RandomData;

public class MechaRandom<T extends RandomElement> {

	private List<T> permanents = new ArrayList<T>();
	private List<T> rates = new ArrayList<T>();
	private IntegerData iteration = new IntegerData(1);
	
	public void addPermanent(T re){ permanents.add(re); }
	public void addRate(T re){ rates.add(re); }
	public void add(T re){
		if(re.getRandomData().permanent) addPermanent(re); else addRate(re);
	}
	public void clear() {
		permanents.clear(); rates.clear();
	}
	
	public List<T> getRandomList(){
		if(iteration.get() <= permanents.size()) return new ArrayList<T>(permanents);
		List<T> list = new ArrayList<T>();
		Random rand = new Random();
		int iterationtmp = iteration.get();
		
		List<T> permanentstmp = new ArrayList<T>(permanents);
		int size = permanentstmp.size();
		for(int i = 0 ; i < size ; i++){
			if(iterationtmp <= 0 || permanentstmp.isEmpty()) break;
			int nbrand = rand.nextInt(permanentstmp.size());
			T randElement = permanentstmp.get(nbrand);
			if(randElement.getRandomData().unique) permanentstmp.remove(randElement);
			list.add(randElement);
			iterationtmp--;
		}
		
		List<T> ratestmp = new ArrayList<T>(rates);
		for(int i = 0 ; i < iterationtmp ; i++){
			if(ratestmp.isEmpty()) break;	
			int pre = 0;
			for(T randElement : ratestmp){
				RandomData rd = randElement.getRandomData();
				rd.ratetmp = (int) ((rd.rate.get()*100)+pre);
				pre = rd.ratetmp;
			}				
			int raterand = rand.nextInt(10000);
			int index = 0;
			boolean unique = false;
			for(T randElement : ratestmp){
				RandomData rd = randElement.getRandomData();
				if(raterand < rd.ratetmp){
					unique = rd.unique;
					list.add(randElement);
					break;
				}
				index++;
			}
			if(unique) ratestmp.remove(index);						
		}
		return list;
	}
	
	public String getParams(){ return iteration.toString(); }
	
	public int load(int index, String[] params){
		iteration.load(params[index++]);
		return index;
	}
	
	public void setIteration(int i){ iteration.set(i); }
	
	public void clone(AbsMechaStateEntity mse, MechaRandom mr){ iteration.clone(mse, mr.iteration); }
	
	public RandomData newRandomData(){ return new RandomData(); }
	
	class RandomData{
		private boolean permanent = true, unique = true;
		private FloatData rate = new FloatData();
		private int ratetmp;
		public String getParams(){
			return new StringBuilder().append(unique).append(Mechanism.DELIMITER).append(permanent).append(Mechanism.DELIMITER).append(rate).toString();
		}
		public int load(int index, String[] params){
			unique = Boolean.valueOf(params[index++]);
			permanent = Boolean.valueOf(params[index++]);
			rate.load(params[index++]);
			return index;
		}
		public RandomData clone(AbsMechaStateEntity mse) {
			RandomData rd = new RandomData();
			rd.permanent = permanent;
			rd.unique = unique;
			rd.rate.clone(mse, rate);
			return rd;
		}
	}
}
interface RandomElement{
	public MechaRandom.RandomData getRandomData();
	public void setRandomData(MechaRandom.RandomData rd);
}