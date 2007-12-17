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
package net.sf.l2j.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2NpcWalkerNode;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Main Table to Load Npc Walkers Routes and Chat SQL Table.<br>
 * 
 * @author Rayan RPG for L2Emu Project
 * 
 * @since 927 
 *
 */
public class NpcWalkerRoutesTable 
{
	private final static Log _log = LogFactory.getLog(SpawnTable.class.getName());

	private static NpcWalkerRoutesTable  _instance;

	private FastList<L2NpcWalkerNode> _routes;

	public static NpcWalkerRoutesTable getInstance()
	{
		if(_instance == null)
		{
				_instance = new NpcWalkerRoutesTable();
				_log.info("Initializing Walkers Routes Table.");
		}
		
		return _instance;
	}

	private NpcWalkerRoutesTable()
	{
	}
	//FIXME: NPE while loading. :S
	public void load()
	{
		 _routes = new FastList<L2NpcWalkerNode>();
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT route_id, npc_id, move_point, chatText, move_x, move_y, move_z, delay, running FROM walker_routes");
			ResultSet rset = statement.executeQuery();
			L2NpcWalkerNode  route;
			while (rset.next())
			{
				route = new L2NpcWalkerNode();
				route.setRouteId(rset.getInt("route_id"));
				route.setNpcId(rset.getInt("npc_id"));
				route.setMovePoint(rset.getString("move_point"));
				route.setChatText(rset.getString("chatText"));
				
				route.setMoveX(rset.getInt("move_x"));
				route.setMoveY(rset.getInt("move_y"));
				route.setMoveZ(rset.getInt("move_z"));
				route.setDelay(rset.getInt("delay"));
				route.setRunning(rset.getBoolean("running"));

			
				_routes.add(route);
			}

			rset.close();
			statement.close();

			_log.info("WalkerRoutesTable: Loaded "+_routes.size()+" Npc Walker Routes.");
			rset.close();
			statement.close();
		}
		catch (Exception e) 
		{
			_log.fatal("WalkerRoutesTable: Error while loading Npc Walkers Routes: "+e.getMessage());
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e) {}
		}
	}
	
	public FastList<L2NpcWalkerNode> getRouteForNpc(int id)
	{
		FastList<L2NpcWalkerNode> _return = new FastList<L2NpcWalkerNode>();
		
		 for (FastList.Node<L2NpcWalkerNode> n = _routes.head(), end = _routes.tail(); (n = n.getNext()) != end;) {
	         if(n.getValue().getNpcId() == id)
	         {
	        	 _return.add(n.getValue());
	         }
	     }
		return _return;
		
		
	}
}
