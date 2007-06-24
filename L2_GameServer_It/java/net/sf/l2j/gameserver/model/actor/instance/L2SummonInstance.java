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
package net.sf.l2j.gameserver.model.actor.instance;

import java.util.concurrent.Future;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2SummonInstance extends L2Summon
{
    protected static final Logger log = Logger.getLogger(L2SummonInstance.class.getName());

    private float _expPenalty = 0; // exp decrease multiplier (i.e. 0.3 (= 30%) for shadow)
    private int _itemConsumeId;
    private int _itemConsumeCount;
    private int _itemConsumeSteps;
    private int _itemConsumeStepsElapsed;

    private Future _summonLifeTask;

    private static final int SUMMON_LIFETIME_INTERVAL = 1200000; // 20 minutes

    public L2SummonInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
    {
        super(objectId, template, owner);
        setShowSummonAnimation(true);

        _itemConsumeId = 0;
        _itemConsumeCount = 0;
        _itemConsumeSteps = 0;
        _itemConsumeStepsElapsed = 0;

        if (skill != null)
        {
            _itemConsumeId = skill.getItemConsumeIdOT();
            _itemConsumeCount = skill.getItemConsumeOT();
            if (skill.getItemConsumeTime() > 0)
                _itemConsumeSteps = (SUMMON_LIFETIME_INTERVAL / skill.getItemConsumeTime()) - 1;
        }

        // When no item consume is defined task only need to check when summon life time has ended.
        // Otherwise have to destroy items from owner's inventory in order to let summon live.
        int delay = SUMMON_LIFETIME_INTERVAL / (_itemConsumeSteps + 1);

        if (Config.DEBUG && (_itemConsumeCount != 0))
            _log.warning("L2SummonInstance: Item Consume ID: " + _itemConsumeId + ", Count: "
                + _itemConsumeCount + ", Rate: " + _itemConsumeSteps + " times.");
        if (Config.DEBUG) _log.warning("L2SummonInstance: Task Delay " + (delay / 1000) + " seconds.");

        _summonLifeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(
                                                                                     new SummonLifetime(
                                                                                                        getOwner(),
                                                                                                        this),
                                                                                     delay, delay);
    }

    public final int getLevel()
    {
        return (getTemplate() != null ? getTemplate().level : 0);
    }

    public int getSummonType()
    {
        return 1;
    }

    public void setExpPenalty(float expPenalty)
    {
        _expPenalty = expPenalty;
    }

    public float getExpPenalty()
    {
        return _expPenalty;
    }

    public int getItemConsumeCount()
    {
        return _itemConsumeCount;
    }

    public int getItemConsumeId()
    {
        return _itemConsumeId;
    }

    public int getItemConsumeSteps()
    {
        return _itemConsumeSteps;
    }

    public void incItemConsumeStepsElapsed()
    {
        _itemConsumeStepsElapsed++;
    }

    public int getItemConsumeStepsElapsed()
    {
        return _itemConsumeStepsElapsed;
    }

    public void addExpAndSp(int addToExp, int addToSp)
    {
        getOwner().addExpAndSp(addToExp, addToSp);
    }

    public void reduceCurrentHp(int damage, L2Character attacker)
    {
        super.reduceCurrentHp(damage, attacker);
        SystemMessage sm = new SystemMessage(SystemMessageId.SUMMON_RECEIVED_DAMAGE_S2_BY_S1);
        if (attacker instanceof L2NpcInstance)
        {
            sm.addNpcName(((L2NpcInstance) attacker).getTemplate().npcId);
        }
        else
        {
            sm.addString(attacker.getName());
        }
        sm.addNumber(damage);
        getOwner().sendPacket(sm);
    }

    public synchronized void doDie(L2Character killer)
    {
        if (Config.DEBUG)
            _log.warning("L2SummonInstance: " + getTemplate().name + " (" + getOwner().getName()
                + ") has been killed.");

        if (_summonLifeTask != null)
        {
            _summonLifeTask.cancel(true);
            _summonLifeTask = null;
        }

        super.doDie(killer);
    }

    /*
     protected void displayHitMessage(int damage, boolean crit, boolean miss)
     {
     if (crit)
     {
     getOwner().sendPacket(new SystemMessage(SystemMessage.SUMMON_CRITICAL_HIT));
     }
     
     if (miss)
     {
     getOwner().sendPacket(new SystemMessage(SystemMessage.MISSED_TARGET));
     }
     else
     {
     SystemMessage sm = new SystemMessage(SystemMessage.SUMMON_GAVE_DAMAGE_OF_S1);
     sm.addNumber(damage);
     getOwner().sendPacket(sm);
     }
     }
     */

    static class SummonLifetime implements Runnable
    {
        private L2PcInstance _activeChar;
        private L2SummonInstance _summon;

        SummonLifetime(L2PcInstance activeChar, L2SummonInstance newpet)
        {
            _activeChar = activeChar;
            _summon = newpet;
        }

        public void run()
        {
            if (Config.DEBUG)
                log.warning("L2SummonInstance: " + _summon.getTemplate().name + " ("
                    + _activeChar.getName() + ") run task.");

            // check if life time of summon is ended
            if (_summon.getItemConsumeStepsElapsed() >= _summon.getItemConsumeSteps())
            {
                _summon.unSummon(_activeChar);
            }
            // check if owner has enought itemConsume, if requested
            else if (_summon.getItemConsumeCount() > 0
                && _summon.getItemConsumeId() != 0
                && !_summon.isDead()
                && !_summon.destroyItemByItemId("Consume", _summon.getItemConsumeId(),
                                                _summon.getItemConsumeCount(), _activeChar, true))
            {
                _summon.unSummon(_activeChar);
            }

            _summon.incItemConsumeStepsElapsed();
        }
    }

    public void unSummon(L2PcInstance owner)
    {
        if (Config.DEBUG)
            _log.warning("L2SummonInstance: " + getTemplate().name + " (" + owner.getName()
                + ") unsummoned.");

        if (_summonLifeTask != null)
        {
            _summonLifeTask.cancel(true);
            _summonLifeTask = null;
        }

        super.unSummon(owner);
    }

    public boolean destroyItem(String process, int objectId, int count, L2Object reference,
                               boolean sendMessage)
    {
        return getOwner().destroyItem(process, objectId, count, reference, sendMessage);
    }

    public boolean destroyItemByItemId(String process, int itemId, int count, L2Object reference,
                                       boolean sendMessage)
    {
        if (Config.DEBUG)
            _log.warning("L2SummonInstance: " + getTemplate().name + " (" + getOwner().getName()
                + ") consume.");

        return getOwner().destroyItemByItemId(process, itemId, count, reference, sendMessage);
    }
    
    public final void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
    {
    	if (miss) return;
        	
    	// Prevents the double spam of system messages, if the target is the owning player.
    	if (target.getObjectId() != getOwner().getObjectId())
    	{
    		if (pcrit || mcrit)
    			getOwner().sendPacket(new SystemMessage(SystemMessageId.CRITICAL_HIT_BY_SUMMONED_MOB));

    		SystemMessage sm = new SystemMessage(SystemMessageId.SUMMON_GAVE_DAMAGE_S1);
    		sm.addNumber(damage);
    		getOwner().sendPacket(sm);
        }
    }
    
}
