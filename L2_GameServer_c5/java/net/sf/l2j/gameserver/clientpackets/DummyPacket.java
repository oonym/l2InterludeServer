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
 * @author zabbix
 * Lets drink to code!
 */
public class DummyPacket extends ClientBasePacket
{
	private static Logger _log = Logger.getLogger(DummyPacket.class.getName());
	
	private int _packetId;
	
	public DummyPacket(ByteBuffer buf, ClientThread client, int packetId)
	{
		super(buf,client);
		_packetId = packetId;
	}

	public void runImpl()
	{
		_log.warning("DummyPacket " + _packetId + " (Length = " + getLength() + ") recieved.");
		//getClient().getConnection().close();
	}

	public String getType()
	{
		return "DummyPacket";
	}
}
