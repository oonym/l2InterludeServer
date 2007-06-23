/*
 * $Header: MultiSellList.java, 2/08/2005 14:21:01 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 2/08/2005 14:21:01 $
 * $Revision: 1 $
 * $Log: MultiSellList.java,v $
 * Revision 1  2/08/2005 14:21:01  luisantonioa
 * Added copyright notice
 *
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Multisell;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellEntry;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellIngredient;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellListContainer;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Armor;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;


/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class MultiSellList extends L2GameServerPacket
{
    private static final String _S__D0_MULTISELLLIST = "[S] D0 MultiSellList";

    private L2PcInstance _activeChar;
    private L2NpcInstance _merchant;
    private int _listId;
    private boolean _inventoryOnly;
    private MultiSellListContainer _list;

    public MultiSellList(int listId, L2NpcInstance merchant)
    {
        _listId = listId;
        _merchant = merchant;
        _inventoryOnly = false;
        _activeChar = null;
        
        this.generateMultiSell();
    }   

    public MultiSellList(int listId, L2NpcInstance merchant, boolean inventoryOnly, L2PcInstance player)
    {
        _listId = listId;
        _inventoryOnly = inventoryOnly;
        _activeChar = player;
        _merchant = merchant;
        
        this.generateMultiSell();
    }   

    /**
     * This will generate the multisell list for the items.  There exist various
     * parameters in multisells that affect the way they will appear:
     * 1) inventory only: 
     * 		* if true, only show items of the multisell for which the
     * 		  "primary" ingredients are already in the player's inventory.  By "primary"
     * 		  ingredients we mean weapon and armor. 
     * 		* if false, show the entire list.
     * 2) maintain enchantment: presumably, only lists with "inventory only" set to true 
     * 		should sometimes have this as true.  This makes no sense otherwise...
     * 		* If true, then the product will match the enchantment level of the ingredient.
     * 		  if the player has multiple items that match the ingredient list but the enchantment
     * 		  levels differ, then the entries need to be duplicated to show the products and 
     * 		  ingredients for each enchantment level.
     * 		  For example: If the player has a crystal staff +1 and a crystal staff +3 and goes
     * 		  to exchange it at the mammon, the list should have all exchange possibilities for 
     * 		  the +1 staff, followed by all possibilities for the +3 staff.
     * 		* If false, then any level ingredient will be considered equal and product will always
     * 		  be at +0 		
     * 3) apply taxes: affects the amount of adena and ancient adena in ingredients.     
     *  
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#runImpl()
     */
    private void generateMultiSell()
    {
    	if (_merchant == null)
    		return;
    	
        MultiSellListContainer listTemplate = L2Multisell.getInstance().getList(_listId);
        if (listTemplate == null) return;

        _list = L2Multisell.getInstance().new MultiSellListContainer();
        _list.setListId(_listId);

        if (_inventoryOnly)
        {
        	if (_activeChar == null)
        		return;
        	
        	L2ItemInstance[] items;
        	if (listTemplate.getMaintainEnchantment()) 
        		items = _activeChar.getInventory().getUniqueItemsByEnchantLevel(false,false);
        	else 
        		items = _activeChar.getInventory().getUniqueItems(false,false);
        		
        	int enchantLevel;
            for (L2ItemInstance item : items)
            {
            	// only do the matchup on equipable items that are not currently equipped
            	// so for each appropriate item, produce a set of entries for the multisell list. 
            	if (!item.isWear() && ((item.getItem() instanceof L2Armor) || (item.getItem() instanceof L2Weapon)))
            	{
            		enchantLevel = (listTemplate.getMaintainEnchantment()? item.getEnchantLevel() : 0);
            		// loop through the entries to see which ones we wish to include
	                for (MultiSellEntry ent : listTemplate.getEntries())
	                {
	                	boolean doInclude = false;

	                	// check ingredients of this entry to see if it's an entry we'd like to include.
		                for (MultiSellIngredient ing : ent.getIngredients())
		                {
		                    if (item.getItemId() == ing.getItemId())
		                    {
		                    	doInclude = true;
		                        break;
		                    }
		                }
		                
		                // manipulate the ingredients of the template entry for this particular instance shown
		                // i.e: Assign enchant levels and/or apply taxes as needed.
		                if (doInclude)
		                	_list.addEntry(prepareEntry(ent, listTemplate.getApplyTaxes(), listTemplate.getMaintainEnchantment(), enchantLevel));
	                }  
            	}
            } // end for each inventory item.
        } // end if "inventory-only"
        else  // this is a list-all type
        {
        	// if no taxes are applied, no modifications are needed
    		for (MultiSellEntry ent : listTemplate.getEntries())
    			_list.addEntry(prepareEntry(ent, listTemplate.getApplyTaxes(), false, 0));        			
        }
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
    private MultiSellEntry prepareEntry(MultiSellEntry templateEntry, boolean applyTaxes, boolean maintainEnchantment, int enchantLevel)
    {
    	MultiSellEntry newEntry = L2Multisell.getInstance().new MultiSellEntry();
    	newEntry.setEntryId(templateEntry.getEntryId()*100000+enchantLevel);

        for (MultiSellIngredient ing : templateEntry.getIngredients())
        {
        	// load the ingredient from the template
        	MultiSellIngredient newIngredient = L2Multisell.getInstance().new MultiSellIngredient(ing);

        	// if taxes are to be applied, modify/add the adena count based on the template adena/ancient adena count
        	if ( applyTaxes && ((ing.getItemId() == 57) || (ing.getItemId() == 5575)) )
        	{
            	double taxRate = 0.0;
            	if (_merchant != null && _merchant.getIsInTown()) 
            		taxRate = _merchant.getCastle().getTaxRate();
            	
            	if (ing.getItemId() == 57)
            		newIngredient.setItemCount((int)Math.round(ing.getItemCount()*(1+taxRate)));
            	else	// ancient adena
            	{
            		// add the ancient adena count normally
            		newEntry.addIngredient(newIngredient);
                	double taxableCount = ing.getItemCount()*5.0/6;
                	if (taxRate==0)
                		continue;
            		newIngredient = L2Multisell.getInstance().new MultiSellIngredient(57, (int)Math.round(taxableCount*taxRate));
            	}
        	}
        	// if it is an armor/weapon, modify the enchantment level appropriately, if necessary
        	else if (maintainEnchantment)
        	{
            	L2Item tempItem = ItemTable.getInstance().createDummyItem(ing.getItemId()).getItem();
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
            	L2Item tempItem = ItemTable.getInstance().createDummyItem(ing.getItemId()).getItem();
            	if ((tempItem instanceof L2Armor) || (tempItem instanceof L2Weapon))
            		newIngredient.setEnchantmentLevel(enchantLevel);
            }
        	newEntry.addProduct(newIngredient);
        }
        return newEntry;
    }
    
    protected void writeImpl()
    {
    	// [ddddd] [dchh] [hdhdh] [hhdh]
    	
        writeC(0xd0);
        writeD(_listId);    // list id
        writeD(1);			// ?
        writeD(1);			// ?
        writeD(0x1c);		// ?
        writeD(_list == null ? 0 : _list.getEntries().size()); //list lenght
        
        if(_list != null)
        {
            for(MultiSellEntry ent : _list.getEntries())
            {
            	writeD(ent.getEntryId());
            	writeD(0x00); // C6
            	writeD(0x00); // C6
            	writeC(1);
            	writeH(ent.getProducts().size());
            	writeH(ent.getIngredients().size());
    
            	for(MultiSellIngredient i: ent.getProducts())
            	{
	            	writeH(i.getItemId());
	            	writeD(0);
	            	writeH(ItemTable.getInstance().getTemplate(i.getItemId()).getType2());
	            	writeD(i.getItemCount());
	        	    writeH(i.getEnchantmentLevel()); //enchtant lvl
	            	writeD(0x00); // C6
	            	writeD(0x00); // C6
            	}
        	    
                for(MultiSellIngredient i : ent.getIngredients())
                {
                	int typeE = ItemTable.getInstance().getTemplate(i.getItemId()).getType2();
                    writeH(i.getItemId());      //ID
                    writeH(typeE);
                    writeD(i.getItemCount());	//Count
                    writeH(i.getEnchantmentLevel()); //Enchant Level
                	writeD(0x00); // C6
                	writeD(0x00); // C6
                }
            }
        }
    }

    @Override
    public String getType()
    {
        return _S__D0_MULTISELLLIST;
    }

}
