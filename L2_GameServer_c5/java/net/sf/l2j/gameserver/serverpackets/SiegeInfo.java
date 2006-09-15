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

import java.util.Calendar;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;

/**
 * Shows the Siege Info<BR>
 * <BR>
 * packet type id 0xc9<BR>
 * format: cdddSSdSdd<BR>
 * <BR>
 * c = c9<BR>
 * d = CastleID<BR>
 * d = Show Owner Controls (0x00 default || >=0x02(mask?) owner)<BR>
 * d = Owner ClanID<BR>
 * S = Owner ClanName<BR>
 * S = Owner Clan LeaderName<BR>
 * d = Owner AllyID<BR>
 * S = Owner AllyName<BR>
 * d = current time (seconds)<BR>
 * d = Siege time (seconds) (0 for selectable)<BR>
 * d = (UNKNOW) Siege Time Select Related
 * @author KenM
 */
public class SiegeInfo extends ServerBasePacket
{
    private static final String _S__C9_SIEGEINFO = "[S] c9 SiegeInfo";
    private static Logger _log = Logger.getLogger(SiegeInfo.class.getName());
    private Castle _Castle;
    
    public SiegeInfo(Castle castle)
    {
        _Castle = castle;
    }

    final void runImpl()
    {
        // no long-running tasks
    }

    final void writeImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        if (activeChar == null) return;

        writeC(0xc9);
        writeD(_Castle.getCastleId());
        writeD(((_Castle.getOwnerId() == activeChar.getClanId()) && (activeChar.isClanLeader())) ? 0x07 : 0x00);        
        writeD(_Castle.getOwnerId());
        if (_Castle.getOwnerId() > 0)
        {
            L2Clan owner = ClanTable.getInstance().getClan(_Castle.getOwnerId());
            if (owner != null)
            {
                writeS(owner.getName());        // Clan Name
                writeS(owner.getLeaderName());  // Clan Leader Name
                writeD(owner.getAllyId());      // Ally ID
                writeS(owner.getAllyName());    // Ally Name
            }
            else
                _log.warning("Null owner for castle: " + _Castle.getName());
        }
        else
        {
            writeS("NPC");  // Clan Name
            writeS("");     // Clan Leader Name
            writeD(0);      // Ally ID
            writeS("");     // Ally Name
        }
        
        writeD((int) (Calendar.getInstance().getTimeInMillis()/1000));
        writeD((int) (_Castle.getSiege().getSiegeDate().getTimeInMillis()/1000));
        writeD(0x00); //number of choices?
    }

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
     */
    public String getType()
    {
        return _S__C9_SIEGEINFO;
    }

}
