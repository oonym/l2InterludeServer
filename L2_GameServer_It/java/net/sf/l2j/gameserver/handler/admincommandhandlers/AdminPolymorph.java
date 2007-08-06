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
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: polymorph
 * 
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2007/07/31 10:05:56 $
 */
public class AdminPolymorph implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = { "admin_polymorph", "admin_unpolymorph", "admin_polymorph_menu", "admin_unpolymorph_menu" };

	private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;

	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
				return false;
		}

		if (command.startsWith("admin_polymorph"))
		{
			StringTokenizer st = new StringTokenizer(command);
			try
			{
				st.nextToken();
				String type = st.nextToken();
				String id = st.nextToken();
				L2Object target = activeChar.getTarget();
				doPolymorph(activeChar, target, id, type);
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //polymoprh <type> <id>");
			}
		}
		else if (command.equals("admin_unpolymorph"))
		{
			doUnpoly(activeChar,activeChar.getTarget());
		}
		if (command.contains("menu"))
			showMainPage(activeChar);
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

	/**
	 * @param activeChar
	 * @param target
	 * @param id
	 * @param type
	 */
	private void doPolymorph(L2PcInstance activeChar, L2Object obj, String id, String type)
	{

		if (obj != null)
		{
			obj.getPoly().setPolyInfo(type, id);
			//animation
			if(obj instanceof L2Character)
			{
				L2Character Char = (L2Character) obj;
				MagicSkillUser msk = new MagicSkillUser(Char, 1008, 1, 4000, 0);
				Char.broadcastPacket(msk);
				SetupGauge sg = new SetupGauge(0, 4000);
				Char.sendPacket(sg);
			}
			//end of animation
//			L2Character target = (L2Character) obj;
			obj.decayMe();
			obj.spawnMe(obj.getX(),obj.getY(),obj.getZ());
		}
		else
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
	}

	/**
	 * @param activeChar
	 * @param target
	 */
	private void doUnpoly(L2PcInstance activeChar, L2Object target)
	{
		if (target !=null)
		{
			target.getPoly().setPolyInfo(null, "1");
			target.decayMe();
			target.spawnMe(target.getX(),target.getY(),target.getZ());
		}
		else
			activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
	}

	private void showMainPage(L2PcInstance activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "effects_menu.htm");
	}
}
