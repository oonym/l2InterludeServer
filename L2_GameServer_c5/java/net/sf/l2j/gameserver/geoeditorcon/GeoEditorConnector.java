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
package net.sf.l2j.gameserver.geoeditorcon;

import java.util.*;

import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * 
 *
 * @author  Luno
 */
public class GeoEditorConnector
{	
	private static GeoEditorConnector _instance = new GeoEditorConnector();
	public static GeoEditorConnector getInstance()
	{
		return _instance;
	}
	
	private GeoEditorThread _geThread;
	
	private List<L2PcInstance> _gmList = new ArrayList<L2PcInstance>();
	
	int RegionX;
	int RegionY;
	
	private GeoEditorConnector()
	{
		
	}
	public void connect(L2PcInstance gm, int ticks)
	{
		if(_geThread != null)
		{
			gm.sendMessage("GameServer is already connected to GeoEditor.");
			if(!_gmList.contains(gm))
				join(gm);

			return;
		}
		RegionX = getRegionX(gm);
		RegionY = getRegionY(gm);
		
		_gmList.add(gm);
		
		_geThread = new GeoEditorThread(this);
		_geThread.setTicks(ticks);
		_geThread.start();
	}
	public void leave(L2PcInstance gm)
	{
		_gmList.remove(gm);
		gm.sendMessage("You have been removed from the list");
		if(_gmList.isEmpty())
		{
			_geThread.stopRecording();
			_geThread = null;
			gm.sendMessage("Connection closed.");
		}
	}
	public void join(L2PcInstance gm)
	{
		if(_geThread == null)
		{
			gm.sendMessage("GameServer is not connected to GeoEditor.");
			gm.sendMessage("Use //geoeditor connect <ticks>  first.");
			return;
		}
		if(_gmList.contains(gm))
		{
			gm.sendMessage("You are already on the list.");
			return;
		}
		if(getRegionX(gm) != RegionX || getRegionY(gm) != RegionY)
		{
			gm.sendMessage("Only people from region: ["+RegionX+","+RegionY+"] can join.");
			return;
		}
		_gmList.add(gm);
		gm.sendMessage("You have been added to the list.");
	}
	public List<L2PcInstance> getGMs()
	{
		return _gmList;
	}
	public void sendMessage(String msg)
	{
		for(L2PcInstance gm: _gmList)
			gm.sendMessage(msg);
	}
	public void stoppedConnection()
	{
		_geThread = null;
		_gmList.clear();
	}
	private int getRegionX(L2PcInstance g)
	{
		int gx = (g.getX() - L2World.MAP_MIN_X) >> 4;
		gx >>= 11;
		return gx + 16;
	}
	private int getRegionY(L2PcInstance g)
	{
		int gy = (g.getY() - L2World.MAP_MIN_Y) >> 4;
		gy >>= 11;
		return gy + 10;
	}
}