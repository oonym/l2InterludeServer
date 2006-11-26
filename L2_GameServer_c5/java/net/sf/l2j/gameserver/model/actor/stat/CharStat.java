package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.Calculator;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Stats;

public class CharStat
{
    // =========================================================
    // Data Field
    private L2Character _ActiveChar;
    private long _Exp                       = 1;
    private int _Sp                         = 1;
    private byte _Level                     = 1;
    
    // =========================================================
    // Constructor
    public CharStat(L2Character activeChar)
    {
        _ActiveChar = activeChar;
    }

    // =========================================================
    // Method - Public
    /**
     * Calculate the new value of the state with modifiers that will be applied on the targeted L2Character.<BR><BR>
     *
     * <B><U> Concept</U> :</B><BR><BR>
     * A L2Character owns a table of Calculators called <B>_calculators</B>.
     * Each Calculator (a calculator per state) own a table of Func object.
     * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...) : <BR><BR>
     *
     * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR><BR>
     *
     * When the calc method of a calculator is launched, each mathematic function is called according to its priority <B>_order</B>.
     * Indeed, Func with lowest priority order is executed firsta and Funcs with the same order are executed in unspecified order.
     * The result of the calculation is stored in the value property of an Env class instance.<BR><BR>
     *
     * @param stat The stat to calculate the new value with modifiers
     * @param init The initial value of the stat before applying modifiers
     * @param target The L2Charcater whose properties will be used in the calculation (ex : CON, INT...)
     * @param skill The L2Skill whose properties will be used in the calculation (ex : Level...)
     *
     */
    public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
    {
        int id = stat.ordinal();

        Calculator c = getActiveChar().getCalculators()[id];

        // If no Func object found, no modifier is applied
        if (c == null || c.size() == 0) return init;

        // Create and init an Env object to pass parameters to the Calculator
        Env env = new Env();
        env._player = getActiveChar();
        env._target = target;
        env._skill = skill;
        env.value = init;

        // Launch the calculation
        c.calc(env);
        // avoid some troubles with negative stats (some stats should never be negative)
        if(env.value <= 0 && (
                (stat == Stats.MAX_HP) || 
                (stat == Stats.MAX_MP) ||
                (stat == Stats.MAX_CP) ||
                (stat == Stats.MAGIC_DEFENCE) ||
                (stat == Stats.POWER_DEFENCE) ||
                (stat == Stats.POWER_ATTACK) ||
                (stat == Stats.MAGIC_ATTACK) ||
                (stat == Stats.POWER_ATTACK_SPEED) ||
                (stat == Stats.MAGIC_ATTACK_SPEED) ||
                (stat == Stats.SHIELD_DEFENCE) ||
                (stat == Stats.STAT_CON) ||
                (stat == Stats.STAT_DEX) ||
                (stat == Stats.STAT_INT) ||
                (stat == Stats.STAT_MEN) ||
                (stat == Stats.STAT_STR) ||
                (stat == Stats.STAT_WIT)))
        {
            env.value = 1;
        }

        return env.value;
    }

    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    /** Return the Accuracy (base+modifier) of the L2Character in function of the Weapon Expertise Penalty. */
    public int getAccuracy() { return (int)(calcStat(Stats.ACCURACY_COMBAT, 0, null, null) / getActiveChar().getWeaponExpertisePenalty()); }

    public L2Character getActiveChar()
    {        
        return _ActiveChar;
    }
    
    /** Return the Attack Speed multiplier (base+modifier) of the L2Character to get proper animations. */
    public final float getAttackSpeedMultiplier() { return (float) ((1.1) * getPAtkSpd() / getActiveChar().getTemplate().basePAtkSpd); }

    /** Return the CON of the L2Character (base+modifier). */
    public final int getCON() { return (int)calcStat(Stats.STAT_CON, getActiveChar().getTemplate().baseCON, null, null); }

    /** Return the Critical Damage rate (base+modifier) of the L2Character. */
    public final double getCriticalDmg(L2Character target, double init) { return calcStat(Stats.CRITICAL_DAMAGE, init, target, null); }

    /** Return the Critical Hit rate (base+modifier) of the L2Character. */
    public int getCriticalHit(L2Character target, L2Skill skill) { return (int)calcStat(Stats.CRITICAL_RATE, getActiveChar().getTemplate().baseCritRate, target, skill); }

    /** Return the DEX of the L2Character (base+modifier). */
    public final int getDEX() { return (int)calcStat(Stats.STAT_DEX, getActiveChar().getTemplate().baseDEX, null, null); }

    /** Return the Attack Evasion rate (base+modifier) of the L2Character. */
    public int getEvasionRate(L2Character target) { return (int)(calcStat(Stats.EVASION_RATE, 0, target, null) / getActiveChar().getArmourExpertisePenalty()); }

