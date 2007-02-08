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
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2LvlupData;
import net.sf.l2j.gameserver.model.base.ClassId;

/**
 * This class ...
 * 
 * @author NightMarez
 * @version $Revision: 1.3.2.4.2.3 $ $Date: 2005/03/27 15:29:18 $
 */
public class LevelUpData
{
    private static final String SELECT_ALL = "SELECT classid, defaulthpbase, defaulthpadd, defaulthpmod, defaultcpbase, defaultcpadd, defaultcpmod, defaultmpbase, defaultmpadd, defaultmpmod, class_lvl FROM lvlupgain";
    private static final String CLASS_LVL = "class_lvl";
    private static final String MP_MOD = "defaultmpmod";
    private static final String MP_ADD = "defaultmpadd";
    private static final String MP_BASE = "defaultmpbase";
    private static final String HP_MOD = "defaulthpmod";
    private static final String HP_ADD = "defaulthpadd";
    private static final String HP_BASE = "defaulthpbase";
    private static final String CP_MOD = "defaultcpmod";
    private static final String CP_ADD = "defaultcpadd";
    private static final String CP_BASE = "defaultcpbase";
    private static final String CLASS_ID = "classid";
    
    private static Logger _log = Logger.getLogger(LevelUpData.class.getName());
	
	private static LevelUpData _instance;
	
	private Map<Integer, L2LvlupData> _lvltable;
	
	public static LevelUpData getInstance()
	{
		if (_instance == null)
		{
			_instance = new LevelUpData();
		}
		return _instance;
	}
	
	private LevelUpData()
	{
		_lvltable = new FastMap<Integer, L2LvlupData>();
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_ALL);
			ResultSet rset = statement.executeQuery();
			L2LvlupData lvlDat;
			
			while (rset.next())
			{
				lvlDat = new L2LvlupData();
				lvlDat.set_classid(rset.getInt(CLASS_ID));
				lvlDat.set_classLvl(rset.getInt(CLASS_LVL));
				lvlDat.set_classHpBase(rset.getFloat(HP_BASE));
				lvlDat.set_classHpAdd(rset.getFloat(HP_ADD));
				lvlDat.set_classHpModifier(rset.getFloat(HP_MOD));
                lvlDat.set_classCpBase(rset.getFloat(CP_BASE));
                lvlDat.set_classCpAdd(rset.getFloat(CP_ADD));
                lvlDat.set_classCpModifier(rset.getFloat(CP_MOD));
				lvlDat.set_classMpBase(rset.getFloat(MP_BASE));
				lvlDat.set_classMpAdd(rset.getFloat(MP_ADD));
				lvlDat.set_classMpModifier(rset.getFloat(MP_MOD));
				
				_lvltable.put(new Integer(lvlDat.get_classid()), lvlDat);
			}
			
			rset.close();
			statement.close();

			_log.config("LevelUpData: Loaded " + _lvltable.size() + " Character Level Up Templates.");
		}
		catch (Exception e)
		{
			_log.warning("error while creating Lvl up data table "+e);
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
	}

	/**
	 * @param template id
	 * @return
	 */
	public L2LvlupData getTemplate(int classId)
	{
		return _lvltable.get(classId);
	}
	public L2LvlupData getTemplate(ClassId classId)
	{
		return _lvltable.get(classId.getId());
	}
}
