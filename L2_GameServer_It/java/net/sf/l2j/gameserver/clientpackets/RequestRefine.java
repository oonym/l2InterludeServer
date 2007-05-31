/* This program is free software; you can redistribute it and/or modify
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

import net.sf.l2j.gameserver.datatables.AugmentationData;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ExVariationResult;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;

/**
 * Format:(ch) dddd
 * @author  -Wooden-
 */
public final class RequestRefine extends L2GameClientPacket
{
	private static final String _C__D0_2C_REQUESTREFINE = "[C] D0:2C RequestRefine";
	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _gemstoneItemObjId;
	private int _gemstoneCount;
	
	protected void readImpl()
	{
		_targetItemObjId = readD();
		_refinerItemObjId = readD();
		_gemstoneItemObjId = readD();
		_gemstoneCount = readD();
	}

	/**
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected
	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		L2ItemInstance targetItem = (L2ItemInstance)L2World.getInstance().findObject(_targetItemObjId);
		L2ItemInstance refinerItem = (L2ItemInstance)L2World.getInstance().findObject(_refinerItemObjId);
		L2ItemInstance gemstoneItem = (L2ItemInstance)L2World.getInstance().findObject(_gemstoneItemObjId);
		
		if (activeChar == null || targetItem == null || refinerItem == null || gemstoneItem == null ||
				targetItem.getOwnerId() != activeChar.getObjectId() ||
				refinerItem.getOwnerId() != activeChar.getObjectId() ||
				gemstoneItem.getOwnerId() != activeChar.getObjectId())
		{
			activeChar.sendPacket(new ExVariationResult(0,0,0));
			activeChar.sendPacket(new SystemMessage(SystemMessage.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS));
			return;
		}
		
		// unequip item
		if (targetItem.isEquipped()) activeChar.disarmWeapons();
		
		if (TryAugmentItem(activeChar, targetItem, refinerItem, gemstoneItem))
		{
			int stat12 = 0x0000FFFF&targetItem.getAugmentation().getAugmentationId();
			int stat34 = targetItem.getAugmentation().getAugmentationId()>>16;
			activeChar.sendPacket(new ExVariationResult(stat12,stat34,1));
			activeChar.sendPacket(new SystemMessage(SystemMessage.THE_ITEM_WAS_SUCCESSFULLY_AUGMENTED));
		}
		else
		{
			activeChar.sendPacket(new ExVariationResult(0,0,0));
			activeChar.sendPacket(new SystemMessage(SystemMessage.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS));
		}
	}
	
	boolean TryAugmentItem(L2PcInstance player, L2ItemInstance targetItem,L2ItemInstance refinerItem, L2ItemInstance gemstoneItem)
	{
		if (targetItem.isAugmented() || targetItem.isWear()) return false;

		int itemGrade = targetItem.getItem().getItemGrade();
		int itemType = targetItem.getItem().getType2();
		int lifeStoneId = refinerItem.getItemId();
		int gemstoneItemId = gemstoneItem.getItemId();
		
		// is the refiner Item a life stone?
		if (lifeStoneId < 8723 || lifeStoneId > 8762) return false;
		
		// must be a weapon, must be > d grade
		// TODO: can do better? : currently: using isdestroyable() as a check for hero / cursed weapons
		if (itemGrade < L2Item.CRYSTAL_C || itemType != L2Item.TYPE2_WEAPON || !targetItem.isDestroyable()) return false;
		
		// player must be able to use augmentation
		if (player.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_NONE || player.isDead()
				|| player.isParalyzed() || player.isFishing() || player.isSitting()) return false;
		
		int modifyGemstonCount = 30;
		int lifeStoneLevel = getLifeStoneLevel(lifeStoneId);
		switch (itemGrade)
		{
			case L2Item.CRYSTAL_C:
				if (player.getLevel() < 40 || gemstoneItemId != 2130) return false;
				modifyGemstonCount = 20;
				break;
			case L2Item.CRYSTAL_B:
				if (player.getLevel() < 52 || gemstoneItemId != 2130) return false;
				modifyGemstonCount = 30;
				break;
			case L2Item.CRYSTAL_A:
				if (player.getLevel() < 61 || gemstoneItemId != 2131) return false;
				modifyGemstonCount = 20;
				break;
			case L2Item.CRYSTAL_S:
				if (player.getLevel() < 76 || gemstoneItemId != 2131) return false;
				modifyGemstonCount = 25;
				break;
		}
		
		// check if the lifestone is appropriate for this player
		switch (lifeStoneLevel)
		{
			case 1:
				if (player.getLevel() < 46 || player.getLevel() >= 49) return false;
				break;
			case 2:
				if (player.getLevel() < 49  || player.getLevel() >= 51) return false;
				break;
			case 3:
				if (player.getLevel() < 51  || player.getLevel() >= 54) return false;
				break;
			case 4:
				if (player.getLevel() < 54  || player.getLevel() >= 57) return false;
				break;
			case 5:
				if (player.getLevel() < 57  || player.getLevel() >= 60) return false;
				break;
			case 6:
				if (player.getLevel() < 60  || player.getLevel() >= 63) return false;
				break;
			case 7:
				if (player.getLevel() < 63  || player.getLevel() >= 66) return false;
				break;
			case 8:
				if (player.getLevel() < 66  || player.getLevel() >= 69) return false;
				break;
			case 9:
				if (player.getLevel() < 69  || player.getLevel() >= 75) return false;
				break;
			case 10:
				if (player.getLevel() < 75) return false;
				break;
		}
				
		if (gemstoneItem.getCount()-modifyGemstonCount < 0) return false;
		player.destroyItem("RequestRefine", _gemstoneItemObjId, modifyGemstonCount, null, false);
		if (gemstoneItem.getCount() == 0) player.destroyItem("RequestRefine", gemstoneItem, null, false);
		
		// consume the life stone
		player.destroyItem("RequestRefine", refinerItem, null, false);

		// generate augmentation
		targetItem.setAugmentation(AugmentationData.getInstance().generateRandomAugmentation(targetItem, lifeStoneLevel));

		// send an inventory update packet
		InventoryUpdate iu = new InventoryUpdate();
		iu.addItem(targetItem);
		iu.addItem(refinerItem);
		iu.addItem(gemstoneItem);
		player.sendPacket(iu);
		
		return true;
	}
	
	private int getLifeStoneGrade(int itemId)
	{
		itemId -= 8723;
		if (itemId < 10) return 0;
		if (itemId < 20) return 1;
		if (itemId < 30) return 2;
		return 3;
	}
	
	private int getLifeStoneLevel(int itemId)
	{
		itemId -= 10 * getLifeStoneGrade(itemId);
		itemId -= 8722;
		return itemId;
	}

	/**
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_2C_REQUESTREFINE;
	}
}
