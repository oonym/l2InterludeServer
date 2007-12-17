/*
 * $HeadURL: $
 *
 * $Author: $
 * $Date: $
 * $Revision: $
 *
 *
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
package net.sf.l2j.gameserver;

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.clientpackets.L2GameClientPacket;
import net.sf.l2j.gameserver.network.L2GameClient;

/**
 * This interface can be implemented by custom extensions to l2j to get packets
 * before the normal processing of PacketHandler
 *
 * @version $Revision: $ $Date: $
 * @author  galun
 */
public interface CustomPacketHandlerInterface
{

    /**
     * interface for a custom packethandler to ckeck received packets
     * PacketHandler will take care of the packet if this function returns null.
     * @param data the packet
     * @param client the ClientThread
     * @return a ClientBasePacket if the packet has been processed, null otherwise
     */
    public L2GameClientPacket handlePacket(ByteBuffer data, L2GameClient client);
}
