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
package net.sf.l2j.gameserver.clientpackets;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.MapRegionTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.Revive;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.IllegalPlayerAction;
import net.sf.l2j.gameserver.util.Util;


/**
 * This class ...
 * 
 * @version $Revision: 1.7.2.3.2.6 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestRestartPoint extends L2GameClientPacket
{
    private static final String _C__6d_REQUESTRESTARTPOINT = "[C] 6d RequestRestartPoint";
    private static Logger _log = Logger.getLogger(RequestRestartPoint.class.getName());	
    
    protected int     _requestedPointType;
    protected boolean _continuation;
    
    
    protected void readImpl()
    {
        _requestedPointType = readD();
    }
    
    class DeathTask implements Runnable
    {
        L2PcInstance activeChar;
        DeathTask (L2PcInstance _activeChar)
        {
            activeChar = _activeChar;
        }
        
        public void run()
        {
	    //_log.warning(activeChar.getName()+" request restartpoint "+requestedPointType);
            try
            {
                Location loc = null;
                if (activeChar.isInJail()) // to jail
                    loc = new Location(-114356, -249645, -2984);
                else if (_requestedPointType == 1) // to clanhall
                {
                    if (activeChar.getClan().getHasHideout() == 0)
                    {
                        //cheater
                        activeChar.sendMessage("Ohh Cheat dont work? You have a problem now!");
                        Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName()
                                                       + " used resapwn cheat!!!", IllegalPlayerAction.PUNISH_KICK);
                        return;
                    }
                    loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.ClanHall);
                    if (ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan())!= null && ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP)!= null)
                        activeChar.restoreExp(ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan()).getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl());
                }
                else if (_requestedPointType == 2) // to castle
                {
                    Boolean isInDefense = false;
                    Castle castle = CastleManager.getInstance().getCastle(activeChar);                	
                	if (castle != null && castle.getSiege().getIsInProgress())
                	{
                    	//siege in progress            	
                        if (castle.getSiege().checkIsDefender(activeChar.getClan()))
                        	isInDefense = true;
                    }
                    if (activeChar.getClan().getHasCastle() == 0 && !isInDefense)
                    {
                        //cheater
                        activeChar.sendMessage("Ohh Cheat dont work? You have a problem now!");
                        Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName()
                                                       + " used resapwn cheat!!!", IllegalPlayerAction.PUNISH_KICK);
                        return;
                    }
                    loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Castle);
                }                    
                else if (_requestedPointType == 3) // to siege HQ
                {
                    L2SiegeClan siegeClan = null;
                    Castle castle = CastleManager.getInstance().getCastle(activeChar);
                    if (castle != null && castle.getSiege().getIsInProgress())
                        siegeClan = castle.getSiege().getAttackerClan(activeChar.getClan());
                    if (siegeClan == null || siegeClan.getFlag().size() == 0)
                    {
                        //cheater
                        activeChar.sendMessage("Ohh Cheat dont work? You have a problem now!");
                        Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName()
                                                       + " used resapwn cheat!!!", IllegalPlayerAction.PUNISH_KICK);
                        return;
                    }
                    loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.SiegeFlag);
                }                    
                else if (_requestedPointType == 4 || // Fixed or
                        activeChar.isFestivalParticipant()) // Player is a festival participant
                {
                    if (!activeChar.isGM() && !activeChar.isFestivalParticipant())
                    {
                        //cheater
                        activeChar.sendMessage("Ohh Cheat dont work? You have a problem now!");
                        Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName()
                                                       + " used resapwn cheat!!!", IllegalPlayerAction.PUNISH_KICK);
                        return;
                    }
                    loc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ()); // spawn them where they died
                }                    
                else
                    loc = MapRegionTable.getInstance().getTeleToLocation(activeChar, MapRegionTable.TeleportWhereType.Town);

                //Teleport and revive
                activeChar.setIsPendingRevive(true);
                activeChar.teleToLocation(loc, true);
            } catch (Throwable e) {
                //_log.log(Level.SEVERE, "", e);
            }
        }
    }
    
    protected void runImpl()
    {
        L2PcInstance activeChar = getClient().getActiveChar();
        
        if (activeChar == null)
            return;
            //SystemMessage sm2 = new SystemMessage(SystemMessage.S1_S2);
	    //sm2.addString("type:"+requestedPointType);
	    //activeChar.sendPacket(sm2);
        
        if (activeChar.isFakeDeath())
        {
            activeChar.stopFakeDeath(null);
            activeChar.broadcastPacket(new Revive(activeChar));
            return;
        }
        else if(!activeChar.isAlikeDead())
        {
        	_log.warning("Living player ["+activeChar.getName()+"] called RestartPointPacket! Ban this player!");
        	return;
        }

        Castle castle = CastleManager.getInstance().getCastle(activeChar.getX(),activeChar.getY());
        if (castle != null && castle.getSiege().getIsInProgress())
        {
            //DeathFinalizer df = new DeathFinalizer(10000);
            SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
            if (activeChar.getClan() != null
                    && castle.getSiege().checkIsAttacker(activeChar.getClan()))
            {
                // Schedule respawn delay for attacker
            	ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(activeChar), castle.getSiege().getAttackerRespawnDelay());
                sm.addString("You will be re-spawned in " + castle.getSiege().getAttackerRespawnDelay()/1000 + " seconds");
                activeChar.sendPacket(sm);
            }
            else
            {
                // Schedule respawn delay for defender with penalty for CT lose
            	ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(activeChar), castle.getSiege().getDefenderRespawnDelay());
                sm.addString("You will be re-spawned in " + castle.getSiege().getDefenderRespawnDelay()/1000 + " seconds");
                activeChar.sendPacket(sm);
            }
            sm = null;
            return;
        }
        
        ThreadPoolManager.getInstance().scheduleGeneral(new DeathTask(activeChar), 1);
    }
    
    
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
     */
    public String getType()
    {
        return _C__6d_REQUESTRESTARTPOINT;
    }
}
