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
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * <p>Format: (c) Sd
 * <ul>
 * <li>S: content</li>
 * <li>d: type</li>
 * </ul></p>
 * @author -Wooden-, TempyIncursion
 *
 */
public class RequestPetition extends ClientBasePacket
{
	private static final String _C__7F_RequestPetition = "[C] 7F RequestPetition";
	//private static Logger _log = Logger.getLogger(RequestPetition.class.getName());

	private final String _content;
	private final int _type;       // 1 = on : 0 = off;

	public RequestPetition(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_content = readS();
		_type    = readD();
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (!GmListTable.getInstance().isGmOnline())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.NO_GM_PROVIDING_SERVICE_NOW));
			return;
		}

		if (!PetitionManager.getInstance().isPetitioningAllowed())
		{
			activeChar.sendPacket(new SystemMessage(381));
			return;
		}

		if (PetitionManager.getInstance().isPlayerPetitionPending(activeChar))
		{
			activeChar.sendPacket(new SystemMessage(390));
			return;
		}

		if (PetitionManager.getInstance().getPendingPetitionCount() == Config.MAX_PETITIONS_PENDING)
		{
			activeChar.sendPacket(new SystemMessage(602));
			return;
		}

		int totalPetitions = PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar) + 1;

		if (totalPetitions > Config.MAX_PETITIONS_PER_PLAYER)
		{
			SystemMessage sm = new SystemMessage(733);
			sm.addNumber(totalPetitions);
			activeChar.sendPacket(sm);
			sm = null;
			return;
		}

		if (_content.length() > 255)
		{
			activeChar.sendPacket(new SystemMessage(971));
			return;
		}

		int petitionId = PetitionManager.getInstance().submitPetition(activeChar, _content, _type);

		SystemMessage sm = new SystemMessage(389);
		sm.addNumber(petitionId);
		activeChar.sendPacket(sm);

		sm = new SystemMessage(730);
		sm.addNumber(totalPetitions);
		sm.addNumber(Config.MAX_PETITIONS_PER_PLAYER - totalPetitions);
		activeChar.sendPacket(sm);

		sm = new SystemMessage(601);
		sm.addNumber(PetitionManager.getInstance().getPendingPetitionCount());
		activeChar.sendPacket(sm);
		sm = null;
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	 public String getType()
	{
		return _C__7F_RequestPetition;
	}
}