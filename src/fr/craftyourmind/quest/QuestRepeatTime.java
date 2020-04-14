package fr.craftyourmind.quest;

import java.util.TreeMap;

import org.bukkit.Bukkit;
import fr.craftyourmind.quest.Quest.StateQuestPlayer;

public class QuestRepeatTime implements Runnable{
	
	private TreeMap<Long, StateQuestPlayer> list = new TreeMap<Long, StateQuestPlayer>();
	
	private int id = 0;
	private Quest quest;
	
	public QuestRepeatTime(Quest quest) {
		this.quest = quest;
	}

	public void run() {

		StateQuestPlayer sqp = list.firstEntry().getValue();
		
		if(sqp.isTerminate() || sqp.isBeginning())
			sqp.decline();

		sqp.isRepeat = false;
		list.remove(list.firstKey());
		start();
	}

	public void start(){
		stop();
		if(!list.isEmpty()){
			long currentTime = System.currentTimeMillis();
			
			if(list.firstKey() < currentTime) 
				run();
			else
				id = Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.it, this, (list.firstKey() - currentTime) / 1000 * 20);
		}
	}
	
	public void stop(){
		if(id != 0) Bukkit.getScheduler().cancelTask(id);
		id = 0;
	}
	
	public void add(StateQuestPlayer sqp){
		add(System.currentTimeMillis() + quest.repeateTime, sqp);
	}
	
	public void add(long time, StateQuestPlayer sqp){
		if(!sqp.isRepeat){
			sqp.isRepeat = true;
			if(quest.repeateTime != 0){
				stop();
				while(list.containsKey(time)){
					time++;
				}
				list.put(time, sqp);
				start();
			}else
				remove();
		}
	}
	
	public void remove(){
		stop();
		for(StateQuestPlayer sqp : list.values()) sqp.isRepeat = false;
		list.clear();
	}
}