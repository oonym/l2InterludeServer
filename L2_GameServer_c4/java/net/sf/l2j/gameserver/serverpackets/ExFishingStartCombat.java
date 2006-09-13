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
 * Format (ch)dddcc
 * @author -Wooden-
 *
 */
public class ExFishingStartCombat extends ServerBasePacket
{
	private static final String _S__FE_15_EXFISHINGSTARTCOMBAT = "[S] FE:15 ExFishingStartCombat";
	L2Character _character;
	int _time,_hp;
	boolean _Newbie;
	
	public ExFishingStartCombat(L2Character character, int time, int hp, boolean Newbie)
	{
		_character = character;
		_time = time;
		_hp = hp;
		_Newbie = Newbie;
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
		writeH(0x15);
		
		writeD(_character.getObjectId());
		writeD(_time);
		writeD(_hp); 
		writeC(_Newbie ? 0 : 1); //Not Sure but works
		writeC(_Newbie ? 0 : 1); //Not Sure but works		
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_15_EXFISHINGSTARTCOMBAT;
	}
	
}