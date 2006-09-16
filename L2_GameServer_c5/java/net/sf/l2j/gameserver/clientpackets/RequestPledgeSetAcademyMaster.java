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
 * Format: (ch) dSS
 * @author  -Wooden-
 * 
 */
public class RequestPledgeSetAcademyMaster extends ClientBasePacket
{
    private static final String _C__D0_19_REQUESTSETPLEADGEACADEMYMASTER = "[C] D0:19 RequestPledgeSetAcademyMaster";
    private String _unk2;
    private int _unk1;
    private String _unk3;

    /**
     * @param buf
     * @param client
     */
    public RequestPledgeSetAcademyMaster(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _unk1 = readD();
        _unk2 = readS();
        _unk3 = readS();
    }

    /**
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
     */
    @Override
    void runImpl()
    {
        // TODO
        System.out.println("C5:RequestAskJoinPartyRoom: d: " + _unk1);
        System.out.println("C5:RequestAskJoinPartyRoom: S: " + _unk2);
        System.out.println("C5:RequestAskJoinPartyRoom: S: " + _unk3);
    }

    /**
     * @see net.sf.l2j.gameserver.BasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__D0_19_REQUESTSETPLEADGEACADEMYMASTER;
    }

}