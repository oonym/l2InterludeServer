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
import net.sf.l2j.gameserver.serverpackets.AllyInfo;

/**
 * This class ...
 * 
 * @version $Revision: 1479 $ $Date: 2005-11-09 00:47:42 +0100 (mer., 09 nov. 2005) $
 */
public class RequestAllyInfo extends ClientBasePacket
{
	private static final String _C__8E_REQUESTALLYINFO = "[C] 8E RequestAllyInfo";
	/**
	 * packet type id 0x8E
	 * format:		c
	 * @param rawPacket
	 */
	public RequestAllyInfo(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
	}

	void runImpl()
	{
		AllyInfo ai = new AllyInfo(getClient().getActiveChar());
		sendPacket(ai);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__8E_REQUESTALLYINFO;
	}
}
