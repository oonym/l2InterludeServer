
package net.sf.l2j.gameserver.npcserverpackets;

/**
 * 
 *
 * @author  Luno
 */
public class PacketsQueue
{
	private GsBasePacket _first = null;
	private GsBasePacket _last  = null;
	private int _size = 0;
	
	public PacketsQueue()
	{
		
	}
	public boolean isEmpty()
	{
		return _first == null;
	}
	public void add(GsBasePacket packet)
	{
		if(_first == null)
		{
			_first = packet;
			_last  = packet;
		}
		else
		{
			_last._next = packet;
			_last = packet;
		}
		_size++;	
	}
	/**
	 * Returns first packet in queue
	 * @return packet that were put in queue as first
	 * @throws EmptyQueueException
	 */
	public GsBasePacket get() //throws EmptyQueueException
	{
		if(isEmpty())
			//throw new EmptyQueueException("Empty packet queue");
			return null;
		
		GsBasePacket res = _first;
		_first = res._next;
		
		_size--;
		
		return res;
	}
}