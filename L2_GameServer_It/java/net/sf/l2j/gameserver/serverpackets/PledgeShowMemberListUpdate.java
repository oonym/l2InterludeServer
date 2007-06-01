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
 * @author -Wooden-
 * 
 */
public class PledgeShowMemberListUpdate extends L2GameServerPacket
{
	private static final String _S__54_PLEDGESHOWMEMBERLISTUPDATE = "[S] 54 PledgeShowMemberListUpdate";
	private L2PcInstance _player;
	private int _pledgeType;
	private int _hasSponsor;
	private String _name;
	private int _level;
	private int _classId;
	private int _objectId;
	private int _isOnline;

	
	public PledgeShowMemberListUpdate(L2PcInstance player)
	{
		_player = player;
		_pledgeType = player.getPledgeType();
		if (_pledgeType == L2Clan.SUBUNIT_ACADEMY) 
			_hasSponsor = _player.getSponsor() != 0 ? 1 : 0;
		else 
			_hasSponsor = 0;
		_name = _player.getName();
		_level = _player.getLevel();
		_classId = _player.getClassId().getId();
		_objectId = _player.getObjectId();
		_isOnline = _player.isOnline();
	}
	
	public PledgeShowMemberListUpdate(L2ClanMember player)
	{
		_name=player.getName();
		_level=player.getLevel();
		_classId=player.getClassId();
		_objectId=player.getObjectId();
		if (player.isOnline())
			_isOnline=1;
		else
			_isOnline=0;
		_pledgeType = player.getPledgeType();
		if (_pledgeType == L2Clan.SUBUNIT_ACADEMY) 
			_hasSponsor = _player.getSponsor() != 0 ? 1 : 0;
		else 
			_hasSponsor = 0;
	 	}	

	
	protected final void writeImpl()
	{
		writeC(0x54);
		writeS(_name);
		writeD(_level);
		writeD(_classId);
		writeD(0); 
		writeD(_objectId);
		writeD(_isOnline); // 1=online 0=offline
		writeD(_pledgeType);
		writeD(_hasSponsor); 
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__54_PLEDGESHOWMEMBERLISTUPDATE;
	}

}
