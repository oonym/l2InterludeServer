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
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.LeaveWorld;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class AdminKick implements IAdminCommandHandler {
    //private static Logger _log = Logger.getLogger(AdminKick.class.getName());
    private static String[] _adminCommands = {"admin_kick" ,"admin_kick_non_gm"};
    private static final int REQUIRED_LEVEL = Config.GM_KICK;
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {

        if (!Config.ALT_PRIVILEGES_ADMIN)
        {
    		if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
            {
                //System.out.println("Not required level");
                return false;
            }
        }
		String target = (activeChar.getTarget() != null?activeChar.getTarget().getName():"no-target");
        GMAudit.auditGMAction(activeChar.getName(), command, target, "");
        
        if (command.startsWith("admin_kick"))
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
                if (plyr != null)
                {
                    //System.out.println("Player2 "+plyr.getName());
                    plyr.logout();
    				SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
    				sm.addString("You kicked " + plyr.getName() + " from the game.");
    				activeChar.sendPacket(sm);
    				RegionBBSManager.getInstance().changeCommunityBoard();
                }
            }
        }
        if (command.startsWith("admin_kick_non_gm"))
        {
        	int counter = 0;
        	for (L2PcInstance player : L2World.getInstance().getAllPlayers())
            {
        		if(!player.isGM())
        		{
        			counter++;
        			player.sendPacket(new LeaveWorld());
        			player.logout();
        			RegionBBSManager.getInstance().changeCommunityBoard();
        		}
            }
        	activeChar.sendMessage("Kicked "+counter+" players");
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
