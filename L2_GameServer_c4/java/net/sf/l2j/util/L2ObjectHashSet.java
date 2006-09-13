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
package net.sf.l2j.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Object;

/**
 * This class is a highly optimized hashtable, where
 * keys are integers. The main goal of this class is to allow
 * concurent read/iterate and write access to this table,
 * plus minimal used memory.
 * 
 * This class uses plain array as the table of values, and
 * keys are used to get position in the table. If the position
 * is already busy, we iterate to the next position, unil we
 * find the needed element or null.
 * 
 * To iterate over the table (read access) we may simply iterate
 * throgh table array.
 * 
 * In case we remove an element from the table, we check - if
 * the next position is null, we reset table's slot to null,
 * otherwice we assign it to a dummy value
 * 
 * 
 * @author mkizub
 *
 * @param <T> type of values stored in this hashtable 
 */
public final class L2ObjectHashSet<T extends L2Object> extends L2ObjectSet<T>
	implements Iterable<T>
{
	
	private final boolean TRACE = false;
	private final boolean DEBUG = false;
	
	private final static int[] primes = {
		5, 7, 11, 17, 23, 29, 37, 47, 59, 71, 89, 107, 131, 163, 197, 239, 293,
		353, 431, 521, 631, 761, 919, 1103, 1327, 1597, 1931, 2333, 2801,
		3371, 4049, 4861, 5839, 7013, 8419, 10103, 12143, 14591, 17519,
		21023, 25229, 30293, 36353, 43627, 52361, 62851, 75431, 90523,
		108631, 130363, 156437, 187751, 225307, 270371, 324449, 389357,
		467237, 560689, 672827, 807403, 968897, 1162687, 1395263, 1674319,
		2009191, 2411033, 2893249, 3471899, 4166287, 4999559, 5999471, 7199369
		};
	
	private T[] table;
	private int[] collisions;
	private int count;
	
	private static int getPrime(int min)
	{
		for (int i=0; i < primes.length; i++)
		{
			if (primes[i] >= min)
				return primes[i];
		}
		throw new OutOfMemoryError();
	}
	
	public L2ObjectHashSet()
	{
		int size = primes[0];
		table = (T[])new L2Object[size];
		collisions = new int[(size+31)>>5];
		if (DEBUG) check();
	}
	
	/* (non-Javadoc)
     * @see net.sf.l2j.util.L2ObjectSet#size()
     */
	public int size()
	{
		return count;
	}
	
	/* (non-Javadoc)
     * @see net.sf.l2j.util.L2ObjectSet#isEmpty()
     */
	public boolean isEmpty()
	{
		return count == 0;
	}
	
	/* (non-Javadoc)
     * @see net.sf.l2j.util.L2ObjectSet#clear()
     */
	public synchronized void clear()
	{
		int size = primes[0];
		table = (T[])new L2Object[size];
		collisions = new int[(size+31)>>5];
		count = 0;
		if (DEBUG) check();
	}
	
	private void check()
	{
		if (DEBUG)
		{
			int cnt = 0;
			assert collisions.length == ((table.length+31)>>5); 
			for (int i=0; i < table.length; i++)
			{
				L2Object obj = table[i]; 
				if (obj != null)
					cnt++;
			}
			assert cnt == count;
		}
	}
	
	/* (non-Javadoc)
     * @see net.sf.l2j.util.L2ObjectSet#put(T)
     */
	public synchronized void put(T obj)
	{
		if (obj == null)
		    return;
		if (contains(obj))
		    return;
		if (count >= table.length/2)
			expand();
		final int hashcode = obj.getObjectId();
		if (Config.ASSERT) assert hashcode > 0;
		int seed = hashcode;
		int incr = 1 + (((seed >> 5) + 1) % (table.length - 1));
		int ntry = 0;
		int slot = -1; // keep last found slot
		do
		{
			int pos = (seed % table.length) & 0x7FFFFFFF;
			if (table[pos] == null)
			{
				if (slot < 0)
					slot = pos;
				if ((collisions[pos>>5] & (1<<(pos&31))) == 0)
				{
					// found an empty slot without previous collisions,
					// but use previously found slot
					table[slot] = obj;
					count++;
					if (TRACE) System.err.println("ht: put obj id="+hashcode+" at slot="+slot);
					if (DEBUG) check();
					return;
				}
			}
			else
			{
				// check if we are adding the same object
				if (table[pos] == obj)
					return;
				// this should never happen
				if (Config.ASSERT) assert obj.getObjectId() != table[pos].getObjectId();
				// if there was no collisions at this slot, and we found a free
				// slot previously - use found slot
				if (slot >= 0 && (collisions[pos>>5] & (1<<(pos&31))) == 0)
				{
					table[slot] = obj;
					count++;
					if (TRACE) System.err.println("ht: put obj id="+hashcode+" at slot="+slot);
					if (DEBUG) check();
					return;
				}
			}
			
			// set collision bit
			collisions[pos>>5] |= 1<<(pos&31);
			// calculate next slot
			seed += incr;
		} while (++ntry < table.length);
		if (DEBUG) check();
		throw new IllegalStateException();
	}
	
	/* (non-Javadoc)
     * @see net.sf.l2j.util.L2ObjectSet#remove(T)
     */
	public synchronized void remove(T obj)
	{
		if (obj == null)
		    return;
		if (!contains(obj))
		    return;
		int hashcode = obj.getObjectId();
		if (Config.ASSERT) assert hashcode > 0;
		int seed = hashcode;
		int incr = 1 + (((seed >> 5) + 1) % (table.length - 1));
		int ntry = 0;
		do
		{
			int pos = (seed % table.length) & 0x7FFFFFFF;
			if (table[pos] == obj)
			{
				// found the object
				table[pos] = null;
				count--;
				if (TRACE) System.err.println("ht: remove obj id="+hashcode+" from slot="+pos);
				if (DEBUG) check();
				return;
			}
			// check for collision (if we previously deleted element)
			if (table[pos] == null && (collisions[pos>>5] & (1<<(pos&31))) == 0) {
				if (DEBUG) check();
				return; //throw new IllegalArgumentException();
			}
			// calculate next slot
			seed += incr;
		} while (++ntry < table.length);
		if (DEBUG) check();
		throw new IllegalStateException();
	}
	
	/* (non-Javadoc)
     * @see net.sf.l2j.util.L2ObjectSet#contains(T)
     */
	public boolean contains(T obj)
	{
		final int size = table.length; 
		if (size <= 11)
		{
			// for small tables linear check is fast 
			for (int i=0; i < size; i++)
			{
				if (table[i] == obj)
					return true;
			}
			return false;
		}
		int hashcode = obj.getObjectId();
		if (Config.ASSERT) assert hashcode > 0;
		int seed = hashcode;
		int incr = 1 + (((seed >> 5) + 1) % (size - 1));
		int ntry = 0;
		do
		{
			int pos = (seed % size) & 0x7FFFFFFF;
			if (table[pos] == obj)
				return true;
			// check for collision (if we previously deleted element)
			if (table[pos] == null && (collisions[pos>>5] & (1<<(pos&31))) == 0) {
				return false;
			}
			// calculate next slot
			seed += incr;
		} while (++ntry < size);
		return false;
	}
	
	private /*already synchronized in put()*/ void expand()
	{
		int newSize = getPrime(table.length+1);
		L2Object[] newTable = new L2Object[newSize];
		int[] newCollisions = new int[(newSize+31)>>5];
		
		// over all old entries
	next_entry:
		for (int i=0; i < table.length; i++)
		{
			L2Object obj = table[i]; 
			if (obj == null)
				continue;
			final int hashcode = obj.getObjectId();
			int seed = hashcode;
			int incr = 1 + (((seed >> 5) + 1) % (newSize - 1));
			int ntry = 0;
			do
			{
				int pos = (seed % newSize) & 0x7FFFFFFF;
				if (newTable[pos] == null)
				{
					// found an empty slot without previous collisions,
					// but use previously found slot
					newTable[pos] = obj;
					if (TRACE) System.err.println("ht: move obj id="+hashcode+" from slot="+i+" to slot="+pos);
					continue next_entry;
				}
				// set collision bit
				newCollisions[pos>>5] |= 1<<(pos&31); 
				// calculate next slot
				seed += incr;
			} while (++ntry < newSize);
			throw new IllegalStateException();
		}
		table = (T[])newTable;
		collisions = newCollisions;
		if (DEBUG) check();
	}
	
	/* (non-Javadoc)
     * @see net.sf.l2j.util.L2ObjectSet#iterator()
     */
	public Iterator<T> iterator()
	{
		return new Itr(table);
	}
	
	class Itr implements Iterator<T>
	{
		private final T[] array;
		private int nextIdx;
		private T nextObj;
		private T lastRet;
		Itr(T[] array)
		{
			this.array = array;
			for (; nextIdx < array.length; nextIdx++)
			{
				nextObj = array[nextIdx]; 
				if (nextObj != null)
					return;
			}
		}
		public boolean hasNext()
		{
			return nextObj != null;
		}
		public T next()
		{
			if (nextObj == null)
				throw new NoSuchElementException();
			lastRet = nextObj;
			for (nextIdx++; nextIdx < array.length; nextIdx++)
			{
				nextObj = array[nextIdx]; 
				if (nextObj != null)
					break;
			}
			if (nextIdx >= array.length)
				nextObj = null;
			return lastRet;
		}
		public void remove()
		{
			if (lastRet == null)
				throw new IllegalStateException();
			L2ObjectHashSet.this.remove(lastRet);
		}
	}
}
