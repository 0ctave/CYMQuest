package fr.craftyourmind.quest.mecha;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import fr.craftyourmind.quest.Plugin;
import fr.craftyourmind.quest.mecha.MechaControler.Mode;

public class ActionBuildBlock extends Mechanism{

	private MechaCoordRelative coordR = new MechaCoordRelative();
	public IntegerData length = new IntegerData();
	public IntegerData height = new IntegerData();
	public IntegerData width = new IntegerData();
	
	public String inside;
	public String extrem;
	public String facex = BlockFace.NORTH.name();
	public String facey = BlockFace.UP.name();
	public String facez = BlockFace.WEST.name();
	public boolean opening = true;
	public boolean inversely = false;
	public boolean local;
	
	public Material insideMat;
	public byte insideData;
	public Material extremMat;
	public byte extremData;
	
	public IntegerData timer = new IntegerData();
	public boolean onlyAir;
	public boolean centerWidth, centerHeight, centerLength;
	private MechaDirectionRelative directR = new MechaDirectionRelative();
	
	public ActionBuildBlock() {}
	@Override
	public boolean isMechaStoppable(){ return true; }
	
	@Override
	public int getType() { return MechaType.ACTBUILDBLOCK; }
	@Override
	public String getParams() {
		return new StringBuilder("4").append(DELIMITER).append(coordR.getParams()).append(DELIMITER).append(length).append(DELIMITER).append(height).append(DELIMITER).append(width).append(DELIMITER)
				.append(opening).append(DELIMITER).append(inside).append(DELIMITER).append(extrem).append(DELIMITER).append(directR.getParams()).append(DELIMITER).append(local).append(DELIMITER)
				.append(inversely).append(DELIMITER).append(insideData).append(DELIMITER).append(extremData).append(DELIMITER).append(timer).append(DELIMITER).append(onlyAir).append(DELIMITER)
				.append(centerWidth).append(DELIMITER).append(centerLength).append(DELIMITER).append(centerHeight).toString();
	}
	@Override
	public String getParamsGUI() {
		StringBuilder param = new StringBuilder();
		if(insideMat != null && extremMat != null){
			param.append(insideMat.getKey().toString()).append(DELIMITER).append(extremMat.getKey().toString());
		}
		return param.toString();
	}
	@Override
	protected void loadParams(String[] params) {
		if(params.length == 15){
			coordR.setCoord(params[0], Integer.valueOf(params[1]), Integer.valueOf(params[2]), Integer.valueOf(params[3]), 0, false);
			length.load(params[4]);
			height.load(params[5]);
			width.load(params[6]);
			opening = Boolean.valueOf(params[7]);
			inside = params[8];
			extrem = params[9];
			facex = String.valueOf(params[10]);
			facey = String.valueOf(params[11]);
			facez = String.valueOf(params[12]);
			local = Boolean.valueOf(params[13]);
			inversely = Boolean.valueOf(params[14]);
			insideMat = Material.getMaterial(inside);
			extremMat = Material.getMaterial(extrem);
			directR.setCardinalNorthSouth(facex);
			directR.setCardinalUpDown(facey);
			directR.setCardinalEastWest(facez);
			sqlSave();
		}else if(params.length == 17){
			coordR.setCoord(params[0], Integer.valueOf(params[1]), Integer.valueOf(params[2]), Integer.valueOf(params[3]), 0, false);
			length.load(params[4]);
			height.load(params[5]);
			width.load(params[6]);
			opening = Boolean.valueOf(params[7]);
			inside = params[8];
			extrem = params[9];
			facex = String.valueOf(params[10]);
			facey = String.valueOf(params[11]);
			facez = String.valueOf(params[12]);
			local = Boolean.valueOf(params[13]);
			inversely = Boolean.valueOf(params[14]);
			insideData = Byte.valueOf(params[15]);
			extremData = Byte.valueOf(params[16]);
			insideMat = Material.getMaterial(inside);
			extremMat = Material.getMaterial(extrem);
			directR.setCardinalNorthSouth(facex);
			directR.setCardinalUpDown(facey);
			directR.setCardinalEastWest(facez);
			sqlSave();
		}else{
			int version = Integer.valueOf(params[0]);
			if(version == 2){
				int index = coordR.loadParams(1, params);
				length.load(params[index++]);
				height.load(params[index++]);
				width.load(params[index++]);
				opening = Boolean.valueOf(params[index++]);
				inside = params[index++];
				extrem = params[index++];
				facex = String.valueOf(params[index++]);
				facey = String.valueOf(params[index++]);
				facez = String.valueOf(params[index++]);
				local = Boolean.valueOf(params[index++]);
				inversely = Boolean.valueOf(params[index++]);
				insideData = Byte.valueOf(params[index++]);
				extremData = Byte.valueOf(params[index++]);
				insideMat = Material.getMaterial(inside);
				extremMat = Material.getMaterial(extrem);
				directR.setCardinalNorthSouth(facex);
				directR.setCardinalUpDown(facey);
				directR.setCardinalEastWest(facez);
				sqlSave();
			}else if(version == 3){
				int index = coordR.loadParams(1, params);
				length.load(params[index++]);
				height.load(params[index++]);
				width.load(params[index++]);
				opening = Boolean.valueOf(params[index++]);
				inside = params[index++];
				extrem = params[index++];
				facex = String.valueOf(params[index++]);
				facey = String.valueOf(params[index++]);
				facez = String.valueOf(params[index++]);
				local = Boolean.valueOf(params[index++]);
				inversely = Boolean.valueOf(params[index++]);
				insideData = Byte.valueOf(params[index++]);
				extremData = Byte.valueOf(params[index++]);
				insideMat = Material.getMaterial(inside);
				extremMat = Material.getMaterial(extrem);
				timer.load(params[index++]);
				onlyAir = Boolean.valueOf(params[index++]);
				directR.setCardinalNorthSouth(facex);
				directR.setCardinalUpDown(facey);
				directR.setCardinalEastWest(facez);
				sqlSave();
			}else if(version == 4){
				int index = coordR.loadParams(1, params);
				length.load(params[index++]);
				height.load(params[index++]);
				width.load(params[index++]);
				opening = Boolean.valueOf(params[index++]);
				inside = params[index++];
				extrem = params[index++];
				index = directR.loadParams(index, params);
				local = Boolean.valueOf(params[index++]);
				inversely = Boolean.valueOf(params[index++]);
				insideData = Byte.valueOf(params[index++]);
				extremData = Byte.valueOf(params[index++]);
				insideMat = Material.getMaterial(inside);
				extremMat = Material.getMaterial(extrem);
				timer.load(params[index++]);
				onlyAir = Boolean.valueOf(params[index++]);
				centerWidth = Boolean.valueOf(params[index++]);
				centerLength = Boolean.valueOf(params[index++]);
				centerHeight = Boolean.valueOf(params[index++]);
			}
		}
	}
	
