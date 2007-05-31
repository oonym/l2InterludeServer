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

import net.sf.l2j.util.Rnd;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.actor.instance.L2FeedableBeastInstance;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Point3D;

// While a tamed beast behaves a lot like a pet (ingame) and does have
// an owner, in all other aspects, it acts like a mob.  
// In addition, it can be fed in order to increase its duration.
// This class handles the running tasks, AI, and feed of the mob.
// The (mostly optional) AI on feeding the spawn is handled by the datapack ai script 
public final class L2TamedBeastInstance extends L2FeedableBeastInstance
{
	private int _foodSkillId;
	private final int MAX_DISTANCE_FROM_HOME = 30000;	
	private final int MAX_DISTANCE_FROM_OWNER = 2000;	
	private final int MAX_DURATION = 1200000;	// 20 minutes
	private final int DURATION_CHECK_INTERVAL = 60000;	// 1 minute
	private final int BUFF_INTERVAL = 5000;	// 5 seconds
	private int _remainingTime = MAX_DURATION;
	private int _homeX, _homeY, _homeZ;
	private L2PcInstance _owner;
	private Future _buffTask = null;
	private Future _durationCheckTask = null;
	
	public L2TamedBeastInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setHome(this);
	}
	
    public L2TamedBeastInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, int foodSkillId, int x, int y, int z)
    {
        super(objectId, template);

        setCurrentHp(getMaxHp());
        setCurrentMp(getMaxMp());
        setOwner(owner);
        setFoodType(foodSkillId);
        setHome(x,y,z);
        this.spawnMe(x, y, z);
    }
    
    public void onReceiveFood()
    {
    	// Eating food extends the duration, to a max of 
    	_remainingTime = _remainingTime + 60000;
    	if (_remainingTime > MAX_DURATION)
    		_remainingTime = MAX_DURATION;
    }
    
    public Point3D getHome()
    {
    	return new Point3D(_homeX, _homeY, _homeZ);
    }
    
    public void setHome(int x, int y, int z)
    {
    	_homeX = x;
    	_homeY = y;
    	_homeZ = z;
    }
    
    public void setHome(L2Character c)
    {
    	setHome(c.getX(), c.getY(), c.getZ());
    }
    
    public int getRemainingTime()
    {
    	return _remainingTime;
    }

    public void setRemainingTime(int duration)
    {
    	_remainingTime = duration;
    }

    public int getFoodType()
    {
    	return _foodSkillId;
    }
    
    public void setFoodType(int foodItemId)
    {
    	if (foodItemId > 0)
    	{
        	_foodSkillId = foodItemId;

        	// start the duration checks
	    	// start the buff tasks 
        	if (_durationCheckTask != null)
        		_durationCheckTask.cancel(true);
        	_durationCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckDuration(this), DURATION_CHECK_INTERVAL, DURATION_CHECK_INTERVAL);
    	}
    }
    
    public void doDie(L2Character killer)
    {
    	super.doDie(killer);
    	
    	getAI().stopFollow();
    	_buffTask.cancel(true);
    	_durationCheckTask.cancel(true);
    	
    	// clean up variables
    	if (_owner != null)
    		_owner.setTrainedBeast(null);
    	_buffTask = null;
    	_durationCheckTask = null;
    	_owner = null;
    	_foodSkillId = 0;
    	_remainingTime = 0;
    }
    
    public L2PcInstance getOwner()
    {
    	return _owner;
    }
    
    public void setOwner(L2PcInstance owner)
    {
    	if (owner != null)
    	{
        	_owner = owner;
	    	this.setTitle(owner.getName());
	    	// broadcast the new title
	    	broadcastPacket( new NpcInfo(this, owner) );

	    	owner.setTrainedBeast(this);
	    	
	    	// always and automatically follow the owner.
	    	getAI().startFollow(_owner,100);
	    	
	    	// instead of calculating this value each time, let's get this now and pass it on
	    	int totalBuffsAvailable = 0;
    		for (L2Skill skill: getTemplate().getSkills().values())
    		{
	    		// if the skill is a buff, check if the owner has it already [  owner.getEffect(L2Skill skill) ]
    			if (skill.getSkillType() == L2Skill.SkillType.BUFF)
    				totalBuffsAvailable++;
    		}

	    	// start the buff tasks 
    		if (_buffTask !=null)
    			_buffTask.cancel(true);
    		_buffTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckOwnerBuffs(this, totalBuffsAvailable), BUFF_INTERVAL, BUFF_INTERVAL);
    	}
    	else
    	{
    		doDespawn();	// despawn if no owner
    	}
    }
    
    public boolean isTooFarFromHome()
    {
    	return !(this.isInsideRadius(_homeX, _homeY, _homeZ, MAX_DISTANCE_FROM_HOME, true, true)); 
    }
    
    public void doDespawn()
    {
    	// stop running tasks
    	getAI().stopFollow();
    	_buffTask.cancel(true);
    	_durationCheckTask.cancel(true);
    	stopHpMpRegeneration();
    	
    	// clean up variables
    	if (_owner != null)
    		_owner.setTrainedBeast(null);
    	setTarget(null);
    	_buffTask = null;
    	_durationCheckTask = null;
    	_owner = null;
    	_foodSkillId = 0;
    	_remainingTime = 0;
    	
    	// remove the spawn
    	onDecay();
    }
    
    // notification triggered by the owner when the owner is attacked.
    // tamed mobs will heal/recharge or debuff the enemy according to their skills
    public void onOwnerGotAttacked(L2Character attacker)
    {
		if ((_owner == null) || (_owner.isDead()) || (_owner.isOnline()==0) || !_owner.isInsideRadius(this, MAX_DISTANCE_FROM_OWNER, true, true))
		{
			doDespawn();
			return;
		}
		float HPRatio = ((float) _owner.getCurrentHp())/_owner.getMaxHp();
		
		System.out.println("Owner got attacked.  Current HPRatio: "+HPRatio);
		
		// if the owner has a lot of HP, then debuff the enemy with a random debuff among the available skills
		// use of more than one debuff at this moment is acceptable
		if (HPRatio >= 0.8 )
		{
			FastMap<Integer, L2Skill> skills = (FastMap<Integer, L2Skill>) getTemplate().getSkills();
			
			for (L2Skill skill: skills.values())
			{
	    		// if the skill is a debuff, check if the attacker has it already [  attacker.getEffect(L2Skill skill) ]
				if ((skill.getSkillType() == L2Skill.SkillType.DEBUFF) && Rnd.get(3) < 1 && (attacker.getEffect(skill) != null))
				{
					setTarget(attacker);
					doCast(skill);
				}
			}
		}
		// for HP levels between 80% and 50%, do not react to attack events (so that MP can regenerate a bit)
		// for lower HP ranges, heal or recharge the owner with 1 skill use per attack.  
		else if (HPRatio < 0.5)
		{
			int chance = 1;
			if (HPRatio < 0.25 )
				chance = 2;
			
	    	// if the owner has a lot of HP, then debuff the enemy with a random debuff among the available skills
			FastMap<Integer, L2Skill> skills = (FastMap<Integer, L2Skill>) getTemplate().getSkills();
			
			for (L2Skill skill: skills.values())
			{
	    		// if the skill is a buff, check if the owner has it already [  owner.getEffect(L2Skill skill) ]
				if ( (Rnd.get(5) < chance) && ((skill.getSkillType() == L2Skill.SkillType.HEAL) || 
						(skill.getSkillType() == L2Skill.SkillType.HOT) ||
						(skill.getSkillType() == L2Skill.SkillType.BALANCE_LIFE) ||
						(skill.getSkillType() == L2Skill.SkillType.HEAL_PERCENT) ||
						(skill.getSkillType() == L2Skill.SkillType.HEAL_STATIC) ||
						(skill.getSkillType() == L2Skill.SkillType.COMBATPOINTHEAL) ||
						(skill.getSkillType() == L2Skill.SkillType.CPHOT) ||
						(skill.getSkillType() == L2Skill.SkillType.MANAHEAL) ||
						(skill.getSkillType() == L2Skill.SkillType.MANA_BY_LEVEL) ||
						(skill.getSkillType() == L2Skill.SkillType.MANAHEAL_PERCENT) ||
						(skill.getSkillType() == L2Skill.SkillType.MANARECHARGE) ||
						(skill.getSkillType() == L2Skill.SkillType.MPHOT) )
					)
				{
					setTarget(_owner);
					doCast(skill);
					return;
				}
			}
		}
		getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _owner);
    }	
			
    
    private class CheckDuration implements Runnable
    {
    	private L2TamedBeastInstance _tamedBeast;
    	
    	CheckDuration(L2TamedBeastInstance tamedBeast)
    	{
    		_tamedBeast = tamedBeast;
    	}
    	
    	public void run()
    	{
    		int remainingTime = _tamedBeast.getRemainingTime() - DURATION_CHECK_INTERVAL;
    		if (_tamedBeast.isTooFarFromHome())
    			remainingTime = -1;
    		
    		if (remainingTime <= 0)
    			_tamedBeast.doDespawn();
    		else
    			_tamedBeast.setRemainingTime(remainingTime);
    	}
    }

    private class CheckOwnerBuffs implements Runnable
    {
    	private L2TamedBeastInstance _tamedBeast;
    	private int _numBuffs;
    	
    	CheckOwnerBuffs(L2TamedBeastInstance tamedBeast, int numBuffs)
    	{
    		_tamedBeast = tamedBeast;
    		_numBuffs = numBuffs;
    	}
    	
    	public void run()
    	{
    		L2PcInstance owner = _tamedBeast.getOwner();
    		
    		// if the owner is missing (null), is dead, or is too far away, then unsummon
    		if ((owner == null) || (owner.isDead()) || (owner.isOnline()==0) || !owner.isInsideRadius(_tamedBeast, MAX_DISTANCE_FROM_OWNER, true, true))
    		{
    			_tamedBeast.doDespawn();
    			return;
    		}
    		
    		int totalBuffsOnOwner = 0;
    		int i=0;
    		int rand = Rnd.get(_numBuffs);
    		L2Skill buffToGive = null;
    		
	    	// get this npc's skills:  getSkills()
    		FastMap<Integer, L2Skill> skills = (FastMap<Integer, L2Skill>) _tamedBeast.getTemplate().getSkills();
    		
    		for (L2Skill skill: skills.values())
    		{
	    		// if the skill is a buff, check if the owner has it already [  owner.getEffect(L2Skill skill) ]
    			if (skill.getSkillType() == L2Skill.SkillType.BUFF)
    			{
    				if (i==rand)
    					buffToGive = skill;
    				i++;
    				if(owner.getEffect(skill) != null)
    				{
    					totalBuffsOnOwner++;
    				}
    			}
    		}
			// if the owner has less than 60% of this beast's available buff, cast a random buff
			if (_numBuffs*2/3 > totalBuffsOnOwner)
			{
				_tamedBeast.setTarget(owner);
				_tamedBeast.doCast(buffToGive);
			}
    		getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _tamedBeast.getOwner());
    	}
    }
    
}
