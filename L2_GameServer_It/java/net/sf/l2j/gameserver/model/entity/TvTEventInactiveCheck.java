package net.sf.l2j.gameserver.model.entity;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.entity.TvTEventTeam;

/**
 * Check if players in TvT event are inactive, i don't thing the methods need comments
 *
 * @author FBIagent
 */
public class TvTEventInactiveCheck implements Runnable
{
	public TvTEventInactiveCheck()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(this, 30000);
	}

	public void run()
	{
		while (TvTEvent.isStarted())
		{			
			TvTEventTeam[] teams = TvTEvent.getTeams();
			
			for (byte i=0;i<2;i++)
			{
				for (String playerName : teams[i].getParticipatedPlayerNames())
				{
					long lastActivity = teams[i].getPlayerLastActivity(playerName);
					
					if (lastActivity + 30000 < System.currentTimeMillis())
					{
						L2PcInstance playerInstance = teams[i].getPlayerInstance(playerName);

						if (playerInstance == null)
							continue;
						
						teams[i].removePlayer(playerName);
						new TvTEventTeleporter(playerInstance, Config.TVT_EVENT_PARTICIPATION_NPC_COORDINATES, true, true);
					}
				}
			}
			
			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException ie)
			{}
		}
	}
}
