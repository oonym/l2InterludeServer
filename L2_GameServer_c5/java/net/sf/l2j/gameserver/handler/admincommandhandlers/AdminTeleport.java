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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.text.TextBuilder;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.datatables.SpawnTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class handles following admin commands:
 * - show_moves
 * - show_teleport
 * - teleport_to_character
 * - move_to
 * - teleport_character
 * 
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminTeleport implements IAdminCommandHandler 
{
    private static final Logger _log = Logger.getLogger(AdminTeleport.class.getName());
    
    private static String[] _adminCommands = {
        "admin_show_moves",
        "admin_show_moves_other",
        "admin_show_teleport",
        "admin_teleport_to_character",
        "admin_teleportto",
        "admin_move_to",
        "admin_teleport_character",
        "admin_recall",
        "admin_walk",
        "admin_explore",
        "teleportto",
        "recall",
        "admin_recall_npc",
        "admin_gonorth",
        "admin_gosouth",
        "admin_goeast",
        "admin_gowest",
        "admin_goup",
        "admin_godown",
        "admin_tele",
        "admin_teleto",
        "admin_failed"
    };
    private static final int REQUIRED_LEVEL = Config.GM_TELEPORT;
    private static final int REQUIRED_LEVEL2 = Config.GM_TELEPORT_OTHER;
    
    public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
        
		String target = (activeChar.getTarget() != null?activeChar.getTarget().getName():"no-target");
        GMAudit.auditGMAction(activeChar.getName(), command, target, "");

        if (command.equals("admin_teleto"))
        {
            activeChar.setTeleMode(1);
        }
        if (command.equals("admin_teleto r"))
        {
            activeChar.setTeleMode(2);
        }
        if (command.equals("admin_teleto end"))
        {
            activeChar.setTeleMode(0);
        }
        if (command.equals("admin_show_moves"))
        {
            AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
        }
        if (command.equals("admin_show_moves_other"))
        {
            AdminHelpPage.showHelpPage(activeChar, "tele/other.html");
        }
        else if (command.equals("admin_show_teleport"))
        {
            showTeleportCharWindow(activeChar);
        }
        else if (command.equals("admin_recall_npc"))
        {
            recallNPC(activeChar);
        }
        else if (command.equals("admin_teleport_to_character"))
        {
            teleportToCharacter(activeChar, activeChar.getTarget());
        }
        else if (command.equals("admin_explore") && Config.ACTIVATE_POSITION_RECORDER)
        {
            activeChar._exploring = ! activeChar._exploring;
            activeChar.explore();
        }
        else if (command.startsWith("admin_walk"))
        {
            try
            {
                String val = command.substring(11);
                StringTokenizer st = new StringTokenizer(val);
                String x1 = st.nextToken();
                int x = Integer.parseInt(x1);
                String y1 = st.nextToken();
                int y = Integer.parseInt(y1);
                String z1 = st.nextToken();
                int z = Integer.parseInt(z1);
                L2CharPosition pos = new L2CharPosition(x,y,z,0);
                activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO,pos);
            }
            catch (Exception e)
            {
                if (Config.DEBUG) _log.info("admin_walk: "+e);
            }
        }
        else if (command.startsWith("admin_move_to"))
        {
            try
            {
                String val = command.substring(14);
                teleportTo(activeChar, val);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                //Case of empty co-ordinates
                activeChar.sendMessage("Wrong or no Co-ordinates given.");
            }		
        }
        else if (command.startsWith("admin_teleport_character"))
        {
            try
            {
                String val = command.substring(25); 
                
                if (activeChar.getAccessLevel()>=REQUIRED_LEVEL2)
            	    teleportCharacter(activeChar, val);
            }
            catch (StringIndexOutOfBoundsException e)
            {
                //Case of empty co-ordinates
                activeChar.sendMessage("Wrong or no Co-ordinates given.");
                
                showTeleportCharWindow(activeChar); //back to character teleport
            }
        }
        else if (command.startsWith("admin_teleportto "))
        {
            try
            {
                String targetName = command.substring(17);
                L2PcInstance player = L2World.getInstance().getPlayer(targetName);
                teleportToCharacter(activeChar, player);
            }
            catch (StringIndexOutOfBoundsException e)
            { }
        }
        else if (command.startsWith("admin_recall "))
        {
            try
            {
                String targetName = command.substring(13);
                L2PcInstance player = L2World.getInstance().getPlayer(targetName);
                if (activeChar.getAccessLevel()>=REQUIRED_LEVEL2)
            	    teleportCharacter(player, activeChar.getX(), activeChar.getY(), activeChar.getZ());
            }
            catch (StringIndexOutOfBoundsException e)
            { }
        }
        else if (command.startsWith("admin_failed"))
        {
            SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
            sm.addString("Trying ActionFailed...");
            activeChar.sendPacket(sm);
            activeChar.sendPacket(new ActionFailed());
        }
        else if (command.equals("admin_tele"))
        {
            showTeleportWindow(activeChar);
        }
        else if (command.equals("admin_goup"))
        {
            int x = activeChar.getX();
            int y = activeChar.getY();
            int z = activeChar.getZ()+150;
            activeChar.teleToLocation(x, y, z, false);
            showTeleportWindow(activeChar);
        }
        else if (command.startsWith("admin_goup"))
        {
            try
            {
            	String val = command.substring(11);
                int intVal = Integer.parseInt(val);
                int x = activeChar.getX();
                int y = activeChar.getY();
                int z = activeChar.getZ()+intVal;
                activeChar.teleToLocation(x, y, z, false);
                showTeleportWindow(activeChar);
            }
            catch (StringIndexOutOfBoundsException e) {}
            catch (NumberFormatException nfe) {}
        }
        else if (command.equals("admin_godown"))
        {
            int x = activeChar.getX();
            int y = activeChar.getY();
            int z = activeChar.getZ();
            activeChar.teleToLocation(x, y, z - 150, false);
            showTeleportWindow(activeChar);
        }
        else if (command.startsWith("admin_godown"))
        {
            try
            {
            	String val = command.substring(13);
                int intVal = Integer.parseInt(val);
                int x = activeChar.getX();
                int y = activeChar.getY();
                int z = activeChar.getZ()-intVal;
                activeChar.teleToLocation(x, y, z, false);
                showTeleportWindow(activeChar);
            }
            catch (StringIndexOutOfBoundsException e) {}
            catch (NumberFormatException nfe) {}
        }
        else if (command.equals("admin_goeast"))
        {
            int x = activeChar.getX();
            int y = activeChar.getY();
            int z = activeChar.getZ();
            activeChar.teleToLocation(x+150, y, z, false);
            showTeleportWindow(activeChar);
        }
        else if (command.startsWith("admin_goeast"))
        {
            try
            {
            	String val = command.substring(13);
                int intVal = Integer.parseInt(val);
                int x = activeChar.getX()+intVal;
                int y = activeChar.getY();
                int z = activeChar.getZ();
                activeChar.teleToLocation(x, y, z, false);
                showTeleportWindow(activeChar);
            }
            catch (StringIndexOutOfBoundsException e) {}
            catch (NumberFormatException nfe) {}
        }
        else if (command.equals("admin_gowest"))
        {
            int x = activeChar.getX();
            int y = activeChar.getY();
            int z = activeChar.getZ();
            activeChar.teleToLocation(x-150, y, z, false);
            showTeleportWindow(activeChar);
        }
        else if (command.startsWith("admin_gowest"))
        {
            try
            {
            	String val = command.substring(13);
                int intVal = Integer.parseInt(val);
                int x = activeChar.getX()-intVal;
                int y = activeChar.getY();
                int z = activeChar.getZ();
                activeChar.teleToLocation(x, y, z, false);
                showTeleportWindow(activeChar);
            }
            catch (StringIndexOutOfBoundsException e) {}
            catch (NumberFormatException nfe) {}
        }
        else if (command.equals("admin_gosouth"))
        {
            int x = activeChar.getX();
            int y = activeChar.getY()+150;
            int z = activeChar.getZ();
            activeChar.teleToLocation(x, y, z, false);
            showTeleportWindow(activeChar);
        }
        else if (command.startsWith("admin_gosouth"))
        {
            try
            {
            	String val = command.substring(14);
                int intVal = Integer.parseInt(val);
                int x = activeChar.getX();
                int y = activeChar.getY()+intVal;
                int z = activeChar.getZ();
                activeChar.teleToLocation(x, y, z, false);
                showTeleportWindow(activeChar);
            }
            catch (StringIndexOutOfBoundsException e) {}
            catch (NumberFormatException nfe) {}
        }
        else if (command.equals("admin_gonorth"))
        {
            int x = activeChar.getX();
            int y = activeChar.getY();
            int z = activeChar.getZ();
            activeChar.teleToLocation(x, y-150, z, false);
            showTeleportWindow(activeChar);
        }
        else if (command.startsWith("admin_gonorth"))
        {
            try
            {
            	String val = command.substring(14);
                int intVal = Integer.parseInt(val);
                int x = activeChar.getX();
                int y = activeChar.getY()-intVal;
                int z = activeChar.getZ();
                activeChar.teleToLocation(x, y, z, false);
                showTeleportWindow(activeChar);
            }
            catch (StringIndexOutOfBoundsException e) {}
            catch (NumberFormatException nfe) {}
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
    
    private void teleportTo(L2PcInstance activeChar, String Cords)
    {
        try
        {
            StringTokenizer st = new StringTokenizer(Cords);
            String x1 = st.nextToken();
            int x = Integer.parseInt(x1);
            String y1 = st.nextToken();
            int y = Integer.parseInt(y1);
            String z1 = st.nextToken();
            int z = Integer.parseInt(z1);
            
            activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            activeChar.teleToLocation(x, y, z, false);
            
            SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
            sm.addString("You have been teleported to " + Cords);
            activeChar.sendPacket(sm);
        } catch (NoSuchElementException nsee)
        {
            activeChar.sendMessage("Wrong or no Co-ordinates given.");
        }
    }
    
    
    private void showTeleportWindow(L2PcInstance activeChar)
    {
        
        NpcHtmlMessage adminReply = new NpcHtmlMessage(5); 
        
        TextBuilder replyMSG = new TextBuilder("<html><title>Teleport Menu</title>");
        replyMSG.append("<body>");
        
        replyMSG.append("<br>");
        replyMSG.append("<center><table>");
        
        replyMSG.append("<tr><td><button value=\"  \" action=\"bypass -h admin_tele\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td><button value=\"North\" action=\"bypass -h admin_gonorth\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td><button value=\"Up\" action=\"bypass -h admin_goup\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
        replyMSG.append("<tr><td><button value=\"West\" action=\"bypass -h admin_gowest\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td><button value=\"  \" action=\"bypass -h admin_tele\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td><button value=\"East\" action=\"bypass -h admin_goeast\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
        replyMSG.append("<tr><td><button value=\"  \" action=\"bypass -h admin_tele\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td><button value=\"South\" action=\"bypass -h admin_gosouth\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td><button value=\"Down\" action=\"bypass -h admin_godown\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
        
        replyMSG.append("</table></center>");
        replyMSG.append("</body></html>");
        
        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply);			
    }
    
    
    private void showTeleportCharWindow(L2PcInstance activeChar)
    {
        L2Object target = activeChar.getTarget();
        L2PcInstance player = null;
        if (target instanceof L2PcInstance) 
        {
            player = (L2PcInstance)target;
        } 
        else 
        {
            activeChar.sendMessage("Incorrect target.");
            return;
        }
        NpcHtmlMessage adminReply = new NpcHtmlMessage(5); 
        
        TextBuilder replyMSG = new TextBuilder("<html><title>Teleport Character</title>");
        replyMSG.append("<body>");
        replyMSG.append("The character you will teleport is " + player.getName() + ".");
        replyMSG.append("<br>");
        
        replyMSG.append("Co-ordinate x");
        replyMSG.append("<edit var=\"char_cord_x\" width=110>");
        replyMSG.append("Co-ordinate y");
        replyMSG.append("<edit var=\"char_cord_y\" width=110>");
        replyMSG.append("Co-ordinate z");
        replyMSG.append("<edit var=\"char_cord_z\" width=110>");
        replyMSG.append("<button value=\"Teleport\" action=\"bypass -h admin_teleport_character $char_cord_x $char_cord_y $char_cord_z\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");	
        replyMSG.append("<button value=\"Teleport near you\" action=\"bypass -h admin_teleport_character " + activeChar.getX() + " " + activeChar.getY() + " " + activeChar.getZ() + "\" width=115 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        replyMSG.append("<center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
        replyMSG.append("</body></html>");
        
        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply);			
    }
    
    private void teleportCharacter(L2PcInstance activeChar , String Cords)
    {
        L2Object target = activeChar.getTarget();
        L2PcInstance player = null;
        if (target instanceof L2PcInstance) 
        {
            player = (L2PcInstance)target;
        } 
        else 
        {
            activeChar.sendMessage("Incorrect target.");
            return;
        }
        
        if (player.getObjectId() == activeChar.getObjectId())
        {
            player.sendMessage("You cannot teleport your character.");
        }
        else
        {
            try
            {
                StringTokenizer st = new StringTokenizer(Cords);
                String x1 = st.nextToken();
                int x = Integer.parseInt(x1);
                String y1 = st.nextToken();
                int y = Integer.parseInt(y1);
                String z1 = st.nextToken();
                int z = Integer.parseInt(z1);
                teleportCharacter(player, x,y,z);
            } catch (NoSuchElementException nsee) {}
        }
    }
    
    /**
     * @param player
     * @param x
     * @param y
     * @param z
     */
    private void teleportCharacter(L2PcInstance player, int x, int y, int z)
    {
        if (player != null) {
            //Common character information
            player.sendMessage("Admin is teleporting you.");
            
            player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            player.teleToLocation(x, y, z, true);
        }
    }
    
    private void teleportToCharacter(L2PcInstance activeChar, L2Object target)
    {
        L2PcInstance player = null;
        if (target != null && target instanceof L2PcInstance) 
        {
            player = (L2PcInstance)target;
        } 
        else 
        {
            activeChar.sendMessage("Incorrect target.");
            return;
        }
        
        if (player.getObjectId() == activeChar.getObjectId())
        {	
            activeChar.sendMessage("You cannot self teleport.");
        }
        else
        {
            int x = player.getX();
            int y = player.getY();
            int z = player.getZ();
            
            activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
            activeChar.teleToLocation(x, y, z, true);
            
            activeChar.sendMessage("You have teleported to character " + player.getName() + ".");
        }
    }
    private void recallNPC(L2PcInstance activeChar)
    {
        L2Object obj = activeChar.getTarget();
        if ((obj != null) && (obj instanceof L2NpcInstance))
        {
            L2NpcInstance target = (L2NpcInstance) obj;
            
            int monsterTemplate = target.getTemplate().npcId;
            L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
            if (template1 == null)
            {
                activeChar.sendMessage("Incorrect monster template.");
                _log.warning("ERROR: NPC " + target.getObjectId() + " has a 'null' template.");
                return;
            }

            L2Spawn spawn = target.getSpawn();
            if (spawn == null)
            {
                activeChar.sendMessage("Incorrect monster spawn.");
                _log.warning("ERROR: NPC " + target.getObjectId() + " has a 'null' spawn.");
                return;
            }
            int respawnTime = spawn.getRespawnDelay();
            
            target.deleteMe();
            spawn.stopRespawn();
            SpawnTable.getInstance().deleteSpawn(spawn, true);
            
            try
            {
                //L2MonsterInstance mob = new L2MonsterInstance(monsterTemplate, template1);
                
                spawn = new L2Spawn(template1);
                spawn.setLocx(activeChar.getX());
                spawn.setLocy(activeChar.getY());
                spawn.setLocz(activeChar.getZ());
                spawn.setAmount(1);
                spawn.setHeading(activeChar.getHeading());
                spawn.setRespawnDelay(respawnTime);
                SpawnTable.getInstance().addNewSpawn(spawn, true);
                spawn.init();
                
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("Created " + template1.name + " on " + target.getObjectId() + ".");
                activeChar.sendPacket(sm);
                
                if (Config.DEBUG)
                {
                    _log.fine("Spawn at X="+spawn.getLocx()+" Y="+spawn.getLocy()+" Z="+spawn.getLocz());
                    _log.warning("GM: "+activeChar.getName()+"("+activeChar.getObjectId()+") moved NPC " + target.getObjectId());
                }
            }
            catch (Exception e)
            {
                activeChar.sendMessage("Target is not in game.");
            }
            
        }
        else
        {
            activeChar.sendMessage("Incorrect target.");
        }
    }
    
}
