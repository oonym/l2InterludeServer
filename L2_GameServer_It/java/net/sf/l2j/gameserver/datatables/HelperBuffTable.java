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
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.templates.L2HelperBuff;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * This class represents the Newbie Helper Buff list
 *
 * Author: Ayor
 *
 */

public class HelperBuffTable
{

    private static Logger _log = Logger.getLogger(HennaTable.class.getName());

    private static HelperBuffTable _instance;

    /** The table containing all Buff of the Newbie Helper */
    private List<L2HelperBuff> _helperBuff;

    private boolean _initialized = true;

    /** The player level since Newbie Helper can give the fisrt buff <BR>
     *  Used to generate message : "Come back here when you have reached level ...") */
    private int _magicClassLowestLevel   = 100;
    private int _physicClassLowestLevel  = 100;

    /** The player level above which Newbie Helper won't give any buff <BR>
     *  Used to generate message : "Only novice character of level ... or less can receive my support magic.") */
    private int _magicClassHighestLevel  = 1;
    private int _physicClassHighestLevel = 1;


    public static HelperBuffTable getInstance()
    {
        if (_instance == null)
        {
            _instance = new HelperBuffTable();
        }
        return _instance;
    }


    /**
     * Create and Load the Newbie Helper Buff list from SQL Table helper_buff_list
     */
    private HelperBuffTable()
    {
        _helperBuff = new FastList<L2HelperBuff>();
        restoreHelperBuffData();

    }

    /**
     * Read and Load the Newbie Helper Buff list from SQL Table helper_buff_list
     */
    private void restoreHelperBuffData()
    {
        java.sql.Connection con = null;
        try
        {
            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT * FROM helper_buff_list");
                ResultSet helperbuffdata = statement.executeQuery();

                fillHelperBuffTable(helperbuffdata);
                helperbuffdata.close();
                statement.close();
            }
            catch (Exception e)
            {
                _log.severe("Table helper_buff_list not found : Update your DataPack" + e);
                e.printStackTrace();
            }

        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }


    /**
     * Load the Newbie Helper Buff list from SQL Table helper_buff_list
     */
    private void fillHelperBuffTable(ResultSet HelperBuffData) throws Exception
    {

        while (HelperBuffData.next())
        {
            StatsSet helperBuffDat = new StatsSet();
            int id = HelperBuffData.getInt("id");

            helperBuffDat.set("id", id);
            helperBuffDat.set("skillID", HelperBuffData.getInt("skill_id"));
            helperBuffDat.set("skillLevel", HelperBuffData.getInt("skill_level"));
            helperBuffDat.set("lowerLevel", HelperBuffData.getInt("lower_level"));
            helperBuffDat.set("upperLevel", HelperBuffData.getInt("upper_level"));
            helperBuffDat.set("isMagicClass", HelperBuffData.getString("is_magic_class"));


            // Calulate the range level in wich player must be to obtain buff from Newbie Helper
            if("false".equals(HelperBuffData.getString("is_magic_class")))
            {
                if(HelperBuffData.getInt("lower_level")<_physicClassLowestLevel)
                    _physicClassLowestLevel=HelperBuffData.getInt("lower_level");

                if(HelperBuffData.getInt("upper_level")>_physicClassHighestLevel)
                    _physicClassHighestLevel=HelperBuffData.getInt("upper_level");
            }
            else
            {
                if(HelperBuffData.getInt("lower_level")<_magicClassLowestLevel)
                    _magicClassLowestLevel=HelperBuffData.getInt("lower_level");

                if(HelperBuffData.getInt("upper_level")>_magicClassHighestLevel)
                    _magicClassHighestLevel=HelperBuffData.getInt("upper_level");
            }

            // Add this Helper Buff to the Helper Buff List
            L2HelperBuff template = new L2HelperBuff(helperBuffDat);
            _helperBuff.add(template);
        }

        _log.config("Helper Buff Table: Loaded " + _helperBuff.size() + " Templates.");

    }

    public boolean isInitialized()
    {
        return _initialized;
    }


    public L2HelperBuff getHelperBuffTableItem(int id)
    {
        return _helperBuff.get(id);
    }


    /**
     * Return the Helper Buff List
     */
    public List<L2HelperBuff> getHelperBuffTable()
    {
        return _helperBuff;
    }


    /**
     * @return Returns the magicClassHighestLevel.
     */
    public int getMagicClassHighestLevel()
    {
        return _magicClassHighestLevel;
    }



    /**
     * @param magicClassHighestLevel The magicClassHighestLevel to set.
     */
    public void setMagicClassHighestLevel(int magicClassHighestLevel)
    {
        _magicClassHighestLevel = magicClassHighestLevel;
    }



    /**
     * @return Returns the magicClassLowestLevel.
     */
    public int getMagicClassLowestLevel()
    {
        return _magicClassLowestLevel;
    }



    /**
     * @param magicClassLowestLevel The magicClassLowestLevel to set.
     */
    public void setMagicClassLowestLevel(int magicClassLowestLevel)
    {
        _magicClassLowestLevel = magicClassLowestLevel;
    }



    /**
     * @return Returns the physicClassHighestLevel.
     */
    public int getPhysicClassHighestLevel()
    {
        return _physicClassHighestLevel;
    }



    /**
     * @param physicClassHighestLevel The physicClassHighestLevel to set.
     */
    public void setPhysicClassHighestLevel(int physicClassHighestLevel)
    {
        _physicClassHighestLevel = physicClassHighestLevel;
    }



    /**
     * @return Returns the physicClassLowestLevel.
     */
    public int getPhysicClassLowestLevel()
    {
        return _physicClassLowestLevel;
    }



    /**
     * @param physicClassLowestLevel The physicClassLowestLevel to set.
     */
    public void setPhysicClassLowestLevel(int physicClassLowestLevel)
    {
        _physicClassLowestLevel = physicClassLowestLevel;
    }


}
