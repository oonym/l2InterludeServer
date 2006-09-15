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
package net.sf.l2j.gameserver.communitybbs.BB;

import java.sql.PreparedStatement;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.communitybbs.Manager.TopicBBSManager;

public class Topic
{

	private static Logger _log = Logger.getLogger(Topic.class.getName());
	public static final int MORMAL = 0;
	public static final int MEMO = 1;
	
	private int _ID;
	private int _ForumID;
	private String _TopicName;
	private long _date;
	private String _OwnerName;
	private int _OwnerID;
	private int _type;
	private int _Creply;

	/**
	 * @param restaure
	 * @param i
	 * @param j
	 * @param string
	 * @param k
	 * @param string2
	 * @param l
	 * @param m
	 * @param n
	 */
	public Topic(ConstructorType ct, int id, int fid, String name, long date, String oname, int oid, int type, int Creply)
	{
			_ID = id;
			_ForumID = fid;
			_TopicName = name;
			_date = date;
			_OwnerName = oname;
			_OwnerID = oid;
			_type =  type;
			_Creply = Creply;
			TopicBBSManager.getInstance().addTopic(this);
			
	
		 if(ct == ConstructorType.CREATE)
		{
			
			 insertindb();
		}
	}

	/**
	 * 
	 */
	public void insertindb()
	{
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO topic (topic_id,topic_forum_id,topic_name,topic_date,topic_ownername,topic_ownerid,topic_type,topic_reply) values (?,?,?,?,?,?,?,?)");
			statement.setInt(1, _ID);
			statement.setInt(2, _ForumID);
			statement.setString(3, _TopicName);
			statement.setLong(4, _date);
			statement.setString(5, _OwnerName);
			statement.setInt(6, _OwnerID);
			statement.setInt(7, _type);
			statement.setInt(8, _Creply);
			statement.execute();
			statement.close();
			
		}
		catch (Exception e)
		{
			_log.warning("error while saving new Topic to db " + e);
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

	public enum ConstructorType { RESTORE , CREATE }

	/**
	 * @return
	 */
	public int getID()
	{		
		return _ID;
	}
	public int getForumID()
	{		
		return _ForumID;
	}
	/**
	 * @return
	 */
	public String getName()
	{
		// TODO Auto-generated method stub
		return _TopicName;
	}
	public String getOwnerName()
	{
		// TODO Auto-generated method stub
		return _OwnerName;
	}

	/**
	 * 
	 */
	public void deleteme(Forum f)
	{
		TopicBBSManager.getInstance().delTopic(this);
		f.RmTopicByID(getID());
		java.sql.Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM topic WHERE topic_id=? AND topic_forum_id=?");
			statement.setInt(1, getID());
			statement.setInt(2, f.getID());
			statement.execute();					
			statement.close();
		}
		catch (Exception e)
		{			
			e.printStackTrace();
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

	/**
	 * @return
	 */
	public long getDate()
	{		
		return _date;
	};
}