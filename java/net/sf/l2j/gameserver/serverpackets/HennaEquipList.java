/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class HennaEquipList extends L2GameServerPacket
{
	private static final String _S__E2_HennaEquipList = "[S] E2 HennaEquipList";
	
	private final L2PcInstance _player;
	private final L2HennaInstance[] _hennaEquipList;
	
	public HennaEquipList(L2PcInstance player, L2HennaInstance[] hennaEquipList)
	{
		_player = player;
		_hennaEquipList = hennaEquipList;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe2);
		writeD(_player.getAdena()); // activeChar current amount of aden
		writeD(3); // available equip slot
		// writeD(10); // total amount of symbol available which depends on difference classes
		writeD(_hennaEquipList.length);
		
		for (L2HennaInstance element : _hennaEquipList)
		{
			/*
			 * Player must have at least one dye in inventory to be able to see the henna that can be applied with it.
			 */
			if ((_player.getInventory().getItemByItemId(element.getItemIdDye())) != null)
			{
				writeD(element.getSymbolId()); // symbolid
				writeD(element.getItemIdDye()); // itemid of dye
				writeD(element.getAmountDyeRequire()); // amount of dye require
				writeD(element.getPrice()); // amount of aden require
				writeD(1); // meet the requirement or not
			}
			else
			{
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
				writeD(0x00);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__E2_HennaEquipList;
	}
	
}
