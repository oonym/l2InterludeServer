/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.entity.Zone;
import net.sf.l2j.gameserver.model.entity.ZoneType;

public class ZoneManager
{
    protected static Logger _log = Logger.getLogger(ZoneManager.class.getName());

    // =========================================================
    private static ZoneManager _Instance;
    public static final ZoneManager getInstance()
    {
        if (_Instance == null)
        {
    		System.out.println("Initializing ZoneManager");
        	_Instance = new ZoneManager();
        	_Instance.load();
        }
        return _Instance;
    }
    // =========================================================


    // =========================================================
    // Data Field
    private List<ZoneType> _ZoneTypes;
    
    // =========================================================
    // Constructor
    public ZoneManager()
    {
    }

    // =========================================================
    // Method - Public
    public int addZone(String zoneType, int id, String zoneName, int taxById)
    {
        return getZoneType(zoneType).addZone(id, zoneName, taxById);
    }

    public int addZoneCoord(String zoneType, String zoneName, int x1, int y1, int x2, int y2, int z)
    {
        return getZoneType(zoneType).getZone(zoneName).addCoord(x1, y1, x2, y2, z);
    }

    public int addZoneType(String typeName)
    {
        getZoneTypes().add(new ZoneType(typeName));
        return getZoneTypes().size() - 1;
    }

    public boolean checkIfInZone(String zoneType, L2Object obj)
    {
        return checkIfInZone(zoneType, obj.getX(), obj.getY());
    }
    
    public boolean checkIfInZone(String zoneType, int x, int y)
    {
        return ZoneManager.getInstance().getZoneType(zoneType).checkIfInZone(x, y);
    }
    
    public boolean checkIfInZonePeace(L2Object obj)
    {
        return checkIfInZonePeace(obj.getX(), obj.getY());
    }
    
    public boolean checkIfInZonePeace(int x, int y)
    {
        return (
                checkIfInZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Peace), x, y) || // object is inside peace zone or
                (                                                                   // (
                        Config.ZONE_TOWN == 0 &&                                    //   Town is set as peace zone and
                        TownManager.getInstance().checkIfInZone(x, y)               //   object is inside Town zone
                )                                                                   // )
        );
    }
    
    public boolean checkIfInZoneNoLanding(L2Object obj) 
    { 
        return checkIfInZoneNoLanding(obj.getX(), obj.getY()); 
    } 
    
    public boolean checkIfInZoneNoLanding(int x, int y) 
    { 
        return 
      ( 
            checkIfInZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.NoLanding), x, y)// object is trying to dismount in nolanding zone
        ); 
    }
    
    public boolean checkIfInZoneFishing(L2Object obj) 
    { 
        return checkIfInZoneFishing(obj.getX(), obj.getY()); 
    } 
    
    public boolean checkIfInZoneFishing(int x, int y) 
    { 
        return 
      ( 
            checkIfInZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Fishing), x, y)// check if the fishing float is in water, the player does not need to be in the water
        ); 
    } 

    public boolean checkIfInZonePvP(L2Object obj)
    {
        return checkIfInZonePvP(obj.getX(), obj.getY());
    }
    
    public boolean checkIfInZonePvP(int x, int y)
    {
        return  (
                    SiegeManager.getInstance().checkIfInZone(x, y) ||               // object is inside a siege zone or
                    checkIfInZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.OlympiadStadia), x, y) || // object is inside an olympaid stadia
                    checkIfInZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Arena), x, y) || // object is inside pvp zone or
                    (                                                               // (
                            Config.ZONE_TOWN == 2 &&                                //   Zone Town is set as PvP and
                            TownManager.getInstance().checkIfInZone(x, y)           //   object is inside town zone
                    ) ||
                    (Config.JAIL_IS_PVP && checkIfInZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Jail), x, y)) // object is inside jail
                );
    }

    public void reload()
    {
    	this.getZoneTypes().clear();
    	this.load();
    }
    
    // =========================================================
    // Method - Private
    private void load()
    {
    	java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("Select * from zone order by id");
            ResultSet rs = statement.executeQuery();

            String columnNameId                 = "id";
            String columnNameName               = "name";
            String columnNameTaxBy              = "taxById";
            String columnNameType               = "type";
            String columnNameX1                 = "x1";
            String columnNameX2                 = "x2";
            String columnNameY1                 = "y1";
            String columnNameY2                 = "y2";
            String columnNameZ                  = "z";

            Zone zone;
            while (rs.next())
            {
                zone = getZone(rs.getString(columnNameType).trim(), rs.getString(columnNameName).trim());
                zone.setId(rs.getInt(columnNameId));
                zone.setTaxById(rs.getInt(columnNameTaxBy));
                zone.addCoord(rs.getInt(columnNameX1), rs.getInt(columnNameY1), rs.getInt(columnNameX2), rs.getInt(columnNameY2), rs.getInt(columnNameZ));
            }
            statement.close();

            String msgSpace = " ";
            String msgZoneLoaded = "Zone: Loaded ";
            for (ZoneType zt: getZoneTypes())
                _log.info(msgZoneLoaded + zt.getZones().size() + msgSpace + zt.getTypeName());
        }
        catch (Exception e)
        {
            e.getMessage();
            e.printStackTrace();
        }
        finally {try { con.close(); } catch (Exception e) {}}
    }

    // =========================================================
    // Property - Public
    public final Zone getZone(String typeName, int zoneId)
    {
        return getZoneType(typeName).getZone(zoneId);
    }
    
    public final Zone getZone(String typeName, String zoneName)
    {
        return getZoneType(typeName).getZone(zoneName);
    }
    
    public final ZoneType getZoneType(String typeName)
    {
        for (ZoneType zt: getZoneTypes())
        {
            if (zt.getTypeName().equalsIgnoreCase(typeName.trim())) return zt;
        }

        getZoneTypes().add(new ZoneType(typeName));
        return getZoneTypes().get(getZoneTypes().size() - 1);
    }
    
    public final Zone getZone(String typeName, int x, int y)
    {
        ZoneType zt = ZoneManager.getInstance().getZoneType(typeName);
        if (zt != null) return zt.getZone(x, y);
        return null;
    }
    
    public final List<Zone> getZones(String typeName)
    {
        return getZoneType(typeName).getZones();
    }
    
    public final List<ZoneType> getZoneTypes()
    {
        if (_ZoneTypes == null) _ZoneTypes = new FastList<ZoneType>();
        return _ZoneTypes;
    }
}