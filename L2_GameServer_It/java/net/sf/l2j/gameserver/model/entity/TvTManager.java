package net.sf.l2j.gameserver.model.entity;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.entity.TvTEvent;

public class TvTManager implements Runnable
{
	private static TvTManager _instance = null;

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

	public static TvTManager getInstance()
	{
		if (_instance == null)
			_instance = new TvTManager();

		return _instance;
	}

	public void run()
	{
		TvTEvent.init();

		for (;;)
		{
			waiter(Config.TVT_EVENT_INTERVAL * 60 * 1000); // in config given as minutes
			
			if (!TvTEvent.startParticipation())
			{
				Announcements.getInstance().announceToAll("TvT Event: Event was canceled.");
				System.out.println("TvTEventEngine[TvTManager.run()]: Error spawning event npc for participation.");
				continue;
			}
			else
				Announcements.getInstance().announceToAll("TvT Event: Registration opened for " + Config.TVT_EVENT_PARTICIPATION_TIME +  " minute(s).");

			waiter(Config.TVT_EVENT_PARTICIPATION_TIME * 60 * 1000); // in config given as minutes

			if (!TvTEvent.startFight())
			{
				Announcements.getInstance().announceToAll("TvT Event: Event canceled cause of registration lack.");
				System.out.println("TvTEventEngine[TvTManager.run()]: Lack of registration, abbort event.");
				continue;
			}
			else
				Announcements.getInstance().announceToAll("TvT Event: Teleport participants to team spot in " + Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY + " second(s).");

			waiter(Config.TVT_EVENT_RUNNING_TIME * 60 * 1000); // in config given as minutes
			Announcements.getInstance().announceToAll("TvT Event: Event finish. Team \"" + TvTEvent.calculateRewards() + "\" wins.");
			Announcements.getInstance().announceToAll("TvT Event: Teleport back to registration npc in " + Config.TVT_EVENT_START_LEAVE_TELEPORT_DELAY + " second(s).");
			TvTEvent.stopFight();
		}
	}

	void waiter(long interval)
	{
		long startWaiterTime = System.currentTimeMillis();
		int seconds = (int)(interval / 1000);

		while (startWaiterTime + interval > System.currentTimeMillis())
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
						Announcements.getInstance().announceToAll("TvT Event: " + seconds / 60 / 60 + " hour(s) till event finish!");

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
						Announcements.getInstance().announceToAll("TvT Event: " + seconds / 60 + " minute(s) till event finish!");

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
						Announcements.getInstance().announceToAll("TvT Event: " + seconds + " second(s) till event finish!");

					break;
				}
			}

			long startOneSecondWaiterStartTime = System.currentTimeMillis();

			// only the try catch with Thread.sleep(1000) give bad countdown on high wait times
			while (startOneSecondWaiterStartTime + 1000 > System.currentTimeMillis())
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
