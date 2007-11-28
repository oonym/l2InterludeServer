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

package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.RadarControl;
import net.sf.l2j.gameserver.serverpackets.ShowMiniMap;

/**
 * This class ...
 * @author Kerberos
 * @version 0.1 11/28/07
 * Created for quest item: Map - Forest of the Dead.
 */

public class ForestMap implements IItemHandler
{
	private static final int[] FOREST_MAP = {7063};
	
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance)) return;
		L2PcInstance activeChar = (L2PcInstance)playable;
		int itemId = item.getItemId();
		
		switch (itemId)
		{
		case 7063:
			activeChar.sendPacket(new ShowMiniMap(1665));
			activeChar.sendPacket(new RadarControl(0, 1, 51995, -51265, -3104));
			break;
		}
	}

	/**
	 * @see net.sf.l2j.gameserver.handler.IItemHandler#getItemIds()
	 */
	public int[] getItemIds()
	{
		return FOREST_MAP;
	}
}