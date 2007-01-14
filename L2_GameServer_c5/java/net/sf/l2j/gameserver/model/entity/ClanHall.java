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
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.DoorTable;
import net.sf.l2j.gameserver.MapRegionTable;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
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
    private int _lease                         = 0;
    private String _desc                       = "";
    private String _location                   = "";
    private Calendar _paidUntil;
    private Zone _Zone;
    private int _grade;
    private List<ClanHallFunction> _functions = new FastList<ClanHallFunction>();
    
    //clan hall functions
    public static final int FUNC_TELEPORT = 1;
    public static final int FUNC_ITEM_CREATE = 2;
    public static final int FUNC_RESTORE_HP = 3;
    public static final int FUNC_RESTORE_MP = 4;
    public static final int FUNC_RESTORE_EXP = 5;
    public static final int FUNC_SUPPORT = 6;
    
    private class AutoTask implements Runnable
    {
        public AutoTask()
        {
            //Do nothing
        }
        public void run()
        {
            try
            {
                if (getPaidUntil() - Calendar.getInstance().getTimeInMillis() > 0)
                {
                    ThreadPoolManager.getInstance().scheduleGeneral(new AutoTask(), getPaidUntil() - Calendar.getInstance().getTimeInMillis());
                }
                else
                    if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= getLease())
                    {
                        ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_rental_fee", 57, getLease(), null, null);
                        if (Config.DEBUG)
                        	_log.warning("deducted "+getLease()+" adena from "+getName()+" owner's cwh for functions");
                        updateRentTime(Calendar.getInstance().getTimeInMillis()+ 604800000);
                        ThreadPoolManager.getInstance().scheduleGeneral(new AutoTask(), getPaidUntil() - Calendar.getInstance().getTimeInMillis()); //TODO not sure if this should be like exactly like it :p
                    }
                    else
                        setOwner(null);
                
            } catch (Throwable t) {
                
            }
        }
    }
    
    private void startAutoTask()
    {
        //Calendar tmp = Calendar.getInstance();
        getPaidUntilCalendar().set(Calendar.MINUTE, 0);
        ThreadPoolManager.getInstance().scheduleGeneral(new AutoTask(), 1000);
        if (Config.DEBUG) 
        _log.warning("clan hall lease is gonna be deducted from "+getName()+" owner's cwh at"+_paidUntil.get(Calendar.DAY_OF_MONTH)+"/"+_paidUntil.get(Calendar.MONTH));
    }

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
    public boolean checkIfInZone(L2Object obj) 
    { 
    	return checkIfInZone(obj.getX(), obj.getY()); 
    }

    /** Return true if object is inside the zone */
    public boolean checkIfInZone(int x, int y) 
    {
    	Zone zone = getZone();
    	
    	if (zone == null)
    		return false;
    	else
    		return zone.checkIfInZone(x, y); 
    }

    public double findDistanceToZone(int x, int y, int z, boolean checkZ) 
    { 
    	Zone zone = getZone();

    	if (zone == null)
    		return 99999999;
    	else
    		return zone.findDistanceToZone(x, y, z, checkZ); 
    }
	
	public void openCloseDoor(int doorId, boolean open) 
	{
		this.openCloseDoor(getDoor(doorId), open); 
	}

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

            statement = con.prepareStatement("select * from clanhall where id = ?");
            statement.setInt(1, getId());
            rs = statement.executeQuery();

            while (rs.next())
            {
        	    _Name = rs.getString("name");
        	    _OwnerId = rs.getInt("ownerId");
                _lease = rs.getInt("lease");
                _desc = rs.getString("desc");
                _location = rs.getString("location");
                _paidUntil = Calendar.getInstance();
                _paidUntil.setTimeInMillis(rs.getLong("paidUntil"));
                _grade = rs.getInt("Grade");
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
        if (getOwnerId() == 0) //this should never happen, but one never knows ;)
            return;
        if (Config.DEBUG)
            _log.warning("found owner for clanhall: "+getName());
        loadFunctions();
        startAutoTask();
	}
    
    private void loadFunctions()
    {
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("Select * from clanhall_functions where hall_id = ?");
            statement.setInt(1, getId());
            rs = statement.executeQuery();

            while (rs.next())
            {
                _functions.add(new ClanHallFunction(rs.getInt("type"), rs.getInt("lvl"), rs.getInt("lease"), rs.getLong("rate"), rs.getLong("endTime"), rs.getBoolean("inDebt")));
            }

            statement.close();

        }
        catch (Exception e)
        {
        	_log.log(Level.SEVERE, "Exception: ClanHall.loadFunctions(): " + e.getMessage(),e);
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
	// Property
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
    
    public final int getLease()
    {
        return _lease;
    }
    
    public final String getDesc()
    {
        return _desc;
    }
    
    public final String getLocation()
    {
        return _location;
    }
    
    public final long getPaidUntil()
    {
        return _paidUntil.getTimeInMillis();
    }
    
    public final Calendar getPaidUntilCalendar()
    {
        return _paidUntil;
    }
    
    public final int getGrade()
    {
        return _grade;
    }
    
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
    
    public ClanHallFunction getFunction(int type)
    {        
        for (int i = 0; i < _functions.size(); i++)
        {
            if (_functions.get(i).getType() == type) return _functions.get(i);
        }
        return null;
    }
    
    public void removeFunctions(int functionType)
    {
        for (int i = 0; i < _functions.size(); i++)
        {
            if (_functions.get(i).getType() == functionType) _functions.remove(i);
        }
       //===================================== Removes from DB===============
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;

            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("DELETE FROM clanhall_functions WHERE hall_id=? AND type=?");
            statement.setInt(1, getId());
            statement.setInt(2, functionType);
            statement.execute();
            statement.close();
            // ============================================================================

        }
        catch (Exception e)
        {
            _log.log(Level.SEVERE, "Exception: ClanHall.removeFunctions(int functionType): " + e.getMessage(),e);
        }
        finally {try { con.close(); } catch (Exception e) {}}
    }
    
    public boolean updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew)
    {
        if (Config.DEBUG)
    	_log.warning("Called ClanHall.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew)");

       //===================================== Removes from DB===============
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;

            con = L2DatabaseFactory.getInstance().getConnection();

            if (addNew)
            {
                if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= lease)
                {
                    ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_function_fee", 57, lease, null, null);
                }
                else
                {
                    return false;
                }
                statement = con.prepareStatement("INSERT INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");
                statement.setInt(1, getId());
                statement.setInt(2, type);
                statement.setInt(3, lvl);
                statement.setInt(4, lease);
                statement.setLong(5, rate);
                statement.setLong(6, time);
                statement.execute();
                statement.close();
                _functions.add(new ClanHallFunction(type, lvl, lease, rate, time, false));
                if (Config.DEBUG)
                _log.warning("INSERT INTO clanhall_functions (hall_id, type, lvl, lease, rate, endTime) VALUES (?,?,?,?,?,?)");
            }
            else
            {
                if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= lease-getFunction(type).getLease())
                {
                    if (lease-getFunction(type).getLease()>0)
                    ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_function_fee", 57, lease-getFunction(type).getLease(), null, null);
                }
                else
                {
                    return false;
                }
                statement = con.prepareStatement("UPDATE clanhall_functions SET lvl=?, lease=? WHERE hall_id=? AND type=?");
                statement.setInt(1, lvl);
                statement.setInt(2, lease);
                statement.setInt(3, getId());
                statement.setInt(4, type);  
                statement.execute();
                statement.close();
                getFunction(type).setLvl(lvl);
                getFunction(type).setLease(lease);
                if (Config.DEBUG)
                _log.warning("UPDATE clanhall_functions WHERE hall_id=? AND id=? SET lvl, lease");
            }
            // ============================================================================

        }
        catch (Exception e)
        {
           _log.log(Level.SEVERE, "Exception: ClanHall.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e.getMessage(),e);
        }
        finally {try { con.close(); } catch (Exception e) {}}
        return true;
    }
    
    public void updateRentTime(long paidUntil)
    {
        _paidUntil.setTimeInMillis(Calendar.getInstance().getTimeInMillis()+604800000);
        _paidUntil.set(Calendar.MINUTE,0);
        
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            
            statement = con.prepareStatement("UPDATE clanhall SET paidUntil=? WHERE id=?");
            statement.setLong(1, paidUntil);
            statement.setInt(2, getId());
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
        	_log.log(Level.SEVERE, "Exception: ClanHall.updateRentTime(): " + e.getMessage(),e);
        }
        finally {try { con.close(); } catch (Exception e) {}}
    }
    
    public class ClanHallFunction
    {
        private int _type;
        private int _lvl;
        protected int _fee;
        private long _rate;
        private Calendar _endDate;
        protected boolean _inDebt;
        
        public ClanHallFunction(int type, int lvl, int lease, long rate, long time, boolean inDebt)
        {
            _type = type;
            _lvl = lvl;
            _fee = lease;
            _rate = rate;
            _endDate = Calendar.getInstance();
            _endDate.setTimeInMillis(time);
            _inDebt = inDebt;
            StartAutoTask();            
        }
        public int getType()
        {
            return _type;
        }
        public int getLvl()
        {
            return _lvl;
        }
        public int getLease()
        {
            return _fee;
        }
        public long getRate()
        {
            return _rate;
        }
        public void setLvl(int lvl)
        {
            _lvl = lvl;
        }
        public void setLease(int lease)
        {
            _fee = lease;
        }
        public void setEndTime(long time)
        {
            _endDate.setTimeInMillis(time);
        }
        public long getEndTime()
        {
            return _endDate.getTimeInMillis();
        }
        
        private void StartAutoTask()
        {
            boolean needsUpdating = false;
            if (getEndTime() <= Calendar.getInstance().getTimeInMillis())
            {
                needsUpdating = true;
                if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= _fee && ((_inDebt) && ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= _fee*2))
                {
                    ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_function_fee", 57, _fee, null, null);
                    _log.warning( "deducted "+_fee+" adena from "+getName()+" owner's cwh for functions" );
                }
                else if (!_inDebt)
                {
                    _inDebt = true;
                    updateRentTime(true);
                }
                else
                {
                    removeFunctions(getType());
                }
            }
            while (getEndTime() <= Calendar.getInstance().getTimeInMillis())
                setEndTime(Calendar.getInstance().getTimeInMillis() + getRate());
            if (needsUpdating)
                updateRentTime(false);
            ThreadPoolManager.getInstance().scheduleGeneral(new AutoTask(), 1000);
            _log.warning( "clan hall function fee is gonna be deducted from "+getName()+" owner's cwh at"+_endDate.get(Calendar.DAY_OF_MONTH)+"/"+_endDate.get(Calendar.MONTH)+"for functionId: "+getId());
        }
        
        public void updateRentTime(boolean inDebt)
        {
            java.sql.Connection con = null;
            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement;
                
                statement = con.prepareStatement("UPDATE clanhall_functions SET endTime=?, inDebt=? WHERE type=? AND hall_id=?");
                statement.setLong(1, getEndTime()+getRate());
                statement.setInt(2, inDebt ? 1 : 0);
                statement.setInt(3, getType());
                statement.setInt(4, getId());
                statement.execute();
                statement.close();
            }
            catch (Exception e)
            {
                _log.log(Level.SEVERE, "Exception: ClanHall.ClanHallFunction.updateRentTime(int functionType): " + e.getMessage(),e);
            }
            finally {try { con.close(); } catch (Exception e) {}}
        }
        
