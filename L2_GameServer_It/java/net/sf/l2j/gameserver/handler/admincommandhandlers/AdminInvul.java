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
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class handles following admin commands:
 * - invul = turns invulnerability on/off
 * 
 * @version $Revision: 1.2.4.4 $ $Date: 2005/04/11 10:06:02 $
 */
public class AdminInvul implements IAdminCommandHandler {
	private static Logger _log = Logger.getLogger(AdminInvul.class.getName());
	private static String[] _adminCommands = {"admin_invul", "admin_setinvul"};
	private static final int REQUIRED_LEVEL = Config.GM_GODMODE;

	public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;

        GMAudit.auditGMAction(activeChar.getName(), command, (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target"), "");
        
		if (command.equals("admin_invul")) handleInvul(activeChar);
        if (command.equals("admin_setinvul")){
           L2Object target = activeChar.getTarget();
            if (target instanceof L2PcInstance){
              handleInvul((L2PcInstance)target);
            }
        }
		return true;
	}
	
	public String[] getAdminCommandList() {
		return _adminCommands;
	}
	
	private boolean checkLevel(int level) {
		return (level >= REQUIRED_LEVEL);
	}
	
	private void handleInvul(L2PcInstance activeChar) {
		if (activeChar.isInvul())
		{
        	activeChar.setIsInvul(false);
        	String text = activeChar.getName() + " is now mortal";
        	SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
        	sm.addString(text);
        	activeChar.sendPacket(sm);
        	if (Config.DEBUG)
        		_log.fine("GM: Gm removed invul mode from character "+activeChar.getName()+"("+activeChar.getObjectId()+")");
		} else
		{
			activeChar.setIsInvul(true);
			String text = activeChar.getName() + " is now invulnerable";
			SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
			sm.addString(text);
			activeChar.sendPacket(sm);
			if (Config.DEBUG) 
				_log.fine("GM: Gm activated invul mode for character "+activeChar.getName()+"("+activeChar.getObjectId()+")");
		}
	}
}
