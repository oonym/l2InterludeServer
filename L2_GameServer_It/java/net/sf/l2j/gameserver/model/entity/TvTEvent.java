package net.sf.l2j.gameserver.model.entity;

import net.sf.l2j.Config;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEventTeam;
import net.sf.l2j.gameserver.model.entity.TvTEventTeleporter;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * @author FBIagent
 */
public class TvTEvent
{
	enum EventState
	{
		INACTIVE,
		INACTIVATING,
		PARTICIPATING,
		STARTING,
		STARTED,
		REWARDING
	}

	/**	The teams of the TvTEvent<br> */
	private static TvTEventTeam[] _teams = new TvTEventTeam[2]; // event only allow max 2 teams
	/** The state of the TvTEvent<br> */
	private static EventState _state = EventState.INACTIVE;
	/** The spawn of the participation npc<br> */
	private static L2Spawn _npcSpawn = null;
	/** the npc instance of the participation npc<br> */
	private static L2NpcInstance _lastNpcSpawn = null;

	/**
	 * No instance of this class!<br>
	 */
	private TvTEvent()
	{}

	/**
	 * Teams initializing<br>
	 */
	public static void init()
	{
		_teams[0] = new TvTEventTeam(Config.TVT_EVENT_TEAM_1_NAME, Config.TVT_EVENT_TEAM_1_COORDINATES);
		_teams[1] = new TvTEventTeam(Config.TVT_EVENT_TEAM_2_NAME, Config.TVT_EVENT_TEAM_2_COORDINATES);
	}

	/**
	 * Starts the participation of the TvTEvent<br>
	 * 1. Get L2NpcTemplate by Config.TVT_EVENT_PARTICIPATION_NPC_ID<br>
	 * 2. Try to spawn a new npc of it for participation<br><br>
	 * 
	 * @return boolean<br>
	 */
	public static boolean startParticipation()
	{
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(Config.TVT_EVENT_PARTICIPATION_NPC_ID);
		
		if (tmpl == null)
		{
			System.out.println("TvTEventEngine[TvTEvent.startParticipation()]: L2NpcTemplate is a NullPointer -> Invalid npc id in configs?");
			return false;
		}

        try
        {
            _npcSpawn = new L2Spawn(tmpl);

            _npcSpawn.setLocx(Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0]);
            _npcSpawn.setLocy(Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1]);
            _npcSpawn.setLocz(Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
            _npcSpawn.setAmount(1);
            _npcSpawn.setHeading(0);
            _npcSpawn.setRespawnDelay(1);
            // later no need to delete spawn from db, we don't store it (false)
            SpawnTable.getInstance().addNewSpawn(_npcSpawn, false);
            _npcSpawn.init();
            _lastNpcSpawn = _npcSpawn.getLastSpawn();
            _lastNpcSpawn.setCurrentHp(_lastNpcSpawn.getMaxHp());
            _lastNpcSpawn.setTitle("TvT Event Participation");
            _lastNpcSpawn.isAggressive();
            _lastNpcSpawn.decayMe();
            _lastNpcSpawn.spawnMe(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
            _lastNpcSpawn.broadcastPacket(new MagicSkillUser(_lastNpcSpawn, _lastNpcSpawn, 1034, 1, 1, 1));
        }
        catch (Exception e)
        {
            System.out.println("TvTEventEngine[TvTEvent.startParticipation()]: exception: " + e);
            return false;
        }

		setState(EventState.PARTICIPATING);
		return true;
	}

	/**
	 * Starts the TvTEvent fight<br>
	 * 1. Set state EventState.STARTING<br>
	 * 2. Remove participation npc from world<br>
	 * 3. Abort if not enought participants(return false)<br>
	 * 4. Set state EventState.STARTED<br>
	 * 5. Teleport all participants to team spot<br><br>
	 * 
	 * @return boolean<br>
	 */
	public static boolean startFight()
	{
		setState(EventState.STARTING);
		_lastNpcSpawn.deleteMe();
        _npcSpawn.stopRespawn();
        _npcSpawn = null;
		_lastNpcSpawn = null;

		// not enought participants
		if (_teams[0].getParticipatedPlayerCount() < Config.TVT_EVENT_MIN_PLAYERS_IN_TEAMS || _teams[1].getParticipatedPlayerCount() < Config.TVT_EVENT_MIN_PLAYERS_IN_TEAMS)
		{
			setState(EventState.INACTIVE);
			_teams[0].cleanMe();
			_teams[1].cleanMe();
			return false;
		}
		
		setState(EventState.STARTED); // set state to STARTED here, so TvTEventTeleporter know to teleport to team spot

		// teleport all participants to there team spot
		for (TvTEventTeam team : _teams)
		{
			for (String playerName : team.getParticipatedPlayerNames())
			{
				L2PcInstance playerInstance = team.getParticipatedPlayers().get(playerName);

				if (playerInstance == null)
					continue;

				// implements Runnable and starts itself in constructor
				new TvTEventTeleporter(playerInstance, team.getCoordinates(), false);
			}
		}

		return true;
	}

