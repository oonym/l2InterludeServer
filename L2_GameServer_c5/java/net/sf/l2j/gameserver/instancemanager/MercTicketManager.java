package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.quest.QuestPcSpawn;


/**
 * @author yellowperil & Fulminus
 * This class is similar to the SiegeGuardManager, except it handles
 * the loading of the mercenary tickets that are dropped on castle floors 
 * by the castle lords.
 * These tickets (aka badges) need to be readded after each server reboot
 * except when the server crashed in the middle of an ongoig siege.
 * In addition, this class keeps track of the added tickets, in order to 
 * properly limit the number of mercenaries in each castle and the 
 * number of mercenaries from each mercenary type.
 * Finally, we provide auxilary functions to identify the castle in 
 * which each item (and its corresponding NPC) belong to, in order to 
 * help avoid mixing them up.
 *  
 */
public class MercTicketManager
{
    protected static Logger _log = Logger.getLogger(CastleManager.class.getName());

    // =========================================================
    private static MercTicketManager _Instance;
    public static final MercTicketManager getInstance()
    {
    	//CastleManager.getInstance();
        if (_Instance == null)
        {
    		System.out.println("Initializing MercTicketManager");
            _Instance = new MercTicketManager();
            _Instance.load();
        }
        return _Instance;
    }
    // =========================================================

    
    // =========================================================
    // Data Field
    private List<L2ItemInstance> _DroppedTickets;	// to keep track of items on the ground
    
    //TODO move all these values into siege.properties
    // max tickets per merc type = 10 + (castleid * 2)?
	// max ticker per castle = 40 + (castleid * 20)?
    private final int[] maxmercpertype = {
            10, 10, 10, 10, 10, 10, 10, 10, 10, 10,// Gludio
            15, 15, 15, 15, 15, 15, 15, 15, 15, 15, // Dion
            10, 10, 10, 10, 10, 10, 10, 10, 10, 10,// Giran
            10, 10, 10, 10, 10, 10, 10, 10, 10, 10,// Oren
            10, 10, 10, 10, 10, 10, 10, 10, 10, 10 // Aden
            };
    private final int[] mercsmaxpercastle = {
            50, // Gludio
            75, // Dion
            100,// Giran
            150,// Oren
            200 // Aden
            };
    
    private final int[] _ItemIds = {
            3960, 3961, 3962, 3963, 3964, 3965, 3966, 3967, 3968, 3969, // Gludio
            3973, 3974, 3975, 3976, 3977, 3978, 3979, 3980, 3981, 3982, // Dion
            3986, 3987, 3988, 3989, 3990, 3991, 3992, 3993, 3994, 3995, // Giran
            3999, 4000, 4001, 4002, 4003, 4004, 4005, 4006, 4007, 4008, // Oren
            4012, 4013, 4014, 4015, 4016, 4017, 4018, 4019, 4020, 4021 // Aden
            };

    private final int[] _NpcIds = {
           35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, // Gludio
           35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, // Dion
           35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, // Giran
           35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019, // Oren
           35010, 35011, 35012, 35013, 35014, 35015, 35016, 35017, 35018, 35019 // Aden
           };
    
    // =========================================================
    // Constructor
    public MercTicketManager()
    {
    }
    
    // =========================================================
    // Method - Public
    // returns the castleId for the passed ticket item id
    public int getTicketCastleId(int itemId)
    {
    	if (itemId >= _ItemIds[0] &&  itemId <= _ItemIds[9] )
    		return 1;	// Gludio
    	if (itemId >= _ItemIds[10] &&  itemId <= _ItemIds[19] )
    		return 2;	// Dion
    	if (itemId >= _ItemIds[20] &&  itemId <= _ItemIds[29] )
    		return 3;	// Giran
    	if (itemId >= _ItemIds[30] &&  itemId <= _ItemIds[39] )
    		return 4;	// Oren
    	if (itemId >= _ItemIds[40] &&  itemId <= _ItemIds[49] )
    		return 5;	// Aden
    	//if (itemId >= _ItemIds[50] &&  itemId <= _ItemIds[59] )
    	//	return 6;	// Heinne - todo
    	return -1;
    }

