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

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.UserInfo;

/**
 * Appearing Packet Handler<p>
 * <p>
 * 0000: 30 <p>
 * <p>
 *
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/29 23:15:33 $
 */
public class Appearing extends ClientBasePacket
{
	private static final String _C__30_APPEARING = "[C] 30 Appearing";
	//private static Logger _log = Logger.getLogger(Appearing.class.getName());

	// c

	/**
	 * @param decrypt
	 */
	public Appearing(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		// this is just a trigger packet. it has no content
	}

	/** urgent messages, execute immediatly */
    public TaskPriority getPriority() { return TaskPriority.PR_HIGH; }

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
        if(activeChar == null) return;
        if (activeChar.isTeleporting()) activeChar.onTeleported();

        sendPacket(new UserInfo(activeChar));
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__30_APPEARING;
	}
}
