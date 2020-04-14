package fr.craftyourmind.skill;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import fr.craftyourmind.quest.QuestPlayer;
import fr.craftyourmind.quest.QuestTools;
import fr.craftyourmind.quest.mecha.AbsStateContainer;
import fr.craftyourmind.quest.mecha.IMechaParamSave;
import fr.craftyourmind.quest.mecha.MechaParamSave;
import fr.craftyourmind.quest.mecha.StarterTier;
import fr.craftyourmind.quest.mecha.StringData;

public abstract class StateSkillManager extends AbsStateContainer{

	public static final String DELIMITER = QuestTools.DELIMITER;
	
	private ISkillManager con;
	private CYMLevel cymLevel;
	
	private List<Descriptive> descriptives = new ArrayList<Descriptive>();
	private MechaParamSave level;
	private MechaParamSave xp;
	private int preidtier;
	private boolean isLevelEnd;
	private List<StateSkillManager> linkParents = new ArrayList<StateSkillManager>();
	private List<StateSkillManager> linkChilds = new ArrayList<StateSkillManager>();
	private List<StateSkillManager> nodeParents = new ArrayList<StateSkillManager>();
	private List<StateSkillManager> nodeChilds = new ArrayList<StateSkillManager>();
	
	private IMechaParamSave mpsLevel = new IMechaParamSave() { @Override public void save() { if(!setlevel) updateLevel(); updateStateSql(); } };
	private IMechaParamSave mpsXp = new IMechaParamSave() { @Override public void save() { if(!setxp) changeXp(xp.getInt(), true); updateStateSql(); } };
	
	private boolean setlevel, setxp, changexp, activatedLinks;
	private int xplinks;
	
	public StateSkillManager(ISkillManager con, QuestPlayer qp) {
		super(con, qp);
		this.con = con;
		level = new MechaParamSave(false, "1", mpsLevel);
		xp  = new MechaParamSave(false, "0", mpsXp);
		level.setSystem(true); xp.setSystem(true);
		preidtier = con.getIdTier(level.getInt());
	}
	@Override
	public void cloneData(){
		driver.addConParams(con, "level", level);
		driver.addConParams(con, "xp", xp);
		descriptives.clear();
		for(fr.craftyourmind.skill.SkillManager.Descriptive desc : con.getDescriptivesList()){
			Descriptive d = new Descriptive();
			d.idIcon = desc.idIcon;
			d.dataIcon = desc.dataIcon;
			d.descriptive.clone(this, desc.get());
			descriptives.add(d);
		}
	}
	@Override
	public boolean activate() {
		if(isActivated() || !con.isActivated()) return false;
		
		if(!canActivate()) return false;
		
		for(StateSkillManager ssm : linkParents){
			if(!ssm.isLevelEnd) return false;
		}
		setActivate(true);
		
		if(isActivated()){
			super.activate();
			if(con.isShowPlayer() && con.isShowMessage()) qp.sendMessage(ChatColor.GOLD+"Activate "+con.getName()+" . . .");
			
			if(con.getStarterActivate() != null)
				con.getStarterActivate().start(driver);
			return true;
		}else return false;
	}
	
	public boolean deactivate(){
		if(isActivated()){
			if(con.isShowPlayer() && con.isShowMessage()) qp.sendMessage(ChatColor.GOLD+". . . deactivate "+con.getName());
			if(con.getStarterDeactivate() != null)
				con.getStarterDeactivate().start(driver);
			
		}
		return super.deactivate();
	}
	
	public void updateLevel() {
		cymLevel = con.getLevel(level.getInt());
		changeLevel(level.getInt(), true);
	}
	
	public void updateTier() {
		updateLevel();
	}
	
	public void changeLevel(int lvl){
		changeLevel(lvl, false);
	}
	
