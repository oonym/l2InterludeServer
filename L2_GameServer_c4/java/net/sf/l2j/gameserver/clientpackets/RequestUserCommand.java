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
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.handler.UserCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.1.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestUserCommand extends ClientBasePacket
{
	private static final String _C__AA_REQUESTUSERCOMMAND = "[C] aa RequestUserCommand";
	static Logger _log = Logger.getLogger(RequestUserCommand.class.getName());
			
	private final int _command;
	
	/**
	 * packet type id 0xaa
	 * format:	cd
	 *  
	 * @param rawPacket
	 */
	public RequestUserCommand(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_command = readD();
	}

	void runImpl()
	{
        L2PcInstance player = getClient().getActiveChar();
	if (player == null)
	    return;
		
        IUserCommandHandler handler = UserCommandHandler.getInstance().getUserCommandHandler(_command);
        
        if (handler == null)
        {
            SystemMessage sm = new SystemMessage(614);
            sm.addString("user commandID "+_command+" not implemented yet");
            player.sendPacket(sm);
        }
        else
        {
            handler.useUserCommand(_command, getClient().getActiveChar());
        }
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__AA_REQUESTUSERCOMMAND;
	}
}
