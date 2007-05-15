package net.sf.l2j.gameserver.model.quest;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.AutoChatHandler;
import net.sf.l2j.gameserver.model.AutoSpawnHandler;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.AutoChatHandler.AutoChatInstance;
import net.sf.l2j.gameserver.model.AutoSpawnHandler.AutoSpawnInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class QuestPcSpawn
{
    protected static Logger _log = Logger.getLogger(QuestPcSpawn.class.getName());

    public class DeSpawnScheduleTimerTask implements Runnable
    {
        int _ObjectId = 0;
        public DeSpawnScheduleTimerTask(int objectId)
        {
            _ObjectId = objectId;
        }
        
        public void run()
        {
            try
            {
                removeSpawn(_ObjectId);
            } catch (Throwable t){}
        }
    }

    // =========================================================
    // Data Field
    private L2PcInstance _Player;
    private List<AutoSpawnInstance> _autoSpawns = new FastList<AutoSpawnInstance>();
    private List<L2Spawn> _Spawns = new FastList<L2Spawn>();
    
    // =========================================================
    // Constructor
    public QuestPcSpawn(L2PcInstance player)
    {
        _Player = player;
    }

    // =========================================================
    // Method - Public
    /**
     * Add spawn for player instance
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId)
    {
        return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), true);
    }

    /**
     * Add spawn for player instance
     * Will despawn after the spawn length expires
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId, int spawnLength)
    {
        return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), true, spawnLength);
    }

    /**
     * Add spawn for player instance
     * Will random message when chat delay time reached
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId, String[] messages, int chatDelay)
    {
        return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), true, messages, chatDelay);
    }

    /**
     * Add spawn for player instance
     * Will despawn after the spawn length expires
     * Will random message when chat delay time reached
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId, int spawnLength, String[] messages, int chatDelay)
    {
        return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), true, spawnLength, messages, chatDelay);
    }

    /**
     * Add spawn for player instance
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId, int x, int y, int z)
    {
        return addSpawn(npcId, x, y, z, false);
    }

    /**
     * Add spawn for player instance
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId, int x, int y, int z, boolean randomOffset)
    {
        try {
            if (randomOffset)
            {
                int offset;

                offset = Rnd.get(1); // Get the direction of the offset
                if (offset == 0) {offset = -1;} // make offset negative
                offset *= Rnd.get(50, 100);
                x += offset;

                offset = Rnd.get(1); // Get the direction of the offset
                if (offset == 0) {offset = -1;} // make offset negative
                offset *= Rnd.get(50, 100); 
                y += offset;
            }
            
            L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(npcId);
            if (template1 != null)
            {
                L2Spawn spawn = new L2Spawn(template1);

                spawn.setId(IdFactory.getInstance().getNextId());
                spawn.setHeading(getPlayer().getHeading());

                // Sometimes, even if the quest script specifies some xyz (for example npc.getX() etc) by the time the code
            	// reaches here, xyz have become 0!  Also, a questdev might have purposely set xy to 0,0...however,
            	// the spawn code is coded such that if x=y=0, it looks into location for the spawn loc!  This will NOT work
            	// with quest spawns!  For both of the above cases, we need a fail-safe spawn.  For this, we use the 
                // default spawn location, which is at the player's loc.
                if ((x == 0) && (y == 0))
                {
                	_log.log(Level.WARNING, getPlayer().getName() + " requested quest spawn with loc 0,0.  Loc is being adjusted");

                	// attempt to use the player's xyz as a the default spawn location.
                	x = getPlayer().getX();
                	y = getPlayer().getY();
                	z = getPlayer().getZ();
                	// if the fail-safe also did not help, abort this spawning and give a severe log 
                    if ((x == 0) && (y == 0))
                    {
                    	_log.log(Level.SEVERE, "Failed to adjust bad locks for quest spawn requested by "+getPlayer().getName() + "!  Spawn aborted!");
                    	return 0;
                    }
                }
                spawn.setLocx(x);
                spawn.setLocy(y);
                spawn.setLocz(z + 20);
                spawn.stopRespawn();
                spawn.doSpawn(true);

                _Spawns.add(spawn);

                return spawn.getLastSpawn().getObjectId();
            }
          } catch (Exception e1) {_log.warning("Could not spawn Npc " + npcId);}
          
          return 0;
    }

    /**
     * Add spawn for player instance
     * Will despawn after the spawn length expires
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId, int x, int y, int z, int spawnLength)
    {
        return addSpawn(npcId, x, y, z, false, spawnLength);
    }

    /**
     * Add spawn for player instance
     * Will despawn after the spawn length expires
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId, int x, int y, int z, boolean randomOffset, int spawnLength)
    {
        return addSpawn(npcId, x, y, z, randomOffset, spawnLength, null, 0);
    }

    /**
     * Add spawn for player instance
     * Will random message when chat delay time reached
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId, int x, int y, int z, String[] messages, int chatDelay)
    {
        return addSpawn(npcId, x, y, z, false, messages, chatDelay);
    }

    /**
     * Add spawn for player instance
     * Will random message when chat delay time reached
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId, int x, int y, int z, boolean randomOffset, String[] messages, int chatDelay)
    {
        return addSpawn(npcId, x, y, z, randomOffset, 0, messages, chatDelay);
    }

    /**
     * Add spawn for player instance
     * Will despawn after the spawn length expires
     * Will random message when chat delay time reached
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId, int x, int y, int z, int spawnLength, String[] messages, int chatDelay)
    {
        return addSpawn(npcId, x, y, z, false, spawnLength, messages, chatDelay);
    }

    /**
     * Add spawn for player instance
     * Will despawn after the spawn length expires
     * Will random message when chat delay time reached
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId, int x, int y, int z, boolean randomOffset, int spawnLength, String[] messages, int chatDelay)
    {
        int objectId = addSpawn(npcId, x, y, z, randomOffset);
        addDeSpawnTask(objectId, spawnLength);
        addChatTask(getSpawn(objectId).getLastSpawn(), messages, chatDelay);
        return objectId;
    }

    /**
     * Add random spawn for player instance
     * Return object id of newly spawned npc
     */
    public int addRandomSpawn(int npcId, int count, int initialDelay, int respawnDelay, int despawnDelay)
    {
        _autoSpawns.add(AutoSpawnHandler.getInstance().registerSpawn(npcId, initialDelay, respawnDelay, despawnDelay));
        _autoSpawns.get(_autoSpawns.size() - 1).setSpawnCount(count);
        
        return _autoSpawns.get(_autoSpawns.size() - 1).getObjectId();
    }
    
    /**
     * Add random chat for npc
     */
    public void addRandomSpawnChat(int objectId, String[] messages, int chatDelay)
    {
        if (messages != null && chatDelay > 0)
            return;
            
        AutoSpawnInstance randomSpawn = getRandomSpawn(objectId);
        
        if (randomSpawn == null)
            return;

        for (L2NpcInstance npcInst: randomSpawn.getNPCInstanceList())
        {
            addChatTask(npcInst, messages, chatDelay);
        }
    }
    
    /**
     * Add random spawn location for npc
     */
    public void addRandomSpawnLoc(int objectId, int x, int y, int z)
    {
        AutoSpawnInstance randomSpawn = getRandomSpawn(objectId);
        
        if (randomSpawn == null)
            return;

        randomSpawn.addSpawnLocation(x, y, z, -1);
    }
    
    /**
     * Remove all spawn for player instance
     */
    public void removeAllSpawn()
    {
        for (int i = _autoSpawns.size() - 1; i >= 0; i--)
        {
            AutoSpawnInstance currSpawn = _autoSpawns.get(i);
            
            AutoSpawnHandler.getInstance().removeSpawn(currSpawn.getObjectId());
            _autoSpawns.remove(i);
        }
        
        if (getSpawns() == null)
            return;

        for (int i = getSpawns().size() - 1; i >= 0; i--)
        {
            getSpawns().get(i).getLastSpawn().decayMe();
            getSpawns().remove(i);
        }
    }
    
    /**
     * Remove spawn with object id for player instance
     */
    public void removeSpawn(int objectId)
    {
        // these spawns are used more commonly.  Might as well
        // make them the first to be checked and avoid wasting cycles
    	for (int i = getSpawns().size() - 1; i >= 0; i--)
        {
            // also use this opportunity to get rid of junk 
    		// (for example old spawns of killed NPCs or old failed spawns, etc)
    		L2NpcInstance npc = getSpawns().get(i).getLastSpawn();
            if (npc == null)
                getSpawns().remove(i);
            else if (npc.getObjectId() == objectId)
            {
                npc.decayMe();
                getSpawns().remove(i);
                return;
            }
        }

        for (int i = 0; i < _autoSpawns.size(); i++)
        {
            if (_autoSpawns.get(i).getObjectId() == objectId)
            {
                AutoSpawnHandler.getInstance().removeSpawn(objectId);
                _autoSpawns.remove(i);
                return;
            }
        }
    }
    
    /**
     * Remove spawn with object id from list for player instance
     */
    public void removeSpawns(int[] objectIds)
    {
        for (int objectId: objectIds)
        {
            removeSpawn(objectId);
        }
    }
    
    // =========================================================
    // Method - Private
    private void addChatTask(L2NpcInstance npcInst, String[] messages, int chatDelay)
    {
    	if ( (messages != null) && (npcInst != null) )
            AutoChatHandler.getInstance().registerChat(npcInst, messages, chatDelay);
    }
    
    private void addDeSpawnTask(int objectId, int spawnLength)
    {
        if (spawnLength > 0)
        	ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnScheduleTimerTask(objectId), spawnLength);
    }

    // =========================================================
    // Property - Public
    /** Return current player instance */
    public L2PcInstance getPlayer()
    {
        return _Player;
    }

    /**
     * Return AutoChatInstance for player instance
     */
    public AutoChatInstance getAutoChat(int objectId)
    {
        return AutoChatHandler.getInstance().getAutoChatInstance(objectId, true);
    }
    
    /**
     * Return AutoSpawnInstance for player instance
     */
    public AutoSpawnInstance getRandomSpawn(int objectId)
    {
        for (AutoSpawnInstance randomSpawn: _autoSpawns)
        {
            if (randomSpawn.getObjectId() == objectId)
                return randomSpawn;
        }
        
        return null;
    }
    
    /**
     * Return list of AutoSpawnInstances for player instance
     */
    public List<AutoSpawnInstance> getRandomSpawns()
    {
        if (_autoSpawns == null)
            _autoSpawns = new FastList<AutoSpawnInstance>();
        
        return _autoSpawns;
    }
    
    /**
     * Return spawn instance for player instance
     */
    public L2Spawn getSpawn(int objectId)
    {
        for (AutoSpawnInstance randomSpawn: getRandomSpawns())
        {
            for (L2NpcInstance npcInst: randomSpawn.getNPCInstanceList())
            {
                if (npcInst.getObjectId() == objectId)
                    return npcInst.getSpawn();
            }
        }

        for (int i = getSpawns().size() - 1; i >= 0; i--)
        {
        	L2Spawn spawn = getSpawns().get(i);
        	// get rid of junk in the process...if any...
        	if ( (spawn == null) || (spawn.getLastSpawn() == null) )
        		getSpawns().remove(i);
        	else if (spawn.getLastSpawn().getObjectId() == objectId)
                  return getSpawns().get(i);
        }
        return null;
    }
    
    /**
     * Return list of L2Spawn for player instance
     */
    public List<L2Spawn> getSpawns()
    {
        if (_Spawns == null)
            _Spawns = new FastList<L2Spawn>();
        return _Spawns;
    }
}