	/**
	 * Calculates the TvTEvent reward<br>
	 * 1. If both teams are at a tie(points equals), send it as system message to all participants, if one of the teams have 0 participants left online abort rewarding<br>
	 * 2. Wait till teams are not at a tie anymore<br>
	 * 3. Set state EvcentState.REWARDING<br>
	 * 4. Reward team with more points<br>
	 * 5. Show win html to wining team participants<br><br>
	 * 
	 * @return String<br>
	 */
	public static String calculateRewards()
	{
		if (_teams[0].getPoints() == _teams[1].getPoints())
		{
			if (_teams[0].getParticipatedPlayerCount() == 0 || _teams[1].getParticipatedPlayerCount() == 0)
			{
				// the fight cannot be completed
				setState(EventState.REWARDING);
				return "Nobody";
			}
			
			sysMsgToAllParticipants("TvT Event: Both teams are at a tie, next killing team win!");
		}

		while (_teams[0].getPoints() == _teams[1].getPoints())
		{
			try
			{
				Thread.sleep(1);
			}
			catch (InterruptedException ie)
			{}
		}
 
		setState(EventState.REWARDING); // after state REWARDING is set, nobody can point anymore

		byte teamId = (byte)(_teams[0].getPoints() > _teams[1].getPoints() ? 0 : 1); // which team wins?
		TvTEventTeam team = _teams[teamId];

		for (String playerName : team.getParticipatedPlayerNames())
		{
			L2PcInstance playerInstance = team.getParticipatedPlayers().get(playerName);

			if (playerInstance == null)
				continue;
			
        	PcInventory inv = playerInstance.getInventory();
            
        	if (ItemTable.getInstance().createDummyItem(Config.TVT_EVENT_REWARD[0]).isStackable())
        		inv.addItem("TvT Event", Config.TVT_EVENT_REWARD[0], Config.TVT_EVENT_REWARD[1], playerInstance, playerInstance);
        	else
        	{
        		for (int i=0;i<Config.TVT_EVENT_REWARD[1];i++)
        			inv.addItem("TvT Event", Config.TVT_EVENT_REWARD[0], 1, playerInstance, playerInstance);
        	}
            
        	SystemMessage systemMessage = null;

        	if (Config.TVT_EVENT_REWARD[1] > 1)
        	{
        		systemMessage = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
        		systemMessage.addItemName(Config.TVT_EVENT_REWARD[0]);
        		systemMessage.addNumber(Config.TVT_EVENT_REWARD[1]);
        	}
        	else
        	{
        		systemMessage = new SystemMessage(SystemMessageId.EARNED_ITEM);
        		systemMessage.addItemName(Config.TVT_EVENT_REWARD[0]);
        	}
        	
        	playerInstance.sendPacket(systemMessage);
            
        	StatusUpdate statusUpdate = new StatusUpdate(playerInstance.getObjectId());
        	statusUpdate.addAttribute(StatusUpdate.CUR_LOAD, playerInstance.getCurrentLoad());
        	playerInstance.sendPacket(statusUpdate);

        	NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
        	npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>Your team win the event. Look in your inventar there should be the reward.</body></html>");
        	playerInstance.sendPacket(npcHtmlMessage);
		}
		
		return team.getName();
	}

	/**
	 * Stops the TvTEvent fight<br>
	 * 1. Set state EventState.INACTIVATING<br>
	 * 2. Teleport all participants back to participation npc location<br>
	 * 3. Teams cleaning<br>
	 * 4. Set state EventState.INACTIVE<br>
	 */
	public static void stopFight()
	{
		setState(EventState.INACTIVATING);

		for (TvTEventTeam team : _teams)
		{
			for (String playerName : team.getParticipatedPlayerNames())
			{
				L2PcInstance playerInstance = team.getParticipatedPlayers().get(playerName);

				if (playerInstance == null)
					continue;

				new TvTEventTeleporter(playerInstance, Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES, false);
			}
		}

		_teams[0].cleanMe();
		_teams[1].cleanMe();
		setState(EventState.INACTIVE);
	}

