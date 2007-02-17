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
package net.sf.l2j.gameserver.ai;

import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static net.sf.l2j.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.MobGroup;
import net.sf.l2j.gameserver.model.MobGroupTable;
import net.sf.l2j.gameserver.model.L2Character.AIAccessor;
import net.sf.l2j.gameserver.model.actor.instance.L2ControllableMobInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2FolkInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author littlecrow
 * AI for controllable mobs
 *
 */
public class L2ControllableMobAI extends L2AttackableAI 
{
	public static final int AI_IDLE = 1;
	public static final int AI_NORMAL = 2;
	public static final int AI_FORCEATTACK = 3;
	public static final int AI_FOLLOW = 4;
	public static final int AI_CAST = 5;
	public static final int AI_ATTACK_GROUP = 6;

	private int _alternateAI;

	private boolean _isThinking; // to prevent thinking recursively
    private boolean _isNotMoving;
    
	private L2Character _forcedTarget;
    private MobGroup _targetGroup;

	protected void thinkFollow() 
    {
		L2Attackable me = (L2Attackable)_actor;

        if (!me.isInsideRadius(getForcedTarget(), MobGroupTable.FOLLOW_RANGE, false, false)) 
        {
			int signX = (Rnd.nextInt(2) == 0) ? -1 : 1;
			int signY = (Rnd.nextInt(2) == 0) ? -1 : 1;
			int randX = Rnd.nextInt(MobGroupTable.FOLLOW_RANGE);
			int randY = Rnd.nextInt(MobGroupTable.FOLLOW_RANGE);

			moveTo(getForcedTarget().getX() + signX * randX, getForcedTarget().getY() + signY * randY, getForcedTarget().getZ());
		}
	}

