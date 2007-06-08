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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.GameServer;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.instancemanager.AuctionManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class Auction
{
    protected static Logger _log = Logger.getLogger(Auction.class.getName());
	private int _Id								= 0;
	private int _AdenaId						= 57;
	private long _EndDate;
	private int _HighestBidderId				= 0;
	private String _HighestBidderName			= "";
	private int _HighestBidderMaxBid			= 0;
	private int _ItemId							= 0;
	private String _ItemName					= "";
	private int _ItemObjectId					= 0;
	private int _ItemQuantity					= 0;
	private String _ItemType					= "";
	private int _SellerId						= 0;
	private String _SellerClanName              = "";
	private String _SellerName					= "";
	private int _CurrentBid						= 0;
	private int _StartingBid					= 0;

	private Map<Integer, Bidder> _bidders        = new FastMap<Integer, Bidder>();
	public static String[] ItemTypeName =
	{
	             "ClanHall" 
	};
	public static enum ItemTypeEnum
	{
	    ClanHall
	}
	 public class Bidder
	 {
	     private String _Name;
	     private String _ClanName;
	     private int _Bid;
	     private Calendar _timeBid;
	     public Bidder(String name, String clanName, int bid, long timeBid)
	     {
	         _Name = name;
	         _ClanName = clanName;
	         _Bid = bid;
	         _timeBid = Calendar.getInstance();
	         _timeBid.setTimeInMillis(timeBid);
	     }
	     public String getName()
	     {
	         return _Name;
	     }
	     public String getClanName()
	     {
	         return _ClanName;
	     }
	     public int getBid()
	     {
	         return _Bid;
	     }
	     public Calendar getTimeBid()
	     {
	         return _timeBid;
	     }
	     public void setTimeBid(long timeBid)
	     {
	         _timeBid.setTimeInMillis(timeBid);
	     }
	     public void setBid(int bid)
	     {
	         _Bid = bid;
	     }
	 }
	/** Task Sheduler for endAuction */
    public class AutoEndTask implements Runnable
    {
        public AutoEndTask(){}
        public void run()
        {
            try
            {
                 endAuction();
            } catch (Throwable t) { }
        }
    }
    /** Constructor */

	public Auction(int auctionId)
	{
		_Id = auctionId;
		load();
        StartAutoTask();
	}
    public Auction(int itemId, L2Clan Clan, long delay, int bid, String name)
    {
        _Id = itemId;
        _EndDate = System.currentTimeMillis() + delay;
        _ItemId = itemId;
        _ItemName = name;
        _ItemType = "ClanHall";
        _SellerId = Clan.getLeaderId();
        _SellerName = Clan.getLeaderName();
        _SellerClanName = Clan.getName();
        _StartingBid = bid;
    }
    /** Load auctions */
	private void load()
	{
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("Select * from auction where id = ?");
            statement.setInt(1, getId());
            rs = statement.executeQuery();

            while (rs.next())
            {
        	    _CurrentBid = rs.getInt("currentBid");
        	    _EndDate= rs.getLong("endDate");        	    
        	    _ItemId = rs.getInt("itemId");
        	    _ItemName = rs.getString("itemName");
        	    _ItemObjectId = rs.getInt("itemObjectId");
        	    _ItemType = rs.getString("itemType");
        	    _SellerId = rs.getInt("sellerId");
                _SellerClanName = rs.getString("sellerClanName");
        	    _SellerName = rs.getString("sellerName");
        	    _StartingBid = rs.getInt("startingBid");
            }
            statement.close();            
            loadBid();
        }
        catch (Exception e)
        {
            System.out.println("Exception: Auction.load(): " + e.getMessage());
            e.printStackTrace();
        }
        finally {try { con.close(); } catch (Exception e) {}}
	}
	/** Load bidders **/
	private void loadBid()
	{
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("SELECT bidderId, bidderName, maxBid, clan_name, time_bid FROM auction_bid WHERE auctionId = ? ORDER BY maxBid DESC");
            statement.setInt(1, getId());
            rs = statement.executeQuery();

            while (rs.next())
            {
                if (rs.isFirst())
                {
                    _HighestBidderId = rs.getInt("bidderId");
                    _HighestBidderName = rs.getString("bidderName");
                    _HighestBidderMaxBid = rs.getInt("maxBid");
                }
                _bidders.put(rs.getInt("bidderId"), new Bidder(rs.getString("bidderName"), rs.getString("clan_name"), rs.getInt("maxBid"), rs.getLong("time_bid")));
            }

            statement.close();
        }
        catch (Exception e)
        {
            System.out.println("Exception: Auction.loadBid(): " + e.getMessage());
            e.printStackTrace();
        }
        finally {try { con.close(); } catch (Exception e) {}}
	}
    /** Task Manage */
    private void StartAutoTask()
    {
    	long currentTime = System.currentTimeMillis();
    	long taskDelay = 0;
        if (_EndDate <= currentTime){
        	_EndDate = currentTime + 7*24*60*60*1000;
        	saveAuctionDate();
        }else
        	taskDelay = _EndDate - currentTime;
        ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(), taskDelay);
    }
	public static String getItemTypeName(ItemTypeEnum value)
	{
	    return ItemTypeName[value.ordinal()];
	}
	/** Save Auction Data End */
    private void saveAuctionDate()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("Update auction set endDate = ? where id = ?");
            statement.setLong(1, _EndDate);
            statement.setInt(2, _Id);
            statement.execute();
        }
        catch (Exception e)
        {
        	 _log.log(Level.SEVERE, "Exception: saveAuctionDate(): " + e.getMessage(),e);
        } 
        finally {try { con.close(); } catch (Exception e) {}}
    }
    /** Set a bid */
	public void setBid(L2PcInstance bidder, int bid)
	{
	    int requiredAdena = bid;
	    if (getHighestBidderName().equals(bidder.getClan().getLeaderName()))
	    		requiredAdena = bid - getHighestBidderMaxBid();
		if ((getHighestBidderId() >0 && bid > this.getHighestBidderMaxBid()) 
				|| (getHighestBidderId() == 0 && bid > getStartingBid()))
	    {
			if(takeItem(bidder, 57, requiredAdena))
			{
				this.updateInDB(bidder, bid);
            	bidder.getClan().setAuctionBiddedAt(_Id, true);
            	return;
			}
	    }
		bidder.sendMessage("Invalid bid!");
	}
	/** Return Item in WHC */
	private void returnItem(String Clan, int itemId, int quantity, boolean penalty)
	{
        if (penalty)
            quantity *= 0.9; //take 10% tax fee if needed
        ClanTable.getInstance().getClanByName(Clan).getWarehouse().addItem("Outbidded", _AdenaId, quantity, null, null);
	}
	/** Take Item in WHC */
	private boolean takeItem(L2PcInstance bidder, int itemId, int quantity)
	{
    	if (bidder.getClan() != null && bidder.getClan().getWarehouse().getAdena() >= quantity)
    	{
    		bidder.getClan().getWarehouse().destroyItemByItemId("Buy", _AdenaId, quantity, bidder, bidder);
        	return true;
    	}
		bidder.sendMessage("You do not have enough adena");
        return false;
	}
	/** Update auction in DB */
	private void updateInDB(L2PcInstance bidder, int bid)
	{
		java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;

            if (getBidders().get(bidder.getClanId()) != null)
            {
                statement = con.prepareStatement("UPDATE auction_bid SET bidderId=?, bidderName=?, maxBid=?, time_bid=? WHERE auctionId=? AND bidderId=?");
                statement.setInt(1, bidder.getClanId());
                statement.setString(2, bidder.getClan().getLeaderName());
                statement.setInt(3, bid);
                statement.setLong(4, System.currentTimeMillis());
                statement.setInt(5, getId());
                statement.setInt(6, bidder.getClanId());
                statement.execute();
                statement.close();
            }
            else
            {
                statement = con.prepareStatement("INSERT INTO auction_bid (id, auctionId, bidderId, bidderName, maxBid, clan_name, time_bid) VALUES (?, ?, ?, ?, ?, ?, ?)");
                statement.setInt(1, IdFactory.getInstance().getNextId());
                statement.setInt(2, getId());
                statement.setInt(3, bidder.getClanId());
                statement.setString(4, bidder.getName());
                statement.setInt(5, bid);
                statement.setString(6, bidder.getClan().getName());
                statement.setLong(7, System.currentTimeMillis());
                statement.execute();
                statement.close();
                if (L2World.getInstance().getPlayer(_HighestBidderName) != null)
                    L2World.getInstance().getPlayer(_HighestBidderName).sendMessage("You have been out bidded");   
            }
            _HighestBidderId = bidder.getClanId();
            _HighestBidderMaxBid = bid;
            _HighestBidderName = bidder.getClan().getLeaderName();
            if (_bidders.get(_HighestBidderId) == null)
                _bidders.put(_HighestBidderId, new Bidder(_HighestBidderName, bidder.getClan().getName(), bid, Calendar.getInstance().getTimeInMillis()));
            else
            {
                _bidders.get(_HighestBidderId).setBid(bid);
                _bidders.get(_HighestBidderId).setTimeBid(Calendar.getInstance().getTimeInMillis());
            }
            bidder.sendMessage("You have bidded successfully");
        }
        catch (Exception e)
        {
        	 _log.log(Level.SEVERE, "Exception: Auction.updateInDB(L2PcInstance bidder, int bid): " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
	}
    /** Remove bids */
    private void removeBids()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            
            statement = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=?");
            statement.setInt(1, getId());
            statement.execute();
        }
        catch (Exception e)
        {
        	 _log.log(Level.SEVERE, "Exception: Auction.deleteFromDB(): " + e.getMessage(),e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
        for (Bidder b : _bidders.values())
        {
          if (ClanTable.getInstance().getClanByName(b.getClanName()).getHasHideout() == 0)
        	  returnItem(b.getClanName(), 57, 9*b.getBid()/10, false); // 10 % tax
          else
          {
        	  if (L2World.getInstance().getPlayer(b.getName()) != null)
        		  L2World.getInstance().getPlayer(b.getName()).sendMessage("Congratulation you have won ClanHall!");
          }
          ClanTable.getInstance().getClanByName(b.getClanName()).setAuctionBiddedAt(0, true);
        }
        _bidders.clear();
    }
    /** Remove auctions */
    public void deleteAuctionFromDB()
    {
        AuctionManager.getInstance().getAuctions().remove(this);
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("DELETE FROM auction WHERE itemId=?");
            statement.setInt(1, _ItemId);
            statement.execute();
            statement.close();
        }
        catch (Exception e)
        {
        	 _log.log(Level.SEVERE, "Exception: Auction.deleteFromDB(): " + e.getMessage(),e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
    }
    /** End of auction */
    public void endAuction()
    {
    	if(GameServer.gameServer.GetCHManager() != null && GameServer.gameServer.GetCHManager().loaded()){
	        if (_HighestBidderId == 0 && _SellerId == 0)
	        {
	            StartAutoTask();
	            return;
	        }
	        if (_HighestBidderId == 0 && _SellerId > 0)
	        {
	            /** If seller haven't sell ClanHall, auction removed,
	             *  THIS MUST BE CONFIRMED */
	        	int aucId = AuctionManager.getInstance().getAuctionIndex(_Id);
	        	AuctionManager.getInstance().getAuctions().remove(aucId);
	            return;
	        }
	        if (_SellerId > 0)
	        {
	            returnItem(_SellerClanName, 57, _HighestBidderMaxBid, true);
	            returnItem(_SellerClanName, 57, ClanHallManager.getInstance().getClanHall(_ItemId).getLease(), false);
	        }
		    deleteAuctionFromDB();
		    L2Clan Clan = ClanTable.getInstance().getClanByName(_bidders.get(_HighestBidderId).getClanName());
		    _bidders.remove(_HighestBidderId);
		    Clan.setAuctionBiddedAt(0, true);
		    removeBids();
		    ClanHallManager.getInstance().setOwner(_ItemId, Clan);
    	}else{
    		/** Task waiting ClanHallManager is loaded every 3s */
            ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(), 3000); 
    	}
    }
    /** Cancel bid */
    public void cancelBid(int bidder)
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            
            statement = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=? AND bidderId=?");
            statement.setInt(1, getId());
            statement.setInt(2, bidder);
            statement.execute();
        }
        catch (Exception e)
        {
        	 _log.log(Level.SEVERE, "Exception: Auction.cancelBid(String bidder): " + e.getMessage(),e);
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
        returnItem(_bidders.get(bidder).getClanName(), 57, _bidders.get(bidder).getBid(), true);
        ClanTable.getInstance().getClanByName(_bidders.get(bidder).getClanName()).setAuctionBiddedAt(0, true);
        _bidders.clear();
        loadBid();
    }
    /** Cancel auction */
    public void cancelAuction()
    {
        deleteAuctionFromDB();
        removeBids();
    }
    /** Confirm an auction */
    public void confirmAuction()
    {
        AuctionManager.getInstance().getAuctions().add(this);
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("INSERT INTO auction (id, sellerId, sellerName, sellerClanName, itemType, itemId, itemObjectId, itemName, itemQuantity, startingBid, currentBid, endDate) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"); 
            statement.setInt(1, getId());
            statement.setInt(2, _SellerId);
            statement.setString(3, _SellerName);
            statement.setString(4, _SellerClanName);
            statement.setString(5, _ItemType);
            statement.setInt(6, _ItemId);
            statement.setInt(7, _ItemObjectId);
            statement.setString(8, _ItemName);
            statement.setInt(9, _ItemQuantity);
            statement.setInt(10, _StartingBid);
            statement.setInt(11, _CurrentBid);
            statement.setLong(12, _EndDate); 
            statement.execute();
            statement.close();            
            this.loadBid();
        }
        catch (Exception e)
        {
            _log.log(Level.SEVERE, "Exception: Auction.load(): " + e.getMessage(),e);
        }
        finally {try { con.close(); } catch (Exception e) {}}
    }
    /** Get var auction */
	public final int getId() { return _Id; }
	public final int getCurrentBid() { return _CurrentBid; }
	public final long getEndDate() { return _EndDate; }
	public final int getHighestBidderId() { return _HighestBidderId; }
	public final String getHighestBidderName() { return _HighestBidderName; }
	public final int getHighestBidderMaxBid() { return _HighestBidderMaxBid; }
	public final int getItemId() { return _ItemId; }
	public final String getItemName() { return _ItemName; }
	public final int getItemObjectId() { return _ItemObjectId; }
	public final int getItemQuantity() { return _ItemQuantity; }
	public final String getItemType() { return _ItemType; }
	public final int getSellerId() { return _SellerId; }
	public final String getSellerName() { return _SellerName; }
    public final String getSellerClanName() { return _SellerClanName; }
	public final int getStartingBid() { return _StartingBid; }
    public final Map<Integer, Bidder> getBidders(){ return _bidders; };
}