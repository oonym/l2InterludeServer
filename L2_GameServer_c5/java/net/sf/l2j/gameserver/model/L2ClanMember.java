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
import java.sql.SQLException;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * 
 * @version $Revision: 1.5.4.2 $ $Date: 2005/03/27 15:29:33 $
 */
public class L2ClanMember
{
	private L2Clan _clan;
	private int _objectId;
	private String _name;
	private String _title;
	private int _powerGrade;
	private int _level;
	private int _classId;
	private L2PcInstance _player;
	private int _pledgeType;
	private int _apprentice;
	private int _sponsor;
	
	public L2ClanMember(L2Clan clan, String name, int level, int classId, int objectId, int pledgeType, int powerGrade, String title)
	{
		if(clan == null)
			throw new IllegalArgumentException("Can not create a ClanMember with a null clan.");
		_clan = clan;
		_name = name;
		_level = level;
		_classId = classId;
		_objectId = objectId;
		_powerGrade = powerGrade;
		_title = title;
		_pledgeType = pledgeType;
		_apprentice = 0;
		_sponsor = 0;

	}
	
	public L2ClanMember(L2PcInstance player)
	{
		if(player.getClan() == null)
			throw new IllegalArgumentException("Can not create a ClanMember if player has a null clan.");
		_clan = player.getClan();
		_player = player;
		_name = _player.getName();
		_level = _player.getLevel();
		_classId = _player.getClassId().getId();
		_objectId = _player.getObjectId();
		_powerGrade = _player.getPowerGrade();
		_pledgeType = _player.getPledgeType();
		_title = _player.getTitle();
		_apprentice = 0;
		_sponsor = 0;
	}

		
	public void setPlayerInstance(L2PcInstance player)
	{
		if (player == null && _player != null)
		{
			// this is here to keep the data when the player logs off
			_name = _player.getName();
			_level = _player.getLevel();
			_classId = _player.getClassId().getId();
			_objectId = _player.getObjectId();
			_powerGrade = _player.getPowerGrade();
			_title = _player.getTitle();
		}
		if (player != null) {
			L2Skill[] skills = _clan.getAllSkills();
			for (L2Skill sk : skills) 
			{
				if(sk.getMinPledgeClass() <= player.getPledgeClass())
					player.addSkill(sk, false);
			}
		}
		_player = player;
	}

	public L2PcInstance getPlayerInstance()
	{
		return _player;
	}
	
	public boolean isOnline()
	{
		return _player != null;
	}
	
	/**
	 * @return Returns the classId.
	 */
	public int getClassId()
	{
		if (_player != null)
		{
			return _player.getClassId().getId();
		}
        return _classId;
	}

	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		if (_player != null)
		{
			return _player.getLevel();
		}
        return _level;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		if (_player != null)
		{
			return _player.getName();
		}
        return _name;
	}

	/**
	 * @return Returns the objectId.
	 */
	public int getObjectId()
	{
		if (_player != null)
		{
			return _player.getObjectId();
		}
        return _objectId;
	}
	
	public String getTitle() {
		if (_player != null) {
			return _player.getTitle();
		}
        return _title;
	}
	
	public int getPledgeType()
    {
        if (_player != null)
        {
            return _player.getPledgeType();
        }
        return _pledgeType;
    }
	
	public int getPowerGrade()
	{
		if(_player != null)
			return _player.getPowerGrade();
		return _powerGrade;
	}

	/**
	 * @param powerGrade
	 */
	public void setPowerGrade(int powerGrade)
	{
		_powerGrade = powerGrade;
		if(_player != null)
		{
			_player.setPowerGrade(powerGrade);
		}
		else
		{
			// db save if char not logged in
			updatePowerGrade();
		}
	}
	
	/**
	 * Update the characters table of the database with power grade.<BR><BR>
	 */
	public void updatePowerGrade()
	{
		java.sql.Connection con = null;
		
		try 
        {
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE characters SET power_grade=? WHERE obj_id=?");
			statement.setLong(1, _powerGrade);
			statement.setInt(2, getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			//_log.warning("could not set char power_grade:"+e);
		}
		finally
		{
			try { con.close(); } catch (Exception e) {}
		}
	}
	
	public void initApprenticeAndSponsor(int apprenticeID, int sponsorID)
	{
		_apprentice = apprenticeID;
		_sponsor = sponsorID;
	}

	public int getSponsor()
	{
		if (_player != null) return _player.getSponsor();
		else return _sponsor;
	}
	
	public int getApprentice()
	{
		if (_player != null) return _player.getApprentice();
		else return _apprentice;
	}
	
	public String getApprenticeOrSponsorName() 
	{ 
		if(_player != null) 
		{
			_apprentice = _player.getApprentice();
			_sponsor = _player.getSponsor();
		}
		
		if(_apprentice != 0)
		{
			L2ClanMember apprentice = _clan.getClanMember(_apprentice);
			if(apprentice != null) return apprentice.getName();
			else return "Error";
		}
		if(_sponsor != 0)
		{
			L2ClanMember sponsor = _clan.getClanMember(_sponsor);
			if(sponsor != null) return sponsor.getName();
			else return "Error";
		}
		return "";
	} 
	
	public L2Clan getClan() 
	{ 
		return _clan; 
	} 
}
