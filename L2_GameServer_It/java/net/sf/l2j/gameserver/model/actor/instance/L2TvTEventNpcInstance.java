package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2TvTEventNpcInstance extends L2NpcInstance
{
	public L2TvTEventNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void showChatWindow(L2PcInstance playerInstance, int val)
	{
		if (playerInstance == null)
			return;
		
		if (TvTEvent.isParticipating())
		{
			String htmFile = "data/html/mods/";

			if (!TvTEvent.isPlayerParticipant(playerInstance.getName()))
				htmFile += "TvTEventParticipation";
			else
				htmFile += "TvTEventRemoveParticipation";
			
			htmFile += ".htm";

			String htmContent = HtmCache.getInstance().getHtm(htmFile);

	    	if (htmContent != null)
	    	{
	    		int[] teamsPlayerCounts = TvTEvent.getTeamsPlayerCounts();
	    		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

				npcHtmlMessage.setHtml(htmContent);
	    		npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
				npcHtmlMessage.replace("%team1name%", Config.TVT_EVENT_TEAM_1_NAME);
				npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
				npcHtmlMessage.replace("%team2name%", Config.TVT_EVENT_TEAM_2_NAME);
				npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
	    		playerInstance.sendPacket(npcHtmlMessage);
	    	}
		}
		else if (TvTEvent.isStarting() || TvTEvent.isStarted())
		{
			String htmFile = "data/html/mods/TvTEventStatus.htm";
			String htmContent = HtmCache.getInstance().getHtm(htmFile);

	    	if (htmContent != null)
	    	{
	    		int[] teamsPlayerCounts = TvTEvent.getTeamsPlayerCounts();
	    		int[] teamsPointsCounts = TvTEvent.getTeamsPoints();
	    		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

				npcHtmlMessage.setHtml(htmContent);
	    		//npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
				npcHtmlMessage.replace("%team1name%", Config.TVT_EVENT_TEAM_1_NAME);
				npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
				npcHtmlMessage.replace("%team1points%", String.valueOf(teamsPointsCounts[0]));
				npcHtmlMessage.replace("%team2name%", Config.TVT_EVENT_TEAM_2_NAME);
				npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
				npcHtmlMessage.replace("%team2points%", String.valueOf(teamsPointsCounts[1])); // <---- array index from 0 to 1 thx DaRkRaGe
	    		playerInstance.sendPacket(npcHtmlMessage);
	    	}
		}
		
		playerInstance.sendPacket(new ActionFailed());
	}
}
