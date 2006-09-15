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

import java.util.logging.Logger;

import net.sf.l2j.Config;

/**
 * Fromat: d
 * d: response
 */
public class GGAuth extends ServerBasePacket

{
    static Logger _log = Logger.getLogger(GGAuth.class.getName());
    public static int SKIP_GG_AUTH_REQUEST = 0x0b;
    
    public GGAuth(int response) 
    {
        writeC(0x0b);
        writeD(response); 
        if (Config.DEBUG) 
            _log.warning("Reason " + "Hex: "+(Integer.toHexString(response)));
    }
    
    public byte[] getContent()
    {
        return getBytes();
    }
}

