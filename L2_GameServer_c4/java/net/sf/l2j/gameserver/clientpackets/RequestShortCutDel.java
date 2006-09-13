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
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestShortCutDel extends ClientBasePacket
{
	private static final String _C__35_REQUESTSHORTCUTDEL = "[C] 35 RequestShortCutDel";
	
	private final int _slot;
	private final int _page;
	
	/**
	 * packet type id 0x35
	 * format:		cd
	 * @param rawPacket
	 */
	public RequestShortCutDel(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		int id = readD();
		if (getClient().getRevision() >= 514) {
			_slot = id % 12;
			_page = id / 12;
		} else {
			_slot = id % 10;
			_page = id / 10;
		}
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		    return;

		activeChar.deleteShortCut(_slot, _page);
		// client needs no confirmation. this packet is just to inform the server
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__35_REQUESTSHORTCUTDEL;
	}
}
