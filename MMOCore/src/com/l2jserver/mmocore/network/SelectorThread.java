/* This program is free software; you can redistribute it and/or modify
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
package com.l2jserver.mmocore.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.l2jserver.mmocore.network.SelectorConfig.HeaderSize;


import javolution.util.FastList;

/**
 * @author KenM<BR>
 * Parts of design based on networkcore from WoodenGil
 */
public class SelectorThread<T extends MMOClient> extends Thread
{
    private Selector _selector;
    
    // Implementations
    private IPacketHandler<T> _packetHandler;
    private IMMOExecutor<T> _executor;
    private IClientFactory<T> _clientFactory;
    private IAcceptFilter _acceptFilter;
    
    private boolean _shutdown;

    // Pending Close
    private FastList<MMOConnection<T>> _pendingClose = new FastList<MMOConnection<T>>();
    
    // Configs
    private InetAddress ADDRESS;
    private int PORT;
    private int HELPER_BUFFER_SIZE;
    private int HELPER_BUFFER_COUNT;
    private int HEADER_SIZE = 2;
    private ByteOrder BYTE_ORDER;
    private HeaderSize HEADER_TYPE;

    // MAIN BUFFERS
    private ByteBuffer WRITE_BUFFER;
    private ByteBuffer READ_BUFFER;

    // ByteBuffers General Purpose Pool
    private final FastList<ByteBuffer> _bufferPool = new FastList<ByteBuffer>();

    public SelectorThread(SelectorConfig sc, IPacketHandler<T> packetHandler, IMMOExecutor<T> executor) throws IOException
    {
        this.readConfig(sc);

        this.initBufferPool();
        this.setPacketHandler(packetHandler);
        this.setExecutor(executor);
        this.initializeSelector();
    }

    public SelectorThread(SelectorServerConfig ssc, IPacketHandler<T> packetHandler, IClientFactory<T> clientFactory, IMMOExecutor<T> executor) throws IOException
    {
        this.readConfig(ssc);
        
        PORT = ssc.getPort();
        ADDRESS = ssc.getAddress();
        
        this.initBufferPool();
        this.setPacketHandler(packetHandler);
        this.setClientFactory(clientFactory);
        this.setExecutor(executor);
        this.initializeSelector();
    }

    protected void readConfig(SelectorConfig sc)
    {
        HELPER_BUFFER_SIZE = sc.getHelperBufferSize();
        HELPER_BUFFER_COUNT = sc.getHelperBufferCount();
        BYTE_ORDER = sc.getByteOrder();

        WRITE_BUFFER = ByteBuffer.wrap(new byte[sc.getWriteBufferSize()]).order(BYTE_ORDER);
        READ_BUFFER = ByteBuffer.wrap(new byte[sc.getReadBufferSize()]).order(BYTE_ORDER);

        HEADER_TYPE = sc.getHeaderType();
    }

    protected void initBufferPool()
    {
        for (int i = 0; i < HELPER_BUFFER_COUNT; i++)
        {
            this.getFreeBuffers().addLast(ByteBuffer.wrap(new byte[HELPER_BUFFER_SIZE]).order(BYTE_ORDER));
        }
    }

    public void openServerSocket() throws IOException
    {

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);

        ServerSocket ss = ssc.socket();
        if (ADDRESS == null)
        {
            ss.bind(new InetSocketAddress(PORT));
        }
        else
        {
            ss.bind(new InetSocketAddress(ADDRESS, PORT));
        }