    public void reload()
    {
    	this.getDroppedTickets().clear();
    	this.load();
    }

    // =========================================================
    // Method - Private
    private final void load()
    {
        java.sql.Connection con = null;
        // load merc tickets into the world
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection();
	        statement = con.prepareStatement("SELECT * FROM castle_siege_guards Where isHired = 1");
	        rs = statement.executeQuery();
	        
	        int npcId;
	        int itemId;
	        int x,y,z;
	        // start index to begin the search for the itemId corresponding to this NPC
	        // this will help with: 
	        //    a) skip unnecessary iterations in the search loop
	        //    b) avoid finding the wrong itemId whenever tickets of different spawn the same npc!  
	        int startindex = 0;

	        while (rs.next())
	        {
	        	npcId = rs.getInt("npcId");
            	x = rs.getInt("x");
            	y = rs.getInt("y");
            	z = rs.getInt("z");
            	Castle castle = CastleManager.getInstance().getCastle(x,y);
            	if(castle != null)
            		startindex = 10*(castle.getCastleId()-1);
	        	
            	// find the FIRST ticket itemId with spawns the saved NPC in the saved location 
	        	for (int i=startindex;i<_NpcIds.length;i++)
	                if (_NpcIds[i] == npcId) // Find the index of the item used
	                {	
	    	        	// only handle tickets if a siege is not ongoing in this npc's castle
	                	
	                	if((castle != null) && !(castle.getSiege().getIsInProgress()))
	                	{
	                		itemId = _ItemIds[i];
		    	        	// create the ticket in the gameworld
		    	    		L2ItemInstance dropticket = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		    	    		dropticket.setLocation(L2ItemInstance.ItemLocation.INVENTORY);
		    	    		dropticket.dropMe(null, x, y, z);
		    	            dropticket.setDropTime(0); //avoids it from beeing removed by the auto item destroyer
		    	            L2World.getInstance().storeObject(dropticket);
		    	            getDroppedTickets().add(dropticket);
	                	}
	                	break;
	                }
	        }
            statement.close();

            System.out.println("Loaded: " + getDroppedTickets().size() + " Mercenary Tickets");
        }
        catch (Exception e)
        {
            System.out.println("Exception: loadMercenaryData(): " + e.getMessage());
            e.printStackTrace();
        }
        finally {try { con.close(); } catch (Exception e) {}}
    }

    // =========================================================
    // Property - Public
    /**
     * Checks if the passed item has reached the limit of number of dropped
     * tickets that this SPECIFIC item may have in its castle
     */
    public boolean isAtTypeLimit(int itemId)
    {
    	int limit = -1; 
    	// find the max value for this item 
    	for (int i=0;i<_ItemIds.length;i++)
            if (_ItemIds[i] == itemId) // Find the index of the item used
            {	
            	limit = maxmercpertype[i];
            	break;
            }
    	
    	if (limit <= 0)
    		return true;
    	
    	int count = 0;
    	L2ItemInstance ticket;
    	for(int i=0; i<getDroppedTickets().size(); i++)
    	{
    		ticket = getDroppedTickets().get(i);
    		if ( ticket != null && ticket.getItemId() == itemId)
    			count++;    		
    	}
    	if(count >= limit)
    		return true;
    	return false;
    }
    
    /**
     * Checks if the passed item belongs to a castle which has reached its limit 
     * of number of dropped tickets.
     */
    public boolean isAtCasleLimit(int itemId)
    {
    	int castleId = getTicketCastleId(itemId);
    	if (castleId <= 0)
    		return true;
    	int limit = mercsmaxpercastle[castleId-1];
    	if (limit <= 0)
    		return true;
    	
    	int count = 0;
    	L2ItemInstance ticket;
    	for(int i=0; i<getDroppedTickets().size(); i++)
    	{
    		ticket = getDroppedTickets().get(i);
    		if ( (ticket != null) && (getTicketCastleId(ticket.getItemId()) == castleId) )
    			count++;    		
    	}
    	if(count >= limit)
    		return true;
    	return false;
    }
    
    /**
     * addTicket actions 
     * 1) find the npc that needs to be saved in the mercenary spawns, given this item
     * 2) Use the passed character's location info to add the spawn
     * 3) create a copy of the item to drop in the world
     * returns the id of the mercenary npc that was added to the spawn
     * returns -1 if this fails.
     */ 
    public int addTicket(int itemId, L2PcInstance activeChar, String[] messages)
    {
    	int x = activeChar.getX();
    	int y = activeChar.getY();
    	int z = activeChar.getZ();
    	int heading = activeChar.getHeading();
    	
        Castle castle = CastleManager.getInstance().getCastle(activeChar);
        if (castle == null)		//this should never happen at this point
        	return -1;

        //check if this item can be added here
        for (int i = 0; i < _ItemIds.length; i++)
        {
            if (_ItemIds[i] == itemId) // Find the index of the item used
            {
               // Temporary tap into QuestPcSpawn to spawn, auto chat, and despawn merc
                QuestPcSpawn qps = new QuestPcSpawn(new L2PcInstance[] {activeChar});
                int spawnObjectId = qps.addSpawn(_NpcIds[i], x, y, z, 3000, messages, 0); // Spawn, auto chat, and despawn merc.
                /*L2Spawn spawn = */qps.getSpawn(spawnObjectId);

                // Hire merc for this caslte.  NpcId is at the same index as the item used.
                castle.getSiege().getSiegeGuardManager().hireMerc(x, y, z, heading, _NpcIds[i]);
                
                // create the ticket in the gameworld
        		L2ItemInstance dropticket = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
        		dropticket.setLocation(L2ItemInstance.ItemLocation.INVENTORY);
                dropticket.dropMe(null, x, y, z);
                dropticket.setDropTime(0); //avoids it from beeing removed by the auto item destroyer
                L2World.getInstance().storeObject(dropticket);	//add to the world
                // and keep track of this ticket in the list
                _DroppedTickets.add(dropticket);

                return _NpcIds[i];
            }
        }
        return -1;
    }
    
    /**
     * Delete all tickets from a castle; 
     * remove the items from the world and remove references to them from this class  
     */
    public void deleteTickets(int castleId)
    {
    	int i = 0;
    	while ( i<getDroppedTickets().size() )
    	{
    		L2ItemInstance item = getDroppedTickets().get(i);
    		if ((item != null) && (getTicketCastleId(item.getItemId()) == castleId))
    		{
    			item.decayMe();
    			L2World.getInstance().removeObject(item);
    		 
    			// remove from the list
    			getDroppedTickets().remove(i);
    		}
    		else
    			i++;
    	}
    }
    
    
    /**
     * remove a single ticket and its associated spawn from the world
     * (used when the castle lord picks up a ticket, for example) 
     */
    public void removeTicket(L2ItemInstance item)
    {
    	int itemId = item.getItemId();
    	int npcId = -1;
    	
    	// find the FIRST ticket itemId with spawns the saved NPC in the saved location 
    	for (int i=0;i<_ItemIds.length;i++)
            if (_ItemIds[i] == itemId) // Find the index of the item used
            {	
            	npcId = _NpcIds[i];
            	break;
            }
    	// find the castle where this item is
    	Castle castle = CastleManager.getInstance().getCastle(getTicketCastleId(itemId));
    	
    	if (npcId > 0 && castle != null)
    	{
    		(new SiegeGuardManager(castle)).removeMerc(npcId, item.getX(), item.getY(), item.getZ());
    	}
    	
    	getDroppedTickets().remove(item);
    }
    public int[] getItemIds()
    {
        return _ItemIds;
    }
    
    public final List<L2ItemInstance> getDroppedTickets()
    {
        if (_DroppedTickets == null) _DroppedTickets = new FastList<L2ItemInstance>();
        return _DroppedTickets;
    }
}
