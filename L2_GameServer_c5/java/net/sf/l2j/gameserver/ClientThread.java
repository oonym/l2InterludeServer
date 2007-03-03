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
package net.sf.l2j.gameserver;

import java.net.Socket;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.LoginServerThread.SessionKey;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.CharSelectInfoPackage;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.util.EventData;

/**
 * This class ...
 * 
 * @version $Revision: 1.21.2.19.2.12 $ $Date: 2005/04/04 19:47:01 $
 */
public final class ClientThread
{
	protected static final Logger _log = Logger.getLogger(ClientThread.class.getName());
	private boolean _authed;
    
	public static int devCharId;
	
	class AutoSaveTask implements Runnable
	{
		public void run() {
			try
			{
				L2PcInstance player = ClientThread.this.getActiveChar();
				if (player != null) {
					saveCharToDisk(player);
				}
				else if (getConnection() == null
						|| !getConnection().getChannel().isOpen())
				{
					_autoSaveInDB.cancel(false);
				}
			} catch (Throwable e) {
				_log.severe(e.toString());
			}
		}
	}
	
	private String _loginName;
	private L2PcInstance _activeChar;
	private SessionKey _sessionId;
	private final Connection _connection;
	final ScheduledFuture _autoSaveInDB;
	
	//private byte[] _filter;
	
	private final byte[] _cryptkey = {
			(byte)0x94, (byte)0x35, (byte)0x00, (byte)0x00, 
			(byte)0xa1, (byte)0x6c,	(byte)0x54, (byte)0x87   // these 4 bytes are fixed
	};
	
    private int _revision = 0;
    private boolean _gameGuardOk = false;
    
    private List<Integer> _charSlotMapping = new FastList<Integer>();
    
    public static ClientThread create(Socket socket)
    {
    	final ClientThread client = new ClientThread(socket);
    	return client;
    }
	
	public ClientThread(Socket socket)
	{
		_sessionId = new SessionKey(-1,-1,-1,-1);
		_connection = new Connection(this, socket, _cryptkey);
   		_autoSaveInDB = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(
   				new AutoSaveTask(), 300000L, 900000L
   				);
	}
    
	void onDisconnect()
	{
		try
		{
			_autoSaveInDB.cancel(false);
            L2PcInstance player = _activeChar;
			if (player != null)  // this should only happen on connection loss
			{
				
                // we store all data from players who are disconnected while in an event in order to restore it in the next login
                if (player.atEvent)
                {
                	EventData data = new EventData(player.eventX, player.eventY, player.eventZ, player.eventkarma, player.eventpvpkills, player.eventpkkills, player.eventTitle, player.kills, player.eventSitForced);
                    L2Event.connectionLossData.put(player.getName(), data);
                }
                if (player.isFlying()) 
                { 
                	player.removeSkill(SkillTable.getInstance().getInfo(4289, 1));
                }
				// notify the world about our disconnect
				player.deleteMe();
				
				try
                {
					saveCharToDisk(player);
				}
                catch (Exception e2) { /* ignore any problems here */ }
			}
            _activeChar = null;
			_connection.close();
		}
		catch (Exception e1)
		{
			_log.log(Level.WARNING, "error while disconnecting client", e1);
		}
		finally
		{
			// remove the account
			LoginServerThread.getInstance().sendLogout(getLoginName());
		}
	}
	
	/**
	 * Save the L2PcInstance to the database.
	 */
	public static void saveCharToDisk(L2PcInstance cha)
	{
        try
        {
            cha.store();
        }
        catch(Exception e)
        {
            _log.warning("Error saving player character: "+e);
        }
	}

