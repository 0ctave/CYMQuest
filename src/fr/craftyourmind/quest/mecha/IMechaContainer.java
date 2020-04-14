package fr.craftyourmind.quest.mecha;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import fr.craftyourmind.quest.QuestPlayer;

public interface IMechaContainer {
	public int getTypeContainer();
	public int getId();
	public void setId(int id);
	public String getName();
	public void init();
	public String getParamsCon();
	public void loadParamsCon(String params);
	public int loadParamsCon(String[] params);
	public void removeMecha(Mechanism m);
	public void addMecha(Mechanism m);
	public List<Mechanism> getMechas();
	public Mechanism getMecha(int id);
	public Mechanism getMecha(String name);
	public void clearMechas();
	public IMechaDriver getDriver(QuestPlayer qp);
	public IMechaDriver newDriver(QuestPlayer qp);
	public IMechaDriver newDriverGuest(QuestPlayer qp);
	public IMechaDriver getDriverGuest(QuestPlayer qp);
	public IMechaDriver newDriver(Entity e);
	public Map<String, MechaParam> getMechaParams();
	public MechaParam getMechaParam(String param);
	public List<String> getNameMechaParams();
	public void addConParams(IMechaDriver driver);
	public void updateConParams(Map<String, MechaParam> addmps, List<String> removemps);
	public AbsStateContainer getState(QuestPlayer qp);
	public AbsStateContainer getStateOrCreate(QuestPlayer qp);
	public AbsStateContainer newStateContainer(QuestPlayer qp);
	public AbsStateContainer createStateContainer(QuestPlayer qp);
	public AbsStateContainer loadStateContainer(QuestPlayer qp);
	public boolean isActivated();
	public void removeState(QuestPlayer qp);
	public IMechaParamSave getMechaParamSave();
	public String getMechaParamPlayers(String name);
	public void updateMechaParamPlayer(String name, QuestPlayer qp, String value);
	public void updateMechaParamAll(String name, String value);
}