	public void changeLevel(int lvl, boolean force){
		if(force || (isActivated() && lvl != level.getInt())){
			int tmplvl = level.getInt();
			isLevelEnd = false;
			
			if(cymLevel != null){
				if(lvl >= cymLevel.lvlEnd){
					cymLevel = con.getLevel(lvl);
					if(cymLevel != null){
						if(lvl >= cymLevel.lvlEnd){
							lvl = cymLevel.lvlEnd;
							activatedLinks();
						}
					}
				}else if(lvl < cymLevel.lvlBegin){
					cymLevel = con.getLevel(lvl);
					if(cymLevel != null){
						if(lvl < cymLevel.lvlBegin){
							lvl = cymLevel.lvlBegin;
							deactivatedLinks();
						}
					}
				}
			}
			
			setLevel(lvl);
			
			if(cymLevel != null){
				int basexp = cymLevel.getBaseXp(level.getInt());
				if(basexp == 0)
					setXp(0);
				else if(xp.getInt() >= basexp){
					setXp(basexp-1);
				}
			}
			
			if(tmplvl != level.getInt() && con.getStarterEachLevel() != null)
				con.getStarterEachLevel().start(driver);
			
			int idtier = con.getIdTier(level.getInt());
			if(preidtier != idtier){
				preidtier = idtier;
				if(con.getStarterEachTier() != null)
					con.getStarterEachTier().start(driver);
				for(StarterTier st : con.getStarterSelectTier()){
					if(st.idtier == idtier) st.start(driver);
				}
			}
		}
	}
	
	public void changeXp(int xp){
		changeXp(xp, false);
	}
	
	public void changeXp(int xp, boolean force){
		if(force || (isActivated() && xp != this.xp.getInt())){
			
			if(!changexp){
				int tmpxp = this.xp.getInt();
				int diffxp = xp - tmpxp;
				for(StateSkillManager ssm : nodeChilds){
					if(!ssm.isLevelEnd && !ssm.activatedLinks){
						ssm.changeXp(ssm.getXP() + diffxp, force);
					}
					ssm.activatedLinks = false;
				}
			}
			
			if(cymLevel != null){
				int basexp = cymLevel.getBaseXp(level.getInt());
				if(basexp == 0 && xp >= 0){
					setXpOnly(0);
				}else if(xp >= basexp){
					xp -= basexp;
					xplinks = xp;
					changeLevel(level.getInt() + 1, force);
					changexp = true; changeXp(xp, force); changexp = false;
				}else if(xp < 0){
					int tmplvl = level.getInt();
					xplinks = xp;
					changeLevel(level.getInt() - 1, force);
					if(cymLevel != null){
						basexp = cymLevel.getBaseXp(level.getInt());
						xp += basexp;
					}else xp += this.xp.getInt();
					if(tmplvl != level.getInt()){
						changexp = true; changeXp(xp, force); changexp = false;
					}else setXp(0);
				}else{
					setXp(xp);
				}
			}else{
				setXp(xp);
			}
		}
	}
	public int getLevelLimit() { return con.getLevelLimit(); }
	public int getLevel() { return level.getInt(); }
	public void setLevel(int level){
		if(level != this.level.getInt()){
			setLevelOnly(level);
		}
	}
	protected void setLevelOnly(int level){ setlevel = true; this.level.setInt(level); setlevel = false; }
	public void setLevelNoSave(int level){ this.level.setIntNoSave(level); }
	
	public int getXPLimit() {
		if(cymLevel == null) return 0;
		int xplimit = cymLevel.getBaseXp(level.getInt());
		if(xplimit == 0) return 0;
		return xplimit;
	}
	public int getXP() { return xp.getInt(); }
	public void setXp(int xp){
		if(xp != this.xp.getInt()){
			setXpOnly(xp);
		}
	}
	protected void setXpOnly(int xp){ setxp = true; this.xp.setInt(xp); setxp = false; }
	public void setXpNoSave(int xp){ this.xp.setIntNoSave(xp); }
	
	public int getTierLimit() { return con.getTierLimit(); }
	public int getTier(){ return con.getTier(level.getInt()); }
	
