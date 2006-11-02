package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.instancemanager.AuctionManager;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2AuctioneerInstance extends L2FolkInstance
{
    //private static Logger _log = Logger.getLogger(L2AuctioneerInstance.class.getName());

    private static int Cond_All_False = 0;
    private static int Cond_Busy_Because_Of_Siege = 1;

    public L2AuctioneerInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void onAction(L2PcInstance player)
    {
        player.sendPacket(new ActionFailed());
        player.setTarget(this);
        player.sendPacket(new MyTargetSelected(getObjectId(), -15));

        if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
            showMessageWindow(player);
    }

    public void onBypassFeedback(L2PcInstance player, String command)
    {
        if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false)) return;

        player.sendPacket(new ActionFailed());

        int condition = validateCondition(player);
        if (condition <= Cond_All_False) return;

        if (condition == Cond_Busy_Because_Of_Siege) return;
        else
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
                // TODO: Code to auction item
                return;
            }
            else if (actualCommand.equalsIgnoreCase("bidding"))
            {
                if (val == "") return;

                try
                {
                    // TODO: Code to show bidding window
                    //int auctionId = Integer.parseInt(val);
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
            else if (actualCommand.equalsIgnoreCase("list"))
            {
                // TODO: Code list auction item
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

        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
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
        }

        return Cond_All_False;
    }
}