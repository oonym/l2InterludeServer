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
 * @version $Revision: 1.7.4.4 $ $Date: 2005/03/27 18:46:19 $
 */
public class ObserverReturn extends ClientBasePacket
{
	private static final String OBSRETURN__C__04 = "[C] b8 ObserverReturn";
	//private static Logger _log = Logger.getLogger(Action.class.getName());
	
	// cddddc

	/**
	 * @param decrypt
	 */
	public ObserverReturn(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		//_objectId  = readD();
		//_originX   = readD();
		//_originY   = readD();
		//_originZ   = readD();
		//_actionId  = readC();// 0 for simple click  1 for shift click
		/* writeC( 20 c2 fe ff de 5a 02 f0 97 f3 ff ff 5c 07 00);*/
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;
		activeChar.leaveObserverMode();
		//activeChar.teleToLocation(activeChar.getObsX(), activeChar.getObsY(), activeChar.getObsZ());
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return OBSRETURN__C__04;
	}
}