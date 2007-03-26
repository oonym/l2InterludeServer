package net.sf.l2j.gameserver.model.quest;

import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.lib.Rnd;
import net.sf.l2j.gameserver.model.AutoChatHandler;
import net.sf.l2j.gameserver.model.AutoSpawnHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.AutoChatHandler.AutoChatInstance;
import net.sf.l2j.gameserver.model.AutoSpawnHandler.AutoSpawnInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

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
                spawn.setLocx(x);
                spawn.setLocy(y);
                spawn.setLocz(z + 20);
                spawn.stopRespawn();
                
                spawn.spawnOne();
                
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
        addChatTask(objectId, messages, chatDelay);
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
            addChatTask(npcInst.getObjectId(), messages, chatDelay);
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
        for (int i = 0; i < _autoSpawns.size(); i++)
        {
            if (_autoSpawns.get(i).getObjectId() == objectId)
            {
                AutoSpawnHandler.getInstance().removeSpawn(objectId);
                _autoSpawns.remove(i);
                return;
            }
        }

        for (int i = 0; i < getSpawns().size(); i++)
        {
            if (getSpawns().get(i).getId() == objectId)
            {
                getSpawns().get(i).getLastSpawn().decayMe();
                getSpawns().remove(i);
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
    private void addChatTask(int objectId, String[] messages, int chatDelay)
    {
        L2Object obj = L2World.getInstance().findObject(objectId);
        
        if (!(obj instanceof L2NpcInstance))
            return;
        
        L2NpcInstance npcInst = (L2NpcInstance)obj;
        
        if (messages != null)
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

        for (L2Spawn spawn: getSpawns())
        {
        	  if (spawn.getLastSpawn().getObjectId() == objectId)
                  return spawn;
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
