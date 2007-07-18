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
package net.sf.l2j.gameserver.model.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.CastleUpdater;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.CropProcure;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.SeedProduction;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.serverpackets.PledgeShowInfoUpdate;

public class Castle
{
    protected static Logger _log = Logger.getLogger(Castle.class.getName());
    
	// =========================================================
    // Data Field
    private List<CropProcure> _procure = new FastList<CropProcure>();
    private List<SeedProduction> _production = new FastList<SeedProduction>();

	// =========================================================
    // Data Field
	private int _castleId                      = 0;
	private List<L2DoorInstance> _doors        = new FastList<L2DoorInstance>();
	private List<String> _doorDefault          = new FastList<String>();
	private String _name                       = "";
	private int _ownerId                       = 0;
	private Siege _siege                       = null;
	private Calendar _siegeDate;
	private int _siegeDayOfWeek                = 7; // Default to saturday
	private int _siegeHourOfDay                = 20; // Default to 8 pm server time
	private int _taxPercent                    = 0;
	private double _taxRate                    = 0;
	private int _treasury                      = 0;
    private Zone _zone;
    private List<Zone> _zoneTown;
    private L2Clan _formerOwner				   = null;

	// =========================================================
	// Constructor
	public Castle(int castleId)
	{
		_castleId = castleId;
        this.load();
		this.loadDoor();
	}

