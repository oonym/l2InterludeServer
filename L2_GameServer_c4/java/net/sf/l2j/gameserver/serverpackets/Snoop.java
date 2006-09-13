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

public class Snoop extends ServerBasePacket
{
	private static final String _S__D5_SNOOP = "[S] D5 Snoop";
	private int _convoID;
	private String _name;
	private int _type;
	private String _speaker;
	private String _msg;
	
	public Snoop(int id, String name, int type, String speaker, String msg)
	{
		_convoID = id;
		_name = name;
		_type = type;
		_speaker = speaker;
		_msg = msg;
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#runImpl()
	 */
	@Override
	void runImpl()
	{
		// no long running
		
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	void writeImpl()
	{
		writeC(0xd5);
		
		writeD(_convoID);
		writeS(_name);
		writeD(0x00); //??
		writeD(_type);
		writeS(_speaker);
		writeS(_msg);
		
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__D5_SNOOP;
	}
	
}