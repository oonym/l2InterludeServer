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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.l2jserver.mmocore.network.ReceivablePacket;

import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.network.L2GameClient;

/**
 * <p>This class is made to handle all the ThreadPools used in L2j.</p>
 * <p>Scheduled Tasks can either be sent to a {@link #_generalScheduledThreadPool "general"} or {@link #_effectsScheduledThreadPool "effects"} {@link ScheduledThreadPoolExecutor ScheduledThreadPool}:
 * The "effects" one is used for every effects (skills, hp/mp regen ...) while the "general" one is used for
 * everything else that needs to be scheduled.<br>
 * There also is an {@link #_aiScheduledThreadPool "ai"} {@link ScheduledThreadPoolExecutor ScheduledThreadPool} used for AI Tasks.</p>
 * <p>Tasks can be sent to {@link ScheduledThreadPoolExecutor ScheduledThreadPool} either with:
 * <ul>
 * <li>{@link #scheduleEffect(Runnable, long)} : for effects Tasks that needs to be executed only once.</li>
 * <li>{@link #scheduleGeneral(Runnable, long)} : for scheduled Tasks that needs to be executed once.</li>
 * <li>{@link #scheduleAi(Runnable, long)} : for AI Tasks that needs to be executed once</li>
 * </ul>
 * or
 * <ul>
 * <li>{@link #scheduleEffectAtFixedRate(Runnable, long, long)(Runnable, long)} : for effects Tasks that needs to be executed periodicaly.</li>
 * <li>{@link #scheduleGeneralAtFixedRate(Runnable, long, long)(Runnable, long)} : for scheduled Tasks that needs to be executed periodicaly.</li>
 * <li>{@link #scheduleAiAtFixedRate(Runnable, long, long)(Runnable, long)} : for AI Tasks that needs to be executed periodicaly</li>
 * </ul></p>
 * 
 * <p>For all Tasks that should be executed with no delay asynchronously in a ThreadPool there also are usual {@link ThreadPoolExecutor ThreadPools}
 * that can grow/shrink according to their load.:
 * <ul>
 * <li>{@link #_generalPacketsThreadPool GeneralPackets} where most packets handler are executed.</li>
 * <li>{@link #_urgentPacketsThreadPool UrgentPackets} where all the urgent packets are executed.</li>
 * <li>There will be an AI ThreadPool where AI events should be executed</li>
 * <li>A general ThreadPool where everything else that needs to run asynchronously with no delay should be executed ({@link net.sf.l2j.gameserver.model.actor.knownlist KnownList} updates, SQL updates/inserts...)?</li>
 * </ul>
 * </p> 
 * @author -Wooden-
 *
 */
public class ThreadPoolManager
{
	private static ThreadPoolManager _instance;
	
	private ScheduledThreadPoolExecutor _effectsScheduledThreadPool;
	private ScheduledThreadPoolExecutor _generalScheduledThreadPool;
	
	private ThreadPoolExecutor _generalPacketsThreadPool;
	private ThreadPoolExecutor _urgentPacketsThreadPool;
	// will be really used in the next AI implementation.
	private ThreadPoolExecutor _aiThreadPool;
	private ThreadPoolExecutor _generalThreadPool;
	
	// temp
	private ScheduledThreadPoolExecutor _aiScheduledThreadPool;
	
	private boolean _shutdown;
	
