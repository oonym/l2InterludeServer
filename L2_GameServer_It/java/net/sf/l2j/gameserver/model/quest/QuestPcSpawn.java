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
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.AutoChatHandler;
import net.sf.l2j.gameserver.model.AutoSpawnHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.AutoChatHandler.AutoChatInstance;
import net.sf.l2j.gameserver.model.AutoSpawnHandler.AutoSpawnInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class QuestPcSpawn
{
    protected static final Logger _log = Logger.getLogger(QuestPcSpawn.class.getName());

    public class DeSpawnScheduleTimerTask implements Runnable
    {
        int _objectId = 0;
        public DeSpawnScheduleTimerTask(int objectId)
        {
            _objectId = objectId;
        }
        
        public void run()
        {
            try
            {
                removeSpawn(_objectId);
            } catch (Throwable t){}
        }
    }

    // =========================================================
    // Data Field
    private L2PcInstance _player;
    private List<AutoSpawnInstance> _autoSpawns = new FastList<AutoSpawnInstance>();
    private List<L2Spawn> _spawns = new FastList<L2Spawn>();
    
    // =========================================================
    // Constructor
    public QuestPcSpawn(L2PcInstance player)
    {
        _player = player;
    }

    // =========================================================
    // Method - Public
    /**
     * Add spawn for player instance
     * Return object id of newly spawned npc
     * Inherits player's coords and heading.
     * Adds a little randomization in the x y coords
     */
    public int addSpawn(int npcId)
    {
        return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), getPlayer().getHeading(), true, 0);
    }

    /**
     * Add spawn for player instance
     * Will despawn after the spawn length expires
     * Uses player's coords and heading.
     * Adds a little randomization in the x y coords
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId, int spawnLength)
    {
        return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), getPlayer().getHeading(), true, spawnLength);
    }

    /**
     * Add spawn for player instance
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId, int x, int y, int z)
    {
    	return addSpawn(npcId, x, y, z, 0, false, 0);
    }

    /**
     * Add spawn for player instance
     * Inherits coords and heading from specified L2Character instance.
     * It could be either the player, or any killed/attacked mob
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId, L2Character cha, boolean randomOffset)
    {
        return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), randomOffset, 0);
    }

    /**
     * Add spawn for player instance
     * Will despawn after the spawn length expires
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId, int x, int y, int z, int despawnDelay)
    {
        return addSpawn(npcId, x, y, z, 0, false, despawnDelay);
    }

    
    /**
     * Add spawn for player instance
     * Return object id of newly spawned npc
     */
    public int addSpawn(int npcId, int x, int y, int z,int heading, boolean randomOffset, int despawnDelay)
    {
        try 
        {
            L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
            if (template != null)
            {
                L2Spawn spawn = new L2Spawn(template);

                spawn.setId(IdFactory.getInstance().getNextId());
                spawn.setHeading(heading);

                // Sometimes, even if the quest script specifies some xyz (for example npc.getX() etc) by the time the code
            	// reaches here, xyz have become 0!  Also, a questdev might have purposely set xy to 0,0...however,
            	// the spawn code is coded such that if x=y=0, it looks into location for the spawn loc!  This will NOT work
            	// with quest spawns!  For both of the above cases, we need a fail-safe spawn.  For this, we use the 
                // default spawn location, which is at the player's loc.
                if ((x == 0) && (y == 0))
                {
                	_log.log(Level.WARNING, getPlayer().getName() + " requested quest spawn with loc 0,0.  Loc is being adjusted");

                	// attempt to use the player's xyz as a the default spawn location.
                	x = getPlayer().getClientX();
                	y = getPlayer().getClientY();
                	z = getPlayer().getClientZ();
                	// if the fail-safe also did not help, abort this spawning and give a severe log 
                    if ((x == 0) && (y == 0))
                    {
                    	_log.log(Level.SEVERE, "Failed to adjust bad locks for quest spawn requested by "+getPlayer().getName() + "!  Spawn aborted!");
                    	return 0;
                    }
                }
                
                if (randomOffset)
                {
                    int offset;

                    offset = Rnd.get(2); // Get the direction of the offset
                    if (offset == 0) {offset = -1;} // make offset negative
                    offset *= Rnd.get(50, 100);
                    x += offset;

                    offset = Rnd.get(2); // Get the direction of the offset
                    if (offset == 0) {offset = -1;} // make offset negative
                    offset *= Rnd.get(50, 100); 
                    y += offset;
                }
                
                spawn.setLocx(x);
                spawn.setLocy(y);
                spawn.setLocz(z + 20);
                spawn.stopRespawn();
                spawn.doSpawn();
                _spawns.add(spawn);
                int objectId = spawn.getLastSpawn().getObjectId();
                if (despawnDelay > 0)
                	addDeSpawnTask(objectId, despawnDelay);
                return objectId;
            }
        }
        catch (Exception e1)
        {
        	_log.warning("Could not spawn Npc " + npcId);
        }
          
          return 0;
    }

    
	/**
	 * This method has been kept for compatibility with MercTicketManager only
	 * and could be deprecated in the near future.
	 * 
	 * Any further Quest/AI related developments should take care of any other
	 * detail that is not strictly <b>spawn</b> related on their own.
	 * 
	 */
    public int addSpawn(int npcId, int x, int y, int z, int spawnLength, String[] messages, int chatDelay)
    {
        int objectId = addSpawn(npcId, x, y, z, 0, false, spawnLength);
        addDeSpawnTask(objectId, spawnLength);
        addChatTask(getSpawn(objectId).getLastSpawn(), messages, chatDelay);
        return objectId;
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
    // Kept for compatibility only
    @Deprecated
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
        return _player;
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
        if (_spawns == null)
            _spawns = new FastList<L2Spawn>();
        return _spawns;
    }
}
