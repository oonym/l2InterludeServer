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

package net.sf.l2j.gameserver.model;

import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * 
 *
 * @author  Luno
 */
public final class L2ArmorSet
{
	private final int chest;
	private final int legs;
	private final int head;
	private final int gloves;
	private final int feet;
	private final int skill_id;
	
	private final int shield;
	private final int shield_skill_id;
	
	private final int enchant6skill;
	
	public L2ArmorSet(int chest, int legs, int head, int gloves, int feet, int skill_id, int shield, int shield_skill_id, int enchant6skill)
	{
		this.chest = chest;
		this.legs  = legs;
		this.head  = head;
		this.gloves = gloves;
		this.feet  = feet;
		this.skill_id = skill_id;
		
		this.shield = shield;
		this.shield_skill_id = shield_skill_id;
		
		this.enchant6skill = enchant6skill;
	}
	/**
	 * Checks if player have equiped all items from set (not checking shield)
	 * @param player whose inventory is being checked
	 * @return True if player equips whole set
	 */
	public boolean containAll(L2PcInstance player)
	{
		Inventory inv = player.getInventory();
		
		L2ItemInstance legsItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		L2ItemInstance headItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		L2ItemInstance feetItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);
		
		int legs = 0;
		int head = 0;
		int gloves = 0;
		int feet = 0;
		
		if(legsItem != null)   legs = legsItem.getItemId();
		if(headItem != null)   head = headItem.getItemId();
		if(glovesItem != null) gloves = glovesItem.getItemId();
		if(feetItem != null)   feet = feetItem.getItemId();
		
		return containAll(this.chest,legs,head,gloves,feet);
		
	}
	public boolean containAll(int chest, int legs, int head, int gloves, int feet)
	{
		if(this.chest != 0 && this.chest != chest)
			return false;
		if(this.legs != 0 && this.legs != legs)
			return false;
		if(this.head != 0 && this.head != head)
			return false;
		if(this.gloves != 0 && this.gloves != gloves)
			return false;
		if(this.feet != 0 && this.feet != feet)
			return false;
	
		return true;
	}
	public boolean containItem(int slot, int itemId)
	{
		switch(slot)
		{
		case Inventory.PAPERDOLL_CHEST:
			return chest == itemId;
		case Inventory.PAPERDOLL_LEGS:
			return legs == itemId;
		case Inventory.PAPERDOLL_HEAD:
			return head == itemId;
		case Inventory.PAPERDOLL_GLOVES:
			return gloves == itemId;
		case Inventory.PAPERDOLL_FEET:
			return feet == itemId;
		default:
			return false;
		}
	}
	public int getSkillId()
	{
		return skill_id;
	}
	public boolean containShield(L2PcInstance player)
	{
		Inventory inv = player.getInventory();
		
		L2ItemInstance shieldItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if(shieldItem!= null && shieldItem.getItemId() == shield)
			return true;
	
		return false;
	}
	public boolean containShield(int shield_id)
	{
		if(shield == 0)
			return false;
		
		return shield == shield_id;
	}
	public int getShieldSkillId()
	{
		return shield_skill_id;
	}
	public int getEnchant6skillId()
	{
		return enchant6skill;
	}
	/**
	 * Checks if all parts of set are enchanted to +6 or more
	 * @param player
	 * @return 
	 */
	public boolean isEnchanted6(L2PcInstance player)
	{
		 // Player don't have full set
		if(!containAll(player))
			return false;
		
		Inventory inv = player.getInventory();
		
		L2ItemInstance chestItem  = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		L2ItemInstance legsItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		L2ItemInstance headItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		L2ItemInstance feetItem   = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);
		
		if(chestItem.getEnchantLevel() < 6)
			return false;
		if(this.legs != 0 && legsItem.getEnchantLevel() < 6)
			return false;
		if(this.gloves != 0 && glovesItem.getEnchantLevel() < 6)
			return false;
		if(this.head != 0 && headItem.getEnchantLevel() < 6)
			return false;
		if(this.feet != 0 && feetItem.getEnchantLevel() < 6)
			return false;
		
		return true;
	}
}
