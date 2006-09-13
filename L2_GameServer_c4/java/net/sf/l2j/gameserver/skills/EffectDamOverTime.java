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
package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

class EffectDamOverTime extends L2Effect
{		
	public EffectDamOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	public EffectType getEffectType()
	{
		return EffectType.DMG_OVER_TIME;
	}

	public boolean onActionTime()
	{	
		if (getEffected().isDead())
			return false;
		
		double damage = calc();
        
		if (damage >= getEffected().getCurrentHp())
		{
			if (getSkill().isToggle())
			{
				SystemMessage sm = new SystemMessage(614);
				sm.addString("Not enough HP. Effect of " + getSkill().getName() + " has been removed.");
				getEffected().sendPacket(sm);
				return false;
			}
            
            // ** This is just hotfix, needs better solution **
            // 1947: "DOT skills shouldn't kill"
            // Well, some of them should ;-)
            if (getSkill().getId() != 4082) damage = getEffected().getCurrentHp() - 1;
		}

        boolean awake = !(getEffected() instanceof L2Attackable)
        					&& !(getSkill().getTargetType() == SkillTargetType.TARGET_SELF 
        							&& getSkill().isToggle());
        
        if(getSkill().getSkillType() == SkillType.POISON &&
                getEffected().getCurrentHp() > damage)
        {
            getEffected().reduceCurrentHp(damage, getEffector(),awake);
        }
        else
        {
            getEffected().reduceCurrentHp(damage, getEffector(),awake);
        }
		
		return true;
	}
}