        ssc.register(this.getSelector(), SelectionKey.OP_ACCEPT);
    }

    protected void initializeSelector() throws IOException
    {
        this.setName("SelectorThread-"+this.getId());
        this.setSelector(Selector.open());
    }

    public ByteBuffer getPooledBuffer()
    {
        if (this.getFreeBuffers().isEmpty())
        {
            return ByteBuffer.wrap(new byte[HELPER_BUFFER_SIZE]).order(BYTE_ORDER);
        }
        else
        {
            return this.getFreeBuffers().removeFirst();
        }
    }

    public void recycleBuffer(ByteBuffer buf)
    {
        if (this.getFreeBuffers().size() < HELPER_BUFFER_COUNT)
        {
            buf.clear();
            this.getFreeBuffers().addLast(buf);
        }
    }

    public FastList<ByteBuffer> getFreeBuffers()
    {
        return _bufferPool;
    }
    
    public SelectionKey registerClientSocket(SocketChannel sc, int interestOps) throws ClosedChannelException
    {
        SelectionKey sk = null;
        
        sk = sc.register(this.getSelector(), interestOps);

        return sk;
    }

    public void run()
    {
        System.out.println("Selector Started");
        int totalKeys = 0;
        Iterator<SelectionKey> iter;
        SelectionKey key;
        MMOConnection<T> con;
        FastList.Node<MMOConnection<T>> n, end, temp;
        
        // main loop
        for (;;)
        {
            // check for shutdown
            if (this.isShuttingDown())
            {
                this.closeSelectorThread();
                break;
            }

            try
            {
                totalKeys = this.getSelector().selectNow();
            }
            catch (IOException e)
            {
                //TODO logging
                e.printStackTrace();
            }
            //System.out.println("Selector Selected "+totalKeys);

            if (totalKeys > 0)
            {
                Set<SelectionKey> keys = this.getSelector().selectedKeys();
                iter = keys.iterator();



                while (iter.hasNext())
                {
                    key = iter.next();
                    iter.remove();

                    switch (key.readyOps())
                    {
                        case SelectionKey.OP_CONNECT:
                            this.finishConnection(key);
                            break;
                        case SelectionKey.OP_ACCEPT:
                            this.acceptConnection(key);
                            break;
                        case SelectionKey.OP_READ:
                            this.readPacket(key);
                            break;
                        case SelectionKey.OP_WRITE:
                            this.writePacket(key);
                            break;
                        case SelectionKey.OP_READ | SelectionKey.OP_WRITE:
                            this.writePacket(key);
                            // key might have been invalidated on writePacket
                            if (key.isValid())
                            {
                                this.readPacket(key);
                            }
                            break;
                    }
                }
            }
            
            // process pending close
            for (n = this.getPendingClose().head(), end = this.getPendingClose().tail(); (n = n.getNext()) != end;)
            {
                con = n.getValue();
                if (con.getSendQueue().isEmpty())
                {
                    temp = n.getPrevious();
                    this.getPendingClose().delete(n);
                    n = temp;
                    this.closeConnectionImpl(con);
                }
            }
            
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }


    }

    @SuppressWarnings("unchecked")
    protected void finishConnection(SelectionKey key)
    {
        try
        {
            ((SocketChannel) key.channel()).finishConnect();
        }
        catch (IOException e)
        {
            T client = (T) key.attachment();
            client.getConnection().onForcedDisconnection();
            this.closeConnectionImpl(client.getConnection());
        }

        // key might have been invalidated on finishConnect()
        if (key.isValid())
        {
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
            key.interestOps(key.interestOps() & ~SelectionKey.OP_CONNECT);
        }
    }

    protected void acceptConnection(SelectionKey key)
    {
        SocketChannel sc;
        try
        {
            while ((sc = ((ServerSocketChannel) key.channel()).accept()) != null)
            {
                if (this.getAcceptFilter() == null || this.getAcceptFilter().accept(sc))
                {
                    sc.configureBlocking(false);
                    SelectionKey clientKey = sc.register(this.getSelector(), SelectionKey.OP_READ /*| SelectionKey.OP_WRITE*/);
                    clientKey.attach(this.getClientFactory().create(new MMOConnection<T>(this, sc, clientKey)));
                }
                else
                {
                    key.cancel();
                    sc.socket().close();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    protected void readPacket(SelectionKey key)
    {
        T client = (T) key.attachment();
        MMOConnection con = client.getConnection();

        ByteBuffer buf;
        if ((buf = con.getReadBuffer()) == null)
        {
            buf = READ_BUFFER;
        }
        else
        {
            //buf.limit(buf.capacity());
            //buf.position()
            //System.out.println("TEM BUF PENDENTE LIMIT: "+buf.limit());
        }
        int result = -2;
        
        // if we try to to do a read with no space in the buffer it will read 0 bytes
        // going into infinite loop
        if (buf.position() == buf.limit())
        {
            // should never happen
            System.out.println("POS ANTES SC.READ(): "+buf.position()+" limit: "+buf.limit());
            System.out.println("NOOBISH ERROR "+( buf == READ_BUFFER ? "READ_BUFFER" : "temp"));
            System.exit(0);
        }

        try
        {
            result = con.getSocketChannel().read(buf);
        }
        catch (IOException e)
        {
            //error handling goes bellow
        }
        
        //System.out.println("LEU: "+result+" pos: "+buf.position());
        if (result > 0)
        {
            // TODO this should be done vefore even reading
            if (!con.isClosed())
            {
                buf.flip();
                // try to read as many packets as possible
                while (this.tryReadPacket(client, buf));
            }
        }
        else if (result == 0)
        {
            // read interest but nothing to read? wtf?
            System.out.println("NOOBISH ERROR 2 THE MISSION");
            System.exit(0);
        }
        else if (result == -1)
        {
            this.closeConnectionImpl(con);
        }
        else
        {
            con.onForcedDisconnection();
            this.closeConnectionImpl(con);
        }
    }

    protected boolean tryReadPacket(T client, ByteBuffer buf)
    {
        MMOConnection con = client.getConnection();
        //System.out.println("BUFF POS ANTES DE LER: "+buf.position()+" - REMAINING: "+buf.remaining());

        if (buf.hasRemaining())
        {
            int result = buf.remaining();

            // then check if there are enough bytes for the header
            if (result >= HEADER_SIZE)
            {
                // then read header and check if we have the whole packet
                int size = this.getHeaderValue(buf);
                //System.out.println("IF: ("+size+" <= "+result+") => (size <= result)");
                if (size <= result)
                {
                    //System.out.println("BOA");

                    // avoid parsing dummy packets (packets without body)
                    if (size > HEADER_SIZE)
                    {
                        this.parseClientPacket(buf, size, client);
                    }

                    // if we are done with this buffer
                    if (!buf.hasRemaining())
                    {
                        //System.out.println("BOA 2");
                        if (buf != READ_BUFFER)
                        {
                            con.setReadBuffer(null);
                            this.recycleBuffer(buf);
                        }
                        else
                        {
                            READ_BUFFER.clear();
                        }

                        return false;
                    }
                    else
                    {
                        // do nothing
                    }

                    return true;
                }
                else
                {
                    //System.out.println("ENABLEI");
                    client.getConnection().enableReadInterest();
                    
                    //System.out.println("LIMIT "+buf.limit());
                    if (buf == READ_BUFFER)
                    {
                        buf.position(buf.position() - HEADER_SIZE);
                        this.allocateReadBuffer(con);
                    }
                    else
                    {
                        buf.position(buf.position() - HEADER_SIZE);
                        buf.compact();
                    }
                    return false;
                }
            }
            else
            {
                if (buf == READ_BUFFER)
                {
                    this.allocateReadBuffer(con);
                }
                return false;
            }
        }
        else
        {
            //con.disableReadInterest();
            return false; //empty buffer
        }
    }

    protected void allocateReadBuffer(MMOConnection con)
    {
        //System.out.println("con: "+Integer.toHexString(con.hashCode()));
        //Util.printHexDump(READ_BUFFER);
        con.setReadBuffer(this.getPooledBuffer().put(READ_BUFFER));
        READ_BUFFER.clear();
    }

    protected void parseClientPacket(ByteBuffer buf, int size, T client)
    {
        int pos = buf.position();
        
        boolean ret = client.decrypt(buf, size - HEADER_SIZE);
        
        buf.position(pos);
       
        if (buf.hasRemaining() && ret)
        {
            //  apply limit
            int limit = buf.limit();
            buf.limit(pos + size - HEADER_SIZE);
            ReceivablePacket<T> cp = this.getPacketHandler().handlePacket(buf, client);

            if (cp != null)
            {
                cp.setByteBuffer(buf);
                cp.setClient(client);
                
                if (cp.read())
                {
                    this.getExecutor().execute(cp);
                }
            }
            buf.limit(limit);
        }
        buf.position(pos + size - HEADER_SIZE);
    }

    @SuppressWarnings("unchecked")
    protected void writePacket(SelectionKey key)
    {
        T client = ((T) key.attachment());
        MMOConnection<T> con = client.getConnection();

        ByteBuffer buf;
        boolean sharedBuffer = false;
        if ((buf = con.getWriteBuffer()) == null)
        {
            SendablePacket<T> sp = null;
            synchronized (con.getSendQueue())
            {
                if (!con.getSendQueue().isEmpty())
                {
                    sp = con.getSendQueue().removeFirst();
                }
            }
            if (sp == null)
            {
                System.out.println("OMG WRITE BUT NO WRITE");
                return;
            }
            //System.out.println("WRITING: "+sp.getClass().getSimpleName());
            this.prepareWriteBuffer(client, sp);
            buf = sp.getByteBuffer();
            //System.out.println("WRITED:: "+sp.getClass().getSimpleName());
            //System.out.println("wp:" +buf.position());
            buf.flip();
            
            sharedBuffer = true;
        }

        int size = buf.remaining();
        int result = -1;

        try
        {
            result = con.getSocketChannel().write(buf);
        }
        catch (IOException e)
        {
            // error handling goes on the if bellow
        }

        // check if no error happened
        if (result > 0)
        {
            // check if we writed everything
            if (result == size)
            {
                //System.out.println("WRITEI COMPLETO");
                synchronized (con.getSendQueue())
                {
                    if (con.getSendQueue().isEmpty())
                    {
                        con.disableWriteInterest();
                    }
                }

                // if it was a pooled buffer we can recycle
                if (!sharedBuffer)
                {
                    this.recycleBuffer(buf);
                    con.setWriteBuffer(null);
                }
            }
            else //incomplete write
            {
                //System.out.println("WRITEI INCOMPLETO");
                // if its the main buffer allocate a pool one
                if (sharedBuffer)
                {
                    con.setWriteBuffer(this.getPooledBuffer().put(buf));
                }
                else
                {
                    con.setWriteBuffer(buf);
                }
            }
        }
        else
        {
            con.onForcedDisconnection();
            this.closeConnectionImpl(con);
        }
    }

    protected void prepareWriteBuffer(T client, SendablePacket<T> sp)
    {
        WRITE_BUFFER.clear();

        //set the write buffer
        sp.setByteBuffer(WRITE_BUFFER);

        // reserve space for the size
        int headerPos = sp.getByteBuffer().position();
        sp.getByteBuffer().position(headerPos + HEADER_SIZE);

        // write contents
        sp.write();
        

        int size = sp.getByteBuffer().position() - headerPos - HEADER_SIZE;
        sp.getByteBuffer().position(headerPos + HEADER_SIZE);
        client.encrypt(sp.getByteBuffer(), size);
        
        // write size
        sp.writeHeader(HEADER_TYPE, headerPos);
    }

    protected int getHeaderValue(ByteBuffer buf)
    {
        switch (HEADER_TYPE)
        {
            case BYTE_HEADER:
                return buf.get() & 0xFF;
            case SHORT_HEADER:
                return buf.getShort() & 0xFFFF;
            case INT_HEADER:
                return buf.getInt();
        }
        return -1; // O.o
    }

    protected void setSelector(Selector selector)
    {
        _selector = selector;
    }

    public Selector getSelector()
    {
        return _selector;
    }

    protected void setExecutor(IMMOExecutor<T> executor)
    {
        _executor = executor;
    }

    protected IMMOExecutor<T> getExecutor()
    {
        return _executor;
    }

    protected void setPacketHandler(IPacketHandler<T> packetHandler)
    {
        _packetHandler = packetHandler;
    }

    public IPacketHandler<T> getPacketHandler()
    {
        return _packetHandler;
    }

    protected void setClientFactory(IClientFactory<T> clientFactory)
    {
        _clientFactory = clientFactory;
    }

    public IClientFactory<T> getClientFactory()
    {
        return _clientFactory;
    }

    public void setAcceptFilter(IAcceptFilter acceptFilter)
    {
        _acceptFilter = acceptFilter;
    }
    
    public IAcceptFilter getAcceptFilter()
    {
        return _acceptFilter;
    }
    
    public void closeConnection(MMOConnection<T> con)
    {
        synchronized (this.getPendingClose())
        {
            this.getPendingClose().addLast(con);
        }
    }
    
    protected void closeConnectionImpl(MMOConnection<T> con)
    {
        // notify connection
        con.onDisconection();
        
        try
        {
            // close socket and the SocketChannel
            con.getSocketChannel().socket().close();
        }
        catch (IOException e)
        {
            // ignore, we are closing anyway
        }
        
        // clear attachment
        con.getSelectionKey().attach(null);
        // cancel key
        con.getSelectionKey().cancel();
    }
    
    protected FastList<MMOConnection<T>> getPendingClose()
    {
        return _pendingClose;
    }
    
    public void shutdown()
    {
        _shutdown = true;
    }

    public boolean isShuttingDown()
    {
        return _shutdown;
    }

    protected void closeAllChannels()
    {
        Set<SelectionKey> keys = this.getSelector().keys();
        for (Iterator<SelectionKey> iter = keys.iterator(); iter.hasNext();)
        {
            SelectionKey key = iter.next();
            try
            {
                key.channel().close();
            }
            catch (IOException e)
            {
                // ignore
            }
        }
    }

    protected void closeSelectorThread() 
    {
        this.closeAllChannels();
        try
        {
            this.getSelector().close();
        }
        catch (IOException e)
        {
            // Ignore
        }
    }
}
