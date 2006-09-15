package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;

public class PlayableStatus extends CharStatus
{
    // =========================================================
    // Data Field
    
    // =========================================================
    // Constructor
    public PlayableStatus(L2PlayableInstance[] activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public
    public void reduceHp(double value, L2Character attacker) { reduceHp(value, attacker, true); }
    public void reduceHp(double value, L2Character attacker, boolean awake)
    {
        if (getActiveChar().isDead()) return;

        super.reduceHp(value, attacker, awake);
        /*
        if (attacker != null && attacker != getActiveChar())
        {
            // Flag the attacker if it's a L2PcInstance outside a PvP area
            L2PcInstance player = null;
            if (attacker instanceof L2PcInstance)
                player = (L2PcInstance)attacker;
            else if (attacker instanceof L2Summon)
                player = ((L2Summon)attacker).getOwner();

            if (player != null) player.updatePvPStatus(getActiveChar());
        }
        */
    }

    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public L2PlayableInstance getActiveChar() { return (L2PlayableInstance)super.getActiveChar(); }
}
