package fr.craftyourmind.quest.mecha;

import java.util.Random;

import org.bukkit.util.Vector;
import fr.craftyourmind.quest.QuestTools;

public class MechaVelocity {

	private FloatData speed = new FloatData(0.5f);
	private IntegerData accuracy = new IntegerData(200);
	private Random rand = new Random();
	
	public int loadParams(int index, String[] params) {
		speed.load(params[index++]);
		accuracy.load(params[index++]);
		control();
		return index;
	}
	
	public String getParams() {
		return speed+QuestTools.DELIMITER+accuracy;
	}

	public void clone(AbsMechaStateEntity mse, MechaVelocity velocity) {
		speed.clone(mse, velocity.speed);
		accuracy.clone(mse, velocity.accuracy);
		control();
	}
	
	private void control(){
		if(accuracy.get() < 1) accuracy.set(1); else if(accuracy.get() > 1000) accuracy.set(1000);
		if(speed.get() < -15) speed.set(-15f); else if(speed.get() > 15) speed.set(15f);
	}	

	public Vector getVelocity(float yaw, float pitch) {
        int acc = accuracy.get();
        double xwep = (rand.nextInt(acc) - rand.nextInt(acc) + 0.5D) / 1000.0D;
        double ywep = (rand.nextInt(acc) - rand.nextInt(acc) + 0.5D) / 1000.0D;
        double zwep = (rand.nextInt(acc) - rand.nextInt(acc) + 0.5D) / 1000.0D;
        double xd = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) + xwep;
        double yd = Math.sin(Math.toRadians(pitch)) + ywep;
        double zd = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) + zwep;
        Vector vec = new Vector(xd, yd, zd);
        vec.multiply(speed.get());
		return vec;
	}
	
	public static Vector convertVelocity(float yaw, float pitch){
        double xd = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
        double yd = Math.sin(Math.toRadians(pitch));
        double zd = -Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
        return new Vector(xd, yd, zd);
	}
}