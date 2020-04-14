package fr.craftyourmind.quest;

import fr.craftyourmind.manager.CYMManager;
import fr.craftyourmind.manager.CYMTextScreen;
import fr.craftyourmind.quest.mecha.AbsMechaStateEntity;
import fr.craftyourmind.quest.mecha.IntegerData;
import fr.craftyourmind.quest.mecha.StringData;

public class QuestTextScreen extends CYMTextScreen{

	public StringData text = new StringData();
	public IntegerData timer = new IntegerData();
	public IntegerData color = new IntegerData(0xffffff);
	public IntegerData colorBackground = new IntegerData(0x101010);
	public StringData idMat = new StringData();
	public IntegerData dataMat = new IntegerData();

	@Override
	public String getDatas() {
		super.text = text.get();
		super.timer = timer.get();
		super.color = color.get();
		super.colorBackground = colorBackground.get();
		super.idMat = idMat.get();
		super.dataMat = dataMat.get();
		return super.getDatas();
	}
	
	public void clone(AbsMechaStateEntity mse, QuestTextScreen ts){
		id = ts.id;
		useTextScreen = ts.useTextScreen;
		text.clone(mse, ts.text);
		position = ts.position;
		timer.clone(mse, ts.timer);
		showTimer = ts.showTimer;
		color.clone(mse, ts.color);
		background = ts.background;
		colorBackground.clone(mse, ts.colorBackground);
		shadow = ts.shadow;
		idMat.clone(mse, ts.idMat);
		dataMat.clone(mse, ts.dataMat);
		posMat = ts.posMat;
	}

	public String getParams() {
		if(!useTextScreen) return new StringBuilder("0").append(CYMManager.DELIMITER).append(useTextScreen).toString();
		return new StringBuilder("0").append(CYMManager.DELIMITER)
				.append(useTextScreen).append(CYMManager.DELIMITER)
				.append(text).append(CYMManager.DELIMITER)
				.append(position).append(CYMManager.DELIMITER)
				.append(timer).append(CYMManager.DELIMITER)
				.append(showTimer).append(CYMManager.DELIMITER)
				.append(color).append(CYMManager.DELIMITER)
				.append(background).append(CYMManager.DELIMITER)
				.append(colorBackground).append(CYMManager.DELIMITER)
				.append(shadow).append(CYMManager.DELIMITER)
				.append(idMat).append(CYMManager.DELIMITER)
				.append(dataMat).append(CYMManager.DELIMITER)
				.append(posMat).toString();
	}
	
	public int load(int index, String[] params){
		int version = Integer.valueOf(params[index++]);
		useTextScreen = Boolean.valueOf(params[index++]);
		if(useTextScreen){
			text.load(params[index++]);
			position = Integer.valueOf(params[index++]);
			timer.load(params[index++]);
			showTimer = Boolean.valueOf(params[index++]);
			color.load(params[index++]);
			background = Boolean.valueOf(params[index++]);
			colorBackground.load(params[index++]);
			shadow = Boolean.valueOf(params[index++]);
			idMat.load(params[index++]);
			dataMat.load(params[index++]);
			posMat = Integer.valueOf(params[index++]);
		}
		return index;
	}
}