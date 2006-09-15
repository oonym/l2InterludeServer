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
package net.sf.l2j.loginserver.clientpackets;

/**
 * Format: ddc
 * d: fist part of session id
 * d: second part of session id
 * c: ?
 * 
 * (session ID is sent in LoginOk packet and fixed to 0x55555555 0x44444444)
 */
public class RequestServerList extends ClientBasePacket
{
	private int _data1;
	private int _data2;
	private int _data3;
	
	/**
	 * @return
	 */
	public int getData1()
	{
		return _data1;
	}

	/**
	 * @return
	 */
	public int getData2()
	{
		return _data2;
	}

	/**
	 * @return
	 */
	public int getData3()
	{
		return _data3;
	}
	
	public RequestServerList(byte[] rawPacket)
	{
		super(rawPacket);
		_data1  = readD();
		_data2  = readD();
		_data3 =  readC();
	}
}
