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
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2MercManagerInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MerchantInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.util.Util;

/**
 * This class ...
 * 
 * @version $Revision: 1.12.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestBuyItem extends ClientBasePacket
{
	private static final String _C__1F_REQUESTBUYITEM = "[C] 1F RequestBuyItem";
	private static Logger _log = Logger.getLogger(RequestBuyItem.class.getName());

	private final int _listId;
	private int _count;
	private int[] _items; // count*2
	/**
	 * packet type id 0x1f
	 * 
	 * sample
	 * 
	 * 1f
	 * 44 22 02 01		// list id
	 * 02 00 00 00		// items to buy
	 * 
	 * 27 07 00 00		// item id
	 * 06 00 00 00		// count
	 * 
	 * 83 06 00 00
	 * 01 00 00 00
	 * 
	 * format:		cdd (dd) 
	 * @param decrypt
	 */
	public RequestBuyItem(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_listId = readD();
		_count = readD();
		if(_count * 2 < 0) _count = 0;

		_items = new int[_count * 2];
		for (int i = 0; i < _count; i++)
		{
			int itemId   = readD(); _items[i * 2 + 0] = itemId;
			long cnt      = readD(); 
			if (cnt > Integer.MAX_VALUE || cnt < 0)
			{
			    _count=0; _items = null;
			    return;
			}
			_items[i * 2 + 1] = (int)cnt;
		}
	}

	void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
        if (player == null) return;

        // Alt game - Karma punishment
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && player.getKarma() > 0) return;

        L2Object target = player.getTarget();
        if (!player.isGM() && (target == null								// No target (ie GM Shop)
        		|| !(target instanceof L2MerchantInstance || target instanceof L2MercManagerInstance)	// Target not a merchant and not mercmanager
			    || !player.isInsideRadius(target, L2NpcInstance.INTERACTION_DISTANCE, false, false) 	// Distance is too far
			        )) return;

		L2MerchantInstance merchant = (target != null && target instanceof L2MerchantInstance) ? (L2MerchantInstance)target : null;

        if (_listId > 1000000) // lease
		{
			if (merchant.getTemplate().npcId != _listId-1000000)
			{
				sendPacket(new ActionFailed());
				return;
			}
		}

        if(_count < 1)
		{
            sendPacket(new ActionFailed());
		    return;
		}

		double taxRate = 0;
		if (merchant != null && merchant.getIsInTown()) taxRate = merchant.getCastle().getTaxRate();
		long subTotal = 0;
		int tax = 0;
		
		// Check for buylist validity and calculates summary values
		long slots = 0;
		long weight = 0;
		for (int i = 0; i < _count; i++)
		{
			int itemId = _items[i * 2 + 0];
			int count  = _items[i * 2 + 1];
			int price = 0;

            L2Item template = ItemTable.getInstance().getTemplate(itemId);
            if (template == null) continue;

            if (count > Integer.MAX_VALUE || (!template.isStackable() && count > 1))
			{
				Util.handleIllegalPlayerAction(player,"Warning!! Character "+player.getName()+" of account "+player.getAccountName()+" tried to purchase invalid quantity of items at the same time.",Config.DEFAULT_PUNISH);
				SystemMessage sm = new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED);
				sendPacket(sm);
				return;
			}

			if (_listId < 1000000)
			{
				L2TradeList list = TradeController.getInstance().getBuyList(_listId);
				price = list.getPriceForItemId(itemId);
                if (itemId >= 3960 && itemId <= 4026) price *= Config.RATE_SIEGE_GUARDS_PRICE;
				if (price < 0)
				{
					_log.warning("ERROR, no price found .. wrong buylist ??");
                    sendPacket(new ActionFailed());
                    return;
				}
			}
/* TODO: Disabled until Leaseholders are rewritten ;-)
			} else {
				L2ItemInstance li = merchant.findLeaseItem(itemId, 0);
				if (li == null || li.getCount() < cnt) {
					cnt = li.getCount();
					if (cnt <= 0) {
						items.remove(i);
						continue;
					}
					items.get(i).setCount((int)cnt);
				}
				price = li.getPriceToSell(); // lease holder sells the item
				weight = li.getItem().getWeight();
			}
*/
            subTotal += (long)count * price;	// Before tax
			tax = (int)(subTotal * taxRate);
            if (subTotal + tax > Integer.MAX_VALUE)
            {
                Util.handleIllegalPlayerAction(player,"Warning!! Character "+player.getName()+" of account "+player.getAccountName()+" tried to purchase over "+Integer.MAX_VALUE+" adena worth of goods.", Config.DEFAULT_PUNISH);
                return;
            }

            weight += (long)count * template.getWeight();
			if (!template.isStackable()) slots += count;
            else if (player.getInventory().getItemByItemId(itemId) == null) slots++;
		}

		if (weight > Integer.MAX_VALUE || weight < 0 || !player.getInventory().validateWeight((int)weight))
		{
			sendPacket(new SystemMessage(SystemMessage.WEIGHT_LIMIT_EXCEEDED));
			return;
		}

		if (slots > Integer.MAX_VALUE || slots < 0 || !player.getInventory().validateCapacity((int)slots))
		{
			sendPacket(new SystemMessage(SystemMessage.SLOTS_FULL));
			return;
		}

		// Charge buyer and add tax to castle treasury if not owned by npc clan
		if ((subTotal < 0) || !player.reduceAdena("Buy", (int)(subTotal + tax), player.getLastFolkNPC(), false))
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
			return;
		}

		if (merchant != null && merchant.getIsInTown() && merchant.getCastle().getOwnerId() > 0)
		    merchant.getCastle().addToTreasury(tax);

		// Proceed the purchase
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (int i=0; i < _count; i++)
		{
			int itemId = _items[i * 2 + 0];
			int count  = _items[i * 2 + 1];
			if (count < 0) count = 0;

			// Add item to Inventory and adjust update packet
			L2ItemInstance item = player.getInventory().addItem("Buy", itemId, count, player, merchant);
			if (playerIU != null)
			{
				if (item.getCount() > count) playerIU.addModifiedItem(item);
				else playerIU.addNewItem(item);
			}

/* TODO: Disabled until Leaseholders are rewritten ;-)
			// Update Leaseholder list
			if (_listId >= 1000000) 
			{
				L2ItemInstance li = merchant.findLeaseItem(item.getItemId(), 0);
				if (li == null)
					continue;
				if (li.getCount() < item.getCount())
					item.setCount(li.getCount());
				li.setCount(li.getCount() - item.getCount());
				li.updateDatabase();
				price = item.getCount() + li.getPriceToSell();
				L2ItemInstance la = merchant.getLeaseAdena();
				la.setCount(la.getCount() + price);

				la.updateDatabase();
				player.getInventory().addItem(item);
				item.updateDatabase();
			}
*/
		}
		// Send update packets
		if (playerIU != null) player.sendPacket(playerIU);
		else player.sendPacket(new ItemList(player, false));
		
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}
    
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__1F_REQUESTBUYITEM;
	}
}
