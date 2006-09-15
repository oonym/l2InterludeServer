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
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.serverpackets.SiegeDefenderList;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestSiegeDefenderList extends ClientBasePacket{
    
    private static final String _C__a3_RequestSiegeDefenderList = "[C] a3 RequestSiegeDefenderList";
    //private static Logger _log = Logger.getLogger(RequestJoinParty.class.getName());

    private final int _CastleId;
    
    public RequestSiegeDefenderList(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _CastleId = readD();
    }

    void runImpl()
    {
        Castle castle = CastleManager.getInstance().getCastle(_CastleId);
        if (castle == null) return;
        SiegeDefenderList sdl = new SiegeDefenderList(castle);
        sendPacket(sdl);
    }
    
    
    public String getType()
    {
        return _C__a3_RequestSiegeDefenderList;
    }
}
