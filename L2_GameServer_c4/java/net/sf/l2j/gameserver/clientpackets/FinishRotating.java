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
import net.sf.l2j.gameserver.serverpackets.StopRotation;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class FinishRotating extends ClientBasePacket
{
	private static final String _C__4B_FINISHROTATING = "[C] 4B FinishRotating";

	private final int _degree;
	@SuppressWarnings("unused")
    private final int _unknown;
	
	/**
	 * packet type id 0x4a
	 * 
	 * sample
	 * 
	 * 4b
	 * d // unknown
	 * d // unknown
	 * 
	 * format:		cdd
	 * @param decrypt
	 */
	public FinishRotating(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_degree = readD();
		_unknown = readD();
	}

	void runImpl()
	{
		if (getClient().getActiveChar() == null)
		    return;
		StopRotation sr = new StopRotation(getClient().getActiveChar(), _degree);
		getClient().getActiveChar().sendPacket(sr);
		getClient().getActiveChar().broadcastPacket(sr);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__4B_FINISHROTATING;
	}
}
