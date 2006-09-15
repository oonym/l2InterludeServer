package net.sf.l2j.gameserver.model.actor.status;

import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;

public class DoorStatus extends CharStatus
{
    // =========================================================
    // Data Field
    
    // =========================================================
    // Constructor
    public DoorStatus(L2DoorInstance[] activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public

    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public L2DoorInstance getActiveChar() { return (L2DoorInstance)super.getActiveChar(); }
}
