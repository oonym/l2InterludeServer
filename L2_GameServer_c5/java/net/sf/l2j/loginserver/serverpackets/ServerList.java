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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javolution.util.FastList;
import net.sf.l2j.loginserver.gameserverpackets.ServerStatus;

/**
 * ServerList
 * Format: cc [cddcchhcdc]
 * 
 * c: server list size (number of servers)
 * c: ?
 * [ (repeat for each servers)
 * c: server id (ignored by client?)
 * d: server ip
 * d: server port
 * c: age limit (used by client?)
 * c: pvp or not (used by client?)
 * h: current number of players
 * h: max number of players
 * c: 0 if server is down
 * d: 2nd bit: clock 
 *    3rd bit: wont dsiplay server name 
 *    4th bit: test server (used by client?)
 * c: 0 if you dont want to display brackets in front of sever name
 * ]
 * 
 * Server will be considered as Good when the number of  online players
 * is less than half the maximum. as Normal between half and 4/5
 * and Full when there's more than 4/5 of the maximum number of players
 */
public class ServerList extends ServerBasePacket
{
    // cc (cddcchhcd)

    // 04 
    // 10
    // 05

    // 01 
    // dc 4c 0c 33 
    // 61 1e 00 00 
    // 0f 
    // 01 
    // f7 0a 
    // 7c 15 
    // 01 

    // 02 
    // dc 4c 0c 69 
    // 61 1e 00 00 
    // 0f 
    // 01 
    // 3c 09
    // 7c 15
    // 01

    private List<ServerData> _servers;
    private boolean _listDone = false;

    //	private byte[] _content = {
    //		(byte)0x04,
    //		(byte)0x02,			// number of servers in list
    //		(byte)0x00,			// login server num ... seems to be unused
    //				
    //		(byte)0x01,									// server id
    //		(byte)0xc0,(byte)0xa8,(byte)0x00,(byte)0x01,   // 192.68.0.1
    //		
    //		(byte)0x61,(byte)0x1e,(byte)0x00,(byte)0x00,   // port 7777
    //		(byte)0x12,
    //		(byte)0x01,
    //		(byte)0xf7,(byte)0x0a,
    //		
    //		(byte)0x7c,(byte)0x15,
    //		(byte)0x01,
    //
    //		(byte)0x02,									// server id
    //		(byte)0xc0,(byte)0xa8,(byte)0x00,(byte)0x01,   // 192.68.0.1
    //		
    //		(byte)0x61,(byte)0x1e,(byte)0x00,(byte)0x00,   // port 7777
    //		(byte)0x0f,				// age limit
    //		(byte)0x00,				// pvp possible
    //		(byte)0x00,(byte)0x00,	// current player count
    //
    //		(byte)0x10,(byte)0x27,  // max players
    //		(byte)0x01,		// testing == 0
    //		
    //		0x00,0x00,0x00,0x00,	// align and checksum
    //		0x00,
    //		
    //		0x00,0x00,0x00,
    //		0x00,0x00,0x00,0x00,
    //		0x00
    //	};	

    class ServerData
    {
        String ip;
        int port;
        boolean pvp;
        int currentPlayers;
        int maxPlayers;
        boolean testServer;
        boolean brackets;
        boolean clock;
        int status;
        public int server_id;

        ServerData(String pIp, int pPort, boolean pPvp, boolean pTestServer, int pCurrentPlayers,
                   int pMaxPlayers, boolean pBrackets, boolean pClock, int pStatus, int pServer_id)
        {
            this.ip = pIp;
            this.port = pPort;
            this.pvp = pPvp;
            this.testServer = pTestServer;
            this.currentPlayers = pCurrentPlayers;
            this.maxPlayers = pMaxPlayers;
            this.brackets = pBrackets;
            this.clock = pClock;
            this.status = pStatus;
            this.server_id = pServer_id;
        }
    }

    public ServerList()
    {
        _servers = new FastList<ServerData>();
    }

    public void addServer(String ip, int port, boolean pvp, boolean testServer, int currentPlayer,
                          int maxPlayer, boolean brackets, boolean clock, int status, int server_id)
    {
        _servers.add(new ServerData(ip, port, pvp, testServer, currentPlayer, maxPlayer, brackets,
                                    clock, status, server_id));
    }

    public byte[] getContent()
    {
        if (!_listDone) // list should only be done once even if there are multiple getContent calls
        {
            writeC(0x04);
            writeC(_servers.size());
            writeC(0x00);
            for (int i = 0; i < _servers.size(); i++)
            {
                ServerData server = _servers.get(i);

                writeC(server.server_id + 1); // server id
                try
                {
                    InetAddress i4 = InetAddress.getByName(server.ip);
                    byte[] raw = i4.getAddress();
                    writeC(raw[0] & 0xff);
                    writeC(raw[1] & 0xff);
                    writeC(raw[2] & 0xff);
                    writeC(raw[3] & 0xff);
                }
                catch (UnknownHostException e)
                {
                    e.printStackTrace();
                    writeC(127);
                    writeC(0);
                    writeC(0);
                    writeC(1);
                }

                writeD(server.port);
                writeC(0x0f); // age limit
                if (server.pvp)
                {
                    writeC(0x01);
                }
                else
                {
                    writeC(0x00);
                }

                writeH(server.currentPlayers);
                writeH(server.maxPlayers);
                if (server.status == ServerStatus.STATUS_DOWN)
                {
                    writeC(0x00);
                }
                else
                {
                    writeC(0x01);
                }
                int bits = 0;
                if (server.testServer)
                {
                    bits |= 0x04;
                }
                if (server.clock)
                {
                    bits |= 0x02;
                }
                writeD(bits);
                if (server.brackets)
                {
                    writeC(0x01);
                }
                else
                {
                    writeC(0x00);
                }
            }
            _listDone = true;
        }

        return getBytes();
    }
}
