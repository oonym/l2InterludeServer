/* This program is free software; you can redistribute it and/or modify
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

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author  L2Fortress
 *
 * visual: "Crop sale"
 */

public class ShowSellCropList extends L2GameServerPacket
{
	private static final String _S__FE_21_SHOWSELLCROPLIST = "[S] FE:21 ShowSellCropList";

	@SuppressWarnings("unused")
	private L2PcInstance _player;
	private byte _manorId = 1;

	public ShowSellCropList(L2PcInstance player, byte manorId)
	{
		_player = player;
		_manorId = manorId;
	}

	@Override
	public void runImpl()
	{
	// no long running
	}

	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x21);

		writeD(_manorId); // manor id? 1 gludio, 2 dion, 3 giran...
		writeD(1); // size?

		writeD(0); // ?
		writeD(5078); // crop id
		writeD(31); // level ?
		writeC(1); // ???
		writeD(1871); // reward 1 id ?
		writeC(1); // ???
		writeD(4042); // reward 2 id ?

		writeD(_manorId); // territory = manor(castle) id 1 gludio, 2 dion, 3 giran...
		writeD(3); // remaining
		writeD(10); // buy price
		writeC(1); // reward
		writeD(20); // my crops
	}

	@Override
	public String getType()
	{
		return _S__FE_21_SHOWSELLCROPLIST;
	}

}
