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
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class PrivateStoreMsgBuy extends L2GameServerPacket
{
	private static final String _S__D2_PRIVATESTOREMSGBUY = "[S] b9 PrivateStoreMsgBuy";
	private L2PcInstance _activeChar;
    private String _storeMsg;
	
	public PrivateStoreMsgBuy(L2PcInstance player)
	{
		_activeChar = player;
		if (_activeChar.getBuyList() != null) 
        	_storeMsg = _activeChar.getBuyList().getTitle();
	}
	
	protected final void writeImpl()
	{
		writeC(0xb9);
		writeD(_activeChar.getObjectId());
		writeS(_storeMsg);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__D2_PRIVATESTOREMSGBUY;
	}

}
