package fr.craftyourmind.quest.mecha;

public abstract class AbsMechaStateEntitySave extends AbsMechaStateEntity{

	public AbsMechaStateEntitySave(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
	@Override
	public void start() {
		super.start();
		sqlStart();
	}
	@Override
	public void stop() {
		super.stop();
		sqlStop();
	}
}