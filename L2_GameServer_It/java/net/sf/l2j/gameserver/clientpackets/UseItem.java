/*
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
package net.sf.l2j.gameserver.clientpackets;

import java.util.Arrays;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.ShowCalculator;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
/**
 * This class ...
 * 
 * @version $Revision: 1.18.2.7.2.9 $ $Date: 2005/03/27 15:29:30 $
 */
public final class UseItem extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(UseItem.class.getName());
	private static final String _C__14_USEITEM = "[C] 14 UseItem";

	private int _objectId;
	
	protected void readImpl()
	{
		_objectId = readD();
	}

	protected void runImpl()
	{

		L2PcInstance activeChar = getClient().getActiveChar();
        
		if (activeChar == null) 
            return;

		if (activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			activeChar.sendPacket(new ActionFailed());
			return;
		}

		// NOTE: disabled due to deadlocks
//        synchronized (activeChar.getInventory())
//		{
			L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);

			if (item == null)
                return;

			// Alt game - Karma punishment // SOE
			int itemId = item.getItemId();
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT
				&& (itemId == 736 || itemId == 1538 || itemId == 1829
					|| itemId == 1830 || itemId == 3958 || itemId == 5858 || itemId == 5859)
				&& activeChar.getKarma() > 0) return;

			// Items that cannot be used
			if (itemId == 57) 
                return;
            
            if (activeChar.isFishing() && (itemId < 6535 || itemId > 6540))
            {
                // You cannot do anything else while fishing
                SystemMessage sm = new SystemMessage(SystemMessage.CANNOT_DO_WHILE_FISHING_3);
                getClient().getActiveChar().sendPacket(sm);
                sm = null;
                return;
            }
            
			// Char cannot use item when dead
			if (activeChar.isDead())
			{
				SystemMessage sm = new SystemMessage(SystemMessage.S1_CANNOT_BE_USED);
				sm.addItemName(itemId);
				getClient().getActiveChar().sendPacket(sm);
				sm = null;
				return;
			}
            
			// Char cannot use pet items
			if (item.getItem().isForWolf() || item.getItem().isForHatchling()
				|| item.getItem().isForStrider())
			{
				SystemMessage sm = new SystemMessage(600); // You cannot equip a pet item.
				sm.addItemName(itemId);
				getClient().getActiveChar().sendPacket(sm);
				sm = null;
				return;
			}
            
			if (Config.DEBUG) 
                _log.finest(activeChar.getObjectId() + ": use item " + _objectId);

			if (item.isEquipable())
			{
                // Don't allow weapon/shield equipment if mounted
				int bodyPart = item.getItem().getBodyPart();
                if (activeChar.isMounted() 
                	&& (bodyPart == L2Item.SLOT_LR_HAND 
                        || bodyPart == L2Item.SLOT_L_HAND 
                        || bodyPart == L2Item.SLOT_R_HAND))
                {
                	return;
                }
                
                // Prevent player to remove the weapon on special conditions
                if ((activeChar.isStunned() || activeChar.isSleeping() || activeChar.isAttackingNow()
                		|| activeChar.isCastingNow() || activeChar.isParalyzed() || activeChar.isAlikeDead())
                        && (bodyPart == L2Item.SLOT_LR_HAND 
                            || bodyPart == L2Item.SLOT_L_HAND 
                            || bodyPart == L2Item.SLOT_R_HAND))
                {
                    return;
                }
                /* Since c5 you can equip weapon again
                // Don't allow weapon/shield equipment if wearing formal wear
                if (activeChar.isWearingFormalWear()
                	&& (bodyPart == L2Item.SLOT_LR_HAND 
                            || bodyPart == L2Item.SLOT_L_HAND 
                            || bodyPart == L2Item.SLOT_R_HAND))
                {
        				SystemMessage sm = new SystemMessage(SystemMessage.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR);
        				activeChar.sendPacket(sm);
                        return;
                } */
                
                // Don't allow weapon/shield equipment if a cursed weapon is equiped
                if (activeChar.isCursedWeaponEquiped()
                		&& ((bodyPart == L2Item.SLOT_LR_HAND 
                				|| bodyPart == L2Item.SLOT_L_HAND 
                				|| bodyPart == L2Item.SLOT_R_HAND)
                		|| itemId == 6408)) // Don't allow to put formal wear
                {
                	return;
                }
                
                // Equip or unEquip
                L2ItemInstance[] items = null;
                boolean isEquiped = item.isEquipped();
	            SystemMessage sm = null;
	            L2ItemInstance old = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
	            if (old == null)
	            	old = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);

	            activeChar.checkSSMatch(item, old);

	            if (isEquiped)
                {
		            if (item.getEnchantLevel() > 0)
		            {
		            	sm = new SystemMessage(SystemMessage.EQUIPMENT_S1_S2_REMOVED);
		            	sm.addNumber(item.getEnchantLevel());
		            	sm.addItemName(itemId);
		            }
		            else
		            {
			            sm = new SystemMessage(SystemMessage.S1_DISARMED);
			            sm.addItemName(itemId);
		            }
		            activeChar.sendPacket(sm);
		            
		            int slot = activeChar.getInventory().getSlotFromItem(item);
                	items = activeChar.getInventory().unEquipItemInBodySlotAndRecord(slot);
                }
                else
                {
					if (item.getEnchantLevel() > 0)
					{
						sm = new SystemMessage(SystemMessage.S1_S2_EQUIPPED);
						sm.addNumber(item.getEnchantLevel());
						sm.addItemName(itemId);
					}
					else
					{
						sm = new SystemMessage(SystemMessage.S1_EQUIPPED);
						sm.addItemName(itemId);
					}
					activeChar.sendPacket(sm);
					
					items = activeChar.getInventory().equipItemAndRecord(item);
                }
                sm = null;

                activeChar.refreshExpertisePenalty();
                
				if (item.getItem().getType2() == L2Item.TYPE2_WEAPON)
					activeChar.CheckIfWeaponIsAllowed();

				InventoryUpdate iu = new InventoryUpdate();
				iu.addItems(Arrays.asList(items));
				activeChar.sendPacket(iu);
				activeChar.sendPacket(new EtcStatusUpdate());
				activeChar.abortAttack();
				activeChar.broadcastUserInfo();
			}
			else
			{
                L2Weapon weaponItem = activeChar.getActiveWeaponItem();
                int itemid = item.getItemId();
				//_log.finest("item not equipable id:"+ item.getItemId());
                if (itemid == 4393) 
                {
                        activeChar.sendPacket(new ShowCalculator(4393));
                }
                else if ((weaponItem != null && weaponItem.getItemType() == L2WeaponType.ROD)
                    && ((itemid >= 6519 && itemid <= 6527) || (itemid >= 7610 && itemid <= 7613) || (itemid >= 7807 && itemid <= 7809) || (itemid >= 8484 && itemid <= 8486) || (itemid >= 8505 && itemid <= 8513)))
                {
                    activeChar.getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, item);
                
                    // Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
                    ItemList il = new ItemList(activeChar, false);
                    sendPacket(il);
                    return;
                }
				else
				{
					IItemHandler handler = ItemHandler.getInstance().getItemHandler(item.getItemId());
                    
					if (handler == null) 
                        _log.warning("No item handler registered for item ID " + item.getItemId() + ".");
					else 
                        handler.useItem(activeChar, item);
				}
			}
//		}
	}

	public String getType()
	{
		return _C__14_USEITEM;
	}

}
