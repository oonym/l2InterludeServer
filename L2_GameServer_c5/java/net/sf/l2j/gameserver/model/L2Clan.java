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
package net.sf.l2j.gameserver.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.communitybbs.BB.Forum;
import net.sf.l2j.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.4.2.7 $ $Date: 2005/04/06 16:13:41 $
 */
public class L2Clan
{
	private static final Logger _log = Logger.getLogger(L2Clan.class.getName());

	private String _name;
	private int _clanId;
	private L2ClanMember _leader;
	private Map<String, L2ClanMember> _members = new FastMap<String, L2ClanMember>();
	
	private String _allyName;
	private int _allyId;
	private int _level;
	private int _hasCastle;
	private int _hasHideout;
    private boolean _hasCrest;
    private int _hiredGuards;
    private int _crestId;
    private int _crestLargeId;
    private int _allyCrestId;

    private ItemContainer _warehouse = new ClanWarehouse(this);
    private List<Integer> _atWarWith = new FastList<Integer>();

	private boolean _hasCrestLarge;

	private Forum _Forum;

	private List<L2Skill> _skills = new FastList<L2Skill>(); 
    
    //  Clan Privileges  
	public static final int CP_NOTHING = 0;  
	public static final int CP_CL_JOIN_CLAN = 1; // Join clan  
	public static final int CP_CL_GIVE_TITLE = 2; // Give a title  
	public static final int CP_CL_VIEW_WAREHOUSE = 4; // View warehouse content  
	public static final int CP_CL_REGISTER_CREST = 8; // Register clan crest  
	public static final int CP_CH_OPEN_DOOR = 16; // open a door  
	public static final int CP_CH_OTHER_RIGHTS = 32; //??  
	public static final int CP_CH_DISMISS = 64; //??  
	public static final int CP_CS_OPEN_DOOR = 128;  
	public static final int CP_CS_OTHER_RIGHTS = 256; //???  
	public static final int CP_CS_DISMISS = 512; //???  
	public static final int CP_ALL = 1023;  

    /**
     * called if a clan is referenced only by id.
     * in this case all other data needs to be fetched from db
     * @param clanId
     */
    public L2Clan(int clanId)
    {
        _clanId = clanId;
        restore();
        getWarehouse().restore();
    }
    
    /**
     * this is only called if a new clan is created
     * @param clanId
     * @param clanName
     * @param leader
     */
    public L2Clan(int clanId, String clanName, L2ClanMember leader)
    {
        _clanId = clanId;
        _name = clanName;
        setLeader(leader);
    }
    
