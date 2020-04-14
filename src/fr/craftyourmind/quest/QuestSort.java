package fr.craftyourmind.quest;

public class QuestSort {

	private IQuestSort manager;
	private int order = 0;
	
	public QuestSort(IQuestSort manager) {
		this.manager = manager;
	}
	
	public int getOrder(){ return order; }
	
	public void setOrder(int order){
		this.order = order;
	}
	
	public void upOrder(){
		if(order > 1){
			order--;
			for(IQuestSort qs : manager.getSortList()){
				if(qs.getOrder() == order  && qs.getIdSort() != manager.getIdSort()){
					qs.setOrder(qs.getOrder()+1);
					qs.save();
					break;
				}
			}
			manager.save();
		}
	}
	
	public void downOrder(){
		if(order > 0 && order < manager.getSortList().size()){
			order++;
			for(IQuestSort qs : manager.getSortList()){
				if(qs.getOrder() == order && qs.getIdSort() != manager.getIdSort()){
					qs.setOrder(qs.getOrder()-1);
					qs.save();
					break;
				}
			}
			manager.save();
		}
	}
	
	public void createOrder(int order){
		this.order = order;
		for(IQuestSort qs : manager.getSortList()){
			if(qs.getOrder() >= order && qs.getIdSort() != manager.getIdSort()){
				qs.setOrder(qs.getOrder()+1);
				qs.save();
			}
		}
	}
	
	public void deleteOrder(){
		for(IQuestSort qs : manager.getSortList()){
			if(qs.getOrder() > order && qs.getIdSort() != manager.getIdSort()){
				qs.setOrder(qs.getOrder()-1);
				qs.save();
			}
		}
	}
}