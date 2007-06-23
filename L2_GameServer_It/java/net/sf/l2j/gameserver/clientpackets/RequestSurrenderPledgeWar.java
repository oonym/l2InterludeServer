package net.sf.l2j.gameserver.clientpackets;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public final class RequestSurrenderPledgeWar extends L2GameClientPacket
{
    private static final String _C__51_REQUESTSURRENDERPLEDGEWAR = "[C] 51 RequestSurrenderPledgeWar";
    private static Logger _log = Logger.getLogger(RequestSurrenderPledgeWar.class.getName());

    private String _pledgeName;
    private L2Clan _clan;
    private L2PcInstance _activeChar;
    
    protected void readImpl()
    {
        _pledgeName  = readS();
    }

    protected void runImpl()
    {
    	_activeChar = getClient().getActiveChar();
		if (_activeChar == null)
		    return;
        _clan = _activeChar.getClan();
		if (_clan == null)
		    return;
        L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);

        if(clan == null)
        {
        	_activeChar.sendMessage("No such clan.");
        	_activeChar.sendPacket(new ActionFailed());
            return;                        
        }

        _log.info("RequestSurrenderPledgeWar by "+getClient().getActiveChar().getClan().getName()+" with "+_pledgeName);
        
        if(!_clan.isAtWarWith(clan.getClanId()))
        {
        	_activeChar.sendMessage("You aren't at war with this clan.");
        	_activeChar.sendPacket(new ActionFailed());
            return;            
        }
        
        
        SystemMessage msg = new SystemMessage(SystemMessage.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN);
        msg.addString(_pledgeName);
        _activeChar.sendPacket(msg);
        msg = null;
        _activeChar.deathPenalty(false);
        ClanTable.getInstance().deleteclanswars(_clan.getClanId(), clan.getClanId());
        /*L2PcInstance leader = L2World.getInstance().getPlayer(clan.getLeaderName());
        if(leader != null && leader.isOnline() == 0)
        {
            player.sendMessage("Clan leader isn't online.");
            player.sendPacket(new ActionFailed());
            return;                        
        }
        
        if (leader.isTransactionInProgress())
        {
            SystemMessage sm = new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
            sm.addString(leader.getName());
            player.sendPacket(sm);
            return;
        } 
        
        leader.setTransactionRequester(player);
        player.setTransactionRequester(leader);
        leader.sendPacket(new SurrenderPledgeWar(_clan.getName(),player.getName()));*/
    }
    
    public String getType()
    {
        return _C__51_REQUESTSURRENDERPLEDGEWAR;
    }
}