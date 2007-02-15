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
package net.sf.l2j.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.templates.L2PcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * This class ...
 * 
 * @version $Revision: 1.6.2.1.2.10 $ $Date: 2005/03/29 14:00:54 $
 */
public class CharTemplateTable
{
	private static Logger _log = Logger.getLogger(CharTemplateTable.class.getName());
			
	private static CharTemplateTable _instance;
	
    public static final String[] charClasses = {
                                                "Human Fighter", "Warrior", "Gladiator", "Warlord", "Human Knight", "Paladin", "Dark Avenger", "Rogue", "Treasure Hunter", "Hawkeye", "Human Mystic", "Human Wizard", "Sorceror", "Necromancer", "Warlock", "Cleric", "Bishop", "Prophet",
                                                "Elven Fighter", "Elven Knight", "Temple Knight", "Swordsinger", "Elven Scout", "Plainswalker", "Silver Ranger", "Elven Mystic", "Elven Wizard", "Spellsinger", "Elemental Summoner", "Elven Oracle", "Elven Elder",
                                                "Dark Fighter", "Palus Knight", "Shillien Knight", "Bladedancer", "Assassin", "Abyss Walker", "Phantom Ranger", "Dark Elven Mystic", "Dark Elven Wizard", "Spellhowler", "Phantom Summoner", "Shillien Oracle", "Shillien Elder",
                                                "Orc Fighter", "Orc Raider", "Destroyer", "Orc Monk", "Tyrant", "Orc Mystic", "Orc Shaman", "Overlord", "Warcryer",
                                                "Dwarven Fighter", "Dwarven Scavenger", "Bounty Hunter", "Dwarven Artisan", "Warsmith", 
                                                "dummyEntry1", "dummyEntry2", "dummyEntry3", "dummyEntry4", "dummyEntry5", "dummyEntry6", "dummyEntry7", "dummyEntry8", "dummyEntry9", "dummyEntry10", "dummyEntry11", "dummyEntry12", "dummyEntry13", "dummyEntry14", "dummyEntry15",
                                                "dummyEntry16", "dummyEntry17", "dummyEntry18", "dummyEntry19", "dummyEntry20", "dummyEntry21", "dummyEntry22", "dummyEntry23", "dummyEntry24", "dummyEntry25", "dummyEntry26", "dummyEntry27", "dummyEntry28", "dummyEntry29", "dummyEntry30", 
                                                "Duelist", "DreadNought", "Phoenix Knight", "Hell Knight", "Sagittarius", "Adventurer", 
                                                "Archmage", "Soultaker", "Arcana Lord", "Cardinal", "Hierophant", 
                                                "Eva Templar", "Sword Muse", "Wind Rider", "Moonlight Sentinel", "Mystic Muse", "Elemental Master", "Eva's Saint", 
                                                "Shillien Templar", "Spectral Dancer", "Ghost Hunter", "Ghost Sentinel", "Storm Screamer", "Spectral Master", "Shillien Saint", 
                                                "Titan", "Grand Khauatari", "Dominator", "Doomcryer", 
                                                "Fortune Seeker", "Maestro"
    };
    
	private Map<Integer, L2PcTemplate> _templates;
	
	public static CharTemplateTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new CharTemplateTable();
		}
		return _instance;
	}
	
	private CharTemplateTable()
	{
		_templates = new FastMap<Integer, L2PcTemplate>();
		java.sql.Connection con = null;
		
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(
					"SELECT * FROM class_list, char_templates, lvlupgain" +
					" WHERE class_list.id = char_templates.classId" +
					" AND class_list.id = lvlupgain.classId" +
					" ORDER BY class_list.id");
			ResultSet rset = statement.executeQuery();

			while (rset.next())
			{
				StatsSet set = new StatsSet();
				//ClassId classId = ClassId.values()[rset.getInt("id")];
				set.set("classId", rset.getInt("id"));
				set.set("className", rset.getString("className"));
				set.set("raceId", rset.getInt("raceId"));
				set.set("baseSTR", rset.getInt("STR"));
				set.set("baseCON", rset.getInt("CON"));
				set.set("baseDEX", rset.getInt("DEX"));
				set.set("baseINT", rset.getInt("_INT"));
				set.set("baseWIT", rset.getInt("WIT"));
				set.set("baseMEN", rset.getInt("MEN"));
				set.set("baseHpMax", rset.getFloat("defaultHpBase"));
				set.set("lvlHpAdd", rset.getFloat("defaultHpAdd"));
				set.set("lvlHpMod", rset.getFloat("defaultHpMod"));
				set.set("baseMpMax", rset.getFloat("defaultMpBase"));
                set.set("baseCpMax", rset.getFloat("defaultCpBase"));
                set.set("lvlCpAdd", rset.getFloat("defaultCpAdd"));
                set.set("lvlCpMod", rset.getFloat("defaultCpMod"));
				set.set("lvlMpAdd", rset.getFloat("defaultMpAdd"));
				set.set("lvlMpMod", rset.getFloat("defaultMpMod"));
				set.set("baseHpReg", 1.5);
				set.set("baseMpReg", 0.9);
				set.set("basePAtk", rset.getInt("p_atk"));
				set.set("basePDef", /*classId.isMage()? 77 : 129*/ rset.getInt("p_def"));
				set.set("baseMAtk", rset.getInt("m_atk"));
				set.set("baseMDef", rset.getInt("char_templates.m_def"));
				set.set("classBaseLevel", rset.getInt("class_lvl"));
				set.set("basePAtkSpd", rset.getInt("p_spd"));
				set.set("baseMAtkSpd", /*classId.isMage()? 166 : 333*/ rset.getInt("char_templates.m_spd"));
				set.set("baseCritRate", rset.getInt("char_templates.critical")/10);
				set.set("baseRunSpd", rset.getInt("move_spd"));
				set.set("baseLoad", rset.getInt("_load"));
				set.set("baseShldDef", 0);
				set.set("baseShldRate", 0);
				set.set("baseAtkRange", 40);

				set.set("spawnX", rset.getInt("x"));
				set.set("spawnY", rset.getInt("y"));
				set.set("spawnZ", rset.getInt("z"));

				L2PcTemplate ct;
				
				set.set("collision_radius", rset.getDouble("m_col_r"));
				set.set("collision_height", rset.getDouble("m_col_h"));
				ct = new L2PcTemplate(set);
				//5items must go here
				for (int x=1; x < 6 ;x++)
				{
					if (rset.getInt("items"+x) != 0)
					{
						ct.addItem(rset.getInt("items"+x));
					}
				}
				_templates.put(ct.classId.getId(), ct);				
			}
			
			rset.close();
			statement.close();
		}
		catch (SQLException e)
		{
			_log.warning("error while loading char templates "+e.getMessage());
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}

		_log.config("CharTemplateTable: Loaded " + _templates.size() + " Character Templates.");
	}
	
	public L2PcTemplate getTemplate(ClassId classId)
	{
		return getTemplate(classId.getId());
	}
	
	public L2PcTemplate getTemplate(int classId)
	{
		int key = classId;
		return _templates.get(key);
	}
    
    public static final String getClassNameById(int classId)
    {
        return charClasses[classId];
    }
    
    public static final int getClassIdByName(String className)
    {
        int currId = 1;
        
        for (String name : charClasses)
        {
            if (name.equalsIgnoreCase(className))
                break;
            
            currId++;
        }
        
        return currId;
    }
	
//	public L2CharTemplate[] getAllTemplates()
//	{
//		return _templates.values().toArray(new L2CharTemplate[_templates.size()]);
//	}
}
