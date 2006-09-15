package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class RequestStartPledgeWar extends ClientBasePacket
{
    private static final String _C__4D_REQUESTSTARTPLEDGEWAR = "[C] 4D RequestStartPledgewar";
    private static Logger _log = Logger.getLogger(RequestStartPledgeWar.class.getName());

    String _pledgeName;
    L2Clan _clan;
    L2PcInstance player;

    public RequestStartPledgeWar(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _pledgeName = readS();
    }

    void runImpl()
    {
        player = getClient().getActiveChar();
        if (player == null) return;

        _clan = getClient().getActiveChar().getClan();
        if (_clan == null) return;

        if (_clan.getLevel() < 3 || _clan.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR)
        {
            SystemMessage sm = new SystemMessage(1564);
            player.sendPacket(sm);
            player.sendPacket(new ActionFailed());
            return;
        }
        else if (!player.isClanLeader())
        {
            player.sendMessage("You can't declare war. You are not clan leader.");
            player.sendPacket(new ActionFailed());
            return;
        }

        L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
        if (clan == null)
        {
            player.sendMessage("No such clan.");
            player.sendPacket(new ActionFailed());
            return;
        }
        else if (_clan.getAllyId() == clan.getAllyId() && _clan.getAllyId() != 0)
        {
            SystemMessage sm = new SystemMessage(1569);
            player.sendPacket(sm);
            player.sendPacket(new ActionFailed());
            return;
        }
        //else if(clan.getLevel() < 3)
        else if (clan.getLevel() < 3 || clan.getMembersCount() < Config.ALT_CLAN_MEMBERS_FOR_WAR)
        {
            SystemMessage sm = new SystemMessage(1564);
            player.sendPacket(sm);
            player.sendPacket(new ActionFailed());
            return;
        }

        _log.warning("RequestStartPledgeWar, leader: " + clan.getLeaderName() + " clan: "
            + _clan.getName());

        //        L2PcInstance leader = L2World.getInstance().getPlayer(clan.getLeaderName());

        //        if(leader == null)
        //            return;

        //        if(leader != null && leader.isOnline() == 0)
        //        {
        //            player.sendMessage("Clan leader isn't online.");
        //            player.sendPacket(new ActionFailed());
        //            return;                        
        //        }

        //        if (leader.isProcessingRequest())
        //        {
        //            SystemMessage sm = new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
        //            sm.addString(leader.getName());
        //            player.sendPacket(sm);
        //            return;
        //        } 

        //        if (leader.isTransactionInProgress())
        //        {
        //            SystemMessage sm = new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
        //            sm.addString(leader.getName());
        //            player.sendPacket(sm);
        //            return;
        //        } 

        //        leader.setTransactionRequester(player);
        //        player.setTransactionRequester(leader);
        //        leader.sendPacket(new StartPledgeWar(_clan.getName(),player.getName()));
        ClanTable.getInstance().storeclanswars(player.getClanId(), clan.getClanId());
    }

    public String getType()
    {
        return _C__4D_REQUESTSTARTPLEDGEWAR;
    }
}