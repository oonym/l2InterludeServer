/* This program is free software; you can redistribute it and/or modify */
package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.ConfirmDlg;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * @author evill33t
 * 
 */
public class Wedding implements IVoicedCommandHandler
{
    static final Log _log = LogFactory.getLog(Wedding.class);
    private static String[] _voicedCommands = { "divorce", "engage", "gotolove" };

    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#useUserCommand(int, net.sf.l2j.gameserver.model.L2PcInstance)
     */
    public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
    {
        if(command.startsWith("engage"))
            return Engage(activeChar);
        else if(command.startsWith("divorce"))
            return Divorce(activeChar);
        else if(command.startsWith("gotolove"))
            return GoToLove(activeChar);
        return false;
    }
    
    public boolean Divorce(L2PcInstance activeChar)
    {
        if(activeChar.getPartnerId()==0)
            return false;

        int _partnerId = activeChar.getPartnerId();
        int _coupleId = activeChar.getCoupleId();
        int AdenaAmount = 0;
        
        if(activeChar.isMarried())
        {
            activeChar.sendMessage("You are now divorced."); 

            AdenaAmount = (activeChar.getAdena()/100)*Config.L2JMOD_WEDDING_DIVORCE_COSTS;
            activeChar.getInventory().reduceAdena("Wedding", AdenaAmount, activeChar, null);
            
        }
        else
            activeChar.sendMessage("You have broken up as a couple.");
       
        L2PcInstance partner;
        partner = (L2PcInstance)L2World.getInstance().findObject(_partnerId);
        
        if (partner != null)
        {
            partner.setPartnerId(0);
            if(partner.isMarried())
                partner.sendMessage("Your spouse has decided to divorce you.");
            else
                partner.sendMessage("Your fiance has decided to break the engagement with you.");

            // give adena
            if(AdenaAmount>0)
                partner.addAdena("WEDDING", AdenaAmount, null, false);
        }
        
        CoupleManager.getInstance().deleteCouple(_coupleId);
        return true;
    }

    public boolean Engage(L2PcInstance activeChar)
    {
        // check target
        if (activeChar.getTarget()==null)
        {
            activeChar.sendMessage("You have no one targeted.");
            return false;
        }
        
        // check if target is a l2pcinstance
        if (!(activeChar.getTarget() instanceof L2PcInstance))
        {
            activeChar.sendMessage("You can only ask another player to engage you.");

            return false;
        }
        
        // check if player is already engaged
        if (activeChar.getPartnerId()!=0)
        {
            activeChar.sendMessage("You are already engaged.");
            if(Config.L2JMOD_WEDDING_PUNISH_INFIDELITY)
            {
                activeChar.startAbnormalEffect((short)0x2000); // give player a Big Head
                // lets recycle the sevensigns debuffs
                int skillId;
    
                int skillLevel = 1;
                
                if (activeChar.getLevel() > 40)
                    skillLevel = 2;
                
                if(activeChar.isMageClass())
                    skillId = 4361;
                else
                    skillId = 4362;
                
                L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
                
                if (activeChar.getEffect(skill) == null)
                {
                    skill.getEffects(activeChar, activeChar);
                    SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
                    sm.addSkillName(skillId);
                    activeChar.sendPacket(sm);
                }
            }   
            return false;
        }

        L2PcInstance ptarget = (L2PcInstance)activeChar.getTarget();
        
        // check if player target himself
        if(ptarget.getObjectId()==activeChar.getObjectId())
        {
            activeChar.sendMessage("Is there something wrong with you, are you trying to go out with youself?");
            return false;
        }

        if(ptarget.isMarried())
        {
            activeChar.sendMessage("Player already married.");
            return false;
        }

        if(ptarget.isEngageRequest())
        {
            activeChar.sendMessage("Player already asked by someone else.");
            return false;
        }

        if(ptarget.getPartnerId()!=0)
        {
            activeChar.sendMessage("Player already engaged with someone else.");
            return false;
        }
        
        
        if (ptarget.getAppearance().getSex()==activeChar.getAppearance().getSex() && !Config.L2JMOD_WEDDING_SAMESEX)
        {
            activeChar.sendMessage("Gay marriage is not allowed on this server!");
            return false;
        }

        // check if target has player on friendlist
        boolean FoundOnFriendList = false;
        int objectId;
        java.sql.Connection con = null;
        try 
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement;
            statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id=?");
            statement.setInt(1, ptarget.getObjectId());
            ResultSet rset = statement.executeQuery();
        
            while (rset.next())
            {
                objectId = rset.getInt("friend_id");
                if(objectId == activeChar.getObjectId())
                    FoundOnFriendList = true;
            }
        } 
        catch (Exception e) 
        {
            _log.warn("could not read friend data:"+e);
        } 
        finally 
        {
            try {con.close();} catch (Exception e){}
        }
        
        if(!FoundOnFriendList)
        {
            activeChar.sendMessage("The player you want to ask is not on your friends list, you must first be on each others friends list before you choose to engage.");
            return false;
        }
        
