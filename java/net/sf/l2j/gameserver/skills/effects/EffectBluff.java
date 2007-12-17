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
package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeSummonInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;

/**
 * @author decad
 *
 * Implementation of the Bluff Effect
 */
final class EffectBluff extends L2Effect {

    public EffectBluff(Env env, EffectTemplate template)
    {
        super(env, template);
    }

    @Override
	public EffectType getEffectType()
    {
        return EffectType.BLUFF; //test for bluff effect
    }

    /** Notify started */
    @Override
	public void onStart()
    {
        getEffected().startFear();
    	if(getEffected() instanceof L2FolkInstance) return;
    	// if(getEffected() instanceof L2SiegeGuardInstance) return;
    	// Cannot be used on Headquarters Flag.
    	// bluff now is a PVE PVP skill
    	if(getEffected() instanceof L2NpcInstance && ((L2NpcInstance)getEffected()).getNpcId() == 35062 || getSkill().getId() != 358) return;

    	if(getEffected() instanceof L2SiegeSummonInstance)
    	{
    		return;
    	}
        int posX = getEffected().getX();
        int posY = getEffected().getY();
        int posZ = getEffected().getZ();
        int signx=-1;
        int signy=-1;
        if (getEffected().getX()>getEffector().getX())
            signx=1;
        if (getEffected().getY()>getEffector().getY())
            signy=1;

        //posX += signx*40; //distance less than melee attacks (40)
        //posY += signy*40;

        getEffected().setRunning();
        getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,
                              new L2CharPosition(posX +(signx*40),posY + (signy*40),posZ,0));
        getEffected().sendPacket(SystemMessage.sendString("You can feel Bluff's effect"));
        getEffected().setTarget(null);
        onActionTime();
    }
    @Override
	public void onExit()
    {
        getEffected().stopFear(this);

    }
    @Override
	public boolean onActionTime()
    {
        return false;
    }
}
