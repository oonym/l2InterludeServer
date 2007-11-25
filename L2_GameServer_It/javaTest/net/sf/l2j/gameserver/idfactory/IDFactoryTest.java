/*
 * $Header: IDFactoryTest.java, 26/08/2005 01:04:53 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 26/08/2005 01:04:53 $
 * $Revision: 1 $
 * $Log: IDFactoryTest.java,v $
 * Revision 1  26/08/2005 01:04:53  luisantonioa
 * Added copyright notice
 *
 *
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
package net.sf.l2j.gameserver.idfactory;

import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;
import net.sf.l2j.Config;
import net.sf.l2j.Server;
import net.sf.l2j.Config.IdFactoryType;
import net.sf.l2j.util.Rnd;

/**
 * This class ...
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class IDFactoryTest extends TestCase
{
	private static final boolean _debug = false;

	// Compaction, BitSet, Stack, (null to use config)
	private static final IdFactoryType FORCED_TYPE = IdFactoryType.Stack;

	protected IdFactory _idFactory;
	protected AtomicInteger _count = new AtomicInteger(0), _adds = new AtomicInteger(0), _removes = new AtomicInteger(0);

	protected static final int REQUESTER_THREADS              = 50;
	protected static final int REQUESTER_THREAD_REQUESTS      = 1000;
	protected static final int REQUESTER_THREAD_RANDOM_DELAY  = 30;
	protected static final int RELEASER_THREADS               = 50;
	protected static final int RELEASER_THREAD_RELEASES       = 1000;
	protected static final int RELEASER_THREAD_RANDOM_DELAY   = 35;

	private static final long F_SLEEP_INTERVAL = 100;

	CountDownLatch _latch = new CountDownLatch(REQUESTER_THREADS + RELEASER_THREADS);
	protected static Vector<Integer> _map = new Vector<Integer>();

	public static void main(String[] args)
	{
	}

	/**
	 * Constructor for IDFactoryTest.
	 * @param arg0
	 */
	public IDFactoryTest(String arg0)
	{
		super(arg0);
		Server.serverMode = Server.MODE_GAMESERVER;
		Config.load();
		if(FORCED_TYPE != null)
			Config.IDFACTORY_TYPE = FORCED_TYPE;
		_idFactory = IdFactory.getInstance();
	}

	/*
	 * @see TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		System.out.println("Initial Free ID's: "+IdFactory.FREE_OBJECT_ID_SIZE);
		System.out.println("IdFactoryType: "+Config.IDFACTORY_TYPE.name());
		/*long startMemoryUse = getMemoryUse();
        BitSet freeIds     = new BitSet(0x6FFFFFFF);
        long endMemoryUse = getMemoryUse();
        freeIds.clear();
        long approximateSize = ( endMemoryUse - startMemoryUse ) / (1024*1024);

        System.out.println("Size: "+approximateSize+"Mb.");*/
	}

	/*
	 * @see TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
		_idFactory = null;
	}

	/*
	 * Test method for 'net.sf.l2j.gameserver.idfactory.IdFactory.getNextId()'
	 */
	public final void testFactory()
	{
		System.out.println("Free ID's: "+_idFactory.size());
		System.out.println("Used ID's: "+(IdFactory.FREE_OBJECT_ID_SIZE - _idFactory.size()));
		_map.add(_idFactory.getNextId());
		for (int i=0; i<REQUESTER_THREADS; i++)
		{
			new Thread(new RequestID(), "Request-Thread-"+i).start();
		}
		for (int i=0; i<RELEASER_THREADS; i++)
		{
			new Thread(new ReleaseID(), "Release-Thread-"+i).start();
		}
		try
		{
			_latch.await();
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Free ID's: "+_idFactory.size());
		System.out.println("Used ID's: "+(IdFactory.FREE_OBJECT_ID_SIZE - _idFactory.size()));
		System.out.println("Count: "+_count.get());
	}

	public class RequestID implements Runnable
	{
		long _time1;
		long _time2;
		AtomicInteger _myCount   = new AtomicInteger(0);
		public void run()
		{
			for (int i=0; i<REQUESTER_THREAD_REQUESTS; i++)
			{
				synchronized (_map)
				{
					_time1 = System.nanoTime();
					int newId = _idFactory.getNextId();
					_time2 = System.nanoTime() - _time1;
					_count.incrementAndGet();
					_adds.incrementAndGet();
					_myCount.incrementAndGet();
					_map.add(newId);
					if (_debug) System.out.println("Got new ID "+newId);
					if (Rnd.nextInt(10) == 0)
					{
						System.out.println("					Total ID requests: "+_adds.get()+". "+_time2+"ns");
					}
				}
				try
				{
					Thread.sleep(Rnd.nextInt(REQUESTER_THREAD_RANDOM_DELAY));
				}
				catch (InterruptedException e)
				{
					System.out.println(Thread.currentThread().getName()+" was Interupted.");
				}
			}
			if (_debug) System.out.println(getName()+ " myCount is "+_myCount.get()+"/100.");
			_latch.countDown();
		}
	}


	public class ReleaseID implements Runnable
	{
		AtomicInteger _myCount = new AtomicInteger(100);
		long _time1;
		long _time2;
		public void run()
		{
			for (int i=0; i<RELEASER_THREAD_RELEASES; i++)
			{
				synchronized (_map)
				{
					int size    = _map.size();
					if (_map.size() <= 0)
					{
						i--;
						continue;
					}
					//if (size > 0)
						//{
						int pos     = Rnd.nextInt(size);
						int id      = _map.get(pos);
						_time1 = System.nanoTime();
						_idFactory.releaseId(id);
						_time2 = System.nanoTime() - _time1;
						_map.remove(pos);
						_count.decrementAndGet();
						_myCount.decrementAndGet();
						_removes.incrementAndGet();
						if (_debug) System.out.println("Released ID "+id);
						if (Rnd.nextInt(10) == 0)
						{
							System.out.println("Total ID releases: "+_removes.get()+". "+_time2+"ns");
						}
						//}
				}
				try
				{
					Thread.sleep(Rnd.nextInt(RELEASER_THREAD_RANDOM_DELAY));
				}
				catch (InterruptedException e)
				{

				}
			}
			if (_debug) System.out.println(getName()+ " count is "+_myCount.get()+"/100.");

			_latch.countDown();
		}
	}

	@SuppressWarnings("unused")
	private static long getMemoryUse(){
		putOutTheGarbage();
		long totalMemory = Runtime.getRuntime().totalMemory();

		putOutTheGarbage();
		long freeMemory = Runtime.getRuntime().freeMemory();

		return (totalMemory - freeMemory);
	}

	private static void putOutTheGarbage() {
		collectGarbage();
		collectGarbage();
	}

	private static void collectGarbage() {
		try {
			System.gc();
			Thread.sleep(F_SLEEP_INTERVAL);
			System.runFinalization();
			Thread.sleep(F_SLEEP_INTERVAL);
		}
		catch (InterruptedException ex){
			ex.printStackTrace();
		}
	}

}
