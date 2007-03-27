package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.BuyList;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public final class L2MercManagerInstance extends L2FolkInstance
{
    //private static Logger _log = Logger.getLogger(L2MercManagerInstance.class.getName());

    private static int Cond_All_False = 0;
    private static int Cond_Busy_Because_Of_Siege = 1;
    private static int Cond_Owner = 2;

    public L2MercManagerInstance(int objectId, L2NpcTemplate template)
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
        else if (condition == Cond_Owner)
        {
            StringTokenizer st = new StringTokenizer(command, " ");
            String actualCommand = st.nextToken(); // Get actual command

            String val = "";
            if (st.countTokens() >= 1)
            {
                val = st.nextToken();
            }

            if (actualCommand.equalsIgnoreCase("hire"))
            {
                if (val == "") return;

                showBuyWindow(player, Integer.parseInt(val));
                return;
            }
        }

        super.onBypassFeedback(player, command);
    }

    private void showBuyWindow(L2PcInstance player, int val)
    {
        player.tempInvetoryDisable();
        if (Config.DEBUG) _log.fine("Showing buylist");
        L2TradeList list = TradeController.getInstance().getBuyList(val);
        if ((list != null) && (list.getNpcId().equals(String.valueOf(getNpcId()))))
        {
            BuyList bl = new BuyList(list, player.getAdena(), 0);
            player.sendPacket(bl);
        }
        else
        {
            _log.warning("possible client hacker: " + player.getName()
                + " attempting to buy from GM shop! < Ban him!");
            _log.warning("buylist id:" + val);
        }
    }

    public void showMessageWindow(L2PcInstance player)
    {
        String filename = "data/html/mercmanager/mercmanager-no.htm";

        int condition = validateCondition(player);
        if (condition == Cond_Busy_Because_Of_Siege) filename = "data/html/mercmanager/mercmanager-busy.htm"; // Busy because of siege
        else if (condition == Cond_Owner) // Clan owns castle
            filename = "data/html/mercmanager/mercmanager.htm"; // Owner message window

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
            if (player.getClan() != null)
            {
                if (getCastle().getSiege().getIsInProgress()) return Cond_Busy_Because_Of_Siege; // Busy because of siege
                else if (getCastle().getOwnerId() == player.getClanId()) // Clan owns castle
                {
                    if ((player.getClanPrivileges() & L2Clan.CP_CS_MERCENARIES) == L2Clan.CP_CS_MERCENARIES) return Cond_Owner;
                }
            }
        }

        return Cond_All_False;
    }
}