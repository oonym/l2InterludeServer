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
package net.sf.l2j.gameserver.pathfinding.geonodes;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.pathfinding.AbstractNodeLoc;
import net.sf.l2j.gameserver.pathfinding.PathFinding;

/**
 *
 * @author -Nemesiss-
 */
public class GeoPathFinding extends PathFinding
{
	private static Logger _log = Logger.getLogger(GeoPathFinding.class.getName());
	private static GeoPathFinding _instance;
	
	public static GeoPathFinding getInstance()
	{
		if (_instance == null)
			_instance = new GeoPathFinding();
		return _instance;
	}
	
	//TODO! [Nemesiss]
	/**
	 * @see net.sf.l2j.gameserver.pathfinding.PathFinding#PathNodesExist(short)
	 */
	@Override
	public boolean PathNodesExist(short regionoffset)
	{
		return false;
	}
	
	//	TODO! [Nemesiss]
	/**
	 * @see net.sf.l2j.gameserver.pathfinding.PathFinding#FindPath(int, int, short, int, int, short)
	 */
	@Override
	public AbstractNodeLoc[] FindPath(int gx, int gy, short z, int gtx, int gtz, short tz)
	{
		return null;
	}
	
	//Private
	
	private GeoPathFinding()
	{
		//TODO! {Nemesiss] Load PathNodes.
	}
}
