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
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.PetInfo;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * Beast SoulShot Handler
 * 
 * @author Tempy
 */
public class BeastSoulShot implements IItemHandler
{
    // All the item IDs that this handler knows.
    private static short[] _itemIds = {6645};
    
    public void useItem(L2PlayableInstance playable, L2ItemInstance item)
    {
    	if (playable == null) return;
    	
        L2PcInstance activeOwner = null;
        if (playable instanceof L2Summon)
        {
            activeOwner = ((L2Summon)playable).getOwner();
            activeOwner.sendPacket(new SystemMessage(SystemMessage.PET_CANNOT_USE_ITEM));
            return;
        } else if (playable instanceof L2PcInstance)
        {
        	activeOwner = (L2PcInstance)playable;
        }
        
        if (activeOwner == null)
        	return;
        L2Summon activePet = activeOwner.getPet();
        
        if (activePet == null)
        {
            activeOwner.sendPacket(new SystemMessage(574));
            return;
        }
        
        if (activePet.isDead())
        {
            activeOwner.sendPacket(new SystemMessage(1598));
            return;
        }
        
        short itemId = 6645;
        int shotConsumption = 1;
        L2ItemInstance weaponInst = null;
        L2Weapon weaponItem = null;
        
        if (activePet instanceof L2PetInstance)
        {
            weaponInst = ((L2PetInstance)activePet).getActiveWeaponInstance();
            weaponItem = ((L2PetInstance)activePet).getActiveWeaponItem();
            
            if (weaponInst == null)
            {
                activeOwner.sendPacket(new SystemMessage(SystemMessage.CANNOT_USE_SOULSHOTS));
                return;
            }
            
            if (weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE)
            {
                // SoulShots are already active.
                return;
            }
        
            int shotCount = item.getCount();
            shotConsumption = weaponItem.getSoulShotCount();
            
            if (shotConsumption == 0)
            {
                activeOwner.sendPacket(new SystemMessage(SystemMessage.CANNOT_USE_SOULSHOTS));
                return;
            }   
            
            if (!(shotCount > shotConsumption))
            {
                // Not enough Soulshots to use.
                activeOwner.sendPacket(new SystemMessage(1701));
                return;
            }

            weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_SOULSHOT);
        }
        else
        {
            if (activePet.getChargedSoulShot() != L2ItemInstance.CHARGED_NONE)
                return;
            
            activePet.setChargedSoulShot(L2ItemInstance.CHARGED_SOULSHOT);
        }
        
        // If the player doesn't have enough beast soulshot remaining, remove any auto soulshot task.
        if (!activeOwner.destroyItem("Consume", item.getObjectId(), shotConsumption, null, false))
        {
            if (activeOwner.getAutoSoulShot().contains(itemId))
            {
                activeOwner.removeAutoSoulShot(itemId);
                activeOwner.sendPacket(new ExAutoSoulShot(itemId, 0));
                
                SystemMessage sm = new SystemMessage(SystemMessage.AUTO_USE_OF_S1_CANCELLED); 
                sm.addString(item.getItem().getName());
                activeOwner.sendPacket(sm);
                return;
            }

            activeOwner.sendPacket(new SystemMessage(SystemMessage.NOT_ENOUGH_SOULSHOTS));
            return;     
        }

        // Update used soulshot count and send a Server->Client update.
        activePet.increaseUsedSoulShots(1);
        activeOwner.sendPacket(new PetInfo(activePet));
        
        // Pet uses the power of spirit.
        activeOwner.sendPacket(new SystemMessage(1576));
        
        Broadcast.toSelfAndKnownPlayersInRadius(activeOwner, new MagicSkillUser(activePet, activePet, 2033, 1, 0, 0), 360000/*600*/);
    }
    
    public short[] getItemIds()
    {
        return _itemIds;
    }
}
