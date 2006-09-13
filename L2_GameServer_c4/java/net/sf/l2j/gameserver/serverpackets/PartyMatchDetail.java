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
 * 
 *
 * sample
 * b0 
 * d8 a8 10 48  objectId 
 * 00 00 00 00 
 * 00 00 00 00 
 * 00 00  
 * 
 * format   ddddS
 * 
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PartyMatchDetail extends ServerBasePacket
{
	private static final String _S__B0_PARTYMATCHDETAIL = "[S] 97 PartyMatchDetail";
	private L2PcInstance _player;
	
	/**
	 * @param allPlayers
	 */
	public PartyMatchDetail(L2PcInstance player)
	{
		_player = player;
	}


	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
		writeC(0x97);
		
		writeD(_player.getObjectId());
		if (_player.isPartyMatchingShowLevel())
		{
			writeD(1); // show level
		}
		else
		{
			writeD(0); // hide level 
		}
		
		if (_player.isPartyMatchingShowClass())
		{
			writeD(1); // show class
		}
		else
		{
			writeD(0); // hide class
		}
		
		writeD(0); //c2
		
		writeS("  " + _player.getPartyMatchingMemo()); // seems to be bugged.. first 2 chars get stripped away
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__B0_PARTYMATCHDETAIL;
	}
}
