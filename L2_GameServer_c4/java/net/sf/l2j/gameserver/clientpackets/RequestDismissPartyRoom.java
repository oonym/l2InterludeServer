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

/**
 * Format: (ch) dd
 * @author -Wooden-
 *
 */
public class RequestDismissPartyRoom extends ClientBasePacket
{
	private static Logger _log = Logger.getLogger(RequestDismissPartyRoom.class.getName());
	private static final String _C__D0_02_REQUESTDISMISSPARTYROOM = "[C] D0:02 RequestDismissPartyRoom";
	private int _data1;
	private int _data2;

	/**
	 * @param buf
	 * @param client
	 */
	public RequestDismissPartyRoom(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_data1 = readD();
		_data2 = readD();
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	void runImpl()
	{
		// TODO Auto-generated method stub
		_log.info("This packet is not well known : RequestDismissPartyRoom");
		_log.info("Data received: d:"+_data1+" d:"+_data2);

	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_02_REQUESTDISMISSPARTYROOM;
	}

}
