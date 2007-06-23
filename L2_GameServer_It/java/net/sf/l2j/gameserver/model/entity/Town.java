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

import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.L2Object;

public class Town
{
	// =========================================================
    // Data Field
    private int _castleIndex                    = 0;    // This is the index of the castle controling over this town
    private String _name                        = "";
    private int _redirectToTownId               = 0;    // This is the id of the town to redirect players to
    //private double _TaxRate                     = 0;    // This is the town's local tax rate used by merchant
    private int _townId                         = 0;
    private List<int[]> _spawn;
    private Zone _zone;

	// =========================================================
	// Constructor
	public Town(int townId)
	{
		_townId = townId;
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
        // TEMP UNTIL TOWN'S TABLE IS ADDED
        Zone zone = ZoneManager.getInstance().getZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Town), getTownId());
        if (zone != null)
        {
            _castleIndex    = CastleManager.getInstance().getCastleIndex(zone.getTaxById());
            _name           = zone.getName();
        }
        
        switch (getTownId())
        {
            case 7: _redirectToTownId = 5;break;      // Gludio => Gludin
            case 8: _redirectToTownId = 7;break;      // Dion => Gludio
            case 9: _redirectToTownId = 11;break;      // Giran => HV (should be Giran Harbor, but its not a zone town "yet")
            case 10: _redirectToTownId = 11;break;    // Oren => HV
            case 12: _redirectToTownId = 10;break;    // Aden => Oren
            case 13: _redirectToTownId = 14;break;    // Goddard => Rune
            case 14: _redirectToTownId = 13;break;    // Rune => Goddard
            case 15: _redirectToTownId = 16;break;      // Heine => Floran (should be Giran Harbor, but its not a zone town "yet")
            case 17: _redirectToTownId = 14;break;    // Shuttgart => Rune
            default: _redirectToTownId = 9;break; // Have to use another town here, else we cause a stack overflow :D 
       }
    }
	
	// =========================================================
	// Proeprty
    public final Castle getCastle()
    {
        if (_castleIndex >= 0) return CastleManager.getInstance().getCastles().get(_castleIndex);
        return null;
    }

    public final String getName() { return _name; }

    public final List<int[]> getSpawn()
    {
        // If a redirect to town id is avail, town belongs to a castle, and castle is under siege then redirect
        //if (_redirectToTownId != getTownId() && getCastle() != null && getCastle().getSiege().getIsInProgress()) return TownManager.getInstance().getTown(_redirectToTownId).getSpawn();
       // if (_redirectToTownId != getTownId() && getCastle() != null && getCastle().getSiege().getIsInProgress())
    	if(TownManager.getInstance().townHasCastleInSeige(getTownId()))
        	return TownManager.getInstance().getTown(_redirectToTownId).getSpawn();

        if (_spawn == null) _spawn = ZoneManager.getInstance().getZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.TownSpawn), getName()).getCoords();
        return _spawn;
    }

    public final int getRedirectToTownId() { return _redirectToTownId; }

    public final int getTownId() { return _townId; }

    public final Zone getZone()
    {
        if (_zone == null) _zone = ZoneManager.getInstance().getZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Town), getName());
        return _zone;
    }
}