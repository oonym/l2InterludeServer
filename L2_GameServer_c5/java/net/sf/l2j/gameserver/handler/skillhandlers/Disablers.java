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

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Attackable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;

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
                                       L2Skill.SkillType.CANCEL, L2Skill.SkillType.PARALYZE};

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
                weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
            }
            else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
            {
                sps = true;
                weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
            }
            else if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
            {
                ss = true;
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

            switch (type)
            {
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
                    L2Effect[] effects = target.getAllEffects();
                    for (L2Effect e : effects)
                    {
                        if (e.getSkill().getSkillType() == SkillType.BLEED
                            && skill.getPower() >= e.getSkill().getPower())
                        {
                            e.exit();
                            break;
                        }
                    }
                    break;
                }
                case UNPOISON:
                {
                    L2Effect[] effects = target.getAllEffects();
                    for (L2Effect e : effects)
                    {
                        if (e.getSkill().getSkillType() == SkillType.POISON
                            && skill.getPower() >= e.getSkill().getPower())
                        {
                            e.exit();
                            break;
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
                    	if(skill.getMagicLevel()==12) lvlmodifier = 78;
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
                    					if (level > 0) rate = (int) 150/(1 + level);
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
                    // purify
                    else if (skill.getId() == 1018)
                    {
                        L2Effect[] effects = target.getAllEffects();
                        for (L2Effect e : effects)
                        {
                            if (skill.getLevel() == 1)
                            {
                                if (e.getSkill().getSkillType() == SkillType.BLEED
                                    && e.getSkill().getLevel() == 1
                                    || e.getSkill().getSkillType() == SkillType.POISON
                                    && e.getSkill().getLevel() == 1
                                    || e.getSkill().getSkillType() == SkillType.PARALYZE
                                    && e.getSkill().getLevel() == 1)
                                {
                                    e.exit();
                                }
                            }
                            else
                            {
                                if (e.getSkill().getSkillType() == SkillType.BLEED
                                    || e.getSkill().getSkillType() == SkillType.POISON
                                    || e.getSkill().getSkillType() == SkillType.PARALYZE)
                                {
                                    e.exit();
                                }
                            }
                        }
                    }
                    // vitalize
                    else if (skill.getId() == 1020)
                    {
                    	// Temporary support fix for this skill: adds healing power
                    	if(!target.isDead()) 
                    	{
                    		int heal_amount = 115 + skill.getLevel()*5; 
                    		target.setCurrentHp(heal_amount + target.getCurrentHp()); 
                    		target.setLastHealAmount(heal_amount);            
                    		StatusUpdate su = new StatusUpdate(target.getObjectId());
                    		su.addAttribute(StatusUpdate.CUR_HP, (int)target.getCurrentHp());
                    		target.sendPacket(su);
                    	}
                    	L2Effect[] effects = target.getAllEffects();
                        for (L2Effect e : effects)
                        {
                            if (skill.getLevel() < 6)
                            {
                                if (e.getSkill().getSkillType() == SkillType.BLEED
                                    && e.getSkill().getLevel() == 1
                                    || e.getSkill().getSkillType() == SkillType.POISON
                                    && e.getSkill().getLevel() == 1)
                                {
                                    e.exit();
                                }
                            }
                            else
                            {
                                if (e.getSkill().getSkillType() == SkillType.BLEED
                                    || e.getSkill().getSkillType() == SkillType.POISON)
                                {
                                    e.exit();
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public SkillType[] getSkillIds()
    {
        return _skillIds;
    }
}
