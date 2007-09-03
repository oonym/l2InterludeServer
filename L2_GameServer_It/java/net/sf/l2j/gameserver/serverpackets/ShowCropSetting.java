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

/**
 *
 * @author  L2Fortress
 */

/*
 * visual: "crop purchase"
 * 
 * format
 * dd (ddcdcdddddddcddc)
 * 
 */
public class ShowCropSetting extends L2GameServerPacket
{
	private static final String _S__FE_20_SHOWCROPSETTING = "[S] FE:20 ShowCropSetting";

	@Override
	public void runImpl()
	{
	// no long running
	}

	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x20);

		writeD(0); // manor id
		writeD(1); // size

		writeD(5078); // crop id
		writeD(31); // level
		writeC(1); // size?
		writeD(1871); // reward 1 id
		writeC(1); // size?
		writeD(4042); // reward 2 id
		writeD(2500); // buy
		writeD(200); // count
		writeD(120); // min crop price
		writeD(2000); // max crop price
		writeD(100); // today buy
		writeD(200); // today price
		writeC(1); // today reward type
		writeD(100); // next buy
		writeD(200); // next price
		writeC(2); // next reward type
	}

	@Override
	public String getType()
	{
		return _S__FE_20_SHOWCROPSETTING;
	}

}
