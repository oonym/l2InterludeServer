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
package net.sf.l2j.gameserver.model.entity;

import java.util.List;

import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;

public class Arena
{
	// =========================================================
    // Data Field
	private int _arenaId                       = 0;
    private String _name                       = "";
    private List<int[]> _spawn;
    private Zone _zone;

	// =========================================================
	// Constructor
	public Arena(int arenaId)
	{
		_arenaId = arenaId;
        loadData();
	}

	// =========================================================
	// Method - Public
    /** Return true if object is inside the zone */
    public boolean checkIfInZone(L2Object obj) { return checkIfInZone(obj.getX(), obj.getY()); }

    /** Return true if object is inside the zone */
    public boolean checkIfInZone(int x, int y) { return getZone().checkIfInZone(x, y); }
    
	// =========================================================
	// Method - Private
    private void loadData()
    {
        // TEMP UNTIL ARENA'S TABLE IS ADDED
        Zone zone = ZoneManager.getInstance().getZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Arena), getArenaId());
        if (zone != null) _name = zone.getName();
    }
	
	// =========================================================
	// Proeprty
	public final int getArenaId() { return _arenaId; }

    public final String getName() { return _name; }

    public final List<int[]> getSpawn()
    {
        if (_spawn == null) _spawn = ZoneManager.getInstance().getZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.ArenaSpawn), getName()).getCoords();
        return _spawn;
    }
    
    public final Location getSpawnLoc()
    {
    	int[] spawnLoc = getSpawn().get(0);
    	return new Location(spawnLoc[0], spawnLoc[1], spawnLoc[2]);
    }

    public final Zone getZone()
    {
        if (_zone == null) _zone = ZoneManager.getInstance().getZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Arena), getName());
        return _zone;
    }
}