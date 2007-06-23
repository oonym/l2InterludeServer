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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.geoeditorcon.GeoEditorConnector;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * 
 *
 * @author  Luno
 */
public class AdminGeoEditor implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = 
		{
			"admin_geoeditor_connect",
			"admin_geoeditor_join",
			"admin_geoeditor_leave"
		};
	
	private static final int REQUIRED_LEVEL = Config.GM_MIN;
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar) 
	{
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) 
            	return false;
        
		String target = (activeChar.getTarget() != null) ? activeChar.getTarget().getName() : "no-target";
        GMAudit.auditGMAction(activeChar.getName(), command, target, "");

        GeoEditorConnector ge = GeoEditorConnector.getInstance();
        
        if(command.startsWith("admin_geoeditor_connect"))
        {
        	try
        	{
        		int ticks = Integer.parseInt(command.substring(24));
        		ge.connect(activeChar, ticks);
        	}catch(Exception e){activeChar.sendMessage("Usage: //geoeditor_connect <number>"); }
        }
        else if(command.equals("admin_geoeditor_join"))
        {
        	ge.join(activeChar);
        }
        else if(command.equals("admin_geoeditor_leave"))
        {
        	ge.leave(activeChar);
        }
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
}
