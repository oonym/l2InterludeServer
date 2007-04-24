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

import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.serverpackets.SiegeDefenderList;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestConfirmSiegeWaitingList extends L2GameClientPacket
{
    private static final String _C__a5_RequestConfirmSiegeWaitingList = "[C] a5 RequestConfirmSiegeWaitingList";
    //private static Logger _log = Logger.getLogger(RequestConfirmSiegeWaitingList.class.getName());

    private int _Approved;
    private int _CastleId;
    private int _ClanId;
    
    protected void readImpl()
    {
        _CastleId = readD();
        _ClanId = readD();
        _Approved = readD();
    }

    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if(activeChar == null) return;
        
        // Check if the player has a clan
        if (activeChar.getClan() == null) return;
        
        Castle castle = CastleManager.getInstance().getCastle(_CastleId);
        if (castle == null) return;
        
        // Check if leader of the clan who owns the castle?
        if ((castle.getOwnerId() != activeChar.getClanId()) || (!activeChar.isClanLeader())) return;
        
        L2Clan clan = ClanTable.getInstance().getClan(_ClanId);
        if (clan == null) return;
        
        if (!castle.getSiege().getIsRegistrationOver())
        {
            if (_Approved == 1)
            {
                if (castle.getSiege().checkIsDefenderWaiting(clan))
                    castle.getSiege().approveSiegeDefenderClan(_ClanId);
                else
                    return;
            }
            else
            {
                if ((castle.getSiege().checkIsDefenderWaiting(clan)) || (castle.getSiege().checkIsDefender(clan)))
                    castle.getSiege().removeSiegeClan(_ClanId);
            }
        }
        
        //Update the defender list
        activeChar.sendPacket(new SiegeDefenderList(castle));

    }
    
    
    public String getType()
    {
        return _C__a5_RequestConfirmSiegeWaitingList;
    }
}