	public void markRestoredChar(int charslot) throws Exception
	{	
		//have to make sure active character must be nulled
		if (getActiveChar() != null)
		{
			saveCharToDisk (getActiveChar());
			if (Config.DEBUG) _log.fine("active Char saved");
			_activeChar = null;
		}

		int objid = getObjectIdForSlot(charslot);
    		if (objid < 0)
    		    return;
		java.sql.Connection con = null;
		try 
		{
		con = L2DatabaseFactory.getInstance().getConnection();
		PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?");
		statement.setInt(1, objid);
		statement.execute();
		statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Data error on restoring char: " + e);
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
	}
	public L2PcInstance markToDeleteChar(int charslot) throws Exception
	{
		//have to make sure active character must be nulled
		if (getActiveChar() != null)
		{
			saveCharToDisk (getActiveChar());
			if (Config.DEBUG) _log.fine("active Char saved");
			_activeChar = null;
		}

		int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
		    return null;

		L2PcInstance character = L2PcInstance.load(objid);
		if (character.getClanId() != 0)
			return character;

		java.sql.Connection con = null;
		try 
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?");
			statement.setLong(1, System.currentTimeMillis() + Config.DELETE_DAYS*86400000); // 24*60*60*1000 = 86400000 
			statement.setInt(2, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Data error on update delete time of char: " + e);
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
	    return null;
	}

	public L2PcInstance deleteChar(int charslot) throws Exception
	{
		//have to make sure active character must be nulled
		if (getActiveChar() != null)
		{
			saveCharToDisk (getActiveChar());
			if (Config.DEBUG) _log.fine("active Char saved");
			_activeChar = null;
		}
	
		int objid = getObjectIdForSlot(charslot);
		if (objid < 0)
    	    return null;

		L2PcInstance character = L2PcInstance.load(objid);
		if (character.getClanId() != 0)
			return character;

		deleteCharByObjId(objid);
		return null;
	}

	public static void deleteCharByObjId(int objid)	{
	    if (objid < 0)
	        return;
        
	    java.sql.Connection con = null;
        
		try 
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement ;

        	statement = con.prepareStatement("DELETE FROM character_friends WHERE char_id=? OR friend_id=?");
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.close();
            
            statement = con.prepareStatement("DELETE FROM character_hennas WHERE char_obj_id=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
	
			statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_quests WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_recipebook WHERE char_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
	
			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
	
			statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE char_obj_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
            
            statement = con.prepareStatement("DELETE FROM heroes WHERE char_id=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            
            statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE char_id=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();
            
            statement = con.prepareStatement("DELETE FROM seven_signs WHERE char_obj_id=?");
            statement.setInt(1, objid);
            statement.execute();
            statement.close();

        	statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();

			statement = con.prepareStatement("DELETE FROM merchant_lease WHERE player_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
			
			
			statement = con.prepareStatement("DELETE FROM characters WHERE obj_Id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.warning("Data error on deleting char: " + e);
		} 
		finally 
		{
			try { con.close(); } catch (Exception e) {}
		}
	}
	
	public L2PcInstance loadCharFromDisk(int charslot)
	{
		L2PcInstance character = L2PcInstance.load(getObjectIdForSlot(charslot));
		
		if (Config.DEVELOPER)
		{
			if ("test".equals(getLoginName()))
				devCharId = character.getObjectId();
		}
        if (character != null)
		{
			//restoreInventory(character);
			//restoreSkills(character);
            //character.restoreSkills();
			//restoreShortCuts(character);
			//restoreWarehouse(character);

			// preinit some values for each login
			character.setRunning();	// running is default
			character.standUp();		// standing is default
			
            character.updateStats();
			character.updateKarma();
            character.setOnlineStatus(true);
		}
		else
		{
			_log.warning("could not restore in slot:"+ charslot);
		}
		
		//setCharacter(character);
		return character;
	}
	
    /**
     * @param charslot
     * @return
     */
    private int getObjectIdForSlot(int charslot)
    {
        if (charslot < 0 || charslot >= _charSlotMapping.size())
        {
            _log.warning(getLoginName() + " tried to delete Character in slot "+charslot+" but no characters exits at that slot.");
            return -1;
        }
        Integer objectId = _charSlotMapping.get(charslot);
        return objectId.intValue();
    }
	
	/**
	 * @return
	 */
	public Connection getConnection()
	{
		return _connection;
	}
	
	/**
	 * @return
	 */
	public L2PcInstance getActiveChar()
	{
		return _activeChar;
	}
	/**
	 * @return Returns the sessionId.
	 */
	public SessionKey getSessionId()
	{
		return _sessionId;
	}
	
	public String getLoginName()
	{
		return _loginName;
	}
	
	public void setLoginName(String loginName)
	{
		_loginName = loginName;
	}
	
	
	
	/**
	 * @param cha
	 */
	public void setActiveChar(L2PcInstance cha)
	{
		_activeChar = cha;
		if (cha != null)
		{
			// we store the connection in the player object so that external
			// events can directly send events to the players client
			// might be changed later to use a central event management and distribution system
			_activeChar.setNetConnection(_connection);
			
			// update world data
			L2World.getInstance().storeObject(_activeChar);
		}
	}

	/**
	 * @param key
	 */
	public void setSessionId(SessionKey key)
	{
		_sessionId = key;
	}
    
    /**
     * @param chars
     */
    public void setCharSelection(CharSelectInfoPackage[] chars)
    {
        _charSlotMapping.clear();
        
        for (int i = 0; i < chars.length; i++)
        {
            int objectId = chars[i].getObjectId();
            _charSlotMapping.add(new Integer(objectId));
        }
    }
    /**
     * @return Returns the revision.
     */
    public int getRevision()
    {
        return _revision;
    }
    /**
     * @param revision The revision to set.
     */
    public void setRevision(int revision)
    {
        _revision = revision;
    }

    public void setGameGuardOk(boolean gameGuardOk)
    {
        _gameGuardOk = gameGuardOk;
    }

    public boolean isGameGuardOk()
    {
        return _gameGuardOk;
    }
    
    public void setAuthed(boolean b)
    {
    	_authed = b;
    }
    
    public boolean isAuthed()
    {
    	return _authed;
    }
}
