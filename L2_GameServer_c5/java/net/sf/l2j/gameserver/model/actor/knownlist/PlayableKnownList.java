package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;

public class PlayableKnownList extends CharKnownList
{
    // =========================================================
    // Data Field
    
    // =========================================================
    // Constructor
    public PlayableKnownList(L2PlayableInstance[] activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public
    
    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public L2PlayableInstance getActiveChar() { return (L2PlayableInstance)super.getActiveChar(); }
}
