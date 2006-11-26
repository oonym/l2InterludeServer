package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.skills.Stats;

public class NpcStat extends CharStat
{
    // =========================================================
    // Data Field
    
    // =========================================================
    // Constructor
    public NpcStat(L2NpcInstance activeChar)
    {
        super(activeChar);

        setLevel(getActiveChar().getTemplate().level);
    }

    // =========================================================
    // Method - Public

    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public L2NpcInstance getActiveChar() { return (L2NpcInstance)super.getActiveChar(); }

    public final int getMaxHp() { return (int)calcStat(Stats.MAX_HP, getActiveChar().getTemplate().baseHpMax * getActiveChar().getTemplate().rateHp , null, null); }
}
