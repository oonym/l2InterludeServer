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

import static net.sf.l2j.gameserver.TaskPriority.PR_NORMAL;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.xml.ObjectWriter;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.clientpackets.ClientBasePacket;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.LeaveWorld;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * NIO Selector thread.
 * Reads and writes network data.
 * All network messages are encoded and decoded withing this thread.
 * You may send messages using sendMessage() - it push the message
 * into internal queue and return immediatly.
 * A scheduler may read messages using recvMessage(), which returns
 * null when no more messaged left in internal queue.
 * 
 * Implementation description:
 * 1. Messages to sent are pushed into outbound queue by external threads.
 * 2. Received messages are pushed into inbound queue by this thread.
 * 3. The loop of Selector is:
 * - try to write mesages from outbound queue
 * - if a message packet is not fully written - store write buffer in the client object
 * - wait for read/write/accept
 * - write data from buffers stored in client objects
 * - read data into a shared read buffer, parse packet and put messages to inbound queue
 * - if a message is not fully received - create a buffer of required
 * size and copy partially read date into it, store this new buffer in the client
 * 
 * TODO: Manage thread's priority. The thread has higher then normal
 * priority to handle network transfers quickly. But wakeup of this thread
 * for each small packet may be time-consuming.
 * 
 * @author Maxim Kizub
 */ 
public final class SelectorThread extends Thread {
	private static Logger _log = Logger.getLogger(SelectorThread.class.getName());
	
	private static SelectorThread _instance;
	
	/** Amount of buffers for writing data */
	private final int WRITE_BUF_HASH_SIZE = 4000;
	/** A size of each write buffer */
	private final int WRITE_BUF_SIZE = 128;
	/** A size of shared read/write buffer */
	private final int SHARED_BUF_SIZE = 64*1024;
	
	/** Stack of write buffers */
	private final ByteBuffer[] writeBuffers;
	private int numWriteBuffers;
	/** Shared write buffer */
	private final ByteBuffer sharedWriteBuffer;
	/** Shared read buffer */
	private final ByteBuffer sharedReadBuffer;
	
	/** Outbound message queue */
	private BasePacketQueue sendMsgQueue;
	/** Inbound message queue */
	private BasePacketQueue recvMsgQueue;

	/** The selector */
	private Selector _selector;
	
	private final String _hostname;
	private final int _port;	
	
    public static Map<Class, Long> packetCount              = new FastMap<Class, Long>();
    public static Map<Class, Long> byteCount                = new FastMap<Class, Long>();
    public static final List<PacketHistory> packetHistory   = new FastList<PacketHistory>();
    public static final List<PacketHistory> byteHistory     = new FastList<PacketHistory>();
    private static ScheduledFuture packetMonitor            = null;
    
	/** push counter, currently counts messages in putbound queue,
	 * but better it count size of outbound queue...
	 */
	private int msgCounter;
    
	public static SelectorThread getInstance()
	{
		if(_instance == null)
		{
			_instance = new SelectorThread(Config.GAMESERVER_HOSTNAME, Config.PORT_GAME);
		}
		return _instance;
	}
	
    class DisconnectionTask implements Runnable
    {
        private Connection _connection;
        public DisconnectionTask(Connection c)
        {
            _connection = c;
        }
        public void run()
        {
            closeClient(_connection,true);
        }
    }
    
	class MonitorPackets implements Runnable
	{
		public void run()
		{
			updateHistory();
		}
	}
    
