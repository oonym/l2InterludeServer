package net.sf.l2j.gameserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.templates.L2CharTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

public class DoorTable 
{
	private static Logger _log = Logger.getLogger(DoorTable.class.getName());

	private Map<Integer,L2DoorInstance> _staticItems;

	private static DoorTable _instance;

	public static DoorTable getInstance() 
	{
		if (_instance == null) 
			_instance = new DoorTable();

		return _instance;
	}

	public DoorTable() 
	{
		_staticItems = new FastMap<Integer,L2DoorInstance>();
		parseData();
	}
        
	public void reloadAll() 
	{
	    respawn();
	}
	public void respawn() 
	{
//	    L2DoorInstance[] currentDoors = getDoors();
	    _staticItems = null;
	    _instance = null;
	    _instance = new DoorTable();
	}

	private void parseData() 
	{
		LineNumberReader lnr = null;
		try 
		{
			File doorData = new File(Config.DATAPACK_ROOT, "data/door.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(doorData)));

			String line = null;
			_log.warning("Searching clan halls doors:");

			while ((line = lnr.readLine()) != null) 
			{
				if (line.trim().length() == 0 || line.startsWith("#")) 
					continue;

				L2DoorInstance door = parseList(line);
				_staticItems.put(door.getDoorId(), door);
				door.spawnMe(door.getX(), door.getY(),door.getZ());
				ClanHall clanhall = ClanHallManager.getInstance().getClanHall(door.getX(),door.getY(),1500);
				if (clanhall != null)
				{
				    clanhall.getDoors().add(door);
				    door.setClanHall(clanhall);
                    if (Config.DEBUG)
                        _log.warning("door "+door.getDoorName()+" attached to ch "+clanhall.getName());
				}
			}

			_log.config("DoorTable: Loaded " + _staticItems.size() + " Door Templates.");
		} 
		catch (FileNotFoundException e) 
		{
			_initialized = false;
			_log.warning("door.csv is missing in data folder");
		} 
		catch (Exception e) 
		{
			_initialized = false;
			_log.warning("error while creating door table " + e);
		} 
		finally 
		{
			try { lnr.close(); } catch (Exception e1) { /* ignore problems */ }
		}
	}

	public static L2DoorInstance parseList(String line) 
	{
		StringTokenizer st = new StringTokenizer(line, ";");

		String name = st.nextToken();
		int id = Integer.parseInt(st.nextToken());
		int x = Integer.parseInt(st.nextToken());
		int y = Integer.parseInt(st.nextToken());
		int z = Integer.parseInt(st.nextToken());
		int hp = Integer.parseInt(st.nextToken());
		int pdef = Integer.parseInt(st.nextToken());
		int mdef = Integer.parseInt(st.nextToken());
		boolean unlockable = false;
		
		if (st.hasMoreTokens())
			unlockable = Boolean.parseBoolean(st.nextToken());

		StatsSet npcDat = new StatsSet(); 
		npcDat.set("npcId", id);
		npcDat.set("level", 0);
		npcDat.set("jClass", "door");

		npcDat.set("baseSTR", 0);
		npcDat.set("baseCON", 0);
		npcDat.set("baseDEX", 0);
		npcDat.set("baseINT", 0);
		npcDat.set("baseWIT", 0);
		npcDat.set("baseMEN", 0);

		npcDat.set("baseShldDef", 0);
		npcDat.set("baseShldRate", 0);
		npcDat.set("baseAccCombat", 38);
		npcDat.set("baseEvasRate",  38);
		npcDat.set("baseCritRate",  38);

		//npcDat.set("name", "");
		npcDat.set("collision_radius", 0);
		npcDat.set("collision_height", 0);
		npcDat.set("sex", "male");
		npcDat.set("type", "");
		npcDat.set("baseAtkRange", 0);
        npcDat.set("baseMpMax", 0);
        npcDat.set("baseCpMax", 0);
		npcDat.set("revardExp", 0);
		npcDat.set("revardSp", 0);
		npcDat.set("basePAtk", 0);
		npcDat.set("baseMAtk", 0);
		npcDat.set("basePAtkSpd", 0);
		npcDat.set("aggroRange", 0);
		npcDat.set("baseMAtkSpd", 0);
		npcDat.set("rhand", 0);
		npcDat.set("lhand", 0);
		npcDat.set("armor", 0);
		npcDat.set("baseWalkSpd", 0);
		npcDat.set("baseRunSpd", 0);
		npcDat.set("name", name);
		npcDat.set("baseHpMax", hp);
		npcDat.set("baseHpReg", 3.e-3f);
		npcDat.set("baseMpReg", 3.e-3f);
		npcDat.set("basePDef", pdef);
		npcDat.set("baseMDef", mdef);
		
		L2CharTemplate template = new L2CharTemplate(npcDat);
		L2DoorInstance door = new L2DoorInstance(IdFactory.getInstance().getNextId(),template, id, name, unlockable);
		
		door.setCurrentHpMp(door.getMaxHp(), door.getMaxMp());
		door.setOpen(1);
		door.setXYZInvisible(x,y,z);

		return door;
	}
        
	public boolean isInitialized() 
	{
		return _initialized;
	}

	private boolean _initialized = true;

	public L2DoorInstance getDoor(Integer id) 
	{
		return _staticItems.get(id);
	}

	public L2DoorInstance[] getDoors() 
	{
		L2DoorInstance[] _allTemplates = _staticItems.values().toArray(new L2DoorInstance[_staticItems.size()]);
		return _allTemplates;
	}
    
    /**
     * Performs a check and sets up a scheduled task for 
     * those doors that require auto opening/closing.
     */
    public void checkAutoOpen()
    {
        for (L2DoorInstance doorInst : getDoors())
            // Garden of Eva (every 7 minutes)
            if (doorInst.getDoorName().startsWith("goe"))
                doorInst.setAutoActionDelay(420000);
        
            // Tower of Insolence (every 5 minutes)
            else if (doorInst.getDoorName().startsWith("aden_tower"))
                doorInst.setAutoActionDelay(300000);
    }
}
