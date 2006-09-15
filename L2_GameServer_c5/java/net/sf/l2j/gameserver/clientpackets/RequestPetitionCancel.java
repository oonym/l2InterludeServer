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

import java.nio.ByteBuffer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * <p>Format: (c) d
 * <ul>
 * <li>d: Unknown</li>
 * </ul></p>
 * 
 * @author -Wooden-, TempyIncursion
 */
public class RequestPetitionCancel extends ClientBasePacket
{
	private static final String _C__80_REQUEST_PETITIONCANCEL = "[C] 80 RequestPetitionCancel";
	
	//private int _unknown;
	
	public RequestPetitionCancel(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		//_unknown = readD(); This is pretty much a trigger packet.
	}
	
	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null)
            return;
		
		if (PetitionManager.getInstance().isPlayerInConsultation(activeChar))
		{
			if (activeChar.isGM())
				PetitionManager.getInstance().endActivePetition(activeChar);
			else 
				activeChar.sendPacket(new SystemMessage(407));
		}
		else
		{
			if (PetitionManager.getInstance().isPlayerPetitionPending(activeChar))
			{
				if (PetitionManager.getInstance().cancelActivePetition(activeChar))
				{
					int numRemaining = Config.MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar);
					
					SystemMessage sm = new SystemMessage(736);
					sm.addString(String.valueOf(numRemaining));
					activeChar.sendPacket(sm);
                    
                    // Notify all GMs that the player's pending petition has been cancelled.
                    String msgContent = activeChar.getName() + " has canceled a pending petition."; 
                    GmListTable.broadcastToGMs(new CreatureSay(activeChar.getObjectId(), 17, "Petition System", msgContent));
				}
				else 
				{
					activeChar.sendPacket(new SystemMessage(393));
				}
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(738));
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	public String getType()
	{
		return _C__80_REQUEST_PETITIONCANCEL;
	}
	
}