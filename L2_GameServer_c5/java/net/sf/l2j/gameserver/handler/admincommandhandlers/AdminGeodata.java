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

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GeoData;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author  -Nemesiss-
 */
public class AdminGeodata implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminKill.class.getName());
	private static String[] _adminCommands = {"admin_geo_z", "admin_geo_type", "admin_geo_nswe", "admin_geo_los"};
	private static final int REQUIRED_LEVEL = Config.GM_MIN;
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar) 
	{
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) 
            	return false;
        
		String target = (activeChar.getTarget() != null) ? activeChar.getTarget().getName() : "no-target";
        GMAudit.auditGMAction(activeChar.getName(), command, target, "");

        if (!Config.GEODATA)
        {
        	activeChar.sendMessage("Geo Engine is Turned Off!");        			
        	return true;
        }
        
        if (command.equals("admin_geo_z"))                    
        	activeChar.sendMessage("GeoEngine: Geo_Z = "+GeoData.getInstance().getHeight(activeChar.getX(),activeChar.getY(),activeChar.getZ())+ " Loc_Z = "+activeChar.getZ());            
        else if (command.equals("admin_geo_type"))
        {
            short type = GeoData.getInstance().getType(activeChar.getX(),activeChar.getY());
            activeChar.sendMessage("GeoEngine: Geo_Type = "+type);            
        }
        else if (command.equals("admin_geo_nswe"))
        {
            String result = "";
            short nswe = GeoData.getInstance().getNSWE(activeChar.getX(),activeChar.getY(),activeChar.getZ());            
            if ((nswe & 8) == 0) result += " N";
            if ((nswe & 4) == 0) result += " S";
            if ((nswe & 2) == 0) result += " W";
            if ((nswe & 1) == 0) result += " E";
            activeChar.sendMessage("GeoEngine: Geo_NSWE -> "+nswe+ "->"+result);            
        }
        else if (command.equals("admin_geo_los"))
        {
            if (activeChar.getTarget() != null)
            {
                if(GeoData.getInstance().canSeeTargetDebug(activeChar,activeChar.getTarget()))                
                    activeChar.sendMessage("GeoEngine: Can See Target");                
                else 
                	activeChar.sendMessage("GeoEngine: Can't See Target");
                
            }
            else            
                activeChar.sendMessage("None Target!");            
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

}
