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
package net.sf.l2j.gameserver;

import java.util.Iterator;

import net.sf.l2j.Config;

/**
 * @author mkizub
 */
final class BasePacketQueue {

	BasePacket _first;
	BasePacket _last;
    
    private int size;
	
	boolean isEmpty() {
		return _first == null;
	}
	

	synchronized void put(BasePacket pkt) {
		if (Config.ASSERT) assert pkt._queue == null;
		if (Config.ASSERT) assert pkt._prev == null;
		if (Config.ASSERT) assert pkt._next == null;
		pkt._queue = this;
		if (_first == null) {
			_first = _last = pkt;
		} else {
			pkt._prev = _last;
			_last._next = pkt;
			_last = pkt;
		}
        size++;
	}

	synchronized BasePacket get() {
		if (_first == null)
			return null;
		BasePacket pkt = _first;
		_first = pkt._next;
		pkt._queue = this;
		if (_first == null)
			_last = null;
		else
			_first._prev = null;
		pkt._queue = null;
		pkt._prev = null;
		pkt._next = null;
        size--;
		return pkt;
	}
	
	synchronized void remove(BasePacket pkt) {
		if (pkt == null || pkt._queue == null)
			return;
		if (Config.ASSERT) assert pkt._queue == this;
		if (pkt._prev != null)
			pkt._prev._next = pkt._next;
		if (pkt._next != null)
			pkt._next._prev = pkt._prev;
		if (_first == pkt)
			_first = pkt._next;
		if (_last == pkt)
			_last = pkt._prev;
		pkt._queue = null;
		pkt._prev = null;
		pkt._next = null;
        size--;
	}
    
    public int size()
    {
        return size;
    }
	
	Iterator<BasePacket> iterator() {
		return new Iter();
	}
	
	class Iter implements Iterator<BasePacket>
	{
		private BasePacket _lastRet;
		private BasePacket _next;

		Iter() {
			_next = _first;
		}
		
		public void remove() {
			BasePacketQueue.this.remove(_lastRet);
		}

		public boolean hasNext() {
			return _next != null;
		}

		public BasePacket next() {
			if (_next == null)
				return null;
			_lastRet = _next;
			_next = _next._next;
			return _lastRet;
		}
	}
}
