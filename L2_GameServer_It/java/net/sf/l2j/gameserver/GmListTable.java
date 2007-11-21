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
package net.sf.l2j.gameserver;

import javolution.util.FastList;
import javolution.util.FastMap;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class stores references to all online game masters. (access level > 100)
 * 
 * @version $Revision: 1.2.2.1.2.7 $ $Date: 2005/04/05 19:41:24 $
 */
public class GmListTable
{
	private static Logger _log = Logger.getLogger(GmListTable.class.getName());
	private static GmListTable _instance;
	
	
	/** Set(L2PcInstance>) containing all the GM in game */
	private FastMap<L2PcInstance, Boolean> _gmList;
	
	public static GmListTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new GmListTable();
		}
		return _instance;
	}
	
	public FastList<L2PcInstance> getAllGms(boolean includeHidden)
	{
		FastList<L2PcInstance> tmpGmList = new FastList<L2PcInstance>();
		
		for (FastMap.Entry<L2PcInstance, Boolean> n = _gmList.head(), end = _gmList.tail(); (n = n.getNext())!=end;)
			if (includeHidden || !n.getValue())
				tmpGmList.add(n.getKey());
		
		return tmpGmList;
	}
	
	public FastList<String> getAllGmNames(boolean includeHidden)
	{
		FastList<String> tmpGmList = new FastList<String>();
		
		for (FastMap.Entry<L2PcInstance, Boolean> n = _gmList.head(), end = _gmList.tail(); (n = n.getNext())!=end;)
			if (!n.getValue())
				tmpGmList.add(n.getKey().getName());
			else if (includeHidden)
				tmpGmList.add(n.getKey().getName()+" (invis)");
		
		return tmpGmList;
	}
	
	private GmListTable()
	{
		_gmList = new FastMap<L2PcInstance,Boolean>().setShared(true);
	}
	
	/**
	 * Add a L2PcInstance player to the Set _gmList
	 */
	public void addGm(L2PcInstance player, boolean hidden)
	{
		if (Config.DEBUG) _log.fine("added gm: "+player.getName());
		_gmList.put(player,hidden);
	}
	
	public void deleteGm(L2PcInstance player)
	{
		if (Config.DEBUG) _log.fine("deleted gm: "+player.getName());
		
		_gmList.remove(player);
	}
	
	/**
	 * GM will be displayed on clients gmlist
	 * @param player
	 */
	public void showGm(L2PcInstance player)
	{
		FastMap.Entry<L2PcInstance, Boolean> gm = _gmList.getEntry(player);
		if (gm != null) gm.setValue(false);
	}
	
	/**
	 * GM will no longer be displayed on clients gmlist
	 * @param player
	 */
	public void hideGm(L2PcInstance player)
	{
		FastMap.Entry<L2PcInstance, Boolean> gm = _gmList.getEntry(player);
		if (gm != null) gm.setValue(true);
	}
	
	public boolean isGmOnline(boolean includeHidden)
	{
		for (FastMap.Entry<L2PcInstance, Boolean> n = _gmList.head(), end = _gmList.tail(); (n = n.getNext())!=end;)
		{
			if (includeHidden || !n.getValue())
				return true;
		}
		
		return false;
	}
	
	public void sendListToPlayer (L2PcInstance player)
	{
		if (!isGmOnline(player.isGM()))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW); //There are not any GMs that are providing customer service currently.
			player.sendPacket(sm);
		} else
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.GM_LIST);
			player.sendPacket(sm);
			
            for (String name : getAllGmNames(player.isGM()))
            {
				sm = new SystemMessage(SystemMessageId.GM_S1);
				sm.addString(name);
				player.sendPacket(sm);
			}
		}
	}
	
	public static void broadcastToGMs(L2GameServerPacket packet)
	{
		for (L2PcInstance gm : getInstance().getAllGms(true))
		{
			gm.sendPacket(packet);
		}
	}
	
	public static void broadcastMessageToGMs(String message)
	{
		for (L2PcInstance gm : getInstance().getAllGms(true))
		{
			gm.sendPacket(SystemMessage.sendString(message));
		}
	}
}