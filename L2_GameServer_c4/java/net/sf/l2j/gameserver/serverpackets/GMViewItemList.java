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

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;


/**
 * 
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class GMViewItemList extends ServerBasePacket
{
	//private static Logger _log = Logger.getLogger(GMViewItemList.class.getName());
	private static final String _S__AD_GMVIEWITEMLIST = "[S] 94 GMViewItemList";
	private L2ItemInstance[] _items;
	private L2PcInstance _cha;
	private String _playerName;



	public GMViewItemList(L2PcInstance cha)
	{
		_items = cha.getInventory().getItems();
		_playerName = cha.getName();
		_cha = cha;
	}	


	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
		writeC(0x94);
		writeS(_playerName);
		writeD(_cha.GetInventoryLimit()); // inventory limit
		writeH(0x01); // show window ??
		writeH(_items.length);
		
	for (L2ItemInstance temp : _items)
	{
		if (temp == null || temp.getItem() == null)
			continue;
            
		writeH(temp.getItem().getType1()); // item type1
		writeD(temp.getObjectId());
		writeD(temp.getItemId());
		writeD(temp.getCount());
		writeH(temp.getItem().getType2());  // item type2
		writeH(temp.getCustomType1());  // item type3
		writeH(temp.isEquipped() ? 0x01 : 0x00);
		writeD(temp.getItem().getBodyPart());   // rev 415  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
		//writeH(temp.getItem().getBodyPart());   // rev 377  slot    0006-lr.ear  0008-neck  0030-lr.finger  0040-head  0080-??  0100-l.hand  0200-gloves  0400-chest  0800-pants  1000-feet  2000-??  4000-r.hand  8000-r.hand
		writeH(temp.getEnchantLevel()); // enchant level
		//race tickets
		writeH(temp.getCustomType2());  // item type3
        }
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__AD_GMVIEWITEMLIST;
	}
}
