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

import java.util.Random;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SkillTreeTable;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/** 
 * @author _drunk_ 
 * 
 * TODO To change the template for this generated type comment go to 
 * Window - Preferences - Java - Code Style - Code Templates 
 */ 
public class Spoil implements ISkillHandler 
{ 
    //private static Logger _log = Logger.getLogger(Spoil.class.getName()); 
    protected SkillType[] _skillIds = {SkillType.SPOIL};
    
    public void useSkill(L2Character activeChar, L2Skill skill, @SuppressWarnings("unused") L2Object[] targets)
    { 
        if (!(activeChar instanceof L2PcInstance))
			return;

		L2Object[] targetList = skill.getTargetList(activeChar);
        
        if (targetList == null)
        {
            return;
        }

		for (int index = 0; index < targetList.length; index++) 
		{
			if (!(targetList[index] instanceof L2MonsterInstance))
				continue;
			
			L2MonsterInstance target = (L2MonsterInstance) targetList[index];

			// SPOIL SYSTEM by ootz0rz
			boolean spoil = false;
			if ( target.isDead() == false ) 
			{
				boolean canSpoilLowerLevel = Config.CAN_SPOIL_LOWER_LEVEL_MOBS;
				boolean canDelevelToSpoil =  Config.CAN_DELEVEL_AND_SPOIL_MOBS;			
				boolean wasNegative = false;
				boolean delevelledPlayer = false;
				double x = 0;
				double lvlDifference = 0;
				double rateOfSpoil = 0;
				double 	maxLevelDifference = 	Config.MAXIMUM_PLAYER_AND_MOB_LEVEL_DIFFERENCE;
				double 	baseRateOfSpoil = 		Config.BASE_SPOIL_RATE;
				double 	minimumRateOfSpoil = 	Config.MINIMUM_SPOIL_RATE;
				double 	spoilLevelLimit = 		Config.SPOIL_LEVEL_DIFFERENCE_LIMIT;
				int 	maxLevelOfSpoil = 		Config.LAST_LEVEL_SPOIL_IS_LEARNED;
				int mobLevel = target.getLevel();
				int playerLevel = activeChar.getLevel();
				int spoilCheck = 0;
				int spoilLevelANDPlayerLevelDifference = 0;
				
				Random rand = new Random();
				
				lvlDifference = mobLevel - playerLevel;
				
				// rate of spoil
				x = lvlDifference / maxLevelDifference;
				if ( x < 0 ) 
				{
					x *= -1;
					wasNegative = true;
				}
				if ( canSpoilLowerLevel == true && wasNegative ) 
					rateOfSpoil = baseRateOfSpoil;
				else 
				{
					if ( x > 1 ) x = 1;
					rateOfSpoil = baseRateOfSpoil * ( 1 - Math.pow(x, 2) );
					if ( rateOfSpoil < minimumRateOfSpoil ) rateOfSpoil = minimumRateOfSpoil;
				}
				
				// now check for level of spoil skill
				if ( playerLevel > maxLevelOfSpoil ) 
					playerLevel = maxLevelOfSpoil;
				
				L2PcInstance activecharPC = L2World.getInstance().getPlayer(activeChar.getName());
				
				ClassId classID = activecharPC.getClassId();
				spoilCheck = SkillTreeTable.getInstance().getMinSkillLevel(skill.getId(), classID, skill.getLevel());
				
				spoilLevelANDPlayerLevelDifference = playerLevel - spoilCheck;
				
				if ( spoilLevelANDPlayerLevelDifference < 0 ) 
				{
					if ( canDelevelToSpoil ) 
						delevelledPlayer = true;
					else 
					{
						spoilLevelANDPlayerLevelDifference *= -1;
						delevelledPlayer = false;
					}
				}
				
				if ( spoilLevelANDPlayerLevelDifference > spoilLevelLimit && !delevelledPlayer ) 
				{
					spoilLevelANDPlayerLevelDifference /= spoilLevelLimit;
					
					if ( spoilLevelANDPlayerLevelDifference < 0 ) 
						spoilLevelANDPlayerLevelDifference *= -1;
					if ( spoilLevelANDPlayerLevelDifference > 1 ) 
						spoilLevelANDPlayerLevelDifference = 1;
					
					double rateOfSpoilMultiplier = (baseRateOfSpoil * ( 1 - Math.pow(spoilLevelANDPlayerLevelDifference, 2) ))/100;
					rateOfSpoil *= rateOfSpoilMultiplier;
					if ( rateOfSpoil < minimumRateOfSpoil ) 
						rateOfSpoil = minimumRateOfSpoil;
				}
								
				// now do randomization
				int randomSuccessRate = rand.nextInt(99);			
				if ( randomSuccessRate < rateOfSpoil )
				{ 
					spoil = true;
					target.setIsSpoiledBy(activeChar.getObjectId());
				}

				if (spoil) 
				{
					target.setSpoil(true);
					activeChar.sendPacket(new SystemMessage(SystemMessage.SPOIL_SUCCESS));
				} 
				else 
				{
					SystemMessage sm = new SystemMessage(614);
					sm.addString("Spoil failed");
					activeChar.sendPacket(sm);
				}
				//target.addDamageHate(activeChar, 0, (int)skill.getPower());
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
    } 
    
    public SkillType[] getSkillIds() 
    { 
        return _skillIds; 
    } 
}
