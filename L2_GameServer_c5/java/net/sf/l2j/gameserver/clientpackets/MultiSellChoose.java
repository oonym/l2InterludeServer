package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
//import java.util.logging.Logger;
import javolution.util.FastList;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellEntry;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellIngredient;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellListContainer;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;


public class MultiSellChoose extends ClientBasePacket
{
    private static final String _C__A7_MULTISELLCHOOSE = "[C] A7 MultiSellChoose";
    //private static Logger _log = Logger.getLogger(MultiSellChoose.class.getName());
    private int _listId;
    private int _entryId;
    private int _amount;
    
    public MultiSellChoose(ByteBuffer buf, ClientThread client)
    {
        super(buf,client);
        _listId = readD();
        _entryId = readD();
        _amount = readD();
    }
    
    public void runImpl()
    {
    	if(_amount < 1 || _amount > 5000 || _amount > Integer.MAX_VALUE )
    		return;

        MultiSellListContainer list = L2Multisell.getInstance().getList(_listId);
        if(list == null) return;
        
        L2PcInstance player = getClient().getActiveChar();
        if(player == null) return;

        for(MultiSellEntry entry : list.getEntries())
        {
            if(entry.getEntryId() == _entryId)
            {
            	doExchange(player,entry);
            	return;
            }
        }
    }
    
    private void doExchange(L2PcInstance player, MultiSellEntry entry)
    {
    	PcInventory inv = player.getInventory();
        
        L2ItemInstance oldItem = null;
        
        // Used only for checking amount of items
    	FastList<MultiSellIngredient> _ingredientsList = new FastList<MultiSellIngredient>();
    	boolean newIng = true;
    	for(MultiSellIngredient e: entry.getIngredients())
    	{
    		newIng = true;
    		
    		for(MultiSellIngredient ex: _ingredientsList)
    		{
    			if(ex.getItemId() == e.getItemId() && ex.getItemEnchant() == e.getItemEnchant())
    			{
    				ex.setItemCount(ex.getItemCount() + e.getItemCount());
    				newIng = false;
    			}
    		}
    		if(newIng)
    		{
    			_ingredientsList.add(L2Multisell.getInstance().new MultiSellIngredient(e));
    		}
    	}
    	for(MultiSellIngredient e : _ingredientsList)
    	{
    		L2ItemInstance item = inv.getItemByItemId(e.getItemId(), oldItem);
            
            if((double)e.getItemCount() * (double)_amount > Integer.MAX_VALUE )
            {
                player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
                _ingredientsList.clear();
                _ingredientsList = null;
                return;
            }
    		if(item == null || inv.getInventoryItemCount(item.getItemId(), e.getItemEnchant()) < (e.getItemCount() * _amount))
    		{
    			player.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_ITEMS));
    			_ingredientsList.clear();
    			_ingredientsList = null;
    			return;
    		}
            
            oldItem = item;
    	}
    	
    	_ingredientsList.clear();
    	_ingredientsList = null;
    	/** All ok, remove items and add final product */
    	
    	for(MultiSellIngredient e : entry.getIngredients())
    	{
            if (inv.getItemByItemId(e.getItemId()).isStackable())
                player.destroyItemByItemId("Multisell", e.getItemId(), (e.getItemCount() * _amount), player.getTarget(), true);
            else
                for (int i = 1; i <= (e.getItemCount() * _amount); i++)
                    player.destroyItemByItemId("Multisell", e.getItemId(), 1, player.getTarget(), true);
    	}
    	
    	// Generate the appropriate items
    	if (ItemTable.getInstance().createDummyItem(entry.getProductId()).isStackable())
    	{
	    	L2ItemInstance product = inv.addItem("Multisell", entry.getProductId(), (entry.getProductCount() * _amount), player, player.getTarget());
	    	product.setEnchantLevel(entry.getProductEnchant());
    	} else
    	{
    		L2ItemInstance product = null;
            for (int i = 1; i <= (entry.getProductCount() * _amount); i++)
            {
            	product = inv.addItem("Multisell", entry.getProductId(), 1, player, player.getTarget());
    	    	product.setEnchantLevel(entry.getProductEnchant());
            }
    	}
        
        SystemMessage sm;
        if (entry.getProductCount() * _amount > 1)
        {
            sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
            sm.addItemName(entry.getProductId());
            sm.addNumber(entry.getProductCount() * _amount);
            player.sendPacket(sm);
            sm = null;
        }
        else
        {
            if(entry.getProductEnchant() > 0)
            {
                sm = new SystemMessage(SystemMessage.ACQUIRED);
                sm.addNumber(entry.getProductEnchant());
                sm.addItemName(entry.getProductId());
            }
            else
            {
                sm = new SystemMessage(SystemMessage.EARNED_ITEM);
                sm.addItemName(entry.getProductId());
            }
            player.sendPacket(sm);
            sm = null;
        }
        player.sendPacket(new ItemList(player, false));
        
        StatusUpdate su = new StatusUpdate(player.getObjectId());
        su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
        player.sendPacket(su);
        su = null;
    }
    
    public String getType()
    {
        return _C__A7_MULTISELLCHOOSE;
    }
}
