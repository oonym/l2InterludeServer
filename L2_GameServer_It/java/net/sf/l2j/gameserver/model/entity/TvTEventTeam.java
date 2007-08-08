package net.sf.l2j.gameserver.model.entity;

import java.util.Map;
import java.util.Vector;

import javolution.util.FastMap;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class TvTEventTeam
{
	private String _name;
	private int[] _coordinates = new int[3];
	private short _points;
	private Map<String, L2PcInstance> _participatedPlayers = new FastMap<String, L2PcInstance>();
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

		synchronized (_participatedPlayers)
		{
			String playerName = playerInstance.getName();

			_participatedPlayers.put(playerName, playerInstance);
			
			if (!_participatedPlayerNames.contains(playerName))
				_participatedPlayerNames.add(playerName);
		}

		return true;
	}

	public void removePlayer(String playerName)
	{
		synchronized (_participatedPlayers)
		{
			_participatedPlayers.remove(playerName);
			_participatedPlayerNames.remove(playerName);
		}
	}

	public void increasePoints()
	{
		_points++;
	}
	
	public void cleanMe()
	{
		_participatedPlayers.clear();
		_participatedPlayerNames.clear();
		_participatedPlayers = new FastMap<String, L2PcInstance>();
		_participatedPlayerNames = new Vector<String>();
		_points = 0;
	}

	public boolean containsPlayer(String playerName)
	{
		boolean containsPlayer;

		synchronized (_participatedPlayers)
		{
			containsPlayer = _participatedPlayerNames.contains(playerName);
		}

		return containsPlayer;
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

	public Map<String, L2PcInstance> getParticipatedPlayers()
	{
		Map<String, L2PcInstance> participatedPlayers = null;

		synchronized (_participatedPlayers)
		{
			participatedPlayers = _participatedPlayers;
		}

		return participatedPlayers;
	}

	public Vector<String> getParticipatedPlayerNames()
	{
		Vector<String> participatedPlayerNames = null;

		synchronized (_participatedPlayers)
		{
			participatedPlayerNames = _participatedPlayerNames;
		}

		return participatedPlayerNames;
	}

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
