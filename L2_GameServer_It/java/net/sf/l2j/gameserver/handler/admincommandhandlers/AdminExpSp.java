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
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class handles following admin commands:
 * - add_exp_sp_to_character = show menu
 * - add_exp_sp exp sp = adds exp & sp to target
 * 
 * @version $Revision: 1.2.4.6 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminExpSp implements IAdminCommandHandler {
	private static Logger _log = Logger.getLogger(AdminExpSp.class.getName());

	private static final String[] ADMIN_COMMANDS = {
			"admin_add_exp_sp_to_character",
			"admin_add_exp_sp",
            "admin_remove_exp_sp"};
	private static final int REQUIRED_LEVEL = Config.GM_CHAR_EDIT;

	public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;

        if (command.equals("admin_add_exp_sp_to_character"))
		{
			addExpSp(activeChar);
			GMAudit.auditGMAction(activeChar.getName(), command, (activeChar.getTarget()!=null ? activeChar.getTarget().getName() : "no-target"), "");
		}
		else if (command.startsWith("admin_add_exp_sp"))
		{
			try
			{
				String val = command.substring(16); 
				adminAddExpSp(activeChar, val);
				GMAudit.auditGMAction(activeChar.getName(), command,  val, "");
			}
			catch (StringIndexOutOfBoundsException e)
			{	//Case of empty character name
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Error while adding Exp-Sp.");
				activeChar.sendPacket(sm);
				//listCharacters(client, 0);
			}			
		}
        else if(command.startsWith("admin_remove_exp_sp"))
        {
            try
            {
                String val = command.substring(19); 
                adminRemoveExpSP(activeChar, val);
                GMAudit.auditGMAction(activeChar.getName(), command,  val, "");
            }
            catch (StringIndexOutOfBoundsException e)
            {   //Case of empty character name
                SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
                sm.addString("Error while removing Exp-Sp.");
                activeChar.sendPacket(sm);
                //listCharacters(client, 0);            
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
	
	private void addExpSp(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance) {
			player = (L2PcInstance)target;
		} else {
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
			return;
		}
		
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);		

		TextBuilder replyMSG = new TextBuilder("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
        replyMSG.append("<center>Editing <font color=\"LEVEL\">" + player.getName() + "</font></center>");
		replyMSG.append("<table width=270>");
        replyMSG.append("<tr><td>Level: " + player.getLevel() + "</td></tr>");
		replyMSG.append("<tr><td>Class: " + player.getTemplate().className + "</td></tr>");
		replyMSG.append("<tr><td>Exp: " + player.getExp() + "</td></tr>");
		replyMSG.append("<tr><td>SP: " + player.getSp() + "</td></tr></table>");
		replyMSG.append("<table width=270><tr><td>Note: Fill BOTH values before saving the modifications</td></tr>");
		replyMSG.append("<tr><td>and use 0 if no changes are needed.</td></tr></table><br>");
		replyMSG.append("<center><table><tr>");
		replyMSG.append("<td>Exp: <edit var=\"exp_to_add\" width=50></td>");
		replyMSG.append("<td>SP:  <edit var=\"sp_to_add\" width=50></td>");
		replyMSG.append("<td>&nbsp;<center><button value=\"Add\" action=\"bypass -h admin_add_exp_sp $exp_to_add $sp_to_add\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></td>");
		replyMSG.append("<td>&nbsp;<center><button value=\"Remove\" action=\"bypass -h admin_remove_exp_sp $exp_to_add $sp_to_add\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></td>");
		replyMSG.append("</tr></table></center>");
		replyMSG.append("</body></html>");
		
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}
    
    private void removeExp(L2PcInstance activeChar)
    {
        L2Object target = activeChar.getTarget();
        L2PcInstance player = null;
        if (target instanceof L2PcInstance) {
            player = (L2PcInstance)target;
        } else {
            SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
            sm.addString("Incorrect target.");
            activeChar.sendPacket(sm);
            return;
        }
        
        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);      

        TextBuilder replyMSG = new TextBuilder("<html><body>");
        replyMSG.append("<table width=260><tr>");
        replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
        replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table>");
        replyMSG.append("<br><br>");
        replyMSG.append("<center>Editing <font color=\"LEVEL\">" + player.getName() + "</font></center>");
        replyMSG.append("<table width=270>");
        replyMSG.append("<tr><td>Level: " + player.getLevel() + "</td></tr>");
        replyMSG.append("<tr><td>Class: " + player.getTemplate().className + "</td></tr>");
        replyMSG.append("<tr><td>Exp: " + player.getExp() + "</td></tr>");
        replyMSG.append("<tr><td>SP: " + player.getSp() + "</td></tr></table>");
        replyMSG.append("<table width=270><tr><td>Note: Fill BOTH values before saving the modifications</td></tr>");
        replyMSG.append("<tr><td>and use 0 if no changes are needed.</td></tr></table><br>");
        replyMSG.append("<center><table><tr>");
        replyMSG.append("<td>remove Exp: <edit var=\"exp_to_remove\" width=50></td>");
        replyMSG.append("<td>remove SP:  <edit var=\"sp_to_remove\" width=50></td>");
        replyMSG.append("<td>&nbsp;<button value=\"Save Changes\" action=\"bypass -h admin_remove_exp_sp $exp_to_remove $sp_to_remove\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        replyMSG.append("</tr></table></center>");
        replyMSG.append("</body></html>");
        
        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply);
    }

	private void adminAddExpSp(L2PcInstance activeChar, String ExpSp)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if (target instanceof L2PcInstance)
        {
			player = (L2PcInstance)target;
		}
        else
        {
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Incorrect target.");
			activeChar.sendPacket(sm);
			return;
		}

		StringTokenizer st = new StringTokenizer(ExpSp);
		if (st.countTokens()!=2)
		{
			addExpSp(activeChar);
		}
		else
		{
    		String exp = st.nextToken();
    		String sp = st.nextToken();
            long expval = 0;
            int spval = 0;
            try
            {
        		expval = Long.parseLong(exp);
        		spval = Integer.parseInt(sp);
            }
            catch (NumberFormatException e)
            {
                //Wrong number (maybe it's too big?)
                SystemMessage smA = new SystemMessage(SystemMessageId.S1_S2);
                smA.addString("Wrong Number Format");
                activeChar.sendPacket(smA);
            }
            if(expval != 0 || spval != 0)
            {
        		//Common character information
        		SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
        		sm.addString("Admin is adding you "+expval+" xp and "+spval+" sp.");
        		player.sendPacket(sm);
        		
        		player.addExpAndSp(expval,spval);
        
        		//Admin information	
        		SystemMessage smA = new SystemMessage(SystemMessageId.S1_S2);
        		smA.addString("Added "+expval+" xp and "+spval+" sp to "+player.getName()+".");
        		activeChar.sendPacket(smA);
        		if (Config.DEBUG)
                {
        			_log.fine("GM: "+activeChar.getName()+"("+activeChar.getObjectId()+") added "+expval+
        					" xp and "+spval+" sp to "+player.getObjectId()+".");
                }
        		
    		}
		}
	}
    
    private void adminRemoveExpSP(L2PcInstance activeChar, String ExpSp)
    {
        L2Object target = activeChar.getTarget();
        L2PcInstance player = null;
        if (target instanceof L2PcInstance)
        {
            player = (L2PcInstance)target;
        }
        else
        {
            SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
            sm.addString("Incorrect target.");
            activeChar.sendPacket(sm);
            return;
        }

        StringTokenizer st = new StringTokenizer(ExpSp);
        if (st.countTokens()!=2)
        {
            removeExp(activeChar);
        }
        else
        {
            String exp = st.nextToken();
            String sp = st.nextToken();
            long expval = 0;
            int spval = 0;
            try
            {
                expval = Long.parseLong(exp);
                spval = Integer.parseInt(sp);
            }
            catch (NumberFormatException e)
            {
                //Wrong number (maybe it's too big?)
                SystemMessage smA = new SystemMessage(SystemMessageId.S1_S2);
                smA.addString("Wrong Number Format");
                activeChar.sendPacket(smA);
            }
            if(expval != 0 || spval != 0)
            {
                //Common character information
                SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
                sm.addString("Admin is removing you "+expval+" xp and "+spval+" sp.");
                player.sendPacket(sm);
                
                player.removeExpAndSp(expval,spval);
        
                //Admin information 
                SystemMessage smA = new SystemMessage(SystemMessageId.S1_S2);
                smA.addString("Removed "+expval+" xp and "+spval+" sp from "+player.getName()+".");
                activeChar.sendPacket(smA);
                if (Config.DEBUG)
                {
                    _log.fine("GM: "+activeChar.getName()+"("+activeChar.getObjectId()+") added "+expval+
                            " xp and "+spval+" sp to "+player.getObjectId()+".");
                }
                
            }
        }
    }
}
