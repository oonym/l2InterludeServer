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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.SendTradeDone;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class AnswerTradeRequest extends ClientBasePacket{
	private static final String _C__40_ANSWERTRADEREQUEST = "[C] 40 AnswerTradeRequest";
	//private static Logger _log = Logger.getLogger(AnswerTradeRequest.class.getName());
	
	private final int _response;
	
	public AnswerTradeRequest(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_response = readD();
	}

	void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
        if (player == null) return;

        if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
        {
        	player.sendMessage("Transactions are disable for your Access Level");
            sendPacket(new ActionFailed());
            return;
        }

        
        L2PcInstance partner = player.getActiveRequester();
        if (partner == null || L2World.getInstance().findObject(partner.getObjectId()) == null)
        {
            // Trade partner not found, cancel trade
			player.sendPacket(new SendTradeDone(0));
            SystemMessage msg = new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME);
            player.sendPacket(msg);
			player.setActiveRequester(null);
            return;
        }

		if (_response == 1) player.startTrade(partner);
		else
		{
			SystemMessage msg = new SystemMessage(SystemMessage.S1_DENIED_TRADE_REQUEST);
			msg.addString(player.getName());
			partner.sendPacket(msg);
		}

		// Clears requesting status
		player.setActiveRequester(null);
		partner.onTransactionResponse();
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__40_ANSWERTRADEREQUEST;
	}
}
