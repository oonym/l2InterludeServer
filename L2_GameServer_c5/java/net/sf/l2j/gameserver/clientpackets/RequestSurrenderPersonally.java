package net.sf.l2j.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class RequestSurrenderPersonally extends ClientBasePacket
{
    private static final String _C__69_REQUESTSURRENDERPERSONALLY = "[C] 69 RequestSurrenderPersonally";
    private static Logger _log = Logger.getLogger(RequestSurrenderPledgeWar.class.getName());

    String _pledgeName;
    L2Clan _clan;
    L2PcInstance player;
    
    public RequestSurrenderPersonally(ByteBuffer buf, ClientThread client)
    {
        super(buf, client);
        _pledgeName  = readS();
    }

    void runImpl()
    {
        player = getClient().getActiveChar();
	if (player == null)
	    return;
        _log.info("RequestSurrenderPersonally by "+getClient().getActiveChar().getName()+" with "+_pledgeName);
        _clan = getClient().getActiveChar().getClan();
        L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
        
        if(_clan == null)
            return;
        
        if(clan == null)
        {
            player.sendMessage("No such clan.");
            player.sendPacket(new ActionFailed());
            return;                        
        }

        if(!_clan.isAtWarWith(clan.getClanId()) || player.getWantsPeace() == 1)
        {
            player.sendMessage("You aren't at war with this clan.");
            player.sendPacket(new ActionFailed());
            return;            
        }
        
        player.setWantsPeace(1);
        player.deathPenalty(false);
        SystemMessage msg = new SystemMessage(SystemMessage.YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN);
        msg.addString(_pledgeName);
        player.sendPacket(msg);
        msg = null;
        ClanTable.getInstance().CheckSurrender(_clan, clan);
    }
    
    public String getType()
    {
        return _C__69_REQUESTSURRENDERPERSONALLY;
    }
}