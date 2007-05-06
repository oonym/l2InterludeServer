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

import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


import javolution.util.FastList;

/**
 * @author KenM
 *
 */
public class MMOConnection<T extends MMOClient>
{
    private final SelectorThread<T> _selectorThread;
    private T _client;
    
    private SocketChannel _socketChannel;
    private FastList<SendablePacket<T>> _sendQueue = new FastList<SendablePacket<T>>();
    private SelectionKey _selectionKey;
    
    private ByteBuffer _readBuffer;
    private ByteBuffer _writeBuffer;
    
    private boolean _pendingClose;
    
    public MMOConnection(SelectorThread<T> selectorThread, SocketChannel sc, SelectionKey key)
    {
        _selectorThread = selectorThread;
        this.setSocketChannel(sc);
        this.setSelectionKey(key);
    }
    
    protected void setClient(T client)
    {
        _client = client;
    }
    
    protected T getClient()
    {
        return _client;
    }
    
    public void sendPacket(SendablePacket<T> sp)
    {
        sp.setClient(this.getClient());
        synchronized (this.getSendQueue())
        {
            if (!_pendingClose)
            {
                try
                {
                    this.getSelectionKey().interestOps(this.getSelectionKey().interestOps() | SelectionKey.OP_WRITE);
                    this.getSendQueue().addLast(sp);
                }
                catch (CancelledKeyException e)
                {
                    // ignore
                }
            }
        }
    }
    
    protected SelectorThread<T> getSelectorThread()
    {
        return _selectorThread;
    }
    
    protected void setSelectionKey(SelectionKey key)
    {
        _selectionKey = key;
    }
    
    protected SelectionKey getSelectionKey()
    {
        return _selectionKey;
    }
    
    protected void enableReadInterest()
    {
        try
        {
            this.getSelectionKey().interestOps(this.getSelectionKey().interestOps() | SelectionKey.OP_READ);
        }
        catch (CancelledKeyException e)
        {
            // ignore
        }
    }
    
    protected void disableReadInterest()
    {
        try
        {
            this.getSelectionKey().interestOps(this.getSelectionKey().interestOps() & ~SelectionKey.OP_READ);
        }
        catch (CancelledKeyException e)
        {
            // ignore
        }
    }
    
    protected void enableWriteInterest()
    {
        try
        {
            this.getSelectionKey().interestOps(this.getSelectionKey().interestOps() | SelectionKey.OP_WRITE);
        }
        catch (CancelledKeyException e)
        {
            // ignore
        }
    }
    
    protected void disableWriteInterest()
    {
        try
        {
            this.getSelectionKey().interestOps(this.getSelectionKey().interestOps() & ~SelectionKey.OP_WRITE);
        }
        catch (CancelledKeyException e)
        {
            // ignore
        }
    }
    
    protected void setSocketChannel(SocketChannel sc)
    {
        _socketChannel = sc;
    }
    
    public SocketChannel getSocketChannel()
    {
        return _socketChannel;
    }
    
    protected FastList<SendablePacket<T>> getSendQueue()
    {
        return _sendQueue;
    }
    
    protected void setWriteBuffer(ByteBuffer buf)
    {
        _writeBuffer = buf;
    }
    
    protected ByteBuffer getWriteBuffer()
    {
        return _writeBuffer;
    }
    
    protected void setReadBuffer(ByteBuffer buf)
    {
        _readBuffer = buf;
    }
    
    protected ByteBuffer getReadBuffer()
    {
        return _readBuffer;
    }
    
    public boolean isClosed()
    {
        return _pendingClose;
    }
    
    protected void closeNow()
    {
        synchronized (this.getSendQueue())
        {
            _pendingClose = true;
            this.getSendQueue().clear();
            this.disableWriteInterest();
        }
        this.getSelectorThread().closeConnection(this);
    }
    
    public void close(SendablePacket<T> sp)
    {
        synchronized (this.getSendQueue())
        {
            this.getSendQueue().clear();
            this.sendPacket(sp);
            _pendingClose = true;
        }
        this.getSelectorThread().closeConnection(this);
    }
    
    protected void closeLater()
    {
        synchronized (this.getSendQueue())
        {
            _pendingClose = true;
        }
        this.getSelectorThread().closeConnection(this);
    }
    
    protected void onDisconection()
    {
        this.getClient().onDisconection();
    }
    
    protected void onForcedDisconnection()
    {
        this.getClient().onForcedDisconnection();
    }
}
