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
package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 *
 * @author  chris_00
 *
 * when User press the "List Update" button in CCInfo window
 *
 */
public class ChannelListUpdate implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 97 };

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useUserCommand(int id, L2PcInstance activeChar)
    {
        if (id != COMMAND_IDS[0]) return false;

        L2CommandChannel channel = activeChar.getParty().getCommandChannel();

        activeChar.sendMessage("================");
        activeChar.sendMessage("Command Channel Information is not fully implemented now.");
        activeChar.sendMessage("There are "+channel.getPartys().size()+" Party's in the Channel.");
        activeChar.sendMessage(channel.getMemberCount()+" Players overall.");
        activeChar.sendMessage("Leader is "+channel.getChannelLeader().getName()+".");
        activeChar.sendMessage("Partyleader, Membercount:");
        for(L2Party party : channel.getPartys())
        {
        	activeChar.sendMessage(party.getPartyMembers().get(0).getName()+", "+party.getMemberCount());
        }
        activeChar.sendMessage("================");
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
     */
    public int[] getUserCommandList()
    {
        return COMMAND_IDS;
    }
}
