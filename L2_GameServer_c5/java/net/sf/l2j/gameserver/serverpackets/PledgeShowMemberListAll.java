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
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * 
 *
 * sample
 * 0000: 68 
 * b1010000 
 * 48 00 61 00 6d 00 62 00 75 00 72 00 67 00 00 00   H.a.m.b.u.r.g...
 * 43 00 61 00 6c 00 61 00 64 00 6f 00 6e 00 00 00   C.a.l.a.d.o.n...
 * 00000000  crestid | not used (nuocnam)
 * 00000000 00000000 00000000 00000000 
 * 22000000 00000000 00000000 
 * 00000000 ally id
 * 00 00	ally name
 * 00000000 ally crrest id 
 * 
 * 02000000
 *  
 * 6c 00 69 00 74 00 68 00 69 00 75 00 6d 00 31 00 00 00  l.i.t.h.i.u.m...
 * 0d000000		level 
 * 12000000 	class id
 * 00000000 	
 * 01000000 	offline 1=true
 * 00000000
 *  
 * 45 00 6c 00 61 00 6e 00 61 00 00 00   E.l.a.n.a...
 * 08000000 
 * 19000000 
 * 01000000 
 * 01000000 
 * 00000000
 * 
 *  
 * format   dSS dddddddddSdd d (Sddddd)
 *          dddSS dddddddddSdd d (Sdddddd)
 *          
 * @version $Revision: 1.6.2.2.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class PledgeShowMemberListAll extends ServerBasePacket
{
	private static final String _S__68_PLEDGESHOWMEMBERLISTALL = "[S] 53 PledgeShowMemberListAll";
	private L2Clan _clan;
	private L2PcInstance _activeChar;
	private L2ClanMember[] _members;
	
	public PledgeShowMemberListAll(L2Clan clan, L2PcInstance activeChar)
	{
		_clan = clan;
		_activeChar = activeChar;
	}	
	
	final void runImpl()
	{
		_members = _clan.getMembers();
	}
	
	final void writeImpl()
	{
		writeC(0x53);
		
		writeD(0); //c5
		writeD(_clan.getClanId());
		writeD(0); //c5
		writeS(_clan.getName());
		writeS(_clan.getLeaderName());
		
		writeD(_clan.getCrestId()); // creast id .. is used again
		writeD(_clan.getLevel());
		writeD(_clan.getHasCastle());
		writeD(_clan.getHasHideout());
		writeD(0);
		writeD(_activeChar.getLevel()); 
		writeD(0);
		writeD(0);
		
		writeD(_clan.getAllyId());
		writeS(_clan.getAllyName());
		writeD(_clan.getAllyCrestId());
        writeD(_clan.isAtWar());// new c3
		writeD(_members.length);
		for (L2ClanMember m : _members)
		{
			writeS(m.getName());
			writeD(m.getLevel());
			writeD(m.getClassId());
			writeD(0); 
			writeD(m.getObjectId());//writeD(1); 
			writeD(m.isOnline() ? 1 : 0);  // 1=online 0=offline
			writeD(0); //c5 grade/power?
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__68_PLEDGESHOWMEMBERLISTALL;
	}

}
