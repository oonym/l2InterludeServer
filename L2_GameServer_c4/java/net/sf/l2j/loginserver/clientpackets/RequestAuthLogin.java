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
package net.sf.l2j.loginserver.clientpackets;

import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPrivateKey;

import javax.crypto.Cipher;

/**
 * Format: x
 * 0 (a leading null)
 * x: the rsa encrypted block with the login an password
 */
public class RequestAuthLogin
{
	private String _user;
	private String _password;
	
	/**
	 * @return
	 */
	public String getPassword()
	{
		return _password;
	}

	/**
	 * @return
	 */
	public String getUser()
	{
		return _user;
	}

	public RequestAuthLogin(byte[] rawPacket, RSAPrivateKey _key)
	{
        try
        {
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/nopadding");
            rsaCipher.init(Cipher.DECRYPT_MODE, _key);
            byte[] decrypted = rsaCipher.doFinal(rawPacket, 0x01, 0x80 );
            //System.out.println("RSA DECRYPTED");
            //System.out.println(printData(decrypted, decrypted.length));
            
            _user = new String(decrypted, 0x62, 14 ).trim();
            _user = _user.toLowerCase();
            _password = new String(decrypted, 0x70, 16).trim();
        }
        catch (GeneralSecurityException e)
        {
            e.printStackTrace();
        }
	}
}
