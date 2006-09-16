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

import net.sf.l2j.gameserver.ClientThread;

/**
 * Format: (ch) d
 * @author  -Wooden-
 * 
 */
public class AnswerJoinPartyRoom extends ClientBasePacket
{
    private static final String _C__D0_15_ANSWERJOINPARTYROOM = "[C] D0:15 AnswerJoinPartyRoom";
    private int _requesterID; // not tested, just guessed
    /**
     * @param buf
     * @param client
     */
    public AnswerJoinPartyRoom(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _requesterID = readD();
    }

    /**
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
     */
    @Override
    void runImpl()
    {
        // TODO
        System.out.println("C5:AnswerJoinPartyRoom: d: "+_requesterID);
    }

    /**
     * @see net.sf.l2j.gameserver.BasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__D0_15_ANSWERJOINPARTYROOM;
    }
    
}