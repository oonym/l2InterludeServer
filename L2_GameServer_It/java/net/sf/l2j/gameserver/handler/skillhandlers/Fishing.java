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
package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;

public class Fishing implements ISkillHandler 
{ 
    //private static Logger _log = Logger.getLogger(SiegeFlag.class.getName()); 
	//protected SkillType[] _skillIds = {SkillType.FISHING};
	private static final SkillType[] SKILL_IDS = {SkillType.FISHING}; 
    
    public void useSkill(L2Character activeChar, @SuppressWarnings("unused") L2Skill skill, @SuppressWarnings("unused") L2Object[] targets)
    {
        if (activeChar == null || !(activeChar instanceof L2PcInstance)) return;

        L2PcInstance player = (L2PcInstance)activeChar;
		
		if (player.isFishing())
		{
			if (player.GetFishCombat() != null) player.GetFishCombat().doDie(false);
			else player.EndFishing(false);
			//Cancels fishing			
			player.sendPacket(new SystemMessage(SystemMessage.FISHING_ATTEMPT_CANCELLED));
			return;
		}
        if (player.isInBoat())
		{			
			//You can't fish while you are on boat
        	player.sendPacket(new SystemMessage(SystemMessage.CANNOT_FISH_ON_BOAT));
			if (!player.isGM())
				return;
		}
		if (!ZoneManager.getInstance().checkIfInZoneFishing(player))
		{
            //You can't fish here
			player.sendPacket(new SystemMessage(SystemMessage.CANNOT_FISH_HERE));
			if (!player.isGM())
				return;
		}
		if (player.getZ() <= -3800)
		{
            //You can't fish in water
			player.sendPacket(new SystemMessage(SystemMessage.CANNOT_FISH_UNDER_WATER));
			if (!player.isGM())
				return;
		}
		if (player.isInCraftMode() || player.isInStoreMode()) {
			player.sendPacket(new SystemMessage(SystemMessage.CANNOT_FISH_WHILE_USING_RECIPE_BOOK));
			if (!player.isGM())
				return;
		}
		L2Weapon weaponItem = player.getActiveWeaponItem();
		if ((weaponItem==null || weaponItem.getItemType() != L2WeaponType.ROD))
		{
			//Fishing poles are not installed
			player.sendPacket(new SystemMessage(SystemMessage.FISHING_POLE_NOT_EQUIPPED));
			return;
		}		
		L2ItemInstance lure = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (lure == null)
		{
		    //Not enough bait
			player.sendPacket(new SystemMessage(SystemMessage.BAIT_ON_HOOK_BEFORE_FISHING));
            return;
		}
		if (!Config.ALLOWFISHING && !player.isGM())
		{
			player.sendMessage("Not Working Yet");
			return;
		}		
		player.SetLure(lure);
		L2ItemInstance lure2 = player.getInventory().destroyItem("Consume", player.getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_LHAND), 1, player, null);

		if (lure2 == null || lure2.getCount() == 0)
		{
		    player.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_BAIT));
			player.sendPacket(new ItemList(player,false));
		}
		else
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(lure2);
			player.sendPacket(iu);
		}
		player.startFishing();		
		
		
        
        
    } 
    
    public SkillType[] getSkillIds() 
    { 
        return SKILL_IDS; 
    } 
    
}
