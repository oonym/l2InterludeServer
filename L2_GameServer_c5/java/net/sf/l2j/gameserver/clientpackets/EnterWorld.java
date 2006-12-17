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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import net.sf.l2j.Base64;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ClientThread;
import net.sf.l2j.gameserver.CrownTable;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.MapRegionTable;
import net.sf.l2j.gameserver.Olympiad;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.TaskPriority;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.AdminCommandHandler;
import net.sf.l2j.gameserver.instancemanager.PetitionManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.model.entity.L2Event;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.serverpackets.Die;
import net.sf.l2j.gameserver.serverpackets.ExStorageMaxCount;
import net.sf.l2j.gameserver.serverpackets.FriendList;
import net.sf.l2j.gameserver.serverpackets.GameGuardQuery;
import net.sf.l2j.gameserver.serverpackets.HennaInfo;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.serverpackets.PledgeStatusChanged;
import net.sf.l2j.gameserver.serverpackets.QuestList;
import net.sf.l2j.gameserver.serverpackets.ShortCutInit;
import net.sf.l2j.gameserver.serverpackets.SignsSky;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;
/**
 * Enter World Packet Handler<p>
 * <p>
 * 0000: 03 <p>
 * packet format rev656 cbdddd  
 * <p>
 * 
 * @version $Revision: 1.16.2.1.2.7 $ $Date: 2005/03/29 23:15:33 $
 */
public class EnterWorld extends ClientBasePacket
{
	private static final String _C__03_ENTERWORLD = "[C] 03 EnterWorld";
	private static Logger _log = Logger.getLogger(EnterWorld.class.getName());
    
	public TaskPriority getPriority() { return TaskPriority.PR_URGENT; }
	
	/**
	 * @param decrypt
	 */
	public EnterWorld(ByteBuffer buf, ClientThread client)
	{
		super(buf, client);
		// this is just a trigger packet. it has no content
	}

