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

import net.sf.l2j.gameserver.model.L2ClanMember;

/**
 * Format : (ch) dSd
 * @author  -Wooden-
 */
public class PledgeReceivePowerInfo extends ServerBasePacket
{
	private static final String _S__FE_3C_PLEDGERECEIVEPOWERINFO = "[S] FE:3D PledgeReceivePowerInfo";
	private L2ClanMember _member;

	/**
	 * @param member
	 */
	public PledgeReceivePowerInfo(L2ClanMember member)
	{
		_member = member;
	}

	/**
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#runImpl()
	 */
	@Override
	void runImpl()
	{
	}

	/**
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	void writeImpl()
	{
		writeC(0xfe);
		writeH(0x3c);

		writeD(_member.getPowerGrade()); //power grade
		writeS(_member.getName());
		writeD(_member.getClan().getRankPrivs(_member.getPowerGrade())); //privileges
	}

	/**
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_3C_PLEDGERECEIVEPOWERINFO;
	}

}
