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
package net.sf.l2j.loginserver.serverpackets;

/**
 * Format: ddb
 * d: session id
 * d: protocol revision
 * b: 0x90 bytes : 0x80 bytes for the scrambled RSA public key
 *                 0x10 bytes at 0x00
 */
public class Init
{
	// format	ddb

	private static byte[] _content = //new byte[0x9b];
    {
        (byte)0x00, 
        (byte)0xfd, (byte)0x8a, (byte)0x22, (byte)0x00,  // session id 
        (byte)0x5a, (byte)0x78, (byte)0x00, (byte)0x00,  // protocol revision  0x785a or 0xc621, c621 has additional gameguard authentication 
        // dummy public key
        (byte)0x0e, (byte)0xea, (byte)0x0b, (byte)0xf3, (byte)0x3a, (byte)0x65, (byte)0xc6, (byte)0xc4, 
        (byte)0x62, (byte)0xc7, (byte)0x77, (byte)0x2e, (byte)0x95, (byte)0xde, (byte)0xbc, (byte)0x8c, 
        (byte)0xe0, (byte)0xf1, (byte)0xc9, (byte)0x87, (byte)0xcb, (byte)0x5f, (byte)0xe5, (byte)0x0e, 
        (byte)0x85, (byte)0xa6, (byte)0xf4, (byte)0xac, (byte)0x49, (byte)0xb6, (byte)0x29, (byte)0xe3, 
        (byte)0xa5, (byte)0x11, (byte)0xbe, (byte)0x85, (byte)0x5d, (byte)0x4c, (byte)0x2a, (byte)0x87, 
        (byte)0x0d, (byte)0xd5, (byte)0x17, (byte)0x48, (byte)0x87, (byte)0x0a, (byte)0xd4, (byte)0xa8, 
        (byte)0x9b, (byte)0x9b, (byte)0x8b, (byte)0x0f, (byte)0xad, (byte)0xa3, (byte)0x4d, (byte)0x60, 
        (byte)0x23, (byte)0x6f, (byte)0x2c, (byte)0x53, (byte)0xcc, (byte)0xfb, (byte)0x90, (byte)0xea, 
        (byte)0xa2, (byte)0x91, (byte)0x24, (byte)0x0e, (byte)0x55, (byte)0x6b, (byte)0xb7, (byte)0xb6, 
        (byte)0x6e, (byte)0x30, (byte)0x26, (byte)0x7f, (byte)0xf9, (byte)0x49, (byte)0xd8, (byte)0xb2, 
        (byte)0x2a, (byte)0x47, (byte)0x17, (byte)0xce, (byte)0xd7, (byte)0x10, (byte)0xfc, (byte)0x7d, 
        (byte)0x6f, (byte)0xbc, (byte)0x83, (byte)0xb4, (byte)0xd4, (byte)0x53, (byte)0x04, (byte)0x6e, 
        (byte)0x08, (byte)0x14, (byte)0x7b, (byte)0x92, (byte)0xca, (byte)0xb1, (byte)0x52, (byte)0x55, 
        (byte)0xf7, (byte)0x45, (byte)0x4c, (byte)0xaa, (byte)0xe9, (byte)0xb0, (byte)0x01, (byte)0x1e, 
        (byte)0xac, (byte)0xe2, (byte)0x9b, (byte)0x68, (byte)0x21, (byte)0x29, (byte)0x68, (byte)0x21, 
        (byte)0xe1, (byte)0x93, (byte)0x70, (byte)0xbd, (byte)0x3f, (byte)0x13, (byte)0x16, (byte)0xab,
        // not sure what these are for
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00
     };

    private static boolean _keySet;

    public Init(byte[] publickey)
    {
        if (!_keySet)
        {
            for (int i = 0; i < publickey.length; i++)
            {
                _content[9+i] = publickey[i];
            }
        }
    }
	
	public byte[] getContent()
	{
		return _content;
	}
	
	public int getLength()
	{
		return _content.length+2;
	}
}
