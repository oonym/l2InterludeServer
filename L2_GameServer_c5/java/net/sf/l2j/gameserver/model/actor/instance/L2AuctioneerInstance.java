package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.MapRegionTable;
import net.sf.l2j.gameserver.instancemanager.AuctionManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.Auction;
import net.sf.l2j.gameserver.model.entity.Auction.Bidder;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2AuctioneerInstance extends L2FolkInstance
{
    //private static Logger _log = Logger.getLogger(L2AuctioneerInstance.class.getName());

    private static int Cond_All_False = 0;
    private static int Cond_Busy_Because_Of_Siege = 1;
    private static int Cond_Regular = 3;
    
    private Map<Integer, Auction> _pendingAuctions = new FastMap<Integer, Auction>();

    public L2AuctioneerInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void onAction(L2PcInstance player)
    {
    	if (Config.DEBUG) _log.warning("Auctioneer activated");
        player.sendPacket(new ActionFailed());
        player.setTarget(this);
        player.setLastFolkNPC(this);
        super.onAction(player);
        if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
            showMessageWindow(player);
    }

    public void onBypassFeedback(L2PcInstance player, String command)
    {

        if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false)) return;
        player.sendPacket(new ActionFailed());

        int condition = validateCondition(player);
        if (condition == Cond_All_False)
        {
        	//TODO: html
        	player.sendMessage("Wrong conditions.");
        	return;
        }
        if (condition == Cond_Busy_Because_Of_Siege)
        {
        	//TODO: html
        	player.sendMessage("Busy because of siege.");
        	return;
        }
        else if (condition == Cond_Regular)
        {
            StringTokenizer st = new StringTokenizer(command, " ");
            String actualCommand = st.nextToken(); // Get actual command

            String val = "";
            if (st.countTokens() >= 1)
            {
                val = st.nextToken();
            }

            if (actualCommand.equalsIgnoreCase("auction"))
            {
                if (val == "") return;

                try
                {
                    int days = Integer.parseInt(val);
                    try
                    {
                        int bid = 0;
                        if (st.countTokens() >= 1) bid = Integer.parseInt(st.nextToken());

                        Auction a = new Auction(player.getClan().getHasHideout(), player.getClan(), days*86400000, bid, ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getName());
                        if (_pendingAuctions.get(a.getId()) != null)
                            _pendingAuctions.remove(a.getId());
                        
                        _pendingAuctions.put(a.getId(), a);
                        
                        String filename = "data/html/auction/AgitSale3.htm";                        
                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        html.setFile(filename);
                        html.replace("%x%", val);
                        html.replace("%AGIT_AUCTION_END_YY%", String.valueOf(a.getEndDate().get(Calendar.YEAR)));
                        html.replace("%AGIT_AUCTION_END_MM%", String.valueOf(a.getEndDate().get(Calendar.MONTH)+1));
                        html.replace("%AGIT_AUCTION_END_DD%", String.valueOf(a.getEndDate().get(Calendar.DAY_OF_MONTH)));
                        html.replace("%AGIT_AUCTION_END_HH%", String.valueOf(a.getEndDate().get(Calendar.HOUR_OF_DAY)));
                        html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
                        html.replace("%AGIT_AUCTION_MIN%", String.valueOf(a.getStartingBid()));
                        html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getDesc());
                        html.replace("%objectId%", String.valueOf((getObjectId())));
                        player.sendPacket(html);
                    }
                    catch (Exception e)
                    {
                        player.sendMessage("Invalid bid!");
                    }
                }
                catch (Exception e)
                {
                    player.sendMessage("Invalid auction duration!");
                }
                return;
            }
            if (actualCommand.equalsIgnoreCase("confirmAuction"))
            {
                try
                {
                    Auction a = _pendingAuctions.get(player.getClan().getHasHideout());
                    a.confirmAuction();
                    _pendingAuctions.remove(player.getClan().getHasHideout());
                }
                catch (Exception e)
                {
                    player.sendMessage("Invalid auction");
                }                   
                return;
            }
            else if (actualCommand.equalsIgnoreCase("bidding"))
            {
                if (val == "") return;               
                if(Config.DEBUG) player.sendMessage("bidding show successful");

                try
                {
                    int auctionId = Integer.parseInt(val);
                    if(Config.DEBUG) player.sendMessage("auction test started");
                    String filename = "data/html/auction/AgitAuctionInfo.htm";
                    Auction a = AuctionManager.getInstance().getAuction(auctionId);

                    NpcHtmlMessage html = new NpcHtmlMessage(1);
                    html.setFile(filename);
                    html.replace("%AGIT_NAME%", a.getItemName());
                    html.replace("%OWNER_PLEDGE_NAME%", a.getSellerClanName());
                    html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
                    html.replace("%AGIT_SIZE%", "30 ");
                    html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHall(a.getItemId()).getLease()));
                    html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHall(a.getItemId()).getLocation());
                    html.replace("%AGIT_AUCTION_END_YY%", String.valueOf(a.getEndDate().get(Calendar.YEAR)));
                    html.replace("%AGIT_AUCTION_END_MM%", String.valueOf(a.getEndDate().get(Calendar.MONTH)+1));
                    html.replace("%AGIT_AUCTION_END_DD%", String.valueOf(a.getEndDate().get(Calendar.DAY_OF_MONTH)));
                    html.replace("%AGIT_AUCTION_END_HH%", String.valueOf(a.getEndDate().get(Calendar.HOUR_OF_DAY)));
                    html.replace("%AGIT_AUCTION_REMAIN%", String.valueOf((a.getEndDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 3600000)+" hours "+String.valueOf((((a.getEndDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis()) / 60000) % 60))+" minutes");
                    html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
                    html.replace("%AGIT_AUCTION_COUNT%", String.valueOf(a.getBidders().size()));
                    html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHall(a.getItemId()).getDesc());
                    html.replace("%AGIT_LINK_BACK%", "bypass -h npc_"+getObjectId()+"_list");
                    html.replace("%AGIT_LINK_BIDLIST%", "bypass -h npc_"+getObjectId()+"_bidlist "+a.getId());
                    html.replace("%AGIT_LINK_RE%", "bypass -h npc_"+getObjectId()+"_bid1 "+a.getId());
                    player.sendPacket(html);
                }
                catch (Exception e)
                {
                    player.sendMessage("Invalid auction!");
                }

                return;
            }
            else if (actualCommand.equalsIgnoreCase("bid"))
            {
                if (val == "") return;

                try
                {
                    int auctionId = Integer.parseInt(val);
                    try
                    {
                        int bid = 0;
                        if (st.countTokens() >= 1) bid = Integer.parseInt(st.nextToken());

                        AuctionManager.getInstance().getAuction(auctionId).setBid(player, bid);
                    }
                    catch (Exception e)
                    {
                        player.sendMessage("Invalid bid!");
                    }
                }
                catch (Exception e)
                {
                    player.sendMessage("Invalid auction!");
                }

                return;
            }
            else if (actualCommand.equalsIgnoreCase("bid1"))
            {
                if (player.getClan() == null || player.getClan().getLevel() < 2)
                {
                    player.sendMessage("Your clan's level needs to be at least 2, before you can bid in an auction");
                    return;
                }

                if (val == "") return;
                if ((player.getClan().getAuctionBiddedAt() > 0 && player.getClan().getAuctionBiddedAt() != Integer.parseInt(val)) || player.getClan().getHasHideout() > 0)
                {
                    player.sendMessage("You can't bid at more than one auction");
                    return;
                }

                try
                {
                    String filename = "data/html/auction/AgitBid1.htm";

                    int minimumBid = AuctionManager.getInstance().getAuction(Integer.parseInt(val)).getHighestBidderMaxBid();
                    if (minimumBid == 0) minimumBid = AuctionManager.getInstance().getAuction(Integer.parseInt(val)).getStartingBid(); 
                    
                    NpcHtmlMessage html = new NpcHtmlMessage(1);
                    html.setFile(filename);
                    html.replace("%AGIT_LINK_BACK%", "bypass -h npc_"+getObjectId()+"_bidding "+val);
                    html.replace("%PLEDGE_ADENA%", String.valueOf(player.getClan().getWarehouse().getAdena())); 
                    html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(minimumBid));
                    html.replace("npc_%objectId%_bid", "npc_"+getObjectId()+"_bid "+val);
                    player.sendPacket(html);
                    return;
                }
                catch (Exception e)
                {
                    player.sendMessage("Invalid auction!");
                }
                return;
            }
            else if (actualCommand.equalsIgnoreCase("list"))
            {
                //char spec = '"';
            	if (Config.DEBUG) player.sendMessage("cmd list: auction test started");
                String items = "";
                List<Auction> auctions =AuctionManager.getInstance().getAuctions();
                for(Auction a:auctions)
                {
                    items+="<tr>" +
                            "<td>"+ClanHallManager.getInstance().getClanHall(a.getItemId()).getLocation()+"</td><td><a action=\"bypass -h npc_"+getObjectId()+"_bidding "+a.getId()+"\">"+a.getItemName()+"</a></td><td>"+a.getEndDate().get(Calendar.YEAR)+"/"+(a.getEndDate().get(Calendar.MONTH)+1)+"/"+a.getEndDate().get(Calendar.DATE)+"</td><td>"+a.getStartingBid()+"</td>" +
                            "</tr>";
                }
                String filename = "data/html/auction/AgitAuctionList.htm";

                NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                html.replace("%itemsField%", items);
                player.sendPacket(html);
                return;
            }
            else if (actualCommand.equalsIgnoreCase("bidlist"))
            {
                int auctionId = 0;
                if (val == "")
                {
                    if (player.getClan().getAuctionBiddedAt() <= 0)
                        return;
                    else
                        auctionId = player.getClan().getAuctionBiddedAt();
                }
                else
                    auctionId = Integer.parseInt(val);
                if (Config.DEBUG) player.sendMessage("cmd bidlist: auction test started");
                String biders = "";
                Map<Integer, Bidder> bidders = AuctionManager.getInstance().getAuction(auctionId).getBidders();
                for(Bidder b :bidders.values())
                {
                    biders+="<tr>" +
                            "<td>"+b.getClanName()+"</td><td>"+b.getName()+"</td><td>"+b.getTimeBid().get(Calendar.YEAR)+"/"+(b.getTimeBid().get(Calendar.MONTH)+1)+"/"+b.getTimeBid().get(Calendar.DATE)+"</td><td>"+b.getBid()+"</td>" +
                            "</tr>";
                }
                String filename = "data/html/auction/AgitBidderList.htm";

                NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                html.replace("%AGIT_LIST%", biders);
                html.replace("%x%", val);
                html.replace("%objectId%", String.valueOf(getObjectId()));
                player.sendPacket(html);
                return;
            }
            else if (actualCommand.equalsIgnoreCase("selectedItems"))
            {
                if (player.getClan() != null && player.getClan().getHasHideout() == 0 && player.getClan().getAuctionBiddedAt() > 0)
                {
                    String filename = "data/html/auction/AgitBidInfo.htm";
                    NpcHtmlMessage html = new NpcHtmlMessage(1);
                    html.setFile(filename);
                    Auction a = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
                    html.replace("%AGIT_NAME%", a.getItemName());
                    html.replace("%OWNER_PLEDGE_NAME%", a.getSellerClanName());
                    html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
                    html.replace("%AGIT_SIZE%", "30 ");
                    html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHall(a.getItemId()).getLease()));
                    html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHall(a.getItemId()).getLocation());
                    html.replace("%AGIT_AUCTION_END_YY%", String.valueOf(a.getEndDate().get(Calendar.YEAR)));
                    html.replace("%AGIT_AUCTION_END_MM%", String.valueOf(a.getEndDate().get(Calendar.MONTH)+1));
                    html.replace("%AGIT_AUCTION_END_DD%", String.valueOf(a.getEndDate().get(Calendar.DAY_OF_MONTH)));
                    html.replace("%AGIT_AUCTION_END_HH%", String.valueOf(a.getEndDate().get(Calendar.HOUR_OF_DAY)));
                    html.replace("%AGIT_AUCTION_REMAIN%", String.valueOf((a.getEndDate().getTimeInMillis()-Calendar.getInstance().getTimeInMillis()) / 3600000)+" hours "+String.valueOf((((a.getEndDate().getTimeInMillis()-Calendar.getInstance().getTimeInMillis()) / 60000) % 60))+" minutes");
                    html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
                    html.replace("%AGIT_AUCTION_MYBID%", String.valueOf(a.getBidders().get(player.getClanId()).getBid()));
                    html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHall(a.getItemId()).getDesc());
                    html.replace("%objectId%", String.valueOf(getObjectId()));
                    player.sendPacket(html);
                    return;
                }
                else if (player.getClan() != null && AuctionManager.getInstance().getAuction(player.getClan().getHasHideout()) != null)
                {
                    String filename = "data/html/auction/AgitSaleInfo.htm";
                    NpcHtmlMessage html = new NpcHtmlMessage(1);
                    html.setFile(filename);
                    Auction a = AuctionManager.getInstance().getAuction(player.getClan().getHasHideout());
                    html.replace("%AGIT_NAME%", a.getItemName());
                    html.replace("%AGIT_OWNER_PLEDGE_NAME%", a.getSellerClanName());
                    html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
                    html.replace("%AGIT_SIZE%", "30 ");
                    html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHall(a.getItemId()).getLease()));
                    html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHall(a.getItemId()).getLocation());
                    html.replace("%AGIT_AUCTION_END_YY%", String.valueOf(a.getEndDate().get(Calendar.YEAR)));
                    html.replace("%AGIT_AUCTION_END_MM%", String.valueOf(a.getEndDate().get(Calendar.MONTH)+1));
                    html.replace("%AGIT_AUCTION_END_DD%", String.valueOf(a.getEndDate().get(Calendar.DAY_OF_MONTH)));
                    html.replace("%AGIT_AUCTION_END_HH%", String.valueOf(a.getEndDate().get(Calendar.HOUR_OF_DAY)));
                    html.replace("%AGIT_AUCTION_REMAIN%", String.valueOf((a.getEndDate().getTimeInMillis()-Calendar.getInstance().getTimeInMillis()) / 3600000)+" hours "+String.valueOf((((a.getEndDate().getTimeInMillis()-Calendar.getInstance().getTimeInMillis()) / 60000) % 60))+" minutes");
                    html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
                    html.replace("%AGIT_AUCTION_BIDCOUNT%", String.valueOf(a.getBidders().size()));
                    html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHall(a.getItemId()).getDesc());
                    html.replace("%id%", String.valueOf(a.getId()));
                    html.replace("%objectId%", String.valueOf(getObjectId()));
                    player.sendPacket(html);
                    return;
                }
                else if(player.getClan() != null && player.getClan().getHasHideout() != 0)
                {
                    int ItemId = player.getClan().getHasHideout();
                    String filename = "data/html/auction/AgitInfo.htm";
                    NpcHtmlMessage html = new NpcHtmlMessage(1);
                    html.setFile(filename);
                    html.replace("%AGIT_NAME%", ClanHallManager.getInstance().getClanHall(ItemId).getName());
                    html.replace("%AGIT_OWNER_PLEDGE_NAME%", player.getClan().getName());
                    html.replace("%OWNER_PLEDGE_MASTER%", player.getClan().getLeaderName());
                    html.replace("%AGIT_SIZE%", "30 ");
                    html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getClanHall(ItemId).getLease()));
                    html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getClanHall(ItemId).getLocation());
                    html.replace("%objectId%", String.valueOf(getObjectId()));
                    player.sendPacket(html);
                    return;
                }
            }
            else if (actualCommand.equalsIgnoreCase("cancelBid"))
            {
                int bid = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()).getBidders().get(player.getClanId()).getBid();
                String filename = "data/html/auction/AgitBidCancel.htm";
                NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                html.replace("%AGIT_BID%", String.valueOf(bid));
                html.replace("%AGIT_BID_REMAIN%", String.valueOf((bid*0.9)));
                html.replace("%objectId%", String.valueOf(getObjectId()));
                player.sendPacket(html);
                return;
            }
            else if (actualCommand.equalsIgnoreCase("doCancelBid"))
            {
                if (AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()) != null)
                {
                    AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()).cancelBid(player.getClanId());
                    player.sendMessage("You have succesfully canceled your bidding at the auction");
                }
                return;
            }
            else if (actualCommand.equalsIgnoreCase("cancelAuction"))
            {
                if (!((player.getClanPrivileges() & L2Clan.CP_CH_AUCTION) == L2Clan.CP_CH_AUCTION))
                {
                    player.sendMessage("You don't have the right privilleges to do this");
                    return;
                }
                String filename = "data/html/auction/AgitSaleCancel.htm";
                NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                html.replace("%AGIT_DEPOSIT%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
                html.replace("%objectId%", String.valueOf(getObjectId()));
                player.sendPacket(html);
                return;
            }
            else if (actualCommand.equalsIgnoreCase("doCancelAuction"))
            {
                if (AuctionManager.getInstance().getAuction(player.getClan().getHasHideout()) != null)
                {
                    AuctionManager.getInstance().getAuction(player.getClan().getHasHideout()).cancelAuction();
                    player.sendMessage("Your auction has been canceled");
                }
                return;
            }
            else if (actualCommand.equalsIgnoreCase("sale2"))
            {
                String filename = "data/html/auction/AgitSale2.htm";
                NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                html.replace("%AGIT_LAST_PRICE%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
                html.replace("%objectId%", String.valueOf(getObjectId()));
                player.sendPacket(html);
                return;
            }
            else if (actualCommand.equalsIgnoreCase("sale"))
            {
                if (!((player.getClanPrivileges() & L2Clan.CP_CH_AUCTION) == L2Clan.CP_CH_AUCTION))
                {
                    player.sendMessage("You don't have the right privilleges to do this");
                    return;
                }
                String filename = "data/html/auction/AgitSale1.htm";
                NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                html.replace("%AGIT_DEPOSIT%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
                html.replace("%AGIT_PLEDGE_ADENA%", String.valueOf(player.getClan().getWarehouse().getAdena()));
                html.replace("%objectId%", String.valueOf(getObjectId()));
                player.sendPacket(html);
                return;
            }
            else if (actualCommand.equalsIgnoreCase("rebid"))
            {
                if (!((player.getClanPrivileges() & L2Clan.CP_CH_AUCTION) == L2Clan.CP_CH_AUCTION))
                {
                    player.sendMessage("You don't have the right privilleges to do this");
                    return;
                }
                try
                {
                String filename = "data/html/auction/AgitBid2.htm";
                NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile(filename);
                Auction a = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
                html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
                html.replace("%AGIT_AUCTION_END_YY%", String.valueOf(a.getEndDate().get(Calendar.YEAR)));
                html.replace("%AGIT_AUCTION_END_MM%", String.valueOf(a.getEndDate().get(Calendar.MONTH)+1));
                html.replace("%AGIT_AUCTION_END_DD%", String.valueOf(a.getEndDate().get(Calendar.DAY_OF_MONTH)));
                html.replace("%AGIT_AUCTION_END_HH%", String.valueOf(a.getEndDate().get(Calendar.HOUR_OF_DAY)));
                html.replace("npc_%objectId%_bid1", "npc_"+getObjectId()+"_bid1 "+a.getId());
                player.sendPacket(html);
                }
                catch (Exception e)
                {
                    player.sendMessage("Invalid auction!");
                }
                return;
            }
            else if (actualCommand.equalsIgnoreCase("location"))
            {
                NpcHtmlMessage html = new NpcHtmlMessage(1);
                html.setFile("data/html/auction/location.htm");
                html.replace("%location%", MapRegionTable.getInstance().getClosestTownName(player));
                html.replace("%LOCATION%", getPictureName(player));
                player.sendPacket(html);
                return;
            }
            else if (actualCommand.equalsIgnoreCase("start"))
            {
                showMessageWindow(player);
                return;
            }
        }
        super.onBypassFeedback(player, command);
    }

    public void showMessageWindow(L2PcInstance player)
    {
        String filename = "data/html/auction/auction-no.htm";

        int condition = validateCondition(player);
        if (condition == Cond_Busy_Because_Of_Siege) filename = "data/html/auction/auction-busy.htm"; // Busy because of siege
        else filename = "data/html/auction/auction.htm";

        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcId%", String.valueOf(getNpcId()));
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    }

    private int validateCondition(L2PcInstance player)
    {
        if (getCastle() != null && getCastle().getCastleId() > 0)
        {
            if (getCastle().getSiege().getIsInProgress()) return Cond_Busy_Because_Of_Siege; // Busy because of siege
            else return Cond_Regular;
        }

        return Cond_All_False;
    }
    private String getPictureName(L2PcInstance plyr)
    {
        int nearestTownId = MapRegionTable.getInstance().getMapRegion(plyr.getX(), plyr.getY());
        String nearestTown;
        
        switch (nearestTownId)
        {
            case 5: nearestTown = "GLUDIO"; break;
            case 6: nearestTown = "GLUDIN"; break;
            case 7: nearestTown = "DION"; break;
            case 8: nearestTown = "GIRAN"; break;
            case 14: nearestTown = "RUNE"; break;
            case 15: nearestTown = "GODDARD"; break;
            case 16: nearestTown = "SHUTTGART"; break;
            default: nearestTown = "ADEN"; break;
        }
        
        return nearestTown;
    }
}