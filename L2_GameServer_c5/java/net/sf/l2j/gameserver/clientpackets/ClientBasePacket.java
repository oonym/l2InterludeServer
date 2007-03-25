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
package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.text.TextBuilder;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.BasePacket;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.4.9 $ $Date: 2005/03/27 15:29:30 $
 */
public abstract class ClientBasePacket extends BasePacket implements Runnable
{
	private static Logger _log = Logger.getLogger(ClientBasePacket.class.getName());
	
	protected ClientBasePacket(ByteBuffer buf, ClientThread client)
	{
		super(client);
		if (Config.DEBUG) _log.fine(getType()+" <<< "+client.getLoginName());
		_buf = buf;
		if (Config.ASSERT) assert _buf.position() == 1;
	}

	/** urgent messages are executed immediatly */
	public TaskPriority getPriority() { return TaskPriority.PR_NORMAL; }
	
	public final void run()
	{
		//assert _isValid;
		try
		{
//            if (Config.DEVELOPER) System.out.println(getType());
            runImpl();
            if (!(this instanceof ValidatePosition || this instanceof Appearing || this instanceof EnterWorld || this instanceof RequestPledgeInfo || this instanceof RequestSkillList || this instanceof RequestQuestList || getClient().getActiveChar() == null)) getClient().getActiveChar().onActionRequest();
		}
		catch (Throwable e)
		{
       L2PcInstance player = getClient().getActiveChar();
       if (player != null)
       {
       	  _log.log( Level.SEVERE, "Character "+player.getName()+" of account "+player.getAccountName()+" caused the following error at packet-handling: "+getType(), e);			
       }
       else
			   _log.log(Level.SEVERE, "error handling client message "+getType(), e);
		}
		
	}
	
	/**
	 * This is only called once per packet instane ie: when you construct a packet and send it to many players,
	 * it will only run when the first packet is sent
	 */
	abstract void runImpl();
	
	protected void sendPacket(ServerBasePacket msg)
	{
		getConnection().sendPacket(msg);
	}

	public final int readD()
	{
	    try {
		return _buf.getInt();
	    } catch (Exception e) {}
	    return 0;
	}

	public final int readC()
	{
	    try {
		return _buf.get() & 0xFF;
	    } catch (Exception e) {}
	    return 0;
	}

	public final int readH()
	{
	    try {
		return _buf.getShort() & 0xFFFF;
	    } catch (Exception e) {}
	    return 0;
	}

	public final double readF()
	{
	    try {
		return _buf.getDouble();
	    } catch (Exception e) {e.printStackTrace();}
	    return 0;
	}
    
    public final long readQ()
    {
        try {
            return _buf.getLong();
        } catch (Exception e) {e.printStackTrace();}
        return 0;
    }

	public final String readS()
	{
        TextBuilder sb = new TextBuilder();
		char ch;
	    try {
		while ((ch = _buf.getChar()) != 0)
			sb.append(ch);
	    } catch (Exception e) {}
		return sb.toString();
	}
	
	public final byte[] readB(int length)
	{
		byte[] result = new byte[length];
	    try {
		_buf.get(result);
	    } catch (Exception e) {}
		return result;
	}
}
