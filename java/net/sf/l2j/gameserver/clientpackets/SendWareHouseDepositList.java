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

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.ClanWarehouse;
import net.sf.l2j.gameserver.model.ItemContainer;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2EtcItemType;

/**
 * This class ...
 *
 * 31  SendWareHouseDepositList  cd (dd)
 *
 * @version $Revision: 1.3.4.5 $ $Date: 2005/04/11 10:06:09 $
 */
public final class SendWareHouseDepositList extends L2GameClientPacket
{
	private static final String _C__31_SENDWAREHOUSEDEPOSITLIST = "[C] 31 SendWareHouseDepositList";
	private static Logger _log = Logger.getLogger(SendWareHouseDepositList.class.getName());

	private int _count;
	private int[] _items;

	@Override
	protected void readImpl()
	{
		_count = readD();

		// check packet list size
		if (_count < 0  || _count * 8 > _buf.remaining() || _count > Config.MAX_ITEM_IN_PACKET)
		{
            _count = 0;
		}

		_items = new int[_count * 2];
		for (int i=0; i < _count; i++)
		{
			int objectId = readD();
			_items[i * 2 + 0] = objectId;
			long cnt = readD();
			if (cnt > Integer.MAX_VALUE || cnt < 0)
			{
			    _count = 0;
			    _items = null;
			    return;
			}
			_items[i * 2 + 1] = (int) cnt;
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
        if (player == null) return;
        ItemContainer warehouse = player.getActiveWarehouse();
        if (warehouse == null) return;
		L2FolkInstance manager = player.getLastFolkNPC();
        if ((manager == null || !player.isInsideRadius(manager, L2NpcInstance.INTERACTION_DISTANCE, false, false)) && !player.isGM()) return;

        if ((warehouse instanceof ClanWarehouse) && Config.GM_DISABLE_TRANSACTION && player.getAccessLevel() >= Config.GM_TRANSACTION_MIN && player.getAccessLevel() <= Config.GM_TRANSACTION_MAX)
        {
            player.sendMessage("Transactions are disable for your Access Level");
            return;
        }

        // Alt game - Karma punishment
        if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && player.getKarma() > 0) return;

        // Freight price from config or normal price per item slot (30)
		int fee = _count * 30;
		int currentAdena = player.getAdena();
        int slots = 0;

		for (int i = 0; i < _count; i++)
		{
			int objectId = _items[i * 2 + 0];
			int count = _items[i * 2 + 1];

			// Check validity of requested item
			L2ItemInstance item = player.checkItemManipulation(objectId, count, "deposit");
            if (item == null)
            {
            	_log.warning("Error depositing a warehouse object for char "+player.getName()+" (validity check)");
            	_items[i * 2 + 0] = 0;
            	_items[i * 2 + 1] = 0;
            	continue;
            }

            if ((warehouse instanceof ClanWarehouse) && !item.isTradeable() || item.getItemType() == L2EtcItemType.QUEST) return;
            // Calculate needed adena and slots
            if (item.getItemId() == 57) currentAdena -= count;
            if (!item.isStackable()) slots += count;
            else if (warehouse.getItemByItemId(item.getItemId()) == null) slots++;
		}

        // Item Max Limit Check
        if (!warehouse.validateCapacity(slots))
        {
            sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
            return;
        }

        // Check if enough adena and charge the fee
        if (currentAdena < fee || !player.reduceAdena("Warehouse", fee, player.getLastFolkNPC(), false))
        {
            sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
            return;
        }

        // Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (int i = 0; i < _count; i++)
		{
			int objectId = _items[i * 2 + 0];
			int count = _items[i * 2 + 1];

			// check for an invalid item
			if (objectId == 0 && count == 0) continue;

			L2ItemInstance oldItem = player.getInventory().getItemByObjectId(objectId);
            if (oldItem == null)
            {
                _log.warning("Error depositing a warehouse object for char "+player.getName()+" (olditem == null)");
                continue;
            }

            int itemId = oldItem.getItemId();

            if ((itemId >= 6611 && itemId <= 6621) || itemId == 6842)
                continue;

			L2ItemInstance newItem = player.getInventory().transferItem("Warehouse", objectId, count, warehouse, player, player.getLastFolkNPC());
            if (newItem == null)
            {
            	_log.warning("Error depositing a warehouse object for char "+player.getName()+" (newitem == null)");
            	continue;
            }

            if (playerIU != null)
            {
            	if (oldItem.getCount() > 0 && oldItem != newItem) playerIU.addModifiedItem(oldItem);
            	else playerIU.addRemovedItem(oldItem);
            }
		}

        // Send updated item list to the player
		if (playerIU != null) player.sendPacket(playerIU);
		else player.sendPacket(new ItemList(player, false));

		// Update current load status on player
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__31_SENDWAREHOUSEDEPOSITLIST;
	}
}
