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

    /*
     * TODO:
     * Announce to losing bidder that they have been out bidded
     * Take adena when bidding
     * Return adena when out bid
     * Give item when auction end
     * UpdateBidInDb
     * Schedule Auction end
     * Remove auction from auction and auction_bid table when auction end
     */
    
	// =========================================================
    public static enum ItemTypeEnum
    {
        ClanHall
    }
    
    public static String[] ItemTypeName =
        {
             "ClanHall" 
        };
    
    public static String getItemTypeName(ItemTypeEnum value)
    {
        return ItemTypeName[value.ordinal()];
    }

    // =========================================================
    // Schedule Task
    public class AutoEndTask implements Runnable
    {
        public AutoEndTask()
        {
            //do nothing???
        }
        public void run()
        {
            try
            {
                long timeRemaining = getEndDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
                if (timeRemaining > 0)
                {
                    ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(), timeRemaining); 
                }
                else
                {
                    endAuction();
                }
            } catch (Throwable t) { }
        }
    }
    
    private void StartAutoTask(boolean forced)
    {
        correctAuctionTime(forced);
        ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(), 1000);
    }
    
    private void correctAuctionTime(boolean forced)
    {
        boolean corrected = false;

        if (_EndDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() || forced)
        {
            // Since auction has past reschedule it to the next one (7 days)
            // This is usually caused by server being down
            corrected = true;
            if (forced) 
                setNextAuctionDate();
            else
                endAuction(); //end auction normally in case it had bidders and server was down when it ended
        }

        _EndDate.set(Calendar.MINUTE, 0);

        if (corrected) saveAuctionDate();
    }
    
    private void setNextAuctionDate()
    {
        while (_EndDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
        {
            // Set next auction date if auction has passed
            _EndDate.add(Calendar.DAY_OF_MONTH, 7); // Schedule to happen in 7 days
        }
    }
    
    private void saveAuctionDate()
    {
        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("Update auction set endDate = ? where id = ?");
            statement.setLong(1, _EndDate.getTimeInMillis());
            statement.setInt(2, _Id);
            statement.execute();
        }
        catch (Exception e)
        {
        	 _log.log(Level.SEVERE, "Exception: saveAuctionDate(): " + e.getMessage(),e);
        } 
        finally {try { con.close(); } catch (Exception e) {}}
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
    
    // =========================================================
    // Data Field
	private int _Id								= 0;

	private int _AdenaId						= 57;
	
	private Calendar _EndDate;

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

	// =========================================================
	// Constructor
	public Auction(int auctionId)
	{
		this._Id = auctionId;
		this.load();
        
        //end auction automatically
        StartAutoTask(false);
	}
    
    public Auction(int itemId, L2Clan Clan, long delay, int bid, String name)
    {
        _Id = itemId;
        _EndDate = Calendar.getInstance();
        _EndDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis()+delay);
        _EndDate.set(Calendar.MINUTE, 0);
        _ItemId = itemId;
        _ItemName = name;
        _ItemType = "ClanHall";
        _SellerId = Clan.getLeaderId();
        _SellerName = Clan.getLeaderName();
        _SellerClanName = Clan.getName();
        _StartingBid = bid;
    }

	// =========================================================
	// Method - Public
	public void setBid(L2PcInstance bidder, int bid)
	{
		// Update bid if new bid is higher
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

            statement = con.prepareStatement("Select * from auction where id = ?");
            statement.setInt(1, getId());
            rs = statement.executeQuery();

            while (rs.next())
            {
        	    this._CurrentBid = rs.getInt("currentBid");
        	    this._EndDate = Calendar.getInstance();
        	    this._EndDate.setTimeInMillis(rs.getLong("endDate"));        	    
        	    this._ItemId = rs.getInt("itemId");
        	    this._ItemName = rs.getString("itemName");
        	    this._ItemObjectId = rs.getInt("itemObjectId");
        	    this._ItemType = rs.getString("itemType");
        	    this._SellerId = rs.getInt("sellerId");
                this._SellerClanName = rs.getString("sellerClanName");
        	    this._SellerName = rs.getString("sellerName");
        	    this._StartingBid = rs.getInt("startingBid");
            }
            statement.close();            
            this.loadBid();
        }
        catch (Exception e)
        {
            System.out.println("Exception: Auction.load(): " + e.getMessage());
            e.printStackTrace();
        }
        finally {try { con.close(); } catch (Exception e) {}}
	}

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
                    this._HighestBidderId = rs.getInt("bidderId");
                    this._HighestBidderName = rs.getString("bidderName");
                    this._HighestBidderMaxBid = rs.getInt("maxBid");
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

	private void returnItem(String Clan, int itemId, int quantity, boolean penalty)
	{
        if (penalty)
            quantity *= 0.9; //take 10% tax fee if needed
        ClanTable.getInstance().getClanByName(Clan).getWarehouse().addItem("Outbidded", _AdenaId, quantity, null, null);
	}
	
	private boolean takeItem(L2PcInstance bidder, int itemId, int quantity)
	{
        // Take item from bidder
        /*if (this.getItemType()== getItemTypeName(ItemTypeEnum.ClanHall))
        {*/
        	// Take item from clan warehouse
        	if (bidder.getClan() != null && bidder.getClan().getWarehouse().getAdena() >= quantity)
        	{
        		bidder.getClan().getWarehouse().destroyItemByItemId("Buy", this._AdenaId, quantity, bidder, bidder);
            	return true;
        	}
        /*}
        else
        {
        	// Take item from inventory
        	if (bidder.getAdena() >= quantity)
        	{
            	bidder.reduceAdena("Buy", quantity, bidder, false);
            	return true;
        	}
        }*/

		bidder.sendMessage("You do not have enough adena");
        return false;
	}
	
	private void updateInDB(L2PcInstance bidder, int bid)
	{
		// Check and remove amount being bid
		//if (!this.takeItem(bidder, this._AdenaId, bid)) return;

		java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;

            if (this.getBidders().get(bidder.getClanId()) != null)
            {
                statement = con.prepareStatement("UPDATE auction_bid SET bidderId=?, bidderName=?, maxBid=?, time_bid=? WHERE auctionId=? AND bidderId=?");
                statement.setInt(1, bidder.getClanId());
                statement.setString(2, bidder.getClan().getLeaderName());
                statement.setInt(3, bid);
                statement.setLong(4, Calendar.getInstance().getTimeInMillis());
                statement.setInt(5, this.getId());
                statement.setInt(6, bidder.getClanId());
                statement.execute();
                statement.close();
            }
            else
            {
                statement = con.prepareStatement("INSERT INTO auction_bid (id, auctionId, bidderId, bidderName, maxBid, clan_name, time_bid) VALUES (?, ?, ?, ?, ?, ?, ?)");
                statement.setInt(1, IdFactory.getInstance().getNextId());
                statement.setInt(2, this.getId());
                statement.setInt(3, bidder.getClanId());
                statement.setString(4, bidder.getName());
                statement.setInt(5, bid);
                statement.setString(6, bidder.getClan().getName());
                statement.setLong(7, Calendar.getInstance().getTimeInMillis());
                statement.execute();
                statement.close();
                if (L2World.getInstance().getPlayer(_HighestBidderName) != null)
                    L2World.getInstance().getPlayer(_HighestBidderName).sendMessage("You have been out bidded");   
            }

            // Announce to losing bidder that they have been out bidded
            //
            //

            // Update internal var
            this._HighestBidderId = bidder.getClanId();
            this._HighestBidderMaxBid = bid;
            this._HighestBidderName = bidder.getClan().getLeaderName();
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
          {             
        	  returnItem(b.getClanName(), 57, 9*b.getBid()/10, false); // 10 % tax
        	  ClanTable.getInstance().getClanByName(b.getClanName()).setAuctionBiddedAt(0, true);
          }
          if (ClanTable.getInstance().getClanByName(b.getClanName()).getHasHideout() != 0)
          {
        	  ClanTable.getInstance().getClanByName(b.getClanName()).setAuctionBiddedAt(0, true);
        	  if (L2World.getInstance().getPlayer(b.getName()) != null)
        		  L2World.getInstance().getPlayer(b.getName()).sendMessage("Congratulation you have won ClanHall!");
          }             
        }
        _bidders.clear();
    }
    
    private void deleteAuctionFromDB()
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
    
    public void endAuction()
    {
        if (_HighestBidderId == 0 && _SellerId == 0)
        {
            StartAutoTask(true);
            return;
        }
        if (_HighestBidderId == 0 && _SellerId > 0)
        {
            deleteAuctionFromDB();
            return;
        }
        if (_SellerId > 0)
        {
            returnItem(_SellerClanName, 57, _HighestBidderMaxBid, true);
            returnItem(_SellerClanName, 57, ClanHallManager.getInstance().getClanHall(_ItemId).getLease(), false);
        }
        ClanHallManager.getInstance().getClanHall(_ItemId).setOwner(ClanTable.getInstance().getClanByName(_bidders.get(_HighestBidderId).getClanName()));
        deleteAuctionFromDB();
        removeBids();
    }
    
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
        _bidders.remove(bidder);
    }
    
    public void cancelAuction()
    {
        deleteAuctionFromDB();
        removeBids();
    }
    
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
            statement.setLong(12, _EndDate.getTimeInMillis()); 
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
	
	// =========================================================
	// Proeprty
	public final int getId() { return this._Id; }

	public final int getCurrentBid() { return this._CurrentBid; }

	public final Calendar getEndDate() { return this._EndDate; }

	public final int getHighestBidderId() { return this._HighestBidderId; }

	public final String getHighestBidderName() { return this._HighestBidderName; }

	public final int getHighestBidderMaxBid() { return this._HighestBidderMaxBid; }

	public final int getItemId() { return this._ItemId; }

	public final String getItemName() { return this._ItemName; }

	public final int getItemObjectId() { return this._ItemObjectId; }

	public final int getItemQuantity() { return this._ItemQuantity; }

	public final String getItemType() { return this._ItemType; }

	public final int getSellerId() { return this._SellerId; }

	public final String getSellerName() { return this._SellerName; }
    
    public final String getSellerClanName() { return this._SellerClanName; }

	public final int getStartingBid() { return this._StartingBid; }
    
    public final Map<Integer, Bidder> getBidders(){ return this._bidders; };
}