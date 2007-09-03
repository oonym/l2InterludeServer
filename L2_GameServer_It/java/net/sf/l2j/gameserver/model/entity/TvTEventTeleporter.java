package net.sf.l2j.gameserver.model.entity;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class TvTEventTeleporter implements Runnable
{
	/** The instance of the player to teleport */
	private L2PcInstance _playerInstance;
	/** Coordinates of the spot to teleport to */
	private int[] _coordinates = new int[3];

	/**
	 * Initialize the teleporter and start the delayed task
	 * 
	 * @param playerInstance
	 * @param coordinates
	 * @param reAdd
	 */
	public TvTEventTeleporter(L2PcInstance playerInstance, int[] coordinates, boolean reAdd)
	{
		_playerInstance = playerInstance;
		_coordinates = coordinates;

		// in config as seconds
		long delay = (TvTEvent.isStarted() ? Config.TVT_EVENT_RESPAWN_TELEPORT_DELAY : Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY) * 1000;

		if (reAdd)
			delay = 0;

		ThreadPoolManager.getInstance().scheduleGeneral(this, delay);
	}

	/**
	 * The task method to teleport the player<br>
	 * 1. Unsummon pet if there is one
	 * 2. Remove all effects
	 * 3. Revive and full heal the player
	 * 4. Teleport the player
	 * 5. Broadcast status and user info
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		if (_playerInstance == null)
			return;
		
		L2Summon summon = _playerInstance.getPet();
	   
		if (summon != null)
			summon.unSummon(_playerInstance);
		   
		for (L2Effect effect : _playerInstance.getAllEffects())
		{
			if (effect != null)
				effect.exit();
		}

		_playerInstance.doRevive();
		_playerInstance.setCurrentCp(_playerInstance.getMaxCp());
		_playerInstance.setCurrentHp(_playerInstance.getMaxHp());
		_playerInstance.setCurrentMp(_playerInstance.getMaxMp());
		_playerInstance.teleToLocation(_coordinates[0], _coordinates[1], _coordinates[2], false);
		
		if (TvTEvent.isStarted())
			_playerInstance.setTeam(TvTEvent.getParticipantTeamId(_playerInstance.getName())+1);
		else
			_playerInstance.setTeam(0);
		
		_playerInstance.broadcastStatusUpdate();
		_playerInstance.broadcastUserInfo();
	}
}
