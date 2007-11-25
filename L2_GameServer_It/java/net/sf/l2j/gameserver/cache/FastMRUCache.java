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
package net.sf.l2j.gameserver.cache;

import javolution.lang.Reusable;
import javolution.util.FastCollection;
import javolution.util.FastComparator;
import javolution.util.FastList;
import javolution.util.FastMap;
;

/**
 * @author Layane
 *
 */
public class FastMRUCache<K,V> extends FastCollection implements Reusable
{
    /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_CAPACITY = 50;
    private static final int DEFAULT_FORGET_TIME = 300000; //5 Minutes

    private FastMap<K,CacheNode> _cache = new FastMap<K,CacheNode>().setKeyComparator(FastComparator.DIRECT);
    private FastMap<K,V> _map;
    private FastList<K> _mruList = new FastList<K>();
    private int _cacheSize;
    private int _forgetTime;

    class CacheNode
    {
        long _lastModified;
        V _node;

        public CacheNode(V object)
        {
            _lastModified = System.currentTimeMillis();
            _node = object;
        }

        @Override
		public boolean equals(Object object)
        {
        	return _node == object;
        }

    }

    /**
     * Holds the set factory.
     */
    private static final Factory FACTORY = new Factory() {

        @Override
		public Object create() {
            return new FastMRUCache();
        }

        @Override
		public void cleanup(Object obj) {
            ((FastMRUCache) obj).reset();
        }
    };

    /**
     * Returns a set allocated from the stack when executing in a
     * {@link javolution.realtime.PoolContext PoolContext}).
     *
     * @return a new, pre-allocated or recycled set instance.
     */
    public static FastMRUCache newInstance() {
        return (FastMRUCache) FACTORY.object();
    }

    public FastMRUCache()
    {
        this(new FastMap<K,V>(),DEFAULT_CAPACITY,DEFAULT_FORGET_TIME);
    }

    public FastMRUCache (FastMap<K,V> map)
    {
        this(map,DEFAULT_CAPACITY,DEFAULT_FORGET_TIME);
    }

    public FastMRUCache (FastMap<K,V> map, int max)
    {
        this(map,max,DEFAULT_FORGET_TIME);
    }

    public FastMRUCache (FastMap<K,V> map, int max, int forgetTime)
    {
        _map = map;
        _cacheSize = max;
        _forgetTime = forgetTime;
        _map.setKeyComparator(FastComparator.DIRECT);
    }

    // Implements Reusable.
    @Override
	public synchronized void reset() {
        _map.reset();
        _cache.reset();
        _mruList.reset();
        _map.setKeyComparator(FastComparator.DIRECT);
        _cache.setKeyComparator(FastComparator.DIRECT);
    }

    public synchronized V get(K key)
    {
        V result;

        if (!_cache.containsKey(key))
        {
                if (_mruList.size() >= _cacheSize)
                {

                    _cache.remove(_mruList.getLast());
                    _mruList.removeLast();
                }

                result = _map.get(key);

                _cache.put(key, new CacheNode(result));
                _mruList.addFirst(key);
        }
        else
        {
            CacheNode current = _cache.get(key);

            if ((current._lastModified + _forgetTime) <= System.currentTimeMillis())
            {
                    current._lastModified = System.currentTimeMillis();
                    current._node = _map.get(key);
                    _cache.put(key,current);
            }

                _mruList.remove(key);
                _mruList.addFirst(key);

            result = current._node;
        }

        return result;
    }

    @Override
	public synchronized boolean remove(Object key)
    {
        _cache.remove(key);
        _mruList.remove(key);
        return _map.remove(key) == key;
    }

    public FastMap<K,V> getContentMap()
    {
        return _map;
    }

    @Override
	public int size()
    {
        return _mruList.size();
    }

    public int capacity()
    {
        return _cacheSize;
    }

    public int getForgetTime()
    {
        return _forgetTime;
    }

    @Override
	public synchronized void clear()
    {
        _cache.clear();
        _mruList.clear();
        _map.clear();
    }

    // Implements FastCollection abstract method.
    @Override
	public final Record head() {
        return _mruList.head();
    }

    @Override
	public final Record tail() {
        return _mruList.tail();
    }

    @Override
	public final Object valueOf(Record record) {
        return ((FastMap.Entry) record).getKey();
    }

    @Override
	public final void delete(Record record) {
        remove(((FastMap.Entry) record).getKey());
    }
}
