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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javolution.lang.TextBuilder;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.Shutdown;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;


/**
 * This class handles following admin commands:
 * - server_shutdown [sec] = shows menu or shuts down server in sec seconds
 * 
 * @version $Revision: 1.5.2.1.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminShutdown implements IAdminCommandHandler {
	//private static Logger _log = Logger.getLogger(AdminShutdown.class.getName());
	
	private static String[] _adminCommands = {"admin_server_shutdown", "admin_server_restart", "admin_server_abort"};
	private static final int REQUIRED_LEVEL = Config.GM_RESTART;
	
	public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
		
		if (command.startsWith("admin_server_shutdown"))
		{	
			try
			{
				int val = Integer.parseInt(command.substring(22)); 
				serverShutdown(activeChar, val, false);
			}
			catch (StringIndexOutOfBoundsException e)
			{				
				sendHtmlForm(activeChar);
			}
		} else if (command.startsWith("admin_server_restart"))
		{	
			try
			{
				int val = Integer.parseInt(command.substring(21)); 
				serverShutdown(activeChar, val, true);
			}
			catch (StringIndexOutOfBoundsException e)
			{				
				sendHtmlForm(activeChar);
			}
		} else if (command.startsWith("admin_server_abort"))
		{	
			serverAbort(activeChar);
		} 
		
		return true;
	}
	
	public String[] getAdminCommandList() {
		return _adminCommands;
	}
	
	private boolean checkLevel(int level) {
		return (level >= REQUIRED_LEVEL);
	}

	private void sendHtmlForm(L2PcInstance activeChar) {
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		
		int t = GameTimeController.getInstance().getGameTime();
		int h = t/60;
		int m = t%60;
		SimpleDateFormat format = new SimpleDateFormat("h:mm a");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, h);
		cal.set(Calendar.MINUTE, m);
		
		TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Server Management Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<table>");	
		replyMSG.append("<tr><td>Players Online: " + L2World.getInstance().getAllPlayersCount() + "</td></tr>");				
		replyMSG.append("<tr><td>Used Memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + " bytes</td></tr>");
		replyMSG.append("<tr><td>Server Rates: " + Config.RATE_XP + "x, " + Config.RATE_SP + "x, " + Config.RATE_DROP_ADENA + "x, " + Config.RATE_DROP_ITEMS + "x</td></tr>");
		replyMSG.append("<tr><td>Game Time: " + format.format(cal.getTime()) + "</td></tr>");
		replyMSG.append("</table><br>");		
		replyMSG.append("<table width=270>");
		replyMSG.append("<tr><td>Enter in seconds the time till the server shutdowns bellow:</td></tr>");
		replyMSG.append("<br>");
		replyMSG.append("<tr><td><center>Seconds till: <edit var=\"shutdown_time\" width=60></center></td></tr>");
		replyMSG.append("</table><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<button value=\"Shutdown\" action=\"bypass -h admin_server_shutdown $shutdown_time\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");			
		replyMSG.append("<button value=\"Restart\" action=\"bypass -h admin_server_restart $shutdown_time\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");			
		replyMSG.append("<button value=\"Abort\" action=\"bypass -h admin_server_abort\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td></tr></table></center>");		
		replyMSG.append("</body></html>");
			
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);				
	}
	
	private void serverShutdown(L2PcInstance activeChar, int seconds, boolean restart)
	{
		Shutdown.getInstance().startShutdown(activeChar, seconds, restart);
	}
	
	private void serverAbort(L2PcInstance activeChar)
	{
		Shutdown.getInstance().abort(activeChar);
	}

}
