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

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;

/**
 * Sdh(h dddhh [dhhh] d)
 * Sdh ddddd ddddd ddddd ddddd
 * @version $Revision: 1.1.2.1.2.4 $ $Date: 2005/03/29 23:15:10 $
 */
public class GMViewWarehouseWithdrawList extends L2GameServerPacket
{
	private static Logger _log = Logger.getLogger(GMViewWarehouseWithdrawList.class.getName());
	private static final String _S__95_GMViewWarehouseWithdrawList = "[S] 95 GMViewWarehouseWithdrawList";
	private L2ItemInstance[] _items;
	@SuppressWarnings("unused")
    private String _playerName;
    private L2PcInstance _cha;
    private int _money;

	public GMViewWarehouseWithdrawList(L2PcInstance cha)
	{
        _cha  = cha;
        
		_items = _cha.getWarehouse().getItems();
		_playerName = _cha.getName();
        _money = _cha.getAdena();
        
        if (Config.DEBUG)
        {
	        for (L2ItemInstance item : _items)
	            _log.fine("item:" + item.getItem().getName() +
	                     " type1:" + item.getItem().getType1() + " type2:" + item.getItem().getType2());
        }
	}
	
	protected final void writeImpl()
	{
		try
		{
	        writeC(0x95);
	        writeS(_playerName);
	//	    writeH(0x01); // private WH
	        writeD(_money);
	        writeH(_items.length);
			
	        for (L2ItemInstance item : _items)
			{
	            writeH(item.getItem().getType1()); // item type1 //unconfirmed, works
	            writeD(item.getObjectId()); //unconfirmed, works
	            writeD(item.getItemId()); //unconfirmed, works
	            writeD(item.getCount()); //unconfirmed, works
                writeH(item.getItem().getType2()); // item type2 //unconfirmed, works
	            writeH(0x00);  // ?
	            switch(item.getItem().getType2())
	            {
	                case L2Item.TYPE2_WEAPON:
			            writeD(item.getItem().getBodyPart()); // ?
			            writeH(item.getEnchantLevel()); // enchant level -confirmed
			            writeH(((L2Weapon)item.getItem()).getSoulShotCount());  // ?
			            writeH(((L2Weapon)item.getItem()).getSpiritShotCount());  // ?
			            break;
	                case L2Item.TYPE2_SHIELD_ARMOR: 
	                case L2Item.TYPE2_ACCESSORY:
			            writeD(item.getItem().getBodyPart()); // ?
			            writeH(item.getEnchantLevel()); // enchant level -confirmed
			            writeH(0x00);  // ?
			            writeH(0x00);  // ?
			            break;
	            }
	            writeD(item.getObjectId()); // item id - confimed
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__95_GMViewWarehouseWithdrawList;
	}
}