//      ==========================================================
        //AutoTask
        
        private class AutoTask implements Runnable
        {
            public AutoTask()
            {
                //Do nothing
            }
            public void run()
            {
                try
                {
                    if (getEndTime() - Calendar.getInstance().getTimeInMillis()  > 0)
                    {
                        ThreadPoolManager.getInstance().scheduleGeneral(new AutoTask(),  getEndTime() - Calendar.getInstance().getTimeInMillis());
                    }
                    else
                        if (ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= _fee && ((_inDebt) && ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().getAdena() >= _fee*2)) //if player didn't pay before add extra fee
                        {
                            ClanTable.getInstance().getClan(getOwnerId()).getWarehouse().destroyItemByItemId("CH_function_fee", 57, _fee, null, null);
                            updateRentTime(false);
                            ThreadPoolManager.getInstance().scheduleGeneral(new AutoTask(), getEndTime()+getRate() - Calendar.getInstance().getTimeInMillis());
                            if (Config.DEBUG)
                            	_log.warning("deducted "+_fee+" adena from "+getName()+" owner's cwh for functions");
                        }
                        else if (!_inDebt)
                        {
                            _inDebt = true;
                            updateRentTime(true);
                        }
                        else
                        {
                            removeFunctions(getType());
                        }
                    
                } catch (Throwable t) {
                    
                }
            }
        }
    }
}