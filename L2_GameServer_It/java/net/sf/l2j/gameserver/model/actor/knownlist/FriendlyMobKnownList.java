package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2FriendlyMobInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class FriendlyMobKnownList extends AttackableKnownList
{
    // =========================================================
    // Data Field
    
    // =========================================================
    // Constructor
    public FriendlyMobKnownList(L2FriendlyMobInstance activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public
    public boolean addKnownObject(L2Object object) { return addKnownObject(object, null); }
    public boolean addKnownObject(L2Object object, L2Character dropper)
    {
        if (!super.addKnownObject(object, dropper)) return false;

        if (object instanceof L2PcInstance && getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) 
            getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);

        return true;
    }

    public boolean removeKnownObject(L2Object object)
    {
        if (!super.removeKnownObject(object)) return false;

        if (!(object instanceof L2Character)) return true;

        if (getActiveChar().hasAI()) {
            L2Character temp = (L2Character)object;
            getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
            if (getActiveChar().getTarget() == temp) getActiveChar().setTarget(null);
        }   
    
        if (getActiveChar().isVisible() && getKnownPlayers().isEmpty())
        {
            getActiveChar().clearAggroList();
            //removeAllKnownObjects();
            if (getActiveChar().hasAI()) getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
        }

        return true;
    }
    
    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public final L2FriendlyMobInstance getActiveChar() { return (L2FriendlyMobInstance)super.getActiveChar(); }
}
