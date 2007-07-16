/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.model.quest;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2Character;
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
    private static QuestPcSpawnManager _instance;
    private List<QuestPcSpawn> _pcSpawns = new FastList<QuestPcSpawn>();
    
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
        return getPcSpawn(player).addSpawn(npcId);
    }

    /**
     * Add spawn for the specified player instance at the specified coords
     * Return object id of newly spawned npc
     */
    public int addSpawn(L2PcInstance player, int npcId, int x, int y, int z)
    {
        return getPcSpawn(player).addSpawn(npcId, x, y, z, player.getHeading(),false,0);
    }

    /**
     * Add spawn that will despawn after the specified spawn lenth for the specified player instance at the coords of the player
     * Return object id of newly spawned npc
     */
    public int addSpawn(L2PcInstance player, int npcId, int spawnLength)
    {
        return getPcSpawn(player).addSpawn(npcId, spawnLength);
    }

    /**
     * Add spawn that will despawn after the specified spawn lenth for the specified player instance at the specified coords
     * Return object id of newly spawned npc
     */
    public int addSpawn(L2PcInstance player, int npcId, int x, int y, int z, int spawnLength)
    {
        return getPcSpawn(player).addSpawn(npcId, x, y, z, player.getHeading(),false,spawnLength);
    }

    /**
     * Add spawn for player instance
     * Inherits coords and heading from specified L2Character instance.
     * It could be either the player, or any killed/attacked mob
     * Return object id of newly spawned npc
     */    
    public int addSpawn(L2PcInstance player, int npcId, L2Character cha, boolean randomOffset)
    {
        return getPcSpawn(player).addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), randomOffset, 0);
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
     * Return true if contain player instance
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
        if (_instance == null) _instance = new QuestPcSpawnManager();
        return _instance;
    }
    
    /** Return quest pc spawn for specified player instance */
    public QuestPcSpawn getPcSpawn(L2PcInstance player)
    {
        for (int i = 0; i < getPcSpawns().size(); i++)
        {
            if (getPcSpawns().get(i).getPlayer() != null && getPcSpawns().get(i).getPlayer().getObjectId() == player.getObjectId())
                return getPcSpawns().get(i);
        }
        getPcSpawns().add(new QuestPcSpawn(player));
        return getPcSpawns().get(getPcSpawns().size() - 1);
    }

    /** Return all quest pc spawn */
    public List<QuestPcSpawn> getPcSpawns()
    {
        if (_pcSpawns == null) _pcSpawns = new FastList<QuestPcSpawn>();
        return _pcSpawns;
    }
}