	public void modifyBlock(List<Block> blocks, Player p, Block ref, Material mat, byte data){
		boolean allow = true;
		if(onlyAir) allow = ref.getType() == Material.AIR;
		if(allow){
			if(local){
				if(p != null) p.sendBlockChange(ref.getLocation(), mat, data);
			}else{
				ref.setType(mat);
				//ref.setBlockData(data);
			}
			blocks.add(ref);
		}
	}
	@Override
	public AbsMechaStateEntity newState(MechaControler mc, IMechaDriver driver) { return new StateBuildBlock(this, mc, driver); }
	// ------------------ StateBuildBlock ------------------
	class StateBuildBlock extends AbsMechaStateEntity implements Runnable{
		private MechaCoordRelative coordR = new MechaCoordRelative();
		private IntegerData length = new IntegerData();
		private IntegerData height = new IntegerData();
		private IntegerData width = new IntegerData();
		private IntegerData timer = new IntegerData();
		private MechaDirectionRelative directR = new MechaDirectionRelative();
		public BlockFace blockFaceX;
		public BlockFace blockFaceY;
		public BlockFace blockFaceZ;
		private int idTask = 0;
		private List<Block> blocks = new ArrayList<Block>();
		
		public StateBuildBlock(Mechanism m, MechaControler mc, IMechaDriver driver) { super(m, mc, driver); }
		
