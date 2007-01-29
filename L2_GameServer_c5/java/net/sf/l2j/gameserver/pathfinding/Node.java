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

/**
 *
 * @author -Nemesiss-
 */
public class Node
{	
	private final AbstractNodeLoc _Loc;	
	private final short _Neighbors_idx;
	private Node[] _Neighbors;
	private Node _Parent;
	
	
	public Node(AbstractNodeLoc Loc, short Neighbors_idx)
	{
		_Loc = Loc;
		_Neighbors_idx = Neighbors_idx;
	}

	public void setParent(Node p)
	{
		_Parent = p;
	}
	
	public void attacheNeighbors()
	{
		_Neighbors = PathFinding.getInstance().ReadNeighbors(this, _Neighbors_idx);
	}

	public Node[] getNighbors()
	{
		return _Neighbors;
	}

	public Node getParent()
	{
		return _Parent;
	}

	public AbstractNodeLoc getLoc()
	{
		return _Loc;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0)
	{
		if(!(arg0 instanceof Node))
			return false;
		Node n = (Node)arg0;
		//Check if x,y,z are the same
		return _Loc.getX() == n.getLoc().getX() && _Loc.getY() == n.getLoc().getY()
		&& _Loc.getZ() == n.getLoc().getZ();
	}
}
