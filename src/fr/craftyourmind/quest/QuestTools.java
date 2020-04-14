package fr.craftyourmind.quest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import net.minecraft.server.v1_15_R1.Blocks;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class QuestTools {

	public static final String DELIMITER = ";";
	
	// old
	public static Location getLocation(Player p, World w, int x, int y, int z, int radius, boolean ground, boolean onPlayer){
		double xxx, zzz;
		
		Random rand = new Random();

		int refx, xx, refy, yy, refz, zz;
		refx = xx = x;
		refy = yy = y;
		refz = zz = z;
		if(onPlayer){
			if(p == null) return null;
			Location loc = p.getLocation();
			w = loc.getWorld();
			refx = xx = loc.getBlockX()+x;
			refy = yy = loc.getBlockY()+y;
			refz = zz = loc.getBlockZ()+z;
		}
		if(radius > 0){
			xx += rand.nextInt(radius) * ((rand.nextBoolean())?1:-1);
			yy += rand.nextInt(radius) * ((rand.nextBoolean())?1:-1);
			zz += rand.nextInt(radius) * ((rand.nextBoolean())?1:-1);
		}
		if(ground){
			Block type = w.getBlockAt(xx, yy, zz);
			if(type == Blocks.AIR){
				while(type == Blocks.AIR && yy > 5) type = w.getBlockAt(xx, --yy, zz);
				yy++;
				if(radius > 0 && yy < refy-radius){ xx = refx; yy = refy; zz = refz;}
			}else{
				while(type != Blocks.AIR && yy < 251) type = w.getBlockAt(xx, ++yy, zz);
				if(radius > 0 && yy > refy+radius){ xx = refx; yy = refy; zz = refz;}
			}
		}else{
			if(w.getBlockAt(xx, yy, zz) != Blocks.AIR){ xx = refx; yy = refy; zz = refz;}
		}
		
		xxx = xx + 0.5;
		zzz = zz + 0.5;
		return new Location(w, xxx, yy, zzz);
	}
	
	public static Location getLocation(World w, double x, double y, double z, int radius, boolean ground){
		double refx, xx, refy, yy, refz, zz;
		Random rand = new Random();
		
		refx = xx = x;
		refy = yy = y;
		refz = zz = z;
		
		if(radius > 0){
			xx += rand.nextInt(radius) * ((rand.nextBoolean())?1:-1);
			yy += rand.nextInt(radius) * ((rand.nextBoolean())?1:-1);
			zz += rand.nextInt(radius) * ((rand.nextBoolean())?1:-1);
		}
		if(ground){
			Block type = w.getBlockAt((int)xx, (int)yy, (int)zz);
			if(type == Blocks.AIR){
				while(type == Blocks.AIR && yy > 5) type = w.getBlockAt((int)xx, (int)--yy, (int)zz);
				yy++;
				if(radius > 0 && yy < refy-radius){ xx = refx; yy = refy; zz = refz;}
			}else{
				while(type != Blocks.AIR && yy < 251) type = w.getBlockAt((int)xx, (int)++yy, (int)zz);
				if(radius > 0 && yy > refy+radius){ xx = refx; yy = refy; zz = refz;}
			}
		}
		return new Location(w, xx, yy, zz);
	}
	
	public static  boolean insideLoc(String world, int x, int y, int z, Location loc, int radius){
		return world.equals(loc.getWorld().getName()) && loc.getBlockX() >= x - radius && loc.getBlockX() <= x + radius && loc.getBlockZ() >= z - radius && loc.getBlockZ() <= z + radius && (y == 0 || (loc.getBlockY() >= y - radius - 1 && loc.getBlockY() <= y + radius));
	}
	
	public static void copyFile(String path, String name){
		File f = new File(Plugin.it.getDataFolder()+File.separator+path, name);
		if(!f.exists()){
			try {
				f.createNewFile();
				OutputStream output = new FileOutputStream(f);
				InputStream input = Plugin.it.getResource(path+"/"+name);
				byte[] buf = new byte[8192];
				int length = input.read(buf);
			    while(length >= 0){
			    	output.write(buf, 0, length);
			    	length = input.read(buf);
			    }
		        input.close();
		        output.close();
			} catch (IOException e) { Plugin.log("- error copy "+name); e.printStackTrace(); }
		}
	}
}