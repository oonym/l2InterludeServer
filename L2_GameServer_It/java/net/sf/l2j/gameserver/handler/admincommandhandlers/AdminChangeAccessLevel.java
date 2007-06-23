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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class handles following admin commands: - invul = turns invulnerability
 * on/off
 * 
 * @version $Revision: 1.1.2.2.2.3 $ $Date: 2005/04/11 10:06:00 $
 */
public class AdminChangeAccessLevel implements IAdminCommandHandler
{
    //private static Logger _log = Logger.getLogger(AdminChangeAccessLevel.class.getName());

    private static final String[] ADMIN_COMMANDS = { "admin_changelvl" };

    private static final int REQUIRED_LEVEL = Config.GM_ACCESSLEVEL;

    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!Config.ALT_PRIVILEGES_ADMIN)
        {
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
            {
                return false;
            }
        }
        
        handleChangeLevel(command, activeChar);
		String target = (activeChar.getTarget() != null?activeChar.getTarget().getName():"no-target");
        GMAudit.auditGMAction(activeChar.getName(), command, target, "");
        return true;
    }

    public String[] getAdminCommandList()
    {
        return ADMIN_COMMANDS;
    }

    private boolean checkLevel(int level)
    {
        return (level >= REQUIRED_LEVEL);
    }

    private void handleChangeLevel(String command, L2PcInstance admin)
    {
        String[] parts = command.split(" ");
        if (parts.length == 2)
        {
            int lvl = Integer.parseInt(parts[1]);
            if (admin.getTarget() instanceof L2PcInstance)
            {
                ((L2PcInstance)admin.getTarget()).setAccessLevel(lvl);
            }
        }
        else if (parts.length == 3)
        {
            int lvl = Integer.parseInt(parts[2]);
            L2PcInstance player = L2World.getInstance().getPlayer(parts[1]);
            if (player != null)
            {
                player.setAccessLevel(lvl);
            }
        }
    }
}
