package fr.craftyourmind.quest;

import java.util.List;

public interface IQuestSort {

	public QuestSort getSort();
	
	public int getIdSort();

	public void setOrder(int order);
	
	public int getOrder();

	public void save();

	public List<? extends IQuestSort> getSortList();
	
}