	protected void onEvtThink() 
    {
		if (isThinking() || _actor.isAllSkillsDisabled())
			return;
        
		setThinking(true);
		
		try {
		    switch (getAlternateAI())
		    {
		        case AI_IDLE: 
		            if (getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
		                setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		            break;
		        case AI_FOLLOW:
		            thinkFollow();
		            break;
		        case AI_CAST:
		            thinkCast();
		            break;
		        case AI_FORCEATTACK:
		            thinkForceAttack();
		            break;
		        case AI_ATTACK_GROUP: 
		            thinkAttackGroup();
		            break;
		        default:
		            if (getIntention() == AI_INTENTION_ACTIVE)
		                thinkActive();
		            else if (getIntention() == AI_INTENTION_ATTACK)
		                thinkAttack();
		        break;
		    }
		} 
        finally {
			setThinking(false);
		}
	}

	protected void thinkCast() 
    {
		L2Attackable npc = (L2Attackable)_actor;
        
		if (getAttackTarget() == null || getAttackTarget().isAlikeDead()) 
        {
            setAttackTarget(findNextRndTarget());
			clientStopMoving(null);
		}
			
		if (getAttackTarget() == null)
			return;

		npc.setTarget(getAttackTarget());
		
		L2Skill[] skills = null;
		//double dist2 = 0;

		try {
			skills = _actor.getAllSkills();
		//	dist2 = _actor.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY());
		} 
        catch (NullPointerException e) {
			_log.warning("Encountered Null Value.");
			e.printStackTrace();
		}

		if (!_actor.isMuted()) 
        {
			int max_range = 0;
			// check distant skills
            
			for (L2Skill sk : skills) 
            {
				if (_actor.isInsideRadius(getAttackTarget(), sk.getCastRange(), false, true) //sk.getCastRange() * sk.getCastRange() >= dist2
						&& !_actor.isSkillDisabled(sk.getId())
						&& _actor.getCurrentMp() > _actor.getStat().getMpConsume(sk)) 
                {
					_accessor.doCast(sk);
					return;
				}
                
				max_range = Math.max(max_range, sk.getCastRange());
			}
            
			if (!isNotMoving())
				moveToPawn(getAttackTarget(), max_range);
            
			return;
		}
	}
	
	protected void thinkAttackGroup() 
    {
		L2Character target = getForcedTarget();
		if (target == null || target.isAlikeDead()) 
        {
			// try to get next group target
			setForcedTarget(findNextGroupTarget());
			clientStopMoving(null);
		}
		
		if (target == null)
			return;
		
		L2Skill[] skills = null;
		double dist2 = 0;
		int range = 0;
		int max_range = 0;
		
		_actor.setTarget(target);
		// as a response, we put the target in a forcedattack mode
		L2ControllableMobInstance theTarget = (L2ControllableMobInstance)target;
		L2ControllableMobAI ctrlAi = (L2ControllableMobAI)theTarget.getAI();
		ctrlAi.forceAttack(_actor);
		
		try {
			skills = _actor.getAllSkills();
			dist2 = _actor.getPlanDistanceSq(target.getX(), target.getY());
			range = _actor.getPhysicalAttackRange();
			max_range = range;
		} 
        catch (NullPointerException e) {
			_log.warning("Encountered Null Value.");
			e.printStackTrace();
		}

		if (!_actor.isMuted() && dist2 > (range + 20) * (range + 20)) 
        {
			// check distant skills
			for (L2Skill sk : skills) 
            {
                int castRange = sk.getCastRange();
                
				if (castRange * castRange >= dist2
						&& !_actor.isSkillDisabled(sk.getId())
						&& _actor.getCurrentMp() > _actor.getStat().getMpConsume(sk)) 
                {
					_accessor.doCast(sk);
					return;
				}
                
				max_range = Math.max(max_range, castRange);
			}
            
			if (!isNotMoving())
				moveToPawn(target, range);
            
			return;
		}
		_accessor.doAttack(target);
    }

	protected void thinkForceAttack() 
    {
		if (getForcedTarget() == null || getForcedTarget().isAlikeDead()) 
        {
			clientStopMoving(null);
			setIntention(AI_INTENTION_ACTIVE);
			setAlternateAI(AI_IDLE);
		}
        
		L2Skill[] skills = null;
		double dist2 = 0;
		int range = 0;
		int max_range = 0;

		try {
			_actor.setTarget(getForcedTarget());
			skills = _actor.getAllSkills();
			dist2 = _actor.getPlanDistanceSq(getForcedTarget().getX(), getForcedTarget().getY());
			range = _actor.getPhysicalAttackRange();
			max_range = range;
		} 
        catch (NullPointerException e) {
			_log.warning("Encountered Null Value.");
			e.printStackTrace();
		}

		if (!_actor.isMuted() && dist2 > (range + 20) * (range + 20))
        {
			// check distant skills
			for (L2Skill sk : skills) 
            {
                int castRange = sk.getCastRange();
                
				if (castRange * castRange >= dist2
						&& !_actor.isSkillDisabled(sk.getId())
						&& _actor.getCurrentMp() > _actor.getStat().getMpConsume(sk)) 
                {
					_accessor.doCast(sk);
					return;
				}
                
				max_range = Math.max(max_range, castRange);
			}
            
			if (!isNotMoving())
				moveToPawn(getForcedTarget(), _actor.getPhysicalAttackRange()/*range*/);
            
			return;
		}

		_accessor.doAttack(getForcedTarget());
	}

	protected void thinkAttack() 
    {
		if (getAttackTarget() == null || getAttackTarget().isAlikeDead()) 
        {
			if (getAttackTarget() != null) 
            {
				// stop hating
				L2Attackable npc = (L2Attackable) _actor;
				int hate = npc.getHating(getAttackTarget());
                
				if (hate > 0)
                {
					npc.addDamageHate(getAttackTarget(), 0, -hate);
                    npc.addBufferHate();
                }
			}
            
            setAttackTarget(null);
			clientStopAutoAttack();
			clientStopMoving(null);
			setIntention(AI_INTENTION_ACTIVE);
		} 
        else 
        {
			// notify aggression
			if (((L2NpcInstance) _actor).getFactionId() != null) 
            {
				String faction_id = ((L2NpcInstance) _actor).getFactionId();
                
				for (L2Object obj : _actor.getKnownList().getKnownObjects().values()) 
                {
                    if (!(obj instanceof L2NpcInstance))
                        continue;

                    L2NpcInstance npc = (L2NpcInstance) obj;
                    
                    if (faction_id != npc.getFactionId())
                        continue;
                    
                    if (_actor.isInsideRadius(npc, npc.getFactionRange(), false, true) 
                            && Math.abs(getAttackTarget().getZ() - npc.getZ()) < 200) 
                    {
                        npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, getAttackTarget(), 1);
                    }
				}
			}

			L2Skill[] skills = null;
			double dist2 = 0;
			int range = 0;
			int max_range = 0;

			try {
				_actor.setTarget(getAttackTarget());
				skills = _actor.getAllSkills();
				dist2 = _actor.getPlanDistanceSq(getAttackTarget().getX(), getAttackTarget().getY());
				range = _actor.getPhysicalAttackRange();
				max_range = range;
			} 
            catch (NullPointerException e) {
				_log.warning("Encountered Null Value.");
				e.printStackTrace();
			}

			if (!_actor.isMuted() && dist2 > (range + 20) * (range + 20)) 
            {
				// check distant skills
				for (L2Skill sk : skills) 
                {
                    int castRange = sk.getCastRange();
                    
					if (castRange * castRange >= dist2
							&& !_actor.isSkillDisabled(sk.getId())
							&& _actor.getCurrentMp() > _actor.getStat().getMpConsume(sk)) 
                    {
						_accessor.doCast(sk);
						return;
					}
                    
					max_range = Math.max(max_range, castRange);
				}
                
				moveToPawn(getAttackTarget(), range);
				return;
			}
			
            // Force mobs to attack anybody if confused.
			L2Character hated;
            
			if (_actor.isConfused())
				hated = getAttackTarget();
			else
				hated = findNextRndTarget();
			
			if (hated == null) 
            {
				setIntention(AI_INTENTION_ACTIVE);
				return;
			}

			if (hated != getAttackTarget())
                setAttackTarget(hated);
            
			if (!_actor.isMuted() && skills.length > 0 && Rnd.nextInt(5) == 3) 
            {
				for (L2Skill sk : skills) 
                {
                    int castRange = sk.getCastRange();
                    
					if (castRange * castRange >= dist2
							&& !_actor.isSkillDisabled(sk.getId())
							&& _actor.getCurrentMp() < _actor.getStat().getMpConsume(sk)) 
                    {
						_accessor.doCast(sk);
						return;
					}
				}
			}
            
			_accessor.doAttack(getAttackTarget());
		}
	}

