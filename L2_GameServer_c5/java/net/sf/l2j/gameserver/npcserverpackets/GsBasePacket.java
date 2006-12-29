
package net.sf.l2j.gameserver.npcserverpackets;

import java.nio.ByteBuffer;

import net.sf.l2j.gameserver.NpcServerThread;

/**
 * Represents packets that are being sent to npc server
 *
 * @author  Luno
 */
public abstract class GsBasePacket
{
	private static int PACKET_MAX_SIZE = 8 * 1024;
	
	public GsBasePacket    _next;
	protected ByteBuffer    _buf;
	protected byte[]       _data;
	protected int        _length;
	  
	protected GsBasePacket() 
	{ 
		_buf    = ByteBuffer.allocate(PACKET_MAX_SIZE);
		_length = 0;
		_next   = null;
		
	} 
	public int getLength()
	{
		return _length;
	}
	public  byte[] getData()
	{
		return _data;
	}
	
	protected final void writeByte(byte value)
	{
		_buf.put(value);
		_length += 1;
	}

	public final void send()
	{
		_data = new byte[_length];
		for(int i = 0; i < _length; i++)
			_data[i] = _buf.get(i);
		_buf = null;
		NpcServerThread.getInstance().addPacket(this);
	}
 
	protected final void writeShort(short value)
	{
		_buf.putShort(value);
		_length += 2;
	}
	protected final void writeInt(int value)
	{
		_buf.putInt(value);
		_length += 4;
	}
	protected final void writeLong(long value)
	{
		_buf.putLong(value);
		_length += 8;
	}
	protected final void writeFloat(float value)
	{
		_buf.putFloat(value);
		_length += 4;
	}
	protected final void writeDouble(double value)
	{
		_buf.putDouble(value);
		_length += 8;
	}
	protected final void writeString(String text)
	{
		if (text == null)
		{
			_buf.putChar('\000');
			_length += 2;
		}
		else
		{
			final int len = text.length();
			for (int i=0; i < len; i++)
			{
				_buf.putChar(text.charAt(i));
				_length += 2;
			}
			_buf.putChar('\000');
			_length += 2;
		}
	}
}