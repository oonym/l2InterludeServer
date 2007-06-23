package net.sf.l2j.gameserver.clientpackets;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public final class RequestSurrenderPersonally extends L2GameClientPacket
{
    private static final String _C__69_REQUESTSURRENDERPERSONALLY = "[C] 69 RequestSurrenderPersonally";
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
        _log.info("RequestSurrenderPersonally by "+getClient().getActiveChar().getName()+" with "+_pledgeName);
        _clan = getClient().getActiveChar().getClan();
        L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
        
        if(_clan == null)
            return;
        
        if(clan == null)
        {
        	_activeChar.sendMessage("No such clan.");
        	_activeChar.sendPacket(new ActionFailed());
            return;                        
        }

        if(!_clan.isAtWarWith(clan.getClanId()) || _activeChar.getWantsPeace() == 1)
        {
        	_activeChar.sendMessage("You aren't at war with this clan.");
        	_activeChar.sendPacket(new ActionFailed());
            return;            
        }
        
        _activeChar.setWantsPeace(1);
        _activeChar.deathPenalty(false);
        SystemMessage msg = new SystemMessage(SystemMessage.YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN);
        msg.addString(_pledgeName);
        _activeChar.sendPacket(msg);
        msg = null;
        ClanTable.getInstance().checkSurrender(_clan, clan);
    }
    
    public String getType()
    {
        return _C__69_REQUESTSURRENDERPERSONALLY;
    }
}