package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class MonsterKnownList extends AttackableKnownList
{
    // =========================================================
    // Data Field
    
    // =========================================================
    // Constructor
    public MonsterKnownList(L2MonsterInstance activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public
    public boolean addKnownObject(L2Object object) { return addKnownObject(object, null); }
    public boolean addKnownObject(L2Object object, L2Character dropper)
    {
        if (!super.addKnownObject(object, dropper)) return false;
        

        // Set the L2MonsterInstance Intention to AI_INTENTION_ACTIVE if the state was AI_INTENTION_IDLE
        if (object instanceof L2PcInstance && getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) 
            getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
        return true;
    }

    public boolean removeKnownObject(L2Object object)
    {
        if (!super.removeKnownObject(object)) return false;

        if (!(object instanceof L2Character)) return true;

        if (getActiveChar().hasAI()) 
        {   
            // Notify the L2MonsterInstance AI with EVT_FORGET_OBJECT
            getActiveChar().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
            
            //TODO Remove this function because it's already done in L2Character.removeKnownObject 
            // Set the current target to null if the forgotten L2Object was the targeted L2Object
            // L2Character temp = (L2Character)object;
            
            //if (getTarget() == temp)
            //  setTarget(null);
        }   
    
        if (getActiveChar().isVisible() && getKnownPlayers().isEmpty())
        {
            // Clear the _aggroList of the L2MonsterInstance
            getActiveChar().clearAggroList();
            
            // Remove all L2Object from _knownObjects and _knownPlayer of the L2MonsterInstance then cancel Attak or Cast and notify AI
            //removeAllKnownObjects();
            
            //TODO Remove this function because it's already done in L2Attackable.removeKnownObject 
            // Set the L2MonsterInstance AI to AI_INTENTION_IDLE
            // if (hasAI())
            // getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
        }

        return true;
    }
    
    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public final L2MonsterInstance getActiveChar() { return (L2MonsterInstance)super.getActiveChar(); }
}
