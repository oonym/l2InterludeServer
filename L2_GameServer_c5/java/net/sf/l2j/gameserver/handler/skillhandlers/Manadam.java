package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;

/**
 * Class handling the Mana damage skill
 *
 * @author slyce
 */
public class Manadam implements ISkillHandler
{
	private static SkillType[] _skillIds =
		{ SkillType.MANADAM };

	public void useSkill(@SuppressWarnings("unused")
	L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2Character target = null;

		for (int index = 0; index < targets.length; index++)
		{
			target = (L2Character) targets[index];
			boolean acted = Formulas.getInstance().calcMagicAffected(activeChar, target, skill);
			if (!acted)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.MISSED_TARGET));
			} else
			{
				double mp = (skill.getPower() > target.getCurrentMp() ? target.getCurrentMp() : skill.getPower());
				target.setCurrentMp(target.getCurrentMp() - mp);
				StatusUpdate sump = new StatusUpdate(target.getObjectId());
				sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
				target.sendPacket(sump);
				SystemMessage sm = new SystemMessage(614);
				sm.addString("You have been drained for " + (int) mp + " Mana");
				target.sendPacket(sm);
			}
		}
	}

	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}
