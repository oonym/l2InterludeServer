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

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2ControllableMobInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class handles following admin commands:
 * - kill = kills target L2Character
 * - kill_monster = kills target non-player
 * 
 * - kill <radius> = If radius is specified, then ALL players only in that radius will be killed.
 * - kill_monster <radius> = If radius is specified, then ALL non-players only in that radius will be killed.
 * 
 * @version $Revision: 1.2.4.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminKill implements IAdminCommandHandler 
{
	private static Logger _log = Logger.getLogger(AdminKill.class.getName());
	private static String[] _adminCommands = {"admin_kill", "admin_kill_monster"};
	private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;

	public boolean useAdminCommand(String command, L2PcInstance activeChar) 
	{
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) 
            	return false;
        
		String target = (activeChar.getTarget() != null) ? activeChar.getTarget().getName() : "no-target";
        GMAudit.auditGMAction(activeChar.getName(), command, target, "");

        if (command.startsWith("admin_kill ")) 
        	handleKill(activeChar, command.split(" ")[1]);
        else if (command.equals("admin_kill")) 
        	handleKill(activeChar);
        else if (command.startsWith("admin_kill_monster ")) 
        	handleNonPlayerKill(activeChar, command.split(" ")[1]);
        else if (command.equals("admin_kill_monster")) 
        	handleNonPlayerKill(activeChar);
        
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
	
	private void handleKill(L2PcInstance activeChar, String killParam) 
	{
		L2Object obj = activeChar.getTarget();
		
        if (killParam != null)
        {
            L2PcInstance plyr = L2World.getInstance().getPlayer(killParam);
            
            if (plyr != null)
            {
                obj = plyr;
            }
            else
            {
            	try {
            		int radius  = Integer.parseInt(killParam);
            		
            		for (L2PcInstance knownPlayer : activeChar.getKnownList().getKnownPlayersInRadius(radius))
            		    knownPlayer.reduceCurrentHp(knownPlayer.getMaxHp() + knownPlayer.getMaxCp() + 1, knownPlayer);
            		
            		activeChar.sendMessage("Killed all players within a " + radius + " unit radius.");
            		return;
            	}
				catch (NumberFormatException e) {
					activeChar.sendMessage("Enter a valid player name or radius.");
					return;
				}
            }
        }
        
        if (obj == null)
        	obj = activeChar;
        
        if (obj instanceof L2ControllableMobInstance)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.INCORRECT_TARGET));
            return;
        }
        
		L2Character target = (L2Character)obj;
		target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar);
			
		if (Config.DEBUG) 
		    _log.fine("GM: "+activeChar.getName()+"("+activeChar.getObjectId()+")"+
		              " killed character "+target.getObjectId());
	}
	
    private void handleNonPlayerKill(L2PcInstance activeChar)
    {
        handleNonPlayerKill(activeChar, "");
    }
    
	private void handleNonPlayerKill(L2PcInstance activeChar, String radiusStr) 
	{
        L2Object obj = activeChar.getTarget();
        
		if (!radiusStr.equals(""))
		{
			try {
				int radius = Integer.parseInt(radiusStr);
				
				for (L2Character knownChar : activeChar.getKnownList().getKnownCharactersInRadius(radius))
					if (!(knownChar instanceof L2PcInstance) 
                            && !(knownChar instanceof L2ControllableMobInstance))
						knownChar.reduceCurrentHp(knownChar.getMaxHp() + 1, activeChar);
				
				activeChar.sendMessage("Killed all non-players within a " + radius + " unit radius.");
			}
			catch (NumberFormatException e) {
				activeChar.sendMessage("Enter a valid radius.");
				return;
			}
		}
		
        if (obj == null || obj instanceof L2PcInstance || obj instanceof L2ControllableMobInstance)
        {
            activeChar.sendPacket(new SystemMessage(SystemMessage.INCORRECT_TARGET));
            return;
        }
            			
        L2Character character = (L2Character)obj;
        character.reduceCurrentHp(character.getMaxHp() + 1, activeChar);
	}
}
