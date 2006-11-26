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
package net.sf.l2j.gameserver.templates;

import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2MinionData;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.skills.Stats;

/**
 * This cl contains all generic data of a L2Spawn object.<BR><BR>
 * 
 * <B><U> Data</U> :</B><BR><BR>
 * <li>npcId, type, name, sex</li>
 * <li>revardExp, revardSp</li>
 * <li>aggroRange, factionId, factionRange</li>
 * <li>rhand, lhand, armor</li>
 * <li>isUndead</li>
 * <li>_drops</li>
 * <li>_minions</li>
 * <li>_teachInfo</li>
 * <li>_skills</li>
 * <li>_questsStart</li><BR><BR>
 * 
 * @version $Revision: 1.1.2.4 $ $Date: 2005/04/02 15:57:51 $
 */
public final class L2NpcTemplate extends L2CharTemplate
{
	public final int     npcId;
    public final int     idTemplate;
	public final String  type;
	public final String  name;
    public final boolean serverSideName;
    public final String  title;
    public final boolean serverSideTitle;
	public final String  sex;
	public final byte    level;
	public final int     revardExp;
	public final int     revardSp;
	public final int     aggroRange;
	public final int     rhand;
	public final int     lhand;
	public final int     armor;
	public final String  factionId;
	public final int     factionRange;
	public final boolean isUndead;
    public final int     absorb_level;
	
	private final StatsSet npcStatsSet;

	/** fixed skills*/
	public int     race;
	public double  rateHp;
	
	
	/** The table containing all Item that can be dropped by L2NpcInstance using this L2NpcTemplate*/
	private final List<L2DropData>    _drops       = new FastList<L2DropData>(0);
	private final List<L2DropData>    _fulldrops       = new FastList<L2DropData>(0);
	private final List<L2DropData>    _miscdrops       = new FastList<L2DropData>(0);
	
	/** The table containing all Minions that must be spawn with the L2NpcInstance using this L2NpcTemplate*/
	private final List<L2MinionData>  _minions     = new FastList<L2MinionData>(0);
	
	private List<ClassId>             _teachInfo;
	private Map<Integer, L2Skill> _skills;
	private Map<Stats, Integer> _resists;
	private Quest[]                   _questsStart;

	/**
	 * Constructor of L2Character.<BR><BR>
	 * 
	 * @param set The StatsSet object to transfert data to the method
	 * 
	 */
	public L2NpcTemplate(StatsSet set)
	{
		super(set);
		npcId     = set.getInteger("npcId");
        idTemplate = set.getInteger("idTemplate");
		type      = set.getString("type");
		name      = set.getString("name");
        serverSideName = set.getBool("serverSideName");
        title     = set.getString("title");
        serverSideTitle = set.getBool("serverSideTitle");
		sex       = set.getString("sex");
		level     = set.getByte("level");
		revardExp = set.getInteger("revardExp");
		revardSp  = set.getInteger("revardSp");
		aggroRange= set.getInteger("aggroRange");
		rhand     = set.getInteger("rhand");
		lhand     = set.getInteger("lhand");
		armor     = set.getInteger("armor");
		String f  = set.getString("factionId", null);
		if (f == null)
			factionId = null;
		else
			factionId = f.intern();
		factionRange  = set.getInteger("factionRange");
		isUndead      = (set.getInteger("isUndead", 0) == 1);
        absorb_level  = set.getInteger("absorb_level", 0);
		//String r = set.getString("race", null);
		//if (r == null)
		//	race = null;
		//else
		//	race = r.intern();
		race = 0;
		rateHp = 1;
		npcStatsSet = set;
		_teachInfo = null;
	}
    
    public void addTeachInfo(ClassId classId)
	{
		if (_teachInfo == null)
			_teachInfo = new FastList<ClassId>();
		_teachInfo.add(classId);
	}
	
	public ClassId[] getTeachInfo()
	{
		if (_teachInfo == null)
			return null;
		return _teachInfo.toArray(new ClassId[_teachInfo.size()]);
	}
    
	public boolean canTeach(ClassId classId)
	{
		if (_teachInfo == null)
			return false;
        
        // If the player is on a third class, fetch the class teacher
        // information for its parent class.
        if (classId.getId() >= 88)
            return _teachInfo.contains(classId.getParent());
        
		return _teachInfo.contains(classId);
	}
	
