package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;

public class Guide implements IItemHandler {
	private static short[] _itemIds = { 5588, 6317, 7561 };
	//private static Logger _log = Logger.getLogger(L2NpcInstance.class.getName());
	
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		L2PcInstance activeChar = (L2PcInstance) playable;
        
        String filename = "data/html/tutorial/" + item.getItemId() + ".htm";
        String content = HtmCache.getInstance().getHtm(filename);
        
		if (content == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setHtml("<html><head><body>My Text is missing:<br>"+filename+"</body></html>");
			activeChar.sendPacket(html);
			
			activeChar.sendPacket( new ActionFailed() );
		}
		else
		{
			NpcHtmlMessage itemReply = new NpcHtmlMessage(5);
			itemReply.setHtml(content);
			activeChar.sendPacket(itemReply);
		}
		
		activeChar.sendPacket( new ActionFailed() );
	}

	public short[] getItemIds()
	{
		return _itemIds;
	}
}
