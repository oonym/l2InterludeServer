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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.Func;
import net.sf.l2j.gameserver.skills.Stats;

/** 
 * This Handles Disabler skills
 * @author _drunk_ 
 */
public class Disablers implements ISkillHandler
{
    protected SkillType[] _skillIds = {L2Skill.SkillType.STUN, L2Skill.SkillType.ROOT,
                                       L2Skill.SkillType.SLEEP, L2Skill.SkillType.CONFUSION,
                                       L2Skill.SkillType.AGGDAMAGE, L2Skill.SkillType.AGGREDUCE,
                                       L2Skill.SkillType.AGGREDUCE_CHAR, L2Skill.SkillType.AGGREMOVE,
                                       L2Skill.SkillType.UNBLEED, L2Skill.SkillType.UNPOISON,
                                       L2Skill.SkillType.MUTE, L2Skill.SkillType.FAKE_DEATH,
                                       L2Skill.SkillType.CONFUSE_MOB_ONLY, L2Skill.SkillType.NEGATE,
                                       L2Skill.SkillType.CANCEL, L2Skill.SkillType.PARALYZE, L2Skill.SkillType.ERASE,
                                       L2Skill.SkillType.MAGE_BANE, L2Skill.SkillType.WARRIOR_BANE, L2Skill.SkillType.BETRAY};

    protected static Logger _log = Logger.getLogger(L2Skill.class.getName());
    private  String[] _negateStats=null;
    private  float _negatePower=0.f;
    
    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        SkillType type = skill.getSkillType();

        boolean ss = false;
        boolean sps = false;
        boolean bss = false;

        L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

        if (activeChar instanceof L2PcInstance)
        {
            if (weaponInst == null)
            {
                SystemMessage sm2 = new SystemMessage(614);
                sm2.addString("You must equip a weapon before casting a spell.");
                activeChar.sendPacket(sm2);
                return;
            }
        }

        if (weaponInst != null)
        {
            if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
            {
                bss = true;
                if (skill.getId() != 1020) // vitalize
                	weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
            }
            else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
            {
                sps = true;
                if (skill.getId() != 1020) // vitalize
                	weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
            }
            else if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
            {
                ss = true;
                if (skill.getId() != 1020) // vitalize
                	weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
            }
        }
        // If there is no weapon equipped, check for an active summon.
        else if (activeChar instanceof L2Summon)
        {
            L2Summon activeSummon = (L2Summon) activeChar;

            if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
            {
                bss = true;
                activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
            }
            else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
            {
                sps = true;
                activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
            }
            else if (activeSummon.getChargedSoulShot() == L2ItemInstance.CHARGED_SOULSHOT)
            {
                ss = true;
                activeSummon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
            }
        }

