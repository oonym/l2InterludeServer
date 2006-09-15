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

import net.sf.l2j.gameserver.Connection;
import net.sf.l2j.gameserver.clientpackets.ClientBasePacket;

/**
 * @author mkizub
 */
public class WrappedMessage extends ServerBasePacket
{
    final byte[] data;

    public WrappedMessage(byte[] pData, Connection con)
    {
        super(con);
        this.data = pData;
    }

    public int size()
    {
        return data.length + 2;
    }

    public byte[] getData()
    {
        return data;
    }

    public ClientBasePacket getClientMsg()
    {
        return null;
    }

    final void runImpl()
    {
        // no long-running tasks
    }

    final void writeImpl()
    {
        writeB(data);
    }

    public String getType()
    {
        return null;
    }
}
