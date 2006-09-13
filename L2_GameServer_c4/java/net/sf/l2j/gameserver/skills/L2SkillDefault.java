package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillDefault extends L2Skill {

	public L2SkillDefault(StatsSet set) {
		super(set);
	}

	public void useSkill(L2Character caster, @SuppressWarnings("unused") L2Object[] targets) {
		caster.sendPacket(new ActionFailed());
		SystemMessage sm = new SystemMessage(614);
		sm.addString("Skill not implemented.  Skill ID: " + getId() + " " + getSkillType());
		caster.sendPacket(sm);
	}
	
}
