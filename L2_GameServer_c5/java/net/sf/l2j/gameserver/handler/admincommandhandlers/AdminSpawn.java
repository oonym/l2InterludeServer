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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.SpawnTable;
import net.sf.l2j.gameserver.TeleportLocationTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.DayNightSpawnManager;
import net.sf.l2j.gameserver.instancemanager.RaidBossSpawnManager;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class handles following admin commands: - show_spawns = shows menu -
 * spawn_index lvl = shows menu for monsters with respective level -
 * spawn_monster id = spawns monster id on target
 * 
 * @version $Revision: 1.2.2.5.2.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminSpawn implements IAdminCommandHandler
{

    private static String[] _adminCommands = { "admin_show_spawns", "admin_spawn", "admin_spawn_monster", "admin_spawn_index",
                                               "admin_unspawnall","admin_respawnall","admin_spawn_reload",
					       "admin_teleport_reload", "admin_spawnnight", "admin_spawnday" };
    public static Logger _log = Logger.getLogger(AdminSpawn.class.getName());

    private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;
    private static final int REQUIRED_LEVEL2 = Config.GM_TELEPORT_OTHER;

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
            return false;

        if (command.equals("admin_show_spawns"))
        {
            AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
        }
        else if (command.startsWith("admin_spawn_index"))
        {
            try
            {
                String val = command.substring(18);
                AdminHelpPage.showHelpPage(activeChar, "spawns/" + val + ".htm");
            }
            catch (StringIndexOutOfBoundsException e)
            { }
        }
        else if (command.startsWith("admin_spawn")
                || command.startsWith("admin_spawn_monster"))
        {
            StringTokenizer st = new StringTokenizer(command, " ");
            try
            {
                st.nextToken();
                String id = st.nextToken();
                int respawnTime = 0; 
                //FIXME: 0 time should mean never respawn.
                //At the moment it will just be set to d elsewhere.
                int mobCount = 1;
                if (st.hasMoreTokens())
                    mobCount = Integer.parseInt(st.nextToken());
                if (st.hasMoreTokens())
                    respawnTime = Integer.parseInt(st.nextToken());
                spawnMonster(activeChar, id, respawnTime, mobCount);
            }
            catch (Exception e)
            {
                // Case of wrong monster data
            }
        }
        else if (command.startsWith("admin_unspawnall"))
        {
            for (L2PcInstance player : L2World.getInstance().getAllPlayers())
            {
                player.sendPacket(new SystemMessage(SystemMessage.NPC_SERVER_NOT_OPERATING));
            }

            RaidBossSpawnManager.getInstance().cleanUp();
            DayNightSpawnManager.getInstance().cleanUp();
            L2World.getInstance().deleteVisibleNpcSpawns();
            GmListTable.broadcastMessageToGMs("NPC Unspawn completed!");
        }
        else if (command.startsWith("admin_spawnday"))
        {
            DayNightSpawnManager.getInstance().spawnDayCreatures();
        }
        else if (command.startsWith("admin_spawnnight"))
        {
            DayNightSpawnManager.getInstance().spawnNightCreatures();
        }
        else if (command.startsWith("admin_respawnall") || command.startsWith("admin_spawn_reload"))
        {
            // make shure all spawns are deleted
            RaidBossSpawnManager.getInstance().cleanUp();
            DayNightSpawnManager.getInstance().cleanUp();
        	L2World.getInstance().deleteVisibleNpcSpawns();
        	
        	// now respawn all
            NpcTable.getInstance().reloadAllNpc();
            SpawnTable.getInstance().reloadAll();
            RaidBossSpawnManager.getInstance().reloadBosses();
            GmListTable.broadcastMessageToGMs("NPC Respawn completed!");
        } else if (command.startsWith("admin_teleport_reload"))
        {
            TeleportLocationTable.getInstance().reloadAll();
            GmListTable.broadcastMessageToGMs("Teleport List Table reloaded.");
	}
        return true;
    }

    public String[] getAdminCommandList()
    {
        return _adminCommands;
    }

    private boolean checkLevel(int level)
    {
        return (level >= REQUIRED_LEVEL);
    }

    private void spawnMonster(L2PcInstance activeChar, String monsterId, int respawnTime, int mobCount)
    {
        L2Object target = activeChar.getTarget();
        if (target == null)
        {
            target = activeChar;/*
            SystemMessage sm = new SystemMessage(614);
            sm.addString("Incorrect target.");
            activeChar.sendPacket(sm);
            return;*/
        }
	if (target != activeChar && activeChar.getAccessLevel() < REQUIRED_LEVEL2)
	    return;
       
        Pattern pattern =Pattern.compile("[0-9]*");
        Matcher regexp = pattern.matcher(monsterId);
        L2NpcTemplate template1;
        if (regexp.matches())
        {
         //First parameter was an ID number
            int monsterTemplate = Integer.parseInt(monsterId);
            template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
        }
        else {
//       //First parameter wasn't just numbers so go by name not ID
         monsterId = monsterId.replace('_', ' ');
         template1 = NpcTable.getInstance().getTemplateByName(monsterId); 
        }
        
       if (template1 == null)
        {
            //SystemMessage sm = new SystemMessage(614);
            //sm.addString("Incorrect monster template.");
            //activeChar.sendPacket(sm);
            //return;
            //template1 = NpcTable.getInstance().getTemplate(194);
            //template1.setNpcId(monsterTemplate);
        }

        try
        {
            //L2MonsterInstance mob = new L2MonsterInstance(template1);

            L2Spawn spawn = new L2Spawn(template1);
            spawn.setLocx(target.getX());
            spawn.setLocy(target.getY());
            spawn.setLocz(target.getZ());
            spawn.setAmount(mobCount);
            spawn.setHeading(activeChar.getHeading());
            spawn.setRespawnDelay(respawnTime);

            if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcid()))
            {
                SystemMessage sm = new SystemMessage(614);
                sm.addString("You cannot spawn another instance of " + template1.name + ".");
                activeChar.sendPacket(sm);
            } 
            else
            {
                if (RaidBossSpawnManager.getInstance().getValidTemplate(spawn.getNpcid()) != null)
                    RaidBossSpawnManager.getInstance().addNewSpawn(spawn, 0, template1.getStatsSet().getDouble("baseHpMax"), template1.getStatsSet().getDouble("baseMpMax"), true);
                else
                    SpawnTable.getInstance().addNewSpawn(spawn, true);

                spawn.init();

                SystemMessage sm = new SystemMessage(614);
                sm.addString("Created " + template1.name + " on " + target.getObjectId() + ".");
                activeChar.sendPacket(sm);
            } 
        }
        catch (Exception e)
        {
            SystemMessage sm = new SystemMessage(614);
            sm.addString("Target is not ingame.");
            activeChar.sendPacket(sm);
        }
    }
}