	// =========================================================
	// Method - Public
	// This method add to the treasury
    /** Add amount to castle instance's treasury (warehouse). */
	public void addToTreasury(int amount)
	{
        if (getOwnerId() <= 0) return;

        if (_name.equalsIgnoreCase("Schuttgart") || _name.equalsIgnoreCase("Goddard"))
        {
        	Castle rune = CastleManager.getInstance().getCastle("rune");
        	if (rune != null )
        	{
        		int runeTax = (int)(amount * rune.getTaxRate());
        		if (rune.getOwnerId() > 0 ) rune.addToTreasury(runeTax);
        		amount -= runeTax;
        	}
        }
        if (!_name.equalsIgnoreCase("aden") && !_name.equalsIgnoreCase("Rune") && !_name.equalsIgnoreCase("Schuttgart") && !_name.equalsIgnoreCase("Goddard"))    // If current castle instance is not Aden, Rune, Goddard or Schuttgart.
        {
            Castle aden = CastleManager.getInstance().getCastle("aden");
            if (aden != null)
            {
                int adenTax = (int)(amount * aden.getTaxRate());        // Find out what Aden gets from the current castle instance's income
                if (aden.getOwnerId() > 0) aden.addToTreasury(adenTax); // Only bother to really add the tax to the treasury if not npc owned
                    
                amount -= adenTax; // Subtract Aden's income from current castle instance's income
            }
        }
	    
	    _treasury += amount; // Add to the current treasury total.  Use "-" to substract from treasury
	    
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("Update castle set treasury = ? where id = ?");
            statement.setInt(1, getTreasury());
            statement.setInt(2, getCastleId());
            statement.execute();
            statement.close();
        }
        catch (Exception e) {} 
        finally {try { con.close(); } catch (Exception e) {}}
	}
	
	/**
	 * Move non clan members off castle area and to nearest town.<BR><BR>
	 */
	public void banishForeigner(L2PcInstance activeChar)
    {
		// Get players from this and nearest world regions
        for (L2PlayableInstance player : L2World.getInstance().getVisiblePlayable(activeChar))
        {
            if(!(player instanceof L2PcInstance)) continue;
            
        	// Skip if player is in clan
            if (((L2PcInstance)player).getClanId() == getOwnerId())
                continue;
            
            if (checkIfInZone(player)) player.teleToLocation(MapRegionTable.TeleportWhereType.Town); 
        }
    }

    /**
     * Return true if object is inside the zone
     */
    public boolean checkIfInZone(L2Object obj)
    {
        return checkIfInZone(obj.getX(), obj.getY());
    }

    /**
     * Return true if object is inside the zone
     */
    public boolean checkIfInZone(int x, int y)
    {
        return getZone().checkIfInZone(x, y);
    }

    /**
     * Return true if object is inside the zone
     */
    public boolean checkIfInZoneTowns(L2Object obj)
    {
        return checkIfInZoneTowns(obj.getX(), obj.getY());
    }

    /**
     * Return true if object is inside the zone
     */
    public boolean checkIfInZoneTowns(int x, int y)
    {
        for (Zone zone: getZoneTowns())
            if (zone.checkIfInZone(x, y)) return true;
        return false;
    }
	
	public void closeDoor(L2PcInstance activeChar, int doorId)
	{
	    openCloseDoor(activeChar, doorId, false);
	}

	public void openDoor(L2PcInstance activeChar, int doorId)
	{
	    openCloseDoor(activeChar, doorId, true);
	}

	public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
	{
	    if (activeChar.getClanId() != getOwnerId())
	        return;

	    L2DoorInstance door = getDoor(doorId);
        if (door != null)
        {
            if (open)
                door.openMe();
            else
                door.closeMe();
        }
	}
	
	// This method is used to begin removing all castle upgrades
	public void removeUpgrade()
	{
	    removeDoorUpgrade();
	}
	
	// This method updates the castle tax rate
	public void setOwner(L2Clan clan)
	{
		// Remove old owner
	    if (getOwnerId() > 0 && (clan == null || clan.getClanId() != getOwnerId()))
	    {
	        L2Clan oldOwner = ClanTable.getInstance().getClan(getOwnerId());			// Try to find clan instance 
			if (oldOwner != null)
			{
				if (_formerOwner == null)
                    _formerOwner = oldOwner;
				oldOwner.setHasCastle(0);												// Unset has castle flag for old owner
        		new Announcements().announceToAll(oldOwner.getName() + " has lost " + getName() + " castle!");
			}							
	    }

	    updateOwnerInDB(clan);															// Update in database

	    if (getSiege().getIsInProgress())												// If siege in progress
        	getSiege().midVictory();													// Mid victory phase of siege
	    
	    updateClansReputation();
	}

	// This method updates the castle tax rate
	public void setTaxPercent(L2PcInstance activeChar, int taxPercent)
	{
	    if (taxPercent < 0 || taxPercent > 15)
	    {
	        activeChar.sendMessage("Tax value must be between 1 and 15.");
	        return;
	    }
	    
        _taxPercent = taxPercent;
        _taxRate = _taxPercent / 100.0;

        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("Update castle set taxPercent = ? where id = ?");
            statement.setInt(1, taxPercent);
            statement.setInt(2, getCastleId());
            statement.execute();
            statement.close();
        }
        catch (Exception e) {} 
        finally {try { con.close(); } catch (Exception e) {}}

        activeChar.sendMessage(getName() + " castle tax changed to " + taxPercent + "%.");
	}
    
	/**
	 * Respawn all doors on castle grounds<BR><BR>
	 */
	public void spawnDoor()
    {
	    spawnDoor(false);
    }
    
	/**
	 * Respawn all doors on castle grounds<BR><BR>
	 */
	public void spawnDoor(boolean isDoorWeak)
    {
	    for (int i = 0; i < getDoors().size(); i++)
        {
            L2DoorInstance door = getDoors().get(i);
            if (door.getCurrentHp() <= 0)
            {
                door.decayMe();	// Kill current if not killed already
                door = DoorTable.parseList(_doorDefault.get(i));
                if (isDoorWeak)
                    door.setCurrentHp(door.getMaxHp() / 2);
    			door.spawnMe(door.getX(), door.getY(),door.getZ());
    			getDoors().set(i, door);
            }
            else if (door.getOpen() == 0)
                door.closeMe();
        }
		loadDoorUpgrade(); // Check for any upgrade the doors may have
    }

	// This method upgrade door
	public void upgradeDoor(int doorId, int hp, int pDef, int mDef)
	{
        L2DoorInstance door = getDoor(doorId);
	    if (door == null)
	        return;
	    
        if (door != null && door.getDoorId() == doorId)
        {
        	door.setCurrentHp(door.getMaxHp() + hp);

        	saveDoorUpgrade(doorId, hp, pDef, mDef);
            return;
        }
	}
	
	// =========================================================
	// Method - Private
	// This method loads castle
	private void load()
	{
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("Select * from castle where id = ?");
            statement.setInt(1, getCastleId());
            rs = statement.executeQuery();

            while (rs.next())
            {
        	    _name = rs.getString("name");
        	    //_OwnerId = rs.getInt("ownerId");

        	    _siegeDate = Calendar.getInstance();
        	    _siegeDate.setTimeInMillis(rs.getLong("siegeDate"));
        	    
        	    _siegeDayOfWeek = rs.getInt("siegeDayOfWeek");
        	    if (_siegeDayOfWeek < 1 || _siegeDayOfWeek > 7)
        	        _siegeDayOfWeek = 7;

        	    _siegeHourOfDay = rs.getInt("siegeHourOfDay");
        	    if (_siegeHourOfDay < 0 || _siegeHourOfDay > 23)
        	        _siegeHourOfDay = 20;

        	    _taxPercent = rs.getInt("taxPercent");
        	    _treasury = rs.getInt("treasury");
            }

            statement.close();

            _taxRate = _taxPercent / 100.0;

            statement = con.prepareStatement("Select clan_id from clan_data where hasCastle = ?");
            statement.setInt(1, getCastleId());
            rs = statement.executeQuery();

            while (rs.next())
            {
        	    _ownerId = rs.getInt("clan_id");
            }

            if (getOwnerId() > 0)
            {
                L2Clan clan = ClanTable.getInstance().getClan(getOwnerId());                        // Try to find clan instance 
                ThreadPoolManager.getInstance().scheduleGeneral(new CastleUpdater(clan, 1), 3600000);     // Schedule owner tasks to start running 
            }

            statement.close();
        }
        catch (Exception e)
        {
            System.out.println("Exception: loadCastleData(): " + e.getMessage());
            e.printStackTrace();
        }
        finally {try { con.close(); } catch (Exception e) {}}
	}

	// This method loads castle door data from database
	private void loadDoor()
	{
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("Select * from castle_door where castleId = ?");
            statement.setInt(1, getCastleId());
            ResultSet rs = statement.executeQuery();

            while (rs.next())
            {
                // Create list of the door default for use when respawning dead doors
                _doorDefault.add(rs.getString("name") 
                        + ";" + rs.getInt("id") 
                        + ";" + rs.getInt("x") 
                        + ";" + rs.getInt("y") 
                        + ";" + rs.getInt("z") 
                        + ";" + rs.getInt("range_xmin") 
                        + ";" + rs.getInt("range_ymin") 
                        + ";" + rs.getInt("range_zmin")
                        + ";" + rs.getInt("range_xmax") 
                        + ";" + rs.getInt("range_ymax") 
                        + ";" + rs.getInt("range_zmax") 
                        + ";" + rs.getInt("hp") 
                        + ";" + rs.getInt("pDef") 
                        + ";" + rs.getInt("mDef"));

                L2DoorInstance door = DoorTable.parseList(_doorDefault.get(_doorDefault.size() - 1));
				door.spawnMe(door.getX(), door.getY(),door.getZ());				
                _doors.add(door);
                DoorTable.getInstance().putDoor(door);
            }

            statement.close();
        }
        catch (Exception e)
        {
            System.out.println("Exception: loadCastleDoor(): " + e.getMessage());
            e.printStackTrace();
        }
        finally {try { con.close(); } catch (Exception e) {}}
	}

	// This method loads castle door upgrade data from database
	private void loadDoorUpgrade()
	{
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("Select * from castle_doorupgrade where doorId in (Select Id from castle_door where castleId = ?)");
            statement.setInt(1, getCastleId());
            ResultSet rs = statement.executeQuery();

            while (rs.next())
            {
                upgradeDoor(rs.getInt("id"), rs.getInt("hp"), rs.getInt("pDef"), rs.getInt("mDef"));
            }

            statement.close();
        }
        catch (Exception e)
        {
            System.out.println("Exception: loadCastleDoorUpgrade(): " + e.getMessage());
            e.printStackTrace();
        }
        finally {try { con.close(); } catch (Exception e) {}}
	}

	private void removeDoorUpgrade()
	{
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("delete from castle_doorupgrade where doorId in (select id from castle_door where castleId=?)");
            statement.setInt(1, getCastleId());
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            System.out.println("Exception: removeDoorUpgrade(): " + e.getMessage());
            e.printStackTrace();
        }
        finally {try { con.close(); } catch (Exception e) {}}
	}

	private void saveDoorUpgrade(int doorId, int hp, int pDef, int mDef)
	{
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("INSERT INTO castle_doorupgrade (doorId, hp, pDef, mDef) values (?,?,?,?)");
            statement.setInt(1, doorId);
            statement.setInt(2, hp);
            statement.setInt(3, pDef);
            statement.setInt(4, mDef);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
            System.out.println("Exception: saveDoorUpgrade(int doorId, int hp, int pDef, int mDef): " + e.getMessage());
            e.printStackTrace();
        } 
        finally 
        {
            try { con.close(); } catch (Exception e) {}
        }
	}
	
	private void updateOwnerInDB(L2Clan clan)
	{
		if (clan != null)
		    _ownerId = clan.getClanId();	// Update owner id property
		else
			_ownerId = 0;					// Remove owner

	    java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;

            // ============================================================================
            // NEED TO REMOVE HAS CASTLE FLAG FROM CLAN_DATA
            // SHOULD BE CHECKED FROM CASTLE TABLE
            statement = con.prepareStatement("UPDATE clan_data SET hasCastle=0 WHERE hasCastle=?");
            statement.setInt(1, getCastleId());
            statement.execute();
            statement.close();   

            statement = con.prepareStatement("UPDATE clan_data SET hasCastle=? WHERE clan_id=?");
            statement.setInt(1, getCastleId());
            statement.setInt(2, getOwnerId());
            statement.execute();
            statement.close();   
            // ============================================================================
            
            // Announce to clan memebers
            if (clan != null)
            {
    		    clan.setHasCastle(getCastleId()); // Set has castle flag for new owner
    		    new Announcements().announceToAll(clan.getName() + " has taken " + getName() + " castle!");
    		    clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));

    		    ThreadPoolManager.getInstance().scheduleGeneral(new CastleUpdater(clan, 1), 3600000);	// Schedule owner tasks to start running 
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
	}
	
	// =========================================================
	// Proeprty
	public final int getCastleId()
	{
		return _castleId;
	}

	public final L2DoorInstance getDoor(int doorId)
	{
	    if (doorId <= 0)
	        return null;
	    
        for (int i = 0; i < getDoors().size(); i++)
        {
            L2DoorInstance door = getDoors().get(i);
            if (door.getDoorId() == doorId)
                return door;
        }
		return null;
	}

	public final List<L2DoorInstance> getDoors()
	{
		return _doors;
	}

	public final String getName()
	{
	    return _name;
	}

	public final int getOwnerId()
	{
		return _ownerId;
	}

	public final Siege getSiege()
	{
        if (_siege == null) _siege = new Siege(new Castle[] {this});
		return _siege;
	}

	public final Calendar getSiegeDate() { return _siegeDate; }

	public final int getSiegeDayOfWeek() { return _siegeDayOfWeek; }

	public final int getSiegeHourOfDay() { return _siegeHourOfDay; }

	public final int getTaxPercent()
	{
		return _taxPercent;
	}

	public final double getTaxRate()
	{
		return _taxRate;
	}

	public final int getTreasury()
	{
		return _treasury;
	}

    public final Zone getZone()
    {
        if (_zone == null) _zone = ZoneManager.getInstance().getZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.CastleArea), getName());
        return _zone;
    }

    public final Zone getZoneTown(int id)
    {
        for (Zone zone: getZoneTowns())
            if (zone.getId() == id) return zone;
        return null;
    }

    public final Zone getZoneTown(String name)
    {
        for (Zone zone: getZoneTowns())
            if (zone.getName() == name) return zone;
        return null;
    }

    public final List<Zone> getZoneTowns()
    {
        if (_zoneTown == null)
        {
            _zoneTown = new FastList<Zone>();
            // Add towns that belong to castle
            for (Zone zone: ZoneManager.getInstance().getZones(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.Town)))
                if (zone != null && zone.getTaxById() == getCastleId()) _zoneTown.add(zone);
        }
        return _zoneTown;
    }


    /**
     * Manor specific code
     */
        
    public void restoreManorData()
    {

        java.sql.Connection con = null;
        ResultSet rs;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;

            // restore procure info
            statement = con.prepareStatement("SELECT * FROM castle_manor_procure WHERE castleId=?");
            statement.setInt(1, getCastleId());
            rs = statement.executeQuery();
            
            while(rs.next())
            {
                _procure.add(new CropProcure(rs.getInt("cropId"),rs.getInt("canBuy"),rs.getInt("rewardType")));
            }
            
            statement.close();
            
            // restore seed production info
            statement = con.prepareStatement("SELECT * FROM castle_manor_production WHERE castleId=?");
            statement.setInt(1, getCastleId());
            rs = statement.executeQuery();
            
            while(rs.next())
            {
                _production.add(new SeedProduction(rs.getInt("seedId"),rs.getInt("canProduce")));
            }
            
            statement.close();

        }
        catch (Exception e)
        {
            System.out.println("Error restoring manor procure data: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }

    }
    
    public void addCropProcure(CropProcure crop)
    {
    	_procure.add(crop);
    }
    
    public void addSeedProduction(SeedProduction seed)
    {
    	_production.add(seed);
    }
    
    public List<CropProcure> getManorRewards()
    {
        return _procure;
    }
    
    public int getSeedProduction(int seedId)
    {
        for(SeedProduction s : _production)
        {
            if(s.getSeedId() == seedId)
                return s.getCanProduce();
        }
        
        return 0;
    }
    
    public List<CropProcure> getCropProcure()
    {
        return _procure;
    }
    
    public void updateClansReputation()
    {
        if (_formerOwner != null )
        {
            if (_formerOwner != ClanTable.getInstance().getClan(getOwnerId()))
            {
                int maxreward = Math.max(0,_formerOwner.getReputationScore());
            	_formerOwner.setReputationScore(_formerOwner.getReputationScore()-1000, true);
                L2Clan owner = ClanTable.getInstance().getClan(getOwnerId());
                owner.setReputationScore(owner.getReputationScore()+Math.min(1000,maxreward), true);
                owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
            }
            else
            	_formerOwner.setReputationScore(_formerOwner.getReputationScore()+500, true);

            _formerOwner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(_formerOwner));
        }
        else 
        {
            L2Clan owner = ClanTable.getInstance().getClan(getOwnerId());
            owner.setReputationScore(owner.getReputationScore()+1000, true);
            owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
        }
    }
}