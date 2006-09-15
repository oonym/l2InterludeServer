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
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ShortCutRegister;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestShortCutReg extends ClientBasePacket
{
	private static final String _C__33_REQUESTSHORTCUTREG = "[C] 33 RequestShortCutReg";

	private final int _type;
	private final int _id;
	private final int _slot;
	private final int _page;
	private final int _unk;
	/**
	 * packet type id 0x33
	 * format:		cdddd
	 * @param rawPacket
	 */
	public RequestShortCutReg(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_type = readD();
		int slot = readD();
		_id = readD();
		_unk = readD();
		
		if (getClient().getRevision() >= 514) {
			_slot = slot % 12;
			_page = slot / 12;
		} else {
			_slot = slot % 10;
			_page = slot / 10;
		}
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		    return;

		switch (_type)
		{
			case 0x01:	// item
			case 0x03:	// action
			case 0x04:	// macro
            case 0x05:  // recipe
			{
				L2ShortCut sc = new L2ShortCut(_slot, _page, _type, _id, -1, _unk);
				sendPacket(new ShortCutRegister(sc));
				activeChar.registerShortCut(sc);
				break;
			}
			case 0x02:	// skill
			{
				int level = activeChar.getSkillLevel( _id );
				if (level > 0)
				{
					L2ShortCut sc = new L2ShortCut(_slot, _page, _type, _id, level, _unk);
					sendPacket(new ShortCutRegister(sc));
					activeChar.registerShortCut(sc);
				}
				break;
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__33_REQUESTSHORTCUTREG;
	}
}