	// for all spoil, plus adena, sealstone, quest, Raidboss, and all other drops that
	// should NOT follow the rule of 1 drop per category per kill
    public void addDropData(L2DropData drop)
	{
	    if (drop.isQuestDrop()) {
//			if (_questDrops == null)
//				_questDrops = new FastList<L2DropData>(0);
//	        _questDrops.add(drop);
	    } else {
	        _drops.add(drop);
	    }
	}
    
    // for the first category: Full drops and parts (like fabrics, patterns, edges, etc)
    // only ONE of all items in this catgory should be dropped per kill .
    public void addFullDropData(L2DropData drop)
	{
	    if (drop.isQuestDrop()) {
//			should never happen here
	    } else {
	        _fulldrops.add(drop);
	    }
	}
    
    // for the second category: mats, potions, scrolls, and other miscellaneous items.
    // basically, for everything not included in the other two categories.
    // only ONE of all items in this catgory should be dropped per kill .
    public void addMiscDropData(L2DropData drop)
	{
	    if (drop.isQuestDrop()) {
//			should never happen here
	    } else {
	        _miscdrops.add(drop);
	    }
	}
    
    public void addRaidData(L2MinionData minion)
    {
    	_minions.add(minion);
    }
	
    public void addSkill(L2Skill skill)
	{
		if (_skills == null)
			_skills = new FastMap<Integer, L2Skill>();
		_skills.put(skill.getId(), skill);
	}
    public void addResist(Stats id, int resist)
	{
		if (_resists == null)
			_resists = new FastMap<Stats, Integer>();
		_resists.put(id, new Integer(resist));
	}
    public int getResist(Stats id)
	{
    	if(_resists == null || _resists.get(id) == null)
    		return 0;
		return _resists.get(id);
	}
    public int removeResist(Stats id)
	{
		return _resists.remove(id);
	}
	
	/**
	 * Return the list of all possible UNCATEGORIZED drops of this L2NpcTemplate.<BR><BR>
	 */
	public List<L2DropData> getDropData()
	{
		return _drops;
	}
	
	/**
	 * Return the list of all possible full drops and part drops of this L2NpcTemplate.<BR><BR>
	 */
	public List<L2DropData> getFullDropData()
	{
		return _fulldrops;
	}

	/**
	 * Return the list of all possible mat & miscellaneous item drops of this L2NpcTemplate.<BR><BR>
	 */
	public List<L2DropData> getMiscDropData()
	{
		return _miscdrops;
	}

    /**
     * Return the list of all possible item drops of this L2NpcTemplate.<BR>
     * (ie full drops and part drops, mats, miscellaneous & UNCATEGORIZED)<BR><BR>
     */
    public List<L2DropData> getAllDropData()
    {
        List<L2DropData> lst = new FastList<L2DropData>();
        lst.addAll(_drops);
        lst.addAll(_fulldrops);
        lst.addAll(_miscdrops);
        return lst;
    }
    
    /**
     * Empty all possible drops of this L2NpcTemplate.<BR><BR>
     */
    public void clearAllDropData()
    {
        _drops.clear();
        _fulldrops.clear();
        _miscdrops.clear();
    }

	/**
	 * Return the list of all Minions that must be spawn with the L2NpcInstance using this L2NpcTemplate.<BR><BR>
	 */
	public List<L2MinionData> getMinionData()
	{
		return _minions;
	}

    public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}
	    
	public void addStartQuests(Quest q) {
		if (_questsStart == null) {
			_questsStart = new Quest[]{q};
		} else {
			int len = _questsStart.length;
			Quest[] tmp = new Quest[len+1];
			for (int i=0; i < len; i++) {
				if (_questsStart[i].getName().equals(q.getName())) {
					_questsStart[i] = q;
					return;
	            }
				tmp[i] = _questsStart[i];
	        }
			tmp[len] = q;
			_questsStart = tmp;
	    }
	}
	
	public Quest[] getStartQuests() {
		return _questsStart;
	}
	
	public StatsSet getStatsSet()
	{
		return npcStatsSet;
	}
	public void setRace(int newrace)
	{
	    race = newrace;
	}
	public void setRateHp(double newrate)
	{
	    rateHp = newrate;
	}
}
