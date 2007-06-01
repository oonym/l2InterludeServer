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
    private int _CastleIndex                    = 0;    // This is the index of the castle controling over this town
    private String _Name                        = "";
    private int _RedirectToTownId               = 0;    // This is the id of the town to redirect players to
    //private double _TaxRate                     = 0;    // This is the town's local tax rate used by merchant
    private int _TownId                         = 0;
    private List<int[]> _Spawn;
    private Zone _Zone;

	// =========================================================
	// Constructor
	public Town(int townId)
	{
		_TownId = townId;
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
            _CastleIndex    = CastleManager.getInstance().getCastleIndex(zone.getTaxById());
            _Name           = zone.getName();
        }
        
        switch (getTownId())
        {
            case 7: _RedirectToTownId = 5;break;      // Gludio => Gludin
            case 8: _RedirectToTownId = 7;break;      // Dion => Gludio
            case 9: _RedirectToTownId = 11;break;      // Giran => HV (should be Giran Harbor, but its not a zone town "yet")
            case 10: _RedirectToTownId = 11;break;    // Oren => HV
            case 12: _RedirectToTownId = 10;break;    // Aden => Oren
            case 13: _RedirectToTownId = 14;break;    // Goddard => Rune
            case 14: _RedirectToTownId = 13;break;    // Rune => Goddard
            case 15: _RedirectToTownId = 16;break;      // Heine => Floran (should be Giran Harbor, but its not a zone town "yet")
            case 17: _RedirectToTownId = 14;break;    // Shuttgart => Rune
            default: _RedirectToTownId = 9;break; // Have to use another town here, else we cause a stack overflow :D 
       }
    }
	
	// =========================================================
	// Proeprty
    public final Castle getCastle()
    {
        if (_CastleIndex >= 0) return CastleManager.getInstance().getCastles().get(_CastleIndex);
        return null;
    }

    public final String getName() { return _Name; }

    public final List<int[]> getSpawn()
    {
        // If a redirect to town id is avail, town belongs to a castle, and castle is under siege then redirect
        //if (_RedirectToTownId != getTownId() && getCastle() != null && getCastle().getSiege().getIsInProgress()) return TownManager.getInstance().getTown(_RedirectToTownId).getSpawn();
       // if (_RedirectToTownId != getTownId() && getCastle() != null && getCastle().getSiege().getIsInProgress())
    	if(TownManager.getInstance().townHasCastleInSeige(getTownId()))
        	return TownManager.getInstance().getTown(_RedirectToTownId).getSpawn();

        if (_Spawn == null) _Spawn = ZoneManager.getInstance().getZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.TownSpawn), getName()).getCoords();
        return _Spawn;
    }

    public final int getRedirectToTownId() { return _RedirectToTownId; }

    public final int getTownId() { return _TownId; }

    public final Zone getZone()
    {
        if (_Zone == null) _Zone = ZoneManager.getInstance().getZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Town), getName());
        return _Zone;
    }
}