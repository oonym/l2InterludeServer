/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;

/**
 * This class ...
 * 
 * @version $Revision: 1.13.2.2.2.8 $ $Date: 2005/04/06 16:13:25 $
 */
public class SkillTreeTable
{
	private static Logger _log = Logger.getLogger(SkillTreeTable.class.getName());
	private static SkillTreeTable _instance;
	
	private Map<ClassId, List<L2SkillLearn>> _skillTrees;
    private List<L2SkillLearn> _fishingSkillTrees; //all common skills (teached by Fisherman)
    private List<L2SkillLearn> _expandDwarfCraftSkillTrees; //list of special skill for dwarf (expand dwarf craft) learned by class teacher
	
    public static SkillTreeTable getInstance()
	{
        if (_instance == null)
            _instance = new SkillTreeTable();
        
		return _instance;
	}
	
	/**
	 * Return the minimum level needed to have this Expertise.<BR><BR>
	 * 
	 * @param grade The grade level searched
	 */
	public int getExpertiseLevel(int grade) 
	{
		if (grade <= 0)
			return 0;
		
		List<L2SkillLearn> lst = getSkillTrees().get(ClassId.paladin);
		
		for (L2SkillLearn sl : lst) 
			if (sl.getId() == 239 && sl.getLevel() == grade)
				return sl.getMinLevel();
		
		throw new Error("Expertise not found for grade "+grade);
	}
        
    public int getMinSkillLevel(int skillID, ClassId classID, int skillLvl) 
    {
    	if (skillID > 0 && skillLvl > 0)
    	{
    		List<L2SkillLearn> lst = getSkillTrees().get(classID); 
    		
			for (L2SkillLearn sl : lst)
				if (sl.getLevel() == skillLvl && sl.getId() == skillID)
					return sl.getMinLevel();
    	}
    	
    	return 0;
    }
	
    public int getMinSkillLevel(int skillID, int skillLvl) 
    {
        if (skillID > 0 && skillLvl > 0)
        {
            for (List<L2SkillLearn> lst: getSkillTrees().values())
                for (L2SkillLearn sl : lst)
                    if (sl.getLevel() == skillLvl && sl.getId() == skillID)
                    	return sl.getMinLevel();
        }
        
        return 0;
    }
    
	private SkillTreeTable()
	{
	    int classId = 0;
        int count   = 0;
        
		java.sql.Connection con = null;
        
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM class_list ORDER BY id");
			ResultSet classlist = statement.executeQuery();
			
			List<L2SkillLearn> list = new FastList<L2SkillLearn>();
			int parentClassId;
			L2SkillLearn skill;
			
			while (classlist.next())
			{
				list = new FastList<L2SkillLearn>();
				parentClassId = classlist.getInt("parent_id");
				classId = classlist.getInt("id");
				PreparedStatement statement2 = con.prepareStatement("SELECT class_id, skill_id, level, name, sp, min_level FROM skill_trees where class_id=? ORDER BY skill_id, level");
				statement2.setInt(1, classId);
				ResultSet skilltree = statement2.executeQuery();

				if (parentClassId != -1)
				{
					List<L2SkillLearn> parentList = getSkillTrees().get(ClassId.values()[parentClassId]);
					list.addAll(parentList);
				}
				
				int prevSkillId = -1;
				
				while (skilltree.next())
				{
					int id = skilltree.getInt("skill_id");
					int lvl = skilltree.getInt("level");
					String name = skilltree.getString("name");
					int minLvl = skilltree.getInt("min_level");
					int cost = skilltree.getInt("sp");

					if (prevSkillId != id)
						prevSkillId = id;

                    skill = new L2SkillLearn(id, lvl, minLvl, name, cost, 0, 0);
					list.add(skill);
				}
				
				getSkillTrees().put(ClassId.values()[classId], list);
				skilltree.close();
				statement2.close();
				
                count += list.size();
				_log.fine("SkillTreeTable: skill tree for class " + classId + " has " + list.size() + " skills");		
			}
			
			classlist.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.severe("Error while creating skill tree (Class ID " + classId + "):" + e);
		} 
 
