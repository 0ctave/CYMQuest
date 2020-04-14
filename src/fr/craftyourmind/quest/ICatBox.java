package fr.craftyourmind.quest;

import java.util.List;

import fr.craftyourmind.quest.mecha.IMechaContainer;

public interface ICatBox<M extends IMekaBox> extends IMechaContainer, IQuestSort{

	public void setId(int id);
	
	public int getId();
	
	public int getType();
	
	public void setName(String name);
	
	public String getName();

	public void create();
	
	public void save();
	
	public void delete();
	
	public void addMekabox(M box);
	
	public void removeMekabox(M box);

	public M getMekabox(int idBox);

	public List<M> getMekaboxs();
	
}