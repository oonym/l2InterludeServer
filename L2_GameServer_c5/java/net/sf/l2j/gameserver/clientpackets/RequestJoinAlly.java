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
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.AskJoinAlly;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestJoinAlly extends ClientBasePacket{
	
	private static final String _C__82_REQUESTJOINALLY = "[C] 82 RequestJoinAlly";
	//private static Logger _log = Logger.getLogger(RequestJoinAlly.class.getName());

	private final int _id;
	
	public RequestJoinAlly(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_id = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
		    return;
		}
		if (!(L2World.getInstance().findObject(_id) instanceof L2PcInstance))
		{
        	activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_INVITED_THE_WRONG_TARGET));
		    return;
		}
		if(activeChar.getClan() == null)
        {
			activeChar.sendPacket(new SystemMessage(SystemMessage.YOU_ARE_NOT_A_CLAN_MEMBER));
            return;
        }
		L2PcInstance target = (L2PcInstance) L2World.getInstance().findObject(_id);
        L2Clan clan = activeChar.getClan();
        if (!clan.CheckAllyJoinCondition(activeChar, target))
        {
        	return;
        } 
        if (!activeChar.getRequest().setRequest(target, this))
        {
        	return;
        } 
		
		SystemMessage sm = new SystemMessage(SystemMessage.S2_ALLIANCE_LEADER_OF_S1_REQUESTED_ALLIANCE);
		sm.addString(activeChar.getClan().getAllyName());
		sm.addString(activeChar.getName());
		target.sendPacket(sm);
		sm = null;
		AskJoinAlly aja = new AskJoinAlly(activeChar.getObjectId(), activeChar.getClan().getAllyName());
		target.sendPacket(aja);
	    return;
	}
	
	
	public String getType()
	{
		return _C__82_REQUESTJOINALLY;
	}
}

