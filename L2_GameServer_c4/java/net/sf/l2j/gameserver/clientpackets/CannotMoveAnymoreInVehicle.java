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
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.StopMoveInVehicle;
import net.sf.l2j.util.Point3D;

/**
 * @author Maktakien
 *
 */
public class CannotMoveAnymoreInVehicle extends ClientBasePacket
{
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;
	private final int _boatid;
	/**
	 * @param buf
	 * @param client
	 */
	public CannotMoveAnymoreInVehicle(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_boatid = readD();
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
		
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		if(player.isInBoat())
		{
			if(player.getBoat().getObjectId() == _boatid)
			{
				player.setInBoatPosition(new Point3D(_x,_y,_z));
				player.getPosition().setHeading(_heading);
				StopMoveInVehicle msg = new StopMoveInVehicle(player,_boatid);
				player.broadcastPacket(msg);
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		// TODO Auto-generated method stub
		return "[C] 5D CannotMoveAnymoreInVehicle";
	}

}
