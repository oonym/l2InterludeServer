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
import net.sf.l2j.gameserver.serverpackets.CharSelectInfo;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.1.2.2 $ $Date: 2005/03/27 15:29:29 $
 */
public class CharacterRestore extends ClientBasePacket
{
	private static final String _C__62_CHARACTERRESTORE = "[C] 62 CharacterRestore";
	//private static Logger _log = Logger.getLogger(CharacterRestore.class.getName());

	// cd
	@SuppressWarnings("unused")
    private final int _charSlot;

	/**
	 * @param decrypt
	 */
	public CharacterRestore(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_charSlot = readD();
	}

	void runImpl()
	{
	    try 
	    {
		getClient().markRestoredChar(_charSlot);
	    } catch (Exception e){}
		CharSelectInfo cl = new CharSelectInfo(getClient().getLoginName(), getClient().getSessionId().playOkID1);
		sendPacket(cl);
		getClient().setCharSelection(cl.getCharInfo());
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__62_CHARACTERRESTORE;
	}
}
