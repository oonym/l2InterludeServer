package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2TvTEventNpcInstance extends L2NpcInstance
{
	public L2TvTEventNpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
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
	    		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());

				npcHtmlMessage.setHtml(htmContent);
	    		npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
				npcHtmlMessage.replace("%team1name%", Config.TVT_EVENT_TEAM_1_NAME);
				npcHtmlMessage.replace("%team1playercount%", String.valueOf(TvTEvent.getTeamsPlayerCounts()[0]));
				npcHtmlMessage.replace("%team2name%", Config.TVT_EVENT_TEAM_2_NAME);
				npcHtmlMessage.replace("%team2playercount%", String.valueOf(TvTEvent.getTeamsPlayerCounts()[1]));
	    		playerInstance.sendPacket(npcHtmlMessage);
	    	}
		}
		
		playerInstance.sendPacket(new ActionFailed());
	}
}
