package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.entity.Auction;

public class AuctionManager
{
    protected static Logger _log = Logger.getLogger(AuctionManager.class.getName());

    // =========================================================
    private static AuctionManager _Instance;
    public static final AuctionManager getInstance()
    {
        if (_Instance == null)
        {
    		System.out.println("Initializing AuctionManager");
        	_Instance = new AuctionManager();
        	_Instance.load();
        }
        return _Instance;
    }
    // =========================================================

    
    // =========================================================
    // Data Field
    private List<Auction> _Auctions;
    
    // =========================================================
    // Constructor
    public AuctionManager()
    {
    }

    // =========================================================
    // Method - Public
    public void reload()
    {
    	this.getAuctions().clear();
    	this.load();
    }

    // =========================================================
    // Method - Private
    private final void load()
    {
        java.sql.Connection con = null;
        try
        {
            PreparedStatement statement;
            ResultSet rs;

            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("Select id from auction order by id");
            rs = statement.executeQuery();

            while (rs.next())
            {
            	getAuctions().add(new Auction(rs.getInt("id")));
            }

            statement.close();

            System.out.println("Loaded: " + getAuctions().size() + " auction(s)");
        }
        catch (Exception e)
        {
            System.out.println("Exception: AuctionManager.load(): " + e.getMessage());
            e.printStackTrace();
        }
        
        finally {try { con.close(); } catch (Exception e) {}}
    }

    // =========================================================
    // Property - Public
    public final Auction getAuction(int auctionId)
    {
        int index = getAuctionIndex(auctionId);
        if (index >= 0) return getAuctions().get(index);
        return null;
    }

    public final int getAuctionIndex(int auctionId)
    {
        Auction auction;
        for (int i = 0; i < getAuctions().size(); i++)
        {
        	auction = getAuctions().get(i);
            if (auction != null && auction.getId() == auctionId) return i;
        }
        return -1;
    }

    public final List<Auction> getAuctions()
    {
        if (_Auctions == null) _Auctions = new FastList<Auction>();
        return _Auctions;
    }
}