	/**
	 * Adds a player to a TvTEvent team<br>
	 * 1. Calculate the id of the team in which the player should be added<br>
	 * 2. Add the player to the calculated team<br><br>
	 * 
	 * @param playerInstance<br>
	 * @return boolean<br>
	 */
	public static synchronized boolean addParticipant(L2PcInstance playerInstance)
	{
		if (playerInstance == null)
			return false;
		
		byte teamId = 0;
		
		if (_teams[0].getParticipatedPlayerCount() == _teams[1].getParticipatedPlayerCount())
			teamId = (byte)(Rnd.get(2));
		else
			teamId = (byte)(_teams[0].getParticipatedPlayerCount() > _teams[1].getParticipatedPlayerCount() ? 1 : 0);
		
		_teams[teamId].addPlayer(playerInstance);
		return true;
	}

	/**
	 * Removes a TvTEvent player from it's team<br>
	 * 1. Get team id of the player<br>
	 * 2. Remove player from it's team<br><br>
	 * 
	 * @param playerName<br>
	 * @return boolean<br>
	 */
	public static boolean removeParticipant(String playerName)
	{
		byte teamId = getParticipantTeamId(playerName);

		if (teamId == -1)
			return false;

		_teams[teamId].removePlayer(playerName);
		return true;
	}

	/**
	 * Send a SystemMessage to all participated players<br>
	 * 1. Send the message to all players of team number one<br>
	 * 2. Send the message to all players of team number two<br><br>
	 * 
	 * @param message<br>
	 */
	public static void sysMsgToAllParticipants(String message)
	{
		for (L2PcInstance playerInstance : _teams[0].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
				playerInstance.sendMessage(message);
		}

		for (L2PcInstance playerInstance : _teams[1].getParticipatedPlayers().values())
		{
			if (playerInstance != null)
				playerInstance.sendMessage(message);
		}
	}

	/**
	 * Called when a player logs in<br><br>
	 * 
	 * @param playerInstance<br>
	 */
	public static void onLogin(L2PcInstance playerInstance)
	{
		if (playerInstance == null || (!isStarting() && !isStarted()))
			return;

		byte teamId = getParticipantTeamId(playerInstance.getName());

		if (teamId == -1)
			return;

		_teams[teamId].addPlayer(playerInstance);
		new TvTEventTeleporter(playerInstance, _teams[teamId].getCoordinates(), true);
	}

	/**
	 * Called on every bypass starting with "npc_"<br><br> 
	 * 
	 * @param command<br>
	 * @param playerInstance<br>
	 */
	public static void onBypass(String command, L2PcInstance playerInstance)
	{
		if (playerInstance == null || !isParticipating())
			return;
		
		if (command.equals("tvt_event_participation"))
		{
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

			if (addParticipant(playerInstance))
				npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>You are on the registration list now.</body></html>");
			else
				npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>Unable to register!</body></html>");
			
			playerInstance.sendPacket(npcHtmlMessage);		
		}
		else if (command.equals("tvt_event_remove_participation"))
		{
			removeParticipant(playerInstance.getName());

			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

			npcHtmlMessage.setHtml("<html><head><title>TvT Event</title></head><body>You are not longer on the registration list.</body></html>");
			playerInstance.sendPacket(npcHtmlMessage);
		}
	}

	/**
	 * Called on every onAction in L2PcIstance<br><br>
	 * 
	 * @param playerName<br>
	 * @param targetPlayerName<br>
	 */
	public static boolean onAction(String playerName, String targetPlayerName)
	{
		if (!isStarted())
			return true;

		byte playerTeamId = getParticipantTeamId(playerName);
		byte targetPlayerTeamId = getParticipantTeamId(targetPlayerName);

		if ((playerTeamId != -1 && targetPlayerTeamId == -1) ||
			(playerTeamId == -1 && targetPlayerTeamId != -1))
			return false;
		
		if (playerTeamId != -1 && targetPlayerTeamId != -1 && playerTeamId == targetPlayerTeamId && !Config.TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED)
			return false;
		
		return true;
	}

	/**
	 * Called on every potion use<br><br>
	 * 
	 * @param playerName<br>
	 */
	public static boolean onPotionUse(String playerName)
	{
		if (!isStarted())
			return true;

		if (isPlayerParticipant(playerName) && !Config.TVT_EVENT_POTIONS_ALLOWED)
			return false;

		return true;
	}

	/**
	 * Called on every summon item use<br><br>
	 *
	 * @param playerName<br>
	 */
	public static boolean onItemSummon(String playerName)
	{
		if (!isStarted())
			return true;

		if (isPlayerParticipant(playerName) && !Config.TVT_EVENT_SUMMON_BY_ITEM_ALLOWED)
			return false;

		return true;
	}
	
