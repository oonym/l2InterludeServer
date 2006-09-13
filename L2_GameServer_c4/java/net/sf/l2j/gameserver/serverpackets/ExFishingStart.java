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

import net.sf.l2j.gameserver.model.L2Character;

/**
 * Format (ch)ddddd
 * @author -Wooden-
 *
 */
public class ExFishingStart extends ServerBasePacket
{
	private static final String _S__FE_13_EXFISHINGSTART = "[S] FE:13 ExFishingStart";
	private L2Character _character;
	private int _x,_y,_z;	
	
	public ExFishingStart(L2Character character, int x, int y,int z)
	{
		_character = character;
		_x = x;
		_y = y;
		_z = z;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#runImpl()
	 */
	@Override
	void runImpl()
	{
		// TODO Auto-generated method stub		
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	void writeImpl()
	{
		writeC(0xfe);
		writeH(0x13);
		writeD(_character.getObjectId());
		writeD(0x02); //fish speed??
		writeD(_x); // x poisson
		writeD(_y); // y poisson
		writeD(_z); // z poisson
				
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_13_EXFISHINGSTART;
	}
	
}