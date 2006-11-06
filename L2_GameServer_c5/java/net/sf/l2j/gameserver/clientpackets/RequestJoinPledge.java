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
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.AskJoinPledge;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestJoinPledge extends ClientBasePacket
{
	private static final String _C__24_REQUESTJOINPLEDGE = "[C] 24 RequestJoinPledge";
	static Logger _log = Logger.getLogger(RequestJoinPledge.class.getName());

	private final int _target;
	
	public RequestJoinPledge(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_target  = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		    return;
		
		if (_target == activeChar.getObjectId())
		{
			SystemMessage sm = new SystemMessage(SystemMessage.CANNOT_INVITE_YOURSELF);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}
		
		//is the guy leader of the clan ?
		if ((activeChar.getClanPrivileges() & L2Clan.CP_CL_JOIN_CLAN) == L2Clan.CP_CL_JOIN_CLAN)  
		{	
			L2Object object = L2World.getInstance().findObject(_target);
			if (object instanceof L2PcInstance)
			{
				L2PcInstance member = (L2PcInstance) object;
				L2Clan clan = activeChar.getClan();
				
				int limit   = 0;
                
                switch (clan.getLevel())
                {
                case 4:
                    limit   = 30;
                    break;
                case 3:
                    limit   = 25;
                    break;
                case 2:
                    limit   = 20;
                    break;
                case 1:
                    limit   = 15;
                    break;
                case 0:
                    limit   = 10;
                    break;
                default:
                    limit   = 40;
                break;
                }

				if (member.getClanId() != 0)
				{
					SystemMessage sm = new SystemMessage(SystemMessage.S1_WORKING_WITH_ANOTHER_CLAN);
					sm.addString(member.getName());
					activeChar.sendPacket(sm);
					sm = null;
					return;				
				}
				else if (member.isProcessingRequest())
				{
					SystemMessage sm = new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
					sm.addString(member.getName());
					activeChar.sendPacket(sm);
					sm = null;
					return;
				} 
				else if (clan.getMembers().length >= limit)
				{
					SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
					sm.addString("The clan is full, you cannot invite any more players."); // haven't found a sysmsg for this one
					activeChar.sendPacket(sm);
					sm = null;
					return;
				}
				else
				{
					activeChar.onTransactionRequest(member);
					
					AskJoinPledge ap = new AskJoinPledge(activeChar.getObjectId(), activeChar.getClan().getName()); 
					member.sendPacket(ap);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__24_REQUESTJOINPLEDGE;
	}
}
