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
 * Format (ch)dddcccd
 * d: cahacter oid
 * d: time left
 * d: fish hp
 * c:
 * c:
 * c: 00 if fish gets damage 02 if fish regens
 * d:
 * @author -Wooden-
 *
 */
public class ExFishingHpRegen extends ServerBasePacket
{
	private static final String _S__FE_16_EXFISHINGHPREGEN = "[S] FE:16 ExFishingHpRegen";
	private L2Character _character;
	private int _time, _fishHP, _HPmode, _Anim, _GoodUse, _Penalty;	
	
	public ExFishingHpRegen(L2Character character, int time, int fishHP, int HPmode, int anim, int GoodUse, int penalty)
	{
		_character = character;
		_time = time;
		_fishHP = fishHP;
		_HPmode = HPmode;
		_Anim = anim;
		_GoodUse = GoodUse;
		_Penalty = penalty;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#runImpl()
	 */
	@Override
	void runImpl()
	{		
		
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	void writeImpl()
	{
		writeC(0xfe);
		writeH(0x16);
		
		writeD(_character.getObjectId());
		writeD(_time);
		writeD(_fishHP);
		writeC(_HPmode); //HP raise -1 stop - 0
		writeC(_GoodUse); //its 1 when skill is correct used
		writeC(_Anim); //Anim - 1 realing,2 - pumping, 0 - none
		writeD(_Penalty); //Penalty		
		
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_16_EXFISHINGHPREGEN;
	}
	
}