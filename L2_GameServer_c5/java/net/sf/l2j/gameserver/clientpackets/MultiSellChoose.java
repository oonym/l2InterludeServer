package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
//import java.util.logging.Logger;
import javolution.util.FastList;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellEntry;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellIngredient;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellListContainer;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;


public class MultiSellChoose extends ClientBasePacket
{
    private static final String _C__A7_MULTISELLCHOOSE = "[C] A7 MultiSellChoose";
    //private static Logger _log = Logger.getLogger(MultiSellChoose.class.getName());
    private int _listId;
    private int _entryId;
    private int _amount;
    private int _enchantment;
    private int _transactionTax;	// local handling of taxation
    
    public MultiSellChoose(ByteBuffer buf, ClientThread client)
    {
        super(buf,client);
        _listId = readD();
        _entryId = readD();
        _amount = readD();
        // _enchantment = readH();  // <---commented this line because it did NOT work!
        _enchantment = _entryId % 100000;
        _entryId = _entryId / 100000;
        _transactionTax = 0;	// initialize tax amount to 0...
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
            	doExchange(player,entry,list.getApplyTaxes(), list.getMaintainEnchantment(), _enchantment);
            	return;
            }
        }
    }
    
    private void doExchange(L2PcInstance player, MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantment)
    {
    	PcInventory inv = player.getInventory();
        
        // given the template entry and information about maintaining enchantment and applying taxes
        // re-create the instance of the entry that will be used for this exchange
    	// i.e. change the enchantment level of select ingredient/products and adena amount appropriately.
        L2NpcInstance merchant = (L2NpcInstance)player.getTarget();
        MultiSellEntry entry = prepareEntry(merchant, templateEntry, applyTaxes, maintainEnchantment, enchantment);
        
        // Generate a list of distinct ingredients and counts in order to check if the correct item-counts 
        // are possessed by the player
    	FastList<MultiSellIngredient> _ingredientsList = new FastList<MultiSellIngredient>();
    	boolean newIng = true;
    	for(MultiSellIngredient e: entry.getIngredients())
    	{
    		newIng = true;
    		
    		// at this point, the template has already been modified so that enchantments are properly included
    		// whenever they need to be applied.  Uniqueness of items is thus judged by item id AND enchantment level
    		for(MultiSellIngredient ex: _ingredientsList)
    		{
    			// if the item was already added in the list, merely increment the count
    			// this happens if 1 list entry has the same ingredient twice (example 2 swords = 1 dual)
    			if( (ex.getItemId() == e.getItemId()) && (ex.getEnchantmentLevel() == e.getEnchantmentLevel()) )
    			{
    				ex.setItemCount(ex.getItemCount() + e.getItemCount());
    				newIng = false;
    			}
    		}
    		if(newIng)
    		{
    			// if it's a new ingredient, just store its info directly (item id, count, enchantment)
    			_ingredientsList.add(L2Multisell.getInstance().new MultiSellIngredient(e));
    		}
    	}
    	// now check if the player has sufficient items in the inventory to cover the ingredients' expences
    	for(MultiSellIngredient e : _ingredientsList)
    	{
            if((double)e.getItemCount() * (double)_amount > Integer.MAX_VALUE )
            {
                player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
                _ingredientsList.clear();
                _ingredientsList = null;
                return;
            }
            // if this is not a list that maintains enchantment, check the count of all items that have the given id.
            // otherwise, check only the count of items with exactly the needed enchantment level 
    		if( inv.getInventoryItemCount(e.getItemId(), maintainEnchantment? e.getEnchantmentLevel() : -1) < (e.getItemCount() * _amount) )
    		{
    			player.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_ITEMS));
    			_ingredientsList.clear();
    			_ingredientsList = null;
    			return;
    		}
    	}
    	
    	_ingredientsList.clear();
    	_ingredientsList = null;
    	/** All ok, remove items and add final product */
    	
    	for(MultiSellIngredient e : entry.getIngredients())
    	{
			L2ItemInstance itemToTake = inv.getItemByItemId(e.getItemId());		// initialize and initial guess for the item to take.
			// if it's a stackable item, just reduce the amount from the first (only) instance that is found in the inventory 
			if (itemToTake.isStackable())	
                player.destroyItem("Multisell", itemToTake.getObjectId(), (e.getItemCount() * _amount), player.getTarget(), true);
			else
			{
				// for non-stackable items, one of two scenaria are possible:
				// a) list maintains enchantment: get the instances that exactly match the requested enchantment level
				// b) list does not maintain enchantment: get the instances with the LOWEST enchantment level

				// a) if enchantment is maintained, then get a list of items that exactly match this enchantment
				if (maintainEnchantment)
				{
					// loop through this list and remove (one by one) each item until the required amount is taken.
					L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId(), _enchantment);
        			synchronized (inventoryContents)
        			{
		                for (int i = 0; i < (e.getItemCount() * _amount); i++)
							player.destroyItem("Multisell", inventoryContents[i].getObjectId(), 1, player.getTarget(), true);
        			}
				}
				else	// b) enchantment is not maintained.  Get the instances with the LOWEST enchantment level
				{
					/* NOTE: There are 2 ways to achieve the above goal.
					 * 1) Get all items that have the correct itemId, loop through them until the lowest enchantment 
					 * 		level is found.  Repeat all this for the next item until proper count of items is reached. 
					 * 2) Get all items that have the correct itemId, sort them once based on enchantment level, 
					 * 		and get the range of items that is necessary.
					 * Method 1 is faster for a small number of items to be exchanged.
					 * Method 2 is faster for large amounts.
					 * 
					 * EXPLANATION:
					 *   Worst case scenario for algorithm 1 will make it run in a number of cycles given by:
					 * m*(2n-m+1)/2 where m is the number of items to be exchanged and n is the total
					 * number of inventory items that have a matching id.
					 *   With algorithm 2 (sort), sorting takes n*log(n) time and the choice is done in a single cycle 
					 * for case b (just grab the m first items) or in linear time for case a (find the beginning of items
					 * with correct enchantment, index x, and take all items from x to x+m).
					 * Basically, whenever m > log(n) we have: m*(2n-m+1)/2 = (2nm-m*m+m)/2 > 
					 * (2nlogn-logn*logn+logn)/2 = nlog(n) - log(n*n) + log(n) = nlog(n) + log(n/n*n) =
					 * nlog(n) + log(1/n) = nlog(n) - log(n) = (n-1)log(n) 
					 * So for m < log(n) then m*(2n-m+1)/2 > (n-1)log(n) and m*(2n-m+1)/2 > nlog(n) 
					 * 
					 * IDEALLY:
					 * In order to best optimize the performance, choose which algorithm to run, based on whether 2^m > n
					 * if ( (2<<(e.getItemCount() * _amount)) < inventoryContents.length )
					 *   // do Algorithm 1, no sorting
					 * else
					 *   // do Algorithm 2, sorting  
					 * 
					 * CURRENT IMPLEMENTATION:
					 * In general, it is going to be very rare for a person to do a massive exchange of non-stackable items
					 * For this reason, we assume that algorithm 1 will always suffice and we keep things simple.
					 * If, in the future, it becomes necessary that we optimize, the above discussion should make it clear
					 * what optimization exactly is necessary (based on the comments under "IDEALLY"). 
					 */
					
					// choice 1.  Small number of items exchanged.  No sorting.
	                for (int i = 1; i <= (e.getItemCount() * _amount); i++)
	                {
	    				L2ItemInstance[] inventoryContents = inv.getAllItemsByItemId(e.getItemId());
	
	        			synchronized (inventoryContents)
	        			{
	        				itemToTake = inventoryContents[0];
        					// get item with the LOWEST enchantment level from the inventory...+0 is lowest by default...
	        				if(itemToTake.getEnchantLevel() > 0)
	        				{
		        				for (int j=0; j<inventoryContents.length; j++)
		        				{
		        					if (inventoryContents[j].getEnchantLevel() < itemToTake.getEnchantLevel())
		        					{
		        						itemToTake = inventoryContents[j];
		        						// nothing will have enchantment less than 0.  If a zero-enchanted item is found, just take it
		        						if (itemToTake.getEnchantLevel() == 0)
		        							break;
		        					}
		        				}
	        				}
	        			}				
						player.destroyItem("Multisell", itemToTake.getObjectId(), 1, player.getTarget(), true);
	                }
				}
			}
    	}
    	
    	// Generate the appropriate items
    	for(MultiSellIngredient e : entry.getProducts())
    	{
	    	if (ItemTable.getInstance().createDummyItem(e.getItemId()).isStackable())
	    	{
		    	inv.addItem("Multisell", e.getItemId(), (e.getItemCount() * _amount), player, player.getTarget());
	    	} else
	    	{
	    		L2ItemInstance product = null;
	            for (int i = 1; i <= (e.getItemCount() * _amount); i++)
	            {
	            	product = inv.addItem("Multisell", e.getItemId(), 1, player, player.getTarget());
			    	if (maintainEnchantment)
			    		product.setEnchantLevel(e.getEnchantmentLevel());
	            }
	    	}
	         // msg part
	        SystemMessage sm;
	        
	        if (e.getItemCount() * _amount > 1)
	        {
	            sm = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
	            sm.addItemName(e.getItemId());
	            sm.addNumber(e.getItemCount() * _amount);
	            player.sendPacket(sm);
	            sm = null;
	        }
	        else
	        {
	            if(maintainEnchantment && _enchantment > 0)
	            {
	                sm = new SystemMessage(SystemMessage.ACQUIRED);
	                sm.addNumber(_enchantment);
	                sm.addItemName(e.getItemId());
	            }
	            else
	            {
	                sm = new SystemMessage(SystemMessage.EARNED_ITEM);
	                sm.addItemName(e.getItemId());
	            }
	            player.sendPacket(sm);
	            sm = null;
	        }
    	}
        player.sendPacket(new ItemList(player, false));
        
        StatusUpdate su = new StatusUpdate(player.getObjectId());
        su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
        player.sendPacket(su);
        su = null;
        
        // finally, give the tax to the castle...
		if (merchant != null && merchant.getIsInTown() && merchant.getCastle().getOwnerId() > 0)
		    merchant.getCastle().addToTreasury(_transactionTax * _amount);
    }
    
	// Regarding taxation, the following appears to be the case:
	// a) The count of aa remains unchanged (taxes do not affect aa directly).
	// b) 5/6 of the amount of aa is taxed by the normal tax rate.
	// c) the resulting taxes are added as normal adena value.
    // d) normal adena are taxed fully.
    // e) Items other than adena and ancient adena are not taxed even when the list is taxable.
	// example: If the template has an item worth 120aa, and the tax is 10%,
	// then from 120aa, take 5/6 so that is 100aa, apply the 10% tax in adena (10a)
	// so the final price will be 120aa and 10a!
    private MultiSellEntry prepareEntry(L2NpcInstance merchant, MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantLevel)
    {
    	MultiSellEntry newEntry = L2Multisell.getInstance().new MultiSellEntry();
    	newEntry.setEntryId(templateEntry.getEntryId());

        for (MultiSellIngredient ing : templateEntry.getIngredients())
        {
        	// load the ingredient from the template
        	MultiSellIngredient newIngredient = L2Multisell.getInstance().new MultiSellIngredient(ing);

        	// if taxes are to be applied, modify/add the adena count based on the template adena/ancient adena count
        	if ( applyTaxes && ((newIngredient.getItemId() == 57) || (newIngredient.getItemId() == 5575)) )
        	{
            	
            	double taxRate = 0.0;
            	if (merchant != null && merchant.getIsInTown()) 
            		taxRate = merchant.getCastle().getTaxRate();
            	
            	if (newIngredient.getItemId() == 57)
            	{
                	_transactionTax = (int)Math.round(newIngredient.getItemCount()*taxRate);
            		newIngredient.setItemCount(newIngredient.getItemCount()+_transactionTax);
            	}
            	else	// ancient adena
            	{
            		// add the ancient adena count normally
            		newEntry.addIngredient(newIngredient);
                	double taxableCount = newIngredient.getItemCount()*5.0/6;
                	_transactionTax = (int)Math.round(taxableCount*taxRate);
                	if (_transactionTax==0)
                		continue;
            		newIngredient = L2Multisell.getInstance().new MultiSellIngredient(57, _transactionTax);
            	}
        	}
        	// if it is an armor/weapon, modify the enchantment level appropriately, if necessary
        	else if (maintainEnchantment)
        	{
            	L2Item tempItem = ItemTable.getInstance().createDummyItem(newIngredient.getItemId()).getItem();
            	if ((tempItem instanceof L2Armor) || (tempItem instanceof L2Weapon))
            		newIngredient.setEnchantmentLevel(enchantLevel);
        	}
        	
        	// finally, add this ingredient to the entry
        	newEntry.addIngredient(newIngredient);
        }
        // Now modify the enchantment level of products, if necessary
        for (MultiSellIngredient ing : templateEntry.getProducts())
        {
        	// load the ingredient from the template
        	MultiSellIngredient newIngredient = L2Multisell.getInstance().new MultiSellIngredient(ing);

        	if (maintainEnchantment)
            {
            	// if it is an armor/weapon, modify the enchantment level appropriately
            	// (note, if maintain enchantment is "false" this modification will result to a +0)
            	L2Item tempItem = ItemTable.getInstance().createDummyItem(newIngredient.getItemId()).getItem();
            	if ((tempItem instanceof L2Armor) || (tempItem instanceof L2Weapon))
            		newIngredient.setEnchantmentLevel(enchantLevel);
            }
        	newEntry.addProduct(newIngredient);
        }
        return newEntry;
    }
    
    public String getType()
    {
        return _C__A7_MULTISELLCHOOSE;
    }
}
