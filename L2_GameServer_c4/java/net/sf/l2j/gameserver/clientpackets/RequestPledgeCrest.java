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
import net.sf.l2j.gameserver.serverpackets.PledgeCrest;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.4.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestPledgeCrest extends ClientBasePacket
{
	private static Logger _log = Logger.getLogger(RequestPledgeCrest.class.getName());
	private static final String _C__68_REQUESTPLEDGECREST = "[C] 68 RequestPledgeCrest";
	
	private int _crestId;
	
	/**
	 * packet type id 0x68 format: cd
	 * 
	 * @param rawPacket
	 */
	public RequestPledgeCrest(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_crestId = 0;
		try {
		    _crestId = readD();
		} catch (Exception e) {};
	}

	void runImpl()
	{
		if (_crestId == 0)
		    return;
		if (Config.DEBUG) _log.fine("crestid " + _crestId + " requested");
        
        byte[] data = CrestCache.getInstance().getPledgeCrest(_crestId);
        
		if (data != null)
		{
			PledgeCrest pc = new PledgeCrest(_crestId, data);
			sendPacket(pc);
		}
		else
		{
			if (Config.DEBUG) _log.fine("crest is missing:" + _crestId);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__68_REQUESTPLEDGECREST;
	}
}
