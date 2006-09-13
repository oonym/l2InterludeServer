package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2PetDataTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.base.Experience;
import net.sf.l2j.gameserver.serverpackets.PetInfo;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;

public class PetStat extends SummonStat
{
    // =========================================================
    // Data Field
    
    // =========================================================
    // Constructor
    public PetStat(L2PetInstance[] activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public
    public boolean addExp(int value)
    {
        if (!super.addExp(value)) return false;

        StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
        su.addAttribute(StatusUpdate.EXP, getExp());
        getActiveChar().broadcastPacket(su);

        return true;
    }

    public boolean addExpAndSp(int addToExp, int addToSp)
    {
    	if (!super.addExpAndSp(addToExp, addToSp)) return false;

        SystemMessage sm = new SystemMessage(SystemMessage.PET_EARNED_S1_EXP);
        sm.addNumber(addToExp);
                
        getActiveChar().getOwner().sendPacket(sm);

        return true;
    }

    public final boolean addLevel(int value)
    {
        if (getLevel() + value > 78) return false;

        boolean levelIncreased = super.addLevel(value);

        // Sync up exp with current level
        if (getExp() > getExpForLevel(getLevel() + 1) || getExp() < getExpForLevel(getLevel())) setExp(Experience.LEVEL[getLevel()]);

        if (levelIncreased) getActiveChar().getOwner().sendMessage("Your pet has increased it's level.");

        StatusUpdate su = new StatusUpdate(getActiveChar().getObjectId());
        su.addAttribute(StatusUpdate.LEVEL, getLevel());
        su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
        su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
        getActiveChar().broadcastPacket(su);

        // Send a Server->Client packet PetInfo to the L2PcInstance
        getActiveChar().getOwner().sendPacket(new PetInfo(getActiveChar()));
        
        if (getActiveChar().getControlItem() != null)
        	getActiveChar().getControlItem().setEnchantLevel(getLevel());

        return levelIncreased;
    }

    public final int getExpForLevel(int level) { return L2PetDataTable.getInstance().getPetData(getActiveChar().getNpcId(), level).getPetMaxExp(); }
    
    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public L2PetInstance getActiveChar() { return (L2PetInstance)super.getActiveChar(); }

    public final int getFeedBattle() { return getActiveChar().getPetData().getPetFeedBattle(); }

    public final int getFeedNormal() { return getActiveChar().getPetData().getPetFeedNormal(); }

    public void setLevel(int value)
    {
        getActiveChar().stopFeed();
        super.setLevel(value);

        getActiveChar().setPetData(L2PetDataTable.getInstance().getPetData(getActiveChar().getTemplate().npcId, getLevel()));
        getActiveChar().startFeed( false );

        if (getActiveChar().getControlItem() != null)
        	getActiveChar().getControlItem().setEnchantLevel(getLevel());
    }

    public final int getMaxFeed() { return getActiveChar().getPetData().getPetMaxFeed(); }

    public int getMaxHp() { return (int)calcStat(Stats.MAX_HP, getActiveChar().getPetData().getPetMaxHP(), null, null); }
    
    public int getMaxMp() { return (int)calcStat(Stats.MAX_MP, getActiveChar().getPetData().getPetMaxMP(), null, null); }
    
    public int getMAtk(L2Character target, L2Skill skill)
    {
        double attack = getActiveChar().getPetData().getPetMAtk();
        Stats stat = skill == null? null : skill.getStat();
        if (stat != null)
        {
            switch (stat)
            {
            case AGGRESSION: attack += getActiveChar().getTemplate().baseAggression; break;
            case BLEED:      attack += getActiveChar().getTemplate().baseBleed;      break;
            case POISON:     attack += getActiveChar().getTemplate().basePoison;     break;
            case STUN:       attack += getActiveChar().getTemplate().baseStun;       break;
            case ROOT:       attack += getActiveChar().getTemplate().baseRoot;       break;
            case MOVEMENT:   attack += getActiveChar().getTemplate().baseMovement;   break;
            case CONFUSION:  attack += getActiveChar().getTemplate().baseConfusion;  break;
            case SLEEP:      attack += getActiveChar().getTemplate().baseSleep;      break;
            case FIRE:       attack += getActiveChar().getTemplate().baseFire;       break;
            case WIND:       attack += getActiveChar().getTemplate().baseWind;       break;
            case WATER:      attack += getActiveChar().getTemplate().baseWater;      break;
            case EARTH:      attack += getActiveChar().getTemplate().baseEarth;      break;
            case HOLY:       attack += getActiveChar().getTemplate().baseHoly;       break;
            case DARK:       attack += getActiveChar().getTemplate().baseDark;       break;
            }
        }
        if (skill != null) attack += skill.getPower();
        return (int)calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
    }
    
    public int getMDef(L2Character target, L2Skill skill)
    {
        double defence = getActiveChar().getPetData().getPetMDef();
        Stats stat = skill == null? null : skill.getStat();
        if (stat != null)
        {
            switch (stat)
            {
            case AGGRESSION: defence += getActiveChar().getTemplate().baseAggressionRes; break;
            case BLEED:      defence += getActiveChar().getTemplate().baseBleedRes;      break;
            case POISON:     defence += getActiveChar().getTemplate().basePoisonRes;     break;
            case STUN:       defence += getActiveChar().getTemplate().baseStunRes;       break;
            case ROOT:       defence += getActiveChar().getTemplate().baseRootRes;       break;
            case MOVEMENT:   defence += getActiveChar().getTemplate().baseMovementRes;   break;
            case CONFUSION:  defence += getActiveChar().getTemplate().baseConfusionRes;  break;
            case SLEEP:      defence += getActiveChar().getTemplate().baseSleepRes;      break;
            case FIRE:       defence += getActiveChar().getTemplate().baseFireRes;       break;
            case WIND:       defence += getActiveChar().getTemplate().baseWindRes;       break;
            case WATER:      defence += getActiveChar().getTemplate().baseWaterRes;      break;
            case EARTH:      defence += getActiveChar().getTemplate().baseEarthRes;      break;
            case HOLY:       defence += getActiveChar().getTemplate().baseHolyRes;       break;
            case DARK:       defence += getActiveChar().getTemplate().baseDarkRes;       break;
            }
        }
        return (int)calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
    }
    
    public int getPAtk(L2Character target) { return (int)calcStat(Stats.POWER_ATTACK, getActiveChar().getPetData().getPetPAtk(), target, null); }
    public int getPDef(L2Character target) { return (int)calcStat(Stats.POWER_DEFENCE, getActiveChar().getPetData().getPetPDef(), target, null); }
    public int getAccuracy() { return (int)calcStat(Stats.ACCURACY_COMBAT, getActiveChar().getPetData().getPetAccuracy(), null, null); }
    public int getCriticalHit(L2Character target, L2Skill skill) { return (int)calcStat(Stats.CRITICAL_RATE, getActiveChar().getPetData().getPetCritical(), target, null); }
    public int getEvasionRate(L2Character target) { return (int)calcStat(Stats.EVASION_RATE, getActiveChar().getPetData().getPetEvasion(), target, null); }
    public int getRunSpeed() { return (int)calcStat(Stats.RUN_SPEED, getActiveChar().getPetData().getPetSpeed(), null, null); }
    public int getPAtkSpd() { return (int)calcStat(Stats.POWER_ATTACK_SPEED, getActiveChar().getPetData().getPetAtkSpeed(), null, null); }
    public int getMAtkSpd() { return  (int)calcStat(Stats.MAGIC_ATTACK_SPEED, getActiveChar().getPetData().getPetCastSpeed(), null, null); }
}