    private SelectorThread(String hostname, int port)
    {
		super("NIO Selector");
		if (Config.ASSERT) assert _instance == null;
		if (Config.COUNT_PACKETS)  
		{  
			packetMonitor = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new MonitorPackets(), 10000, 10000);  
		} 
		this._hostname = hostname;
		this._port = port;
		this.setDaemon(true);
		this.setPriority(Thread.NORM_PRIORITY+2);
		// write buffers (HeapByteBuffer is much faster for our perposes)
		writeBuffers = new ByteBuffer[WRITE_BUF_HASH_SIZE];
		for (int i=0; i < WRITE_BUF_HASH_SIZE; i++)
		{
			writeBuffers[i] = ByteBuffer.allocate(WRITE_BUF_SIZE);
			writeBuffers[i].order(ByteOrder.LITTLE_ENDIAN);
			writeBuffers[i].clear();
			if (Config.ASSERT) assert writeBuffers[i].capacity() == WRITE_BUF_SIZE;
		}
		numWriteBuffers = WRITE_BUF_HASH_SIZE;
		// shared buffers (HeapByteBuffer is much faster for our perposes)
		sharedWriteBuffer = ByteBuffer.allocate(SHARED_BUF_SIZE);
		sharedReadBuffer  = ByteBuffer.allocate(SHARED_BUF_SIZE);
		sharedWriteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		sharedReadBuffer.order(ByteOrder.LITTLE_ENDIAN);
		// queues
		sendMsgQueue = new BasePacketQueue();
		recvMsgQueue = new BasePacketQueue();
		
	}

	/** Allocate a write buffer. Take it from a cashed stack,
	 * of allocate a new one, if stack is empty or message is huge.
	 * 
	 * @param sz minimal size of the buffer
	 * @return a read buffer, never null
	 */
	private ByteBuffer allocateBuffer(int sz) {
		if (Config.ASSERT) assert Thread.currentThread() == this;
		if (sz <= WRITE_BUF_SIZE && numWriteBuffers > 0) {
			ByteBuffer b = writeBuffers[--numWriteBuffers]; 
			if (Config.ASSERT) assert b.position() == 0;
			if (Config.ASSERT) assert b.capacity() == WRITE_BUF_SIZE;
			if (Config.ASSERT) assert b.limit() >= sz;
			return b;
		}
		// (HeapByteBuffer is much faster for our perposes)
		ByteBuffer b = ByteBuffer.allocate(sz);
		b.order(ByteOrder.LITTLE_ENDIAN);
		if (Config.ASSERT) assert b.position() == 0;
		if (Config.ASSERT) assert b.limit() == sz;
		if (Config.ASSERT) assert b.capacity() == sz;
		return b;
	}
	
	/** Release a write buffer, store buffers to cashe stack, or just
	 * forget it (to be collected by GC)
	 * 
	 * @param b the freed buffer
	 */
	private void releaseBuffer(ByteBuffer b) {
		if (Config.ASSERT) assert Thread.currentThread() == this;
		if (Config.ASSERT) assert b != null;
		if (b.capacity() == WRITE_BUF_SIZE && numWriteBuffers <= WRITE_BUF_HASH_SIZE-1) {
			b.clear();
			if (Config.ASSERT) assert b.position() == 0;
			if (Config.ASSERT) assert b.limit() == WRITE_BUF_SIZE;
			writeBuffers[numWriteBuffers++] = b;
		}
	}
	
	/** Main loop, see class description */
	public void run() {
		try {
			// create selector
			_selector = Selector.open();
			
			// create a test server socket chanell
			ServerSocketChannel ssc = ServerSocketChannel.open();
			InetSocketAddress isa;
			if ("*".equals(_hostname))
			{
				isa = new InetSocketAddress(_port);
                _log.config("GameServer listening on all available IPs on Port "+_port);
			}
			else
			{
				isa = new InetSocketAddress(_hostname, _port);
                _log.config("GameServer listening on IP: "+_hostname + " Port "+_port);
			}
		    ssc.socket().bind(isa, 100);
		    ssc.configureBlocking(false);
		    ssc.register(_selector, SelectionKey.OP_ACCEPT);			
		    // loop forever
			for (;;) {
				// check if we have messages to pack and send
				processOutboudQueue();
				// reset counter
				if (msgCounter == 0)
					this.setPriority(Thread.NORM_PRIORITY+2); // idle
				else
					msgCounter = 0;
				// check for shutdown
				if (isInterrupted())
				{
					try { _selector.close(); } catch (Throwable t) { _log.log(Level.INFO, "", t); }
					return;
				}
				// wait for read/write, timeout is to be on safe side,
				// if waking up selector after a message push will fail
                // this is needed because we are in an infinite loop
				int numKeys = _selector.select(50L);

				// check for shutdown
				if (isInterrupted())
				{
					try { _selector.close(); } catch (Throwable t) { _log.log(Level.INFO, "", t); }
					return;
				}

				if (numKeys==0 && _selector.selectedKeys().size()==0)
				{
					sleep(20); // sleep some time to prevent thread from eating all cpu time
					continue;  // only continue if we don't have any keys to process
				} 
				
				Iterator<SelectionKey> it = _selector.selectedKeys().iterator();
				// iterate over selected keys
				while (it.hasNext())
				{
					SelectionKey sk = it.next();
					it.remove();	
					if (!sk.isValid() || sk == null)
						continue;
					try
					{
						// Obtain the interest of the key
	    				int readyOps = sk.readyOps();
	    				switch (readyOps)
	    				{
	    				case SelectionKey.OP_WRITE:
	    					writeData(sk);
	    					break;
	    				case SelectionKey.OP_READ:
	    					readData(sk);
	    					break;
	    				case SelectionKey.OP_WRITE | SelectionKey.OP_READ:
	    					writeData(sk);
	    					readData(sk);
	    					break;
						case SelectionKey.OP_ACCEPT:
							acceptConnection(sk);
							break;    						
	    				default:
	    					System.err.println("Impossible readyOps="+readyOps);
	    					_log.severe("Impossible readyOps="+readyOps);
	    					sk.cancel();
	    					break;
	    				}
    				}
					catch(CancelledKeyException e)
					{
						//just in case a key is canceled after sk.isValid()
						continue;
					}
				}
			}
		} catch (Throwable t) {
			_log.log(Level.SEVERE, "", t);
			System.exit(1);
		}
        finally
        {
            try
            {
            	_selector.close();
            } catch (Throwable t) { }
        }
	}
	
	/**
	 * Write data to chanell.
	 * Writes only buffers stored in clients, i.e. data that was not
	 * written completly.
	 * If everything is written - disable intrest on writing and
	 * release write buffer to cache of buffers.
	 *  
	 * @param sk
	 */
	private void writeData(SelectionKey sk) {
		Connection con = (Connection)sk.attachment();
		try {
			if (!sk.isValid()) {
				closeClient(con);
				return;
			}
			final ByteBuffer b = con.writeBuffer;
			if (b != null) {
				int r = ((SocketChannel)sk.channel()).write(b);
				if (r < 0) {
					closeClient(con);
					return;
				}
				if (r > 0)
				{
					con.writeTimeStamp = GameTimeController.getGameTicks();
				}
				else if (GameTimeController.getGameTicks() - con.writeTimeStamp > 300)
				{
					// will release buffers
					closeClient(con);
					return;
				}
				if (b.hasRemaining())
					return;
				con.writeBuffer = null;
				releaseBuffer(b);
			}
			sk.interestOps(sk.interestOps() & ~SelectionKey.OP_WRITE);
		} catch (IOException e) {
            try
            {
                _log.info("Error on network write, player "+con.getClient().getActiveChar().getName()+" disconnected?");
            }
            catch(NullPointerException npe)
            {
                _log.info("Error on network write, player disconnected? (nullpointer, couldn't get player name)");
            }
            
            if (con != null) closeClient(con);
		} catch (Throwable t) {
			_log.log(Level.INFO, "", t);
            
            if (con != null) closeClient(con);
		}
	}
	
	/**
	 * Read data from chanell.
	 * If there is an unfinished message (a read buffer stored on client),
	 * then read it. Otherwice read into a shared read buffer.
	 * If there is enough data - parses it and creates a message.
	 * 
	 * If only one byte of a new message available - store it into
	 * client's object.
	 * If there are two or more bytes - then we know the size of
	 * message, and can allocate a read buffer for it, copy
	 * data and store to client's object.
	 *  
	 * @param sk
	 */
	private void readData(SelectionKey sk) {
		Connection con = (Connection)sk.attachment();
		try {
			if (!sk.isValid() || !con.getChannel().isOpen()) {
				closeClient(con);
				return;
			}
			final ByteBuffer b;
			if (con.readBuffer != null) {
				b = con.readBuffer;
				if (Config.ASSERT) assert b.position() >= 2;
			} else {
				b = sharedReadBuffer;
				b.clear();
				if (Config.ASSERT) assert b.position() == 0;
				byte fb = con.readBufferFirstByte;
				if (fb != 0) {
					b.put(fb);
					con.readBufferFirstByte = 0;
				}
			}
			// read into shared/allocated buffer
			int r = ((SocketChannel)sk.channel()).read(b);
			if (r < 0) {
				closeClient(con);
				return;
			}
			if (r == 0) {
				// no data
				return;
			}
			b.flip();
			boolean parsed = false;
			while ((r = b.remaining()) > 2) {
				int sz = b.getShort(b.position()) & 0xFFFF;
				if (Config.ASSERT) assert sz > 0;
				if (sz <= b.remaining()+2) {
					// got full message
					parse(con, b);
					parsed = true;
				} else {
					break;
				}
			}
			// has no data remaining in buffer
			if (!b.hasRemaining()) {
				releaseBuffer(b);
				con.readBuffer = null;
				return;
			}
			// has 1 byte remaining in buffer
			if (b.remaining() == 1) {
				// we don't know the packet size :(
				con.readBufferFirstByte = b.get();
				releaseBuffer(b);
				con.readBuffer = null;
				return;
			}
			// we have the packet size
			if (parsed || b == sharedReadBuffer)
			{
				// allocate buffer for pending read
				con.readBuffer = null;
				int sz = b.getShort() & 0xFFFF;
				con.readBuffer = allocateBuffer(sz+2);
				con.readBuffer.putShort((short)sz).put(b);
				releaseBuffer(b);
			}
			return;
		}
		catch (IOException e)
		{
			try
			{
				con.getClient().getActiveChar().getInventory().updateDatabase();
				_log.info("Error on network read, player "+con.getClient().getActiveChar().getName()+" disconnected?");
			}
			catch(NullPointerException npe)
			{
				_log.info("Error on network read, player disconnected? (nullpointer, couldn't get player name)");
			}

			if(con != null)
				closeClient(con);
		}
		catch (Throwable t)
		{
			_log.log(Level.INFO, "", t);
			closeClient(con);
		}
	}
	
	/** Parse received packet, push a message into inbound queue,
	 * to be retrieved by scheduler.
	 * 
	 * @param con connection in which we received data
	 * @param buf buffer with packet
	 * @param sz size of the packet
	 */
	private void parse(Connection con, ByteBuffer buf)
		throws Throwable
	{
		try
		{
			int sz = buf.getShort() & 0xFFFF;
            if (sz > 1)
                sz -= 2;
			ByteBuffer b = (ByteBuffer)buf.slice().limit(sz);
			b.order(ByteOrder.LITTLE_ENDIAN);
			buf.position(buf.position()+sz); // read message fully
			con.decript(b);
			con.addReceivedMsg(b);
		}
        catch (IllegalArgumentException e)
        {
            _log.log(Level.SEVERE, "Error on parsing input from client: "+con._client.getLoginName(), e);
            releaseBuffer(buf);
            con.readBuffer = null;
            throw e;
        }
        catch (Throwable t) {
			_log.log(Level.SEVERE, "", t);
			releaseBuffer(buf);
			con.readBuffer = null;
			throw t;
		}
	}
	
	public void addReceivedPkt(ClientBasePacket pkt)
	{
		TaskPriority pr = pkt.getPriority();
		
		if (pr == null)
			pr = PR_NORMAL;
		
		// Add a task to one of the pool of thread in function of its priority (HIGH, MEDIUM,LOW)
		// This has effect equivalent to schedule(command, 0, anyUnit)
		switch (pr)
		{
		case PR_URGENT:
			pkt.run();
			return;
		case PR_HIGH:
			ThreadPoolManager.getInstance().executeUrgentPacket(pkt);
			return;
		case PR_NORMAL:
		default:
			ThreadPoolManager.getInstance().executePacket(pkt);
			return;
		}
	}
	
	/** Pack (encode) a message into a network buffer.
	 * The shared buffer is used.
	 * 
	 * @param msg message to pack
	 * @return a buffer with data
	 */
	private boolean pack(ServerBasePacket msg) {
		if (Config.ASSERT) assert msg.getConnection().writeBuffer == null;
		sharedWriteBuffer.clear();
		try {
			boolean ok = msg.write(sharedWriteBuffer);
			if (!ok)
				return false;
		} catch (Exception e) {
			closeClient(msg.getConnection());
			return false;
		}
		if (Config.ASSERT) assert sharedWriteBuffer.position() == 0;
		if (Config.ASSERT) assert sharedWriteBuffer.limit() >= 3;
		return true;
	}
	
	/** Accepts connection, creates a new chanell, client, etc */
	private void acceptConnection(SelectionKey sk) {
		try {
			if (sk.isAcceptable()) {
				SocketChannel c = ((ServerSocketChannel) sk.channel()).accept();
				if (c != null) {
					c.configureBlocking(false);
					c.register(sk.selector(), SelectionKey.OP_READ|SelectionKey.OP_WRITE);
					SelectionKey sk2 = c.keyFor(sk.selector());
					ClientThread client = ClientThread.create(c.socket());
					sk2.attach(client.getConnection());
				}
			}
		} catch (Throwable t) {
			_log.log(Level.INFO, "", t);
		}
	}
	
	/** Send a message to client.
	 * The message is placed into message queue, and method returns immediatly.
	 * 
	 * @param msg a message to send
	 */
	void sendMessage(ServerBasePacket msg) {
		// the message queue is synchronized itself
		if (Config.ASSERT) assert msg.getConnection() != null;
		if (Config.COUNT_PACKETS)  
		{  
			updateCounter(msg);  
		}
		if (msg.getLifeTime() > 0)
		    msg.setTime(System.currentTimeMillis());
		sendMsgQueue.put(msg);
		msgCounter++;
		if (msgCounter > 20 || msg instanceof LeaveWorld) {
			_selector.wakeup();
			if (msgCounter > 500)
			{
				this.setPriority(Thread.NORM_PRIORITY+3); // up
			}
		}
	}
	
	/** Receives incoming messages.
	 * Must be called by scheduler, which will read messages and dispatch
	 * their execution to worker threads.
	 * 
	 * @return
	 */
	synchronized ClientBasePacket recvMessage() {
		return (ClientBasePacket)recvMsgQueue.get();
	}
	
	/** Scan outbound queue.
	 * If a client's channel if free (the client has no
	 * write buffer attached, with unfinished message), then
	 * remove a message from queue, encode it into network
	 * packet and send.
	 * If message was not sent completly - store write
	 * buffer in client's object, and set write interest for Selector.
	 * 
	 * @param selector
	 */
	private void processOutboudQueue() {
		if (sendMsgQueue.isEmpty())
			return;
		Iterator<BasePacket> iter = sendMsgQueue.iterator();
		while (iter.hasNext())
		{
			ServerBasePacket msg = (ServerBasePacket)iter.next();
			if (msg.getLifeTime() > 0)
			    if (System.currentTimeMillis()-msg.getTime()>msg.getLifeTime())
			    {
				iter.remove();
				continue;
			    }
				
			
			Connection con = msg.getConnection();
			SelectionKey sk = con.getChannel().keyFor(_selector);
			if (sk == null || !sk.isValid())
				continue; // drop message
			iter.remove(); // remove the message
			if (Config.ASSERT) assert (sk.interestOps() & SelectionKey.OP_READ) != 0;
			if (con.writeBuffer != null) {
				if (Config.ASSERT) assert (sk.interestOps() & SelectionKey.OP_WRITE) != 0;
				// move the message into client's queue
				con._sendMsgQueue.put(msg);
				continue;
			}
			boolean ok = pack(msg); // packs into shared writeBuffer
			if (!ok)
				continue;
			int r = 0;
			try {
				// try to write
				r = con.getChannel().write(sharedWriteBuffer);
			} catch (IOException e) {
				r = -1;
			}

			if (r < 0 || !sk.isValid()) {
				closeClient(con);
				return;
			}
			if (sharedWriteBuffer.hasRemaining()) {
				// move remaining data in buffer to connection
				ByteBuffer b = allocateBuffer(sharedWriteBuffer.remaining());
				b.put(sharedWriteBuffer);
				b.flip();
				con.writeBuffer = b;
				con.writeTimeStamp = GameTimeController.getGameTicks();
				if(sk.isValid()) sk.interestOps(sk.interestOps() | SelectionKey.OP_WRITE); 
				else
				{
					return;	// selection key invalit, what should we do ?  just return ?
				}
			}
			else
			{
				// take first message from connection
				if (!con._sendMsgQueue.isEmpty())
				{
					msg = (ServerBasePacket)con._sendMsgQueue.get();
					if (msg != null)
						sendMsgQueue.put(msg);
				}
			}
		}
	}
	
	/** A helper method to close client if connection was
	 * closed or error occured. Frees all buffers, close
	 * connections and so on.
	 * 
	 * @param c a client object
	 */
	protected void closeClient(Connection c, boolean Forced) {
		if (c.readBuffer != null) {
			releaseBuffer(c.readBuffer);
			c.readBuffer = null;
		}
		if (c.writeBuffer != null) {
			releaseBuffer(c.writeBuffer);
			c.writeBuffer = null;
		}
        L2PcInstance player = c.getClient().getActiveChar();
        if(player != null)
        {
            player.setConnected(false);
            if(!Forced)
            {
                //check if player is fighthing
                    if(AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
                    {
                        c.onlyClose();
                        return;
                    }
            }
        }
        try { c.close(); } catch (Exception dummy) {}
	}
    
    private void closeClient(Connection c) {
        if (c.readBuffer != null) {
            releaseBuffer(c.readBuffer);
            c.readBuffer = null;
        }
        if (c.writeBuffer != null) {
            releaseBuffer(c.writeBuffer);
            c.writeBuffer = null;
        }
        //check if player is fighting
        L2PcInstance player = c.getClient().getActiveChar();
        if(player != null)
        {
            player.setConnected(false);
            if(AttackStanceTaskManager.getInstance().getAttackStanceTask(player))
            {
                c.onlyClose();
                ThreadPoolManager.getInstance().scheduleGeneral(new DisconnectionTask(c), 15000);
                return;
            }
        }
        try { c.close(); } catch (Exception dummy) {}
    }
    
    /**  
	* @param msg  
	*/  
	private final void updateCounter(ServerBasePacket msg)  
	{  
		synchronized (packetCount)  
		{  
            Long count  = packetCount.get(msg.getClass());
            Long bytes  = byteCount.get(msg.getClass());

            if (count == null)  
            {  
                count   = new Long(1);
            }
            else
            {  
                count++;  
            }

            if (bytes == null)  
            {  
                bytes   = new Long(msg.getLength());
            }
            else
            {  
                bytes   += msg.getLength();  
            }

            packetCount.put(msg.getClass(), count);
            byteCount.put(msg.getClass(), bytes);
		} 
	}  
    
    public static final void startPacketMonitor()
    {
        if (Config.COUNT_PACKETS && packetMonitor == null)
        {
            packetCount.clear();
            byteCount.clear();
            packetHistory.clear();
            byteHistory.clear();
            
            packetMonitor   = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(
                new Runnable()
                {
                    public void run()
                    {
                        updateHistory();
                    }
                }
            , Config.DUMP_INTERVAL_SECONDS * 1000, Config.DUMP_INTERVAL_SECONDS * 1000);
        }
    }
    
    public static final void stopPacketMonitor()
    {
        if (Config.COUNT_PACKETS && packetMonitor != null)
        {
            synchronized (packetCount)
            {
                packetMonitor.cancel(true);
            }
            
            dumpPacketHistory();
        }
    }
    
    public static final void dumpPacketHistory()
    {
        if (Config.DUMP_PACKET_COUNTS)
        {
            doPacketHistoryDump();
        }
    }

    /**
     * 
     */
    private static final void doPacketHistoryDump()
    {
        ObjectWriter<List<PacketHistory>> ow = new ObjectWriter<List<PacketHistory>>();
        try
        {
            synchronized (packetHistory)
            {
                long currentTimeMillis = System.currentTimeMillis();
                
                ow.write(packetHistory, new FileOutputStream("log/packetCount_"+currentTimeMillis+".xml"));
                ow.write(byteHistory, new FileOutputStream("log/packetBytes_"+currentTimeMillis+".xml"));
                
                packetHistory.clear();
                byteHistory.clear();
            }
        }
        catch (Exception e)
        {
            _log.info("Packet Dump was unsucessfull.");
            _log.info(e.getMessage());
        }
    }
    
    protected static final void updateHistory()
    {
        if (Config.COUNT_PACKETS)
        {
            doUpdateHistory();
        }
    }
    
    protected static final void doUpdateHistory()
    {
        synchronized (packetCount)
        {
            long timestamp = System.nanoTime();
            
            PacketHistory newCountHistory   = new PacketHistory();
            newCountHistory.info            = packetCount;
            newCountHistory.timeStamp       = timestamp;
            
            PacketHistory newByteHistory    = new PacketHistory();
            newByteHistory.info             = byteCount;
            newByteHistory.timeStamp        = timestamp;
            
            synchronized (packetHistory)
            {
                packetHistory.add(newCountHistory);
                byteHistory.add(newByteHistory);
            }

            packetCount = new FastMap<Class, Long>();
            byteCount   = new FastMap<Class, Long>();
        }
    }
    
    /**
     * @return
     */
    public int inboundQueueSize()
    {
        return recvMsgQueue.size();
    }
    public int outboundQueueSize()
    {
        return sendMsgQueue.size();
    }
}
