package net.sf.l2j.gameserver.serverpackets;

/**
 * Format: (ch)
 *
 * @author  -Wooden-
 */
public class ExRestartClient extends L2GameServerPacket
{
	private static final String _S__FE_47_EXRESTARTCLIENT = "[S] FE:47 ExRestartClient";

	/**
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected
	void writeImpl()
	{
		writeC(0xfe);
		writeH(0x47);
	}

	/**
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_47_EXRESTARTCLIENT;
	}

}