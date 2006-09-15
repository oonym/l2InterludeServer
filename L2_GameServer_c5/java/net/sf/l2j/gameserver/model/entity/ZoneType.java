package net.sf.l2j.gameserver.model.entity;

import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Object;

public class ZoneType
{
    protected static Logger _log = Logger.getLogger(ZoneType.class.getName());

    // =========================================================
    // Data Field
    private String _TypeName;
    private List<Zone> _Zones;

    public static enum ZoneTypeEnum
    {
        Arena,
        ArenaSpawn,
        CastleArea,
        CastleDefenderSpawn,
        ClanHall,
        Peace,
        SiegeBattleField,
        Town,
        TownSpawn,
        Underground,
        Water, 
        NoLanding,
        Recharge,
        Damage,
        Fishing,
        Jail,
        MonsterDerbyTrack,
        OlympiadStadia,
        MotherTree
    }
    
    public static String[] ZoneTypeName =
    {
        "Arena", "Arena Spawn", "Castle Area",
        "Castle Defender Spawn", "Clan Hall", "Peace",
        "Siege Battlefield", "Town", "Town Spawn", "Underground",
        "Water","No Landing","Recharge","Damage","Fishing", "Jail",
        "Monster Derby Track", "Olympiad Stadia", "MotherTree"
    };
    
    public static String getZoneTypeName(ZoneTypeEnum zt)
    {
        return ZoneTypeName[zt.ordinal()];
    }

    // =========================================================
    // Constructor
    public ZoneType(String typeName)
    {
        _TypeName = typeName.trim();
    }

    // =========================================================
    // Method - Public
    public int addZone(int id, String zoneName, int taxById)
    {
        getZones().add(new Zone(id, zoneName, taxById));
        return getZones().size() - 1;
    }

    public int addZoneCoord(String zoneName, int x1, int y1, int x2, int y2, int z)
    {
        return getZone(zoneName).addCoord(x1, y1, x2, y2, z);
    }
    
    public boolean checkIfInZone(L2Object obj)
    {
        return checkIfInZone(obj.getX(), obj.getY());
    }
    
    public boolean checkIfInZone(int x, int y)
    {
        for (Zone zone: getZones())
        {
            if (zone.checkIfInZone(x, y)) return true;
        }
        return false;
    }

    // =========================================================
    // Method - Private

    // =========================================================
    // Property - Public
    public final String getTypeName()
    {
        return _TypeName;
    }
    
    public final Zone getZone(int zoneId)
    {
        for (Zone z: getZones())
            if (z.getId() == zoneId) return z;
        return null;
    }
    
    public final Zone getZone(String zoneName)
    {
        for (Zone z: getZones())
            if (z.getName().equalsIgnoreCase(zoneName.trim())) return z;

        getZones().add(new Zone(getZones().size() + 1, zoneName, 0));
        return getZones().get(getZones().size() - 1);
    }
    
    public final Zone getZone(int x, int y)
    {
        for (Zone z: getZones())
            if (z.checkIfInZone(x, y)) return z;
        return null;
    }
    
    public final List<Zone> getZones()
    {
        if (_Zones == null) _Zones = new FastList<Zone>();
        return _Zones;
    }
}