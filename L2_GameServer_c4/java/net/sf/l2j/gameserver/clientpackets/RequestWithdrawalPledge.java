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
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListDelete;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListDeleteAll;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestWithdrawalPledge extends ClientBasePacket
{
	private static final String _C__26_REQUESTWITHDRAWALPLEDGE = "[C] 26 RequestWithdrawalPledge";
	static Logger _log = Logger.getLogger(RequestWithdrawalPledge.class.getName());
			
	public RequestWithdrawalPledge(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		//is the guy in a clan  ?
		if (activeChar == null || activeChar.getClanId() == 0) 
		{	
			return;
		}
		
		L2Clan clan = activeChar.getClan();
        // this also updated the database
		clan.removeClanMember(activeChar.getName());
		//player withdrawed.
		SystemMessage sm = new SystemMessage(223);
		sm.addString(activeChar.getName());
    	clan.broadcastToOnlineMembers(sm);
        // Remove the Player From the Member list
        clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(activeChar.getName()));

		activeChar.sendPacket(new SystemMessage(SystemMessage.CLAN_MEMBERSHIP_TERMINATED));

		activeChar.setClan(null);
		activeChar.setTitle("");
		activeChar.setDeleteClanCurTime();
			
		activeChar.broadcastUserInfo();
		
		// disable clan tab
		activeChar.sendPacket(new PledgeShowMemberListDeleteAll());
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__26_REQUESTWITHDRAWALPLEDGE;
	}
}
