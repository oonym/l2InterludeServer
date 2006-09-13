package net.sf.l2j.gameserver.instancemanager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
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

    
    // =========================================================
    // Data Field
    private int _Attacker_Max_Clans                              = 500; // Max number of clans
    private int _Attacker_RespawnDelay                           = 20000; // Time in ms. Changeable in siege.config
    private int _Defender_Max_Clans                              = 500; // Max number of clans
    private int _Defender_RespawnDelay                           = 10000; // Time in ms. Changeable in siege.config

    // Siege settings
    private int[][] artifactSpawnList                            = {{}, // nowhere
                                                                        {-18120,107984,-2483,16384,7250}, //gludio
                                                                        {22081,161771,-2677,49017,12123}, //dion
                                                                        {117939,145090,-2550,32768,12154}, //giran
                                                                        {84014,37184,-2277,16384,12242}, //oren
                                                                        {147465,1537,-373,16384,12291}, //aden
                                                                        {116031,250555,-798,49200,12602}}; //innadril
    private int _ControlTowerLosePenalty                         = 20000; // Time in ms. Changeable in siege.config
    private int _Flag_BuyCost                                    = 50000; // Changeable in siege.config
    private int _Flag_BuyItemId                                  = 57;  // Changeable in siege.config
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
        character.addSkill(SkillTable.getInstance().getInfo(246, 1));
        character.addSkill(SkillTable.getInstance().getInfo(247, 1));
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
        else
            return true;
        
        if (!isCheckOnly) {player.sendPacket(sm);}
        return false;
    }
    
    /**
     * Return true if the clan is registered or owner of a castle<BR><BR>
     * @param clan The L2Clan of the player
     */
    public final boolean checkIsRegistered(L2Clan clan)
    {
        if (clan == null) return false;

        if (clan.getHasCastle() > 0) return true;
        
        java.sql.Connection con = null;
        boolean register = false;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT clan_id FROM siege_clans where clan_id=?");
            statement.setInt(1, clan.getClanId());
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
            _Flag_BuyItemId = Integer.decode(siegeSettings.getProperty("FlagItem", "57"));
            _Flag_BuyCost = Integer.decode(siegeSettings.getProperty("FlagCost", "200000"));
            _Flag_MaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1"));
            _Siege_Clan_MinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "4"));
            _Siege_Length = Integer.decode(siegeSettings.getProperty("SiegeLength", "120"));
        } catch (Exception e) {
            //_initialized = false;
            System.err.println("Error while loading siege data.");
            e.printStackTrace();
        }
    }

    // =========================================================
    // Property - Public
    public final int[][] getArtifactSpawnList() { return artifactSpawnList; }

    public final int getAttackerMaxClans() { return _Attacker_Max_Clans; }

    public final int getAttackerRespawnDelay() { return _Attacker_RespawnDelay; }

    public final int getControlTowerLosePenalty() { return _ControlTowerLosePenalty; }

    public final int getDefenderMaxClans() { return _Defender_Max_Clans; }

    public final int getDefenderRespawnDelay() { return (_Defender_RespawnDelay); }
    
    public final int getFlagBuyCost() { return _Flag_BuyCost; }

    public final int getFlagBuyItemId() { return _Flag_BuyItemId; }

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
}
