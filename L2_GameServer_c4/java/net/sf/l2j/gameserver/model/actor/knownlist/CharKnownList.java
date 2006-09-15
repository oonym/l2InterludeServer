package net.sf.l2j.gameserver.model.actor.knownlist;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastSet;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.util.Util;

public class CharKnownList extends ObjectKnownList
{
    // =========================================================
    // Data Field
    private Set<L2PcInstance> _KnownPlayers;
    
    // =========================================================
    // Constructor
    public CharKnownList(L2Character[] activeChar)
    {
        super(activeChar);
    }

    // =========================================================
    // Method - Public
    public boolean addKnownObject(L2Object object) { return addKnownObject(object, null); }
    public boolean addKnownObject(L2Object object, L2Character dropper)
    {
        if (!super.addKnownObject(object, dropper)) return false;
        if (object instanceof L2PcInstance) getKnownPlayers().add((L2PcInstance)object);
        return true;
    }

    /**
     * Return True if the L2PcInstance is in _knownPlayer of the L2Character.<BR><BR>
     * @param player The L2PcInstance to search in _knownPlayer
     */
    public final boolean knowsThePlayer(L2PcInstance player) { return getActiveChar() == player || getKnownPlayers().contains(player); }
    
    /** Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI. */
    public final void removeAllKnownObjects()
    {
        super.removeAllKnownObjects();
        getKnownPlayers().clear();

        // Set _target of the L2Character to null
        // Cancel Attack or Cast
        getActiveChar().setTarget(null);

        // Cancel AI Task
        if (getActiveChar().hasAI()) getActiveChar().setAI(null);
    }
    
    public boolean removeKnownObject(L2Object object)
    {
        if (!super.removeKnownObject(object)) return false;
        if (object instanceof L2PcInstance) getKnownPlayers().remove(object);
        // If object is targeted by the L2Character, cancel Attack or Cast
        if (object == getActiveChar().getTarget()) getActiveChar().setTarget(null);

        return true;
    }

    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public L2Character getActiveChar() { return (L2Character)super.getActiveObject(); }
    
    public int getDistanceToForgetObject(L2Object object) { return 0; }

    public int getDistanceToWatchObject(L2Object object) { return 0; }

    public Collection<L2Character> getKnownCharacters()
    {
        FastList<L2Character> result = new FastList<L2Character>();
        
        for (L2Object obj : getKnownObjects())  
        {  
            if (obj != null && obj instanceof L2Character) result.add((L2Character) obj);  
        }
        
        return result;
    }
    
    public Collection<L2Character> getKnownCharactersInRadius(long radius)
    {
       FastList<L2Character> result = new FastList<L2Character>();
       
       for (L2Object obj : getKnownObjects())  
       {  
           if (obj instanceof L2PcInstance)  
           {  
               if (Util.checkIfInRange((int)radius, getActiveChar(), obj, true))  
                   result.add((L2PcInstance)obj);  
           }  
           else if (obj instanceof L2MonsterInstance)  
           {  
               if (Util.checkIfInRange((int)radius, getActiveChar(), obj, true))  
                   result.add((L2MonsterInstance)obj);  
           }  
           else if (obj instanceof L2NpcInstance)  
           {  
               if (Util.checkIfInRange((int)radius, getActiveChar(), obj, true))  
                   result.add((L2NpcInstance)obj);  
           }
       }
       
       return result;
    }

    public final Collection<L2PcInstance> getKnownPlayers()
    {
        if (_KnownPlayers == null) _KnownPlayers = Collections.synchronizedSet(new FastSet<L2PcInstance>());
        return _KnownPlayers;
    }
    
    public final Collection<L2PcInstance> getKnownPlayersInRadius(long radius)
    {
        FastList<L2PcInstance> result = new FastList<L2PcInstance>();
        
        for (L2PcInstance player : getKnownPlayers())
            if (Util.checkIfInRange((int)radius, getActiveChar(), player, true))
                result.add(player);
            
        return result;
    }
}
