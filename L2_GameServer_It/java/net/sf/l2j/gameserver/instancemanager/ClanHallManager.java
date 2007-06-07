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
import java.util.logging.Logger;

import javolution.util.FastMap;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Zone;
import net.sf.l2j.gameserver.util.Util;

/**
 *
 * @author  Steuf
 */
public class ClanHallManager
{
	private static Logger _log = Logger.getLogger(ClanHallManager.class.getName());
	
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
	public boolean loaded(){
		return _loaded;
	}
	private ClanHallManager()
	{
		_clanHall = new FastMap<Integer, ClanHall>();
		_freeClanHall = new FastMap<Integer, ClanHall>();
		load();
	}
	/** Reload All Clan Hall */
	public final void reload(){
		_clanHall.clear();
		_freeClanHall.clear();
		load();
	}
	/** Load All Clan Hall */
	private final void load(){
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
            		
            		if(ClanTable.getInstance().getClan(rs.getInt("ownerId")) != null){
            			_clanHall.put(id,new ClanHall(id,rs.getString("name"),rs.getInt("ownerId"),rs.getInt("lease"),rs.getString("desc"),rs.getString("location"),rs.getLong("paidUntil"),rs.getInt("Grade")));
            			ClanTable.getInstance().getClan(rs.getInt("ownerId")).setHasHideout(id);
            		}else{
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
	public final Map<Integer, ClanHall> getFreeClanHalls(){
		return _freeClanHall;
	}
	/** Get Map with all ClanHalls */
	public final Map<Integer, ClanHall> getClanHalls(){
		return _clanHall;
	}
	/** Check is free ClanHall */
	public final boolean isFree(int chId){
		if(_freeClanHall.containsKey(chId))
			return true;
		return false;
	}
	/** Free a ClanHall */
	public final void setFree(int chId){
		_freeClanHall.put(chId,_clanHall.get(chId));
		ClanTable.getInstance().getClan(_freeClanHall.get(chId).getOwnerId()).setHasHideout(0);
		_freeClanHall.get(chId).free();
		_clanHall.remove(chId);
	}
	/** Set ClanHallOwner */
	public final void setOwner(int chId, L2Clan clan){
		if(!_clanHall.containsKey(chId)){
			_clanHall.put(chId,_freeClanHall.get(chId));
			_freeClanHall.remove(chId);
		}else
			_clanHall.get(chId).free();
		ClanTable.getInstance().getClan(clan.getClanId()).setHasHideout(chId);
		_clanHall.get(chId).setOwner(clan);
	}
    /** Return true if object is inside zone */
    public final boolean checkIfInZone(L2Object obj) { 
    	return (getClanHall(obj) != null); 
    }
    /** Return true if object is inside zone */
    public final boolean checkIfInZone(int x, int y) { 
    	return (getClanHall(x, y) != null); 
    }
    /** Get Clan Hall by Id */
    public final ClanHall getClanHall(int clanHallId)
    {
    	if(_clanHall.containsKey(clanHallId))
    		return _clanHall.get(clanHallId);
    	if(_freeClanHall.containsKey(clanHallId))
    		return _freeClanHall.get(clanHallId);
        return null;
    }
    /** Get Clan Hall by Object */
    public final ClanHall getClanHall(L2Object activeObject) 
    { 
    	return getClanHall(activeObject.getPosition().getX(), activeObject.getPosition().getY()); 
    }
    /** Get Clan Hall by region x,y */
    public final ClanHall getClanHall(int x, int y)
    {
        int index = getClanHallIndex(x, y,_clanHall);
        if (index >= 0) return _clanHall.get(index);
        index = getClanHallIndex(x, y,_freeClanHall);
        if (index >= 0) return _freeClanHall.get(index);
        return null;
    }
    /** Get Clan Hall by region x,y,offset */
    public final ClanHall getClanHall(int x, int y, int offset)
    {
        int index = getClanHallIndex(x, y, offset,_clanHall);
        if (index >= 0) return _clanHall.get(index);
        index = getClanHallIndex(x, y, offset,_freeClanHall);
        if (index >= 0) return _freeClanHall.get(index);
        return null;
    }
    /** Get Clan Hall by name */
    public final ClanHall getClanHall(String name, Map<Integer,ClanHall> clanHall)
    {
        int index = getClanHallIndex(name,_clanHall);
        if (index >= 0) return getClanHalls().get(index);
        index = getClanHallIndex(name,_freeClanHall);
        if (index >= 0) return _freeClanHall.get(index);
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
    /** ClanHallId By Region x,y */
    private final int getClanHallIndex(int x, int y, Map<Integer,ClanHall> clanHall)
    {
        for (Map.Entry<Integer, ClanHall> ch : clanHall.entrySet())
            if (clanHall != null && ch.getValue().checkIfInZone(x, y)) 
            	return ch.getKey();
        return -1;
    }
    /** ClanHallId by region x,y,offset */
    private final int getClanHallIndex(int x, int y, int offset, Map<Integer,ClanHall> clanHall)
    {
        int id = -1;
        for (Map.Entry<Integer, ClanHall> ch : clanHall.entrySet())
        {
            int[] coord;
            Zone zone = ch.getValue().getZone();
            if (zone != null)
            {
            	coord = zone.getCoords().get(0);
            	int x1 = coord[0] + (coord[2] - coord[0])/2;
            	int y1 = coord[1] + (coord[3] - coord[1])/2;
            	if (Config.DEBUG)
            		_log.warning("ch"+ch.getKey()+":("+x+","+y+") distance "+Util.calculateDistance(x,y,0,coord[0],coord[1]));
            	if (clanHall != null && Util.calculateDistance(x,y,0,x1,y1) < offset)
            	{ 
            		id = ch.getKey();
            		offset = (int)Util.calculateDistance(x,y,0,x1,y1);
            	}
            }
        }
        return id;
    }
    /** ClanHallId by name */
    private final int getClanHallIndex(String name, Map<Integer,ClanHall> clanHall)
    {
        for (Map.Entry<Integer, ClanHall> ch : clanHall.entrySet())
            if (ch.getValue().getName().equalsIgnoreCase(name.trim())) 
            	return ch.getKey();
        return -1;
    }
    /** NOT USED MUST BE REMOVED ? */
    /** ClanHall Id by Owner : NOT USED */
    /*private final int getClanHallIndexByOwner(L2Clan clan)
    {
        if (clan == null) return -1;
        ClanHall clanHall;
        for (Map.Entry<Integer, ClanHall> ch : _clanHall.entrySet())
        {
            clanHall = ch.getValue();
            if (clanHall != null && clanHall.getOwnerId() == clan.getClanId()) 
            	return ch.getKey();
        }
        return -1;
    }*/
    /** Get Index Id by object : NOT USED */
    /*private final int getClanHallIndex(L2Object activeObject, Map<Integer,ClanHall> clanHall) 
    { 
    	return getClanHallIndex(activeObject.getPosition().getX(), activeObject.getPosition().getY(), clanHall);
    }*/
}