	private void activatedLinks() {
		isLevelEnd = true;
		if(!con.isKeepEnableOnLink()) deactivate();
		for(StateSkillManager ssm : linkChilds){
			ssm.activatedLinks = true;
			ssm.activate();
			ssm.changeXp(xplinks);
		}
	}
	private void deactivatedLinks() {
		deactivate();
		for(StateSkillManager ssm : linkParents){
			ssm.activate();
			ssm.isLevelEnd = false;
			ssm.changeXp(xplinks);
		}
	}
	// --- LINKS
	public void updateLinks() {
		if(con.getLinkParents().isEmpty()) linkParents.clear();
		else{
			List<StateSkillManager> tmplinkParents = new ArrayList<StateSkillManager>(nodeParents);
			for(ISkillManager sm : con.getLinkParents()){
				boolean find = false;
				for(StateSkillManager ssm : linkParents){
					if(ssm.getId() == sm.getId()){
						tmplinkParents.remove(ssm);
						find = true;
						break;
					}
				}
				if(!find){
					StateSkillManager ssm = (StateSkillManager) sm.getStateOrCreate(qp);
					linkParents.add(ssm);
					if(!ssm.linkChilds.contains(this)) ssm.linkChilds.add(this);
				}
			}
		}
		if(con.getLinkChilds().isEmpty()) linkChilds.clear();
		else{
			List<StateSkillManager> tmplinkChilds = new ArrayList<StateSkillManager>(linkChilds);
			for(ISkillManager sm : con.getNodeChilds()){
				boolean find = false;
				for(StateSkillManager ssm : linkChilds){
					if(ssm.getId() == sm.getId()){
						tmplinkChilds.remove(ssm);
						find = true;
						break;
					}
				}
				if(!find){
					StateSkillManager ssm = (StateSkillManager) sm.getStateOrCreate(qp);
					linkChilds.add(ssm);
					if(!ssm.linkParents.contains(this)) ssm.linkParents.add(this);
				}
			}
			linkChilds.removeAll(tmplinkChilds);
		}
	}
	
	public void checkLinks() {
		if(!linkParents.isEmpty()){
			boolean activated = false;
			for(StateSkillManager ssm : linkParents){
				if(ssm.isLevelEnd){
					activated = true;
					break;
				}
			}
			if(activated) activate(); else deactivate();
		}
		for(StateSkillManager ssm : linkChilds){
			if(isLevelEnd) ssm.activate(); else ssm.deactivate();
		}
	}
	// --- NODES
	public void updateNodes(){
		if(con.getNodeParents().isEmpty()) nodeParents.clear();
		else{
			List<StateSkillManager> tmpnodeParents = new ArrayList<StateSkillManager>(nodeParents);
			for(ISkillManager sm : con.getNodeParents()){
				boolean find = false;
				for(StateSkillManager ssm : nodeParents){
					if(ssm.getId() == sm.getId()){
						tmpnodeParents.remove(ssm);
						find = true;
						ssm.activate();
						break;
					}
				}
				if(!find){
					StateSkillManager ssm = (StateSkillManager) sm.getStateOrCreate(qp);
					nodeParents.add(ssm);
					if(!ssm.nodeChilds.contains(this)) ssm.nodeChilds.add(this);
					ssm.activate();
				}
			}
			nodeParents.removeAll(tmpnodeParents);
		}
			
		if(con.getNodeChilds().isEmpty()) nodeChilds.clear();
		else{
			List<StateSkillManager> tmpnodeChilds = new ArrayList<StateSkillManager>(nodeChilds);
			for(ISkillManager sm : con.getNodeChilds()){
				boolean find = false;
				for(StateSkillManager ssm : nodeChilds){
					if(ssm.getId() == sm.getId()){
						tmpnodeChilds.remove(ssm);
						find = true;
						break;
					}
				}
				if(!find){
					AbsStateContainer sc = sm.getState(qp);
					if(sc != null){
						StateSkillManager ssm = (StateSkillManager) sc;
						nodeChilds.add(ssm);
						if(!ssm.nodeParents.contains(this)) ssm.nodeParents.add(this);
					}
				}
			}
			nodeChilds.removeAll(tmpnodeChilds);
		}
	}
	
	@Override
	public boolean canActivate() {
		for(StateSkillManager ssmp : nodeParents){
			if(ssmp.con.getLimitNode() > 0){
				int i = 0;
				for(StateSkillManager ssmc : ssmp.nodeChilds){
					if(ssmc.isActivated()) i++;
					if(i >= ssmp.con.getLimitNode()) return false;
				}
			}
		}
		return super.canActivate();
	}
	
	public ISkillManager getSkillManager(){ return con; }
	
	public String getDescriptives(){
		StringBuilder sb = new StringBuilder().append(descriptives.size());
		for(Descriptive d : descriptives) sb.append(DELIMITER).append(d.descriptive.get()).append(DELIMITER).append(d.idIcon).append(DELIMITER).append(d.dataIcon);
		return sb.toString();
	}
	
	public int getOrder(){ return con.getOrder(); }
	
	class Descriptive{
		private int idIcon, dataIcon;
		private StringData descriptive = new StringData();
	}
}