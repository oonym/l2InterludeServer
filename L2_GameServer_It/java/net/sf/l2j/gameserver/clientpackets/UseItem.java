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
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.ShowCalculator;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.FloodProtector;

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

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{

		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
            return;

		// Flood protect UseItem
		if (!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_USEITEM))
			return;

		if (activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE));
			activeChar.sendPacket(new ActionFailed());
			return;
		}

		// NOTE: disabled due to deadlocks
//        synchronized (activeChar.getInventory())
//		{
			L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);

			if (item == null)
                return;

			if (item.isWear())
			{
				// No unequipping wear-items
				return;
			}

			int itemId = item.getItemId();
			/*
			 * Alt game - Karma punishment // SOE
			 * 736  	Scroll of Escape
			 * 1538  	Blessed Scroll of Escape
			 * 1829  	Scroll of Escape: Clan Hall
			 * 1830  	Scroll of Escape: Castle
			 * 3958  	L2Day - Blessed Scroll of Escape
			 * 5858  	Blessed Scroll of Escape: Clan Hall
			 * 5859  	Blessed Scroll of Escape: Castle
			 * 6663  	Scroll of Escape: Orc Village
			 * 6664  	Scroll of Escape: Silenos Village
			 * 7117  	Scroll of Escape to Talking Island
			 * 7118  	Scroll of Escape to Elven Village
			 * 7119  	Scroll of Escape to Dark Elf Village
			 * 7120  	Scroll of Escape to Orc Village
			 * 7121  	Scroll of Escape to Dwarven Village
			 * 7122  	Scroll of Escape to Gludin Village
			 * 7123  	Scroll of Escape to the Town of Gludio
			 * 7124  	Scroll of Escape to the Town of Dion
			 * 7125  	Scroll of Escape to Floran
			 * 7126  	Scroll of Escape to Giran Castle Town
			 * 7127  	Scroll of Escape to Hardin's Private Academy
			 * 7128  	Scroll of Escape to Heine
			 * 7129  	Scroll of Escape to the Town of Oren
			 * 7130  	Scroll of Escape to Ivory Tower
			 * 7131  	Scroll of Escape to Hunters Village
			 * 7132  	Scroll of Escape to Aden Castle Town
			 * 7133  	Scroll of Escape to the Town of Goddard
			 * 7134  	Scroll of Escape to the Rune Township
			 * 7135  	Scroll of Escape to the Town of Schuttgart.
			 * 7554  	Scroll of Escape to Talking Island
			 * 7555  	Scroll of Escape to Elven Village
			 * 7556  	Scroll of Escape to Dark Elf Village
			 * 7557  	Scroll of Escape to Orc Village
			 * 7558  	Scroll of Escape to Dwarven Village
			 * 7559  	Scroll of Escape to Giran Castle Town
			 * 7618  	Scroll of Escape - Ketra Orc Village
			 * 7619  	Scroll of Escape - Varka Silenos Village
			 */
			if (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT && activeChar.getKarma() > 0
				&& (itemId == 736 || itemId == 1538 || itemId == 1829 || itemId == 1830
				|| itemId == 3958 || itemId == 5858 || itemId == 5859 || itemId == 6663
				|| itemId == 6664 || (itemId >= 7117 && itemId <= 7135)
				|| (itemId >= 7554 && itemId <= 7559) || itemId == 7618 || itemId == 7619))
				return;

			// Items that cannot be used
			if (itemId == 57)
                return;

            if (activeChar.isFishing() && (itemId < 6535 || itemId > 6540))
            {
                // You cannot do anything else while fishing
                SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_DO_WHILE_FISHING_3);
                getClient().getActiveChar().sendPacket(sm);
                sm = null;
                return;
            }

			// Char cannot use item when dead
			if (activeChar.isDead())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addItemName(itemId);
				getClient().getActiveChar().sendPacket(sm);
				sm = null;
				return;
			}

			// Char cannot use pet items
			if (item.getItem().isForWolf() || item.getItem().isForHatchling()
				|| item.getItem().isForStrider() || item.getItem().isForBabyPet())
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_EQUIP_PET_ITEM); // You cannot equip a pet item.
				sm.addItemName(itemId);
				getClient().getActiveChar().sendPacket(sm);
				sm = null;
				return;
			}

			if (Config.DEBUG)
                _log.finest(activeChar.getObjectId() + ": use item " + _objectId);

			if (item.isEquipable())
			{
				// No unequipping/equipping while the player is in special conditions
				if (activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed()
						|| activeChar.isAlikeDead())
				{
					activeChar.sendMessage("Your status does not allow you to do that.");
					return;
				}

				int bodyPart = item.getItem().getBodyPart();
                // Prevent player to remove the weapon on special conditions
                if ((activeChar.isAttackingNow() || activeChar.isCastingNow() || activeChar.isMounted())
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
        				SystemMessage sm = new SystemMessage(SystemMessageId.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR);
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

                // Don't allow weapon/shield hero equipment during Olympiads
                if (activeChar.isInOlympiadMode()
                		&& (
                				bodyPart == L2Item.SLOT_LR_HAND
                				|| bodyPart == L2Item.SLOT_L_HAND
                				|| bodyPart == L2Item.SLOT_R_HAND
                			)
                		&& (
                				(item.getItemId() >= 6611 && item.getItemId() <= 6621) ||
                				item.getItemId() == 6842
                			)
                	)
                {
                	return;
                }

                // Don't allow weapon/shield hero equipment during Olympiads
                if (activeChar.isInOlympiadMode()
                		&& (
                				bodyPart == L2Item.SLOT_LR_HAND
                				|| bodyPart == L2Item.SLOT_L_HAND
                				|| bodyPart == L2Item.SLOT_R_HAND
                			)
                		&& (
                				(item.getItemId() >= 6611 && item.getItemId() <= 6621) ||
                				item.getItemId() == 6842
                			)
                	)
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
		            	sm = new SystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
		            	sm.addNumber(item.getEnchantLevel());
		            	sm.addItemName(itemId);
		            }
		            else
		            {
			            sm = new SystemMessage(SystemMessageId.S1_DISARMED);
			            sm.addItemName(itemId);
		            }
		            activeChar.sendPacket(sm);

		            // Remove augementation boni on unequip
		            if (item.isAugmented())
		            	item.getAugmentation().removeBoni(activeChar);

		            int slot = activeChar.getInventory().getSlotFromItem(item);
                	items = activeChar.getInventory().unEquipItemInBodySlotAndRecord(slot);
                }
                else
                {
                	int tempBodyPart = item.getItem().getBodyPart();
                	L2ItemInstance tempItem = activeChar.getInventory().getPaperdollItemByL2ItemId(tempBodyPart);

                	// remove augmentation stats for replaced items
                	// currently weapons only..
                	if (tempItem != null && tempItem.isAugmented())
                		tempItem.getAugmentation().removeBoni(activeChar);
                	else if (tempBodyPart == 0x4000)
                	{
                		L2ItemInstance tempItem2 = activeChar.getInventory().getPaperdollItem(7);
                		if (tempItem2 != null && tempItem2.isAugmented())
                			tempItem2.getAugmentation().removeBoni(activeChar);
                		tempItem2 = activeChar.getInventory().getPaperdollItem(8);
                    	if (tempItem2 != null && tempItem2.isAugmented())
                    		tempItem2.getAugmentation().removeBoni(activeChar);
                	}

                	//check if the item replaces a wear-item
                	if (tempItem != null && tempItem.isWear())
                	{
                		// dont allow an item to replace a wear-item
                		return;
                	}
                	else if (tempBodyPart == 0x4000) // left+right hand equipment
                	{
                		// this may not remove left OR right hand equipment
                		tempItem = activeChar.getInventory().getPaperdollItem(7);
                		if (tempItem != null && tempItem.isWear()) return;

                		tempItem = activeChar.getInventory().getPaperdollItem(8);
                		if (tempItem != null && tempItem.isWear()) return;
                	}
                	else if (tempBodyPart == 0x8000) // fullbody armor
                	{
                		// this may not remove chest or leggins
                		tempItem = activeChar.getInventory().getPaperdollItem(10);
                		if (tempItem != null && tempItem.isWear()) return;

                		tempItem = activeChar.getInventory().getPaperdollItem(11);
                		if (tempItem != null && tempItem.isWear()) return;
                	}

					if (item.getEnchantLevel() > 0)
					{
						sm = new SystemMessage(SystemMessageId.S1_S2_EQUIPPED);
						sm.addNumber(item.getEnchantLevel());
						sm.addItemName(itemId);
					}
					else
					{
						sm = new SystemMessage(SystemMessageId.S1_EQUIPPED);
						sm.addItemName(itemId);
					}
					activeChar.sendPacket(sm);

		            // Apply augementation boni on equip
		            if (item.isAugmented())
		            	item.getAugmentation().applyBoni(activeChar);

					items = activeChar.getInventory().equipItemAndRecord(item);

		            // Consume mana - will start a task if required; returns if item is not a shadow item
		            item.decreaseMana(false);
                }
                sm = null;

                activeChar.refreshExpertisePenalty();

				if (item.getItem().getType2() == L2Item.TYPE2_WEAPON)
					activeChar.checkIfWeaponIsAllowed();

				InventoryUpdate iu = new InventoryUpdate();
				iu.addItems(Arrays.asList(items));
				activeChar.sendPacket(iu);
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
                    activeChar.broadcastUserInfo();
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

	@Override
	public String getType()
	{
		return _C__14_USEITEM;
	}

}
