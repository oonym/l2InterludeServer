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
import net.sf.l2j.gameserver.serverpackets.ManagePledgePower;

public class RequestPledgePower extends ClientBasePacket
{
    static Logger _log = Logger.getLogger(ManagePledgePower.class.getName());
    private static final String _C__C0_REQUESTPLEDGEPOWER = "[C] C0 RequestPledgePower";
    private final int _rank;
    private final int _action;
    private final int _privs;
    
    public RequestPledgePower(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _rank = readD();
        _action = readD();
        if(_action == 2)
        {
            _privs = readD();
        }
        else _privs = 0;
    }

    void runImpl()
    {
        L2PcInstance player = getClient().getActiveChar();
        if (player == null) return;
        
        if(_action == 2)
        {
        	if(player.getClan() != null && player.isClanLeader())
        	{
        		// TODO: There are many rights that cannot be bestowed upon adademy
        		// members: join/leave, title, clan war, auction etc... Not checked now.
        		player.getClan().setRankPrivs(_rank, _privs);
        	}
        } else
        {
            ManagePledgePower mpp = new ManagePledgePower(getClient().getActiveChar().getClan(), _action, _rank);    
            player.sendPacket(mpp);
        }
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType()
    {
        return _C__C0_REQUESTPLEDGEPOWER;
    }
}