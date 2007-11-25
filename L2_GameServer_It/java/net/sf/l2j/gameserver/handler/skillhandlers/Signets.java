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
package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.form.ZoneCylinder;
import net.sf.l2j.gameserver.model.zone.type.L2DynamicZone;

/*
 *  Symbol skills creating a signet, temporary zone with effects and an owner
 *  (something like a L2EffectPointInstance (doesn't exist) could also be created
 *  for this but it would require more code changes)
 *
 *  Animation packets still needed
 */
public class Signets implements ISkillHandler {

    private static final SkillType[] SKILL_IDS = {SkillType.SIGNET};

    public void useSkill(L2Character activeChar, L2Skill skill, @SuppressWarnings("unused") L2Object[] targets) {

    	if (!(activeChar instanceof L2PcInstance))
            return;

        L2WorldRegion region = activeChar.getWorldRegion();
        L2DynamicZone zone = new L2DynamicZone(region, activeChar, skill);
		zone.setZone(new ZoneCylinder(activeChar.getX(),activeChar.getY(), activeChar.getZ()-200,
				activeChar.getZ()+200, skill.getSkillRadius()));

        region.addZone(zone);
        for(L2Character c : activeChar.getKnownList().getKnownCharacters())
        	zone.revalidateInZone(c);
        zone.revalidateInZone(activeChar);
    }

    public SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
