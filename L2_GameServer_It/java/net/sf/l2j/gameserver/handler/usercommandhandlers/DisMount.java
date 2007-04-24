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

package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.serverpackets.Ride;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * Support for /dismount command.  
 * @author Micht
 */
public class DisMount implements IUserCommandHandler
{
    private static final int[] COMMAND_IDS = { 62 }; 
	
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public synchronized boolean useUserCommand(int id, L2PcInstance activeChar)
    {
        if (id != COMMAND_IDS[0]) return false;

        if (activeChar.isRentedPet())
        {
        	activeChar.stopRentPet();
        }
        else if (activeChar.isMounted())
        {
            // Don't allow ride with a cursed weapon equiped
            if (activeChar.isCursedWeaponEquiped()) return false;

            // Unequip the weapon
            L2ItemInstance wpn = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
            if (wpn == null) wpn = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
            if (wpn != null)
            {
            	if (wpn.isWear())
            		return false;
            	
                L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
                InventoryUpdate iu = new InventoryUpdate();
                for (int i = 0; i < unequiped.length; i++)
                    iu.addModifiedItem(unequiped[i]);
                activeChar.sendPacket(iu);

                activeChar.abortAttack();
                activeChar.refreshExpertisePenalty();
                activeChar.broadcastUserInfo();

                // this can be 0 if the user pressed the right mousebutton twice very fast
                if (unequiped.length > 0)
                {
                    SystemMessage sm = null;
                    if (unequiped[0].getEnchantLevel() > 0)
                    {
                        sm = new SystemMessage(SystemMessage.EQUIPMENT_S1_S2_REMOVED);
                        sm.addNumber(unequiped[0].getEnchantLevel());
                        sm.addItemName(unequiped[0].getItemId());
                    }
                    else
                    {
                        sm = new SystemMessage(SystemMessage.S1_DISARMED);
                        sm.addItemName(unequiped[0].getItemId());
                    }
                    activeChar.sendPacket(sm);
                }
            }
            
            // Unequip the shield
            L2ItemInstance sld = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
            if (sld != null)
            {
            	if (sld.isWear())
            		return false;
            	
                L2ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(sld.getItem().getBodyPart());
                InventoryUpdate iu = new InventoryUpdate();
                for (int i = 0; i < unequiped.length; i++)
                    iu.addModifiedItem(unequiped[i]);
                activeChar.sendPacket(iu);

                activeChar.abortAttack();
                activeChar.refreshExpertisePenalty();
                activeChar.broadcastUserInfo();

                // this can be 0 if the user pressed the right mousebutton twice very fast
                if (unequiped.length > 0)
                {
                    SystemMessage sm = null;
                    if (unequiped[0].getEnchantLevel() > 0)
                    {
                        sm = new SystemMessage(SystemMessage.EQUIPMENT_S1_S2_REMOVED);
                        sm.addNumber(unequiped[0].getEnchantLevel());
                        sm.addItemName(unequiped[0].getItemId());
                    }
                    else
                    {
                        sm = new SystemMessage(SystemMessage.S1_DISARMED);
                        sm.addItemName(unequiped[0].getItemId());
                    }
                    activeChar.sendPacket(sm);
                }
            }
        	
        	if (activeChar.isFlying())activeChar.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
			Ride dismount = new Ride(activeChar.getObjectId(), Ride.ACTION_DISMOUNT, 0);
			Broadcast.toSelfAndKnownPlayersInRadius(activeChar, dismount, 810000/*900*/);
            activeChar.setMountType(0);
            activeChar.setMountObjectID(0);
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
     */
    public int[] getUserCommandList()
    {
        return COMMAND_IDS;
    }
}
