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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.PrivateStoreManageListBuy;
import net.sf.l2j.gameserver.serverpackets.PrivateStoreMsgBuy;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public final class SetPrivateStoreListBuy extends L2GameClientPacket
{
    private static final String _C__91_SETPRIVATESTORELISTBUY = "[C] 91 SetPrivateStoreListBuy";

    //private static Logger _log = Logger.getLogger(SetPrivateStoreListBuy.class.getName());

    private int _count;
    private int[] _items; // count * 3

    @Override
	protected void readImpl()
    {
        _count = readD();
        if (_count <= 0  || _count * 12 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
        {
            _count = 0;
            _items = null;
            return;
        }
        _items = new int[_count * 3];
        for (int x = 0; x < _count; x++)
        {
            int itemId = readD(); _items[x * 3 + 0] = itemId;
            readH();//TODO analyse this
            readH();//TODO analyse this
            long cnt    = readD();
	    if (cnt > Integer.MAX_VALUE || cnt < 0)
	    {
		_count = 0; _items = null;
		return;
	    }
	    _items[x * 3 + 1] = (int)cnt;
            int price  = readD(); _items[x * 3 + 2] = price;
        }
	}

	@Override
	protected void runImpl()
	{
        L2PcInstance player = getClient().getActiveChar();
    	if (player == null) return;

        if (Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
        {
            player.sendMessage("Transactions are disable for your Access Level");
            return;
        }

        TradeList tradeList = player.getBuyList();
        tradeList.clear();

        int cost = 0;
        for (int i = 0; i < _count; i++)
        {
            int itemId = _items[i * 3 + 0];
            int count    = _items[i * 3 + 1];
            int price    = _items[i * 3 + 2];

            tradeList.addItemByItemId(itemId, count, price);
            cost += count * price;
        }

        if (_count <= 0)
        {
            player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
            return;
        }

        // Check maximum number of allowed slots for pvt shops
        if (_count > player.GetPrivateBuyStoreLimit())
        {
        	player.sendPacket(new PrivateStoreManageListBuy(player));
        	player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
            return;
        }

        // Check for available funds
        if (cost > player.getAdena() || cost <= 0)
        {
        	player.sendPacket(new PrivateStoreManageListBuy(player));
            player.sendPacket(new SystemMessage(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_MONEY));
            return;
        }

        player.sitDown();
        player.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_BUY);
		player.broadcastUserInfo();
        player.broadcastPacket(new PrivateStoreMsgBuy(player));
    }

    @Override
	public String getType()
    {
        return _C__91_SETPRIVATESTORELISTBUY;
    }

}
