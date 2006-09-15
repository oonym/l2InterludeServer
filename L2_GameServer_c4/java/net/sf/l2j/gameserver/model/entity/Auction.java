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
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.idfactory.IdFactory;
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

    
	// =========================================================
    // Schedule Task
    public class AutoEndTask implements Runnable
    {
        public void run()
        {
            try
            {
            	//TODO: Auto End Task code
            } catch (Throwable t){}
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
	private String _SellerName					= "";

	private int _CurrentBid						= 0;
	private int _StartingBid					= 0;

	// =========================================================
	// Constructor
	public Auction(int auctionId)
	{
		this._Id = auctionId;
		this.load();
	}

	// =========================================================
	// Method - Public
	public void setBid(L2PcInstance bidder, int bid)
	{
		// Update bid if new bid is higher
	    if (this.getHighestBidderId() > 0 && this.getHighestBidderId() != bidder.getObjectId() && bid > this.getHighestBidderMaxBid())
	    {
	    	this.updateInDB(bidder, bid);
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

            statement = con.prepareStatement(L2DatabaseFactory.getInstance().prepQuerySelect(new String[] {"bidderId", "bidderName", "maxBid"}, "auction_bid", "auctionId = ?", true));
            statement.setInt(1, getId());
            rs = statement.executeQuery();

            while (rs.next())
            {
            	this._HighestBidderId = rs.getInt("bidderId");
            	this._HighestBidderName = rs.getString("bidderName");
            	this._HighestBidderMaxBid = rs.getInt("maxBid");
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

	@SuppressWarnings("unused")
	private void giveItem()
	{
	}
	
	private boolean takeItem(L2PcInstance bidder, int itemId, int quantity)
	{
        // Take item from bidder
        if (this.getItemType()== getItemTypeName(ItemTypeEnum.ClanHall))
        {
        	// Take item from clan warehouse
        	if (bidder.getClan() != null && bidder.getClan().getWarehouse().getItemByItemId(this._AdenaId).getCount() >= quantity)
        	{
        		bidder.getClan().getWarehouse().destroyItemByItemId("Buy", this._AdenaId, quantity, bidder, bidder);
            	return true;
        	}
        }
        else
        {
        	// Take item from inventory
        	if (bidder.getAdena() >= quantity)
        	{
            	bidder.reduceAdena("Buy", quantity, bidder, false);
            	return true;
        	}
        }

		bidder.sendMessage("You do not have enough material");
        return false;
	}
	
	private void updateInDB(L2PcInstance bidder, int bid)
	{
		// Check and remove amount being bid
		if (!this.takeItem(bidder, this._AdenaId, bid)) return;

		java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;

            if (this.getHighestBidderId() > 0)
            {
                statement = con.prepareStatement("UPDATE auction_bid SET bidderId=?, bidderName=?, maxBid=? WHERE auctionId=?");
                statement.setInt(1, bidder.getObjectId());
                statement.setString(2, bidder.getName());
                statement.setInt(3, bid);
                statement.setInt(4, this.getId());
                statement.execute();
                statement.close();   
            }
            else
            {
                statement = con.prepareStatement("INSERT INTO auction_bid (id, auctionId, bidderId, bidderName, maxBid) VALUES (?, ?, ?, ?, ?)");
                statement.setInt(1, IdFactory.getInstance().getNextId());
                statement.setInt(2, this.getId());
                statement.setInt(3, bidder.getObjectId());
                statement.setString(4, bidder.getName());
                statement.setInt(5, bid);
                statement.execute();
                statement.close();   
            }

            // Announce to losing bidder that they have been out bidded
            //
            //

            // Update internal var
            this._HighestBidderId = bidder.getObjectId();
            this._HighestBidderMaxBid = bid;
            this._HighestBidderName = bidder.getName();
        }
        catch (Exception e)
        {
            System.out.println("Exception: Auction.updateInDB(L2PcInstance bidder, int bid): " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            try { con.close(); } catch (Exception e) {}
        }
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

	public final int getStartingBid() { return this._StartingBid; }
}