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


import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;


/**
 */
public class AllyInfo extends ServerBasePacket
{
	//private static Logger _log = Logger.getLogger(AllyInfo.class.getName());
	private static final String _S__7A_FRIENDLIST = "[S] 7a AllyInfo";
	private static L2PcInstance _cha ;

	public AllyInfo(L2PcInstance cha)
	{
		_cha=cha;
	}	

	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
	    L2PcInstance activeChar = getClient().getActiveChar();
	    if (activeChar == null) return;
        
	    if (activeChar.getAllyId() == 0)
	    {
	        _cha.sendPacket(new SystemMessage(SystemMessage.NO_CURRENT_ALLIANCES));
            return;
	    }
        
		//======<AllyInfo>======
	    SystemMessage sm = new SystemMessage(SystemMessage.ALLIANCE_INFO_HEAD);
		_cha.sendPacket(sm);
		//======<Ally Name>======
		sm = new SystemMessage(SystemMessage.ALLIANCE_NAME_S1);
		sm.addString(_cha.getClan().getAllyName());
		_cha.sendPacket(sm);
		int online = 0;
		int count = 0;
		int clancount = 0;
		for (L2Clan clan : ClanTable.getInstance().getClans()){
		    if (clan.getAllyId() == _cha.getAllyId()){
			clancount++;
			online+=clan.getOnlineMembers("").length;
			count+=clan.getMembers().length;
		    }
		}
		//Connection 
		sm = new SystemMessage(SystemMessage.CONNECTION_S1_TOTAL_S2);
		sm.addString(""+online);
		sm.addString(""+count);
		_cha.sendPacket(sm);
		L2Clan leaderclan = ClanTable.getInstance().getClan(_cha.getAllyId());
		sm = new SystemMessage(SystemMessage.ALLIANCE_LEADER_S2_OF_S1);
        sm.addString(leaderclan.getName());
		sm.addString(leaderclan.getLeaderName());
		_cha.sendPacket(sm);
		//clan count
		sm = new SystemMessage(SystemMessage.ALLIANCE_CLAN_TOTAL_S1);
		sm.addString(""+clancount);
		_cha.sendPacket(sm);
		//clan information
		sm = new SystemMessage(SystemMessage.CLAN_INFO_HEAD);
		_cha.sendPacket(sm);
		for (L2Clan clan : ClanTable.getInstance().getClans()){
		    if (clan.getAllyId() == _cha.getAllyId()){
			//clan name
			sm = new SystemMessage(SystemMessage.CLAN_INFO_NAME);
			sm.addString(clan.getName());
			_cha.sendPacket(sm);
			//clan leader name
			sm = new SystemMessage(SystemMessage.CLAN_INFO_LEADER);
			sm.addString(clan.getLeaderName());
			_cha.sendPacket(sm);
			//clan level
			sm = new SystemMessage(SystemMessage.CLAN_INFO_LEVEL);
			sm.addNumber(clan.getLevel());
			_cha.sendPacket(sm);
			//---------
			sm = new SystemMessage(SystemMessage.CLAN_INFO_SEPARATOR);
			_cha.sendPacket(sm);
		    }
		}
		//=========================
		sm = new SystemMessage(SystemMessage.CLAN_INFO_FOOT);
		_cha.sendPacket(sm);
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__7A_FRIENDLIST;
	}
}
