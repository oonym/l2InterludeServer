package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;

/**
 * 
 *
 * @author  -Wooden-
 */
public class PackageSendableList extends L2GameServerPacket
{
	private static final String _S__C3_PACKAGESENDABLELIST = "[S] C3 PackageSendableList";
	private L2ItemInstance[] _items;
	private int _playerOID;

	public PackageSendableList(L2ItemInstance[] items, int playerOID)
	{
		_items = items;
		_playerOID = playerOID;
	}

	/**
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected
	void writeImpl()
	{
		writeC(0xC3);
		
		writeD(_playerOID);
		writeD(getClient().getActiveChar().getAdena());
		writeD(_items.length);
		for(L2ItemInstance item : _items) // format inside the for taken from SellList part use should be about the same
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(item.getCount());
			writeH(item.getItem().getType2());
			writeH(0x00);
			writeD(item.getItem().getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(0x00);
			writeH(0x00);
			writeD(item.getObjectId()); // some item identifier later used by client to answer (see RequestPackageSend) not item id nor object id maybe some freight system id??
		}
		
	}

	/**
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__C3_PACKAGESENDABLELIST;
	}
	
}