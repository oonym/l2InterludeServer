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
import net.sf.l2j.gameserver.serverpackets.BeginRotation;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class StartRotating extends ClientBasePacket
{
	private static final String _C__4A_STARTROTATING = "[C] 4A StartRotating";

	private final int _degree;
	private final int _side;
	/**
	 * packet type id 0x4a
	 * 
	 * sample
	 * 
	 * 4a
	 * fb 0f 00 00 // degree (goes from 0 to 65535)
	 * 01 00 00 00 // side (01 00 00 00 = right, ff ff ff ff = left)
	 * 
	 * format:		cdd
	 * @param decrypt
	 */
	public StartRotating(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_degree = readD();
		_side = readD();
	}

	void runImpl()
	{
		if (getClient().getActiveChar() == null)
		    return;
		BeginRotation br = new BeginRotation(getClient().getActiveChar(), _degree, _side);
		getClient().getActiveChar().broadcastPacket(br);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__4A_STARTROTATING;
	}
}