		@Override
		public void cloneData() {
			super.cloneData();
			coordR.clone(this, ActionBuildBlock.this.coordR);
			length.clone(this, ActionBuildBlock.this.length);
			height.clone(this, ActionBuildBlock.this.height);
			width.clone(this, ActionBuildBlock.this.width);
			timer.clone(this, ActionBuildBlock.this.timer);
			directR.clone(this, ActionBuildBlock.this.directR);
			blockFaceX = BlockFace.valueOf(directR.getCardinalNorthSouth());
			blockFaceY = BlockFace.valueOf(directR.getCardinalUpDown());
			blockFaceZ = BlockFace.valueOf(directR.getCardinalEastWest());
		}
		@Override
		public Mode getMode() { return timer.get() > 0 ? Mode.MULTIPLE : Mode.ONE; }
		@Override
		public void start() {
			super.start();
			if(local && !driver.hasPlayer()){}
			else{
				Location loc = coordR.getLocationRandomRadius(driver);
				if(loc != null){
					Block b = loc.getWorld().getBlockAt(loc);
					if(b != null && insideMat != null && extremMat != null){
						if(timer.get() > 0){
							blocks = buildBlocks(driver, b);
							idTask = Bukkit.getScheduler().scheduleSyncDelayedTask(Plugin.it, this, timer.get()*20);
						}else
							buildBlocks(driver, b);
					}
				}
			}
			launchMessage();
			if(timer.get() <= 0) stop();
		}
		@Override
		public void run() {
			stop();
		}
		@Override
		public void stop() {
			super.stop();
			Bukkit.getScheduler().cancelTask(idTask);
			for(Block b : blocks){
				if(local){ if(driver.hasPlayer()) driver.getPlayer().sendBlockChange(b.getLocation(), Material.AIR, (byte) 0);
				}else b.setType(Material.AIR);
			}
		}
		
		private List<Block> buildBlocks(IMechaDriver driver, Block ref){
			Location locRef = ref.getLocation();
			int xloc = locRef.getBlockX();
			int yloc = locRef.getBlockY();
			int zloc = locRef.getBlockZ();
			Location locDirect = directR.getDirection(driver, locRef);
			if(!directR.isNo()){ // ----------- direction relative -----------
				if(directR.isYawRelative()){
					String cardinalRef = directR.getCardinalName(locDirect.getYaw());
					String cardinalNS = directR.getCardinalNorthSouth(locDirect.getYaw());
					String cardinalEW = directR.getCardinalEastWest(locDirect.getYaw());
					blockFaceX = BlockFace.valueOf(cardinalNS);
					blockFaceZ = BlockFace.valueOf(cardinalEW);
					if(cardinalRef.equals("EAST") || cardinalRef.equals("WEST")) inversely = true;
					else inversely = false;
					if((centerWidth && width.get() > 2) || (centerLength && length.get() > 2)){ // width length center
						if(cardinalRef.equals("WEST") && cardinalNS.equals("NORTH") && cardinalEW.equals("WEST")){
							zloc = getCenterWidth(zloc, true);
							xloc = getCenterLength(xloc, true);
						}
						else if(cardinalRef.equals("WEST") && cardinalNS.equals("SOUTH") && cardinalEW.equals("WEST")){
							zloc = getCenterWidth(zloc, false);
							xloc = getCenterLength(xloc, true);
						}
						else if(cardinalRef.equals("SOUTH") && cardinalNS.equals("SOUTH") && cardinalEW.equals("WEST")){
							xloc = getCenterWidth(xloc, true);
							zloc = getCenterLength(zloc, false);
						}
						else if(cardinalRef.equals("SOUTH") && cardinalNS.equals("SOUTH") && cardinalEW.equals("EAST")){
							xloc = getCenterWidth(xloc, false);
							zloc = getCenterLength(zloc, false);
						}
						else if(cardinalRef.equals("EAST") && cardinalNS.equals("SOUTH") && cardinalEW.equals("EAST")){
							zloc = getCenterWidth(zloc, false);
							xloc = getCenterLength(xloc, false);
						}
						else if(cardinalRef.equals("EAST") && cardinalNS.equals("NORTH") && cardinalEW.equals("EAST")){
							zloc = getCenterWidth(zloc, true);
							xloc = getCenterLength(xloc, false);
						}
						else if(cardinalRef.equals("NORTH") && cardinalNS.equals("NORTH") && cardinalEW.equals("EAST")){
							xloc = getCenterWidth(xloc, false);
							zloc = getCenterLength(zloc, true);
						}
						else if(cardinalRef.equals("NORTH") && cardinalNS.equals("NORTH") && cardinalEW.equals("WEST")){
							xloc = getCenterWidth(xloc, true);
							zloc = getCenterLength(zloc, true);
						}
					}
				}
				if(directR.isPitchRelative()) blockFaceY = BlockFace.valueOf(directR.getCardinalUpDown(locDirect.getPitch()));
			}else if(directR.isNo() || !directR.isYawRelative()){ // ----------- direction pas relative -----------
				if((centerWidth && width.get() > 2) || (centerLength && length.get() > 2)){ // width length center
					String cardinalNS = directR.getCardinalNorthSouth(locDirect.getYaw());
					String cardinalEW = directR.getCardinalEastWest(locDirect.getYaw());
					if(inversely){
						if(cardinalNS.equals("NORTH") && cardinalEW.equals("WEST")){
							zloc = getCenterWidth(zloc, true);
							xloc = getCenterLength(xloc, true);
						}
						else if(cardinalNS.equals("NORTH") && cardinalEW.equals("EAST")){
							zloc = getCenterWidth(zloc, true);
							xloc = getCenterLength(xloc, false);
						}
						else if(cardinalNS.equals("SOUTH") && cardinalEW.equals("EAST")){
							zloc = getCenterWidth(zloc, false);
							xloc = getCenterLength(xloc, false);
						}
						else if(cardinalNS.equals("SOUTH") && cardinalEW.equals("WEST")){
							zloc = getCenterWidth(zloc, false);
							xloc = getCenterLength(xloc, true);
						}
					}else{
						if(cardinalNS.equals("NORTH") && cardinalEW.equals("WEST")){
							xloc = getCenterWidth(xloc, true);
							zloc = getCenterLength(zloc, true);
						}
						else if(cardinalNS.equals("NORTH") && cardinalEW.equals("EAST")){
							xloc = getCenterWidth(xloc, false);
							zloc = getCenterLength(zloc, true);
						}
						else if(cardinalNS.equals("SOUTH") && cardinalEW.equals("EAST")){
							xloc = getCenterWidth(xloc, false);
							zloc = getCenterLength(zloc, false);
						}
						else if(cardinalNS.equals("SOUTH") && cardinalEW.equals("WEST")){
							xloc = getCenterWidth(xloc, true);
							zloc = getCenterLength(zloc, false);
						}
					}
				}
			}
			if(centerHeight && height.get() > 2){
				if(directR.getCardinalUpDown(locDirect.getPitch()).equals("UP")) yloc = yloc - (height.get() / 2);
				else yloc = yloc + (height.get() / 2);
			}
			ref = ref.getWorld().getBlockAt(xloc, yloc, zloc);
			if(inversely) return rectangle(driver.getPlayer(), ref, width.get(), height.get(), length.get());
			else return rectangle(driver.getPlayer(), ref, length.get(), height.get(), width.get());
		}
		
