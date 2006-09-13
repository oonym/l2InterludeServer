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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.BasePacket;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.Connection;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.7 $ $Date: 2005/03/27 15:29:39 $
 */
public abstract class ServerBasePacket extends BasePacket
{
	private static Logger _log = Logger.getLogger(ServerBasePacket.class.getName());
	private long _packetLifeTime = Config.PACKET_LIFETIME;
	private long _packetTime = 0;
	
	public final void setLifeTime(long time){
	    _packetLifeTime = time;
	}
	public final long getLifeTime(){
	    return _packetLifeTime;
	}
	public final void setTime(long time){
	    _packetTime = time;
	}
	public final long getTime(){
	    return _packetTime;
	}
	
	protected ServerBasePacket()
	{
		if (Config.DEBUG) _log.fine(getType());
	}
	
	protected ServerBasePacket(Connection con)
	{
		super(con);
		if (Config.DEBUG) _log.fine(getType());
	}
	
	protected final void writeD(int value)
	{
		_buf.putInt(value);
	}

	protected final void writeH(int value)
	{
		_buf.putShort((short)value);
	}

	protected final void writeC(int value)
	{
		_buf.put((byte)value);
	}

	protected final void writeF(double value)
	{
		_buf.putDouble(value);
	}
	
	protected final void writeS(String text)
	{
		if (text == null)
		{
			_buf.putChar('\000');
		}
		else
		{
			final int len = text.length();
			for (int i=0; i < len; i++)
				_buf.putChar(text.charAt(i));
			_buf.putChar('\000');
		}
	}

	protected final void writeB(byte[] data)
	{
		_buf.put(data);
	}

	public final BasePacket setConnection(Connection con)
	{
		BasePacket bp = super.setConnection(con);
		if (bp == this)
        {
//			if (Config.DEVELOPER) System.out.println(getType());
			runImpl();
        }
		return bp;
	}
	
	public final boolean write(ByteBuffer buf)
	{
		if (Config.ASSERT) assert buf.position() == 0;
		if (Config.ASSERT) assert buf.order() == ByteOrder.LITTLE_ENDIAN;
		_buf = buf;
		try {
			_buf.putShort((short)0);
			try
			{
				if (Config.DEBUG) {
					ClientThread client = getClient();
					_log.finer(getType()+" >>> "+(client==null?"null":client.getLoginName()));
				}
				writeImpl();
			} catch (Exception e) {
				_log.log(Level.SEVERE, "", e);
				return false;
			}
			_buf.flip();
			_buf.putShort((short)_buf.limit());
			ByteBuffer b = _buf.slice();
			_buf.position(0);
			if (!(this instanceof KeyPacket))
				getConnection().encript(b);
		} finally {
			_buf = null;
		}
		return true;
	}
	
	abstract void runImpl();
	abstract void writeImpl();

}
