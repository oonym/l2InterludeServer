package net.sf.l2j.gameserver.model.quest;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class QuestPcSpawnManager
{
    // =========================================================
    // Schedule Task
    public class ScheduleTimerTask implements Runnable
    {
        public void run()
        {
            try
            {
                cleanUp();
                ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), 60000);
            } catch (Throwable t){}
        }
    }

    // =========================================================
    // Data Field
    private static QuestPcSpawnManager _Instance;
    private List<QuestPcSpawn> _PcSpawns = new FastList<QuestPcSpawn>();
    
    // =========================================================
    // Constructor
    public QuestPcSpawnManager()
    {
    	ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), 60000);
    }

    // =========================================================
    // Method - Public
    /**
     * Add spawn for the specified player instance at the coords of the player
     * Return object id of newly spawned npc
     */
    public int addSpawn(L2PcInstance player, int npcId)
    {
        return addSpawn(player, npcId, player.getX(), player.getY(), player.getZ());
    }

    /**
     * Add spawn for the specified player instance at the specified coords
     * Return object id of newly spawned npc
     */
    public int addSpawn(L2PcInstance player, int npcId, int x, int y, int z)
    {
        return getPcSpawn(player).addSpawn(npcId, x, y, z);
    }

    /**
     * Add spawn that will despawn after the specified spawn lenth for the specified player instance at the coords of the player
     * Return object id of newly spawned npc
     */
    public int addSpawn(L2PcInstance player, int npcId, int spawnLength)
    {
        return addSpawn(player, npcId, player.getX(), player.getY(), player.getZ(), spawnLength);
    }

    /**
     * Add spawn that will despawn after the specified spawn lenth for the specified player instance at the specified coords
     * Return object id of newly spawned npc
     */
    public int addSpawn(L2PcInstance player, int npcId, int x, int y, int z, int spawnLength)
    {
        return getPcSpawn(player).addSpawn(npcId, x, y, z, spawnLength);
    }

    /**
     * Add random spawn for the specified player instance
     * Return object id of newly spawned npc
     */
    public int addRandomSpawn(L2PcInstance player, int npcId, int count, int randomSpawnDelay, int spawnDelay, int offset)
    {
        return getPcSpawn(player).addRandomSpawn(npcId, count, randomSpawnDelay, spawnDelay, offset);
    }
    
    /**
     * Add random spawn location for npc of the  the specified
     */
    public void addRandomSpawnLoc(L2PcInstance player, int objectId)
    {
        getPcSpawn(player).addRandomSpawnLoc(objectId, player.getX(), player.getY(), player.getZ());
    }
    
    /**
     * Add random spawn location for npc of the  the specified
     */
    public void addRandomSpawnLoc(L2PcInstance player, int objectId, int x, int y, int z)
    {
        getPcSpawn(player).addRandomSpawnLoc(objectId, x, y, z);
    }

    /**
     * Remove all spawn for all player instance that does not exist
     */
    public void cleanUp()
    {
        for (int i = getPcSpawns().size() - 1; i >= 0; i--)
        {
            if (getPcSpawns().get(i).getPlayer() == null)
            {
                removeAllSpawn(getPcSpawns().get(i));
                getPcSpawns().remove(i);
            }
        }
    }
    
    /**
     * Return true of contain player instance
     */
    public boolean contains(L2PcInstance player)
    {
        for (int i = 0; i < getPcSpawns().size(); i++)
        {
            if (getPcSpawns().get(i).getPlayer() != null && getPcSpawns().get(i).getPlayer().getObjectId() == player.getObjectId())
                return true;
        }
        return false;
    }

    /**
     * Remove all spawn for all player instance
     */
    public void removeAllSpawn()
    {
        for (int i = getPcSpawns().size() - 1; i >= 0; i--)
        {
            removeAllSpawn(getPcSpawns().get(i));
            getPcSpawns().remove(i);
        }
    }

    /**
     * Remove all spawn for the specified player instance
     */
    public void removeAllSpawn(L2PcInstance player)
    {
        if (contains(player)) {removeAllSpawn(getPcSpawn(player));}
    }

    /**
     * Remove spawn with object id for the specified player instance
     */
    public void removeSpawn(L2PcInstance player, int objectId)
    {
        if (contains(player)) {getPcSpawn(player).removeSpawn(objectId);}
    }

    /**
     * Remove spawn with object id from list for the specified player instance
     */
    public void removeSpawns(L2PcInstance player, int[] objectIds)
    {
        if (contains(player)) {getPcSpawn(player).removeSpawns(objectIds);}
    }
    
    // =========================================================
    // Method - Private
    /**
     * Remove all spawn from the QuestPcSpawn instance
     */
    private void removeAllSpawn(QuestPcSpawn pcspawn)
    {
        pcspawn.removeAllSpawn();
    }

    // =========================================================
    // Property - Public
    /** Return global instance of QuestPcSpawnManager */
    public static final QuestPcSpawnManager getInstance()
    {
        if (_Instance == null) _Instance = new QuestPcSpawnManager();
        return _Instance;
    }
    
    /** Return quest pc spawn for specified player instance */
    public QuestPcSpawn getPcSpawn(L2PcInstance player)
    {
        for (int i = 0; i < getPcSpawns().size(); i++)
        {
            if (getPcSpawns().get(i).getPlayer() != null && getPcSpawns().get(i).getPlayer().getObjectId() == player.getObjectId())
                return getPcSpawns().get(i);
        }
        getPcSpawns().add(new QuestPcSpawn(new L2PcInstance[] {player}));
        return getPcSpawns().get(getPcSpawns().size() - 1);
    }

    /** Return all quest pc spawn */
    public List<QuestPcSpawn> getPcSpawns()
    {
        if (_PcSpawns == null) _PcSpawns = new FastList<QuestPcSpawn>();
        return _PcSpawns;
    }
}
