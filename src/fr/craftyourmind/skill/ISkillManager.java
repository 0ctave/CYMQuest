package fr.craftyourmind.skill;

import java.util.List;

import fr.craftyourmind.quest.mecha.IMechaContainer;
import fr.craftyourmind.quest.mecha.MechaParam;
import fr.craftyourmind.quest.mecha.Mechanism;
import fr.craftyourmind.quest.mecha.StarterTier;
import fr.craftyourmind.skill.SkillManager.Descriptive;

public interface ISkillManager extends IMechaContainer{

	public int getId();
	public String getName();
	
	public boolean isKeepEnableOnLink();
	public void setKeepEnableOnLink(boolean b);
	
	public boolean isShowPlayer();
	public void setShowPlayer(boolean b);
	
	public boolean isActivationPlayer();
	public void setActivationPlayer(boolean b);
	
	public boolean isShowMessage();
	public void setShowMessage(boolean b);
	
	public int getOrder();
	
	public String getDescriptives();
	public void setDescriptives(String str);
	public List<Descriptive> getDescriptivesList();
	
	public String getLinkParentsStr();
	public void setLinkParents(String str);
	public void addLinkParent(ISkillManager sm);
	public void removeLinkParent(ISkillManager sm);
	public List<ISkillManager> getLinkParents();
	public String getLinkChildsStr();
	public void setLinkChilds(String str);
	public void addLinkChild(ISkillManager sm);
	public void removeLinkChild(ISkillManager sm);
	public List<ISkillManager> getLinkChilds();
	
	public int getLevelLimit();
	public CYMLevel getLevel(int lvl);
	public String getLevels();
	public void setLevels(String str);
	public void updateLevel();
	
	public int getTierLimit();
	public List<CYMTier> getCYMTiers();
	public String getTiers();
	public void setTiers(String str);
	
	public int getIdTier(int level);
	public int getTier(int level);
	public void updateTier();
	
	public String getNodeParentsStr();
	public void setNodeParents(String str);
	public void addNodeParent(ISkillManager sm);
	public void removeNodeParent(ISkillManager sm);
	public List<ISkillManager> getNodeParents();
	public String getNodeChildsStr();
	public void setNodeChilds(String strr);
	public void addNodeChild(ISkillManager sm);
	public void removeNodeChild(ISkillManager sm);
	public List<ISkillManager> getNodeChilds();
	public int getLimitNode();
	public void setLimitNode(int limit);
	public boolean isSyncNodeParents();
	public void setSyncNodeParents(boolean b);
	
	public ISkillManager getLink(int idlink);
	public List<? extends ISkillManager> getAllLinks();
	
	public void addParamSys(String name, MechaParam param, String desc);
	
	public Mechanism getStarterActivate();
	public void setStarterActivate(Mechanism m);
	public Mechanism getStarterDeactivate();
	public void setStarterDeactivate(Mechanism m);
	public Mechanism getStarterEachLevel();
	public void setStarterEachLevel(Mechanism m);
	public Mechanism getStarterEachTier();
	public void setStarterEachTier(Mechanism m);
	public List<StarterTier> getStarterSelectTier();
	public void setStarterSelectTier(StarterTier statier);
}