	private void thinkActive() 
    {
        setAttackTarget(findNextRndTarget());
		L2Character hated;
        
		if (_actor.isConfused())
			hated = getAttackTarget();
		else
			hated = getAttackTarget();
        
		if (hated != null) 
        {
			_actor.setRunning();
			setIntention(CtrlIntention.AI_INTENTION_ATTACK, hated);
		}
        
		return;
	}

	private boolean autoAttackCondition(L2Character target) 
    {
		L2Attackable me = (L2Attackable)_actor;
        
		if (target instanceof L2FolkInstance
				|| target instanceof L2DoorInstance)
			return false;
        
		if (target.isAlikeDead()
				|| !me.isInsideRadius(target, me.getAggroRange(), false, false) 
				|| Math.abs(_actor.getZ() - target.getZ()) > 100)
			return false;

        // Check if the target isn't invulnerable
        if (target.isInvul())
            return false;
        
        // Check if the target is a L2PcInstance
        if (target instanceof L2PcInstance)
        {
            
            // Check if the target isn't in silent move mode
            if (((L2PcInstance)target).isSilentMoving())
                return false;
        }
        
        if (target instanceof L2NpcInstance)
        	return false;
        
        return me.isAggressive();
	}

	private L2Character findNextRndTarget() 
    {
        int aggroRange  = ((L2Attackable)_actor).getAggroRange();
        L2Attackable npc = (L2Attackable)_actor;
        int npcX, npcY, targetX, targetY;
        double dy, dx;
        double dblAggroRange = aggroRange*aggroRange;

		List<L2Character> potentialTarget = new FastList<L2Character>();

		for (L2Object obj : npc.getKnownList().getKnownObjects().values()) 
        {
			if (!(obj instanceof L2Character))
				continue;
            
            npcX    = npc.getX();
            npcY    = npc.getY();
            targetX = obj.getX();
            targetY = obj.getY();
            
            dx      = npcX - targetX;
            dy      = npcY - targetY;
            
            if (dx*dx + dy*dy > dblAggroRange)
                continue;
            
			L2Character target = (L2Character) obj;

            if (autoAttackCondition(target)) // check aggression
				potentialTarget.add(target);
		}

		if (potentialTarget.size() == 0) // nothing to do
			return null;
        
		// we choose a random target
		int choice = Rnd.nextInt(potentialTarget.size());
		L2Character target = potentialTarget.get(choice);

		return target;
	}

	private L2ControllableMobInstance findNextGroupTarget() 
    {
		return getGroupTarget().getRandomMob();
	}
    
	public L2ControllableMobAI(AIAccessor accessor) 
    {
		super(accessor);
		setAlternateAI(AI_IDLE);
	}

	public int getAlternateAI() 
    {
		return _alternateAI;
	}
    
	public void setAlternateAI(int _alternateai) 
    {
		_alternateAI = _alternateai;
	}

	public void forceAttack(L2Character target) 
    {
		setAlternateAI(AI_FORCEATTACK);
		setForcedTarget(target);
	}
	
	public void forceAttackGroup(MobGroup group) 
    {
		setForcedTarget(null);
		setGroupTarget(group);
		setAlternateAI(AI_ATTACK_GROUP);
	}

	public void stop() 
    {
		setAlternateAI(AI_IDLE);
		clientStopMoving(null);
	}

	public void move(int x, int y, int z) 
    {
		moveTo(x, y, z);
	}

	public void follow(L2Character target) 
    {
		setAlternateAI(AI_FOLLOW);
		setForcedTarget(target);
	}
    
    public boolean isThinking()
    {
        return _isThinking;
    }

	public boolean isNotMoving() 
    {
		return _isNotMoving;
	}

	public void setNotMoving(boolean isNotMoving) 
    {
		_isNotMoving = isNotMoving;
	}
    
    public void setThinking(boolean isThinking) 
    {
        _isThinking = isThinking;
    }
    
    private L2Character getForcedTarget()
    {
        return _forcedTarget;
    }
    
    private MobGroup getGroupTarget()
    {
        return _targetGroup;
    }
    
    private void setForcedTarget(L2Character forcedTarget)
    {
        _forcedTarget = forcedTarget;
    }
    
    private void setGroupTarget(MobGroup targetGroup)
    {
        _targetGroup = targetGroup;
    }
}