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
package net.sf.l2j.gameserver.model.entity;

import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.util.Util;

public class Zone
{
    protected static Logger _log = Logger.getLogger(Zone.class.getName());

    // =========================================================
    // Data Field
    private List<int[]> _coords;
    private int _id;
    private String _name;
    private int _taxById;

    // =========================================================
    // Constructor
    public Zone(int id, String zoneName, int taxById)
    {
        _name = zoneName.trim();
        _taxById = taxById;
    }

    // =========================================================
    // Method - Public
    public final int addCoord(int x1, int y1, int x2, int y2, int z)
    {
        getCoords().add(new int[] {x1, y1, x2, y2, z});
        return getCoords().size() - 1;
    }

    public final boolean checkIfInZone(L2Object obj)
    {
        return checkIfInZone(obj.getPosition().getX(), obj.getPosition().getY());
    }

    public final boolean checkIfInZone(int x, int y)
    {
        return (getCoord(x, y) != null);
    }

    public final double findDistanceToZone(L2Object obj, boolean includeZAxis)
    {
        return findDistanceToZone(obj.getPosition().getX(), obj.getPosition().getY(),
                                  obj.getPosition().getZ(), includeZAxis);
    }

    public final double findDistanceToZone(int x, int y, int z, boolean includeZAxis)
    {
        double closestDistance = 99999999;
        double distance;
        for (int[] coord : getCoords())
        {
            distance = Util.calculateDistance(x, y, z, Math.abs(coord[0] + coord[2]), Math.abs(coord[1]
                + coord[3]), coord[4], includeZAxis);
            if (distance < closestDistance) closestDistance = distance;
        }
        return closestDistance;
    }

    public final Location getRandomLocation()
    {
        int x, y, x1, x2, y1, y2, z;
        Random rnd = new Random();

        if (getCoords().isEmpty()) return null;

        int[] coords = getCoords().get(0);

        x1 = coords[0];
        y1 = coords[1];
        x2 = coords[2];
        y2 = coords[3];
        z = coords[4];

        x = x1 + rnd.nextInt(x2 - x1);
        y = y1 + rnd.nextInt(y2 - y1);

        return new Location(x, y, z);
    }

    public final void setId(int id)
    {
        _id = id;
    }

    public final void setTaxById(int taxById)
    {
        _taxById = taxById;
    }

    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public final int[] getCoord(L2Object obj)
    {
        return getCoord(obj.getX(), obj.getY());
    }

    public final int[] getCoord(int x, int y)
    {
        for (int[] coord : getCoords())
        {
            if (coord[2] == 0 && coord[3] == 0) continue;
            if (x >= Math.min(coord[0], coord[2]) && x <= Math.max(coord[0], coord[2])
                && y >= Math.min(coord[1], coord[3]) && y <= Math.max(coord[1], coord[3])) return coord;
        }
        return null;
    }

    public final List<int[]> getCoords()
    {
        if (_coords == null) _coords = new FastList<int[]>();
        return _coords;
    }

    public final int getId()
    {
        return _id;
    }

    public final String getName()
    {
        return _name;
    }

    public final int getTaxById()
    {
        return _taxById;
    }
}