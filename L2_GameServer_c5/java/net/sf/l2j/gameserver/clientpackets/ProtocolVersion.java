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
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.serverpackets.KeyPacket;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.2.8.2.8 $ $Date: 2005/04/02 10:43:04 $
 */
public class ProtocolVersion extends ClientBasePacket
{
	private static final String _C__00_PROTOCOLVERSION = "[C] 00 ProtocolVersion";
	static Logger _log = Logger.getLogger(ProtocolVersion.class.getName());
    
    private final long _version;

	/**
	 * packet type id 0x00
	 * format:	cd
	 *  
	 * @param rawPacket
	 */
	public ProtocolVersion(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_version  = readD();
		// ignore the rest
		while (buf.hasRemaining()) buf.get();
	}

	/** urgent messages, execute immediatly */
	public TaskPriority getPriority() { return TaskPriority.PR_HIGH; }
	
	void runImpl()
	{
		// this packet is never encrypted
		if (_version == -2)
		{
            if (Config.DEBUG) _log.info("Ping received");
			// this is just a ping attempt from the new C2 client
			getConnection().close();
			return;
		}
        else if (_version < Config.MIN_PROTOCOL_REVISION)
        {
            _log.info("Client (" + getClient().getLoginName() + ") Protocol Revision:" + _version + " is too low. only "+Config.MIN_PROTOCOL_REVISION+" and "+Config.MAX_PROTOCOL_REVISION+" are supported. Closing connection.");
            _log.warning("Wrong Protocol Version "+_version);
			getConnection().close();
			return;
        }
        else if (_version > Config.MAX_PROTOCOL_REVISION)
        {
            _log.info("Client Protocol Revision:" + _version + " is too high. only "+Config.MIN_PROTOCOL_REVISION+" and "+Config.MAX_PROTOCOL_REVISION+" are supported. Closing connection.");
            _log.warning("Wrong Protocol Version "+_version);
            getConnection().close();
			return;
        }
        getClient().setRevision((int)_version);
        if (Config.DEBUG) _log.fine("Client Protocol Revision is ok:"+_version);
        
		KeyPacket pk = new KeyPacket();
		pk.setKey(getConnection().getCryptKey());
		sendPacket(pk);

		getConnection().activateCryptKey();
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__00_PROTOCOLVERSION;
	}
}
