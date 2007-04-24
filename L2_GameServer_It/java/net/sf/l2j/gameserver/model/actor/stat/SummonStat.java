package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.model.L2Summon;

public class SummonStat extends PlayableStat
{
    // =========================================================
    // Data Field
    
    // =========================================================
    // Constructor
    public SummonStat(L2Summon activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public

    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public L2Summon getActiveChar() { return (L2Summon)super.getActiveChar(); }
}
