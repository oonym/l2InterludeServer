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
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.util.Iterator;

import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;

/**
 *
 *
 */
public class stats implements IVoicedCommandHandler
{
    private static final String[] VOICED_COMMANDS = { "stats" };

    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
    {
        if (command.equalsIgnoreCase("stats"))
        {
            L2PcInstance pc = L2World.getInstance().getPlayer(target);
            if(pc!=null){
                NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

                TextBuilder replyMSG = new TextBuilder("<html><body>");

                replyMSG.append("<center><font color=\"LEVEL\">[ L2J EVENT ENGINE ]</font></center><br>");
                replyMSG.append("<br>Statistics for player <font color=\"LEVEL\">" + pc.getName() + "</font><br>");
                replyMSG.append("Total kills <font color=\"FF0000\">" + pc.kills.size() + "</font><br>");
                replyMSG.append("<br>Detailed list: <br>");
                Iterator<String> it = pc.kills.iterator();
                while(it.hasNext()){
                    replyMSG.append("<font color=\"FF0000\">" + it.next() + "</font><br>");
                }
                replyMSG.append("</body></html>");

                adminReply.setHtml(replyMSG.toString());
                activeChar.sendPacket(adminReply);
            }


        }
    	return true;
    }


    public String[] getVoicedCommandList()
    {
        return VOICED_COMMANDS;
    }

}
