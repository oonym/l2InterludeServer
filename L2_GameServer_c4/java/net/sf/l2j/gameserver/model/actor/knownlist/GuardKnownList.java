package net.sf.l2j.gameserver.model.actor.knownlist;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2GuardInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class GuardKnownList extends AttackableKnownList
{
    private static Logger _log = Logger.getLogger(GuardKnownList.class.getName());

    // =========================================================
    // Data Field
    
    // =========================================================
    // Constructor
    public GuardKnownList(L2GuardInstance[] activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public
    public boolean addKnownObject(L2Object object) { return addKnownObject(object, null); }
    public boolean addKnownObject(L2Object object, L2Character dropper)
    {
        if (!super.addKnownObject(object, dropper)) return false;

        // Set home location of the L2GuardInstance (if not already done)
        if (getActiveChar().getHomeX() == 0) getActiveChar().getHomeLocation();
        
        
        if (object instanceof L2PcInstance) 
        {
            // Check if the object added is a L2PcInstance that owns Karma
            L2PcInstance player = (L2PcInstance) object;
            
            if ( (player.getKarma() > 0) )
            {
                if (Config.DEBUG) _log.fine(getActiveChar().getObjectId()+": PK "+player.getObjectId()+" entered scan range");
                
                // Set the L2GuardInstance Intention to AI_INTENTION_ACTIVE
                if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
                    getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
            }
        }
        else if ((Config.ALLOW_GUARDS) && (object instanceof L2MonsterInstance))
        {
            // Check if the object added is an aggressive L2MonsterInstance
            L2MonsterInstance mob = (L2MonsterInstance) object;
            
            if (mob.isAggressive() )
            {
                if (Config.DEBUG) _log.fine(getActiveChar().getObjectId()+": Aggressive mob "+mob.getObjectId()+" entered scan range");
                
                // Set the L2GuardInstance Intention to AI_INTENTION_ACTIVE
                if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
                    getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
            }
        }

        return true;
    }

    public boolean removeKnownObject(L2Object object)
    {
        if (!super.removeKnownObject(object)) return false;

        // Check if the _aggroList of the L2GuardInstance is Empty
        if (getActiveChar().noTarget()) 
        {
            //removeAllKnownObjects();
            
            // Set the L2GuardInstance to AI_INTENTION_IDLE
            L2CharacterAI ai = getActiveChar().getAI();
            if (ai != null) ai.setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
        }

        return true;
    }
    
    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public final L2GuardInstance getActiveChar() { return (L2GuardInstance)super.getActiveChar(); }
}
