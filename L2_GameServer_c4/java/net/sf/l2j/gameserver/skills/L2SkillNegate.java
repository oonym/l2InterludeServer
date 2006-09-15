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
package net.sf.l2j.gameserver.skills;

import java.io.IOException;
import java.util.logging.Level;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillNegate extends L2Skill {

	private final String[] _negateStats;
	private final float _negatePower;
	
	public L2SkillNegate(StatsSet set) {
		super(set);
		_negateStats = set.getString("negateStats", "").split(" ");
		_negatePower = set.getFloat("negatePower", 0.f);
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets) {
	
		for (int index = 0; index < targets.length; index++)
		{
			if (!(targets[index] instanceof L2Character))
				continue;
			L2Character target = (L2Character)targets[index];

			for (String stat : _negateStats)
			{
				stat = stat.toLowerCase().intern();
				if (stat == "buff") negateBuffs(target);
				if (stat == "debuff") negateDebuffs(target);
				if (stat == "weakness") negateWeakness(target);
				if (stat == "stun") negateStun(target);
				if (stat == "sleep") negateSleep(target);
				if (stat == "confusion") negateConfusion(target);
				if (stat == "mute") negateMute(target);
				if (stat == "fear") negateFere(target);
				if (stat == "poison") negatePoison(target, _negatePower);
				if (stat == "bleed") negateBleed(target, _negatePower);
				if (stat == "paralyze") negateParalyze(target, _negatePower);
				if (stat == "heal")
				{
					ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(SkillType.HEAL);
					if (handler == null)
					{
						_log.severe("Couldn't find skill handler for HEAL.");
						continue;
					}
					L2Object tgts[] = new L2Object[]{target};
					try {
						handler.useSkill(caster, this, tgts);
					} catch (IOException e) {
						_log.log(Level.WARNING, "", e);
					}
				}
			}			
		}
	}

	private void negateBleed(L2Character target, double power) {
		L2Effect[] effects = target.getAllEffects();
		for (L2Effect e : effects)
			if (e.getSkill().getSkillType() == SkillType.BLEED &&
					e.getSkill().getPower() <= power)
				e.exit();
	}

	private void negatePoison(L2Character target, double power) {
		L2Effect[] effects = target.getAllEffects();
		for (L2Effect e : effects)
			if (e.getSkill().getSkillType() == SkillType.POISON &&
					e.getSkill().getPower() <= power)
				e.exit();
	}

	private void negateFere(L2Character target) {
		L2Effect[] effects = target.getAllEffects();
		for (L2Effect e : effects)
			if (e.getSkill().getSkillType() == SkillType.FEAR)
				e.exit();
	}

	private void negateMute(L2Character target) {
		L2Effect[] effects = target.getAllEffects();
		for (L2Effect e : effects)
			if (e.getSkill().getSkillType() == SkillType.MUTE)
				e.exit();
	}

	private void negateConfusion(L2Character target) {
		L2Effect[] effects = target.getAllEffects();
		for (L2Effect e : effects)
			if (e.getSkill().getSkillType() == SkillType.CONFUSION)
				e.exit();
	}

	private void negateSleep(L2Character target) {
		L2Effect[] effects = target.getAllEffects();
		for (L2Effect e : effects)
			if (e.getSkill().getSkillType() == SkillType.SLEEP)
				e.exit();
	}

	private void negateStun(L2Character target) 
	{
		L2Effect[] effects = target.getAllEffects();
		for (L2Effect e : effects)
			if (e.getSkill().getSkillType() == SkillType.STUN)
				e.exit();
	}
	
	private void negateParalyze(L2Character target, double power) {
		L2Effect[] effects = target.getAllEffects();
		for (L2Effect e : effects)
			if (e.getSkill().getSkillType() == SkillType.PARALYZE &&
					e.getSkill().getPower() <= power)
				e.exit();
	}
	
	private void negateBuffs(L2Character target) {
		L2Effect[] effects = target.getAllEffects();
		for (L2Effect e : effects)
			if (e.getSkill().getSkillType() == SkillType.BUFF)
				e.exit();
	}
	
	private void negateDebuffs(L2Character target) {
		L2Effect[] effects = target.getAllEffects();
		for (L2Effect e : effects)
			if (e.getSkill().getSkillType() == SkillType.DEBUFF
					|| e.getSkill().getSkillType() == SkillType.AGGDEBUFF)
				e.exit();
	}
	
	private void negateWeakness(L2Character target) {
		L2Effect[] effects = target.getAllEffects();
		for (L2Effect e : effects)
			if (e.getSkill().getSkillType() == SkillType.WEAKNESS)
				e.exit();
	}
}	