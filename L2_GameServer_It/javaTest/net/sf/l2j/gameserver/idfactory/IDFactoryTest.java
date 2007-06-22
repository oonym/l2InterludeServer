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

import java.util.Random;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;
import net.sf.l2j.Config;
import net.sf.l2j.Server;
import net.sf.l2j.Config.IdFactoryType;

/**
 * This class ...
 * 
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class IDFactoryTest extends TestCase
{
	public static final boolean debug = false;

	// Compaction, BitSet, Stack, (null to use config)
	private static final IdFactoryType FORCED_TYPE = IdFactoryType.Stack;

	protected IdFactory idFactory;
	protected AtomicInteger count = new AtomicInteger(0), adds = new AtomicInteger(0), removes = new AtomicInteger(0);

	protected static final int REQUESTER_THREADS              = 50;
	protected static final int REQUESTER_THREAD_REQUESTS      = 1000;
	protected static final int REQUESTER_THREAD_RANDOM_DELAY  = 30;
	protected static final int RELEASER_THREADS               = 50;
	protected static final int RELEASER_THREAD_RELEASES       = 1000;
	protected static final int RELEASER_THREAD_RANDOM_DELAY   = 35;

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
		Server.SERVER_MODE = Server.MODE_GAMESERVER;
		Config.load();
		if(FORCED_TYPE != null)
			Config.IDFACTORY_TYPE = FORCED_TYPE;
		idFactory   = IdFactory.getInstance();
	}

	/*
	 * @see TestCase#setUp()
	 */
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
	protected void tearDown() throws Exception
	{
		super.tearDown();
		idFactory = null;
	}

	/*
	 * Test method for 'net.sf.l2j.gameserver.idfactory.IdFactory.getNextId()'
	 */
	public final void testFactory()
	{
		System.out.println("Free ID's: "+idFactory.size());
		System.out.println("Used ID's: "+(IdFactory.FREE_OBJECT_ID_SIZE - idFactory.size()));
		map.add(idFactory.getNextId());
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
			latch.await();
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Free ID's: "+idFactory.size());
		System.out.println("Used ID's: "+(IdFactory.FREE_OBJECT_ID_SIZE - idFactory.size()));
		System.out.println("Count: "+count.get());
	}

	CountDownLatch latch = new CountDownLatch(REQUESTER_THREADS + RELEASER_THREADS);

	protected static Vector<Integer> map = new Vector<Integer>();

	public class RequestID implements Runnable
	{
		long time1;
		long time2;
		AtomicInteger myCount   = new AtomicInteger(0);
		public void run()
		{
			for (int i=0; i<REQUESTER_THREAD_REQUESTS; i++)
			{
				synchronized (map)
				{
					time1 = System.nanoTime();
					int newId = idFactory.getNextId();
					time2 = System.nanoTime() - time1;
					count.incrementAndGet();
					adds.incrementAndGet();
					myCount.incrementAndGet();
					map.add(newId);
					if (debug) System.out.println("Got new ID "+newId);
					if (random.nextInt(10) == 0)
					{
						System.out.println("					Total ID requests: "+adds.get()+". "+time2+"ns");
					}
				}
				try
				{
					Thread.sleep(random.nextInt(REQUESTER_THREAD_RANDOM_DELAY));
				}
				catch (InterruptedException e)
				{
					System.out.println(Thread.currentThread().getName()+" was Interupted.");
				}
			}
			if (debug) System.out.println(getName()+ " myCount is "+myCount.get()+"/100.");
			latch.countDown();
		}
	}


	public class ReleaseID implements Runnable
	{
		AtomicInteger myCount   = new AtomicInteger(100);
		long time1;
		long time2;
		public void run()
		{
			for (int i=0; i<RELEASER_THREAD_RELEASES; i++)
			{
				synchronized (map)
				{
					int size    = map.size();
					if (map.size() <= 0)
					{
						i--;
						continue;
					}
					//if (size > 0)
						//{
						int pos     = random.nextInt(size);
						int id      = map.get(pos);
						time1 = System.nanoTime();
						idFactory.releaseId(id);
						time2 = System.nanoTime() - time1;
						map.remove(pos);
						count.decrementAndGet();
						myCount.decrementAndGet();
						removes.incrementAndGet();
						if (debug) System.out.println("Released ID "+id);
						if (random.nextInt(10) == 0)
						{
							System.out.println("Total ID releases: "+removes.get()+". "+time2+"ns");
						}
						//}
				}
				try
				{
					Thread.sleep(random.nextInt(RELEASER_THREAD_RANDOM_DELAY));
				}
				catch (InterruptedException e)
				{

				}
			}
			if (debug) System.out.println(getName()+ " count is "+myCount.get()+"/100.");

			latch.countDown();
		}
	}

	protected Random random = new Random();

	private static long fSLEEP_INTERVAL = 100;

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
			Thread.sleep(fSLEEP_INTERVAL);
			System.runFinalization();
			Thread.sleep(fSLEEP_INTERVAL);
		}
		catch (InterruptedException ex){
			ex.printStackTrace();
		}
	}

}
