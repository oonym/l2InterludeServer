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
package net.sf.l2j.loginserver;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * This class ...
 * 
 * @version $Revision: 1.3.4.1 $ $Date: 2005/03/27 15:30:09 $
 */
public class NewCrypt
{
	protected static Logger _log = Logger.getLogger(NewCrypt.class.getName());
	BlowfishEngine _crypt;
	BlowfishEngine _decrypt;
	
	public NewCrypt(String key)
	{
		byte[] keybytes = key.getBytes();
		_crypt = new BlowfishEngine();
		_crypt.init(true, keybytes);
		_decrypt = new BlowfishEngine();
		_decrypt.init(false, keybytes);
	}
	
	/**
	 * @param blowfishKey
	 */
	public NewCrypt(byte[] blowfishKey)
	{
		_crypt = new BlowfishEngine();
		_crypt.init(true, blowfishKey);
		_decrypt = new BlowfishEngine();
		_decrypt.init(false, blowfishKey);
	}

	public boolean checksum(byte[] raw)
	{
		long chksum = 0;
		int count = raw.length-4;
		long ecx = -1; //avoids ecs beeing == chksum if an error occured in the try
		int i =0;
		try
        {
			for (i=0; i<count; i+=4)
			{
				ecx = raw[i] &0xff;
				ecx |= raw[i+1] << 8 &0xff00;
				ecx |= raw[i+2] << 0x10 &0xff0000;
				ecx |= raw[i+3] << 0x18 &0xff000000;
				
				chksum ^= ecx;
			}
	
			ecx = raw[i] &0xff;
			ecx |= raw[i+1] << 8 &0xff00;
			ecx |= raw[i+2] << 0x10 &0xff0000;
			ecx |= raw[i+3] << 0x18 &0xff000000;
	
			raw[i] = (byte) (chksum &0xff);
			raw[i+1] = (byte) (chksum >>0x08 &0xff);
			raw[i+2] = (byte) (chksum >>0x10 &0xff);
			raw[i+3] = (byte) (chksum >>0x18 &0xff);
        }
		catch (ArrayIndexOutOfBoundsException aioobe)
		{
			//Looks like this will only happen on incoming packets as outgoing ones are padded
			//and the error can only happen in last raw[i] =, raw [i+1] = ... and it doesnt really matters for incomming packets
		}

		return ecx == chksum;	
	}
	

	public byte[] decrypt(byte[] raw) throws IOException
	{
		byte[] result = new byte[raw.length];
		int count = raw.length /8;

		for (int i=0; i<count;i++)
		{
			_decrypt.processBlock(raw,i*8,result,i*8);
		}

		return result;
	}
	
	public byte[] crypt(byte[] raw) throws IOException
	{
		int count = raw.length /8;
		byte[] result = new byte[raw.length];

		for (int i=0; i<count;i++)
		{
			_crypt.processBlock(raw,i*8,result,i*8);
		}
		
		return result;
	}
}
