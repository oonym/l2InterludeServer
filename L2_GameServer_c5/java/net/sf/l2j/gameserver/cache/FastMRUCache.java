/**
 * 
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
	public static final int DEFAULT_CAPACITY = 50;
    public static final int DEFAULT_FORGET_TIME = 300000; //5 Minutes
    
    FastMap<K,CacheNode> _cache = new FastMap<K,CacheNode>();
    FastMap<K,V> _map;
    FastList<K> _mruList = new FastList<K>();
    int _cacheSize;
    int _forgetTime;
    
    class CacheNode
    {
        long lastModified;
        V node;
        
        public CacheNode(V object)
        {
            lastModified = System.currentTimeMillis();
            node = object;
        }
        
        public synchronized boolean equals(Object object)
        {
        	boolean ret;
        	synchronized (object) {
        		ret = node == object;
        	}
        	return ret;
        }
        
    }
    
    /**
     * Holds the set factory.
     */
    private static final Factory FACTORY = new Factory() {

        public Object create() {
            return new FastMRUCache();
        }

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
    }
    
    // Overrides.
    public synchronized FastCollection setValueComparator(FastComparator comparator) {
        super.setValueComparator(comparator);
        _map.setKeyComparator(comparator);
        _cache.setKeyComparator(comparator);
        return this;
    }
    
    // Implements Reusable.
    public synchronized void reset() {
        super.setValueComparator(FastComparator.DIRECT);
        _map.reset();
        _cache.reset();
        _mruList.reset();
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
            
            if ((current.lastModified + _forgetTime) <= System.currentTimeMillis())
            {
                    current.lastModified = System.currentTimeMillis();
                    current.node = _map.get(key);
                    _cache.put(key,current);
            }
            
                _mruList.remove(key);
                _mruList.addFirst(key);
            
            result = current.node;
        }
        
        return result;
    }
    
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
    
    public synchronized void clear()
    {
        _cache.clear();
        _mruList.clear();
        _map.clear();
    }
    
    // Implements FastCollection abstract method.
    public final synchronized Record head() {
        return _mruList.head();
    }

    public final synchronized Record tail() {
        return _mruList.tail();
    }

    public final Object valueOf(Record record) {
        return ((FastMap.Entry) record).getKey();
    }

    public final void delete(Record record) {
        remove(((FastMap.Entry) record).getKey());
    }
}