        for (int index = 0; index < targets.length; index++)
        {
            // Get a target
            if (!(targets[index] instanceof L2Character)) continue;

            L2Character target = (L2Character) targets[index];
            
            if (target == null || target.isDead()) //bypass if target is null or dead
        		continue;
          
            switch (type)
            {
        		case BETRAY: 
        	 	{ 
        	 		if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, sps, bss)) 
        	 			skill.getEffects(activeChar, target); 
        	 		else 
        	 		{
        	 			SystemMessage sm = new SystemMessage(139); 
        	 			sm.addString(target.getName()); 
        	 			sm.addSkillName(skill.getId()); 
        	 			activeChar.sendPacket(sm);  
        	 		} 
        	 		break;
        	 	}
                case FAKE_DEATH:
                {
                    // stun/fakedeath is not mdef dependant, it depends on lvl difference, target CON and power of stun
                    skill.getEffects(activeChar, target);
                    break;
                }
                case STUN:
                {
                    if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, ss, false,
                                                                false))
                    {
                        skill.getEffects(activeChar, target);
                    }
                    else
                    {
                        if (activeChar instanceof L2PcInstance)
                        {
                            SystemMessage sm = new SystemMessage(139);
                            sm.addString(target.getName());
                            sm.addSkillName(skill.getId());
                            activeChar.sendPacket(sm);
                        }
                    }
                    break;
                }
                case SLEEP:
                case ROOT:
                case PARALYZE: //use same as root for now
                {
                    if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, sps,
                                                                bss))
                    {
                        skill.getEffects(activeChar, target);
                    }
                    else
                    {
                        if (activeChar instanceof L2PcInstance)
                        {
                            SystemMessage sm = new SystemMessage(139);
                            sm.addString(target.getName());
                            sm.addSkillName(skill.getId());
                            activeChar.sendPacket(sm);
                        }
                    }
                    break;
                }
                case CONFUSION:
                case MUTE:
                {
                    if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, ss,
                                                                bss))
                    {
                        // stop same type effect if avaiable
                        L2Effect[] effects = target.getAllEffects();
                        for (L2Effect e : effects)
                        {
                            if (e.getSkill().getSkillType() == type) e.exit();
                        }
                        // then restart
                        // Make above skills mdef dependant	        		
                        if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false,
                                                                    sps, bss))
                        //if(Formulas.getInstance().calcMagicAffected(activeChar, target, skill))
                        {
                            skill.getEffects(activeChar, target);
                        }
                        else
                        {
                            if (activeChar instanceof L2PcInstance)
                            {
                                SystemMessage sm = new SystemMessage(139);
                                sm.addString(target.getName());
                                sm.addSkillName(skill.getId());
                                activeChar.sendPacket(sm);
                            }
                        }
                    }
                    else
                    {
                        if (activeChar instanceof L2PcInstance)
                        {
                            SystemMessage sm = new SystemMessage(139);
                            sm.addString(target.getName());
                            sm.addSkillName(skill.getId());
                            activeChar.sendPacket(sm);
                        }
                    }
                    break;
                }
                case CONFUSE_MOB_ONLY:
                {
                    // do nothing if not on mob
                    if (target instanceof L2Attackable) skill.getEffects(activeChar, target);
                    else activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
                    break;
                }
                case AGGDAMAGE:
                {
                    if (target instanceof L2Attackable)
                        target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar,
                                                   (int) skill.getPower());
                    break;
                }
                case AGGREDUCE:
                {
                    if (target instanceof L2Attackable)
                        target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, null,
                                                   -(int) skill.getPower());
                    break;
                }
                case AGGREDUCE_CHAR:
                {
                    if (target instanceof L2Attackable)
                        target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar,
                                                   -(int) skill.getPower());
                    break;
                }
                case AGGREMOVE:
                {
                    // 1034 = repose, 1049 = requiem
                    //if (skill.getId() == 1034 || skill.getId() == 1049)
                    if ((skill.getTargetType() == L2Skill.SkillTargetType.TARGET_UNDEAD && target.isUndead())
                        || target.isAttackable())
                    {
                        target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, null,
                                                   -(int) skill.getPower());
                    }
                    break;
                }
                case UNBLEED:
                {
                    negateEffect(target,SkillType.BLEED,skill.getPower());
                    break;
                }
                case UNPOISON:
                {
                    negateEffect(target,SkillType.POISON,skill.getPower());
                    break;
                }
                case ERASE:
                {
                	if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, ss, bss))
                	{
                		L2PcInstance summonOwner = null;
                		L2Summon summonPet = null;
                		summonOwner = ((L2Summon)target).getOwner();                		
                		summonPet = summonOwner.getPet();
                		summonPet.unSummon(summonOwner);
                        SystemMessage sm = new SystemMessage(1667);
				        summonOwner.sendPacket(sm);                		 
                	}
                	break;
                }
                case MAGE_BANE:
            	{
                    for(L2Object t: targets)
                    {
                    	L2Character target1 = (L2Character) t;
                    	if (! Formulas.getInstance().calcSkillSuccess(activeChar, target1, skill, false, sps, bss))
                    		continue;
                    
                    	 L2Effect[] effects = target1.getAllEffects();
                    	 for(L2Effect e: effects)
                    	 {
                    		 for(Func f: e.getStatFuncs())
                    		 {
                    			 if(f._stat == Stats.MAGIC_ATTACK || f._stat == Stats.MAGIC_ATTACK_SPEED)
                    			 {
                    				 e.exit();
                    				 break;
                    			 }
                    		 }
                    	 }
                    	 
                    }
                    break;
            	}
                case WARRIOR_BANE:
            	{
                    for(L2Object t: targets)
                    {
                    	L2Character target1 = (L2Character) t;
                    	if (! Formulas.getInstance().calcSkillSuccess(activeChar, target1, skill, false, sps, bss))
                    		continue;
                    
                    	 L2Effect[] effects = target1.getAllEffects();
                    	 for(L2Effect e: effects)
                    	 {
                    		 for(Func f: e.getStatFuncs())
                    		 {
                    			 if(f._stat == Stats.RUN_SPEED || f._stat == Stats.POWER_ATTACK_SPEED)
                    			 {
                    				 e.exit();
                    				 break;
                    			 }
                    		 }
                    	 }
                    	 
                    }
                    break;
            	}
                case CANCEL:
                case NEGATE:
                {
                    // cancel
                    if (skill.getId() == 1056)
                    {
                    	int lvlmodifier= 52+skill.getMagicLevel()*2;
                    	if(skill.getMagicLevel()==12) lvlmodifier = (Experience.MAX_LEVEL - 1);
                    	int landrate = 90;
                    	if((target.getLevel() - lvlmodifier)>0) landrate = 90-4*(target.getLevel()-lvlmodifier);
                    	if(Rnd.get(100) < landrate)
                    	{
                    		L2Effect[] effects = target.getAllEffects();
                    		int maxfive = 5;
                    		for (L2Effect e : effects)
                    		{ 
                    			if (e.getSkill().getId() != 4082 && e.getSkill().getId() != 4215 && e.getSkill().getId() != 4515) // Cannot cancel skills 4082, 4215, 4515
                    			{
                    				if(e.getSkill().getSkillType() != SkillType.BUFF) e.exit(); //sleep, slow, surrenders etc
                    				else
                    				{
                    					int rate = 100;
                    					int level = e.getLevel();
                    					if (level > 0) rate = Integer.valueOf(150/(1 + level));
                    					if (rate > 95) rate = 95;
                    					else if (rate < 5) rate = 5;
                    					if(Rnd.get(100) < rate)	{
                    						e.exit();
                    						maxfive--;
                    						if(maxfive == 0) break;
                    					}
                    				}
                    			}
                    		}
                    	} else
                    	{
                    		SystemMessage sm = new SystemMessage(614);
                    		sm.addString(skill.getName() + " failed."); 
                    		if (activeChar instanceof L2PcInstance)
                    			activeChar.sendPacket(sm);
                    	}
                        break;
                    }
                	// all others negate type skills
                    else
                    {
                    	 _negateStats = skill.getNegateStats();
                    	 _negatePower = skill.getNegatePower();
                    	 
                    	 for (String stat : _negateStats)
                    	 {                                
                    		 stat = stat.toLowerCase().intern();
	                    	 if (stat == "buff") negateEffect(target,SkillType.BUFF,-1);
	                    	 if (stat == "debuff") negateEffect(target,SkillType.DEBUFF,-1);
	                    	 if (stat == "weakness") negateEffect(target,SkillType.WEAKNESS,-1);
	                    	 if (stat == "stun") negateEffect(target,SkillType.STUN,-1);
	                    	 if (stat == "sleep") negateEffect(target,SkillType.SLEEP,-1);
	                    	 if (stat == "confusion") negateEffect(target,SkillType.CONFUSION,-1);
	                    	 if (stat == "mute") negateEffect(target,SkillType.MUTE,-1);
	                    	 if (stat == "fear") negateEffect(target,SkillType.FEAR,-1);
	                    	 if (stat == "poison") negateEffect(target,SkillType.POISON,_negatePower);
	                    	 if (stat == "bleed") negateEffect(target,SkillType.BLEED,_negatePower);
	                    	 if (stat == "paralyze") negateEffect(target,SkillType.PARALYZE,-1);
	                    	 if (stat == "heal")
	                    	 {
	                    		 ISkillHandler Healhandler = SkillHandler.getInstance().getSkillHandler(SkillType.HEAL);
	                    		 if (Healhandler == null)
	                    		 {
		                    		 _log.severe("Couldn't find skill handler for HEAL.");
		                    		 continue;
	                    		 }
	                    		 L2Object tgts[] = new L2Object[]{target};
	                    		 try {
	                    			 Healhandler.useSkill(activeChar, skill, tgts);
	                    		 } catch (IOException e) {
	                    		 _log.log(Level.WARNING, "", e);
	                    		 }
                              }
                          }//end for                                              	               
                    }//end else
                }// end case                                    
            }//end switch
        }//end for        
        
        // self Effect :]
        L2Effect effect = activeChar.getEffect(skill.getId());        
        if (effect != null && effect.isSelfEffect())        
        {            
        	//Replace old effect with new one.            
        	effect.exit();        
        }        
        skill.getEffectsSelf(activeChar);
        
    } //end void
    
    private void negateEffect(L2Character target, SkillType type, double power) {
        L2Effect[] effects = target.getAllEffects();
        for (L2Effect e : effects)
        	if (power == -1) // if power is -1 the effect is always removed without power/lvl check ^^
        	{
        		if (e.getSkill().getSkillType() == type || (e.getSkill().getEffectType() != null && e.getSkill().getEffectType() == type))
        			e.exit();
        	}
        	else if ((e.getSkill().getSkillType() == type && e.getSkill().getPower() <= power) 
        			|| (e.getSkill().getEffectType() != null && e.getSkill().getEffectType() == type && e.getSkill().getEffectLvl() <= power))
                e.exit();
    }

    public SkillType[] getSkillIds()
    {
        return _skillIds;
    }
}
