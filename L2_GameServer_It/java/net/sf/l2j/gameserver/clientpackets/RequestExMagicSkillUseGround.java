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
package net.sf.l2j.gameserver.clientpackets;

/**
 * Fromat:(ch) dddddc
 * @author  -Wooden-
 */
public final class RequestExMagicSkillUseGround extends L2GameClientPacket
{
	private static final String _C__D0_2F_REQUESTEXMAGICSKILLUSEGROUND = "[C] D0:2F RequestExMagicSkillUseGround";
	private int _unk;
	private int _unk2;
	private int _unk3;
	private int _unk4;
	private int _unk5;
	private int _unk6;

	/**
	 * @param buf
	 * @param client
	 */
	@Override
	protected void readImpl()
	{
		_unk = readD();
		_unk2 = readD();
		_unk3 = readD();
		_unk4 = readD();
		_unk5 = readD();
		_unk6 = readC();
	}

	/**
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		// TODO
		System.out.println("C6: RequestExMagicSkillUseGround. unk: "+_unk);
		System.out.println("C6: RequestExMagicSkillUseGround. unk2: "+_unk2);
		System.out.println("C6: RequestExMagicSkillUseGround. unk3: "+_unk3);
		System.out.println("C6: RequestExMagicSkillUseGround. unk4: "+_unk4);
		System.out.println("C6: RequestExMagicSkillUseGround. unk5: "+_unk5);
		System.out.println("C6: RequestExMagicSkillUseGround. unk6: "+_unk6);

	}

	/**
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_2F_REQUESTEXMAGICSKILLUSEGROUND;
	}

}
