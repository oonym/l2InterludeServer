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
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * Pledge Manipulation
 * //pledge <create|dismiss|setlevel>
 */
public class AdminPledge implements IAdminCommandHandler
{
    private static String[] _adminCommands = {"admin_pledge"};

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!Config.ALT_PRIVILEGES_ADMIN)
        {
            if (!activeChar.isGM() || activeChar.getAccessLevel() < Config.GM_ACCESSLEVEL || activeChar.getTarget() == null || !(activeChar.getTarget() instanceof L2PcInstance))
                return false;
        }
        
        L2PcInstance target = (L2PcInstance)activeChar.getTarget();

        if(command.startsWith("admin_pledge"))
        {
            StringTokenizer st = new StringTokenizer(command);
            st.nextToken();
            
            String action = st.nextToken(); // create|dismiss|setlevel
            
            if(action.equals("create"))
            {
                String pledgeName = st.nextToken();
                L2Clan clan = ClanTable.getInstance().createClan(target, pledgeName);
                if (clan != null)
                {
                    activeChar.sendMessage("Clan " + pledgeName + " created. Leader: " + target.getName());
                }
            }
            else if(action.equals("dismiss"))
            {
                if (!target.isClanLeader())
                {
                    activeChar.sendMessage("Target are not clan leader");
                    return false;
                }
                ClanTable.getInstance().destroyClan(target.getClanId());
            }
            else if(action.equals("setlevel"))
            {
                if (!target.isClanLeader())
                {
                    activeChar.sendMessage("Target are not clan leader");
                    return false;
                }
                int level = Integer.parseInt(st.nextToken());
                target.getClan().changeLevel(level);
                activeChar.sendMessage("You set level " + level + " for clan " + target.getClan().getName());
            }
        }
        
        return true;
    }
    
    public String[] getAdminCommandList()
    {
        return _adminCommands;
    }

}
