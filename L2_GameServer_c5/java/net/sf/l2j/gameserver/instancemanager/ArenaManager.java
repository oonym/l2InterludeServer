package net.sf.l2j.gameserver.instancemanager;

import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.entity.Arena;
import net.sf.l2j.gameserver.model.entity.Zone;
import net.sf.l2j.gameserver.model.entity.ZoneType;

public class ArenaManager
{
    protected static Logger _log = Logger.getLogger(ArenaManager.class.getName());

    // =========================================================
    private static ArenaManager _Instance;
    public static final ArenaManager getInstance()
    {
        if (_Instance == null)
        {
    		System.out.println("Initializing ArenaManager");
        	_Instance = new ArenaManager();
            _Instance.load();
        }
        return _Instance;
    }
    // =========================================================

    
    // =========================================================
    // Data Field
    private List<Arena> _Arenas;
    
    // =========================================================
    // Constructor
    public ArenaManager()
    {
    }

    // =========================================================
    // Method - Public
    /** Return true if object is inside zone */
    public final boolean checkIfInZone(L2Object obj) { return (getArena(obj) != null); }

    /** Return true if object is inside zone */
    public final boolean checkIfInZone(int x, int y) { return (getArena(x, y) != null); }

    public void reload()
    {
    	this.getArenas().clear();
    	this.load();
    }

    // =========================================================
    // Method - Private
    private final void load()
    {
        // TEMP UNTIL ARENA'S TABLE IS ADDED
        for (Zone zone: ZoneManager.getInstance().getZones(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Arena)))
            getArenas().add(new Arena(zone.getId()));
    }

    // =========================================================
    // Property - Public
    public final Arena getArena(int arenaId)
    {
        int index = getArenaIndex(arenaId);
        if (index >= 0) return getArenas().get(index);
        return null;
    }

    public final Arena getArena(L2Object activeObject) { return getArena(activeObject.getPosition().getX(), activeObject.getPosition().getY()); }

    public final Arena getArena(int x, int y)
    {
        int index = getArenaIndex(x, y);
        if (index >= 0) return getArenas().get(index);
        return null;
    }

    public final Arena getArena(String name)
    {
        int index = getArenaIndex(name);
        if (index >= 0) return getArenas().get(index);
        return null;
    }

    public final int getArenaIndex(int arenaId)
    {
        Arena arena;
        for (int i = 0; i < getArenas().size(); i++)
        {
            arena = getArenas().get(i);
            if (arena != null && arena.getArenaId() == arenaId) return i;
        }
        return -1;
    }

    public final int getArenaIndex(L2Object activeObject) { return getArenaIndex(activeObject.getPosition().getX(), activeObject.getPosition().getY()); }

    public final int getArenaIndex(int x, int y)
    {
        Arena arena;
        for (int i = 0; i < getArenas().size(); i++)
        {
            arena = getArenas().get(i);
            if (arena != null && arena.checkIfInZone(x, y)) return i;
        }
        return -1;
    }

    public final int getArenaIndex(String name)
    {
        Arena arena;
        for (int i = 0; i < getArenas().size(); i++)
        {
            arena = getArenas().get(i);
            if (arena != null && arena.getName().equalsIgnoreCase(name.trim())) return i;
        }
        return -1;
    }

    public final List<Arena> getArenas()
    {
        if (_Arenas == null) _Arenas = new FastList<Arena>();
        return _Arenas;
    }
}
