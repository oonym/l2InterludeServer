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
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.DoorTable;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.PledgeShowInfoUpdate;

public class ClanHall
{
    protected static Logger _log = Logger.getLogger(ClanHall.class.getName());
    
	// =========================================================
    // Data Field
	private int _ClanHallId                    = 0;
	private List<L2DoorInstance> _Doors;
	private List<String> _DoorDefault          = new FastList<String>();
    private String _Name                       = "";
	private int _OwnerId                       = 0;
    private Zone _Zone;

	// =========================================================
	// Constructor
	public ClanHall(int clanHallId)
	{
		_ClanHallId = clanHallId;
		this.load();
	}

	// =========================================================
	// Method - Public
    /** Return true if object is inside the zone */
    public boolean checkIfInZone(L2Object obj) { return checkIfInZone(obj.getX(), obj.getY()); }

    /** Return true if object is inside the zone */
    public boolean checkIfInZone(int x, int y) { return getZone().checkIfInZone(x, y); }

    public double findDistanceToZone(int x, int y, int z, boolean checkZ) { return getZone().findDistanceToZone(x, y, z, checkZ); }
	
	public void openCloseDoor(int doorId, boolean open) { this.openCloseDoor(getDoor(doorId), open); }

	public void openCloseDoor(L2DoorInstance door, boolean open)
	{
        if (door != null)
        {
            if (open)
                door.openMe();
            else
                door.closeMe();
        }
	}

	public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
	{
	    if (activeChar != null && activeChar.getClanId() == getOwnerId()) this.openCloseDoor(doorId, open);
	}

	public void openCloseDoors(boolean open)
	{
	    for (L2DoorInstance door : getDoors())
	    {
	        if (door != null)
	        {
	            if (open)
	                door.openMe();
	            else
	                door.closeMe();
	        }
	    }
	}

	public void openCloseDoors(L2PcInstance activeChar, boolean open)
	{
	    if (activeChar != null && activeChar.getClanId() == getOwnerId()) this.openCloseDoors(open);
	}

	public void setOwner(L2Clan clan)
	{
		// Remove old owner
	    if (getOwnerId() > 0 && (clan == null || clan.getClanId() != getOwnerId()))
	    {
	        L2Clan oldOwner = ClanTable.getInstance().getClan(getOwnerId());			// Try to find clan instance 
			if (oldOwner != null)
			{
				oldOwner.setHasHideout(0);												// Unset has hideout flag for old owner
			}							
	    }
	    
	    updateOwnerInDB(clan);															// Update in database
	}

    /** Respawn all doors */
	public void spawnDoor() { spawnDoor(false); }
    
	/** Respawn all doors */
	public void spawnDoor(boolean isDoorWeak)
    {
	    for (int i = 0; i < getDoors().size(); i++)
        {
            L2DoorInstance door = getDoors().get(i);
            if (door.getCurrentHp() <= 0)
            {
                door.decayMe();	// Kill current if not killed already
                door = DoorTable.parseList(_DoorDefault.get(i));
                if (isDoorWeak) door.setCurrentHp(door.getMaxHp() / 2);
    			door.spawnMe(door.getX(), door.getY(),door.getZ());
    			getDoors().set(i, door);
            }
            else if (door.getOpen() == 0)
                door.closeMe();
        }
    }
	
	// =========================================================
	// Method - Private
	private void load()
	{
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("Select * from clanhall where id = ?");
            statement.setInt(1, getId());
            rs = statement.executeQuery();

            while (rs.next())
            {
        	    _Name = rs.getString("name");
        	    _OwnerId = rs.getInt("ownerId");
            }

            statement.close();

            // ============================================================================
            // NEED TO REMOVE HAS HIDEOUT FLAG FROM CLAN_DATA
            // SHOULD BE CHECKED FROM CLANHALL TABLE
            statement = con.prepareStatement("Select clan_id from clan_data where hasHideout = ?");
            statement.setInt(1, getId());
            rs = statement.executeQuery();

            while (rs.next())
            {
        	    _OwnerId = rs.getInt("clan_id");
            }

            statement.close();
            // ============================================================================

        }
        catch (Exception e)
        {
            System.out.println("Exception: ClanHall.load(): " + e.getMessage());
            e.printStackTrace();
        }
        finally {try { con.close(); } catch (Exception e) {}}
	}

	private void updateOwnerInDB(L2Clan clan)
	{
		if (clan != null)
		    _OwnerId = clan.getClanId();	// Update owner id property
		else
			_OwnerId = 0;					// Remove owner

	    java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;

            statement = con.prepareStatement("UPDATE clanhall SET ownerId=? WHERE id=?");
            statement.setInt(1, getOwnerId());
            statement.setInt(2, getId());
            statement.execute();
            statement.close();   

            // ============================================================================
            // NEED TO REMOVE HAS HIDEOUT FLAG FROM CLAN_DATA
            // SHOULD BE CHECKED FROM CLANHALL TABLE
            statement = con.prepareStatement("UPDATE clan_data SET hasHideout=0 WHERE hasHideout=?");
            statement.setInt(1, getId());
            statement.execute();
            statement.close();   

            statement = con.prepareStatement("UPDATE clan_data SET hasHideout=? WHERE clan_id=?");
            statement.setInt(1, getId());
            statement.setInt(2, getOwnerId());
            statement.execute();
            statement.close();   
            // ============================================================================

            // Announce to clan memebers
            if (clan != null)
            {
    		    clan.setHasHideout(this.getId()); // Set has hideout flag for new owner

    		    for (L2ClanMember member : clan.getMembers())
        		{
        			if (member.isOnline() && member.getPlayerInstance() != null)
        			{
        				member.getPlayerInstance().sendPacket(new PledgeShowInfoUpdate(clan, member.getPlayerInstance()));
        			}
        		}
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
	public final int getId() { return _ClanHallId; }

	public final L2DoorInstance getDoor(int doorId)
	{
	    if (doorId <= 0) return null;
	    
        for (int i = 0; i < getDoors().size(); i++)
        {
            L2DoorInstance door = getDoors().get(i);
            if (door.getDoorId() == doorId) return door;
        }
		return null;
	}

	public final List<L2DoorInstance> getDoors()
	{
        if (_Doors == null) _Doors = new FastList<L2DoorInstance>();
		return _Doors;
	}

    public final String getName() { return _Name; }

	public final int getOwnerId() { return _OwnerId; }

    public final Zone getZone()
    {
        if (_Zone == null) _Zone = ZoneManager.getInstance().getZone(ZoneType.getZoneTypeName(ZoneType.ZoneTypeEnum.ClanHall), _ClanHallId);
        return _Zone;
    }
}