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
 * Format: ch Sddddddddd
 * @author  KenM
 */
public class ExDuelUpdateUserInfo extends L2GameServerPacket
{
	private static final String _S__FE_4F_EXDUELUPDATEUSERINFO = "[S] FE:4F ExDuelUpdateUserInfo";
	private L2PcInstance _cha;

	public ExDuelUpdateUserInfo(L2PcInstance cha)
	{
		_cha = cha;
	}

	/**
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected
	void writeImpl()
	{
		writeC(0xfe);
		writeH(0x4f);
		writeS(_cha.getName());
		writeD(_cha.getObjectId());
		writeD(_cha.getClassId().getId());
		writeD(_cha.getLevel());
		writeD((int)_cha.getCurrentHp());
		writeD(_cha.getMaxHp());
		writeD((int)_cha.getCurrentMp());
		writeD(_cha.getMaxMp());
		writeD((int)_cha.getCurrentCp());
		writeD(_cha.getMaxCp());
	}

	/**
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_4F_EXDUELUPDATEUSERINFO;
	}

}
