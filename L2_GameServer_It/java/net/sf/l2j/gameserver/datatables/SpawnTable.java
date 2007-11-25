/*
 * This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.instancemanager.DayNightSpawnManager;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 *
 * @author Nightmare
 * @version $Revision: 1.5.2.6.2.7 $ $Date: 2005/03/27 15:29:18 $
 */
public class SpawnTable
{
    private static Logger _log = Logger.getLogger(SpawnTable.class.getName());

    private static final SpawnTable _instance = new SpawnTable();

    private Map<Integer, L2Spawn> _spawntable = new FastMap<Integer, L2Spawn>();
    private int _npcSpawnCount;

    private int _highestId;

    public static SpawnTable getInstance()
    {
        return _instance;
    }

    private SpawnTable()
    {
    	if (!Config.ALT_DEV_NO_SPAWNS)
    		fillSpawnTable();
    }

    public Map<Integer, L2Spawn> getSpawnTable()
    {
        return _spawntable;
    }

    private void fillSpawnTable()
    {
        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT id, count, npc_templateid, locx, locy, locz, heading, respawn_delay, loc_id, periodOfDay FROM spawnlist ORDER BY id");
            ResultSet rset = statement.executeQuery();

            L2Spawn spawnDat;
            L2NpcTemplate template1;

            while (rset.next())
            {
                template1 = NpcTable.getInstance().getTemplate(rset.getInt("npc_templateid"));
                if (template1 != null)
                {
                    if (template1.type.equalsIgnoreCase("L2SiegeGuard"))
                    {
                        // Don't spawn
                    }
                    else if (template1.type.equalsIgnoreCase("L2RaidBoss"))
                    {
                        // Don't spawn raidboss
                    }
                    else if (!Config.ALLOW_CLASS_MASTERS && template1.type.equals("L2ClassMaster"))
                    {
                        // Dont' spawn class masters
                    }
                    else
                    {
                        spawnDat = new L2Spawn(template1);
                        spawnDat.setId(rset.getInt("id"));
                        spawnDat.setAmount(rset.getInt("count"));
                        spawnDat.setLocx(rset.getInt("locx"));
                        spawnDat.setLocy(rset.getInt("locy"));
                        spawnDat.setLocz(rset.getInt("locz"));
                        spawnDat.setHeading(rset.getInt("heading"));
                        spawnDat.setRespawnDelay(rset.getInt("respawn_delay"));
                        int loc_id = rset.getInt("loc_id");
                        spawnDat.setLocation(loc_id);

                        switch(rset.getInt("periodOfDay")) {
                            case 0: // default
                                _npcSpawnCount += spawnDat.init();
                                break;
                            case 1: // Day
                                DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
                                _npcSpawnCount++;
                                break;
                            case 2: // Night
                                DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
                                _npcSpawnCount++;
                                break;
                        }

                        _spawntable.put(spawnDat.getId(), spawnDat);
                        if (spawnDat.getId() > _highestId) _highestId = spawnDat.getId();
                    }
                }
                else
                {
                    _log.warning("SpawnTable: Data missing in NPC table for ID: "
                        + rset.getInt("npc_templateid") + ".");
                }
            }
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            // problem with initializing spawn, go to next one
            _log.warning("SpawnTable: Spawn could not be initialized: " + e);
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
        }

        _log.config("SpawnTable: Loaded " + _spawntable.size() + " Npc Spawn Locations.");

        if (Config.DEBUG)
            _log.fine("SpawnTable: Spawning completed, total number of NPCs in the world: "
                + _npcSpawnCount);

    }

    public L2Spawn getTemplate(int id)
    {
        return _spawntable.get(id);
    }

    public void addNewSpawn(L2Spawn spawn, boolean storeInDb)
    {
        _highestId++;
        spawn.setId(_highestId);
        _spawntable.put(_highestId, spawn);

        if (storeInDb)
        {
            java.sql.Connection con = null;

            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement("INSERT INTO spawnlist (id,count,npc_templateid,locx,locy,locz,heading,respawn_delay,loc_id) values(?,?,?,?,?,?,?,?,?)");
                statement.setInt(1, spawn.getId());
                statement.setInt(2, spawn.getAmount());
                statement.setInt(3, spawn.getNpcid());
                statement.setInt(4, spawn.getLocx());
                statement.setInt(5, spawn.getLocy());
                statement.setInt(6, spawn.getLocz());
                statement.setInt(7, spawn.getHeading());
                statement.setInt(8, spawn.getRespawnDelay() / 1000);
                statement.setInt(9, spawn.getLocation());
                statement.execute();
                statement.close();
            }
            catch (Exception e)
            {
                // problem with storing spawn
                _log.warning("SpawnTable: Could not store spawn in the DB:" + e);
            }
            finally
            {
                try
                {
                    con.close();
                }
                catch (Exception e)
                {
                }
            }
        }
    }

    public void deleteSpawn(L2Spawn spawn, boolean updateDb)
    {
        if (_spawntable.remove(spawn.getId()) == null) return;

        if (updateDb)
        {
            java.sql.Connection con = null;

            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement("DELETE FROM spawnlist WHERE id=?");
                statement.setInt(1, spawn.getId());
                statement.execute();
                statement.close();
            }
            catch (Exception e)
            {
                // problem with deleting spawn
                _log.warning("SpawnTable: Spawn " + spawn.getId() + " could not be removed from DB: "
                    + e);
            }
            finally
            {
                try
                {
                    con.close();
                }
                catch (Exception e)
                {
                }
            }
        }
    }

    //just wrapper
    public void reloadAll()
    {
        fillSpawnTable();
    }

    /**
     * Get all the spawn of a NPC<BR><BR>
     *
     * @param npcId : ID of the NPC to find.
     * @return
     */
    public void findNPCInstances(L2PcInstance activeChar, int npcId, int teleportIndex)
    {
        int index = 0;
        for (L2Spawn spawn : _spawntable.values())
        {
            if (npcId == spawn.getNpcid())
            {
                index++;

                if (teleportIndex > -1)
                {
                    if (teleportIndex == index)
                        activeChar.teleToLocation(spawn.getLocx(), spawn.getLocy(), spawn.getLocz(), true);
                }
                else
                {
                    activeChar.sendMessage(index + " - " + spawn.getTemplate().name + " ("
                        + spawn.getId() + "): " + spawn.getLocx() + " " + spawn.getLocy() + " "
                        + spawn.getLocz());
                }
            }
        }

        if (index == 0) activeChar.sendMessage("No current spawns found.");
    }
}
