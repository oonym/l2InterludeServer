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

import java.util.Map;
import java.util.Vector;

import javolution.util.FastMap;

import net.sf.l2j.util.Rnd;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class TvTEventTeam
{
	private String _name;
	private int[] _coordinates = new int[3];
	private short _points;
	private Map<String, L2PcInstance> _participatedPlayersInstances = new FastMap<String, L2PcInstance>();
	private Map<String, Long> _participatedPlayersLastActivities = new FastMap<String, Long>();
	private Vector<String> _participatedPlayerNames = new Vector<String>();

	public TvTEventTeam(String name, int[] coordinates)
	{
		_name = name;
		_coordinates = coordinates;
		_points = 0;
	}

	public boolean addPlayer(L2PcInstance playerInstance)
	{
		if (playerInstance == null)
			return false;

		synchronized (_participatedPlayersInstances)
		{
			String playerName = playerInstance.getName();

			_participatedPlayersInstances.put(playerName, playerInstance);
			_participatedPlayersLastActivities.put(playerName, (long)0);
			
			if (!_participatedPlayerNames.contains(playerName))
				_participatedPlayerNames.add(playerName);
		}

		return true;
	}

	public void removePlayer(String playerName)
	{
		synchronized (_participatedPlayersInstances)
		{
			_participatedPlayersInstances.remove(playerName);
			_participatedPlayersLastActivities.remove(playerName);
			_participatedPlayerNames.remove(playerName);
		}
	}

	public void increasePoints()
	{
		_points++;
	}
	
	public void cleanMe()
	{
		_participatedPlayersInstances.clear();
		_participatedPlayersLastActivities.clear();
		_participatedPlayerNames.clear();
		_participatedPlayersInstances = new FastMap<String, L2PcInstance>();
		_participatedPlayersLastActivities = new FastMap<String, Long>();
		_participatedPlayerNames = new Vector<String>();
		_points = 0;
	}

	public boolean containsPlayer(String playerName)
	{
		boolean containsPlayer;

		synchronized (_participatedPlayersInstances)
		{
			containsPlayer = _participatedPlayerNames.contains(playerName);
		}

		return containsPlayer;
	}
	
	public void updatePlayerLastActivity(String playerName)
	{
		synchronized (_participatedPlayersLastActivities)
		{
			_participatedPlayersLastActivities.put(playerName, System.currentTimeMillis());
		}
	}

	public String getName()
	{
		return _name;
	}

	public int[] getCoordinates()
	{
		return _coordinates;
	}

	public short getPoints()
	{
		return _points;
	}

	public Map<String, L2PcInstance> getParticipatedPlayerInstances()
	{
		Map<String, L2PcInstance> participatedPlayers = null;

		synchronized (_participatedPlayersInstances)
		{
			participatedPlayers = _participatedPlayersInstances;
		}

		return participatedPlayers;
	}

	public Map<String, Long> getParticipatedPlayersLastActivities()
	{
		Map<String, Long> participatedPlayersLastActivities = null;

		synchronized (_participatedPlayersInstances)
		{
			participatedPlayersLastActivities = _participatedPlayersLastActivities;
		}

		return participatedPlayersLastActivities;
	}
	
	public Vector<String> getParticipatedPlayerNames()
	{
		Vector<String> participatedPlayerNames = null;

		synchronized (_participatedPlayersInstances)
		{
			participatedPlayerNames = _participatedPlayerNames;
		}

		return participatedPlayerNames;
	}

	public int getParticipatedPlayerCount()
	{
		int participatedPlayerCount;

		synchronized (_participatedPlayersInstances)
		{
			participatedPlayerCount = _participatedPlayersInstances.size();
		}

		return participatedPlayerCount;
	}

	public L2PcInstance getPlayerInstance(String playerName)
	{
		L2PcInstance playerInstance = null;
		
		synchronized (_participatedPlayersInstances)
		{
			playerInstance = _participatedPlayersInstances.get(playerName);
		}
		
		return playerInstance;
	}

	public long getPlayerLastActivity(String playerName)
	{
		long lastActivity = 0;
		
		synchronized (_participatedPlayersInstances)
		{
			lastActivity = _participatedPlayersLastActivities.get(playerName);
		}
		
		return lastActivity;
	}

	public L2PcInstance getRandomPlayerInstance()
	{
		String playerName = null;

		synchronized (_participatedPlayersInstances)
		{
			 playerName = _participatedPlayerNames.get(Rnd.get(_participatedPlayerNames.size()));
		}

		return getPlayerInstance(playerName);
	}
}
