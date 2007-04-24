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
 * 
 * sample
 * 
 * 0000: 8e  d8 a8 10 48  10 04 00 00  01 00 00 00  01 00 00    ....H...........
 * 0010: 00  d8 a8 10 48                                     ....H
 *  
 *
 * format   ddddd d
 * 
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class MagicSkillLaunched extends L2GameServerPacket
{
	private static final String _S__8E_MAGICSKILLLAUNCHED = "[S] 8E MagicSkillLaunched";
	private int _chaId;
	private int _skillId;
	private int _skillLevel;
	private int _dat2;
	private int _targetId;
	
	public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel, L2Character target)
	{
		_chaId = cha.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_dat2 = 1;
		_targetId = target.getObjectId();
	}
	
	public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel)
	{
		_chaId = cha.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_dat2 = 1;
		_targetId = cha.getTargetId();
	}
	
	protected final void writeImpl()
	{
		writeC(0x76);
		writeD(_chaId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_dat2);  // failed or not
		writeD(_targetId);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__8E_MAGICSKILLLAUNCHED;
	}

}
