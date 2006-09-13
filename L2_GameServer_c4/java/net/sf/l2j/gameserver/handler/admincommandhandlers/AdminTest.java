/*
 * $Header: AdminTest.java, 25/07/2005 17:15:21 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 25/07/2005 17:15:21 $
 * $Revision: 1 $
 * $Log: AdminTest.java,v $
 * Revision 1  25/07/2005 17:15:21  luisantonioa
 * Added copyright notice
 *
 * 
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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SelectorThread;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.Universe;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.waypoint.WayPointNode;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;


/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class AdminTest implements IAdminCommandHandler
{
    private static final int REQUIRED_LEVEL = Config.GM_TEST;
    public static final String[] ADMIN_TEST_COMMANDS =
    {
        "admin_test", "admin_stats", "admin_skill_test", 
        "admin_st", "admin_mp", "admin_known"
    };

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (activeChar.getAccessLevel() < REQUIRED_LEVEL) return false;
        
        if (command.equals("admin_stats"))
        {
            for (String line : ThreadPoolManager.getInstance().getStats())
            {
                activeChar.sendMessage(line);
            }
        }
        else if (command.startsWith("admin_skill_test") || command.startsWith("admin_st"))
        {
            try
            {
                StringTokenizer st = new StringTokenizer(command);
                st.nextToken();
                int id = Integer.parseInt(st.nextToken());
                adminTestSkill(activeChar,id);
            }
            catch(NumberFormatException e)
            {
                activeChar.sendMessage("Command format is //skill_test <ID>");
            }
            catch(NoSuchElementException nsee)
            {
                activeChar.sendMessage("Command format is //skill_test <ID>");
            }
        }
        else if (command.startsWith("admin_test hash "))
        {
            try
            {
                int count = Integer.parseInt(command.substring("admin_test hash ".length()));
                testHashMap(activeChar, count);
            }
            catch (NumberFormatException e)
            {
                activeChar.sendMessage("Command format is //test hash <Number>");
            }
            catch (StringIndexOutOfBoundsException e)
            { }
        }
        else if (command.startsWith("admin_test uni flush"))
        {
            Universe.getInstance().flush();
            activeChar.sendMessage("Universe Map Saved.");
        }
        else if (command.startsWith("admin_test uni"))
        {
            activeChar.sendMessage("Universe Map Size is: "+Universe.getInstance().size());
        }
        else if (command.equals("admin_test hash"))
        {
            testHashMap(activeChar);
        }
                else if (command.equals("admin_mp on"))
        {
            SelectorThread.startPacketMonitor();
            activeChar.sendMessage("Packet monitor enabled");
        }
        else if (command.equals("admin_mp off"))
        {
            SelectorThread.stopPacketMonitor();
            activeChar.sendMessage("Packet monitor disabled");
        }
        else if (command.equals("admin_mp dump"))
        {
            SelectorThread.dumpPacketHistory();
            activeChar.sendMessage("Packet history saved");
        }
        else if (command.equals("admin_known on"))
        {
            Config.CHECK_KNOWN = true;
        }
        else if (command.equals("admin_known off"))
        {
            Config.CHECK_KNOWN = false;
        }
        return true;
    }
    
    /**
     * @param activeChar
     * @param id
     */
    private void adminTestSkill(L2PcInstance activeChar, int id)
    {
        L2Character player;
        L2Object target = activeChar.getTarget();
        if(target == null || !(target instanceof L2Character))
        {
            player = activeChar;
        }
        else
        {
            player = (L2Character)target;
        }
        player.broadcastPacket(new MagicSkillUser(activeChar, player, id, 1, 1, 1));
        
    }

    private void testHashMap(L2PcInstance activeChar) { testHashMap(activeChar, 1); }

    /**
     * @param activeChar
     */
    private void testHashMap(L2PcInstance activeChar, int objectCount)
    {
        WayPointNode[] nodes    = new WayPointNode[objectCount];
        for (int i = 0; i < objectCount; i++)
        {
            nodes[i]    = new WayPointNode(IdFactory.getInstance().getNextId());
            nodes[i].setXYZ(0, 0, 0);
        }
        long start = 0, end = 0;//System.out.println(System.currentTimeMillis());
        start               = System.currentTimeMillis();
        for (WayPointNode node : nodes)
        {
            L2World.getInstance().addVisibleObject(node, L2World.getInstance().getRegion(0, 0), null);
        }
        end                 = System.currentTimeMillis();
        long timeStore      = new Long(end - start);//System.out.println(System.currentTimeMillis());
        
        start               = System.currentTimeMillis();
        for (WayPointNode node : nodes)
        {
            L2World.getInstance().findObject(node.getObjectId());
        }
        end                 = System.currentTimeMillis();
        long timeFind       = (end - start);

        start               = System.currentTimeMillis();
        for (WayPointNode node : nodes)
        {
            L2World.getInstance().removeVisibleObject(node, L2World.getInstance().getRegion(0, 0));
        }
        end                 = System.currentTimeMillis();
        long timeRemove     = (end - start);
        
        for (int i = 0; i < objectCount; i++)
        {
            IdFactory.getInstance().releaseId(nodes[i].getObjectId());
        }
        
        nodes   = null;
        
        activeChar.sendMessage("Testing HashMap on " + objectCount + " object(s).");
        activeChar.sendMessage("WorldObjectTable Insert:     " + timeStore + " ms.");
        activeChar.sendMessage("WorldObjectTable Retreive: " + timeFind + " ms.");
        activeChar.sendMessage("WorldObjectTable Remove:  " + timeRemove + " ms.");
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
     */
    public String[] getAdminCommandList()
    {
        return ADMIN_TEST_COMMANDS;
    }

}
