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
package net.sf.l2j.gameserver.pathfinding.worldnodes;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;
import net.sf.l2j.gameserver.pathfinding.AbstractNodeLoc;
import net.sf.l2j.gameserver.pathfinding.Node;
import net.sf.l2j.gameserver.pathfinding.PathFinding;

/**
 *
 * @author -Nemesiss-
 */
public class WorldPathFinding extends PathFinding
{
	//private static Logger _log = Logger.getLogger(WorldPathFinding.class.getName());
	private static WorldPathFinding _instance;
    @SuppressWarnings("unused")
	private static Map<Short, ByteBuffer> _pathNodes = new FastMap<Short, ByteBuffer>();
	private static Map<Short, IntBuffer> _pathNodesIndex = new FastMap<Short, IntBuffer>();

	public static WorldPathFinding getInstance()
	{
		if (_instance == null)
			_instance = new WorldPathFinding();
		return _instance;
	}

	/**
	 * @see net.sf.l2j.gameserver.pathfinding.PathFinding#PathNodesExist(short)
	 */
	@Override
	public boolean pathNodesExist(short regionoffset)
	{
		return _pathNodesIndex.containsKey(regionoffset);
	}

    //TODO! [Nemesiss]
	/**
	 * @see net.sf.l2j.gameserver.pathfinding.PathFinding#FindPath(int, int, short, int, int, short)
	 */
	@Override
	public List<AbstractNodeLoc> findPath(int gx, int gy, short z, int gtx,	int gtz, short tz)
	{
		return null;
	}

	/**
	 * @see net.sf.l2j.gameserver.pathfinding.PathFinding#ReadNeighbors(short, short)
	 */
	@Override
	public Node[] readNeighbors(short node_x,short node_y, int idx)
	{
		// TODO Auto-generated method stub
		return null;
	}

	//Private

	private WorldPathFinding()
	{
		//TODO! {Nemesiss] Load PathNodes.
	}
}
