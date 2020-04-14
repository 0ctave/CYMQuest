package fr.craftyourmind.quest.command;

import java.io.IOException;
import java.util.List;

import org.bukkit.entity.Player;
import fr.craftyourmind.manager.command.CmdGuiChild;
import fr.craftyourmind.manager.command.CmdGuiEnter;
import fr.craftyourmind.manager.command.CmdGuiMain;

import fr.craftyourmind.quest.CatBox;
import fr.craftyourmind.quest.ICatBox;
import fr.craftyourmind.quest.IMekaBox;
import fr.craftyourmind.quest.MekaBox;

public class CmdMekaBox extends CmdGuiMain{

	public static final int CMDMEKA = 1;
	public static final int CMDCAT = 2;
	
	private static CmdMekaBox cmdbox;
	
	public static void sendOpen(Player p){ sendOpen(p, 0); }
	
	public static void sendOpen(Player p, int idcat){
		cmdbox.sendOpenMain(p, CMDMEKA, -1, idcat, MekaBox.BOX);
	}
	
	public CmdMekaBox() {
		super(CmdQuestManager.CMDMEKABOX);
		cmdbox = this;
		permission = "cymquest.questedit";
	}

	@Override
	public void initChilds() { addChild(new CmdMeka()); addChild(new CmdCat()); }
	
	// **************** CmdMeka ****************
	public class CmdMeka extends CmdGuiChild{

		private static final int UPDATEBOX = 6;
		
		public CmdMeka() { super(CmdMekaBox.this, CMDMEKA); }
		@Override
		public void initChilds() { }
		@Override
		public void initActions() { super.initActions(); addAction(new UPDATEBOX()); }
		@Override
		public void initSendOpen(List<Integer> idlists, List<String> namelists, List<Integer> orderlists, int... ids) {
			for(ICatBox cat : CatBox.get(ids[2])){ idlists.add(cat.getId()); namelists.add(cat.getName()); orderlists.add(cat.getOrder()); }
		}
		@Override
		public void initSendOpenChild(List<Integer> idchilds, List<String> namechilds, List<Integer> orderlists, int... ids) {
			ICatBox<IMekaBox> cat = CatBox.get(ids[2], ids[1]);
			if(cat != null){
				for(IMekaBox mb : cat.getMekaboxs()){
					idchilds.add(mb.getId());
					namechilds.add(mb.getName());
					orderlists.add(mb.getOrder());
				}
			}
		}
		@Override
		public ICmdData getCmdData() {
			return new ICmdData() {
				public int idtype, idcat, idbox, newIdcat, order;
				public String name = "", params = "";
				@Override
				public void initSend(int... ids) {
					IMekaBox mb = MekaBox.get(ids[2], ids[1], ids[0]);
					if(mb != null){
						idtype = mb.getType();
						idbox = mb.getId();
						idcat = mb.getCatId();
						name = mb.getName();
						params = mb.getParamsCon();
						order = mb.getOrder();
					}
				}
				@Override
				public void sendWrite(AbsCYMCommandAction cmd) throws IOException {
					cmd.write(idtype); cmd.write(idcat); cmd.write(idbox); cmd.write(name);
					cmd.write(params); cmd.write(order);
				}
				@Override
				public void receiveRead(AbsCYMCommandAction cmd) throws IOException {
					idtype = cmd.readInt(); idcat = cmd.readInt(); newIdcat = cmd.readInt(); idbox = cmd.readInt(); name = cmd.readStr();
					params = cmd.readStr(); order = cmd.readInt();
				}
				@Override
				public int[] receive() {
					if(idbox == 0){
						IMekaBox mb = MekaBox.newMekaboxCmd(idtype, name, idcat);
						if(mb != null){
							mb.getSort().createOrder(order);
							mb.save();
							idbox = mb.getId();
						}
					}else{
						IMekaBox mb = MekaBox.get(idtype, idcat, idbox);
						if(mb != null){
							mb.setName(name);
							mb.setCat(newIdcat);
							mb.loadParamsCon(params);
							mb.save();
							idcat = mb.getCatId();
						}
					}
					return new int[]{ idbox, idcat, idtype };
				}
			};
		}
		@Override
		public void receiveDelete(int... ids) {
			IMekaBox mb = MekaBox.get(ids[2], ids[1], ids[0]);
			if(mb != null) mb.delete();
		}
		@Override
		public void receiveSort(boolean order, int... ids) {
			IMekaBox mb = MekaBox.get(ids[2], ids[1], ids[0]);
			if(mb != null){
				if(order) mb.getSort().upOrder(); else mb.getSort().downOrder();
			}
		}
		// ----- UPDATEBOX -----
		class UPDATEBOX extends AbsCYMCommandAction{
			private int idtype, idcat, idbox;		
			@Override
			public int getId() { return UPDATEBOX; }
			@Override
			public AbsCYMCommandAction clone() { return new UPDATEBOX(); }
			@Override
			public void initSend(Player p) { }
			@Override
			public void sendWrite() throws IOException { }
			@Override
			public void receiveRead() throws IOException { idtype = readInt(); idcat = readInt(); idbox = readInt(); }
			@Override
			public void receive(Player p) {
				if(p.hasPermission("cymquest.questedit")){
					IMekaBox mb = MekaBox.get(idtype, idcat, idbox);
					if(mb != null) mb.updateTool();
				}
				actionSendOpen(p, new int[]{ idbox, idcat, idtype});
			}
		}
	}
	// **************** CATBOX ****************
	public class CmdCat extends CmdGuiEnter{

		public CmdCat() { super(CmdMekaBox.this, CMDCAT); }
		@Override
		public void initChilds() { }
		@Override
		public void initSendOpen(List<Integer> idlists, List<String> namelists, List<Integer> orderlists, int... ids) {
			for(ICatBox cat : CatBox.get(ids[1])){ idlists.add(cat.getId()); namelists.add(cat.getName()); orderlists.add(cat.getOrder()); }
		}
		@Override
		public ICmdData getCmdData() {
			return new ICmdData() {
				public int idtype, idcat, order;
				public String name;
				@Override
				public void initSend(int... ids) {
					ICatBox cat = CatBox.get(ids[1], ids[0]);
					if(cat != null){
						idtype = cat.getType();
						idcat = cat.getId();
						name = cat.getName();
						order = cat.getOrder();
					}
				}
				@Override
				public void sendWrite(AbsCYMCommandAction cmd) throws IOException {
					cmd.write(idtype); cmd.write(idcat); cmd.write(name); cmd.write(order);
				}
				@Override
				public void receiveRead(AbsCYMCommandAction cmd) throws IOException {
					idtype = cmd.readInt(); idcat = cmd.readInt(); name = cmd.readStr(); order = cmd.readInt();
				}				
				@Override
				public int[] receive() {
					if(idcat < 0){
						ICatBox cb = CatBox.newCatboxCmd(idtype, name);
						if(cb != null){
							cb.getSort().createOrder(order);
							idcat = cb.getId();
						}
					}else{
						ICatBox cat = CatBox.get(idtype, idcat);
						if(cat != null){ cat.setName(name); cat.save(); }
					}
					return new int[]{ idcat, idtype };
				}
			};
		}
		@Override
		public void receiveDelete(int... ids) {
			ICatBox cat = CatBox.get(ids[1], ids[0]);
			if(cat != null) cat.delete();
		}
		@Override
		public void receiveSort(boolean order, int... ids) {
			ICatBox cat = CatBox.get(ids[1], ids[0]);
			if(cat != null){
				if(order) cat.getSort().upOrder(); else cat.getSort().downOrder();
			}
		}
	}
}