	public static ThreadPoolManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new ThreadPoolManager();
		}
		return _instance;
	}
	
	private ThreadPoolManager()
	{
		_effectsScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_EFFECTS, new PriorityThreadFactory("EffectsSTPool", Thread.MIN_PRIORITY));
		_generalScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.THREAD_P_GENERAL, new PriorityThreadFactory("GerenalSTPool", Thread.NORM_PRIORITY));
		
		_urgentPacketsThreadPool = new ThreadPoolExecutor(Config.URGENT_PACKET_THREAD_CORE_SIZE, Integer.MAX_VALUE,
		                                                  5L, TimeUnit.SECONDS,
		                                                  new LinkedBlockingQueue<Runnable>(),
		                                                  new PriorityThreadFactory("High Packet Pool",Thread.NORM_PRIORITY+3));
		
		_generalPacketsThreadPool = new ThreadPoolExecutor(Config.GENERAL_PACKET_THREAD_CORE_SIZE, Config.GENERAL_PACKET_THREAD_CORE_SIZE+2,
		                                                   15L, TimeUnit.SECONDS,
		                                                   new LinkedBlockingQueue<Runnable>(),
		                                                   new PriorityThreadFactory("Normal Packet Pool",Thread.NORM_PRIORITY+1));
		
		_generalThreadPool = new ThreadPoolExecutor(Config.GENERAL_THREAD_CORE_SIZE, Config.GENERAL_THREAD_CORE_SIZE+2,
		                                                   5L, TimeUnit.SECONDS,
		                                                   new LinkedBlockingQueue<Runnable>(),
		                                                   new PriorityThreadFactory("General Pool",Thread.NORM_PRIORITY));
		
		// will be really used in the next AI implementation.
		_aiThreadPool = new ThreadPoolExecutor(1, Config.AI_MAX_THREAD,
			                                      10L, TimeUnit.SECONDS,
			                                      new LinkedBlockingQueue<Runnable>());
		
		_aiScheduledThreadPool = new ScheduledThreadPoolExecutor(Config.AI_MAX_THREAD, new PriorityThreadFactory("AISTPool", Thread.NORM_PRIORITY));
	}
	
	public ScheduledFuture scheduleEffect(Runnable r, long delay)
	{
		try
		{
			if (delay < 0) delay = 0;
			return _effectsScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}
	
	public ScheduledFuture scheduleEffectAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
			if (delay < 0) delay = 0;
			if (initial < 0) initial = 0;
			return _effectsScheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}
	
	public ScheduledFuture scheduleGeneral(Runnable r, long delay)
	{
		try
		{
			if (delay < 0) delay = 0;
			return _generalScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}
	
	public ScheduledFuture scheduleGeneralAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
			if (delay < 0) delay = 0;
			if (initial < 0) initial = 0;
			return _generalScheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}
	
	public ScheduledFuture scheduleAi(Runnable r, long delay)
	{
		try
		{
			if (delay < 0) delay = 0;
			return _aiScheduledThreadPool.schedule(r, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}
	
	public ScheduledFuture scheduleAiAtFixedRate(Runnable r, long initial, long delay)
	{
		try
		{
			if (delay < 0) delay = 0;
			if (initial < 0) initial = 0;
			return _aiScheduledThreadPool.scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) { return null; /* shutdown, ignore */ }
	}
	
	public void executePacket(ReceivablePacket<L2GameClient> pkt)
	{
		_generalPacketsThreadPool.execute(pkt);
	}
	
	public void executeIOPacket(ReceivablePacket<L2GameClient> pkt)
	{
		_urgentPacketsThreadPool.execute(pkt);
	}
	
	public void executeTask(Runnable r)
	{
		_generalThreadPool.execute(r);
	}
	
	public void executeAi(Runnable r)
	{
		_aiThreadPool.execute(r);
	}
	
	public String[] getStats()
	{
		return new String[] {
		                     "STP:",
		                     " + Effects:",
		                     " |- ActiveThreads:   "+_effectsScheduledThreadPool.getActiveCount(),
		                     " |- getCorePoolSize: "+_effectsScheduledThreadPool.getCorePoolSize(),
		                     " |- PoolSize:        "+_effectsScheduledThreadPool.getPoolSize(),
		                     " |- MaximumPoolSize: "+_effectsScheduledThreadPool.getMaximumPoolSize(),
		                     " |- CompletedTasks:  "+_effectsScheduledThreadPool.getCompletedTaskCount(),
		                     " |- ScheduledTasks:  "+(_effectsScheduledThreadPool.getTaskCount() - _effectsScheduledThreadPool.getCompletedTaskCount()),
		                     " | -------",
		                     " + General:",
		                     " |- ActiveThreads:   "+_generalScheduledThreadPool.getActiveCount(),
		                     " |- getCorePoolSize: "+_generalScheduledThreadPool.getCorePoolSize(),
		                     " |- PoolSize:        "+_generalScheduledThreadPool.getPoolSize(),
		                     " |- MaximumPoolSize: "+_generalScheduledThreadPool.getMaximumPoolSize(),
		                     " |- CompletedTasks:  "+_generalScheduledThreadPool.getCompletedTaskCount(),
		                     " |- ScheduledTasks:  "+(_generalScheduledThreadPool.getTaskCount() - _generalScheduledThreadPool.getCompletedTaskCount()),
		                     " | -------",
		                     " + AI:",
		                     " |- ActiveThreads:   "+_aiScheduledThreadPool.getActiveCount(),
		                     " |- getCorePoolSize: "+_aiScheduledThreadPool.getCorePoolSize(),
		                     " |- PoolSize:        "+_aiScheduledThreadPool.getPoolSize(),
		                     " |- MaximumPoolSize: "+_aiScheduledThreadPool.getMaximumPoolSize(),
		                     " |- CompletedTasks:  "+_aiScheduledThreadPool.getCompletedTaskCount(),
		                     " |- ScheduledTasks:  "+(_aiScheduledThreadPool.getTaskCount() - _aiScheduledThreadPool.getCompletedTaskCount()),
		                     "TP:",
		                     " + Packets:",
		                     " |- ActiveThreads:   "+_generalPacketsThreadPool.getActiveCount(),
		                     " |- getCorePoolSize: "+_generalPacketsThreadPool.getCorePoolSize(),
		                     " |- MaximumPoolSize: "+_generalPacketsThreadPool.getMaximumPoolSize(),
		                     " |- LargestPoolSize: "+_generalPacketsThreadPool.getLargestPoolSize(),
		                     " |- PoolSize:        "+_generalPacketsThreadPool.getPoolSize(),
		                     " |- CompletedTasks:  "+_generalPacketsThreadPool.getCompletedTaskCount(),
		                     " |- QueuedTasks:     "+_generalPacketsThreadPool.getQueue().size(),
		                     " | -------",
		                     " + Urgent Packets:",
		                     " |- ActiveThreads:   "+_urgentPacketsThreadPool.getActiveCount(),
		                     " |- getCorePoolSize: "+_urgentPacketsThreadPool.getCorePoolSize(),
		                     " |- MaximumPoolSize: "+_urgentPacketsThreadPool.getMaximumPoolSize(),
		                     " |- LargestPoolSize: "+_urgentPacketsThreadPool.getLargestPoolSize(),
		                     " |- PoolSize:        "+_urgentPacketsThreadPool.getPoolSize(),
		                     " |- CompletedTasks:  "+_urgentPacketsThreadPool.getCompletedTaskCount(),
		                     " |- QueuedTasks:     "+_urgentPacketsThreadPool.getQueue().size(),
		                     " | -------",
		                     " + General Tasks:",
		                     " |- ActiveThreads:   "+_generalThreadPool.getActiveCount(),
		                     " |- getCorePoolSize: "+_generalThreadPool.getCorePoolSize(),
		                     " |- MaximumPoolSize: "+_generalThreadPool.getMaximumPoolSize(),
		                     " |- LargestPoolSize: "+_generalThreadPool.getLargestPoolSize(),
		                     " |- PoolSize:        "+_generalThreadPool.getPoolSize(),
		                     " |- CompletedTasks:  "+_generalThreadPool.getCompletedTaskCount(),
		                     " |- QueuedTasks:     "+_generalThreadPool.getQueue().size(),
		                     " | -------",
		                     " + AI:",
		                     " |- Not Done"
		};
	}
	
    private class PriorityThreadFactory implements ThreadFactory
    {
    	private int _prio;
		private String _name;
		private AtomicInteger _threadNumber = new AtomicInteger(1);
		private ThreadGroup _group;
    	
		public PriorityThreadFactory(String name, int prio)
    	{
    		_prio = prio;
    		_name = name;
    		_group = new ThreadGroup(_name);    		
    	}
		/* (non-Javadoc)
		 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
		 */
		public Thread newThread(Runnable r)
		{
			Thread t = new Thread(_group,r);
			t.setName(_name+"-"+_threadNumber.getAndIncrement());
			t.setPriority(_prio);
			return t;
		}
    	
		public ThreadGroup getGroup()
		{
			return _group;
		}
    }

	/**
	 * 
	 */
	public void shutdown()
	{
		_shutdown = true;
		try
		{
			_effectsScheduledThreadPool.awaitTermination(1,TimeUnit.SECONDS);
			_generalScheduledThreadPool.awaitTermination(1,TimeUnit.SECONDS);
			_generalPacketsThreadPool.awaitTermination(1,TimeUnit.SECONDS);
			_urgentPacketsThreadPool.awaitTermination(1,TimeUnit.SECONDS);
			_generalThreadPool.awaitTermination(1,TimeUnit.SECONDS);
			_aiThreadPool.awaitTermination(1,TimeUnit.SECONDS);
			_effectsScheduledThreadPool.shutdown();
			_generalScheduledThreadPool.shutdown();
			_generalPacketsThreadPool.shutdown();
			_urgentPacketsThreadPool.shutdown();
			_generalThreadPool.shutdown();
			_aiThreadPool.shutdown();
			System.out.println("All ThreadPools are now stoped");
			
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isShutdown()
	{
		return _shutdown;
	}
	
	/**
	 * 
	 */
	public void purge()
	{
		_effectsScheduledThreadPool.purge();
		_generalScheduledThreadPool.purge();
		_aiScheduledThreadPool.purge();
		_urgentPacketsThreadPool.purge();
		_generalPacketsThreadPool.purge();
		_generalThreadPool.purge();
		_aiThreadPool.purge();
	}

	/**
	 * 
	 */
	public String getPacketStats()
	{
		TextBuilder tb = new TextBuilder();
		ThreadFactory tf = _generalPacketsThreadPool.getThreadFactory();
		if (tf instanceof PriorityThreadFactory)
		{
			tb.append("General Packet Thread Pool:\r\n");
			tb.append("Tasks in the queue: "+_generalPacketsThreadPool.getQueue().size()+"\r\n");
			tb.append("Showing threads stack trace:\r\n");
			PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			int count = ptf.getGroup().activeCount();
			Thread[] threads = new Thread[count+2];
			ptf.getGroup().enumerate(threads);
			tb.append("There should be "+count+" Threads\r\n");
			for(Thread t : threads)
			{
				if(t == null)
					continue;
				tb.append(t.getName()+"\r\n");
				for(StackTraceElement ste :t.getStackTrace())
				{
					tb.append(ste.toString());
					tb.append("\r\n");
				}
			}
		}
		tb.append("Packet Tp stack traces printed.\r\n");
		return tb.toString();
	}
	
	public String getUrgentPacketStats()
	{
		TextBuilder tb = new TextBuilder();
		ThreadFactory tf = _urgentPacketsThreadPool.getThreadFactory();
		if (tf instanceof PriorityThreadFactory)
		{
			tb.append("Urgent Packet Thread Pool:\r\n");
			tb.append("Tasks in the queue: "+_urgentPacketsThreadPool.getQueue().size()+"\r\n");
			tb.append("Showing threads stack trace:\r\n");
			PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			int count = ptf.getGroup().activeCount();
			Thread[] threads = new Thread[count+2];
			ptf.getGroup().enumerate(threads);
			tb.append("There should be "+count+" Threads\r\n");
			for(Thread t : threads)
			{
				if(t == null)
					continue;
				tb.append(t.getName()+"\r\n");
				for(StackTraceElement ste :t.getStackTrace())
				{
					tb.append(ste.toString());
					tb.append("\r\n");
				}
			}
		}
		tb.append("Packet Tp stack traces printed.\r\n");
		return tb.toString();
	}
	
	public String getGeneralStats()
	{
		TextBuilder tb = new TextBuilder();
		ThreadFactory tf = _generalThreadPool.getThreadFactory();
		if (tf instanceof PriorityThreadFactory)
		{
			tb.append("General Thread Pool:\r\n");
			tb.append("Tasks in the queue: "+_generalThreadPool.getQueue().size()+"\r\n");
			tb.append("Showing threads stack trace:\r\n");
			PriorityThreadFactory ptf = (PriorityThreadFactory) tf;
			int count = ptf.getGroup().activeCount();
			Thread[] threads = new Thread[count+2];
			ptf.getGroup().enumerate(threads);
			tb.append("There should be "+count+" Threads\r\n");
			for(Thread t : threads)
			{
				if(t == null)
					continue;
				tb.append(t.getName()+"\r\n");
				for(StackTraceElement ste :t.getStackTrace())
				{
					tb.append(ste.toString());
					tb.append("\r\n");
				}
			}
		}
		tb.append("Packet Tp stack traces printed.\r\n");
		return tb.toString();
	}
}