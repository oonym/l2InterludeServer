package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillElemental extends L2Skill {

	private final int[] seeds;
	private final boolean seed_any;
	
	public L2SkillElemental(StatsSet set) {
		super(set);
		
		seeds = new int[3];
		seeds[0] = set.getInteger("seed1",0);
		seeds[1] = set.getInteger("seed2",0);
		seeds[2] = set.getInteger("seed3",0);
		
		if (set.getInteger("seed_any",0)==1)
			seed_any = true;
		else
			seed_any = false;
	}

	public void useSkill(L2Character activeChar, L2Object[] targets) {
		if (activeChar.isAlikeDead())
			return;

		boolean ss = false;
		boolean bss = false;
		
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

		if (activeChar instanceof L2PcInstance) 
        {
			if (weaponInst == null) 
			{ 
				SystemMessage sm2 = new SystemMessage(614);  
				sm2.addString("You must equip one weapon before cast spell.");  
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
		
        for (int index = 0; index < targets.length; index++)
        {
			L2Character target = (L2Character)targets[index];
			if (target.isAlikeDead())
				continue;
			
			boolean charged = true;
			if (!seed_any){
				for (int i=0;i<seeds.length;i++){
					if (seeds[i]!=0){
						L2Effect e = target.getEffect(seeds[i]);
						if (e==null || !e.getInUse()){
							charged = false;
							break;
						}
					}
				}
			}
			else {
				charged = false;
				for (int i=0;i<seeds.length;i++){
					if (seeds[i]!=0){
						L2Effect e = target.getEffect(seeds[i]);
						if (e!=null && e.getInUse()){
							charged = true;
							break;
						}
					}
				}
			}
			if (!charged){
				SystemMessage sm = new SystemMessage(614);
				sm.addString("Target is not charged by elements."); 
				activeChar.sendPacket(sm);
				continue;
			}
			
			boolean mcrit = Formulas.getInstance().calcMCrit(activeChar.getMCriticalHit(target, this));
			
			int damage = (int)Formulas.getInstance().calcMagicDam(
					activeChar, target, this, ss, bss, mcrit);
			
			if (damage > 0)
			{
				target.reduceCurrentHp(damage, activeChar);
	
	            // Manage attack or cast break of the target (calculating rate, sending message...)
	            if (!target.isRaid() && Formulas.getInstance().calcAtkBreak(target, damage))
	            {
	                target.breakAttack();
	                target.breakCast();
	            }
	
				if (activeChar instanceof L2PcInstance) {
					SystemMessage sm = new SystemMessage(SystemMessage.YOU_DID_S1_DMG);
					sm.addNumber(damage); 
					activeChar.sendPacket(sm);
				}
			}
			
			// activate attacked effects, if any
			target.stopEffect(this.getId());
            if (target.getEffect(this.getId()) != null)
                target.removeEffect(target.getEffect(this.getId()));
            this.getEffects(activeChar, target);
		}
	}
}