	/**
	 * Is called when a player is killed<br><br>
	 * 
	 * @param kilerCharacter<br>
	 * @param killedPlayerInstance<br>
	 */
	public static void onKill(L2Character killerCharacter, L2PcInstance killedPlayerInstance)
	{
		if (killerCharacter == null || killedPlayerInstance == null || !(killerCharacter instanceof L2PcInstance) || !isStarted())
			return;

		L2PcInstance killerPlayerInstance = (L2PcInstance)killerCharacter;
		String playerName = killerPlayerInstance.getName();
		byte killerTeamId = getParticipantTeamId(playerName);

		playerName = killedPlayerInstance.getName();

		byte killedTeamId = getParticipantTeamId(playerName);

		if (killerTeamId != -1 && killedTeamId != -1 && killerTeamId != killedTeamId)
			_teams[killerTeamId].increasePoints();

		if (killedTeamId != -1)
			new TvTEventTeleporter(killedPlayerInstance, _teams[killedTeamId].getCoordinates(), false);
	}

	/**
	 * Sets the TvTEvent state<br><br>
	 * 
	 * @param state<br>
	 */
	private static void setState(EventState state)
	{
		synchronized (_state)
		{
			_state = state;
		}
	}

	/**
	 * Is TvTEvent inactive?<br><br>
	 * 
	 * @return boolean<br>
	 */
	public static boolean isInactive()
	{
		boolean isInactive;

		synchronized (_state)
		{
			isInactive = _state == EventState.INACTIVE;
		}

		return isInactive;
	}

	/**
	 * Is TvTEvent in inactivating?<br><br>
	 * 
	 * @return boolean<br>
	 */
	public static boolean isInactivating()
	{
		boolean isInactivating;

		synchronized (_state)
		{
			isInactivating = _state == EventState.INACTIVATING;
		}

		return isInactivating;
	}

	/**
	 * Is TvTEvent in participation?<br><br>
	 * 
	 * @return boolean<br>
	 */
	public static boolean isParticipating()
	{
		boolean isParticipating;

		synchronized (_state)
		{
			isParticipating = _state == EventState.PARTICIPATING;
		}

		return isParticipating;
	}

	/**
	 * Is TvTEvent starting?<br><br>
	 * 
	 * @return boolean<br>
	 */
	public static boolean isStarting()
	{
		boolean isStarting;

		synchronized (_state)
		{
			isStarting = _state == EventState.STARTING;
		}

		return isStarting;
	}

	/**
	 * Is TvTEvent started?<br><br>
	 * 
	 * @return boolean<br>
	 */
	public static boolean isStarted()
	{
		boolean isStarted;

		synchronized (_state)
		{
			isStarted = _state == EventState.STARTED;
		}

		return isStarted;
	}

	/**
	 * Is TvTEvent rewadrding?<br><br>
	 * 
	 * @return boolean<br>
	 */
	public static boolean isRewarding()
	{
		boolean isRewarding;

		synchronized (_state)
		{
			isRewarding = _state == EventState.REWARDING;
		}

		return isRewarding;
	}

	/**
	 * Returns the team id of a player, if playedr is not participant it returns -1<br><br>
	 *
	 * @param playerName<br>
	 * @return byte<br>
	 */
	public static byte getParticipantTeamId(String playerName)
	{
		return (byte)(_teams[0].containsPlayer(playerName) ? 0 : (_teams[1].containsPlayer(playerName) ? 1 : -1));
	}

	/**
	 * Is given player participant of the event?<br><br>
	 * 
	 * @param playerName<br>
	 * @return boolean<br>
	 */
	public static boolean isPlayerParticipant(String playerName)
	{
		return _teams[0].containsPlayer(playerName) || _teams[1].containsPlayer(playerName);
	}

	/**
	 * Returns participated player count<br><br>
	 * 
	 * @return int<br>
	 */
	public static int getParticipatedPlayersCount()
	{
		return _teams[0].getParticipatedPlayerCount() + _teams[1].getParticipatedPlayerCount();
	}

	/**
	 * Returns teams names<br><br>
	 * 
	 * @return String[]<br>
	 */
	public static String[] getTeamNames()
	{
		return new String[]{_teams[0].getName(), _teams[1].getName()};
	}

	/**
	 * Returns player count of both teams<br><br>
	 * 
	 * @return int[]<br>
	 */
	public static int[] getTeamsPlayerCounts()
	{
		return new int[]{_teams[0].getParticipatedPlayerCount(), _teams[1].getParticipatedPlayerCount()};
	}
}
