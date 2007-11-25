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
package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.skills.Env;

/**
 *
 * @author -Nemesiss-
 */
public class EffectTargetMe extends L2Effect
{
	public EffectTargetMe(Env env, EffectTemplate template)
    {
        super(env, template);
    }

    @Override
	public EffectType getEffectType()
    {
        return EffectType.TARGET_ME;
    }

    /** Notify started */
    @Override
	public void onStart() {
    	//Should only work on PC?
    	if (getEffected() instanceof L2PcInstance)
    	{
    		getEffected().setTarget(getEffector());
    		MyTargetSelected my = new MyTargetSelected(getEffector().getObjectId(), 0);
    		getEffected().sendPacket(my);
    	}
    }

    /** Notify exited */
    @Override
	public void onExit() {
        //nothing
    }

    @Override
	public boolean onActionTime()
    {
    	//nothing
        return false;
    }
}
