package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public final class ClanWarehouse extends Warehouse
{
	//private static final Logger _log = Logger.getLogger(PcWarehouse.class.getName());

	private L2Clan _clan;

	public ClanWarehouse(L2Clan clan)
	{
		_clan = clan;
	}

	public int getOwnerId() { return _clan.getClanId(); }
	public L2PcInstance getOwner() { return _clan.getLeader().getPlayerInstance(); }
	public ItemLocation getBaseLocation() { return ItemLocation.CLANWH; }
	public String getLocationId() { return "0"; }
    public int getLocationId(@SuppressWarnings("unused") boolean dummy) { return 0; }
    public void setLocationId(@SuppressWarnings("unused") L2PcInstance dummy) { }

}