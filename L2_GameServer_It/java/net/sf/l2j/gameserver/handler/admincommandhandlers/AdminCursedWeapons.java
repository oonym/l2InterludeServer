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
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class handles following admin commands:
 * - cw_info = displays cursed weapon status
 * - cw_remove = removes a cursed weapon from the world, item id or name must be provided
 * - cw_add = adds a cursed weapon into the world, item id or name must be provided. Target will be the weilder
 * - cw_goto = teleports GM to the specified cursed weapon
 * - cw_reload = reloads instance manager
 * @version $Revision: 1.1.6.3 $ $Date: 2007/07/31 10:06:06 $
 */
public class AdminCursedWeapons implements IAdminCommandHandler {
	//private static Logger _log = Logger.getLogger(AdminBan.class.getName());
	private static final String[] ADMIN_COMMANDS = {"admin_cw_info", "admin_cw_remove", "admin_cw_goto", "admin_cw_reload", "admin_cw_add"};
	private static final int REQUIRED_LEVEL = Config.GM_MIN;

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
			if (!(checkLevel(activeChar.getAccessLevel())))
				return false;

		CursedWeaponsManager cwm = CursedWeaponsManager.getInstance();
		int id=0;

		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();

		if (command.startsWith("admin_cw_info"))
		{
			activeChar.sendMessage("======= Cursed Weapons: =======");
			for (CursedWeapon cw : cwm.getCursedWeapons())
			{
				activeChar.sendMessage("> "+cw.getName()+" ("+cw.getItemId()+")");
				if (cw.isActivated())
				{
					L2PcInstance pl = cw.getPlayer();
					activeChar.sendMessage("  Player holding: "+ pl==null ? "null" : pl.getName());
					activeChar.sendMessage("    Player karma: "+cw.getPlayerKarma());
					activeChar.sendMessage("    Time Remaining: "+(cw.getTimeLeft()/60000)+" min.");
					activeChar.sendMessage("    Kills : "+cw.getNbKills());
				}
				else if (cw.isDropped())
				{
					activeChar.sendMessage("  Lying on the ground.");
					activeChar.sendMessage("    Time Remaining: "+(cw.getTimeLeft()/60000)+" min.");
					activeChar.sendMessage("    Kills : "+cw.getNbKills());
				}
				else
				{
					activeChar.sendMessage("  Don't exist in the world.");
				}
				activeChar.sendPacket(new SystemMessage(SystemMessageId.FRIEND_LIST_FOOT));
			}
		}
		else if (command.startsWith("admin_cw_reload"))
		{
			cwm.reload();
		}
		else
		{
			CursedWeapon cw=null;
			try
			{
				String parameter = st.nextToken();
				if (parameter.matches("[0-9]*"))
					id = Integer.parseInt(parameter);
				else 
				{
					parameter = parameter.replace('_', ' ');
					for (CursedWeapon cwp : cwm.getCursedWeapons())
					{
						if (cwp.getName().toLowerCase().contains(parameter.toLowerCase()))
						{
							id=cwp.getItemId();
							break;
						}
					}
				}
				cw = cwm.getCursedWeapon(id);
				if (cw==null)
				{
					activeChar.sendMessage("Unknown cursed weapon ID.");
					return false;
				}
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //cw_remove|//cw_goto|//cw_add <itemid|name>");
			}

			if (command.startsWith("admin_cw_remove "))
			{
				cw.endOfLife();
			}
			else if (command.startsWith("admin_cw_goto "))
			{
				cw.goTo(activeChar);
			} 
			else if (command.startsWith("admin_cw_add"))
			{
				if (cw==null)
				{
					activeChar.sendMessage("Usage: //cw_add <itemid|name>");
					return false;
				}
				else if (cw.isActive())
					activeChar.sendMessage("This cursed weapon is already active.");
				else 
				{
					L2Object target = activeChar.getTarget();
					if (target != null && target instanceof L2PcInstance)
						((L2PcInstance)target).addItem("AdminCursedWeaponAdd", id, 1, target, true);
					else 
						activeChar.addItem("AdminCursedWeaponAdd", id, 1, activeChar, true);
				}
			}
			else
			{
				activeChar.sendMessage("Unknown command.");
			}
		}
		return true;
	}

	public String[] getAdminCommandList() {
		return ADMIN_COMMANDS;
	}

	private boolean checkLevel(int level) {
		return (level >= REQUIRED_LEVEL);
	}
}
