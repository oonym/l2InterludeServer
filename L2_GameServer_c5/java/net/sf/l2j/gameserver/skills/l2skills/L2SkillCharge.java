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
package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.effects.EffectCharge;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillCharge extends L2Skill {

	final int num_charges;
	
	public L2SkillCharge(StatsSet set) {
		super(set);
		num_charges = set.getInteger("num_charges", getLevel());
	}

	public void useSkill(L2Character caster, @SuppressWarnings("unused") L2Object[] targets) {
		if (caster.isAlikeDead())
			return;
		
		// get the effect
		EffectCharge effect = (EffectCharge) caster.getEffect(this);
		if (effect != null) {
			if (effect.num_charges < num_charges)
			{
				effect.num_charges++;
				caster.updateEffectIcons();
                SystemMessage sm = new SystemMessage(614);
                sm.addString("Charged to " + effect.num_charges);
                caster.sendPacket(sm);
			}
			return;
		}
		this.getEffects(caster, caster);
		
        //effect self :]
        L2Effect seffect = caster.getEffect(getId());
        if (effect != null && seffect.isSelfEffect())
        {             
            //Replace old effect with new one.
            seffect.exit();
        }
        // cast self effect if any
        getEffectsSelf(caster);
	}
	
}
