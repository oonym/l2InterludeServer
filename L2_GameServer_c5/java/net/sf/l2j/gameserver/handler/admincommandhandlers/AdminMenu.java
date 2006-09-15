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

import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class handles following admin commands:
 * - handles ever admin menu command
 * 
 * @version $Revision: 1.3.2.6.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminMenu implements IAdminCommandHandler 
{
	private static final Logger _log = Logger.getLogger(AdminMenu.class.getName());

	private static String[] _adminCommands = {
		"admin_char_manage",
		"admin_teleport_character_to_menu",
		"admin_recall_char_menu",
		"admin_goto_char_menu",
		"admin_kick_menu",
		"admin_kill_menu",
		"admin_ban_menu",
		"admin_unban_menu"
		};
	private static final int REQUIRED_LEVEL = Config.GM_ACCESSLEVEL;

	public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
		
		String target = (activeChar.getTarget() != null?activeChar.getTarget().getName():"no-target");
        GMAudit.auditGMAction(activeChar.getName(), command, target, "");

        if (command.equals("admin_char_manage"))
		{
			AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
		}
		else if (command.startsWith("admin_teleport_character_to_menu"))
        {
			String[] data = command.split(" ");
            if(data.length==5)
            {
				String playerName=data[1];
                int x=Integer.parseInt(data[2]);
                int y=Integer.parseInt(data[3]);
                int z=Integer.parseInt(data[4]);
                L2PcInstance player = L2World.getInstance().getPlayer(playerName);
                if(player!=null)
                {
					teleportCharacter(player,x,y,z,activeChar);
                }
			}
			AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
		}
		else if (command.startsWith("admin_recall_char_menu"))
		{
            try
            {
    		    String targetName = command.substring(23);
    		    L2PcInstance player = L2World.getInstance().getPlayer(targetName);
    		    int x = activeChar.getX();
    			int y = activeChar.getY();
    			int z = activeChar.getZ();
    		    teleportCharacter(player,x,y,z,activeChar);
            }
            catch (StringIndexOutOfBoundsException e)
            { }
		}
		else if (command.startsWith("admin_goto_char_menu"))
		{
            try
            {
    		    String targetName = command.substring(21);
    		    L2PcInstance player = L2World.getInstance().getPlayer(targetName);
    	        teleportToCharacter(activeChar, player);
            }
            catch (StringIndexOutOfBoundsException e)
            { }
		}
		else if (command.equals("admin_kill_menu"))
		{
			handleKill(activeChar);
		}
		else if (command.startsWith("admin_kick_menu"))
        {
            //System.out.println("ADMIN KICK");
            StringTokenizer st = new StringTokenizer(command);
            //System.out.println("Tokens: "+st.countTokens());
            if (st.countTokens() > 1)
            {
                st.nextToken();
                String player = st.nextToken();
                //System.out.println("Player1 "+player);
                L2PcInstance plyr = L2World.getInstance().getPlayer(player);
				SystemMessage sm = new SystemMessage(614);
				if (plyr != null)
				{
					//System.out.println("Player2 "+plyr.getName());
					plyr.logout();
					sm.addString("You kicked " + plyr.getName() + " from the game.");
				}
				else
				{
					sm.addString("Player " + player + " was not found in the game.");
				}
				activeChar.sendPacket(sm);
            }
			AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
        }
		else if (command.startsWith("admin_ban_menu"))
        {
            StringTokenizer st = new StringTokenizer(command);
            if (st.countTokens() > 1)
            {
                st.nextToken();
                String player = st.nextToken();
                L2PcInstance plyr = L2World.getInstance().getPlayer(player);
                if (plyr != null)
                {
                    plyr.logout();
                }
                setAccountAccessLevel(player, activeChar, -100);
            }
			AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
        }
		else if (command.startsWith("admin_unban_menu"))
        {
            StringTokenizer st = new StringTokenizer(command);
            if (st.countTokens() > 1)
            {
				st.nextToken();
                String player = st.nextToken();
                setAccountAccessLevel(player, activeChar, 0);
			}
			AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
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
    private void handleKill(L2PcInstance activeChar)
    {
        handleKill(activeChar, null);
    }
	private void handleKill(L2PcInstance activeChar, String player) {
		L2Object obj = activeChar.getTarget();
        if (player != null)
        {
            L2PcInstance plyr = L2World.getInstance().getPlayer(player);
            if (plyr != null)
            {
                obj = plyr;
            }
			SystemMessage sm = new SystemMessage(614);
			sm.addString("You killed " + plyr.getName() + ".");
			activeChar.sendPacket(sm);
        }
        
		if ((obj != null) && (obj instanceof L2Character))
		{
			L2Character target = (L2Character)obj;
			target.reduceCurrentHp(target.getMaxHp()+1, activeChar);
		}
		else
		{
			SystemMessage sm = new SystemMessage(614);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
		}
		AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
	}
    private void teleportCharacter(L2PcInstance player, int x, int y, int z, L2PcInstance activeChar)
    {
        if (player != null) {
            SystemMessage sm = new SystemMessage(614);
            sm.addString("Admin is teleporting you.");
            player.sendPacket(sm);

    		player.teleToLocation(x, y, z);
        }
		AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
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
			SystemMessage sm = new SystemMessage(614);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
			return;
		}
		
		if (player.getObjectId() == activeChar.getObjectId())
		{	
			SystemMessage sm = new SystemMessage(614);
			sm.addString("You cannot self teleport.");
			activeChar.sendPacket(sm);
		}
		else
		{
			int x = player.getX();
			int y = player.getY();
			int z = player.getZ();
			
			activeChar.teleToLocation(x, y, z);
		
			SystemMessage sm = new SystemMessage(614);
			sm.addString("You have teleported to character " + player.getName() + ".");
			activeChar.sendPacket(sm);
		}
		AdminHelpPage.showHelpPage(activeChar, "charmanage.htm");
	}
	private void setAccountAccessLevel(String user, L2PcInstance player, int banLevel)
    {
        java.sql.Connection con = null;
        try
        {           
            con = L2DatabaseFactory.getInstance().getConnection();
            
            String stmt = "UPDATE accounts, characters SET accounts.access_level = ? WHERE characters.account_name = accounts.login AND characters.char_name=?";
            PreparedStatement statement = con.prepareStatement(stmt);
            statement.setInt(1, banLevel);
            statement.setString(2, user);
            statement.executeUpdate();
            statement.close();
            
            SystemMessage sm = new SystemMessage(614);
            sm.addString("Account Access Level for "+user+" set to "+banLevel+".");
            player.sendPacket(sm);
        }
        catch (Exception e)
        {
            _log.warning("Could not set accessLevl:"+e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
}