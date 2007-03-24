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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.TradeOtherAdd;
import net.sf.l2j.gameserver.serverpackets.TradeOwnAdd;

/**
 * This class ...
 *
 * @version $Revision: 1.5.2.2.2.5 $ $Date: 2005/03/27 15:29:29 $
 */
public class AddTradeItem extends ClientBasePacket
{
    private static final String _C__16_ADDTRADEITEM = "[C] 16 AddTradeItem";
    private static Logger _log = Logger.getLogger(AddTradeItem.class.getName());

    @SuppressWarnings("unused")
    private final int _tradeId;
    private final int _objectId;
    private final int _count;

    public AddTradeItem(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _tradeId = readD();
        _objectId = readD();
        _count = readD();
    }

    void runImpl()
    {
        L2PcInstance player = getClient().getActiveChar();
        if (player == null) return;

        TradeList trade = player.getActiveTradeList();
        if (trade == null)
        	{
            _log.warning("Character: " + player.getName() + " requested item:" + _objectId + " add without active tradelist:" + _tradeId);
        	return;
        	}

        if (trade.getPartner() == null || L2World.getInstance().findObject(trade.getPartner().getObjectId()) == null)
        {
            // Trade partner not found, cancel trade
            if (trade.getPartner() != null)
            	_log.warning("Character:" + player.getName() + " requested invalid trade object: " + _objectId);
            SystemMessage msg = new SystemMessage(SystemMessage.TARGET_IS_NOT_FOUND_IN_THE_GAME);
            player.sendPacket(msg);
            player.cancelActiveTrade();
            return;
        }

        if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN
            && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
        {
            player.sendMessage("Transactions are disable for your Access Level");
            player.cancelActiveTrade();
            return;
        }

        if (!player.validateItemManipulation(_objectId, "trade"))
		{
			player.sendPacket(new SystemMessage(SystemMessage.NOTHING_HAPPENED));
			return;
		}

        TradeList.TradeItem item = trade.addItem(_objectId, _count);
        if (item != null)
        {
        player.sendPacket(new TradeOwnAdd(item));
        trade.getPartner().sendPacket(new TradeOtherAdd(item));
        }
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType()
    {
        return _C__16_ADDTRADEITEM;
    }
}
