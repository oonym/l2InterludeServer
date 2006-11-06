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

import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class AllyDismiss extends ClientBasePacket
{
    private static final String _C__85_ALLYDISMISS = "[C] 85 AllyDismiss";
    //private static Logger _log = Logger.getLogger(AllyDismiss.class.getName());
    
    String _clanName;
    
    public AllyDismiss(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _clanName = readS();
    }
    
    void runImpl()
    {
        SystemMessage msg;
        L2PcInstance player = getClient().getActiveChar();
        
        if(player == null)
            return;
        
        L2Clan leaderClan = player.getClan();
        L2Clan clan;
        if(leaderClan == null)
            return;
        
        if(leaderClan.getAllyId() == 0)
        {
            //no current alliance
            msg = new SystemMessage(465);
            player.sendPacket(msg);
            msg = null;
            return;
        }
        
        if(!player.isClanLeader() || leaderClan.getClanId() != leaderClan.getAllyId())
        {
            msg = new SystemMessage(SystemMessage.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
            player.sendPacket(msg);
            msg = null;
            return;
        }
        
        if (_clanName == null)
            return;
        
        clan = ClanTable.getInstance().getClanByName(_clanName);
        
        if(clan != null)
        {
            if (clan.getClanId() == leaderClan.getClanId())
                return;
            if (clan.getAllyId() != leaderClan.getAllyId())
                return;
            
            clan.setAllyId(0);
            clan.setAllyName(null);
            clan.updateClanInDB();
            
            player.sendMessage(clan.getName() + " has been dismissed from " + leaderClan.getAllyName());
        }        
    }
    
    public String getType()
    {
        return _C__85_ALLYDISMISS;
    }
}