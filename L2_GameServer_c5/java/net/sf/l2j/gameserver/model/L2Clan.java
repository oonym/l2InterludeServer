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
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.communitybbs.BB.Forum;
import net.sf.l2j.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.PledgeReceiveSubPledgeCreated;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.serverpackets.PledgeSkillListAdd;
import net.sf.l2j.gameserver.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.serverpackets.ServerBasePacket;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;

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
    private int _auctionBiddedAt = 0;
    
    private ItemContainer _warehouse = new ClanWarehouse(this);
    private List<Integer> _atWarWith = new FastList<Integer>();

	private boolean _hasCrestLarge;

	private Forum _Forum;

	private List<L2Skill> _skills = new FastList<L2Skill>(); 
    
	//  Clan Privileges  
    public static final int CP_NOTHING = 0;  
    public static final int CP_CL_JOIN_CLAN = 2; // Join clan  
    public static final int CP_CL_GIVE_TITLE = 4; // Give a title  
    public static final int CP_CL_VIEW_WAREHOUSE = 8; // View warehouse content 
    public static final int CP_CL_MANAGE_RANKS = 16; // Manage clan ranks 
    public static final int CP_CL_PLEDGE_WAR = 32;  
    public static final int CP_CL_DISMISS = 64; 
    public static final int CP_CL_REGISTER_CREST = 128; // Register clan crest
    public static final int CP_CL_MASTER_RIGHTS = 256;
    public static final int CP_CL_MANAGE_LEVELS = 512;
    public static final int CP_CH_OPEN_DOOR = 1024; // open a door  
    public static final int CP_CH_OTHER_RIGHTS = 2048; 
    public static final int CP_CH_AUCTION = 4096;    
    public static final int CP_CH_DISMISS = 8192; 
    public static final int CP_CH_SET_FUNCTIONS = 16384;
    public static final int CP_CS_OPEN_DOOR = 32768;  
    public static final int CP_CS_MANOR_ADMIN = 65536;  
    public static final int CP_CS_MANAGE_SIEGE = 131072;
    public static final int CP_CS_USE_FUNCTIONS = 262144;
    public static final int CP_CS_DISMISS = 524288;  
    public static final int CP_CS_TAXES =1048576;
    public static final int CP_CS_MERCENARIES =2097152;
    public static final int CP_CS_SET_FUNCTIONS =4194304;
    public static final int CP_ALL = 8388606;
    
    // Sub-unit types
    public static final int SUBUNIT_ACADEMY = -1;
    public static final int SUBUNIT_ROYAL1 = 100; 
    public static final int SUBUNIT_ROYAL2 = 200; 
    public static final int SUBUNIT_KNIGHT1 = 1001; 
    public static final int SUBUNIT_KNIGHT2 = 1002; 
    public static final int SUBUNIT_KNIGHT3 = 2001; 
    public static final int SUBUNIT_KNIGHT4 = 2002; 
    
    /** FastMap(Integer, L2Skill) containing all skills of the L2Clan */
    protected final Map<Integer, L2Skill> _Skills = new FastMap<Integer, L2Skill>();
    protected final Map<Integer, RankPrivs> _Privs = new FastMap<Integer, RankPrivs>();
    protected final Map<Integer, SubPledge> _SubPledges = new FastMap<Integer, SubPledge>();
    
    private int _reputationScore = 0;
    private int _rank = 0;
    
    /**
     * called if a clan is referenced only by id.
     * in this case all other data needs to be fetched from db
     * @param clanId
     */
    public L2Clan(int clanId)
    {
        _clanId = clanId;
        InitializePrivs();
        restore();
        getWarehouse().restore();
    }
    
    /**
     * this is only called if a new clan is created
     * @param clanId
     * @param clanName
     * @param leader
     */
    public L2Clan(int clanId, String clanName)
    {
        _clanId = clanId;
        _name = clanName;
        InitializePrivs();
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
		L2ClanMember member = new L2ClanMember(this,player.getName(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), player.getPledgeType(), player.getPowerGrade(), player.getTitle());
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
	        
	        statement = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?");
            statement.setInt(1, getClanId());
            statement.execute();
            statement.close();

            statement = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?");
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
		int leadssubpledge = getLeaderSubPledge(name);
		if (leadssubpledge != 0)
		{
			// Sub-unit leader withdraws, position becomes vacant and leader
			// should appoint new via NPC
			getSubPledge(leadssubpledge).setLeaderName("");
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
	
	public int getSubPledgeMembersCount(int subpl)
	{
		int result = 0;
		for (L2ClanMember temp : _members.values())
		{
			if (temp.getPledgeType() == subpl) result++;
		}
		return result;
	}
	
	public int getMaxNrOfMembers(int pledgetype)
	{
		int limit = 0;
		
		switch (getLevel())
        {
        case 4:
            limit   = 30;
            break;
        case 3:
            limit   = 25;
            break;
        case 2:
            limit   = 20;
            break;
        case 1:
            limit   = 15;
            break;
        case 0:
            limit   = 10;
            break;
        default:
            limit   = 40;
        break;
        }
        
        switch (pledgetype)
        {
            case -1:
            case 100:
            case 200:
                limit   = 20;
                break;
            case 1001:
            case 1002:
            case 2001:
            case 2002:
                limit   = 10;
                break;
        }
        
        return limit;
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
			PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET leader_id=?,ally_id=?,ally_name=?,reputation_score=? WHERE clan_id=?");
			statement.setInt(1, getLeaderId());
			statement.setInt(2, getAllyId());
			statement.setString(3, getAllyName());
			statement.setInt(4, getReputationScore());
			statement.setInt(5, getClanId());
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
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid=?,subpledge=? WHERE obj_Id=?");
            statement.setInt(1, getClanId());
            statement.setInt(2, member.getPledgeType());
            statement.setInt(3, member.getObjectId());
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
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid=0, allyId=0, title=?, clan_privs=0, wantspeace=0, subpledge=0 WHERE obj_Id=?");
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
            PreparedStatement statement = con.prepareStatement("SELECT clan_name,clan_level,hasCastle,hasHideout,ally_id,ally_name,leader_id,crest_id,crest_large_id,ally_crest_id,reputation_score,auction_bid_at FROM clan_data where clan_id=?");
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
                setReputationScore(clanData.getInt("reputation_score"), false);
                setAuctionBiddedAt(clanData.getInt("auction_bid_at"));
                
                int leaderId = (clanData.getInt("leader_id"));          

                PreparedStatement statement2 = con.prepareStatement("SELECT char_name,level,classid,obj_Id,title,power_grade,subpledge FROM characters WHERE clanid=?");
                statement2.setInt(1, getClanId());
                ResultSet clanMembers = statement2.executeQuery();
                
                while (clanMembers.next())
                {
                	member = new L2ClanMember(this, clanMembers.getString("char_name"), clanMembers.getInt("level"), clanMembers.getInt("classid"), clanMembers.getInt("obj_id"),clanMembers.getInt("subpledge"), clanMembers.getInt("power_grade"), clanMembers.getString("title"));
                    if (member.getObjectId() == leaderId)
                    	setLeader(member);
                    else
                        addClanMember(member);
                }               
                clanMembers.close();
                statement2.close();              
            }

            clanData.close();
            statement.close();
            
            if (getName() != null)
            	_log.config("Restored clan data for \"" + getName() + "\" from database.");
            restorewars();
            restoreSubPledges();
            restoreRankPrivs();
            restoreSkills();
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
    
    private void restoreSkills()
    {
        java.sql.Connection con = null;
        
        try
        {
            // Retrieve all skills of this L2PcInstance from the database
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT skill_id,skill_level FROM clan_skills WHERE clan_id=?");
            statement.setInt(1, getClanId());

            ResultSet rset = statement.executeQuery();
            
            // Go though the recordset of this SQL query
            while (rset.next())
            {
                int id = rset.getInt("skill_id");
                int level = rset.getInt("skill_level");
                // Create a L2Skill object for each record
                L2Skill skill = SkillTable.getInstance().getInfo(id, level);
                // Add the L2Skill object to the L2Clan _skills
                _Skills.put(skill.getId(), skill);
            }
            
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("Could not restore clan skills: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    /** used to retrieve all skills */
    public final L2Skill[] getAllSkills()
    {
        if (_Skills == null)
            return new L2Skill[0];
        
        return _Skills.values().toArray(new L2Skill[_Skills.values().size()]);
    }
    
    
    /** used to add a skill to skill list of this L2Clan */
    public L2Skill addSkill(L2Skill newSkill)
    {
        L2Skill oldSkill    = null;
        
        if (newSkill != null)
        {
            // Replace oldSkill by newSkill or Add the newSkill
            oldSkill = _Skills.put(newSkill.getId(), newSkill);
        }
        
        return oldSkill;
    }
    
    /** used to add a new skill to the list, send a packet to all online clan members, update their stats and store it in db*/
    public L2Skill addNewSkill(L2Skill newSkill)
    {
        L2Skill oldSkill    = null;
        java.sql.Connection con = null;
        
        if (newSkill != null)
        {
            
            // Replace oldSkill by newSkill or Add the newSkill
            oldSkill = _Skills.put(newSkill.getId(), newSkill);
            
            
            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement;
                
                if (oldSkill != null)
                {
                    statement = con.prepareStatement("UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?");
                    statement.setInt(1, newSkill.getLevel());
                    statement.setInt(2, oldSkill.getId());
                    statement.setInt(3, getClanId());
                    statement.execute();
                    statement.close();
                }
                else
                {
                    statement = con.prepareStatement("INSERT INTO clan_skills (clan_id,skill_id,skill_level,skill_name) VALUES (?,?,?,?)");
                    statement.setInt(1, getClanId());
                    statement.setInt(2, newSkill.getId());
                    statement.setInt(3, newSkill.getLevel());
                    statement.setString(4, newSkill.getName());
                    statement.execute();
                    statement.close();
                }
            }
            catch (Exception e)
            {
                _log.warning("Error could not store char skills: " + e);
            }
            finally
            {
                try { con.close(); } catch (Exception e) {}
            }
            
                
            for (L2ClanMember temp : _members.values())
            {
                if (temp.isOnline() && temp.getPlayerInstance()!=null)
                {
                    if (newSkill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass())
                    {
                    	temp.getPlayerInstance().addSkill(newSkill, false); // Skill is not saved to player DB
                        temp.getPlayerInstance().sendPacket(new PledgeSkillListAdd(newSkill.getId(), newSkill.getLevel()));
                    }
                }
            }
        }
        
        return oldSkill;
    }
    
    
    public void addSkillEffects()
    {
        for(L2Skill skill : _Skills.values())
        {
            for (L2ClanMember temp : _members.values())
            {
                if (temp.isOnline() && temp.getPlayerInstance()!=null)
                {
                    if (skill.getMinPledgeClass() <= temp.getPlayerInstance().getPledgeClass())
                    	temp.getPlayerInstance().addSkill(skill, false); // Skill is not saved to player DB
                }
            }
        }
    }
    
    public void addSkillEffects(L2PcInstance cm)
    {
        if (cm == null)
            return;
        
        for(L2Skill skill : _Skills.values())
        {
            //TODO add skills according to members class( in ex. don't add Clan Agillity skill's effect to lower class then Baron)
            if (skill.getMinPledgeClass() <= cm.getPledgeClass())
            	cm.addSkill(skill, false); // Skill is not saved to player DB
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
	
	public class SubPledge
    {
       private int _Id;
       private String _Name;
       private String _LeaderName;
       
       public SubPledge(int id, String name, String leaderName)
       {
           _Id = id;
           _Name = name;
           _LeaderName = leaderName;
       }
       
       public int getId()
       {
           return _Id;
       }
       public String getName()
       {
           return _Name;
       }
       public String getLeaderName()
       {
           return _LeaderName;
       }
       
       public void setLeaderName(String leaderName)
       {
           _LeaderName = leaderName;
       }
    }
    public class RankPrivs
    {
       @SuppressWarnings("hiding")
       private int _rank;
       private int _party;// TODO find out what this stuff means and implement it
       private int _privs;
       
       public RankPrivs(int rank, int party, int privs)
       {
           _rank = rank;
           _party = party;
           _privs = privs;
       }
       
       public int getRank()
       {
           return _rank;
       }
       public int getParty()
       {
           return _party;
       }
       public int getPrivs()
       {
           return _privs;
       }
       public void setPrivs(int privs)
       {
           _privs = privs;
       }
    }
    
    private void restoreSubPledges()
    {
        java.sql.Connection con = null;
        
        try
        {
            // Retrieve all skills of this L2PcInstance from the database
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT sub_pledge_id,name,leader_name FROM clan_subpledges WHERE clan_id=?");
            statement.setInt(1, getClanId());
            //_log.warning("subPledge restore for ClanId : "+getClanId());
            ResultSet rset = statement.executeQuery();
            
            // Go though the recordset of this SQL query
            while (rset.next())
            {
                int id = rset.getInt("sub_pledge_id");
                String name = rset.getString("name");
                String leaderName = rset.getString("leader_name");
                // Create a SubPledge object for each record
                SubPledge pledge = new SubPledge(id, name, leaderName);
                _SubPledges.put(id, pledge);
            }
            
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("Could not restore clan sub-units: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    /** used to retrieve all subPledges */
    public final SubPledge getSubPledge(int pledgeType)
    {
        if (_SubPledges == null)
            return null;
        
        return _SubPledges.get(pledgeType);
    }
    
    /** used to retrieve all subPledges */
    public final SubPledge[] getAllSubPledges()
    {
        if (_SubPledges == null)
            return new SubPledge[0];
        
        return _SubPledges.values().toArray(new SubPledge[_SubPledges.values().size()]);
    }
    
    public int createSubPledge(int pledgeType, String leaderName, String subPledgeName)
    {
        pledgeType = getAvailablePledgeTypes(pledgeType);
        if (pledgeType == 0)
        {
            return 0;
        }
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("INSERT INTO clan_subpledges (clan_id,sub_pledge_id,name,leader_name) values (?,?,?,?)");
            statement.setInt(1, getClanId());
            statement.setInt(2, pledgeType);
            statement.setString(3, subPledgeName);
            if (pledgeType != -1)
                statement.setString(4, leaderName);
            else
                statement.setString(4, "");
            statement.execute();
            statement.close();
            
            _SubPledges.put(pledgeType, new SubPledge(pledgeType, subPledgeName, leaderName));
            
            if (Config.DEBUG) _log.fine("New sub_clan saved in db: "+getClanId()+"; "+pledgeType);
        }
        catch (Exception e)
        {
            _log.warning("error while saving new sub_clan to db "+e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
        broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(new SubPledge(pledgeType, subPledgeName, leaderName)));
        return 1;
    }
    
    public int getAvailablePledgeTypes(int pledgeType)
    {
    	if (_SubPledges.get(pledgeType) != null)
    	{
    		//_log.warning("found sub-unit with id: "+pledgeType);
    		switch(pledgeType)
    		{
    			case SUBUNIT_ACADEMY:
    				return 0;
    			case SUBUNIT_ROYAL1:
    				pledgeType = getAvailablePledgeTypes(SUBUNIT_ROYAL2);
    				break;
    			case SUBUNIT_ROYAL2:
    				return 0;
    			case SUBUNIT_KNIGHT1:
    				pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT2);
    				break;
    			case SUBUNIT_KNIGHT2:
    				pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT3);
    				break;
                case SUBUNIT_KNIGHT3:
                	pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT4);
                	break;
                case SUBUNIT_KNIGHT4:
                	return 0;
    		}
    	}
        return pledgeType;
    }
    
    private void restoreRankPrivs()
    {
        java.sql.Connection con = null;
        
        try
        {
            // Retrieve all skills of this L2PcInstance from the database
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT privs,rank,party FROM clan_privs WHERE clan_id=?");
            statement.setInt(1, getClanId());
            //_log.warning("clanPrivs restore for ClanId : "+getClanId());
            ResultSet rset = statement.executeQuery();
            
            // Go though the recordset of this SQL query
            while (rset.next())
            {
                int rank = rset.getInt("rank");
                //int party = rset.getInt("party");
                int privileges = rset.getInt("privs");
                // Create a SubPledge object for each record
                //RankPrivs privs = new RankPrivs(rank, party, privileges);
                //_Privs.put(rank, privs);
                _Privs.get(rank).setPrivs(privileges);
            }
            
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("Could not restore clan privs by rank: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    
    public void InitializePrivs()
    {
    	RankPrivs privs;
    	for (int i=1; i < 10; i++) 
    	{
    		privs = new RankPrivs(i, 0, CP_NOTHING);
    		_Privs.put(i, privs);
    	}
    		
    }
    
    public int getRankPrivs(int rank)
    {
        if (_Privs.get(rank) != null)
            return _Privs.get(rank).getPrivs();
        else
            return CP_NOTHING;
    }
    public void setRankPrivs(int rank, int privs)
    {
        if (_Privs.get(rank)!= null)
        {
            _Privs.get(rank).setPrivs(privs);
            
            
            java.sql.Connection con = null;
            
            try
            {
                //_log.warning("requested store clan privs in db for rank: "+rank+", privs: "+privs);
                // Retrieve all skills of this L2PcInstance from the database
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement("INSERT INTO clan_privs (clan_id,rank,party,privs) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE privs = ?");
                statement.setInt(1, getClanId());
                statement.setInt(2, rank);
                statement.setInt(3, 0);
                statement.setInt(4, privs);
                statement.setInt(5, privs);
                
                statement.execute();
                statement.close();
            }
            catch (Exception e)
            {
                _log.warning("Could not store clan privs for rank: " + e);
            }
            finally
            {
                try { con.close(); } catch (Exception e) {}
            }
            for (L2ClanMember cm : getMembers())
            {
                if (cm.isOnline())
                    if (cm.getPowerGrade() == rank)
                        if (cm.getPlayerInstance() != null)
                        {
                            cm.getPlayerInstance().setClanPrivileges(privs);
                            cm.getPlayerInstance().sendPacket(new UserInfo(cm.getPlayerInstance()));
                        }
            }
        }
        else
        {
            _Privs.put(rank, new RankPrivs(rank, 0, privs));
            
            java.sql.Connection con = null;
            
            try
            {
                //_log.warning("requested store clan new privs in db for rank: "+rank);
                // Retrieve all skills of this L2PcInstance from the database
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement("INSERT INTO clan_privs (clan_id,rank,party,privs) VALUES (?,?,?,?)");
                statement.setInt(1, getClanId());
                statement.setInt(2, rank);
                statement.setInt(3, 0);
                statement.setInt(4, privs);
                statement.execute();
                statement.close();
            }
            catch (Exception e)
            {
                _log.warning("Could not create new rank and store clan privs for rank: " + e);
            }
            finally
            {
                try { con.close(); } catch (Exception e) {}
            }
        }
    }
    
    /** used to retrieve all RankPrivs */
    public final RankPrivs[] getAllRankPrivs()
    {
        if (_Privs == null)
            return new RankPrivs[0];
        
        return _Privs.values().toArray(new RankPrivs[_Privs.values().size()]);
    }
    
    public int getLeaderSubPledge(String name)
    {
        int id = 0;
        for (SubPledge sp : _SubPledges.values())
        {
            if (sp.getLeaderName() == name)
                id = sp.getId();
        }
        return id;
    }

    public void setReputationScore(int value, boolean save)
    {
    	if(_reputationScore > 100000000) _reputationScore = 100000000;
    	if(_reputationScore < -100000000) _reputationScore = -100000000;
    	_reputationScore = value;
    	if (save) updateClanInDB();
    }
    
    public int getReputationScore()
    {
    	return _reputationScore;
    }
    
    public void setRank(int rank) 
    { 
    	_rank = rank; 
    } 	
    
    public int getRank() 
    { 
    	return _rank; 
    } 
    public int getAuctionBiddedAt()
    {
        return _auctionBiddedAt;
    }
    
    public void setAuctionBiddedAt(int id)
    {
        _auctionBiddedAt = id;
        //store changes to DB
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET auction_bid_at=? WHERE clan_id=?");
            statement.setInt(1, id);
            statement.setInt(2, getClanId());
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("Could not store auction for clan: " + e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    } 
}
