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
    private final int _clanOrPlayerId;
    private final int _action;
    private final int _privs;
    
    public RequestPledgePower(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _clanOrPlayerId = readD();
        _action = readD();
        if(_action == 3)
        {
            _privs = readD();
        }
        else _privs = 0;
    }

    void runImpl()
    {
        L2PcInstance player = getClient().getActiveChar();
        if(_action == 3)
        {
        	if(player.getClan() != null && player.isClanLeader())
        	{
        		L2PcInstance clanPlayer = (L2PcInstance)L2World.getInstance().findObject(_clanOrPlayerId);
        		if(clanPlayer != null)
        		{
        			clanPlayer.setClanPrivileges(_privs);
        		}
        	}
        }
        else
        {
            ManagePledgePower mpp = new ManagePledgePower(_clanOrPlayerId, _action, player);    
            if(player != null)
            {
                player.sendPacket(mpp);
            }
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