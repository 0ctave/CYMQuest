package fr.craftyourmind.quest.mecha;

import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import fr.craftyourmind.manager.CYMChecker.ICheckerEntity;
import fr.craftyourmind.quest.QuestPlayer;

public interface IMechaDriver {
	public QuestPlayer getQuestPlayer();
	public Player getPlayer();
	public Entity getEntity();
	public boolean isPlayer();
	public boolean isEntity();
	public Entity getEntitySource();
	public Location getLocation();
	public Location getDirection();
	public void setEntitySource(Entity e);
	public void setLocation(Location l);
	public void setDirection(Location d);
	public boolean hasPlayer();
	public boolean hasEntity();
	public boolean hasEntitySource();
	public boolean hasLocation();
	public boolean hasDirection();
	public IMechaContainer getContainer();
	public boolean hasCurrentStatesActive();
	public void cleanControllers();
	public void sqlStart(AbsMechaStateEntity smp);
	public void sqlStop(AbsMechaStateEntity smp);
	public void sendMessage(String msg);
	public ICheckerEntity getChecker();
	public String getNameEntity();
	public void addController(MechaControler mc);
	public void removeController(MechaControler mc);
	public MechaControler getControler(Mechanism m);
	public void addConParams(IMechaContainer con);
	public void addConParams(IMechaContainer con, IMechaParamSave mps);
	public void addConParams(IMechaContainer container, String name, MechaParam mp);
	public MechaParam getMechaParam(String param);
	public MechaParam getMechaParam(IMechaContainer con, String param);
	public Map<String, MechaParam> getMechaParams(IMechaContainer con);
	public Map<String, MechaParam> getSaveMechaParams(IMechaContainer con);
	public void updateConParams(IMechaContainer con, Map<String, MechaParam> addmps, List<String> removemps);
}