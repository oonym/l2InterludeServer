package net.sf.l2j.gameserver.serverpackets;

/**
 * Format: (ch)
 *
 * @author  -Wooden-
 */
public class ShowPCCafeCouponShowUI extends L2GameServerPacket
{
	private static final String _S__FE_43_SHOWPCCAFECOUPONSHOWUI = "[S] FE:43 ShowPCCafeCouponShowUI";

	/**
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x43);
	}

	/**
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_43_SHOWPCCAFECOUPONSHOWUI;
	}
	
}