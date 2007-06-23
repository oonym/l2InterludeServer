package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2ChestInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.util.Rnd;

public class Unlock implements ISkillHandler
{
	//private static Logger _log = Logger.getLogger(Unlock.class.getName()); 
	private static final SkillType[] SKILL_IDS = {SkillType.UNLOCK};

	public void useSkill(L2Character activeChar, L2Skill skill, @SuppressWarnings("unused")
	L2Object[] targets)
	{
		L2Object[] targetList = skill.getTargetList(activeChar);
		
		if (targetList == null) return;

		for (int index = 0; index < targetList.length; index++)
		{
			L2Object target = targetList[index];

			boolean success = Formulas.getInstance().calculateUnlockChance(skill);
			if (target instanceof L2DoorInstance)
			{
				L2DoorInstance door = (L2DoorInstance) target;
				if (!door.isUnlockable())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.UNABLE_TO_UNLOCK_DOOR));
					activeChar.sendPacket(new ActionFailed());
					return;
				}

				if (success && (door.getOpen() == 1))
				{
					door.openMe();
					door.onOpen();
					SystemMessage systemmessage = new SystemMessage(SystemMessage.S1_S2);

					systemmessage.addString("Unlock the door!");
					activeChar.sendPacket(systemmessage);
				}
				else
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.FAILED_TO_UNLOCK_DOOR));
				}
			}
			else if (target instanceof L2ChestInstance)
			{
				L2ChestInstance chest = (L2ChestInstance) targetList[index];
				if (chest.getCurrentHp() <= 0 || chest.isOpen())
				{
					activeChar.sendPacket(new ActionFailed());
					return;
				}
				else
				{
					int chestChance = 0;
					int chestGroup = 0;
					int chestTrapLimit = 0;

					if (chest.getLevel() > 60) chestGroup = 4;
					else if (chest.getLevel() > 40) chestGroup = 3;
					else if (chest.getLevel() > 30) chestGroup = 2;
					else chestGroup = 1;

					switch (chestGroup)
					{
						case 1:
						{
							if (skill.getLevel() > 10) chestChance = 100;
							else if (skill.getLevel() >= 3) chestChance = 50;
							else if (skill.getLevel() == 2) chestChance = 45;
							else if (skill.getLevel() == 1) chestChance = 40;

							chestTrapLimit = 10;
						}
							break;
						case 2:
						{
							if (skill.getLevel() > 12) chestChance = 100;
							else if (skill.getLevel() >= 7) chestChance = 50;
							else if (skill.getLevel() == 6) chestChance = 45;
							else if (skill.getLevel() == 5) chestChance = 40;
							else if (skill.getLevel() == 4) chestChance = 35;
							else if (skill.getLevel() == 3) chestChance = 30;

							chestTrapLimit = 30;
						}
							break;
						case 3:
						{
							if (skill.getLevel() >= 14) chestChance = 50;
							else if (skill.getLevel() == 13) chestChance = 45;
							else if (skill.getLevel() == 12) chestChance = 40;
							else if (skill.getLevel() == 11) chestChance = 35;
							else if (skill.getLevel() == 10) chestChance = 30;
							else if (skill.getLevel() == 9) chestChance = 25;
							else if (skill.getLevel() == 8) chestChance = 20;
							else if (skill.getLevel() == 7) chestChance = 15;
							else if (skill.getLevel() == 6) chestChance = 10;

							chestTrapLimit = 50;
						}
							break;
						case 4:
						{
							if (skill.getLevel() >= 14) chestChance = 50;
							else if (skill.getLevel() == 13) chestChance = 45;
							else if (skill.getLevel() == 12) chestChance = 40;
							else if (skill.getLevel() == 11) chestChance = 35;

							chestTrapLimit = 80;
						}
							break;
					}
					chest.setOpen();
					if (chestChance == 0)
					{
						activeChar.sendPacket(SystemMessage.sendString("Too hard to open for you.."));
						activeChar.sendPacket(new ActionFailed());
						chest.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
						return;
					}

					if (Rnd.get(120) < chestChance)
					{
						activeChar.sendPacket(SystemMessage.sendString("You open the chest!"));
						
						chest.setSpecialDrop();
						chest.setHaveToDrop(true);
						chest.setMustRewardExpSp(false);
						chest.doItemDrop(activeChar);
						chest.doDie(activeChar);
					}
					else
					{
						activeChar.sendPacket(SystemMessage.sendString("Unlock failed!"));

						if (Rnd.get(100) < chestTrapLimit) chest.chestTrap(activeChar);
						chest.setHaveToDrop(false);
						chest.setMustRewardExpSp(false);
						chest.doDie(activeChar);
					}
				}
			}
		}
	}

	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
