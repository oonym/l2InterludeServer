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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.CursedWeaponsManager;
import net.sf.l2j.gameserver.model.CursedWeapon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class handles following admin commands:
 * - kill = kills target L2Character
 * 
 * @version $Revision: 1.1.6.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminCursedWeapons implements IAdminCommandHandler {
    //private static Logger _log = Logger.getLogger(AdminBan.class.getName());
    private static String[] _adminCommands = {"admin_cw_infos", "admin_cw_remove", "admin_cw_goto", "admin_cw_reload"};
    private static final int REQUIRED_LEVEL = Config.GM_MIN;

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!Config.ALT_PRIVILEGES_ADMIN)
        {
            if (!(checkLevel(activeChar.getAccessLevel())))
            {
                return false;
            }
        }

        CursedWeaponsManager cwm = CursedWeaponsManager.getInstance();
    	       
        StringTokenizer st = new StringTokenizer(command);
        st.nextToken();
        if (command.startsWith("admin_cw_infos"))
        {
        	activeChar.sendMessage("Infos on Cursed Weapons:");
        	for (CursedWeapon cw : cwm.getCursedWeapons())
        	{
        		activeChar.sendMessage("> "+cw.getName()+" ("+cw.getItemId()+")");
	        	if (cw.isActivated())
	        	{
	            	L2PcInstance pl = cw.getPlayer();
	        		activeChar.sendMessage("  Player holding: "+pl.getName());
	        		activeChar.sendMessage("    Player karma: "+cw.getPlayerKarma());
	        		activeChar.sendMessage("    Time Remaing: "+(cw.getTimeLeft()/60000)+" min.");
	        		activeChar.sendMessage("    Kills done: "+cw.getNbKills());
	        	} else if (cw.isDropped())
	        	{
	        		activeChar.sendMessage("  Only dropped on the ground.");
	        		activeChar.sendMessage("    Time Remaing: "+(cw.getTimeLeft()/60000)+" min.");
	        		activeChar.sendMessage("    Kills done: "+cw.getNbKills());
	        	} else
	        	{
	            	activeChar.sendMessage("  Didn't exist in the world.");
	        	}
	        	activeChar.sendMessage("----------------------");
        	}
        }
        else if (command.startsWith("admin_cw_remove "))
        {
        	int id = Integer.parseInt(st.nextToken());
        	CursedWeapon cw = cwm.getCursedWeapon(id);
        	
        	if (cw != null)
        		cw.endOfLife();
        	else
        		activeChar.sendMessage("Unknown cursed weapon ID.");
        }
        else if (command.startsWith("admin_cw_goto "))
        {
        	int id = Integer.parseInt(st.nextToken());
        	CursedWeapon cw = cwm.getCursedWeapon(id);
        	
        	if (cw != null)
        		cw.goTo(activeChar);
        	else
        		activeChar.sendMessage("Unknown cursed weapon ID.");
        } else if (command.startsWith("admin_cw_reload"))
        {
        	cwm.reload();
        } else
        {
    		activeChar.sendMessage("Unknown command.");
        }

        return true;
    }
    
    public String[] getAdminCommandList() {
        return _adminCommands;
    }
    
    private boolean checkLevel(int level) {
        return (level >= REQUIRED_LEVEL);
    }
}
