package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class PcWarehouse extends Warehouse
{
	//private static final Logger _log = Logger.getLogger(PcWarehouse.class.getName());

	private L2PcInstance _owner;

	public PcWarehouse(L2PcInstance owner)
	{
		_owner = owner;
	}

	public L2PcInstance getOwner() { return _owner; }
	public ItemLocation getBaseLocation() { return ItemLocation.WAREHOUSE; }
	public String getLocationId() { return "0"; }
	public int getLocationId(@SuppressWarnings("unused") boolean dummy) { return 0; }
	public void setLocationId(@SuppressWarnings("unused") L2PcInstance dummy) { return; }

	public boolean validateCapacity(int slots)
	{
		return (_items.size() + slots <= _owner.GetWareHouseLimit());
	}
}