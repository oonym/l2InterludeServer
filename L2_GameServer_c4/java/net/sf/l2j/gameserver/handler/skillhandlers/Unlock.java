package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;

public class Unlock implements ISkillHandler
{
    //private static Logger _log = Logger.getLogger(Unlock.class.getName()); 
    protected SkillType[] _skillIds = {SkillType.UNLOCK};	

 public void useSkill(L2Character activeChar, L2Skill skill, @SuppressWarnings("unused") L2Object[] targets)
 {
	 	L2Object[] targetList = skill.getTargetList(activeChar);

		for (int index = 0; index < targetList.length; index++) 
		{
			L2DoorInstance target = (L2DoorInstance) targetList[index];
			
			boolean success = Formulas.getInstance().calculateUnlockChance(skill);
			
			if (!target.isUnlockable())
			{
				 SystemMessage systemmessage = new SystemMessage(SystemMessage.S1_S2);
		         systemmessage.addString("You cannot unlock this door!");
		         activeChar.sendPacket(systemmessage);	
		         return;
			}
			
			if (success && (target.getOpen() == 1))
			{
				target.openMe();
				target.onOpen();
		    }
			else
			{
		         SystemMessage systemmessage = new SystemMessage(SystemMessage.S1_S2);
		         systemmessage.addString("UnLock failed!");
		         activeChar.sendPacket(systemmessage);				
			}
		}
 }

 public SkillType[] getSkillIds() 
 { 
     return _skillIds; 
 }
}
