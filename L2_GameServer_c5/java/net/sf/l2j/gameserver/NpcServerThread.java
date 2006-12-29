
package net.sf.l2j.gameserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.concurrent.*;

import net.sf.l2j.gameserver.npcserverpackets.PacketsQueue;
import net.sf.l2j.gameserver.npcserverpackets.GsBasePacket;;

/**
 * 
 *
 * @author  Luno
 */
public class NpcServerThread extends Thread
{
	private static NpcServerThread _instance;
	
	private ServerSocket _serverSocket;
	private Socket       _connection;
	private InputStream _in;
	private OutputStream  out;
	PacketsQueue _queue;
	
	public static NpcServerThread getInstance()
	{
		if(_instance == null)
			_instance = new NpcServerThread();
		return _instance;
	}
	private NpcServerThread()
	{
		_queue = new PacketsQueue();
	}
	public void run()
	{
		while(true)
		{
			try
			{
				_serverSocket = new ServerSocket(3100);

				while(true)
				{
					try
					{
						System.out.println("Listening for npc server.");
						_connection = _serverSocket.accept();
						
						System.out.println("Connection from "+_connection.getRemoteSocketAddress()+" "+_connection.getPort());
						
						out =  _connection.getOutputStream() ;

						while(true)
						{
	
							if(!_queue.isEmpty())
							{
								GsBasePacket packet = _queue.get();
								
								int length = packet.getLength();
								ByteBuffer b = packet.getData();
								byte[] data = new byte[length];
								for(int i = 0; i < length; i++)
									data[i] = b.get(i);
								
								out.write((length + 2)%256); // Size is set on 2 bytes
								out.write((length + 2)/256);
								
								out.write(data); // data contains packet type and content
								out.flush();
	
							}
							yield();
						}
					}
					catch(IOException e){System.out.println("Connection lost");}
				}
			}
			catch(IOException e)
			{
				System.out.println("Couldn't bind socket.");
			}
		}
	}
	public synchronized void addPacket(GsBasePacket packet)
	{
			_queue.add(packet);
	}
}