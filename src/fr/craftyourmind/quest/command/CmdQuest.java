package fr.craftyourmind.quest.command;

import java.io.IOException;

import org.bukkit.entity.Player;
import fr.craftyourmind.manager.command.AbsCYMCommand;
import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.mecha.AbsMechaContainer;
import fr.craftyourmind.quest.mecha.IMechaContainer;

public class CmdQuest extends AbsCYMCommand{

	private static final int MEKAPARAMS = 0;
	
	public CmdQuest() { super(null, CmdQuestManager.CMDQUEST); }
	@Override
	public void initChilds() { }
	@Override
	public void initActions() {
		addAction(new MEKAPARAMS());
	}

	class MEKAPARAMS extends AbsCYMCommandAction{
		private static final int LIST = 0;
		private static final int UPDATE = 1;
		private static final int UPDATEALL = 2;
		private int action, idCon, typeContainer, idPlayer;
		private String name = "", playersValues = "", value = "";
		@Override
		public int getId() { return MEKAPARAMS; }
		@Override
		public AbsCYMCommandAction clone() { return new MEKAPARAMS(); }
		@Override
		public void initSend(Player p) {
			IMechaContainer con = AbsMechaContainer.get(typeContainer, idCon);
			if(con != null)
				playersValues = con.getMechaParamPlayers(name);
		}		
		@Override
		public void sendWrite() throws IOException {
			write(playersValues);
		}
		@Override
		public void receiveRead() throws IOException {
			action = readInt();
			idCon = readInt();
			typeContainer = readInt();
			name = readStr();
			idPlayer = readInt();
			value = readStr();
		}
		@Override
		public void receive(Player p) {
			if(p.hasPermission("cymquest.skilledit")){
				if(action == LIST){
					send(p, this);
				}else if(action == UPDATE){
					IMechaContainer con = AbsMechaContainer.get(typeContainer, idCon);
					if(con != null)
						con.updateMechaParamPlayer(name, QuestPlayer.get(idPlayer), value);
					send(p, this);
				}else if(action == UPDATEALL){
					IMechaContainer con = AbsMechaContainer.get(typeContainer, idCon);
					if(con != null)
						con.updateMechaParamAll(name, value);
					send(p, this);
				}
			}
		}
	}
}