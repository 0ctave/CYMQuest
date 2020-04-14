package fr.craftyourmind.quest.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import fr.craftyourmind.manager.command.AbsCYMCommand;
import fr.craftyourmind.quest.QuestPlayer;

public class CmdChunkPosition extends AbsCYMCommand{
	
	public CmdChunkPosition() { super( CmdQuestManager.CMDCHUNKPOSITION); }

	public static List<PlayerCoord> getPlayersPosition(Location loc, int radius){
		int x = loc.getBlockX(), z = loc.getBlockZ();

		Coord c1 = Coord.parse(x+radius, z+radius);
		Coord c4 = Coord.parse(x-radius, z-radius);
		
		WorldCoord wc = WorldCoord.getWorldCoord(loc.getWorld().getName());
		
		List<PlayerCoord> playercoords = new ArrayList<PlayerCoord>();
		
		for(int i = c4.xchunk ; i <= c1.xchunk ; i++){
			for(int j = c4.zchunk ; j <= c1.zchunk ; j++)
				playercoords.add(wc.getPlayersCoord(new Coord(i, j)));
		}
		return playercoords;
	}
	public static List<QuestPlayer> getPlayers(Location loc, int radius){
		List<QuestPlayer> list = new ArrayList<QuestPlayer>();
		for(PlayerCoord pc : getPlayersPosition(loc, radius)) list.addAll(pc.players);
		return list;
	}
	
	@Override
	public void initChilds() { }
	@Override
	public void initActions() { addAction(new CHUNKPOSITION()); }
	
	class CHUNKPOSITION extends AbsCYMCommandAction{
		private int xchunk, zchunk, prexchunk, prezchunk;
		private String world = "", preworld = "";
		public boolean login = true;
		@Override
		public int getId() { return 0; }
		@Override
		public AbsCYMCommandAction clone() { return new CHUNKPOSITION(); }
		@Override
		public void initSend(Player p) { }
		@Override
		public void sendWrite() throws IOException { }
		@Override
		public void receiveRead() throws IOException {
			login = readBool();
			preworld = readStr();
			prexchunk = readInt();
			prezchunk = readInt();
			world = readStr();
			xchunk = readInt();
			zchunk = readInt();
		}
		@Override
		public void receive(Player p) {
			Coord c = new Coord(xchunk, zchunk);
			if(login){
				WorldCoord wc = WorldCoord.getWorldCoord(world);
				QuestPlayer qp = QuestPlayer.get(p);
				List<QuestPlayer> list = wc.getPlayers(c);
				if(!list.contains(qp)) list.add(qp);
			}else{
				WorldCoord wc = WorldCoord.getWorldCoord(preworld);
				Coord prec = new Coord(prexchunk, prezchunk);
				PlayerCoord pc = wc.getPlayersCoord(prec);
				QuestPlayer qp = null;
				for(QuestPlayer q : pc.players){
					if(q.getPlayer() == p){ qp = q; break; }
				}
				if(qp == null) qp = QuestPlayer.get(p);
				else pc.players.remove(qp);
				if(pc.players.isEmpty()) wc.playercoords.remove(pc);
				
				if(!preworld.equals(world)) wc = WorldCoord.getWorldCoord(world);
				wc.getPlayers(c).add(qp);
			}
		}
	}

	public static void onPlayerQuit(Player p) {
		Location loc = p.getLocation();
		WorldCoord wc = WorldCoord.getWorldCoord(loc.getWorld().getName());
		PlayerCoord pc = wc.getPlayersCoord(Coord.parse(loc.getBlockX(), loc.getBlockZ()));
		for(QuestPlayer qp : pc.players.toArray(new QuestPlayer[0])){
			if(qp.getPlayer() == p){ pc.players.remove(qp); if(pc.players.isEmpty()) wc.playercoords.remove(pc); break; }
		}
	}
	
}

class WorldCoord{
	private static List<WorldCoord> worldcoords = new ArrayList<WorldCoord>();
	public List<PlayerCoord> playercoords = new ArrayList<PlayerCoord>();
	public String name = "";
	public WorldCoord(String name) {
		this.name = name;
	}
	public List<QuestPlayer> getPlayers(Coord c){
		return getPlayersCoord(c).players;
	}
	public PlayerCoord getPlayersCoord(Coord c){
		for(PlayerCoord pc : playercoords) if(c.equals(pc)) return pc;
		PlayerCoord pc;
		playercoords.add(pc = new PlayerCoord(c));
		return pc;
	}
	public static WorldCoord getWorldCoord(String name){
		for(WorldCoord wc : worldcoords) if(name.equals(wc.name)) return wc;
		WorldCoord wc = new WorldCoord(name);
		worldcoords.add(wc);
		return wc;
	}
}

class Coord{
	public int xchunk, zchunk;
	public Coord(int x, int z) {
		xchunk = x;
		zchunk = z;
	}
	
	public static Coord parse(int x, int z){
		int chunksize = 64;
		int xresult = x / chunksize;
		int zresult = z / chunksize;
		boolean xneedfix = x % chunksize != 0;
		boolean zneedfix = z % chunksize != 0;
		return new Coord(xresult - (x < 0 && xneedfix ? 1 : 0), zresult - (z < 0 && zneedfix ? 1 : 0));
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Coord)) return false;
		Coord c = (Coord)obj;
		return xchunk == c.xchunk && zchunk == c.zchunk;
	}
}

class PlayerCoord extends Coord{
	public List<QuestPlayer> players = new ArrayList<QuestPlayer>();
	public PlayerCoord(int x, int z) { super(x, z); }
	public PlayerCoord(Coord c) { super(c.xchunk, c.zchunk); }	
}
