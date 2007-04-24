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
package net.sf.l2j.gameserver.geoeditorcon;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

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
