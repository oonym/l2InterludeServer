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
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.entity.OlympiadStadia;
import net.sf.l2j.gameserver.model.entity.Zone;
import net.sf.l2j.gameserver.model.entity.ZoneType;

public class OlympiadStadiaManager
{
    protected static Logger _log = Logger.getLogger(OlympiadStadiaManager.class.getName());

    // =========================================================
    private static OlympiadStadiaManager _instance;
    public static final OlympiadStadiaManager getInstance()
    {
        if (_instance == null)
        {
            System.out.println("Initializing OlympiadStadiaManager");
            _instance = new OlympiadStadiaManager();
            _instance.load();
        }
        return _instance;
    }
    // =========================================================

    
    // =========================================================
    // Data Field
    private List<OlympiadStadia> _olympiadStadias;
    
    // =========================================================
    // Constructor
    public OlympiadStadiaManager()
    {
    }

    // =========================================================
    // Method - Public
    /** Return true if object is inside zone */
    public final boolean checkIfInZone(L2Object obj) { return (getOlympiadStadia(obj) != null); }

    /** Return true if object is inside zone */
    public final boolean checkIfInZone(int x, int y) { return (getOlympiadStadia(x, y) != null); }

    public void reload()
    {
        this.getOlympiadStadias().clear();
        this.load();
    }

    // =========================================================
    // Method - Private
    private final void load()
    {
        for (Zone zone: ZoneManager.getInstance().getZones(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.OlympiadStadia)))
            getOlympiadStadias().add(new OlympiadStadia(zone.getId()));
    }

    // =========================================================
    // Property - Public
    public final OlympiadStadia getOlympiadStadia(int olympiadStadiaId)
    {
        int index = getOlympiadStadiaIndex(olympiadStadiaId);
        if (index >= 0) return getOlympiadStadias().get(index);
        return null;
    }

    public final OlympiadStadia getOlympiadStadia(L2Object activeObject) { return getOlympiadStadia(activeObject.getPosition().getX(), activeObject.getPosition().getY()); }

    public final OlympiadStadia getOlympiadStadia(int x, int y)
    {
        int index = getOlympiadStadiaIndex(x, y);
        if (index >= 0) return getOlympiadStadias().get(index);
        return null;
    }

    public final OlympiadStadia getOlympiadStadia(String name)
    {
        int index = getOlympiadStadiaIndex(name);
        if (index >= 0) return getOlympiadStadias().get(index);
        return null;
    }

    public final int getOlympiadStadiaIndex(int olympiadStadiaId)
    {
        OlympiadStadia OlympiadStadia;
        for (int i = 0; i < getOlympiadStadias().size(); i++)
        {
            OlympiadStadia = getOlympiadStadias().get(i);
            if (OlympiadStadia != null && OlympiadStadia.getOlympiadStadiaId() == olympiadStadiaId) return i;
        }
        return -1;
    }

    public final int getOlympiadStadiaIndex(L2Object activeObject) { return getOlympiadStadiaIndex(activeObject.getPosition().getX(), activeObject.getPosition().getY()); }

    public final int getOlympiadStadiaIndex(int x, int y)
    {
        OlympiadStadia OlympiadStadia;
        for (int i = 0; i < getOlympiadStadias().size(); i++)
        {
            OlympiadStadia = getOlympiadStadias().get(i);
            if (OlympiadStadia != null && OlympiadStadia.checkIfInZone(x, y)) return i;
        }
        return -1;
    }

    public final int getOlympiadStadiaIndex(String name)
    {
        OlympiadStadia OlympiadStadia;
        for (int i = 0; i < getOlympiadStadias().size(); i++)
        {
            OlympiadStadia = getOlympiadStadias().get(i);
            if (OlympiadStadia != null && OlympiadStadia.getName().equalsIgnoreCase(name.trim())) return i;
        }
        return -1;
    }

    public final List<OlympiadStadia> getOlympiadStadias()
    {
        if (_olympiadStadias == null) _olympiadStadias = new FastList<OlympiadStadia>();
        return _olympiadStadias;
    }
}