		private int getCenterWidth(int w, boolean plus){
			if(plus) return (centerWidth && width.get() > 2)?w + (width.get() / 2):w;
			else return (centerWidth && width.get() > 2)?w - (width.get() / 2):w;
		}
		
		private int getCenterLength(int l, boolean plus){
			if(plus) return (centerLength && length.get() > 2)?l + (length.get() / 2):l;
			else return (centerLength && length.get() > 2)?l - (length.get() / 2):l;
		}
		
		public List<Block> rectangle(Player p, Block ref, int length, int height, int width){
			List<Block> blocks = new ArrayList<Block>();
			int len = length-1;
			int hei = height-1;
			int wid = width-1;
			for(int i = 0 ; i < length ; i++){
				Block x = ref.getRelative(blockFaceX, i);
				for(int j = 0 ; j < height ; j++){
					Block y = x.getRelative(blockFaceY, j);
					for(int k = 0 ; k < width ; k++){
						Block z = y.getRelative(blockFaceZ, k);
						if(i == 0 || j == 0 || k == 0 || i == len || j == hei || k == wid){
							if(opening){
								if(inversely){
									if(i != 0 && i != len && j != 0 && j != hei) modifyBlock(blocks, p, z, Material.AIR, (byte) 0);
									else modifyBlock(blocks, p, z, extremMat, extremData);
								}else{
									if(k != 0 && k != wid && j != 0 && j != hei) modifyBlock(blocks, p, z, Material.AIR, (byte) 0);
									else modifyBlock(blocks, p, z, extremMat, extremData);
								}
							}else modifyBlock(blocks, p, z, extremMat, extremData);	
						}else modifyBlock(blocks, p, z, insideMat, insideData);
					}
				}
			}
			return blocks;
		}
	}
}