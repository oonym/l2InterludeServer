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
package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author mkizub
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
final class ConditionSlotItemType extends ConditionInventory {

	final int _mask;
	
	ConditionSlotItemType(int slot, int mask)
	{
		super(slot);
		_mask = mask;
	}
	
	public boolean testImpl(Env env)
	{
		if (!(env._player instanceof L2PcInstance))
			return false;
		Inventory inv = ((L2PcInstance)env._player).getInventory();
		L2ItemInstance item = inv.getPaperdollItem(_slot); 
		if (item == null)
			return false;
		return (item.getItem().getItemMask() & _mask) != 0;
	}
}