    public long getExp() { return _Exp; }
    public void setExp(long value) { _Exp = value; }

    /** Return the INT of the L2Character (base+modifier). */
    public int getINT() { return (int)calcStat(Stats.STAT_INT, getActiveChar().getTemplate().baseINT, null, null); }

    public byte getLevel() { return _Level; }
    public void setLevel(byte value) { _Level = value; }
    
    /** Return the Magical Attack range (base+modifier) of the L2Character. */
    public final int getMagicalAttackRange(L2Skill skill)
    {
        if (skill != null) return (int)calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
        return getActiveChar().getTemplate().baseAtkRange;
    }

    public final int getMaxCp() { return (int)calcStat(Stats.MAX_CP, getActiveChar().getTemplate().baseCpMax, null, null); }

    public int getMaxHp() { return (int)calcStat(Stats.MAX_HP, getActiveChar().getTemplate().baseHpMax, null, null); }

    public int getMaxMp() { return (int)calcStat(Stats.MAX_MP, getActiveChar().getTemplate().baseMpMax, null, null); }

    /**
     * Return the MAtk (base+modifier) of the L2Character for a skill used in function of abnormal effects in progress.<BR><BR>
     *
     * <B><U> Example of use </U> :</B><BR><BR>
     * <li> Calculate Magic damage </li><BR><BR>
     *
     * @param target The L2Character targeted by the skill
     * @param skill The L2Skill used against the target
     */
    public int getMAtk(L2Character target, L2Skill skill)
    {
        // Get the base MAtk of the L2Character
        double attack = getActiveChar().getTemplate().baseMAtk;

        // Get the skill type to calculate its effect in function of base stats of the L2Character target
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

        // Add the power of the skill to the attack effect
        if (skill != null)
            attack += skill.getPower();

        // Calculate modifiers Magic Attack
        return (int)calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
    }

    /** Return the MAtk Speed (base+modifier) of the L2Character in function of the Armour Expertise Penalty. */
    public int getMAtkSpd()
    {
        double val = calcStat(Stats.MAGIC_ATTACK_SPEED, getActiveChar().getTemplate().baseMAtkSpd, null, null);
        val /= getActiveChar().getArmourExpertisePenalty();
        return (int)val;
    }

    /** Return the Magic Critical Hit rate (base+modifier) of the L2Character. */
    public final int getMCriticalHit(L2Character target, L2Skill skill)
    {
        double mrate = calcStat(Stats.MCRITICAL_RATE, 5, target, skill);
        return (int)mrate;
    }

    /**
     * Return the MDef (base+modifier) of the L2Character against a skill in function of abnormal effects in progress.<BR><BR>
     *
     * <B><U> Example of use </U> :</B><BR><BR>
     * <li> Calculate Magic damage </li><BR><BR>
     *
     * @param target The L2Character targeted by the skill
     * @param skill The L2Skill used against the target
     */
    public int getMDef(L2Character target, L2Skill skill)
    {
        // Get the base MAtk of the L2Character
        double defence = getActiveChar().getTemplate().baseMDef;

        // Get the skill type to calculate its effect in function of base stats of the L2Character target
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
        
        // Calculate modifier for Raid Bosses
        if (getActiveChar().isRaid()) defence *= Config.RAID_DEFENCE_MULTIPLIER;

        // Calculate the elemental Defence
        if (skill!=null)
        {
            switch (skill.getElement())
            {
                case L2Skill.ELEMENT_EARTH: defence = calcStat(Stats.EARTH_RES,defence,target,skill); break;
                case L2Skill.ELEMENT_FIRE: defence = calcStat(Stats.FIRE_RES,defence,target,skill);break;
                case L2Skill.ELEMENT_WATER: defence = calcStat(Stats.WATER_RES,defence,target,skill);break;
                case L2Skill.ELEMENT_WIND: defence = calcStat(Stats.WIND_RES,defence,target,skill);break;
                case L2Skill.ELEMENT_HOLY: defence = calcStat(Stats.HOLY_RES,defence,target,skill);break;
                case L2Skill.ELEMENT_DARK: defence = calcStat(Stats.DARK_RES,defence,target,skill);break;
            }
        }

        // Calculate modifiers Magic Attack
        return (int)calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
    }

    /** Return the MEN of the L2Character (base+modifier). */
    public final int getMEN() { return (int)calcStat(Stats.STAT_MEN, getActiveChar().getTemplate().baseMEN, null, null); }

    public final float getMovementSpeedMultiplier() { return getRunSpeed() * 1f / getActiveChar().getTemplate().baseRunSpd; }

    /** Return the RunSpeed (base+modifier) or WalkSpeed (base+modifier) of the L2Character in function of the movement type. */
    public final float getMoveSpeed()
    {
        if (getActiveChar().isRunning()) return getRunSpeed();
        return getWalkSpeed();
    }

