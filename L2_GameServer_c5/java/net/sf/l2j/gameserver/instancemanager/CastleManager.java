package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.entity.Castle;


public class CastleManager
{
    protected static Logger _log = Logger.getLogger(CastleManager.class.getName());

    // =========================================================
    private static CastleManager _Instance;
    public static final CastleManager getInstance()
    {
        if (_Instance == null)
        {
    		System.out.println("Initializing CastleManager");
            _Instance = new CastleManager();
            _Instance.load();
        }
        return _Instance;
    }
    // =========================================================

    
    // =========================================================
    // Data Field
    private List<Castle> _Castles;
    
    // =========================================================
    // Constructor
    public CastleManager()
    {
    }

    // =========================================================
    // Method - Public
    /** Return true if object is inside zone */
    public final boolean checkIfInZone(L2Object obj) { return (getCastle(obj) != null); }

    /** Return true if object is inside zone */
    public final boolean checkIfInZone(int x, int y) { return (getCastle(x, y) != null); }

    public final int findNearestCastleIndex(L2Object activeObject)
    {
        int index = getCastleIndex(activeObject);
        if (index < 0)
        {
            double closestDistance = 99999999;
            double distance;
            Castle castle;
            for (int i = 0; i < getCastles().size(); i++)
            {
                castle = getCastles().get(i);
                if (castle == null) continue;
                distance = castle.getZone().findDistanceToZone(activeObject, false);
                if (closestDistance > distance)
                {
                    closestDistance = distance;
                    index = i;
                }
            }
        }
        return index;
    }

    public void reload()
    {
    	this.getCastles().clear();
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

            statement = con.prepareStatement("Select id from castle order by id");
            rs = statement.executeQuery();

            while (rs.next())
            {
                getCastles().add(new Castle(rs.getInt("id")));
            }

            statement.close();

            System.out.println("Loaded: " + getCastles().size() + " castles");
        }
        catch (Exception e)
        {
            System.out.println("Exception: loadCastleData(): " + e.getMessage());
            e.printStackTrace();
        }
        
        finally {try { con.close(); } catch (Exception e) {}}
    }

    // =========================================================
    // Property - Public
    public final Castle getCastle(int castleId)
    {
        int index = getCastleIndex(castleId);
        if (index >= 0) return getCastles().get(index);
        return null;
    }

    public final Castle getCastle(L2Object activeObject) { return getCastle(activeObject.getX(), activeObject.getY()); }

    public final Castle getCastle(int x, int y)
    {
        int index = getCastleIndex(x, y);
        if (index >= 0) return getCastles().get(index);
        return null;
    }

    public final Castle getCastle(String name)
    {
        int index = getCastleIndex(name);
        if (index >= 0) return getCastles().get(index);
        return null;
    }

    public final Castle getCastleByOwner(L2Clan clan)
    {
        int index = getCastleIndexByOwner(clan);
        if (index >= 0) return getCastles().get(index);
        return null;
    }

    public final Castle getCastleByTown(int townId)
    {
        int index = getCastleIndexByTown(townId);
        if (index >= 0) return getCastles().get(index);
        return null;
    }

    public final Castle getCastleByTown(L2Object activeObject) { return getCastleByTown(activeObject.getPosition().getX(), activeObject.getPosition().getY()); }

    public final Castle getCastleByTown(int x, int y)
    {
        int index = getCastleIndexByTown(x, y);
        if (index >= 0) return getCastles().get(index);
        return null;
    }

    public final Castle getCastleByTown(String name)
    {
        int index = getCastleIndexByTown(name);
        if (index >= 0) return getCastles().get(index);
        return null;
    }

    public final int getCastleIndex(int castleId)
    {
        Castle castle;
        for (int i = 0; i < getCastles().size(); i++)
        {
            castle = getCastles().get(i);
            if (castle != null && castle.getCastleId() == castleId) return i;
        }
        return -1;
    }

    public final int getCastleIndex(L2Object activeObject) { return getCastleIndex(activeObject.getPosition().getX(), activeObject.getPosition().getY()); }

    public final int getCastleIndex(int x, int y)
    {
        Castle castle;
        for (int i = 0; i < getCastles().size(); i++)
        {
            castle = getCastles().get(i);
            if (castle != null && castle.checkIfInZone(x, y)) return i;
        }
        return -1;
    }

    public final int getCastleIndex(String name)
    {
        Castle castle;
        for (int i = 0; i < getCastles().size(); i++)
        {
            castle = getCastles().get(i);
            if (castle != null && castle.getName().equalsIgnoreCase(name.trim())) return i;
        }
        return -1;
    }

    public final int getCastleIndexByOwner(L2Clan clan)
    {
        if (clan == null) return -1;
        
        Castle castle;
        for (int i = 0; i < getCastles().size(); i++)
        {
            castle = getCastles().get(i);
            if (castle != null && castle.getOwnerId() == clan.getClanId()) return i;
        }
        return -1;
    }

    public final int getCastleIndexByTown(int townId)
    {
        Castle castle;
        for (int i = 0; i < getCastles().size(); i++)
        {
            castle = getCastles().get(i);
            if (castle != null && castle.getZoneTown(townId) != null) return i;
        }
        return -1;
    }

    public final int getCastleIndexByTown(L2Object activeObject) { return getCastleIndexByTown(activeObject.getX(), activeObject.getY()); }

    public final int getCastleIndexByTown(int x, int y)
    {
        Castle castle;
        for (int i = 0; i < getCastles().size(); i++)
        {
            castle = getCastles().get(i);
            if (castle != null && castle.checkIfInZoneTowns(x, y)) return i;
        }
        return -1;
    }

    public final int getCastleIndexByTown(String name)
    {
        Castle castle;
        for (int i = 0; i < getCastles().size(); i++)
        {
            castle = getCastles().get(i);
            if (castle != null && castle.getZoneTown(name) != null) return i;
        }
        return -1;
    }

    public final List<Castle> getCastles()
    {
        if (_Castles == null) _Castles = new FastList<Castle>();
        return _Castles;
    }
}