	void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			_log.warning("EnterWorld failed! activeChar is null...");
		    return;
		}
		
		if (L2World.getInstance().findObject(activeChar.getObjectId()) != null)
		{
			if(Config.DEBUG)
				_log.warning("User already exist in OID map! User "+activeChar.getName()+" is character clone");
			//activeChar.closeNetConnection();
		}
        
        if (activeChar.isGM())
        {
        	if (Config.GM_STARTUP_INVULNERABLE
        			&& (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_GODMODE
        			  || Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_invul")))
        		activeChar.setIsInvul(true);
        	
            if (Config.GM_STARTUP_INVISIBLE 
                    && (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_GODMODE
                      || Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_invisible")))
                activeChar.setInvisible();

            if (Config.GM_STARTUP_SILENCE 
                    && (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_MENU
                      || Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_silence")))
                activeChar.setMessageRefusal(true);
            
            if (Config.GM_STARTUP_AUTO_LIST 
                    && (!Config.ALT_PRIVILEGES_ADMIN && activeChar.getAccessLevel() >= Config.GM_MENU
                      || Config.ALT_PRIVILEGES_ADMIN && AdminCommandHandler.getInstance().checkPrivileges(activeChar, "admin_gmliston")))
            	GmListTable.getInstance().addGm(activeChar);
            
            if (Config.GM_NAME_COLOR_ENABLED)
            {
                if (activeChar.getAccessLevel() >= 100)
                    activeChar.setNameColor(Config.ADMIN_NAME_COLOR);
                else if (activeChar.getAccessLevel() >= 75)
                    activeChar.setNameColor(Config.GM_NAME_COLOR);
            }
        }
        
        if (Config.PLAYER_SPAWN_PROTECTION > 0)
            activeChar.setProtection(true);
        
		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		
		if (L2Event.active && L2Event.connectionLossData.containsKey(activeChar.getName()) && L2Event.isOnEvent(activeChar))
            L2Event.restoreChar(activeChar);
        else if (L2Event.connectionLossData.containsKey(activeChar.getName()))
            L2Event.restoreAndTeleChar(activeChar);

		if (SevenSigns.getInstance().isSealValidationPeriod())
			sendPacket(new SignsSky());
		
        if (Config.STORE_SKILL_COOLTIME)
            activeChar.restoreEffects();
        
        if (activeChar.getAllEffects() != null)
        {
            for (L2Effect e : activeChar.getAllEffects())
            {
                if (e.getEffectType() == L2Effect.EffectType.HEAL_OVER_TIME)
                {
                    activeChar.stopEffects(L2Effect.EffectType.HEAL_OVER_TIME);
                    activeChar.removeEffect(e);
                }
                
                if (e.getEffectType() == L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME)
                {
                    activeChar.stopEffects(L2Effect.EffectType.COMBAT_POINT_HEAL_OVER_TIME);
                    activeChar.removeEffect(e);
                }
            }
        }
        
        //Expand Skill		
        ExStorageMaxCount esmc = new ExStorageMaxCount(activeChar);  
        activeChar.sendPacket(esmc);        
        
        activeChar.getMacroses().sendUpdate();
        
        sendPacket(new ItemList(activeChar, false));

        sendPacket(new UserInfo(activeChar));

		sendPacket(new ShortCutInit(activeChar));

        sendPacket(new HennaInfo(activeChar));
        
        sendPacket(new FriendList(activeChar));
        
        SystemMessage sm = new SystemMessage(34);
        sendPacket(sm);
	    
        sm = new SystemMessage(SystemMessage.S1_S2);
        sm.addString(getText("V2VsY29tZSB0byBhIEwySiBTZXJ2ZXIsIGZvdW5kZWQgYnkgTDJDaGVmLg=="));

        sendPacket(sm);
        sm = new SystemMessage(SystemMessage.S1_S2);
        sm.addString(getText("RGV2ZWxvcGVkIGJ5IHRoZSBMMkogRGV2IFRlYW0gYXQgbDJqc2VydmVyLmNvbS4="));

        sendPacket(sm);

        if (Config.SERVER_VERSION != null)
        {
            sm = new SystemMessage(SystemMessage.S1_S2);
            sm.addString(getText("TDJKIFNlcnZlciBWZXJzaW9uOg==")+"      "+Config.SERVER_VERSION);
            sendPacket(sm);
        }
        
        if (Config.DATAPACK_VERSION != null)
        {
            sm = new SystemMessage(SystemMessage.S1_S2);
            sm.addString(getText("TDJKIERhdGFwYWNrIFZlcnNpb246")+"  "+Config.DATAPACK_VERSION);
            sendPacket(sm);
        }
        sm = null;
        
        sm = new SystemMessage(SystemMessage.S1_S2);
        sm.addString(getText("Q29weXJpZ2h0IDIwMDQtMjAwNg=="));
        sendPacket(sm);
        sm = new SystemMessage(SystemMessage.S1_S2);
        sm.addString(getText("V2VsY29tZSB0byA="));
        sm.addString(LoginServerThread.getInstance().getServerName());
        sendPacket(sm);
        
        SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
        Announcements.getInstance().showAnnouncements(activeChar);

		Quest.playerEnter(activeChar);
		activeChar.sendPacket(new QuestList());

		String serverNews = HtmCache.getInstance().getHtm("data/html/servnews.htm");
		
		if (serverNews != null)
		{
			NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
			htmlMsg.setHtml(serverNews);
			sendPacket(htmlMsg);
		}
		
		PetitionManager.getInstance().checkPetitionMessages(activeChar);
		
        // send user info again .. just like the real client
        //sendPacket(ui);

        if (activeChar.getClanId() != 0 && activeChar.getClan() != null)
        {
        	sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), activeChar));
        	sendPacket(new PledgeStatusChanged(activeChar.getClan()));
        }
	
		if (activeChar.isAlikeDead())
		{
			// no broadcast needed since the player will already spawn dead to others
			sendPacket(new Die(activeChar));
		}

		if (Config.ALLOW_WATER)
		    activeChar.checkWaterState();

        if (Hero.getInstance().getHeroes() != null &&
                Hero.getInstance().getHeroes().containsKey(activeChar.getObjectId()))
            activeChar.setHero(true);
        
        setPledgeClass(activeChar);

		//add char to online characters
		activeChar.setOnlineStatus(true);
		
        notifyFriends(activeChar);
		notifyClanMembers(activeChar);

		activeChar.onPlayerEnter();
        
        if (Olympiad.getInstance().playerInStadia(activeChar))
        {
            activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
            activeChar.sendMessage("You have been teleported to the nearest town due to you being in an Olympiad Stadia");
        }
        
        if(Config.GAMEGUARD_ENFORCE)
            activeChar.sendPacket(new GameGuardQuery());
	}
    
	/**
	 * @param activeChar
	 */
	private void notifyFriends(L2PcInstance cha)
	{
		java.sql.Connection con = null;
		
		try {
		    con = L2DatabaseFactory.getInstance().getConnection();
		    PreparedStatement statement;
		    statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id=?");
		    statement.setInt(1, cha.getObjectId());
		    ResultSet rset = statement.executeQuery();

		    L2PcInstance friend;
            int objectId;
            
            SystemMessage sm = new SystemMessage(SystemMessage.FRIEND_S1_HAS_LOGGED_IN);
            sm.addString(cha.getName());

            while (rset.next())
            {
                objectId = rset.getInt("friend_id");

                friend = (L2PcInstance)L2World.getInstance().findObject(objectId);

                if (friend != null) //friend logged in.
                {
                	friend.sendPacket(new FriendList(friend));
                    friend.sendPacket(sm);
                }
		    }
            sm = null;
        } 
		catch (Exception e) {
            _log.warning("could not restore friend data:"+e);
        } 
		finally {
            try {con.close();} catch (Exception e){}
        }
	}
    
	/**
	 * @param activeChar
	 */
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		L2Clan clan = activeChar.getClan();
		if (clan != null)
		{
			clan.getClanMember(activeChar.getName()).setPlayerInstance(activeChar);
			SystemMessage msg = new SystemMessage(SystemMessage.CLAN_MEMBER_S1_LOGGED_IN);
			msg.addString(activeChar.getName());

			L2PcInstance[] clanMembers = clan.getOnlineMembers(activeChar.getName());
            PledgeShowMemberListUpdate ps = new PledgeShowMemberListUpdate(activeChar);
			for (int i = 0; i < clanMembers.length; i++)
			{
				clanMembers[i].sendPacket(ps);
				clanMembers[i].sendPacket(msg);
			}
			ps = null;
			msg = null;
		}
	}

	/**
	 * @param string
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String getText(String string)
	{
		try {
			String result = new String(Base64.decode(string), "UTF-8"); 
			return result;
		} catch (UnsupportedEncodingException e) {
			// huh, UTF-8 is not supported? :)
			return null;
		}
	}
    
    /* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	public String getType()
	{
		return _C__03_ENTERWORLD;
	}
	
	/**
    * @param activeChar
    * @return
    */
	private void setPledgeClass(L2PcInstance activeChar)
	{
       int pledgeClass = 0;
       L2Clan clan = activeChar.getClan();
       if (clan != null)
       {
           switch (activeChar.getClan().getLevel())
           {
               case 4:
                   if (activeChar.isClanLeader())
                       pledgeClass = 3;
                   break;
               case 5:
                   if (activeChar.isClanLeader())
                       pledgeClass = 4;
                   else
                       pledgeClass = 2;
                   break;
               case 6:
                   switch (activeChar.getPledgeType())
                   {
                       case -1:
                         pledgeClass = 1;
                         break;
                       case 100:
                       case 200:
                           pledgeClass = 2;
                           break;
                       case 0:
                           if (activeChar.isClanLeader())
                               pledgeClass = 5;
                           else
                               switch (clan.getLeaderSubPledge(activeChar.getName()))
                               {
                                   case 100:
                                   case 200:
                                       pledgeClass = 4;
                                       break;
                                   case -1:
                                   default:
                                       pledgeClass = 3;
                                       break;
                               }
                           break;
                   }
                   break;
               case 7:
                   switch (activeChar.getPledgeType())
                   {
                       case -1:
                         pledgeClass = 1;
                         break;
                       case 100:
                       case 200:
                               pledgeClass = 3;
                           break;
                       case 1001:
                       case 1002:
                       case 2001:
                       case 2002:
                               pledgeClass = 2;
                           break;
                       case 0:
                           if (activeChar.isClanLeader())
                               pledgeClass = 7;
                           else
                               switch (clan.getLeaderSubPledge(activeChar.getName()))
                               {
                                   case 100:
                                   case 200:
                                       pledgeClass = 6;
                                       break;
                                   case 1001:
                                   case 1002:
                                   case 2001:
                                   case 2002:
                                       pledgeClass = 5;
                                       break;
                                   case -1:
                                   default:
                                       pledgeClass = 4;
                                       break;
                               }
                           break;
                   }
                   break;
               case 8:
                   switch (activeChar.getPledgeType())
                   {
                       case -1:
                         pledgeClass = 1;
                         break;
                       case 100:
                       case 200:
                               pledgeClass = 4;
                           break;
                       case 1001:
                       case 1002:
                       case 2001:
                       case 2002:
                               pledgeClass = 3;
                           break;
                       case 0:
                           if (activeChar.isClanLeader())
                               pledgeClass = 8;
                           else
                               switch (clan.getLeaderSubPledge(activeChar.getName()))
                               {
                                   case 100:
                                   case 200:
                                       pledgeClass = 7;
                                       break;
                                   case 1001:
                                   case 1002:
                                   case 2001:
                                   case 2002:
                                       pledgeClass = 6;
                                       break;
                                   case -1:
                                   default:
                                       pledgeClass = 5;
                                       break;
                               }
                           break;
                   }
                   break;
               default:
                   pledgeClass = 1;
               break;
           }
       }
       if (pledgeClass < 5 && activeChar.isNoble())
           pledgeClass = 5;
       else if (activeChar.isHero())
           pledgeClass = 8;
       activeChar.setPledgeClass(pledgeClass);
	}
 
}
