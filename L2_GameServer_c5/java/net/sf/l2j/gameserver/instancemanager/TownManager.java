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

import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Town;
import net.sf.l2j.gameserver.model.entity.Zone;
import net.sf.l2j.gameserver.model.entity.ZoneType;

public class TownManager
{
    protected static Logger _log = Logger.getLogger(TownManager.class.getName());

    // =========================================================
    private static TownManager _Instance;
    public static final TownManager getInstance()
    {
        if (_Instance == null)
        {
    		System.out.println("Initializing TownManager");
        	_Instance = new TownManager();
        	_Instance.load();
        }
        return _Instance;
    }
    // =========================================================

    
    // =========================================================
    // Data Field
    private List<Town> _Towns;
    
    // =========================================================
    // Constructor
    public TownManager()
    {
    }

    // =========================================================
    // Method - Public
    /** Return true if object is inside zone */
    public final boolean checkIfInZone(L2Object obj) { return (getTown(obj) != null); }

    /** Return true if object is inside zone */
    public final boolean checkIfInZone(int x, int y) { return (getTown(x, y) != null); }

    public void reload()
    {
    	this.getTowns().clear();
    	this.load();
    }

    // =========================================================
    // Method - Private
    private final void load()
    {
        // TEMP UNTIL TOWN'S TABLE IS ADDED
        for (Zone zone: ZoneManager.getInstance().getZones(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Town)))
            getTowns().add(new Town(zone.getId()));
    }

    // =========================================================
    // Property - Public
    public final Town getClosestTown(L2Object activeObject)
    {
        switch (MapRegionTable.getInstance().getMapRegion(activeObject.getPosition().getX(),
															activeObject.getPosition().getY()))
		{
			case 0:
				return getTown(2); // TI
			case 1:
				return getTown(3); // Elven
			case 2:
				return getTown(1); // DE
			case 3:
				return getTown(4); // Orc
			case 4:
				return getTown(6); // Dwarven
			case 5:
				return getTown(7); // Gludio
			case 6:
				return getTown(5); // Gludin
			case 7:
				return getTown(8); // Dion
			case 8:
				return getTown(9); // Giran
			case 9:
				return getTown(10); // Oren
			case 10:
				return getTown(12); // Aden
			case 11:
				return getTown(11); // HV
			case 12:
				return getTown(16); // Floran
			case 13:
				return getTown(15); // Heine
			case 14:
				return getTown(14); // Rune
			case 15:
				return getTown(13); // Goddard
		}

        return getTown(16); // Default to floran
    }
    public final boolean townHasCastleInSeige(int townId)
    {
    	int[] castleidarray = {0,0,0,0,0,0,0,1,2,3,4,0,5,0,0,6,0};
    	int castleIndex= castleidarray[townId] ;
     
    	if ( castleIndex > 0 )
        {
           	Castle castle = CastleManager.getInstance().getCastles().get(CastleManager.getInstance().getCastleIndex(castleIndex));
           	if (castle != null)
           		return castle.getSiege().getIsInProgress();
        }
        return false;
    }

    public final boolean townHasCastleInSeige(int x, int y)
    {
        int curtown= (MapRegionTable.getInstance().getMapRegion(x, y));
        int[] castleidarray = {0,0,0,0,0,1,0,2,3,4,5,0,0,6,0,0,0};
        //find an instance of the castle for this town.
        int castleIndex = castleidarray[curtown];
        if ( castleIndex > 0 )
        {
        	Castle castle = CastleManager.getInstance().getCastles().get(CastleManager.getInstance().getCastleIndex(castleIndex));
        	if (castle != null)
        		return castle.getSiege().getIsInProgress();
        }
        return false;
    }

    public final Town getTown(int townId)
    {
        int index = getTownIndex(townId);
        if (index >= 0) return getTowns().get(index);
        return null;
    }

    public final Town getTown(L2Object activeObject) { return getTown(activeObject.getPosition().getX(), activeObject.getPosition().getY()); }

    public final Town getTown(int x, int y)
    {
        int index = getTownIndex(x, y);
        if (index >= 0) return getTowns().get(index);
        return null;
    }

    public final Town getTown(String name)
    {
        int index = getTownIndex(name);
        if (index >= 0) return getTowns().get(index);
        return null;
    }

    public final int getTownIndex(int townId)
    {
        Town town;
        for (int i = 0; i < getTowns().size(); i++)
        {
            town = getTowns().get(i);
            if (town != null && town.getTownId() == townId) return i;
        }
        return -1;
    }

    public final int getTownIndex(L2Object activeObject) { return getTownIndex(activeObject.getPosition().getX(), activeObject.getPosition().getY()); }

    public final int getTownIndex(int x, int y)
    {
        Town town;
        for (int i = 0; i < getTowns().size(); i++)
        {
            town = getTowns().get(i);
            if (town != null && town.checkIfInZone(x, y)) return i;
        }
        return -1;
    }

    public final int getTownIndex(String name)
    {
        Town town;
        for (int i = 0; i < getTowns().size(); i++)
        {
            town = getTowns().get(i);
            if (town != null && town.getName().equalsIgnoreCase(name.trim())) return i;
        }
        return -1;
    }

    public final List<Town> getTowns()
    {
        if (_Towns == null) _Towns = new FastList<Town>();
        return _Towns;
    }
}
