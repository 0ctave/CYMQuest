package fr.craftyourmind.quest;

import fr.craftyourmind.quest.mecha.IMechaContainer;
import fr.craftyourmind.quest.mecha.StarterBox;
import fr.craftyourmind.quest.mecha.ToolBox.TOOLMEKABOX;

public interface IMekaBox<C extends ICatBox> extends IMechaContainer, IQuestSort{

	public void setId(int idbox);
	
	public int getId();
	
	public int getType();
	
	public int getCatId();
	
	public void setName(String name);
	
	public String getName();
	
	public void init();
	
	public void create();

	public void save();
	
	public void delete();
	
	public void initStarterCreate();

	public void setCat(int cat);
	
	public void setCat(C cat);

	public StarterBox getEnter();

	public StarterBox getExit();

	public void addTool(TOOLMEKABOX toolBox);

	public void removeTool(TOOLMEKABOX toolmekabox);

	public C getCatbox();

	public void updateTool();
	
}