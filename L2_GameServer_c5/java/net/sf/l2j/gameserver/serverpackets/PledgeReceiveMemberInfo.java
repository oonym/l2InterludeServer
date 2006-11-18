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
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;

/**
 *
 * @author  -Wooden-
 */
public class PledgeReceiveMemberInfo extends ServerBasePacket
{
	private static final String _S__FE_3D_PLEDGERECEIVEMEMBERINFO = "[S] FE:3D PledgeReceiveMemberInfo";
	private L2ClanMember _member;

	/**
	 * @param member
	 */
	public PledgeReceiveMemberInfo(L2ClanMember member)
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
		writeH(0x3d);

		writeD(_member.getPledgeType()); 
		writeS(_member.getName());
		writeS(_member.getTitle()); // title
		writeD(_member.getPowerGrade()); // power
		
		//clan or subpledge name??
		if(_member.getPledgeType() != 0)
		{
			writeS("Subp_name");
			//writeS((_member.getClan().getSubPledge(_member.getPledgeType())).getName());
		}
		else writeS(_member.getClan().getName());
		
		writeS(_member.getApprentice()); // this member's apprentice name
	}

	/**
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_3D_PLEDGERECEIVEMEMBERINFO;
	}

}
