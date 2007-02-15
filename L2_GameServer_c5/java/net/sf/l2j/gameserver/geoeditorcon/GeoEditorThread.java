
package net.sf.l2j.gameserver.geoeditorcon;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * 
 *
 * @author  Luno
 */
public class GeoEditorThread extends Thread
{
	//How many times per second send packet
	int ticks = 2;
	
	boolean working = true;
	
	private int        _port;
	private String _hostname;
	
	private Socket _geSocket;
	
	private BufferedOutputStream _out;
    @SuppressWarnings("unused")
	private BufferedInputStream _in;
	
	private GeoEditorConnector _geCon;
	
	public GeoEditorThread(GeoEditorConnector ge)
	{

		_port = 2109;
		_hostname = "194.187.183.201";
		
		_geCon = ge;
	}
	public void run()
	{
		try
		{
			_geCon.sendMessage("... connecting to GeoEditor.");
			_geSocket = new Socket(_hostname, _port);
			_geCon.sendMessage("Connection established.");
			
			_in  = new BufferedInputStream(_geSocket.getInputStream());
			_out = new BufferedOutputStream(_geSocket.getOutputStream());
			
			while(working)
			{
				try
				{
					TimeUnit.MILLISECONDS.sleep(1000 / ticks);
				}catch(Exception e){}
				for(L2PcInstance gm: _geCon.getGMs())
					sendGmPosition(gm);
			}
			_geCon.stoppedConnection();
			_geCon.sendMessage("Connection with GeoEditor broken.");
		}
		catch (UnknownHostException e)
		{
			_geCon.stoppedConnection();
			_geCon.sendMessage("Couldn't connect to GeoEditor.");
		}
		catch (IOException e)
		{
			_geCon.stoppedConnection();
			_geCon.sendMessage("Connection with GeoEditor broken.");
		}
		finally
		{
			try { _geSocket.close(); } catch (Exception e) {}
		}
	}
	private void sendGmPosition(L2PcInstance _gm) throws IOException
	{
    	int gx = (_gm.getX() - L2World.MAP_MIN_X) >> 4;
    	int gy = (_gm.getY() - L2World.MAP_MIN_Y) >> 4;
    	
    	byte bx = (byte)((gx >> 3) % 256);
    	byte by = (byte)((gy >> 3) % 256);
    	
    	byte cx = (byte)(gx % 8);
    	byte cy = (byte)(gy % 8);
    	
    	short z = (short)(_gm.getZ());
    	
    	 // 6 bytes
    	_out.write(bx);
    	_out.write(by);
    	_out.write(cx);
    	_out.write(cy);
    	sendShort(z);
    	_out.flush();
	}
	private  void sendShort(short v) throws IOException
	{
		_out.write(v >> 8);
		_out.write( v & 0xFFFF);
	}
	
	public void stopRecording()
	{
		working = false;
	}
	public void setTicks(int t)
	{
		ticks = t;
		if(t < 1)
			t = 1;
		else if( t > 5)
			t = 5;
	}
}