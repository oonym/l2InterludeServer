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
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.CharSelected;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class CharacterSelected extends ClientBasePacket
{
	private static final String _C__0D_CHARACTERSELECTED = "[C] 0D CharacterSelected";
	private static Logger _log = Logger.getLogger(CharacterSelected.class.getName());

	// cd
	private final int _charSlot;
	
	@SuppressWarnings("unused")
	private final int _unk1; 	// new in C4
	@SuppressWarnings("unused")
	private final int _unk2;	// new in C4
	@SuppressWarnings("unused")
	private final int _unk3;	// new in C4
	@SuppressWarnings("unused")
	private final int _unk4;	// new in C4

	/**
	 * @param decrypt
	 */
	public CharacterSelected(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		_charSlot = readD();
		_unk1 = readH();
		_unk2 = readD();
		_unk3 = readD();
		_unk4 = readD();
	}

	void runImpl()
	{
		// if there is a playback.dat file in the current directory, it will
		// be sent to the client instead of any regular packets
		// to make this work, the first packet in the playback.dat has to
		// be a  [S]0x21 packet
		// after playback is done, the client will not work correct and need to exit
		//playLogFile(getConnection()); // try to play log file
		

		// HAVE TO CREATE THE L2PCINSTANCE HERE TO SET AS ACTIVE
		if (Config.DEBUG) _log.fine("selected slot:" + _charSlot);
		
		//loadup character from disk
		L2PcInstance cha = getClient().loadCharFromDisk(_charSlot);
		if(cha == null)
		{
			_log.warning("Character could not be loaded (slot:"+_charSlot+")");
			sendPacket(new ActionFailed());
			return;
		}
		
		getClient().setActiveChar(cha);
		
		if(cha.getAccessLevel() < -1)
		{
			cha.closeNetConnection();
			return;
		}
		//weird but usefull, will send i..
		//cha.setAccessLevel(cha.getAccessLevel());
		CharSelected cs = new CharSelected(cha, getClient().getSessionId().playOkID1);
		sendPacket(cs);
	}
	
	/*
	private void playLogFile(Connection connection)
	{
		long diff = 0;
		long first = -1;

		try
		{
			LineNumberReader lnr =
			new LineNumberReader(new FileReader("playback.dat"));

			String line = null;
			while ((line = lnr.readLine()) != null)
			{
				if (line.length() > 0 && line.substring(0, 1).equals("1"))
				{
					String timestamp = line.substring(0, 13);
					long time = Long.parseLong(timestamp);
					if (first == -1)
					{
						long start = System.currentTimeMillis();
						first = time;
						diff = start - first;
					}
					
					String cs = line.substring(14, 15);
					// read packet definition
					ByteArrayOutputStream bais = new ByteArrayOutputStream();

					while (true)
					{
						String temp = lnr.readLine();
						if (temp.length() < 53)
						{
							break;
						}
						
						String bytes = temp.substring(6, 53);
						StringTokenizer st = new StringTokenizer(bytes);
						while (st.hasMoreTokens())
						{
							String b = st.nextToken();
							int number = Integer.parseInt(b, 16);
							bais.write(number);
						}
					}

					if (cs.equals("S"))
					{
						//wait for timestamp and send packet
						int wait =
						(int) (time + diff - System.currentTimeMillis());
						if (wait > 0)
						{
							if (Config.DEBUG) _log.fine("waiting"+ wait);
							Thread.sleep(wait);
						}
						if (Config.DEBUG) _log.fine("sending:"+ time);
						byte[] data = bais.toByteArray();
						if (data.length != 0)
						{
							//connection.sendPacket(data);	
						}
						else
						{
							if (Config.DEBUG) _log.fine("skipping broken data");
						}

					}
					else
					{
						// skip packet
					}
				}

			}
		}
		catch (FileNotFoundException f)
		{
			// should not happen
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error:", e);
		}
	}
    */

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__0D_CHARACTERSELECTED;
	}	
}
