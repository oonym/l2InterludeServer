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
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2MerchantInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.3.2.6 $ $Date: 2005/03/27 15:29:57 $
 */
public class PrivateStoreListSell extends ServerBasePacket
{
//	private static final String _S__B4_PRIVATEBUYLISTSELL = "[S] 9b PrivateBuyListSell";
	private static final String _S__B4_PRIVATESTORELISTSELL = "[S] 9b PrivateStoreListSell";
	private L2PcInstance _storePlayer;
	private L2PcInstance _player;
	private int _playerAdena;
	private TradeList.TradeItem[] _items;
	
	// player's private shop
	public PrivateStoreListSell(L2PcInstance player, L2PcInstance storePlayer)
	{
		_player = player;
		_storePlayer = storePlayer;
	}
	
	// lease shop
	public PrivateStoreListSell(L2PcInstance player, L2MerchantInstance storeMerchant)
	{
		_player = player;
//		_storePlayer = seller;
	}
	
	final void runImpl()
	{
		_playerAdena = _player.getAdena();
//		_storePlayer.getSellList().updateItems();
		_items = _storePlayer.getSellList().getItems();
/*		} else {
			L2MerchantInstance seller = (L2MerchantInstance)_seller;
			_sellList = new FastList<TradeItem>();
			for (L2ItemInstance inst : seller.listLeaseItems()) {
				if (inst.getCount() <= 0)
					continue;
				TradeItem ti = new TradeItem();
				ti.setObjectId(inst.getObjectId());
				ti.setItemId(inst.getItemId());
				ti.setOwnersPrice(exchangeWithLeaseHolder ? 0 : inst.getPriceToSell());
				ti.setCount(inst.getCount());
				ti.setEnchantLevel(inst.getEnchantLevel());
				_sellList.add(ti);
			}
			if (exchangeWithLeaseHolder) {
				L2ItemInstance inst = seller.getLeaseAdena();
				TradeItem ti = new TradeItem();
				ti.setObjectId(inst.getObjectId());
				ti.setItemId(inst.getItemId());
				ti.setOwnersPrice(0);
				ti.setCount(inst.getCount());
				ti.setEnchantLevel(inst.getEnchantLevel());
				_sellList.add(ti);
			}
		}*/
	}
	
	final void writeImpl()
	{
		writeC(0x9b);
		writeD(_storePlayer.getObjectId());
		writeD(0x00);
		writeD(_playerAdena);
		
		writeD(_items.length);
		for (TradeList.TradeItem item : _items)
		{
			writeD(item.getItem().getType2());
			writeD(item.getObjectId());
			writeD(item.getItem().getItemId());
            writeD(item.getCount());
			writeH(0x00);
			writeH(item.getEnchant());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeD(item.getPrice()); //your price
			writeD(item.getItem().getReferencePrice()); //store price
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__B4_PRIVATESTORELISTSELL;
	}
}
