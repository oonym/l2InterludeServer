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

import net.sf.l2j.gameserver.model.L2Clan;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PledgeShowInfoUpdate extends L2GameServerPacket
{
	private static final String _S__A1_PLEDGESHOWINFOUPDATE = "[S] 88 PledgeShowInfoUpdate";
	private L2Clan _clan;
	
	public PledgeShowInfoUpdate(L2Clan clan)
	{
		_clan = clan;
	}
	
	protected final void writeImpl()
	{
		//ddddddddddSdd
		writeC(0x88);
		//sending empty data so client will ask all the info in response ;)
		writeD(_clan.getClanId());
		writeD(0);
		writeD(_clan.getLevel()); //clan level
		writeD(_clan.getHasCastle()); 
		writeD(_clan.getHasHideout()); 
		writeD(0); // displayed in the "tree" view (with the clan skills)
		writeD(_clan.getReputationScore()); // clan reputation score
		writeD(0);
		writeD(0);
		
		writeD(0); //c5
		writeS("bili"); //c5
		writeD(0); //c5
		writeD(0); //c5
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__A1_PLEDGESHOWINFOUPDATE;
	}

}
