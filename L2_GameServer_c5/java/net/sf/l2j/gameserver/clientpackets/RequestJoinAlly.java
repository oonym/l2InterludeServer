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
		SystemMessage sm;
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
    	    return;
		if(activeChar.getClan() == null)
    	    return;
		if(activeChar.getAllyId() == 0)
    	    return;
		L2PcInstance target = (L2PcInstance) L2World.getInstance().findObject(_id);
    	if(target == null){
		    //Target is not found in the game.
		    sm = new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME);
		    activeChar.sendPacket(sm);
		    return;
		}
    		if(target.getClan() == null)
        	    return;
		if (!activeChar.isClanLeader() || activeChar.getClanId() != activeChar.getAllyId()){
		    //feature available only for alliance leader
		    sm = new SystemMessage(SystemMessage.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
		    activeChar.sendPacket(sm);
        	    return;
		}
		if (activeChar == target){
		    //you don't need invite yourself
            //FIXME: i dont think it's the good msg ID 502 is SystemMessage.ALREADY_JOINED_ALLIANCE
		    sm = new SystemMessage(502);
		    activeChar.sendPacket(sm);
        	    return;
		}
		if(target.getAllyId() == activeChar.getAllyId()){
	    //same alliance - no need to invite
	    sm = new SystemMessage(SystemMessage.S1_CLAN_ALREADY_MEMBER_OF_S2_ALLIANCE);
	    sm.addString(activeChar.getClan().getName());
	    sm.addString(activeChar.getClan().getAllyName());
	    activeChar.sendPacket(sm);
    	    return;
		}
		if(!target.isClanLeader()){
		    //not clan leader
		    sm = new SystemMessage(9);
		    sm.addString(target.getName());
		    activeChar.sendPacket(sm);
        	    return;
		}

        if (target.isProcessingRequest())
        {
		    sm = new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
		    activeChar.sendPacket(sm);
		    return;
        }
	    activeChar.onTransactionRequest(target);
	    //leader of alliance request an alliance.
	    sm = new SystemMessage(SystemMessage.S2_ALLIANCE_LEADER_OF_S1_REQUESTED_ALLIANCE);
	    sm.addString(activeChar.getClan().getAllyName());
	    sm.addString(activeChar.getName());
	    target.sendPacket(sm);
	    AskJoinAlly aja = new AskJoinAlly(activeChar.getObjectId(), activeChar.getClan().getAllyName());
	    target.sendPacket(aja);
	    return;
	}
	
	
	public String getType()
	{
		return _C__82_REQUESTJOINALLY;
	}
}