        ptarget.setEngageRequest(true, activeChar.getObjectId());        
        //ptarget.sendMessage("Player "+activeChar.getName()+" wants to engage with you.");
        ptarget.sendPacket(new ConfirmDlg(614,activeChar.getName()+" asking you to engage. Do you want to start a new relationship?"));
        return true;
    }
    
    public boolean GoToLove(L2PcInstance activeChar)
    {
        if(!activeChar.isMarried())
        {
            activeChar.sendMessage("You're not married."); 
            return false;
        }
        
        if(activeChar.getPartnerId()==0)
        {
            activeChar.sendMessage("Couldn't find your fiance in the Database - Inform a Gamemaster.");
            _log.error("Married but couldn't find parter for "+activeChar.getName());
            return false;
        }

        L2PcInstance partner;
        partner = (L2PcInstance)L2World.getInstance().findObject(activeChar.getPartnerId());
        if(partner == null)
        {
            activeChar.sendMessage("Your partner is not online.");
            return false;
        }
        else if(partner.isInJail())
        {
            activeChar.sendMessage("Your partner is in Jail.");
            return false;
        }
        else if(partner.isInOlympiadMode())
        {
            activeChar.sendMessage("Your partner is in the Olympiad now.");
            return false;
        }
        else if(partner.atEvent)
        {
            activeChar.sendMessage("Your partner is in an event.");
            return false;
        }
        else  if (partner.isInDuel())
        {
            activeChar.sendMessage("Your partner is in a duel.");
            return false;
        }
        else if (partner.isFestivalParticipant()) 
        {
            activeChar.sendMessage("Your partner is in a festival.");
            return false;
        }
        else if (partner.isInParty() && partner.getParty().isInDimensionalRift()) 
        {
            activeChar.sendMessage("Your partner is in dimensional rift.");
            return false;
        }
        else if (partner.inObserverMode())
        {
        	activeChar.sendMessage("Your partner is in the observation.");
        }
        else if(partner.getClan() != null  
        		&& CastleManager.getInstance().getCastleByOwner(partner.getClan()) != null  
        		&& CastleManager.getInstance().getCastleByOwner(partner.getClan()).getSiege().getIsInProgress()) 
        { 
        	activeChar.sendMessage("Your partner is in siege, you can't go to your partner."); 
        	return false; 
        } 
        
        else if(activeChar.isInJail())
        {
            activeChar.sendMessage("You are in Jail!");
            return false;
        }
        else if(activeChar.isInOlympiadMode())
        {
            activeChar.sendMessage("You are in the Olympiad now.");
            return false;
        }
        else if(activeChar.atEvent)
        {
            activeChar.sendMessage("You are in an event.");
            return false;
        }
        else  if (activeChar.isInDuel())
        {
            activeChar.sendMessage("You are in a duel!");
            return false;
        }
        else if (activeChar.inObserverMode())
        {
        	activeChar.sendMessage("You are in the observation.");
        }
        else if(activeChar.getClan() != null  
        		&& CastleManager.getInstance().getCastleByOwner(activeChar.getClan()) != null  
        		&& CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).getSiege().getIsInProgress()) 
        { 
        	activeChar.sendMessage("You are in siege, you can't go to your partner."); 
        	return false; 
        } 
        else if (activeChar.isFestivalParticipant()) 
        {
            activeChar.sendMessage("You are in a festival.");
            return false;
        }
        else if (activeChar.isInParty() && activeChar.getParty().isInDimensionalRift()) 
        {
            activeChar.sendMessage("You are in the dimensional rift.");
            return false;
        }
        // Thanks nbd
        else if (!TvTEvent.onEscapeUse(activeChar.getName()))
        {
        	activeChar.sendPacket(new ActionFailed());
        	return false;
        }
        
        
        int teleportTimer = Config.L2JMOD_WEDDING_TELEPORT_DURATION*1000;
        
        activeChar.sendMessage("After " + teleportTimer/60000 + " min. you will be teleported to your fiance.");
        activeChar.getInventory().reduceAdena("Wedding", Config.L2JMOD_WEDDING_TELEPORT_PRICE, activeChar, null);
        
        activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
        //SoE Animation section
        activeChar.setTarget(activeChar);
        activeChar.disableAllSkills();

        MagicSkillUser msk = new MagicSkillUser(activeChar, 1050, 1, teleportTimer, 0);
        Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 810000/*900*/);
        SetupGauge sg = new SetupGauge(0, teleportTimer);
        activeChar.sendPacket(sg);
        //End SoE Animation section

        EscapeFinalizer ef = new EscapeFinalizer(activeChar,partner.getX(),partner.getY(),partner.getZ(),partner.isIn7sDungeon());
        // continue execution later
        activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, teleportTimer));
        activeChar.setSkillCastEndTime(10+GameTimeController.getGameTicks()+teleportTimer/GameTimeController.MILLIS_IN_TICK);
        
        return true;
    }

    static class EscapeFinalizer implements Runnable
    {
        private L2PcInstance _activeChar;
        private int _partnerx;
        private int _partnery;
        private int _partnerz;
        private boolean _to7sDungeon;
        
        EscapeFinalizer(L2PcInstance activeChar, int x, int y, int z, boolean to7sDungeon)
        {
            _activeChar = activeChar;
            _partnerx = x;
            _partnery = y;
            _partnerz = z;
            _to7sDungeon = to7sDungeon;
        }
        
        public void run()
        {
            if (_activeChar.isDead()) 
                return; 
            
            _activeChar.setIsIn7sDungeon(_to7sDungeon);
            
            _activeChar.enableAllSkills();
            
            try 
            {
                _activeChar.teleToLocation(_partnerx, _partnery, _partnerz);
            } catch (Throwable e) { _log.error(e.getMessage(),e); }
        }
    }
    
    /* (non-Javadoc)
     * @see net.sf.l2j.gameserver.handler.IUserCommandHandler#getUserCommandList()
     */
    public String[] getVoicedCommandList()
    {
        return _voicedCommands;
    }
}
