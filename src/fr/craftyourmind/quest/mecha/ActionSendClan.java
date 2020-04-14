package fr.craftyourmind.quest.mecha;

import fr.craftyourmind.manager.CYMClan;

public class ActionSendClan extends Mechanism{

	public StringData nameclan = new StringData();
	public StringData messageClan = new StringData();

	@Override
	public int getType() { return MechaType.ACTSENDCLAN; }
	@Override
	public String getParams() { return nameclan+DELIMITER+messageClan; }
	@Override
	public String getParamsGUI() { return ""; }
	@Override
	protected void loadParams(String[] params) {
		nameclan.set("");
		messageClan.set("");
		if(params.length >= 1) nameclan.load(params[0]);
		if(params.length == 2) messageClan.load(params[1]);
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateClan(this, mc, driver); }
	// ------------------ StateClan ------------------
	class StateClan extends AbsMechaStateEntity{

		private StringData nameclan = new StringData();
		private StringData messageClan = new StringData();
		private CYMClan clan;
		
		public StateClan(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		@Override
		public void cloneData() {
			super.cloneData();
			nameclan.clone(this, ActionSendClan.this.nameclan);
			messageClan.clone(this, ActionSendClan.this.messageClan);
			clan = CYMClan.get(nameclan.get());
		}
		@Override
		public void start() {
			if(clan != null){ //  && !driver.getQuestPlayer().spy
				clan.sendMessage(messageClan.get());
				launchMessage();
			}
		}
	}
}