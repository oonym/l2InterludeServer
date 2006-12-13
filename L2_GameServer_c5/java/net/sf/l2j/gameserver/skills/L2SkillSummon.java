package net.sf.l2j.gameserver.skills;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2CubicInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SummonInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.serverpackets.PetInfo;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

public class L2SkillSummon extends L2Skill {

	private int     npcId;
	private float   expPenalty;
	private boolean isCubic;
	
	public L2SkillSummon(StatsSet set) {
		super(set);
		
		npcId      = set.getInteger("npcId", 0); // default for undescribed skills
		expPenalty = set.getFloat ("expPenalty", 0.f);
		isCubic    = set.getBool  ("isCubic", false);
	}

	public boolean checkCondition(L2Character activeChar)
	{
		if (activeChar instanceof L2PcInstance)
		{
			L2PcInstance player = (L2PcInstance)activeChar;
			if (isCubic) {
				if (getTargetType() != L2Skill.SkillTargetType.TARGET_SELF)
				{
					return true; //Player is always able to cast mass cubic skill
				}				
				int mastery = player.getSkillLevel(L2Skill.SKILL_CUBIC_MASTERY);
				if (mastery < 0)
					mastery = 0;
				int count = player.getCubics().size(); 
				if (count > mastery) {
					SystemMessage sm = new SystemMessage(614);
					sm.addString("You already have "+count+" cubic(s).");
					activeChar.sendPacket(sm);
					return false;
				}
			} else {
				if(player.getPet() != null)
				{
					SystemMessage sm = new SystemMessage(614);
					sm.addString("You already have a pet.");
					activeChar.sendPacket(sm);
					return false;
				}
			}
		}
		return super.checkCondition(activeChar, false);
	}
	
	public void useSkill(L2Character caster, L2Object[] targets) {
		if (caster.isAlikeDead() || !(caster instanceof L2PcInstance))
			return;

		L2PcInstance activeChar = (L2PcInstance) caster;

		if (npcId == 0) {
            SystemMessage sm = new SystemMessage(614);
            sm.addString("Summon skill "+getId()+" not described yet");
			activeChar.sendPacket(sm);
			return;
		}
		
		if (isCubic) {
			if (targets.length > 1) //Mass cubic skill
			{
				for (L2Object obj: targets)
				{
					if (!(obj instanceof L2PcInstance)) continue;
					L2PcInstance player = ((L2PcInstance)obj);
					int mastery = player.getSkillLevel(L2Skill.SKILL_CUBIC_MASTERY);
					if (mastery < 0)
						mastery = 0;
                    if (mastery == 0 && player.getCubics().size() > 0)
					{
						//Player can have only 1 cubic - we shuld replace old cubic with new one
                        for (L2CubicInstance c: player.getCubics().values())
                        {
                            c.stopAction();
                            c = null;
                        }
						player.getCubics().clear();
					}				
					if (player.getCubics().size() > mastery) continue;		
                    if (player.getCubics().containsKey(npcId))
                    {
                        player.sendMessage("You already have such cubic");
                    }
                    else
                    {

						player.addCubic(npcId, getLevel());
						player.broadcastUserInfo();		
                    }
				}
				return;
			}
			else //normal cubic skill
			{
				int mastery = activeChar.getSkillLevel(L2Skill.SKILL_CUBIC_MASTERY);
				if (mastery < 0)
					mastery = 0;
				if (activeChar.getCubics().size() > mastery) {
					if (Config.DEBUG)
						_log.fine("player can't summon any more cubics. ignore summon skill");
					activeChar.sendPacket(new SystemMessage(SystemMessage.CUBIC_SUMMONING_FAILED));
					return;
				}
                if (activeChar.getCubics().containsKey(npcId))
                {
                    activeChar.sendMessage("You already have such cubic");
                    return;
                }
				activeChar.addCubic(npcId, getLevel());
				activeChar.broadcastUserInfo();
				return;
			}			
		}

		if (activeChar.getPet() != null || activeChar.isMounted()) {
			if (Config.DEBUG)
				_log.fine("player has a pet already. ignore summon skill");
			return;
		}
		
		L2NpcTemplate summonTemplate = NpcTable.getInstance().getTemplate(npcId);
        L2SummonInstance summon = new L2SummonInstance(IdFactory.getInstance().getNextId(), summonTemplate, activeChar, this);
		
        summon.setTitle(activeChar.getName());
        summon.setExpPenalty(expPenalty);
        if (summon.getLevel() >= Experience.LEVEL.length)
        {
            summon.getStat().setExp(Experience.LEVEL[Experience.LEVEL.length - 1]);
            _log.warning("Summon ("+summon.getName()+") NpcID: "+summon.getNpcId()+" has a level above 75. Please rectify.");
        }
        else
        {
            summon.getStat().setExp(Experience.LEVEL[(summon.getLevel() % Experience.LEVEL.length)]);
        }
		summon.setCurrentHp(summon.getMaxHp());
		summon.setCurrentMp(summon.getMaxMp());
		summon.setHeading(activeChar.getHeading());
    	summon.setRunning();
		activeChar.setPet(summon);
    		
    	L2World.getInstance().storeObject(summon);
        summon.spawnMe(activeChar.getX()+50, activeChar.getY()+100, activeChar.getZ());
    		
    	summon.setFollowStatus(true);
        summon.setShowSummonAnimation(false); // addVisibleObject created the info packets with summon animation
                                              // if someone comes into range now, the animation shouldnt show any more
        activeChar.sendPacket(new PetInfo(summon));

	}

}
