/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.ClanHall;

/**
 *
 * @author  Steuf
 */
public class ClanHallManager
{
	private static ClanHallManager _instance;
	
	private Map<Integer, ClanHall> _clanHall;
	private Map<Integer, ClanHall> _freeClanHall;
	private boolean _loaded = false;
	
	public static ClanHallManager getInstance()
	{
		if (_instance == null)
		{
			System.out.println("Initializing ClanHallManager");
			_instance = new ClanHallManager();
		}
		return _instance;
	}
	
	public boolean loaded()
	{
		return _loaded;
	}
	
	private ClanHallManager()
	{
		_clanHall = new FastMap<Integer, ClanHall>();
		_freeClanHall = new FastMap<Integer, ClanHall>();
		load();
	}
	
	/** Reload All Clan Hall */
/*	public final void reload() Cant reload atm - would loose zone info
	{
		_clanHall.clear();
		_freeClanHall.clear();
		load();
	}
*/
	
	/** Load All Clan Hall */
	private final void load()
	{
       java.sql.Connection con = null;
        try
        {
        	int id;
            PreparedStatement statement;
            ResultSet rs;
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM clanhall ORDER BY id");
            rs = statement.executeQuery();
            while (rs.next())
            {
            	id = rs.getInt("id");
            	if(rs.getInt("ownerId") == 0)
            		_freeClanHall.put(id,new ClanHall(id,rs.getString("name"),rs.getInt("ownerId"),rs.getInt("lease"),rs.getString("desc"),rs.getString("location"),0,rs.getInt("Grade")));
            	else{
            		
            		if(ClanTable.getInstance().getClan(rs.getInt("ownerId")) != null)
            		{
            			_clanHall.put(id,new ClanHall(id,rs.getString("name"),rs.getInt("ownerId"),rs.getInt("lease"),rs.getString("desc"),rs.getString("location"),rs.getLong("paidUntil"),rs.getInt("Grade")));
            			ClanTable.getInstance().getClan(rs.getInt("ownerId")).setHasHideout(id);
            		}else
            		{
            			_freeClanHall.put(id,new ClanHall(id,rs.getString("name"),rs.getInt("ownerId"),rs.getInt("lease"),rs.getString("desc"),rs.getString("location"),rs.getLong("paidUntil"),rs.getInt("Grade")));
            			_freeClanHall.get(id).free();
            			AuctionManager.getInstance().initNPC(id);
            		}
            			
            	}
            }
            statement.close();
            System.out.println("Loaded: "+getClanHalls().size() +" clan halls");
            System.out.println("Loaded: "+getFreeClanHalls().size() +" free clan halls");
            _loaded = true;
        }
        catch (Exception e)
        {
            System.out.println("Exception: ClanHallManager.load(): " + e.getMessage());
            e.printStackTrace();
        }
        finally {try { con.close(); } catch (Exception e) {}}
	}
	
	/** Get Map with all FreeClanHalls */
	public final Map<Integer, ClanHall> getFreeClanHalls()
	{
		return _freeClanHall;
	}
	
	/** Get Map with all ClanHalls */
	public final Map<Integer, ClanHall> getClanHalls()
	{
		return _clanHall;
	}
	
	/** Check is free ClanHall */
	public final boolean isFree(int chId)
	{
		if(_freeClanHall.containsKey(chId))
			return true;
		return false;
	}
	
	/** Free a ClanHall */
	public final synchronized void setFree(int chId)
	{
		_freeClanHall.put(chId,_clanHall.get(chId));
		ClanTable.getInstance().getClan(_freeClanHall.get(chId).getOwnerId()).setHasHideout(0);
		_freeClanHall.get(chId).free();
		_clanHall.remove(chId);
	}
	
	/** Set ClanHallOwner */
	public final synchronized void setOwner(int chId, L2Clan clan)
	{
		if(!_clanHall.containsKey(chId))
		{
			_clanHall.put(chId,_freeClanHall.get(chId));
			_freeClanHall.remove(chId);
		}else
			_clanHall.get(chId).free();
		ClanTable.getInstance().getClan(clan.getClanId()).setHasHideout(chId);
		_clanHall.get(chId).setOwner(clan);
	}
	
    /** Get Clan Hall by Id */
    public final ClanHall getClanHallById(int clanHallId)
    {
    	if(_clanHall.containsKey(clanHallId))
    		return _clanHall.get(clanHallId);
    	if(_freeClanHall.containsKey(clanHallId))
    		return _freeClanHall.get(clanHallId);
        return null;
    }
    
    /** Get Clan Hall by x,y,z *//*
    public final ClanHall getClanHall(int x, int y, int z)
    {
    	for (Map.Entry<Integer, ClanHall> ch : _clanHall.entrySet())
    		if (ch.getValue().getZone().isInsideZone(x, y, z)) return ch.getValue();
    	
    	for (Map.Entry<Integer, ClanHall> ch : _freeClanHall.entrySet())
    		if (ch.getValue().getZone().isInsideZone(x, y, z)) return ch.getValue();
    	
        return null;
    }*/
    
    public final ClanHall getNearbyClanHall(int x, int y, int maxDist)
    {
    	
    	for (Map.Entry<Integer, ClanHall> ch : _clanHall.entrySet())
    		if (ch.getValue().getZone().getDistanceToZone(x, y) < maxDist) return ch.getValue();
    	
    	for (Map.Entry<Integer, ClanHall> ch : _freeClanHall.entrySet())
    		if (ch.getValue().getZone().getDistanceToZone(x, y) < maxDist) return ch.getValue();
    	
        return null;
    }
    
    /** Get Clan Hall by Owner */
    public final ClanHall getClanHallByOwner(L2Clan clan)
    {
    	for (Map.Entry<Integer, ClanHall> ch : _clanHall.entrySet())
    		if (clan.getClanId() == ch.getValue().getOwnerId())
    			return ch.getValue();
        return null;
    }
}