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


import javolution.text.TextBuilder;

/**
 * @author KenM
 *
 */
public abstract class ReceivablePacket<T extends MMOClient> extends AbstractPacket<T> implements Runnable
{
    protected ReceivablePacket()
    {
        
    }
    
    protected int getAvaliableBytes()
    {
        return this.getByteBuffer().remaining();
    }
    
    protected abstract boolean read();
    
    public abstract void run();
    
    protected void readB(byte[] dst)
    {
        this.getByteBuffer().get(dst);
    }
    
    protected void readB(byte[] dst, int offset, int len)
    {
        this.getByteBuffer().get(dst, offset, len);
    }
    
    protected int readC()
    {
        return this.getByteBuffer().get() & 0xFF;
    }
    
    protected int readH()
    {
        return this.getByteBuffer().getShort() & 0xFFFF;
    }
    
    protected int readD()
    {
        return this.getByteBuffer().getInt();
    }
    
    protected long readQ()
    {
        return this.getByteBuffer().getLong();
    }
    
    protected double readF()
    {
        return this.getByteBuffer().getDouble();
    }
    
    protected String readS()
    {
        TextBuilder tb = TextBuilder.newInstance();
        char ch;
        
        while ((ch = this.getByteBuffer().getChar()) != 0)
        {
            tb.append(ch);
        }
        String str = tb.stringValue();
        TextBuilder.recycle(tb);
        return str;
    }
}
