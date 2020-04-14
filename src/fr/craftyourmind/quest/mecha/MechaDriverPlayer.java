package fr.craftyourmind.quest.mecha;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import fr.craftyourmind.manager.CYMChecker.ICheckerEntity;
import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.sql.QuestSQLManager;

public class MechaDriverPlayer extends MechaDriver{

	private QuestPlayer qp;
	
	public MechaDriverPlayer(QuestPlayer qp, IMechaContainer con) {
		super(con);
		this.qp = qp;
	}
	public MechaDriverPlayer(QuestPlayer qp, IMechaContainer con, IMechaParamSave mps) {
		super(con, mps);
		this.qp = qp;
	}
	@Override
	public QuestPlayer getQuestPlayer() {
		return qp;
	}
	@Override
	public Player getPlayer() {
		return qp.getPlayer();
	}
	@Override
	public Entity getEntity() {
		return qp.getPlayer();
	}
	@Override
	public ICheckerEntity getChecker() {
		return qp.getCYMPlayer();
	}
	@Override
	public boolean isPlayer() {
		return true;
	}
	@Override
	public boolean isEntity() {
		return false;
	}
	@Override
	public boolean hasPlayer() {
		return qp.getPlayer() != null;
	}
	@Override
	public boolean hasEntity() {
		return qp.getPlayer() != null;
	}
	@Override
	public void sqlStart(AbsMechaStateEntity smp) {
		QuestSQLManager.start(smp);
	}
	@Override
	public void sqlStop(AbsMechaStateEntity smp) {
		QuestSQLManager.stop(smp);
	}
	@Override
	public void sendMessage(String msg) {
		qp.sendMessage(msg);
	}
	@Override
	public String getNameEntity() {
		return qp.getName();
	}
}