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
	public abstract AbstractNodeLoc[] FindPath(int gx, int gy, short z, int gtx, int gtz, short tz);
	
	public List<AbstractNodeLoc> search(Node start, Node end)
	{
		//List of Visited Nodes
		LinkedList<Node> visited = new LinkedList<Node>();		

		// List of Nodes to Visit
		LinkedList<Node> to_visit = new LinkedList<Node>();
		to_visit.add(start);
		
		//TODO! [Nemesiss] here must be some limit, else it could check all World
		while(!to_visit.isEmpty())
		{
			Node node = to_visit.removeFirst();
			if (node == end) //path found!
				return ConstructPath(node);
			else
			{
				visited.add(node);
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
	
	public List<AbstractNodeLoc> ConstructPath(Node node)
	{
		LinkedList<AbstractNodeLoc> path = new LinkedList<AbstractNodeLoc>();
		while (node.getParent() != null)
		{
			path.addFirst(node.getLoc());
			node = node.getParent();
		}
		return path;
	}
}
