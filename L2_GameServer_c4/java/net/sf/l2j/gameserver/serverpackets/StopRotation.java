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

import net.sf.l2j.gameserver.model.L2Character;

/**
 * This class ...
 * 
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/27 15:29:57 $
 */
public class StopRotation extends ServerBasePacket
{
	private static final String _S__78_STOPROTATION = "[S] 63 StopRotation";
	private int _charId;
	private int _degree;
	
	public StopRotation(L2Character player, int degree)
	{
		_charId = player.getObjectId();
		_degree = degree;
	}
	
	final void runImpl()
	{
		// no long-running tasks
	}
	
	final void writeImpl()
	{
		writeC(0x63);
		writeD(_charId);
		writeD(_degree);
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	public String getType()
	{
		return _S__78_STOPROTATION;
	}
}
