package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillDrain extends L2Skill {

	private float absorbPart;
	private int   absorbAbs;
	
	public L2SkillDrain(StatsSet set) 
    {
		super(set);
		
		absorbPart = set.getFloat ("absorbPart", 0.f);
		absorbAbs  = set.getInteger("absorbAbs", 0);
	}

	public void useSkill(L2Character activeChar, L2Object[] targets)
    {
		if (activeChar.isAlikeDead())
			return;
		
		boolean ss = false;
		boolean bss = false;
		
        for(int index = 0;index < targets.length;index++)
        {
			L2Character target = (L2Character)targets[index];
			if (target.isAlikeDead() && getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)
				continue;

            if (target instanceof L2PcInstance && activeChar != target && ((L2PcInstance)target).isInvul())
                continue; // No effect on invulnerable players unless they cast it themselves.

			L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
			
			if (weaponInst != null)
			{
			    if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT) 
			    {
			        bss = true;
			        weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			    }
			    else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT) 
			    {
			        ss = true;
			        weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			    }
            }
            // If there is no weapon equipped, check for an active summon.
            else if (activeChar instanceof L2Summon)
            {
                L2Summon activeSummon = (L2Summon)activeChar;
                
                if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
                {
                    bss = true;
                    activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
                }
                else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
                {
                    ss = true;
                    activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
                }
            }

			boolean mcrit = Formulas.getInstance().calcMCrit(activeChar.getMCriticalHit(target, this));
			int damage = (int)Formulas.getInstance().calcMagicDam(
					activeChar, target, this, ss, bss, mcrit);
            
			double hpAdd = absorbAbs + absorbPart * damage;
			double hp = ((activeChar.getCurrentHp() + hpAdd) > activeChar.getMaxHp() ? activeChar.getMaxHp() : (activeChar.getCurrentHp() + hpAdd));

            activeChar.setCurrentHp(hp); 
            
			StatusUpdate suhp = new StatusUpdate(activeChar.getObjectId()); 
			suhp.addAttribute(StatusUpdate.CUR_HP, (int)hp); 
			activeChar.sendPacket(suhp);
			
            // Check to see if we should damage the target
            if (damage > 0 && (!target.isDead() || getTargetType() != SkillTargetType.TARGET_CORPSE_MOB))
            {
    			target.reduceCurrentHp(damage, activeChar);
                
                // Manage attack or cast break of the target (calculating rate, sending message...)
                if (!target.isRaid() && Formulas.getInstance().calcAtkBreak(target, damage))
                {
                    target.breakAttack();
                    target.breakCast();
                }

                if (mcrit) activeChar.sendPacket(new SystemMessage(1280));
                
    			SystemMessage sm = new SystemMessage(SystemMessage.YOU_DID_S1_DMG);
    			sm.addNumber(damage); 
    			activeChar.sendPacket(sm);
            }
            
            // Check to see if we should do the decay right after the cast
            if (target.isDead() && getTargetType() == SkillTargetType.TARGET_CORPSE_MOB && target instanceof L2NpcInstance) {
                ((L2NpcInstance)target).endDecayTask();
            }
		}
        //effect self :]
        L2Effect effect = activeChar.getEffect(getId());
        if (effect != null && effect.isSelfEffect())
        {             
            //Replace old effect with new one.
            effect.exit();
        }
        // cast self effect if any
        getEffectsSelf(activeChar);
	}
	
}
