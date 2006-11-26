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

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.lib.Log;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2BossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2RaidBossInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.templates.L2WeaponType;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.7.2.16 $ $Date: 2005/04/06 16:13:49 $
 */

public class Pdam implements ISkillHandler
{
    // all the items ids that this handler knowns
    private static Logger _log = Logger.getLogger(Pdam.class.getName());

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
     */
    private static SkillType[] _skillIds = {SkillType.PDAM,
    /* SkillType.CHARGEDAM */
    };

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IItemHandler#useItem(net.sf.l2j.gameserver.model.L2PcInstance, net.sf.l2j.gameserver.model.L2ItemInstance)
     */
    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        if (activeChar.isAlikeDead()) return;

        int damage = 0;

        if (Config.DEBUG)
            if (Config.DEBUG) _log.fine("Begin Skill processing in Pdam.java " + skill.getSkillType());

        for (int index = 0; index < targets.length; index++)
        {
            L2Character target = (L2Character) targets[index];
            L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
            if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance
                && target.isAlikeDead() && target.isFakeDeath())
            {
                target.stopFakeDeath(null);
            }
            else if (target.isAlikeDead()) continue;

            boolean dual = activeChar.isUsingDualWeapon();
            boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);
            boolean crit = Formulas.getInstance().calcCrit(activeChar.getCriticalHit(target, skill));
            boolean soul = (weapon != null
                && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT && weapon.getItemType() != L2WeaponType.DAGGER);

            if (!crit && (skill.getCondition() & L2Skill.COND_CRIT) != 0) damage = 0;
            else damage = (int) Formulas.getInstance().calcPhysDam(activeChar, target, skill, shld,
                                                                   crit, dual, soul);

            if (damage > 5000 && activeChar instanceof L2PcInstance)
            {
                String name = "";
                if (target instanceof L2RaidBossInstance) name = "RaidBoss ";
                if (target instanceof L2NpcInstance)
                    name += target.getName() + "(" + ((L2NpcInstance) target).getTemplate().npcId
                        + ")";
                if (target instanceof L2PcInstance)
                    name = target.getName() + "(" + target.getObjectId() + ") ";
                name += target.getLevel() + " lvl";
                Log.add(activeChar.getName() + "(" + activeChar.getObjectId() + ") "
                    + activeChar.getLevel() + " lvl did damage " + damage + " with skill "
                    + skill.getName() + "(" + skill.getId() + ") to " + name, "damage_pdam");
            }
            // Why are we trying to reduce the current target HP here?
            // Why not inside the below "if" condition, after the effects processing as it should be?
            // It doesn't seem to make sense for me. I'm moving this line inside the "if" condition, right after the effects processing...
            // [changed by nexus - 2006-08-15]
            //target.reduceCurrentHp(damage, activeChar);
            if (soul && weapon != null) weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);

            if (damage > 0)
            {
                if (activeChar instanceof L2PcInstance)
                {
                    if (crit) activeChar.sendPacket(new SystemMessage(SystemMessage.CRITICAL_HIT));
                    
                    SystemMessage sm = new SystemMessage(SystemMessage.YOU_DID_S1_DMG);
                    sm.addNumber(damage);
                    activeChar.sendPacket(sm);
                }

                if (skill.hasEffects())
                {
                    // activate attacked effects, if any
                    target.stopEffect(skill.getId());
                    if (target.getEffect(skill.getId()) != null)
                        target.removeEffect(target.getEffect(skill.getId()));
                    if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, false, false))
                    {
                        skill.getEffects(activeChar, target);
                        
                        SystemMessage sm = new SystemMessage(SystemMessage.YOU_FEEL_S1_EFFECT);
                        sm.addSkillName(skill.getId());
                        target.sendPacket(sm);
                    }
                    else
                    {
                        SystemMessage sm = new SystemMessage(139);
                        sm.addString(target.getName());
                        sm.addSkillName(skill.getId());
                        activeChar.sendPacket(sm);
                    }
                }
                
                if((skill.getId() == 30 || skill.getId() == 321 || skill.getId() == 263 || skill.getId() == 409) 
                		&& !target.isRaid()) //TODO check if lethal effect should affect Bosses
                {
                	//Skill is Backstab, deadly or blinding
                	double Hpdam = 0;
                	int chance = 0;
                	chance = Rnd.get(100);
                	// Lethal Strike effect chance
                	if (chance <= 5) 
                	{
                		// Msg: Lethal Strike
                		SystemMessage sm = new SystemMessage(1667);
                        activeChar.sendPacket(sm);
                		
                        if (chance < 2) 
                        {       
                        	// If is a monster damage is (CurrentHp - 1) so HP = 1
                                if (target instanceof L2NpcInstance)
                                    target.reduceCurrentHp(target.getCurrentHp()-1, activeChar);
                           // If is a active player set his HP and CP to 1
                    			else if (target instanceof L2PcInstance)
                    			{
                    				target.setCurrentHp(1);
                    				target.setCurrentCp(1);
                    			}           	        	             	        	
                		} 			
                		else //Chance <=5 and >=2
                		{                                    
                     		   if (target instanceof L2PcInstance) 
                     		   {
                    			   // Set CP to 1
                    			   target.setCurrentCp(1);
                    			   // if skill is Backstab remove HP
                               	  if (skill.getId() == 30)
                               	  {
                            	    if (damage >= target.getCurrentHp()) 
                            	    {
                            		    target.setCurrentHp(0);
                            		    target.doDie(activeChar);
                            	    }
                            	    else 
                            	    {
                            		    Hpdam = (target.getCurrentHp() - damage);
                            		    target.setCurrentHp(Hpdam);
                            	    }
                                  }
                               	  //Else skill is deadly or blinding and remove first 1 CP and after HP
                               	  else 
                               		  target.reduceCurrentHp(damage, activeChar);                		   
                    		   }
                     		   // If is a monster remove first damage and after 50% of current hp
                     		   else if (target instanceof L2MonsterInstance)
                     		   {
                     			  target.reduceCurrentHp(damage, activeChar);
                     			  target.reduceCurrentHp(target.getCurrentHp()/2, activeChar);
                     		   }                			
                	     }
                        // Lethal Strike was succefful!
                		sm = new SystemMessage(1668);
                        activeChar.sendPacket(sm);
                	}               	
                	else //Chance > 5 (Not Lethal effect)
                    	if (skill.getId() == 30)
                    	{
                    		if (target instanceof L2PcInstance)
                    		{
                    	       if (damage >= target.getCurrentHp()) 
                    	       {
                    		      target.setCurrentHp(0);
                    		      target.doDie(activeChar);
                    	       }
                    	       else 
                    	       {
                    		      Hpdam = (target.getCurrentHp() - damage);
                    		      target.setCurrentHp(Hpdam);
                    	       }
                            }
                    		else
                    			target.reduceCurrentHp(damage, activeChar);
                    	}
                    	else
                    		target.reduceCurrentHp(damage, activeChar); 
                }
                else target.reduceCurrentHp(damage, activeChar);
            }
            else activeChar.sendPacket(new SystemMessage(SystemMessage.ATTACK_FAILED));
        }
        
        if (skill.isSuicideAttack())
        {
        	activeChar.doDie(null);
        	activeChar.setCurrentHp(0);
        }
    }

    public SkillType[] getSkillIds()
    {
        return _skillIds;
    }
}
