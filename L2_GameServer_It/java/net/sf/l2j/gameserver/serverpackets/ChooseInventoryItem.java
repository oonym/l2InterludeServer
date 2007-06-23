package net.sf.l2j.gameserver.serverpackets;

public class ChooseInventoryItem extends L2GameServerPacket
{
	private static final String _S__6F_CHOOSEINVENTORYITEM = "[S] 6f ChooseInventoryItem";

	private int _itemId;
	   
	public ChooseInventoryItem(int itemId)
	{
		_itemId=itemId;
	}
	
	protected final void writeImpl()
	{
		writeC(0x6f);
		writeD(_itemId);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__6F_CHOOSEINVENTORYITEM;
	}
}
