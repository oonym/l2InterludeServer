/* * This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 *
 *
 */
public class Time implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 77 };
    /* (non-Javadoc)
     *
     */
    public boolean useUserCommand(int id, L2PcInstance activeChar)
    {
        if (COMMAND_IDS[0] != id) return false;

		int t = GameTimeController.getInstance().getGameTime();
		String h = "" + (t/60)%24;
		String m;
		if (t%60 < 10)
			m = "0" + t%60;
		else
			m = "" + t%60;



		SystemMessage sm;
		if (GameTimeController.getInstance().isNowNight()) {
			sm = new SystemMessage(SystemMessageId.TIME_S1_S2_IN_THE_NIGHT);
        	sm.addString(h);
        	sm.addString(m);
		}
		else {
			sm = new SystemMessage(SystemMessageId.TIME_S1_S2_IN_THE_DAY);
	       	sm.addString(h);
        	sm.addString(m);
		}
        activeChar.sendPacket(sm);
        return true;
    }
    public int[] getUserCommandList()
    {
        return COMMAND_IDS;
    }
}
