package net.sf.l2j.gameserver.serverpackets;

import java.util.List;

/**
 * Format: (ch) d[d]
 *
 * @author  -Wooden-
 */
public class ExCursedWeaponList extends ServerBasePacket
{
	private static final String _S__FE_45_EXCURSEDWEAPONLIST = "[S] FE:45 ExCursedWeaponList";
	private List<Integer> _cursedWeaponIds;
	
	public ExCursedWeaponList(List<Integer> cursedWeaponIds)
	{
		_cursedWeaponIds = cursedWeaponIds;
	}
	
	/**
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#runImpl()
	 */
	@Override
	void runImpl()
	{
		// no long running task		
	}

	/**
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	void writeImpl()
	{
		writeC(0xfe);
		writeH(0x45);
		
		writeD(_cursedWeaponIds.size());
		for(Integer i : _cursedWeaponIds)
		{
			writeD(i.intValue());
		}
	}

	/**
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_45_EXCURSEDWEAPONLIST;
	}
	
}