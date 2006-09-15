/**
 * 
 */
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author zabbix
 *
 */
public class PartyMemberPosition extends ServerBasePacket
{
	L2Party _party;

	public PartyMemberPosition(L2PcInstance actor)
	{
		_party = actor.getParty();
	}

	void runImpl()
	{
	}

	void writeImpl()
	{
		writeC(0xa7);
		writeD(_party.getMemberCount());
		
		for(L2PcInstance pm : _party.getPartyMembers())
		{
            if (pm == null) continue;
            
			writeD(pm.getObjectId());
			writeD(pm.getX());
			writeD(pm.getY());
			writeD(pm.getZ());
		}
	}

	public String getType()
	{
		return null;
	}

}
