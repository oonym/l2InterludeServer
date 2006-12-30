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
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.PartyMatchDetail;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.4.3 $ $Date: 2005/03/27 15:29:30 $
 */

public class RequestPartyMatchDetail extends ClientBasePacket
{
	private static final String _C__71_REQUESTPARTYMATCHDETAIL = "[C] 71 RequestPartyMatchDetail";
	private static Logger _log = Logger.getLogger(RequestPartyMatchDetail.class.getName());

	private final int _objectId;
    @SuppressWarnings("unused")
	private final int _unk1;
	/**
	 * packet type id 0x71
	 * 
	 * sample
	 * 
	 * 71
	 * d8 a8 10 41  object id 
	 * 
	 * packet format rev650  	cdd
	 * @param decrypt
	 */
	public RequestPartyMatchDetail(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_objectId = readD();
        //TODO analyse value unk1
        _unk1 = readD();
	}

	void runImpl()
	{
		//TODO: this packet is currently for starting auto join
		L2PcInstance player = (L2PcInstance) L2World.getInstance().findObject(_objectId);
		if (player == null)
		    return;
		PartyMatchDetail details = new PartyMatchDetail(player);
		sendPacket(details);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__71_REQUESTPARTYMATCHDETAIL;
	}
}
