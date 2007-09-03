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
package net.sf.l2j.gameserver.clientpackets;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SkillList;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestSkillList extends L2GameClientPacket
{
	private static final String _C__3F_REQUESTSKILLLIST = "[C] 3F RequestSkillList";
	//private static Logger _log = Logger.getLogger(RequestSkillList.class.getName());
    @SuppressWarnings("unused")
	private int _unk1;
    @SuppressWarnings("unused")
	private int _unk2;
    @SuppressWarnings("unused")
	private int _unk3;
    
    
	@Override
	protected void readImpl()
	{
		// this is just a trigger packet. it has no content
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance cha = getClient().getActiveChar();
        
        if (cha == null)
            return;

		SkillList response = new SkillList();
		L2Skill[] skills = cha.getAllSkills();
		
		for (int i = 0; i < skills.length; i++)
		{
			L2Skill s = skills[i];
            
            if (s == null) 
                continue;
			if (s.getId() > 9000)
				continue; // fake skills to change base stats
            
			response.addSkill(s.getId(), s.getLevel(), s.isPassive());
		}
		
		sendPacket(response);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__3F_REQUESTSKILLLIST;
	}
}
