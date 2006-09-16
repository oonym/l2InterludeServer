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
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.PledgeReceiveWarList;

/**
 * Format: (ch) dd
 * @author  -Wooden-
 * 
 */
public class RequestPledgeWarList extends ClientBasePacket
{
    private static final String _C__D0_1E_REQUESTPLEDGEWARLIST = "[C] D0:1E RequestPledgeWarList";
    private int _unk1;
    private int _tab;
    /**
     * @param buf
     * @param client
     */
    public RequestPledgeWarList(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _unk1 = readD();
        _tab = readD();
    }

    /**
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
     */
    @Override
    void runImpl()
    {
        System.out.println("C5: RequestPledgeWarList d:"+_unk1);
        System.out.println("C5: RequestPledgeWarList d:"+_tab);
        L2PcInstance activeChar = getClient().getActiveChar();
        if(activeChar == null)
        	return;
        //do we need powers to do that??
        activeChar.sendPacket(new PledgeReceiveWarList(activeChar.getClan()));
    }

    /**
     * @see net.sf.l2j.gameserver.BasePacket#getType()
     */
    @Override
    public String getType()
    {
        return _C__D0_1E_REQUESTPLEDGEWARLIST;
    }
    
}