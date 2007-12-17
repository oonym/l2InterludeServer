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
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.Ride;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

/**
 * Support for /mount command.
 * @author Tempy
 */
public class Mount implements IUserCommandHandler
{
    private static final int[] COMMAND_IDS = { 61 };

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public synchronized boolean useUserCommand(int id, L2PcInstance activeChar)
    {
        if (id != COMMAND_IDS[0]) return false;

        L2Summon pet = activeChar.getPet();

        if (pet != null && pet.isMountable() && !activeChar.isMounted())
        {
            if (activeChar.isDead())
            {
                // A strider cannot be ridden when player is dead.
                SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
                activeChar.sendPacket(msg);
            }
            else if (pet.isDead())
            {
                // A dead strider cannot be ridden.
                SystemMessage msg = new SystemMessage(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
                activeChar.sendPacket(msg);
            }
            else if (pet.isInCombat())
            {
                // A strider in battle cannot be ridden.
                SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
                activeChar.sendPacket(msg);
            }
            else if (activeChar.isInCombat())
            {
                // A pet cannot be ridden while player is in battle.
                SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
                activeChar.sendPacket(msg);
            }
            else if (activeChar.isSitting() || activeChar.isMoving())
            {
                // A strider can be ridden only when player is standing.
                SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
                activeChar.sendPacket(msg);
            }
            else if (!pet.isDead() && !activeChar.isMounted())
            {
            	if(!activeChar.disarmWeapons()) return false;
                Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, pet.getTemplate().npcId);
                Broadcast.toSelfAndKnownPlayersInRadius(activeChar, mount, 810000/*900*/);
                activeChar.setMountType(mount.getMountType());
                activeChar.setMountObjectID(pet.getControlItemId());
                pet.unSummon(activeChar);
            }
        }
        else if (activeChar.isRentedPet())
        {
        	activeChar.stopRentPet();
        }
        else if (activeChar.isMounted())
        {
        	// Dismount
        	if(activeChar.setMountType(0))
        	{
	        	if (activeChar.isFlying())activeChar.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
				Ride dismount = new Ride(activeChar.getObjectId(), Ride.ACTION_DISMOUNT, 0);
				Broadcast.toSelfAndKnownPlayers(activeChar, dismount);
	            activeChar.setMountObjectID(0);
        	}
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
