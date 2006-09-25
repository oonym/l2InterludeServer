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
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.Inventory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class Ride extends ServerBasePacket
{
    private static final String _S__86_Ride = "[S] 86 Ride";
    public static final int ACTION_MOUNT = 1;
    public static final int ACTION_DISMOUNT = 0;
    private int _id;
    private int _bRide;
    private int _rideType;
    private int _rideClassID;

    /**
     * 0x86 UnknownPackets         dddd 
     * @param _
     */

    public Ride(int id, int action, int rideClassId)
    {
        _id = id; // charobjectID
        _bRide = action; // 1 for mount ; 2 for dismount
        _rideClassID = rideClassId + 1000000; // npcID

        if (rideClassId == 12526 || //wind strider
            rideClassId == 12527 || //star strider
            rideClassId == 12528) //twilight strider
        {
            _rideType = 1; // 1 for Strider ; 2 for wyvern
        }
        else if (rideClassId == 12621) // wyvern
        {
            _rideType = 2; // 1 for Strider ; 2 for wyvern
        }

    }

    public int getMountType()
    {
        return _rideType;
    }

    final void runImpl()
    {
        L2PcInstance cha = getClient().getActiveChar();
        if (cha == null) return;

        // Unequip the weapon
        L2ItemInstance wpn = cha.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
        if (wpn == null) wpn = cha.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND);
        if (wpn != null)
        {
            L2ItemInstance[] unequiped = cha.getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
            InventoryUpdate iu = new InventoryUpdate();
            for (int i = 0; i < unequiped.length; i++)
                iu.addModifiedItem(unequiped[i]);
            cha.sendPacket(iu);

            cha.abortAttack();
            cha.refreshExpertisePenalty();
            cha.broadcastUserInfo();

            // this can be 0 if the user pressed the right mousebutton twice very fast
            if (unequiped.length > 0)
            {
                if (unequiped[0].isWear())
                    return;
                
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
                cha.sendPacket(sm);
            }
        }
        
        // Unequip the shield
        L2ItemInstance sld = cha.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
        if (sld != null)
        {
            L2ItemInstance[] unequiped = cha.getInventory().unEquipItemInBodySlotAndRecord(sld.getItem().getBodyPart());
            InventoryUpdate iu = new InventoryUpdate();
            for (int i = 0; i < unequiped.length; i++)
                iu.addModifiedItem(unequiped[i]);
            cha.sendPacket(iu);

            cha.abortAttack();
            cha.refreshExpertisePenalty();
            cha.broadcastUserInfo();

            // this can be 0 if the user pressed the right mousebutton twice very fast
            if (unequiped.length > 0)
            {
                if (unequiped[0].isWear())
                    return;
                
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
                cha.sendPacket(sm);
            }
        }
    }

    final void writeImpl()
    {

        writeC(0x86);
        writeD(_id);
        writeD(_bRide);
        writeD(_rideType);
        writeD(_rideClassID);
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    public String getType()
    {
        return _S__86_Ride;
    }
}
