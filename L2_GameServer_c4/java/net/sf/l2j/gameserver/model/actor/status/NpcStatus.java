package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;

public class NpcStatus extends CharStatus
{
    // =========================================================
    // Data Field
    
    // =========================================================
    // Constructor
    public NpcStatus(L2NpcInstance[] activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public
    public final void reduceHp(double value, L2Character attacker) { reduceHp(value, attacker, true); }

    public final void reduceHp(double value, L2Character attacker, boolean awake)
    {
        if (attacker == null || getActiveChar().isDead()) return;

        // Add attackers to npc's attacker list
        getActiveChar().addAttackerToAttackByList(attacker);

        super.reduceHp(value, attacker, awake);
    }

    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public L2NpcInstance getActiveChar() { return (L2NpcInstance)super.getActiveChar(); }
}
