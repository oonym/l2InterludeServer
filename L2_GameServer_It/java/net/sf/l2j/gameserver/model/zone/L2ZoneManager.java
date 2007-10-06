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
package net.sf.l2j.gameserver.model.zone;

import javolution.util.FastList;

import net.sf.l2j.gameserver.model.L2Character;

/**
 * This class manages all zones for a given world region
 *
 * @author  durgus
 */
public class L2ZoneManager
{
	private FastList<L2ZoneType> _zones;
	
	/**
	 * The Constructor creates an initial zone list
	 * use registerNewZone() / unregisterZone() to
	 * change the zone list
	 *
	 */
	public L2ZoneManager()
	{
		_zones = new FastList<L2ZoneType>();
	}
	
	/**
	 * Register a new zone object into the manager
	 * @param zone
	 */
	public void registerNewZone(L2ZoneType zone)
	{
		_zones.add(zone);
	}
	
	/**
	 * Unregister a given zone from the manager (e.g. dynamic zones)
	 * @param zone
	 */
	public void unregisterZone(L2ZoneType zone)
	{
		_zones.remove(zone);
	}
	
	public void revalidateZones(L2Character character)
	{
		for (FastList.Node<L2ZoneType> e = _zones.head(), end = _zones.tail(); (e = e.getNext()) != end;)
		{
			e.getValue().revalidateInZone(character);
		}
	}
	
	public void removeCharacter(L2Character character)
	{
		for (FastList.Node<L2ZoneType> e = _zones.head(), end = _zones.tail(); (e = e.getNext()) != end;)
		{
			e.getValue().removeCharacter(character);
		}
	}
}
