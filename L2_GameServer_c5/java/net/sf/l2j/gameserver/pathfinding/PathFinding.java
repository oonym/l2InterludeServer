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
package net.sf.l2j.gameserver.pathfinding;

import java.util.LinkedList;
import java.util.List;

import net.sf.l2j.gameserver.model.L2World;

/**
 *
 * @author -Nemesiss-
 */
public abstract class PathFinding
{
	private static PathFinding _instance;
	
	public static PathFinding getInstance()
	{
		if (_instance == null)
		{
			if (true /*Config.GEODATA_PATHFINDING*/)				
			{
				//Smaler Memory Usage, Higher Cpu Usage (CalculatedOnTheFly)				
			}
			else // WORLD_PATHFINDING				
			{
				//Higher Memoru Usage, Lower Cpu Usage (PreCalculated)				
			}			
		}
		return _instance;
	}
	public abstract boolean PathNodesExist(short regionoffset);	
	public abstract List<AbstractNodeLoc> FindPath(int gx, int gy, short z, int gtx, int gtz, short tz);
	public abstract Node[] ReadNeighbors(short node_x,short node_y, int idx);
	
	public List<AbstractNodeLoc> search(Node start, Node end)
	{
		//List of Visited Nodes
		LinkedList<Node> visited = new LinkedList<Node>();		

		// List of Nodes to Visit
		LinkedList<Node> to_visit = new LinkedList<Node>();
		to_visit.add(start);
		
		int i = 0;
		while (i < 800)//TODO! Add limit to cfg
		{
			Node node = to_visit.removeFirst();
			if (node.equals(end)) //path found!
				return constructPath(node);
			else
			{
				i++;
				visited.add(node);
				node.attacheNeighbors();
				for (Node n : node.getNighbors())
				{
					if (!visited.contains(n) && !to_visit.contains(n))
					{
						n.setParent(node);
						to_visit.add(n);
					}
				}
			}
		}
		//No Path found
		return null;
	}
	
	public List<AbstractNodeLoc> constructPath(Node node)
	{
		LinkedList<AbstractNodeLoc> path = new LinkedList<AbstractNodeLoc>();
		while (node.getParent() != null)
		{
			path.addFirst(node.getLoc());
			node = node.getParent();
		}
		return path;
	}
	
	/**
	 * Convert geodata position to pathnode position
	 * @param geo_pos
	 * @return pathnode position
	 */
	public short getNodePos(int geo_pos)
	{
		return (short)(geo_pos >> 3); //OK?
	}
	
	/**
	 * Convert node position to pathnode block position
	 * @param geo_pos
	 * @return pathnode block position (0...255)
	 */
	public short getNodeBlock(int node_pos)
	{
		return (short)(node_pos % 256);
	}
	
	public byte getRegionX(int node_pos)
	{
		return (byte)((node_pos >> 8) + 16);
	}
	
	public byte getRegionY(int node_pos)
	{
		return (byte)((node_pos >> 8) + 10);	    
	}
	
	public short getRegionOffset(byte rx, byte ry)
	{
		return (short)((rx << 5) + ry);
	}
	
	/**
	 * Convert pathnode x to World x position
	 * @param node_x, rx
	 * @return
	 */
	public int calculateWorldX(short node_x)
	{
		return   L2World.MAP_MIN_X  + node_x * 128 + 48 ;
	}
	
	/**
	 * Convert pathnode y to World y position
	 * @param node_y
	 * @return
	 */
	public int CalculateWorldY(short node_y)
	{
		return  L2World.MAP_MIN_Y + node_y * 128 + 48 ;
	}
}
