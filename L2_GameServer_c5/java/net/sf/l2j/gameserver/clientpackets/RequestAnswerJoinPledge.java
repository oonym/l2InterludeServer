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
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.JoinPledge;
import net.sf.l2j.gameserver.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAdd;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestAnswerJoinPledge extends ClientBasePacket
{
	private static final String _C__25_REQUESTANSWERJOINPLEDGE = "[C] 25 RequestAnswerJoinPledge";
	//private static Logger _log = Logger.getLogger(RequestAnswerJoinPledge.class.getName());
	
	private final int _answer;
			
	public RequestAnswerJoinPledge(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_answer  = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
        
		if (activeChar == null)
		    return;
        
		L2PcInstance requestor = activeChar.getActiveRequester();
        
        if (requestor == null)
            return;
		
		if (_answer == 1)
		{
		    if (activeChar.canJoinClan())
		    {
		        //not used ?_?
		        JoinPledge jp = new JoinPledge(requestor.getClanId());
		        activeChar.sendPacket(jp);
		        
		        L2Clan clan = requestor.getClan();
		        if (clan.getSubPledgeMembersCount(requestor.tempJoinPledgeType) >= clan.getMaxNrOfMembers(requestor.tempJoinPledgeType))
		        	return; // hax
		        
		        // this also updates the database
		        clan.addClanMember(activeChar);
		        activeChar.setClan(clan);
		        activeChar.setPledgeType(requestor.tempJoinPledgeType);
		        clan.getClanMember(activeChar.getName()).setPlayerInstance(activeChar);
		        if(requestor.tempJoinPledgeType == L2Clan.SUBUNIT_ACADEMY) {
		        	activeChar.setPowerGrade(9); // adademy
		        	activeChar.setLvlJoinedAcademy(activeChar.getLevel());
		        }
		        else activeChar.setPowerGrade(5); // new member starts at 5, not confirmed
		        activeChar.setClanPrivileges(activeChar.getClan().getRankPrivs(activeChar.getPowerGrade()));
		        
		        //should be update packet only
		        //activeChar.sendPacket(new PledgeShowInfoUpdate(clan, activeChar));
		        
		        
		        SystemMessage sm = new SystemMessage(SystemMessage.ENTERED_THE_CLAN);
		        activeChar.sendPacket(sm);
		        
		        
		        sm = new SystemMessage(SystemMessage.S1_HAS_JOINED_CLAN);
		        sm.addString(activeChar.getName());
		        
		        clan.broadcastToOnlineMembers(sm);
		        sm = null;

		        PledgeShowMemberListAdd la = new PledgeShowMemberListAdd(activeChar);
		        clan.broadcastToOnlineMembers(la);
		        clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan, activeChar));
		        
		        // this activates the clan tab on the new member
		        activeChar.sendPacket(new PledgeShowMemberListAll(clan, activeChar));
		        activeChar.setDeleteClanTime(0);
		        activeChar.broadcastUserInfo();
		    } 
            else 
            {
		        requestor.sendPacket(new SystemMessage(231));
		        activeChar.sendPacket(new SystemMessage(232));
		    }
		} 
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessage.S1_REFUSED_TO_JOIN_CLAN);
			sm.addString(activeChar.getName());
			requestor.sendPacket(sm);
			sm = null;
		}
		
		activeChar.setActiveRequester(null);
		requestor.onTransactionResponse();
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__25_REQUESTANSWERJOINPLEDGE;
	}
}