        _log.config("SkillTreeTable: Loaded " + count + " skills.");
        
        //Skill tree for fishing skill (from Fisherman)
        int count2   = 0;
        int count3   = 0;
        
        try
        {
            _fishingSkillTrees = new FastList<L2SkillLearn>();
            _expandDwarfCraftSkillTrees = new FastList<L2SkillLearn>();

            PreparedStatement statement = con.prepareStatement("SELECT skill_id, level, name, sp, min_level, costid, cost, isfordwarf FROM fishing_skill_trees ORDER BY skill_id, level");
            ResultSet skilltree2 = statement.executeQuery();
        
            int prevSkillId = -1;
            
            while (skilltree2.next())
            {
                int id = skilltree2.getInt("skill_id");
                int lvl = skilltree2.getInt("level");
                String name = skilltree2.getString("name");
                int minLvl = skilltree2.getInt("min_level");
                int cost = skilltree2.getInt("sp");
                int costId = skilltree2.getInt("costid");
                int costCount = skilltree2.getInt("cost");
                int isDwarven = skilltree2.getInt("isfordwarf");
                
                if (prevSkillId != id)
                    prevSkillId = id;
            
                L2SkillLearn skill = new L2SkillLearn(id, lvl, minLvl, name, cost, costId, costCount);
                
                if (isDwarven == 0)
                    _fishingSkillTrees.add(skill);
                else 
                    _expandDwarfCraftSkillTrees.add(skill);                    
            }            
        
            skilltree2.close();
            statement.close();
            
            count2 = _fishingSkillTrees.size();
            count3 = _expandDwarfCraftSkillTrees.size();
		}
        catch (Exception e)
        {
            _log.severe("Error while creating fishing skill table: " + e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
        
        _log.config("FishingSkillTreeTable: Loaded " + count2 + " general skills.");
        _log.config("FishingSkillTreeTable: Loaded " + count3 + " dwarven skills.");
    }
	
    private Map<ClassId, List<L2SkillLearn>> getSkillTrees()
    {
        if (_skillTrees == null)
            _skillTrees = new FastMap<ClassId, List<L2SkillLearn>>();
        
        return _skillTrees;
    }
    
	public L2SkillLearn[] getAvailableSkills(L2PcInstance cha, ClassId classId)
	{
		List<L2SkillLearn> result = new FastList<L2SkillLearn>();
		List<L2SkillLearn> skills = getSkillTrees().get(classId);
        
		if (skills == null)
		{
			// the skilltree for this class is undefined, so we give an empty list
			_log.warning("Skilltree for class " + classId + " is not defined !");
			return new L2SkillLearn[0];
		}
		
		L2Skill[] oldSkills = cha.getAllSkills();
		
        for (L2SkillLearn temp : skills)
        {           
            if (temp.getMinLevel() <= cha.getLevel())
            {
                boolean knownSkill = false;
            
                for (int j = 0; j < oldSkills.length && !knownSkill; j++)
                {
                    if (oldSkills[j].getId() == temp.getId() )
                    {
                        knownSkill = true;
            
                        if ( oldSkills[j].getLevel() == temp.getLevel()-1)
                        {
                            // this is the next level of a skill that we know
                            result.add(temp);
                        }
                    }
                }
                
                if (!knownSkill && temp.getLevel() == 1)
                {
                    // this is a new skill
                    result.add(temp);
                }
            }
        }
            
        return result.toArray(new L2SkillLearn[result.size()]);
	}
    
	public L2SkillLearn[] getAvailableSkills(L2PcInstance cha)
	{
	    List<L2SkillLearn> result = new FastList<L2SkillLearn>();
	    List<L2SkillLearn> skills = new FastList<L2SkillLearn>();
        
        skills.addAll(_fishingSkillTrees);
            
	    if (skills == null)
	    {
	        // the skilltree for this class is undefined, so we give an empty list
	        _log.warning("Skilltree for fishing is not defined !");
	        return new L2SkillLearn[0];
	    }
            
        if (cha.hasDwarvenCraft() && _expandDwarfCraftSkillTrees != null)
        {
            skills.addAll(_expandDwarfCraftSkillTrees);
        }
        
        L2Skill[] oldSkills = cha.getAllSkills();
        
        for (L2SkillLearn temp : skills)
        {           
            if (temp.getMinLevel() <= cha.getLevel())
            {
                boolean knownSkill = false;
            
                for (int j = 0; j < oldSkills.length && !knownSkill; j++)
                {
                    if (oldSkills[j].getId() == temp.getId() )
                    {
                        knownSkill = true;
            
                        if ( oldSkills[j].getLevel() == temp.getLevel()-1)
                        {
                            // this is the next level of a skill that we know
                            result.add(temp);
                        }
                    }
                }
                
                if (!knownSkill && temp.getLevel() == 1)
                {
                    // this is a new skill
                    result.add(temp);
                }
            }
        }
        
        return result.toArray(new L2SkillLearn[result.size()]);
	}
	
	/**
	 * Returns all allowed skills for a given class. - Fairy
	 * @param classId
	 * @return
	 */
	public List<L2SkillLearn> getAllowedSkills(ClassId classId)
	{
		return getSkillTrees().get(classId);
	}

	public int getMinLevelForNewSkill(L2PcInstance cha, ClassId classId)
	{
        int minLevel = 0;
		List<L2SkillLearn> skills = getSkillTrees().get(classId);
        
		if (skills == null)
		{
			// the skilltree for this class is undefined, so we give an empty list
			_log.warning("Skilltree for class " + classId + " is not defined !");
			return minLevel;
        }       
        
        for (L2SkillLearn temp: skills)
        {           
            if (temp.getMinLevel() > cha.getLevel())
                if (minLevel==0 || temp.getMinLevel()<minLevel)
                    minLevel = temp.getMinLevel();
		}
        
		return minLevel;
	}
    
    public int getMinLevelForNewSkill(L2PcInstance cha)
    {
        int minlevel = 0;       
        List<L2SkillLearn> skills = new FastList<L2SkillLearn>();
        
        skills.addAll(_fishingSkillTrees);
            
        if (skills == null)
        {
            // the skilltree for this class is undefined, so we give an empty list
            _log.warning("Skilltree for fishing is not defined !");
            return minlevel;
        }
            
        if (cha.hasDwarvenCraft() && _expandDwarfCraftSkillTrees != null)
        {
            skills.addAll(_expandDwarfCraftSkillTrees);
        }
        
        for (L2SkillLearn s : skills)
        {
            if (s.getMinLevel() > cha.getLevel())
                if (minlevel ==0 || s.getMinLevel() < minlevel)
                    minlevel = s.getMinLevel();
        }
        
        return minlevel;
    }
	
    public int getSkillCost(L2PcInstance player, L2Skill skill)
    {
        int skillCost = 100000000;
        ClassId classId = player.getClassId();
        List<L2SkillLearn> skillLearnList = getSkillTrees().get(classId);
        
        for (L2SkillLearn skillLearn : skillLearnList)
        {
            if (skillLearn.getId() != skill.getId())
                continue;
            
            if (skillLearn.getLevel() != skill.getLevel())
                continue;
            
            if (skillLearn.getMinLevel() > player.getLevel())
                continue;
            
            skillCost = skillLearn.getSpCost();
            
            if (!player.getClassId().equalsOrChildOf(classId))
            {
                if (skill.getCrossLearnAdd() < 0)
                    continue;
                
                skillCost += skill.getCrossLearnAdd();
                skillCost *= skill.getCrossLearnMul();
            }
            
            if ((classId.getRace() != player.getRace()) && !player.isSubClassActive())
                skillCost *= skill.getCrossLearnRace();
            
            if (classId.isMage() != player.getClassId().isMage())
                skillCost *= skill.getCrossLearnProf();
        }
        
        return skillCost;
    }
}
