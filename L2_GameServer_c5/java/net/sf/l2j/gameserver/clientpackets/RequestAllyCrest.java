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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.cache.CrestCache;
import net.sf.l2j.gameserver.serverpackets.AllyCrest;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestAllyCrest extends ClientBasePacket
{
	private static final String _C__88_REQUESTALLYCREST = "[C] 88 RequestAllyCrest";
	private static Logger _log = Logger.getLogger(RequestAllyCrest.class.getName());

	private final int _crestId;
	/**
	 * packet type id 0x88 format: cd
	 * 
	 * @param rawPacket
	 */
	public RequestAllyCrest(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_crestId = readD();
	}

	void runImpl()
	{
		if (Config.DEBUG) _log.fine("allycrestid " + _crestId + " requested");
        
        byte[] data = CrestCache.getInstance().getAllyCrest(_crestId);

		if (data != null)
		{
			AllyCrest ac = new AllyCrest(_crestId,data);
			sendPacket(ac);
		}
		else
		{
			if (Config.DEBUG) _log.fine("allycrest is missing:" + _crestId);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__88_REQUESTALLYCREST;
	}
}
