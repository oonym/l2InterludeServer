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

import java.util.logging.Logger;

/**
 * Format: (ch) dd [ddd]
 * @author -Wooden-
 *
 */
public final class RequestSetSeed extends L2GameClientPacket
{
	private static Logger _log = Logger.getLogger(RequestSetSeed.class.getName());
	private static final String _C__D0_0A_REQUESTSETSEED = "[C] D0:0A RequestSetSeed";
	private int _data1;
	private int[][] _list;
	
	
	protected void readImpl()
	{
		_data1 = readD(); //??
		int size = readD();
		for(int i = 0; i < size; i++)
		{
			_list[i][0] = readD();
			_list[i][1] = readD();
			_list[i][2] = readD();
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected
	void runImpl()
	{
		// TODO Auto-generated method stub
		_log.info("This packet is not well known : RequestSetSeed");
		_log.info("Data received: d:"+_data1+". Elements in list:");
		for(int[] element : _list)
		{
			_log.info("element: d:"+element[0]+" d:"+element[1]+" d:"+element[2]);
		}

	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_0A_REQUESTSETSEED;
	}

}
