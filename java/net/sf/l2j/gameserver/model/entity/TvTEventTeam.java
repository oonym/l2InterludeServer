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
 * [URL]http://www.gnu.org/copyleft/gpl.html[/URL]
 */
package net.sf.l2j.gameserver.model.entity;

import java.util.Map;
import java.util.Vector;

import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author FBIagent
 */
public class TvTEventTeam
{
	/** The name of the team<br> */
	private String _name;
	/** The team spot coordinated<br> */
	private int[] _coordinates = new int[3];
	/** The points of the team<br> */
	private short _points;
	/** Name and instance of all participated players in FastMap<br> */
	private Map<String, L2PcInstance> _participatedPlayers = new FastMap<String, L2PcInstance>();
	/** Name of all participated players in Vector<br> */
	private Vector<String> _participatedPlayerNames = new Vector<String>();

	/**
	 * C'tor initialize the team<br><br>
	 *
	 * @param name<br>
	 * @param coordinates<br>
	 */
	public TvTEventTeam(String name, int[] coordinates)
	{
		_name = name;
		_coordinates = coordinates;
		_points = 0;
	}

	/**
	 * Adds a player to the team<br><br>
	 *
	 * @param playerInstance<br>
	 * @return boolean<br>
	 */
	public boolean addPlayer(L2PcInstance playerInstance)
	{
		if (playerInstance == null)
			return false;

		synchronized (_participatedPlayers)
		{
			String playerName = playerInstance.getName();

			_participatedPlayers.put(playerName, playerInstance);

			if (!_participatedPlayerNames.contains(playerName))
				_participatedPlayerNames.add(playerName);
		}

		return true;
	}

	/**
	 * Removes a player from the team<br><br>
	 *
	 * @param playerName<br>
	 */
	public void removePlayer(String playerName)
	{
		synchronized (_participatedPlayers)
		{
			_participatedPlayers.remove(playerName);
			_participatedPlayerNames.remove(playerName);
		}
	}

	/**
	 * Increases the points of the team<br>
	 */
	public void increasePoints()
	{
		_points++;
	}

	/**
	 * Cleanup the team and make it ready for adding players again<br>
	 */
	public void cleanMe()
	{
		_participatedPlayers.clear();
		_participatedPlayerNames.clear();
		_participatedPlayers = new FastMap<String, L2PcInstance>();
		_participatedPlayerNames = new Vector<String>();
		_points = 0;
	}

	/**
	 * Is given player in this team?<br><br>
	 *
	 * @param playerName<br>
	 * @return boolean<br>
	 */
	public boolean containsPlayer(String playerName)
	{
		boolean containsPlayer;

		synchronized (_participatedPlayers)
		{
			containsPlayer = _participatedPlayerNames.contains(playerName);
		}

		return containsPlayer;
	}

	/**
	 * Returns the name of the team<br><br>
	 *
	 * @return String<br>
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * Returns the coordinates of the team spot<br><br>
	 *
	 * @return int[]<br>
	 */
	public int[] getCoordinates()
	{
		return _coordinates;
	}

	/**
	 * Returns the points of the team<br><br>
	 *
	 * @return short<br>
	 */
	public short getPoints()
	{
		return _points;
	}

	/**
	 * Returns name and instance of all participated players in FastMap<br><br>
	 *
	 * @return Map<String, L2PcInstance><br>
	 */
	public Map<String, L2PcInstance> getParticipatedPlayers()
	{
		Map<String, L2PcInstance> participatedPlayers = null;

		synchronized (_participatedPlayers)
		{
			participatedPlayers = _participatedPlayers;
		}

		return participatedPlayers;
	}

	/**
	 * Returns name of all participated players in Vector<br><br>
	 *
	 * @return Vector<String><br>
	 */
	public Vector<String> getParticipatedPlayerNames()
	{
		Vector<String> participatedPlayerNames = null;

		synchronized (_participatedPlayers)
		{
			participatedPlayerNames = _participatedPlayerNames;
		}

		return participatedPlayerNames;
	}

	/**
	 * Returns player count of this team<br><br>
	 *
	 * @return int<br>
	 */
	public int getParticipatedPlayerCount()
	{
		int participatedPlayerCount;

		synchronized (_participatedPlayers)
		{
			participatedPlayerCount = _participatedPlayers.size();
		}

		return participatedPlayerCount;
	}
}
