package net.sf.l2j.gameserver.model.entity;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;

/**
 * @author FBIagent
 */
public class TvTManager implements Runnable
{
	/** The one and only instance of this class<br> */
	private static TvTManager _instance = null;

	/**
	 * New instance only by getInstance()<br>
	 */
	private TvTManager()
	{
		if (Config.TVT_EVENT_ENABLED)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(this, 0);
			System.out.println("TvTEventEngine[TvTManager.TvTManager()]: Started.");
		}
		else
			System.out.println("TvTEventEngine[TvTManager.TvTManager()]: Engine is disabled.");
	}

	/**
	 * Initialize new/Returns the one and only instance<br><br> 
	 * 
	 * @return TvTManager<br>
	 */
	public static TvTManager getInstance()
	{
		if (_instance == null)
			_instance = new TvTManager();

		return _instance;
	}

	/**
	 * The task method to handle cycles of the event<br><br>
	 * 
	 * @see java.lang.Runnable#run()<br>
	 */
	public void run()
	{
		TvTEvent.init();

		for (;;)
		{
			waiter(Config.TVT_EVENT_INTERVAL * 60); // in config given as minutes
			
			if (!TvTEvent.startParticipation())
			{
				Announcements.getInstance().announceToAll("TvT Event: Event was canceled.");
				System.out.println("TvTEventEngine[TvTManager.run()]: Error spawning event npc for participation.");
				continue;
			}
			else
				Announcements.getInstance().announceToAll("TvT Event: Registration opened for " + Config.TVT_EVENT_PARTICIPATION_TIME +  " minute(s).");

			waiter(Config.TVT_EVENT_PARTICIPATION_TIME * 60); // in config given as minutes

			if (!TvTEvent.startFight())
			{
				Announcements.getInstance().announceToAll("TvT Event: Event canceled cause of registration lack.");
				System.out.println("TvTEventEngine[TvTManager.run()]: Lack of registration, abort event.");
				continue;
			}
			else
				Announcements.getInstance().announceToAll("TvT Event: Teleport participants to team spot in " + Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY + " second(s).");

			waiter(Config.TVT_EVENT_RUNNING_TIME * 60); // in config given as minutes
			Announcements.getInstance().announceToAll(TvTEvent.calculateRewards());
			TvTEvent.sysMsgToAllParticipants("TvT Event: Teleport back to registration npc in " + Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY + " second(s).");
			TvTEvent.stopFight();
		}
	}

	/**
	 * This method waits for a period time delay<br><br>
	 * 
	 * @param interval<br>
	 */
	void waiter(int seconds)
	{
		while (seconds > 1)
		{
			seconds--; // here because we don't want to see two time announce at the same time
			
			if (TvTEvent.isParticipating() || TvTEvent.isStarted())
			{			
				switch (seconds)
				{
				case 3600: // 1 hour left
					if (TvTEvent.isParticipating())
						Announcements.getInstance().announceToAll("TvT Event: " + seconds / 60 / 60 + " hour(s) till registration close!");
					else if (TvTEvent.isStarted())
						TvTEvent.sysMsgToAllParticipants("TvT Event: " + seconds / 60 / 60 + " hour(s) till event finish!");

					break;
				case 1800: // 30 minutes left
				case 900: // 15 minutes left
				case 600: //  10 minutes left 
				case 300: // 5 minutes left
				case 240: // 4 minutes left
				case 180: // 3 minutes left
				case 120: // 2 minutes left
				case 60: // 1 minute left															   
					if (TvTEvent.isParticipating())
						Announcements.getInstance().announceToAll("TvT Event: " + seconds / 60 + " minute(s) till registration close!");
					else if (TvTEvent.isStarted())
						TvTEvent.sysMsgToAllParticipants("TvT Event: " + seconds / 60 + " minute(s) till event finish!");

					break;
				case 30: // 30 seconds left
				case 15: // 15 seconds left
				case 10: // 10 seconds left
				case 5: // 5 seconds left
				case 4: // 4 seconds left
				case 3: // 3 seconds left
				case 2: // 2 seconds left
				case 1: // 1 seconds left
					if (TvTEvent.isParticipating())
						Announcements.getInstance().announceToAll("TvT Event: " + seconds + " second(s) till registration close!");
					else if (TvTEvent.isStarted())
						TvTEvent.sysMsgToAllParticipants("TvT Event: " + seconds + " second(s) till event finish!");

					break;
				}
			}

			long oneSecWaitStart = System.currentTimeMillis();

			// only the try catch with Thread.sleep(1000) give bad countdown on high wait times
			while (oneSecWaitStart + 1000L > System.currentTimeMillis())
			{
				try
				{
					Thread.sleep(1);
				}
				catch (InterruptedException ie)
				{}
			}
		}
	}
}
