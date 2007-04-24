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
import net.sf.l2j.loginserver.GameServerTable;
import net.sf.l2j.loginserver.L2LoginClient;
import net.sf.l2j.loginserver.GameServerTable.GameServerInfo;
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
public final class ServerList extends L2LoginServerPacket
{
	private List<ServerData> _servers;

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

	public ServerList(L2LoginClient client)
	{
		_servers = new FastList<ServerData>();
		for (GameServerInfo gsi : GameServerTable.getInstance().getRegisteredGameServers().values())
		{
			this.addServer(client.usesInternalIP() ? gsi.getInternalHost() : gsi.getExternalHost(), gsi.getPort(), gsi.isPvp(), gsi.isTestServer(), gsi.getCurrentPlayerCount(), gsi.getMaxPlayers(), gsi.isShowingBrackets(), gsi.isShowingClock(), gsi.getStatus(), gsi.getId());
		}
	}

	public void addServer(String ip, int port, boolean pvp, boolean testServer, int currentPlayer,
			int maxPlayer, boolean brackets, boolean clock, int status, int server_id)
	{
		_servers.add(new ServerData(ip, port, pvp, testServer, currentPlayer, maxPlayer, brackets,
				clock, status, server_id));
	}

	public void write()
	{
		writeC(0x04);
		writeC(_servers.size());
		writeC(_servers.size()-1);
		for (ServerData server : _servers)
		{
			writeC(server.server_id); // server id

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
			writeC(0x00); // age limit
			writeC(server.pvp ? 0x01 : 0x00);
			writeH(server.currentPlayers);
			writeH(server.maxPlayers);
			writeC(server.status == ServerStatus.STATUS_DOWN ? 0x00 : 0x01);
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
			writeC(server.brackets ? 0x01 : 0x00);
		}
	}
}
