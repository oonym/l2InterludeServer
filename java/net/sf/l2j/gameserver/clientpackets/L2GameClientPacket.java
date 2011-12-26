/* This program is free software; you can redistribute it and/or modify
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

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.L2GameServerPacket;

import org.mmocore.network.ReceivablePacket;

/**
 * Packets received by the game server from clients
 * @author  KenM
 */
public abstract class L2GameClientPacket extends ReceivablePacket<L2GameClient>
{
	private static final Logger _log = Logger.getLogger(L2GameClientPacket.class.getName());

	@Override
	protected boolean read()
	{
		//System.out.println(this.getType());
		try
		{
			readImpl();
			return true;
		}
		catch (Throwable t)
		{
			_log.severe("Client: "+getClient().toString()+" - Failed reading: "+getType()+" - L2J Server Version: "+Config.SERVER_VERSION+" - DP Revision: "+Config.DATAPACK_VERSION);
			t.printStackTrace();
		}
		return false;
	}

	protected abstract void readImpl();

	@Override
	public void run()
	{
		try
		{
            // flood protection
			if (GameTimeController.getGameTicks() - getClient().packetsSentStartTick > 10)
			{
				getClient().packetsSentStartTick = GameTimeController.getGameTicks();
				getClient().packetsSentInSec = 0;
			}
			else
			{
				getClient().packetsSentInSec++;
				if (getClient().packetsSentInSec > 12) 
				{
					if (getClient().packetsSentInSec < 100)
						sendPacket(new ActionFailed()); 
					return;
				}
			}
			
			runImpl();
            if (this instanceof MoveBackwardToLocation 
            	|| this instanceof AttackRequest 
            	|| this instanceof RequestMagicSkillUse)
            	// could include pickup and talk too, but less is better
            {
            	// Removes onspawn protection - player has faster computer than
            	// average
            	if (getClient().getActiveChar() != null)
            		getClient().getActiveChar().onActionRequest();
            }
		}
		catch (Throwable t)
		{
			_log.severe("Client: "+getClient().toString()+" - Failed running: "+getType()+" - L2J Server Version: "+Config.SERVER_VERSION+" - DP Revision: "+Config.DATAPACK_VERSION);
			t.printStackTrace();
		}
	}
	
	protected abstract void runImpl();
	
	protected final void sendPacket(L2GameServerPacket gsp)
	{
		getClient().sendPacket(gsp);
	}
	
	/**
	 * @return A String with this packet name for debuging purposes
	 */
	public abstract String getType();
}
