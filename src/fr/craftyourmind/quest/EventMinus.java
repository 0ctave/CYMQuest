package fr.craftyourmind.quest;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import fr.craftyourmind.manager.CYMChecker;
import fr.craftyourmind.manager.CYMChecker.ICheckerPosition;
import fr.craftyourmind.manager.checker.Position;

public class EventMinus extends EventParadise{
	
	private static List<EventMinus> minus = new ArrayList<EventMinus>();
	
	@Override
	public void addList() { super.addList(); minus.add(this); }
	@Override
	public void removeList() { super.removeList(); minus.remove(this); }
	
	// ----------------------------- START -----------------------------
	public void start(){
		stop();
		if(world != null){
			if(mechaStartUnitary != null) mechaStartUnitary.start();
			for(Player p : world.getPlayers())
				start(QuestPlayer.get(p));
			idScheduleGeneral = 1;
		}else
			idScheduleGeneral = 0;
		save();
	}
	// ----------------------------- STOP -----------------------------
	public void stop(){
		for(StateEventPlayer sep : players)
			sep.stop();
		players.clear();
		getStates().clear();
		idScheduleGeneral = 0;
		if(mechaStopUnitary != null) mechaStopUnitary.start();
		save();
	}
	@Override
	public void startGeneral() { // no used
		start();
	}
	@Override
	public void finish() { stop(); }
	
	public int getType() { return MINUS; }

	private void stop(QuestPlayer qp) {
		StateEventPlayer sep = getState(qp);
		if(sep != null){
			sep.stop();
			players.remove(sep);
			removeState(qp);
		}
	}
	
	private void start(QuestPlayer qp) {
		StateEventPlayer sep = getState(qp);
		if(sep == null){
			sep = new StateMinusPlayer(qp);
			players.add(sep);
			addState(qp, sep);
			sep.start();
		}
	}
	@Override
	public void quit(QuestPlayer qp) { stop(qp); }
	
	public static void onPlayerChangedWorldEvent(QuestPlayer qp, World from, World world) {
		for(EventMinus em : minus){
			if(em.isStarted()){
				if(em.getWorld() == from)
					em.stop(qp);
				else if(em.getWorld() == world)
					em.start(qp);
			}
		}
	}
	// ************************** STATE **************************
	public class StateMinusPlayer extends StateEventPlayer implements ICheckerPosition{

		private boolean running = true;
		private Position checkerPosition;
		
		public StateMinusPlayer(QuestPlayer qp) { super(qp); }
		@Override
		public void start() {
			super.start();
			checkerPosition = CYMChecker.startPosition(qp.getCYMPlayer(), this);
		}
		@Override
		public void stop() {
			super.stop();
			if(checkerPosition != null) checkerPosition.stop();
		}
		@Override
		public int getRadius() { return radius; }
		@Override
		public String getWorld() { return getWorldName(); }
		@Override
		public int getX() { return x; }
		@Override
		public int getY() { return 0; }
		@Override
		public int getZ() { return z; }
		@Override
		public void inside() {
			begin();
			if(!beginMessage.isEmpty()) qp.sendMessage(ChatColor.LIGHT_PURPLE+beginMessage);
			if(running) running = false;
		}
		@Override
		public void outside() {
			if(!running) finish();
			if(running) running = false;
		}
		@Override
		public boolean checker() { return false; }
	}
}