    /** Return the MReuse rate (base+modifier) of the L2Character. */
    public final double getMReuseRate(L2Skill skill) { return calcStat(Stats.MAGIC_REUSE_RATE, getActiveChar().getTemplate().baseMReuseRate, null, skill); }

    /** Return the PAtk (base+modifier) of the L2Character. */
    public int getPAtk(L2Character target) { return (int)calcStat(Stats.POWER_ATTACK, getActiveChar().getTemplate().basePAtk, target, null); }

    /** Return the PAtk Modifier against animals. */
    public final double getPAtkAnimals(L2Character target) { return calcStat(Stats.PATK_ANIMALS, 1, target, null); }
    
    /** Return the PAtk Modifier against dragons. */
    public final double getPAtkDragons(L2Character target) { return calcStat(Stats.PATK_DRAGONS, 1, target, null); }

    /** Return the PAtk Modifier against insects. */
    public final double getPAtkInsects(L2Character target) { return calcStat(Stats.PATK_INSECTS, 1, target, null); }
    
    /** Return the PAtk Modifier against monsters. */
    public final double getPAtkMonsters(L2Character target) { return calcStat(Stats.PATK_MONSTERS, 1, target, null); }
    
    /** Return the PAtk Modifier against plants. */
    public final double getPAtkPlants(L2Character target) { return calcStat(Stats.PATK_PLANTS, 1, target, null); }
    
    /** Return the PAtk Speed (base+modifier) of the L2Character in function of the Armour Expertise Penalty. */
    public int getPAtkSpd() { return (int)(calcStat(Stats.POWER_ATTACK_SPEED, getActiveChar().getTemplate().basePAtkSpd, null, null) / getActiveChar().getArmourExpertisePenalty()); }

    /** Return the PAtk Modifier against undead. */
    public final double getPAtkUndead(L2Character target) { return calcStat(Stats.PATK_UNDEAD, 1, target, null); }
    public final double getPDefUndead(L2Character target) { return calcStat(Stats.PDEF_UNDEAD, 1, target, null); }
    
    /** Return the PDef (base+modifier) of the L2Character. */
    public int getPDef(L2Character target) { return (int)calcStat(Stats.POWER_DEFENCE, (getActiveChar().isRaid()) ? getActiveChar().getTemplate().basePDef * Config.RAID_DEFENCE_MULTIPLIER : getActiveChar().getTemplate().basePDef, target, null); }

    /** Return the Physical Attack range (base+modifier) of the L2Character. */
    public final int getPhysicalAttackRange() { return (int)calcStat(Stats.POWER_ATTACK_RANGE, getActiveChar().getTemplate().baseAtkRange, null, null); }

    /** Return the Skill/Spell reuse modifier. */
    public final double getReuseModifier(L2Character target) { return calcStat(Stats.ATK_REUSE, 1, target, null); }
    
    /** Return the RunSpeed (base+modifier) of the L2Character in function of the Armour Expertise Penalty. */
    public int getRunSpeed()
    {
        // err we should be adding TO the persons run speed
        // not making it a constant
        int val = (int)calcStat(Stats.RUN_SPEED, getActiveChar().getTemplate().baseRunSpd, null, null);

        if (getActiveChar().isFlying())
        {
            val += Config.WYVERN_SPEED;
            return val;
        }
        if (getActiveChar().isRiding())
        {
            val += Config.STRIDER_SPEED;
            return val;
        }
        val /= getActiveChar().getArmourExpertisePenalty();
        return val;
    }

    /** Return the ShieldDef rate (base+modifier) of the L2Character. */
    public final int getShldDef() { return (int)calcStat(Stats.SHIELD_DEFENCE, 0, null, null); }

    public int getSp() { return _Sp; }
    public void setSp(int value) { _Sp = value; }

    /** Return the STR of the L2Character (base+modifier). */
    public final int getSTR() { return (int)calcStat(Stats.STAT_STR, getActiveChar().getTemplate().baseSTR, null, null); }

    /** Return the WalkSpeed (base+modifier) of the L2Character. */
    public final int getWalkSpeed() { return (getRunSpeed() * 70) / 100; }

    /** Return the WIT of the L2Character (base+modifier). */
    public final int getWIT() { return (int)calcStat(Stats.STAT_WIT, getActiveChar().getTemplate().baseWIT, null, null); }

    /** Return the mpConsume. */
    public final int getMpConsume(L2Skill skill)
    {
        return (int)calcStat(Stats.MP_CONSUME, skill.getMpConsume(), null, null);
    }
    
    /** Return the mpInitialConsume. */
    public final int getMpInitialConsume(L2Skill skill)
    {
    	return (int)calcStat(Stats.MP_CONSUME, skill.getMpInitialConsume(), null, null);
    }
}
