package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.model.entity.Zone;
import net.sf.l2j.gameserver.util.Util;

public class ClanHallManager
{
    protected static Logger _log = Logger.getLogger(ClanHallManager.class.getName());

    // =========================================================
    private static ClanHallManager _Instance;
    public static final ClanHallManager getInstance()
    {
        if (_Instance == null)
        {
    		System.out.println("Initializing ClanHallManager");
        	_Instance = new ClanHallManager();
        	_Instance.load();
        }
        return _Instance;
    }
    // =========================================================

    
    // =========================================================
    // Data Field
    private List<ClanHall> _ClanHalls;
    
    // =========================================================
    // Constructor
    public ClanHallManager()
    {
    }

    // =========================================================
    // Method - Public
    /** Return true if object is inside zone */
    public final boolean checkIfInZone(L2Object obj) { return (getClanHall(obj) != null); }

    /** Return true if object is inside zone */
    public final boolean checkIfInZone(int x, int y) { return (getClanHall(x, y) != null); }

    public void reload()
    {
    	this.getClanHalls().clear();
    	this.load();
    }

    // =========================================================
    // Method - Private
    private final void load()
    {
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("Select id from clanhall order by id");
            rs = statement.executeQuery();

            while (rs.next())
            {
            	getClanHalls().add(new ClanHall(rs.getInt("id")));
            }

            statement.close();

            System.out.println("Loaded: " + getClanHalls().size() + " clan halls");
        }
        catch (Exception e)
        {
            System.out.println("Exception: ClanHallManager.load(): " + e.getMessage());
            e.printStackTrace();
        }
        
        finally {try { con.close(); } catch (Exception e) {}}
    }

    // =========================================================
    // Property - Public
    public final ClanHall getClanHall(int clanHallId)
    {
        int index = getClanHallIndex(clanHallId);
        if (index >= 0) return getClanHalls().get(index);
        return null;
    }

    public final ClanHall getClanHall(L2Object activeObject) { return getClanHall(activeObject.getPosition().getX(), activeObject.getPosition().getY()); }

    public final ClanHall getClanHall(int x, int y)
    {
        int index = getClanHallIndex(x, y);
        if (index >= 0) return getClanHalls().get(index);
        return null;
    }
    public final ClanHall getClanHall(int x, int y, int offset)
    {
        int index = getClanHallIndex(x, y, offset);
        if (index >= 0) return getClanHalls().get(index);
        return null;
    }

    public final ClanHall getClanHall(String name)
    {
        int index = getClanHallIndex(name);
        if (index >= 0) return getClanHalls().get(index);
        return null;
    }

    public final ClanHall getClanHallByOwner(L2Clan clan)
    {
	for (ClanHall clanhall : getClanHalls())
	    if (clan.getClanId() == clanhall.getOwnerId())
		return clanhall;
        return null;
    }

    public final int getClanHallIndex(int clanHallId)
    {
        ClanHall clanHall;
        for (int i = 0; i < getClanHalls().size(); i++)
        {
            clanHall = getClanHalls().get(i);
            if (clanHall != null && clanHall.getId() == clanHallId) return i;
        }
        return -1;
    }

    public final int getClanHallIndex(L2Object activeObject) { return getClanHallIndex(activeObject.getPosition().getX(), activeObject.getPosition().getY()); }

    public final int getClanHallIndex(int x, int y)
    {
        ClanHall clanHall;
        for (int i = 0; i < getClanHalls().size(); i++)
        {
            clanHall = getClanHalls().get(i);
            if (clanHall != null && clanHall.checkIfInZone(x, y)) return i;
        }
        return -1;
    }
    public final int getClanHallIndex(int x, int y, int offset)
    {
        ClanHall clanHall;
	int id = -1;
        for (int i = 0; i < getClanHalls().size(); i++)
        {
            clanHall = getClanHalls().get(i);
	    int[] coord;
	    Zone zone = clanHall.getZone();
            if (zone != null)
            {
        	coord = zone.getCoords().get(0);
		int x1 = coord[0] + (coord[2] - coord[0])/2;
		int y1 = coord[1] + (coord[3] - coord[1])/2;
		//_log.warning("ch"+i+":("+x+","+y+") distance "+Util.calculateDistance(x,y,0,coord[0],coord[1]));
		if (clanHall != null && Util.calculateDistance(x,y,0,x1,y1) < offset){ 
		    id = i;
		    offset = (int)Util.calculateDistance(x,y,0,x1,y1);
		}
	    }
        }
        return id;
    }

    public final int getClanHallIndex(String name)
    {
        ClanHall clanHall;
        for (int i = 0; i < getClanHalls().size(); i++)
        {
            clanHall = getClanHalls().get(i);
            if (clanHall != null && clanHall.getName().equalsIgnoreCase(name.trim())) return i;
        }
        return -1;
    }

    public final int getClanHallIndexByOwner(L2Clan clan)
    {
        if (clan == null) return -1;
        
        ClanHall clanHall;
        for (int i = 0; i < getClanHalls().size(); i++)
        {
            clanHall = getClanHalls().get(i);
            if (clanHall != null && clanHall.getOwnerId() == clan.getClanId()) return i;
        }
        return -1;
    }

    public final List<ClanHall> getClanHalls()
    {
        if (_ClanHalls == null) _ClanHalls = new FastList<ClanHall>();
        return _ClanHalls;
    }
}
