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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.11.2.5.2.5 $ $Date: 2005/03/27 15:29:18 $
 */
public class ClanTable
{
	private static Logger _log = Logger.getLogger(ClanTable.class.getName());
	
	private static ClanTable _instance;
	
	private Map<Integer, L2Clan> _clans;
	
	public static ClanTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new ClanTable();
		}
		return _instance;
	}
	public L2Clan[] getClans()
	{
	    return _clans.values().toArray(new L2Clan[_clans.size()]);
	}
	
	private ClanTable()
	{
		_clans = new FastMap<Integer, L2Clan>();
		java.sql.Connection con = null;
	     try
	        {
	            con = L2DatabaseFactory.getInstance().getConnection();
	            PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM clan_data");	           
	            ResultSet result = statement.executeQuery();
	            
	            while(result.next())
	            {
	            	_clans.put(Integer.parseInt(result.getString("clan_id")),new L2Clan(Integer.parseInt(result.getString("clan_id"))));
	            }	            
	            result.close();
	            statement.close();
	        }
	        catch (Exception e) {
	            _log.warning("data error on ClanTable: " + e);
	            e.printStackTrace();
	        } finally {
	            try { con.close(); } catch (Exception e) {}
	        }
	}
	
	/**
	 * @param clanId
	 * @return
	 */
	public L2Clan getClan(int clanId)
	{
        L2Clan clan = _clans.get(new Integer(clanId));
       /* if (clan == null)
        {
            clan = new L2Clan(clanId);
            if (clan != null)
            {
                _clans.put(clan.getClanId(), clan);
            }
        }*/
        
		return clan; 
	}
    /*
    public L2Clan getClanIfExists(int clanId)
    {
        String clanName = null;
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT clan_name FROM clan_data WHERE clan_id=?");
            statement.setInt(1, clanId);
            ResultSet result = statement.executeQuery();
            
            if (result.next())
            {
                clanName = result.getString("clan_name");
            }
            
            result.close();
            statement.close();
        }
        catch (Exception e) {
            _log.warning("data error on clan item: " + e);
            e.printStackTrace();
        } finally {
            try { con.close(); } catch (Exception e) {}
        }
        
        L2Clan clan = null;
        
        if (clanName != null)
        {
            clan    = getClanByName(clanName);
        }
        
        return clan; 
    }
	*/
    public L2Clan getClanByName(String clanName)
    {
        java.sql.Connection con = null;
        L2Clan clan = null;
		try
		{
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM clan_data WHERE clan_name LIKE ?");
            statement.setString(1, clanName);
            ResultSet result = statement.executeQuery();
			while(result.next())
            {
                int clanId = result.getInt("clan_id");
                clan = getClan(clanId);
            }
            result.close();
			statement.close();
		}
		catch (Exception e) {
			_log.warning("data error on clan item: " + e);
		} finally {
			try { con.close(); } catch (Exception e) {}
		}
        return clan;
    }
	
	/**
	 * @param player
	 * @return NULL if clan with same name already exists
	 */
	public L2Clan createClan(L2PcInstance player, String clanName)
	{
        java.sql.Connection con = null;
        boolean clanExists = true;
        L2Clan clan = null;
        
        try//store the new clan in db
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM clan_data WHERE clan_name=?");
            statement.setString(1, clanName);
            ResultSet result = statement.executeQuery();
            clanExists = result.next(); 
            result.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("error while saving new clan to db "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }

        if (!clanExists)
        {
            // no clan with same name exists
            clan = new L2Clan(IdFactory.getInstance().getNextId(), clanName);
            L2ClanMember leader = new L2ClanMember(clan, player.getName(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), player.getPledgeType(), player.getPowerGrade(), player.getTitle());
            clan.setLeader(leader);
            clan.store();
            player.setClan(clan);
            player.setClanPrivileges(L2Clan.CP_ALL);            

            if (Config.DEBUG) _log.fine("New clan created: "+clan.getClanId() + " " +clan.getName());
            
            _clans.put(new Integer(clan.getClanId()), clan);
        }
		
		return clan;
	}
	public boolean isAllyExists(String allyName)
	{
        java.sql.Connection con = null;
        boolean allyExists = true;
        try//store the new clan in db
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT ally_id FROM clan_data WHERE ally_name=?");
            statement.setString(1, allyName);
            ResultSet result = statement.executeQuery();
            allyExists = result.next(); 
            result.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("error while saving new clan to db "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
	    return allyExists;
	}
    
    public void storeclanswars(int clanId1, int clanId2){
        L2Clan clan1 = ClanTable.getInstance().getClan(clanId1);
        L2Clan clan2 = ClanTable.getInstance().getClan(clanId2);
        clan1.setEnemyClan(clan2);
        //clan2.setEnemyClan(clan1);
        clan1.broadcastClanStatus();
        //clan2.broadcastClanStatus();
     	java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("REPLACE INTO clan_wars (clan1, clan2, wantspeace1, wantspeace2) VALUES(?,?,?,?)");
			statement.setInt(1, clanId1);
			statement.setInt(2, clanId2);
			statement.setInt(3, 0);
			statement.setInt(4, 0);
			statement.execute();
			statement.close();            
        }
        catch (Exception e)
        {
            _log.warning("could not store clans wars data:"+e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
        //SystemMessage msg = new SystemMessage(SystemMessage.WAR_WITH_THE_S1_CLAN_HAS_BEGUN);
	//
        SystemMessage msg = new SystemMessage(1562);
        msg.addString(clan2.getName());
        clan1.broadcastToOnlineMembers(msg);
        //msg = new SystemMessage(SystemMessage.WAR_WITH_THE_S1_CLAN_HAS_BEGUN);
        //msg.addString(clan1.getName());
        //clan2.broadcastToOnlineMembers(msg);
	// clan1 declared clan war.
        msg = new SystemMessage(1561);
        msg.addString(clan1.getName());
        clan2.broadcastToOnlineMembers(msg);
    }

    public void deleteclanswars(int clanId1, int clanId2)
    {
        L2Clan clan1 = ClanTable.getInstance().getClan(clanId1);
        L2Clan clan2 = ClanTable.getInstance().getClan(clanId2);
        clan1.deleteEnemyClan(clan2);
        //clan2.deleteEnemyClan(clan1);
        clan1.broadcastClanStatus();
        //clan2.broadcastClanStatus();
        //for(L2ClanMember player: clan1.getMembers())
        //{
        //    if(player.getPlayerInstance()!=null)
	//			player.getPlayerInstance().setWantsPeace(0);
        //}
        //for(L2ClanMember player: clan2.getMembers())
        //{
        //    if(player.getPlayerInstance()!=null)
	//			player.getPlayerInstance().setWantsPeace(0);
        //}
     	java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
            statement.setInt(1,clanId1);
            statement.setInt(2,clanId2);
            statement.execute();
            //statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
            //statement.setInt(1,clanId2);
            //statement.setInt(2,clanId1);
            //statement.execute();
        }
        catch (Exception e)
        {
            _log.warning("could not restore clans wars data:"+e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
        //SystemMessage msg = new SystemMessage(SystemMessage.WAR_WITH_THE_S1_CLAN_HAS_ENDED);
        SystemMessage msg = new SystemMessage(1567);
        msg.addString(clan2.getName());
        clan1.broadcastToOnlineMembers(msg);
        msg = new SystemMessage(1566);
        msg.addString(clan1.getName());
        clan2.broadcastToOnlineMembers(msg);
        //msg = new SystemMessage(SystemMessage.WAR_WITH_THE_S1_CLAN_HAS_ENDED);
        //msg.addString(clan1.getName());
        //clan2.broadcastToOnlineMembers(msg);
    }
    
    public void CheckSurrender(L2Clan clan1, L2Clan clan2)
    {
        int count = 0;
        for(L2ClanMember player: clan1.getMembers())
        {
            if(player != null && player.getPlayerInstance().getWantsPeace() == 1)
                count++;
        }
        if(count == clan1.getMembers().length-1)
        {
            clan1.deleteEnemyClan(clan2);
            clan2.deleteEnemyClan(clan1);
            deleteclanswars(clan1.getClanId(),clan2.getClanId());
        }
    }
}
