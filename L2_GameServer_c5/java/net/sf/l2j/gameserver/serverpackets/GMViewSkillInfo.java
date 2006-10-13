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

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class GMViewSkillInfo extends ServerBasePacket
{
	private static final String _S__91_GMViewSkillInfo = "[S] 91 GMViewSkillInfo";
	private L2PcInstance _cha;
	private L2Skill[] _skills;
	
	public GMViewSkillInfo (L2PcInstance cha)
	{
		_cha = cha;
		_skills = _cha.getAllSkills();
		if (_skills.length == 0)
			_skills = new L2Skill[0];
	}
	
	final void runImpl()
	{
		// no long-running tasks
	}

	final void writeImpl()
	{
		writeC(0x91);
		writeS(_cha.getName());
		writeD(_skills.length);
		
		for (int i = 0; i < _skills.length; i++)
		{
			L2Skill skill = _skills[i];
			writeD(skill.isPassive() ? 1 : 0);
			writeD(skill.getLevel());
			writeD(skill.getId());
            writeC(0x00); //c5
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__91_GMViewSkillInfo;
	}
}