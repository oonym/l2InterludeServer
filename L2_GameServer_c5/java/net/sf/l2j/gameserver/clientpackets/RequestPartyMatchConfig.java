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

/**
 * This class ...
 * 
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:29:30 $
 */

public class RequestPartyMatchConfig extends ClientBasePacket
{
	private static final String _C__6F_REQUESTPARTYMATCHCONFIG = "[C] 6F RequestPartyMatchConfig";
	//private static Logger _log = Logger.getLogger(RequestPartyMatchConfig.class.getName());

	private final int _automaticRegistration;
	private final int _showLevel;
	private final int _showClass;
	private String _memo;
	/**
	 * packet type id 0x6f
	 * 
	 * sample
	 * 
	 * 6f
	 * 01 00 00 00 
	 * 00 00 00 00 
	 * 00 00 00 00 
	 * 00 00 
	 * 
	 * format:		cdddS 
	 * @param decrypt
	 */
	public RequestPartyMatchConfig(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_automaticRegistration    = readD();
		_showLevel                = readD();
		_showClass                = readD();
        
        /*
         *  TODO: Check if this this part of the packet has been 
         *  removed by latest versions.
         *
		try
        {
            _memo                 = readS();
        }
		catch (BufferUnderflowException e)
        {
            _memo                 = "";
            _log.warning("Memo field non existant in packet. Notify devs.");
            e.printStackTrace();
        }*/
	}

	void runImpl()
	{
		// TODO: this packet is currently for creating a new party room 
		if (getClient().getActiveChar() == null)
		    return;
		
		getClient().getActiveChar().setPartyMatchingAutomaticRegistration(_automaticRegistration == 1);
		getClient().getActiveChar().setPartyMatchingShowLevel(_showLevel == 1);
		getClient().getActiveChar().setPartyMatchingShowClass(_showClass == 1);
		getClient().getActiveChar().setPartyMatchingMemo(_memo);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__6F_REQUESTPARTYMATCHCONFIG;
	}
}