	/**
	 * @return Returns the clanId.
	 */
	public int getClanId()
	{
		return _clanId;
	}
	/**
	 * @param clanId The clanId to set.
	 */
	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}
	/**
	 * @return Returns the leaderId.
	 */
	public int getLeaderId()
	{
		return (_leader != null ? _leader.getObjectId() : 0);
	}
    /**
     * @return L2ClanMember of clan leader.
     */
    public L2ClanMember getLeader()
    {
        return _leader;
    }
	/**
	 * @param leaderId The leaderId to set.
	 */
	public void setLeader(L2ClanMember leader)
	{
		_leader = leader;
		_members.put(leader.getName(), leader);
	}
	public void setNewLeader()
	{
	    int maxLevel = 0;
	    L2ClanMember newLeader = null;
	    for (L2ClanMember member : getMembers())
	    {
	        if (member.getLevel() > maxLevel)
	        {
	            maxLevel = member.getLevel();
	            newLeader = member;
	        }
	    }
	    if (newLeader != null)
	    {
	        setLeader(newLeader);
	        updateClanInDB();
	        broadcastToOnlineMembers(new PledgeStatusChanged(this));
	        broadcastToOnlineMembers(SystemMessage.sendString(newLeader.getName() + " is the new clan leader."));
	    }
	}
	/**
	 * @return Returns the leaderName.
	 */
	public String getLeaderName()
	{
		return (_leader!=null ? _leader.getName() : "");
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return _name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name)
	{
		_name = name;
	}
	
	private void addClanMember(L2ClanMember member)
	{
		_members.put(member.getName(), member);
	}
	
	public void addClanMember(L2PcInstance player)
	{
		L2ClanMember member = new L2ClanMember(player);
        // store in db
        storeNewMemberInDatabase(member);
        // store in memory
		addClanMember(member);
	}
	
	public void updateClanMember(L2PcInstance player)
	{
		L2ClanMember member = new L2ClanMember(player);
		
		addClanMember(member);
	}
	
	public L2ClanMember getClanMember(String name)
	{
		return _members.get(name);
	}

	public void removeClan()
	{
	    broadcastToOnlineMembers(new SystemMessage(SystemMessage.CLAN_MEMBERSHIP_TERMINATED));
	    for (L2ClanMember member : getMembers())
	    {
	        removeClanMember(member.getName());
	    }

	    _warehouse.destroyAllItems("ClanRemove", getLeader().getPlayerInstance(), null);
	    
	    java.sql.Connection con = null;
	    try
	    {
	        con = L2DatabaseFactory.getInstance().getConnection();
	        PreparedStatement statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
	        statement.setInt(1, getClanId());
	        statement.execute();
	        statement.close();                  
	        if (Config.DEBUG) _log.fine("clan removed in db: "+getClanId());
	    }
	    catch (Exception e)
	    {
	        _log.warning("error while removing clan in db "+e);
	    }
	    finally
	    {
	        try { con.close(); } catch (Exception e) {}
	    }
	}

	public void removeClanMember(String name)
	{
		L2ClanMember exMember = _members.remove(name);
		if(exMember == null)
		{
			_log.warning("Member "+name+" not found in clan while trying to remove");
			return;
		}
		if (exMember.getName().equals(getLeaderName()))
        {
            setNewLeader();
        }
        removeMemberInDatabase(exMember);
        if (_members.isEmpty())
        {
            removeClan();
        }
	}

	public L2ClanMember[] getMembers()
	{
		return _members.values().toArray(new L2ClanMember[_members.size()]);
	}
	
	public int getMembersCount()
	{
		return _members.size();
	}
	
	public L2PcInstance[] getOnlineMembers(String exclude)
	{
		List<L2PcInstance> result = new FastList<L2PcInstance>();
		for (L2ClanMember temp : _members.values())
		{
			//L2ClanMember temp = (L2ClanMember) iter.next();
			if (temp.isOnline() && temp.getPlayerInstance()!=null && !temp.getName().equals(exclude))
			{
				result.add(temp.getPlayerInstance());
			}
		}
		
		return result.toArray(new L2PcInstance[result.size()]);
		
	}
	
	/**
	 * @return
	 */
	public int getAllyId()
	{
		return _allyId;
	}
	/**
	 * @return
	 */
	public String getAllyName()
	{
		return _allyName;
	}
    
    public void setAllyCrestId(int allyCrestId)
    {
        _allyCrestId = allyCrestId;
    }
    
	/**
	 * @return
	 */
	public int getAllyCrestId()
	{
		return _allyCrestId;
	}
	/**
	 * @return
	 */
	public int getLevel()
	{
		return _level;
	}
	/**
	 * @return
	 */
	public int getHasCastle()
	{
		return _hasCastle;
	}
	/**
	 * @return
	 */
	public int getHasHideout()
	{
		return _hasHideout;
	}
    
    /**
     * @param crestId The id of pledge crest.
     */
    public void setCrestId(int crestId)
    {
        _crestId = crestId;
    }
    
	/**
	 * @return Returns the clanCrestId.
	 */
	public int getCrestId()
	{
		return _crestId;
	}
	
    /**
     * @param crestLargeId The id of pledge LargeCrest.
     */
    public void setCrestLargeId(int crestLargeId)
    {
        _crestLargeId = crestLargeId;
    }
	
	/**
	 * @return Returns the clan CrestLargeId
	 */
	public int getCrestLargeId()
	{
		return _crestLargeId;
	}
	
	/**
	 * @param allyId The allyId to set.
	 */
	public void setAllyId(int allyId)
	{
		_allyId = allyId;
	}
	/**
	 * @param allyName The allyName to set.
	 */
	public void setAllyName(String allyName)
	{
		_allyName = allyName;
	}
	/**
	 * @param hasCastle The hasCastle to set.
	 */
	public void setHasCastle(int hasCastle)
	{
		_hasCastle = hasCastle;
	}
	/**
	 * @param hasHideout The hasHideout to set.
	 */
	public void setHasHideout(int hasHideout)
	{
		_hasHideout = hasHideout;
	}
	/**
	 * @param level The level to set.
	 */
	public void setLevel(int level)
	{
	    _level = level;
	    if(_Forum == null)
	    {
	    	if(_level >= 2)
	    	{
	    		_Forum = ForumsBBSManager.getInstance().getForumByName("ClanRoot").GetChildByName(_name);
            	if(_Forum == null)
            	{
            		_Forum = ForumsBBSManager.getInstance().CreateNewForum(_name,ForumsBBSManager.getInstance().getForumByName("ClanRoot"),Forum.CLAN,Forum.CLANMEMBERONLY,getClanId());
            	}
	    	}
	    }
	}
	
	/**
	 * @param player name
	 * @return
	 */
	public boolean isMember(String name)
	{
		return (name == null ? false :_members.containsKey(name));
	}
	
	public void updateClanInDB()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET leader_id=?,ally_id=?,ally_name=? WHERE clan_id=?");
			statement.setInt(1, getLeaderId());
			statement.setInt(2, getAllyId());
			statement.setString(3, getAllyName());
			statement.setInt(4, getClanId());
			statement.execute();
			statement.close();
			if (Config.DEBUG) _log.fine("New clan leader saved in db: "+getClanId());
		}
		catch (Exception e)
		{
			_log.warning("error while saving new clan leader to db "+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}	

	public void store()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,hasHideout,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id) values (?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, getClanId());
			statement.setString(2, getName());
			statement.setInt(3, getLevel());
			statement.setInt(4, getHasCastle());
			statement.setInt(5, getHasHideout());
			statement.setInt(6, getAllyId());
			statement.setString(7, getAllyName());
			statement.setInt(8, getLeaderId());
            statement.setInt(9, getCrestId());
            statement.setInt(10,getCrestLargeId());
            statement.setInt(11,getAllyCrestId());
			statement.execute();
			statement.close();
			
			statement = con.prepareStatement("UPDATE characters SET clanid=? WHERE obj_Id=?");
			statement.setInt(1, getClanId());
			statement.setInt(2, getLeaderId());
			statement.execute();
			statement.close();					

			if (Config.DEBUG) _log.fine("New clan saved in db: "+getClanId());
		}
		catch (Exception e)
		{
			_log.warning("error while saving new clan to db "+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}	

    private void storeNewMemberInDatabase(L2ClanMember member)
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid=? WHERE obj_Id=?");
            statement.setInt(1, getClanId());
            statement.setInt(2, member.getObjectId());
            statement.execute();
            statement.close();                  
            if (Config.DEBUG) _log.fine("New clan member saved in db: "+getClanId());
        }
        catch (Exception e)
        {
            _log.warning("error while saving new clan member to db "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }

    private void removeMemberInDatabase(L2ClanMember member)
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid=0, allyId=0, title=?, clan_privs=0, wantspeace=0 WHERE obj_Id=?");
            statement.setString(1, "");
            statement.setInt(2, member.getObjectId());
            statement.execute();
            statement.close();                  
            if (Config.DEBUG) _log.fine("clan member removed in db: "+getClanId());
        }
        catch (Exception e)
        {
            _log.warning("error while removing clan member in db "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    

    @SuppressWarnings("unused")
    private void UpdateWarsInDB()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("UPDATE clan_wars SET wantspeace1=? WHERE clan1=?");
            statement.setInt(1, 0);
            statement.setInt(2, 0);
        }
        catch (Exception e)
        {
            _log.warning("could not update clans wars data:" + e);
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
        }
    }

    private void restorewars()
    {
     	java.sql.Connection con = null;
        
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("SELECT clan1, clan2, wantspeace1, wantspeace2 FROM clan_wars");
            ResultSet rset = statement.executeQuery();
            while(rset.next())
            {
               if(rset.getInt("clan1") == this._clanId) this.setEnemyClan(rset.getInt("clan2"));
//               if(rset.getInt("clan2") == this._clanId) this.setEnemyClan(rset.getInt("clan1"));
           	}
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("could not restore clan wars data:"+e);
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    private void restore()
    {
        //restorewars();
    	java.sql.Connection con = null;
        try
        {
            L2ClanMember member;
            
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT clan_name,clan_level,hasCastle,hasHideout,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id FROM clan_data where clan_id=?");
            statement.setInt(1, getClanId());
            ResultSet clanData = statement.executeQuery();
            
            if (clanData.next())
            {
                setName(clanData.getString("clan_name"));
                setLevel(clanData.getInt("clan_level"));
                setHasCastle(clanData.getInt("hasCastle"));
                setHasHideout(clanData.getInt("hasHideout"));
                setAllyId(clanData.getInt("ally_id"));
                setAllyName(clanData.getString("ally_name"));

                setCrestId(clanData.getInt("crest_id"));
                if (getCrestId() != 0)
                {
                    setHasCrest(true);
                }
                
                setCrestLargeId(clanData.getInt("crest_large_id"));
                if (getCrestLargeId() != 0)
                {
                	setHasCrestLarge(true);
                }
                
                setAllyCrestId(clanData.getInt("ally_crest_id"));
                
                int leaderId = (clanData.getInt("leader_id"));          
                
                PreparedStatement statement2 = con.prepareStatement("SELECT char_name,level,classid,obj_Id,title,power_grade FROM characters WHERE clanid=?");
                statement2.setInt(1, getClanId());
                ResultSet clanMembers = statement2.executeQuery();
                
                while (clanMembers.next())
                {
                    member = new L2ClanMember(clanMembers.getString("char_name"), clanMembers.getInt("level"), clanMembers.getInt("classid"), clanMembers.getInt("obj_id"),clanMembers.getInt("power_grade"),clanMembers.getString("title"));
                    if (member.getObjectId() == leaderId)
                    {
                        setLeader(member);
                    }
                    else
                    {
                        addClanMember(member);
                    }                   
                }               
                clanMembers.close();
                statement2.close();              
            }
            
            clanData.close();
            statement.close();
            
            if (getName() != null)
            	_log.config("Restored clan data for \"" + getName() + "\" from database.");
            restorewars();
        }
        catch (Exception e)
        {
            _log.warning("error while restoring clan "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
	public void broadcastToOnlineAllyMembers(ServerBasePacket packet)
	{
	    if (getAllyId()==0)
		return;
	    for (L2Clan clan : ClanTable.getInstance().getClans()){
		if (clan.getAllyId() == this.getAllyId())
		    clan.broadcastToOnlineMembers(packet);
	    }
	}
	
	public void broadcastToOnlineMembers(ServerBasePacket packet)
	{
		for (L2ClanMember member : _members.values())
		{
			if (member.isOnline() && member.getPlayerInstance() != null)
			{
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}
	
	public void broadcastToOtherOnlineMembers(ServerBasePacket packet, L2PcInstance player)
	{
		for (L2ClanMember member : _members.values())
		{
			if (member.isOnline() && member.getPlayerInstance() != null && member.getPlayerInstance() != player)
			{
				member.getPlayerInstance().sendPacket(packet);
			}
		}
	}
	
	public String toString()
	{
		return getName();
	}

    /**
     * @return
     */
    public boolean hasCrest()
    {
        return _hasCrest;
    }
    
    public boolean hasCrestLarge()
	{
		return _hasCrestLarge;
	}
    
    public void setHasCrest(boolean flag)
    {
        _hasCrest = flag;
    }
    
    public void setHasCrestLarge(boolean flag)
    {
        _hasCrestLarge = flag;
    }
    
    public ItemContainer getWarehouse()
    {
        return _warehouse;
    }
    public boolean isAtWarWith(Integer id)
    {
    	if ((_atWarWith != null)&&(_atWarWith.size() > 0))
    		if (_atWarWith.contains(id)) return true;
    	return false;    	
    }
    public void setEnemyClan(L2Clan clan)
    {
    	Integer id = clan.getClanId();
    	_atWarWith.add(id);
    }
    public void setEnemyClan(Integer clan)
    {
    	_atWarWith.add(clan);
    }
    public void deleteEnemyClan(L2Clan clan)
    {
    	Integer id = clan.getClanId();
    	_atWarWith.remove(id);
    }
    public int getHiredGuards(){ return _hiredGuards; }
    public void incrementHiredGuards(){ _hiredGuards++; }
    
    public int isAtWar()
    {
       if ((_atWarWith != null)&&(_atWarWith.size() > 0))
           return 1;
       return 0;       
    }
    
    public List<Integer> getWarList()
    {
    	return _atWarWith;
    }
    
    public void broadcastClanStatus()
    {
        for(L2PcInstance member: this.getOnlineMembers(""))
        {
           PledgeShowMemberListAll pm = new PledgeShowMemberListAll(this, member);
           member.sendPacket(pm);
        }
    }
    
    public void addSkill(L2Skill sk)
    {
    	_skills.add(sk);
    }
    
    public void removeSkill(int id)
    {
    	L2Skill deleteSkill = null;
    	for(L2Skill sk : _skills)
    	{
    		if(sk.getId() == id)
    		{
    			deleteSkill = sk;
    			return;
    		}
    	}
    	_skills.remove(deleteSkill);
    }
    
    public void removeSkill(L2Skill deleteSkill)
    {
    	_skills.remove(deleteSkill);
    }

	/**
	 * @return
	 */
	public List<L2Skill> getSkills()
	{
		return _skills;
	}

}
