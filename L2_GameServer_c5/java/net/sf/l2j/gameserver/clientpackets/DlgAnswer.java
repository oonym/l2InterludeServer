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

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * @author Dezmond_snz
 * Format: cddd
 */
public class DlgAnswer extends ClientBasePacket
{
	private static final String _C__C5_DLGANSWER = "[C] C5 DlgAnswer";
	private static Logger _log = Logger.getLogger(DlgAnswer.class.getName());
	
	private int _messageId;
	private int _answer, _unk;
	
	public DlgAnswer(ByteBuffer buf, ClientThread client)
	{
		super(buf,client);
		_messageId = readD();
		_answer = readD();
		_unk = readD();
	}

	public void runImpl()
	{
		if (Config.DEBUG)
			_log.fine(getType()+": Answer acepted. Message ID "+_messageId+", asnwer "+_answer+", unknown field "+_unk);
		if (_messageId == SystemMessage.RESSURECTION_REQUEST)
			getClient().getActiveChar().ReviveAnswer(_answer);
	}

	public String getType()
	{
		return _C__C5_DLGANSWER;
	}
}
