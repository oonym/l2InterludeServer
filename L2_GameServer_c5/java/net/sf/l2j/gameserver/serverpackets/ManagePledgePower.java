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
package net.sf.l2j.gameserver.serverpackets;

import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;


public class ManagePledgePower extends ServerBasePacket
{
    private static final String _S__30_MANAGEPLEDGEPOWER = "[S] 30 ManagePledgePower";
    
    private int _action;
    private int _clanId;
    private L2PcInstance _player;
    private int privs;
    
    public ManagePledgePower(int clanId, int action, L2PcInstance player)
    {
        _clanId = clanId;
        _action = action;
        _player = player;
    }   
    
    final void runImpl()
    {
        // no long-running tasks
    }
    
    final void writeImpl()
    {
        if(_action == 1)
			privs = _player.getClanPrivileges();
        else
        {
            if (L2World.getInstance().findObject(_clanId) == null)
                return;
            
			privs = ((L2PcInstance)L2World.getInstance().findObject(_clanId)).getClanPrivileges();
        }
        writeC(0x30);
        writeD(0);
        writeD(0);
        writeD(privs);
   }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    public String getType()
    {
        return _S__30_MANAGEPLEDGEPOWER;
    }

}
