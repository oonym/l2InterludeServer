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
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestJoinSiege extends ClientBasePacket{
    
    private static final String _C__a4_RequestJoinSiege = "[C] a4 RequestJoinSiege";
    //private static Logger _log = Logger.getLogger(RequestJoinSiege.class.getName());

    private final int _CastleId;
    private final int _IsAttacker;
    private final int _IsJoining;
    
    public RequestJoinSiege(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _CastleId = readD();
        _IsAttacker = readD();
        _IsJoining = readD();
    }

    void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if(activeChar == null) return;
        if (!activeChar.isClanLeader()) return;
        
        Castle castle = CastleManager.getInstance().getCastle(_CastleId);
        if (castle == null) return;

        if (_IsJoining == 1)
        {
        	if (System.currentTimeMillis() < activeChar.getClan().getDissolvingExpiryTime())
        	{
        		activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS));
        		return;
        	}
            if (_IsAttacker == 1)
                castle.getSiege().registerAttacker(activeChar);
            else
                castle.getSiege().registerDefender(activeChar);
        }
        else
            castle.getSiege().removeSiegeClan(activeChar);

        castle.getSiege().listRegisterClan(activeChar);
    }
    
    
    public String getType()
    {
        return _C__a4_RequestJoinSiege;
    }
}
