package net.sf.l2j.gameserver.model.actor.knownlist;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2SiegeGuardInstance;

public class SiegeGuardKnownList extends AttackableKnownList
{
    // =========================================================
    // Data Field
    
    // =========================================================
    // Constructor
    public SiegeGuardKnownList(L2SiegeGuardInstance[] activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public
    public boolean addKnownObject(L2Object object) { return addKnownObject(object, null); }
    public boolean addKnownObject(L2Object object, L2Character dropper)
    {
        if (!super.addKnownObject(object, dropper)) return false;

        if (getActiveChar().getHomeX() == 0) getActiveChar().getHomeLocation();

        // Check if siege is in progress 
        if (getActiveChar().getCastle() != null && getActiveChar().getCastle().getSiege().getIsInProgress())
        {
            L2PcInstance player = null;
            if (object instanceof L2PcInstance)
                player = (L2PcInstance) object;
            else if (object instanceof L2Summon)
                player = ((L2Summon)object).getOwner();

            // Check if player is not the defender
            if (player != null && (player.getClan() == null || getActiveChar().getCastle().getSiege().getAttackerClan(player.getClan()) != null))
            {
                //if (Config.DEBUG) _log.fine(getObjectId()+": PK "+player.getObjectId()+" entered scan range");
                if (getActiveChar().getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
                    getActiveChar().getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);//(L2Character)object);
            }

        }

        return true;
    }
    
    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public final L2SiegeGuardInstance getActiveChar() { return (L2SiegeGuardInstance)super.getActiveChar(); }
}
