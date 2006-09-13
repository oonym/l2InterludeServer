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
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.GMViewCharacterInfo;
import net.sf.l2j.gameserver.serverpackets.GMViewItemList;
import net.sf.l2j.gameserver.serverpackets.GMViewPledgeInfo;
import net.sf.l2j.gameserver.serverpackets.GMViewQuestList;
import net.sf.l2j.gameserver.serverpackets.GMViewSkillInfo;
import net.sf.l2j.gameserver.serverpackets.GMViewWarehouseWithdrawList;

/**
 * This class ...
 * 
 * @version $Revision: 1.1.2.2.2.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestGMCommand extends ClientBasePacket
{
	private static final String _C__6E_REQUESTGMCOMMAND = "[C] 6e RequestGMCommand";
	static Logger _log = Logger.getLogger(RequestGMCommand.class.getName());
			
	private final String _targetName;
	private final int _command;
    //private final int _unknown;
	/**
	 * packet type id 0x00
	 * format:	cd
	 *  
	 * @param rawPacket
	 */
	public RequestGMCommand(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_targetName = readS();
		_command    = readD();
		//_unknown  = readD();
	}

	void runImpl()
	{
		L2PcInstance player = L2World.getInstance().getPlayer(_targetName);

        if (player == null)
			return;
		
		switch(_command)
		{
			case 1: // player status
			{
				sendPacket(new GMViewCharacterInfo(player));
				break;
			}
			case 2: // player clan
			{
				if (player.getClan() != null)
					sendPacket(new GMViewPledgeInfo(player.getClan(),player));

                break;
			}
			case 3: // player skills
			{
				sendPacket(new GMViewSkillInfo(player));
				break;
			}
			case 4: // player quests
			{
                sendPacket(new GMViewQuestList(player));
			    break;
			}
			case 5: // player inventory
			{
				sendPacket(new GMViewItemList(player));
				break;
			}
			case 6: // player warehouse
			{
			    // gm warehouse view to be implemented
				sendPacket(new GMViewWarehouseWithdrawList(player));
			    break;
			}
				
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__6E_REQUESTGMCOMMAND;
	}
}
