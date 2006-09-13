package net.sf.l2j.gameserver.model;

import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class PcFreight extends ItemContainer
{
    //private static final Logger _log = Logger.getLogger(PcFreight.class.getName());
    
    private L2PcInstance _owner;    // This is the L2PcInstance that owns this Freight;
    private int _activeLocationId;
    
    public PcFreight(L2PcInstance owner)
    {
        _owner = owner;
    }
    
	public L2PcInstance getOwner() { return _owner; }
    public ItemLocation getBaseLocation() { return ItemLocation.FREIGHT; }
    public void setActiveLocation(int locationId) { _activeLocationId = locationId; }
    public int getactiveLocation() { return _activeLocationId; }
    
	/**
	 * Returns the quantity of items in the inventory
	 * @return int
	 */
	public int getSize() 
	{
		int size = 0;
		for (L2ItemInstance item : _items)
		{
			if (item.getEquipSlot() == 0 || _activeLocationId == 0 
        			|| item.getEquipSlot() == _activeLocationId) size++;
		}
		return size;
	}
	
	/**
	 * Returns the list of items in inventory
	 * @return L2ItemInstance : items in inventory
	 */
	public L2ItemInstance[] getItems()
	{
		List<L2ItemInstance> list = new FastList<L2ItemInstance>();
		for (L2ItemInstance item : _items)
		{
			if (item.getEquipSlot() == 0 || item.getEquipSlot() == _activeLocationId) list.add(item);
		}

		return list.toArray(new L2ItemInstance[list.size()]);
	}

	/**
	 * Returns the item from inventory by using its <B>itemId</B>
	 * @param itemId : int designating the ID of the item
	 * @return L2ItemInstance designating the item or null if not found in inventory
	 */
	public L2ItemInstance getItemByItemId(int itemId)
	{
	    for (L2ItemInstance item : _items)
	        if ((item.getItemId() == itemId) 
	        	&& (item.getEquipSlot() == 0 || _activeLocationId == 0 
	        			|| item.getEquipSlot() == _activeLocationId)
	        	) return item;

	    return null;
	}

    /**
     * Adds item to PcFreight for further adjustments.
     * @param item : L2ItemInstance to be added from inventory
     */
    protected void addItem(L2ItemInstance item)
    {
        super.addItem(item);
    	if (_activeLocationId > 0) item.setLocation(item.getLocation(), _activeLocationId);
    }

    /**
	 * Get back items in PcFreight from database
	 */
    public void restore()
    {
    	int locationId = _activeLocationId;
    	_activeLocationId = 0;
    	super.restore();
    	_activeLocationId = locationId;
    }

    public boolean validateCapacity(int slots)
	{
		return (getSize() + slots <= _owner.GetFreightLimit());
	}
}