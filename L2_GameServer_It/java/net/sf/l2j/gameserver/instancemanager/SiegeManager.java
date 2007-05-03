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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.StringTokenizer;

import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class SiegeManager
{
    protected static Logger _log = Logger.getLogger(SiegeManager.class.getName());

    // =========================================================
    private static SiegeManager _Instance;
    public static final SiegeManager getInstance()
    {
        if (_Instance == null)
        {
    		System.out.println("Initializing SiegeManager");
        	_Instance = new SiegeManager();
        	_Instance.load();
        }
        return _Instance;
    }

    // =========================================================
    // Data Field
    private int _Attacker_Max_Clans                              = 500; // Max number of clans
    private int _Attacker_RespawnDelay                           = 20000; // Time in ms. Changeable in siege.config
    private int _Defender_Max_Clans                              = 500; // Max number of clans
    private int _Defender_RespawnDelay                           = 10000; // Time in ms. Changeable in siege.config

    // Siege settings
    private FastMap<Integer,FastList<SiegeSpawn>>  _artefactSpawnList;
    private FastMap<Integer,FastList<SiegeSpawn>>  _controlTowerSpawnList;
    
    private int _ControlTowerLosePenalty                         = 20000; // Time in ms. Changeable in siege.config
    private int _Flag_MaxCount                                   = 1; // Changeable in siege.config
    private int _Siege_Clan_MinLevel                             = 4; // Changeable in siege.config
    private int _Siege_Length                                    = 120; // Time in minute. Changeable in siege.config
    private List<Siege> _Sieges;
    
    // =========================================================
    // Constructor
    public SiegeManager()
    {
    }

    // =========================================================
    // Method - Public
    public final void addSiegeSkills(L2PcInstance character)
    {
        character.addSkill(SkillTable.getInstance().getInfo(246, 1), false);
        character.addSkill(SkillTable.getInstance().getInfo(247, 1), false);
    }

    /** Return true if object is inside zone */
    public final boolean checkIfInZone(L2Object obj) { return (getSiege(obj) != null); }

    /** Return true if object is inside zone */
    public final boolean checkIfInZone(int x, int y) { return (getSiege(x, y) != null); }

    /**
     * Return true if character summon<BR><BR>
     * @param activeChar The L2Character of the character can summon
     */
    public final boolean checkIfOkToSummon(L2Character activeChar, boolean isCheckOnly)
    {
        if (activeChar == null || !(activeChar instanceof L2PcInstance)) return false;
        
        SystemMessage sm = new SystemMessage(614);
        L2PcInstance player = (L2PcInstance)activeChar;
        Castle castle = CastleManager.getInstance().getCastle(player);

        if (castle == null || castle.getCastleId() <= 0)
            sm.addString("You must be on castle ground to summon this");
        else if (!castle.getSiege().getIsInProgress())
            sm.addString("You can only summon this during a siege.");
        else if (player.getClanId() != 0 && castle.getSiege().getAttackerClan(player.getClanId()) == null)
            sm.addString("You can only summon this as a registered attacker.");
        else
            return true;
        
        if (!isCheckOnly) {player.sendPacket(sm);}
        return false;
    }
    
    /**
     * Return true if the clan is registered or owner of a castle<BR><BR>
     * @param clan The L2Clan of the player
     */
    public final boolean checkIsRegistered(L2Clan clan, int castleid)
    {
        if (clan == null) return false;

        if (clan.getHasCastle() > 0) return true;
        
        java.sql.Connection con = null;
        boolean register = false;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM siege_clans where clan_id=? and castle_id=?");
            statement.setInt(1, clan.getClanId());
            statement.setInt(2, castleid);
            ResultSet rs = statement.executeQuery();

            while (rs.next())
            {
                register = true;
                break;
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception: checkIsRegistered(): " + e.getMessage());
            e.printStackTrace();
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
        return register;
    }

    public final void removeSiegeSkills(L2PcInstance character)
    {
        character.removeSkill(SkillTable.getInstance().getInfo(246, 1));
        character.removeSkill(SkillTable.getInstance().getInfo(247, 1));
    }

    // =========================================================
    // Method - Private
    private final void load()
    {
        try {
            InputStream is              = new FileInputStream(new File(Config.SIEGE_CONFIGURATION_FILE));  
            Properties siegeSettings    = new Properties();
            siegeSettings.load(is);
            is.close();

            // Siege setting
            _Attacker_Max_Clans = Integer.decode(siegeSettings.getProperty("AttackerMaxClans", "500"));
            _Attacker_RespawnDelay = Integer.decode(siegeSettings.getProperty("AttackerRespawn", "30000"));
            _ControlTowerLosePenalty = Integer.decode(siegeSettings.getProperty("CTLossPenalty", "20000"));
            _Defender_Max_Clans = Integer.decode(siegeSettings.getProperty("DefenderMaxClans", "500"));
            _Defender_RespawnDelay = Integer.decode(siegeSettings.getProperty("DefenderRespawn", "20000"));
            _Flag_MaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1"));
            _Siege_Clan_MinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "4"));
            _Siege_Length = Integer.decode(siegeSettings.getProperty("SiegeLength", "120"));
            
            // Siege spawns settings
            _controlTowerSpawnList = new FastMap<Integer,FastList<SiegeSpawn>>();
            _artefactSpawnList = new FastMap<Integer,FastList<SiegeSpawn>>();

            for (Castle castle: CastleManager.getInstance().getCastles())
            {
            	FastList<SiegeSpawn> _controlTowersSpawns = new FastList<SiegeSpawn>();
            	
            	for (int i=1; i<0xFF; i++)
            	{
            		String _spawnParams = siegeSettings.getProperty(castle.getName() + "ControlTower" + Integer.toString(i), "");
            		
            		if (_spawnParams.length() == 0) break;
            		
            		StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
            		
            		try
            		{
            			int x = Integer.parseInt(st.nextToken());
            			int y = Integer.parseInt(st.nextToken());
            			int z = Integer.parseInt(st.nextToken());
            			int npc_id = Integer.parseInt(st.nextToken());
            			int hp = Integer.parseInt(st.nextToken());
            			
            			_controlTowersSpawns.add(new SiegeSpawn(castle.getCastleId(),x,y,z,0,npc_id,hp));
            		}
            		catch (Exception e)
            		{
            			_log.warning("Error while loading control tower(s) for "+castle.getName()+" castle.");
            		}
            	}
            	
            	FastList<SiegeSpawn> _artefactSpawns = new FastList<SiegeSpawn>();
            	
            	for (int i=1; i<0xFF; i++)
            	{
            		String _spawnParams = siegeSettings.getProperty(castle.getName() + "Artefact" + Integer.toString(i), "");
            		
            		if (_spawnParams.length() == 0) break;
            		
            		StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");
            		
            		try
            		{
            			int x = Integer.parseInt(st.nextToken());
            			int y = Integer.parseInt(st.nextToken());
            			int z = Integer.parseInt(st.nextToken());
            			int heading = Integer.parseInt(st.nextToken()); 
            			int npc_id = Integer.parseInt(st.nextToken());
            			
            			_artefactSpawns.add(new SiegeSpawn(castle.getCastleId(),x,y,z,heading,npc_id));
            		}
            		catch (Exception e)
            		{
            			_log.warning("Error while loading artefact(s) for "+castle.getName()+" castle.");
            		}
            	}
            	
            	_controlTowerSpawnList.put(castle.getCastleId(), _controlTowersSpawns);
            	_artefactSpawnList.put(castle.getCastleId(), _artefactSpawns);
            }
            	
        } catch (Exception e) {
            //_initialized = false;
            System.err.println("Error while loading siege data.");
            e.printStackTrace();
        }
    }

    // =========================================================
    // Property - Public
    public final FastList<SiegeSpawn> getArtefactSpawnList(int _castleId) 
    { 
    	if (_artefactSpawnList.containsKey(_castleId))
    		return _artefactSpawnList.get(_castleId);
    	else 
    		return null;
    }
        
    public final FastList<SiegeSpawn> getControlTowerSpawnList(int _castleId) 
    { 
    	if (_controlTowerSpawnList.containsKey(_castleId))
    		return _controlTowerSpawnList.get(_castleId);
    	else 
    		return null;
    }
    
    public final int getAttackerMaxClans() { return _Attacker_Max_Clans; }

    public final int getAttackerRespawnDelay() { return _Attacker_RespawnDelay; }

    public final int getControlTowerLosePenalty() { return _ControlTowerLosePenalty; }

    public final int getDefenderMaxClans() { return _Defender_Max_Clans; }

    public final int getDefenderRespawnDelay() { return (_Defender_RespawnDelay); }
    
    public final int getFlagMaxCount() { return _Flag_MaxCount; }

    public final Siege getSiege(L2Object activeObject) { return getSiege(activeObject.getPosition().getX(), activeObject.getPosition().getY()); }

    public final Siege getSiege(int x, int y)
    {
        for (Castle castle: CastleManager.getInstance().getCastles())
            if (castle.getSiege().checkIfInZone(x, y)) return castle.getSiege();
        return null;
    }
    
    public final int getSiegeClanMinLevel() { return _Siege_Clan_MinLevel; }
    
    public final int getSiegeLength() { return _Siege_Length; }

    public final List<Siege> getSieges()
    {
        if (_Sieges == null) _Sieges = new FastList<Siege>();
        return _Sieges;
    }
    
    public class  SiegeSpawn
    {
    	Location _location; 
    	private int _npc_id;
    	private int _heading;
    	private int _castle_id;
    	private int _hp;
    	
    	public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id)
    	{
    		_castle_id = castle_id;
    		_location = new Location(x,y,z,heading);
    		_heading = heading;
    		_npc_id = npc_id;
    	}
    	
    	public SiegeSpawn(int castle_id, int x, int y, int z, int heading, int npc_id, int hp)
    	{
    		_castle_id = castle_id;
    		_location = new Location(x,y,z,heading);
    		_heading = heading;
    		_npc_id = npc_id;
    		_hp = hp;
    	}
    	
    	public int getCastleId()
    	{
    		return _castle_id;
    	}

    	public int getNpcId()
    	{
    		return _npc_id;
    	}

    	public int getHeading()
    	{
    		return _heading;
    	}
    	
    	public int getHp()
    	{
    		return _hp;
    	}
    	
    	public Location getLocation()
    	{
    		return _location;
    	}
    }
}
