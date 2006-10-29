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
package net.sf.l2j.gameserver.skills;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.templates.StatsSet;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
final class DocumentSkill extends DocumentBase {

    public class Skill
    {
        public int                  id;
        public String               name;
        public StatsSet[]           sets;
        public int                  currentLevel;
        public List<L2Skill>   skills          = new FastList<L2Skill>();
        public List<L2Skill>   currentSkills   = new FastList<L2Skill>();
    }
    
    private Skill currentSkill;
    private List<L2Skill> skillsInFile  = new FastList<L2Skill>();
	
	DocumentSkill(File file)
	{
		super(file);
	}

    private void setCurrentSkill(Skill skill)
    {
        currentSkill    = skill;
    }

    protected StatsSet getStatsSet()
	{
		return currentSkill.sets[currentSkill.currentLevel];
	}
    
	protected List<L2Skill> getSkills()
	{
        return skillsInFile;
	}
    
	protected Number getTableValue(String name)
	{
		try
        {
            return tables.get(name)[currentSkill.currentLevel];
        } catch (RuntimeException e)
        {
            _log.log(Level.SEVERE, "error in table of skill Id "+currentSkill.id, e);
            return 0;
        }
	}
    
	protected Number getTableValue(String name, int idx)
	{
		try
        {
            return tables.get(name)[idx-1];
        } catch (RuntimeException e)
        {
            _log.log(Level.SEVERE, "wrong level count in skill Id "+currentSkill.id, e);
            return 0;
        }
	}
	
	protected void parseDocument(Document doc)
	{
        for (Node n=doc.getFirstChild(); n != null; n = n.getNextSibling())
        {
            if ("list".equalsIgnoreCase(n.getNodeName()))
            {
                for (Node d=n.getFirstChild(); d != null; d = d.getNextSibling())
                {
                    if ("skill".equalsIgnoreCase(d.getNodeName()))
                    {
                        setCurrentSkill(new Skill());
                        parseSkill(d);
                        skillsInFile.addAll(currentSkill.skills);
                        resetTable();
                    }
                }
            }
            else if ("skill".equalsIgnoreCase(n.getNodeName()))
            {
                setCurrentSkill(new Skill());
                parseSkill(n);
                skillsInFile.addAll(currentSkill.skills);
            }
        }
	}
    
    protected void parseSkill(Node n)
	{
		NamedNodeMap attrs = n.getAttributes();
		int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
		String skillName = attrs.getNamedItem("name").getNodeValue();
		String levels = attrs.getNamedItem("levels").getNodeValue();
		int lastLvl = Integer.parseInt(levels);
		
        currentSkill.id     = skillId;
        currentSkill.name   = skillName;
        currentSkill.sets   = new StatsSet[lastLvl];
        
		for (int i=0; i < lastLvl; i++)
		{
            currentSkill.sets[i] = new StatsSet();
            currentSkill.sets[i].set("skill_id", currentSkill.id);
            currentSkill.sets[i].set("level",    i+1);
            currentSkill.sets[i].set("name",     currentSkill.name);
		}

		if (currentSkill.sets.length != lastLvl)
			throw new RuntimeException("Skill id="+skillId+" number of levels missmatch, "+lastLvl+" levels expected");
		
		Node first = n.getFirstChild();
		for (n=first; n != null; n = n.getNextSibling())
		{
			if ("table".equalsIgnoreCase(n.getNodeName()))
				parseTable(n);
		}
		for (int i=1; i <= lastLvl; i++)
		{
			for (n=first; n != null; n = n.getNextSibling())
			{
				if ("set".equalsIgnoreCase(n.getNodeName()))
					parseBeanSet(n, currentSkill.sets[i-1], i);
			}
		}
		makeSkills();
		for (int i=0; i < lastLvl; i++)
		{
            currentSkill.currentLevel = i;
			for (n=first; n != null; n = n.getNextSibling())
			{
				if ("cond".equalsIgnoreCase(n.getNodeName()))
				{
					Condition condition = parseCondition(n.getFirstChild(), currentSkill.currentSkills.get(i));
					Node msg = n.getAttributes().getNamedItem("msg");
					if (condition != null && msg != null)
						condition.setMessage(msg.getNodeValue());
                    currentSkill.currentSkills.get(i).attach(condition, false);
				}
				if ("for".equalsIgnoreCase(n.getNodeName()))
				{
					parseTemplate(n, currentSkill.currentSkills.get(i));
				}
			}
		}
        currentSkill.skills.addAll(currentSkill.currentSkills);
	}
	
	private void makeSkills()
	{
        currentSkill.currentSkills = new FastList<L2Skill>(currentSkill.sets.length);
        //System.out.println(sets.length);
		for (int i=0; i < currentSkill.sets.length; i++)
        {//System.out.println(i);
            currentSkill.currentSkills.add(i, currentSkill.sets[i].getEnum("skillType", SkillType.class).makeSkill(currentSkill.sets[i]));
        }
	}
}
