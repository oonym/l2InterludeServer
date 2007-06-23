/* This program is free software; you can redistribute it and/or modify
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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.communitybbs.Manager.RegionBBSManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.CharInfo;
import net.sf.l2j.gameserver.serverpackets.Earthquake;
import net.sf.l2j.gameserver.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.serverpackets.NpcInfo;
import net.sf.l2j.gameserver.serverpackets.SocialAction;
import net.sf.l2j.gameserver.serverpackets.StopMove;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;

/**
* This class handles following admin commands:<br>
*   <li> invis/invisible/vis/visible = makes yourself invisible or visible
*   <li> earthquake = causes an earthquake of a given intensity and duration around you
*   <li> bighead/shrinkhead = changes head size
*   <li> gmspeed = temporary Super Haste effect.
*   <li> para/unpara = paralyze/remove paralysis from target
*   <li> para_all/unpara_all = same as para/unpara, affects the whole world.
*   <li> polyself/unpolyself = makes you look as a specified mob.
*   <li> changename = temporary change name
*   <li> clearteams/setteam_close/setteam = team related commands
*   <li> social/effect = forces an L2Character instance to broadcast social action and MSU packets.
*/
public class AdminEffects implements IAdminCommandHandler
{
   private static final String[] ADMIN_COMMANDS = { "admin_invis", "admin_invisible", "admin_vis",
                                              "admin_visible", "admin_earthquake", "admin_bighead", "admin_shrinkhead", "admin_gmspeed", 
                                              "admin_unpara_all", "admin_para_all", "admin_unpara", "admin_para", "admin_polyself",
                                              "admin_unpolyself", "admin_changename", "admin_clearteams", "admin_setteam_close", "admin_setteam",
                                              "admin_social", "admin_effect"};

   private static final int REQUIRED_LEVEL = Config.GM_GODMODE;

   public boolean useAdminCommand(String command, L2PcInstance activeChar)
   {
       if (!Config.ALT_PRIVILEGES_ADMIN)
       {
           if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
               return false;
       }
       
       if (command.equals("admin_invis")||command.equals("admin_invisible"))
       {
         activeChar.getAppearance().setInvisible();
         activeChar.broadcastUserInfo();
         
         activeChar.decayMe();
         activeChar.spawnMe();
         
         RegionBBSManager.getInstance().changeCommunityBoard();
       }
       else if (command.equals("admin_vis")||command.equals("admin_visible"))
       {
         activeChar.getAppearance().setVisible();
         activeChar.broadcastUserInfo();

         RegionBBSManager.getInstance().changeCommunityBoard();
       }
       else if (command.startsWith("admin_earthquake"))
       {
           try
           {
               String val = command.substring(17);
               StringTokenizer st = new StringTokenizer(val);
               String val1 = st.nextToken();
               int intensity = Integer.parseInt(val1);
               String val2 = st.nextToken();
               int duration = Integer.parseInt(val2);
               Earthquake eq = new Earthquake(activeChar.getX(), activeChar.getY(), activeChar.getZ(), intensity, duration);
               activeChar.broadcastPacket(eq);
           }
           catch (Exception e)
           {
           }
       }
       else if (command.equals("admin_para"))
       {
           String type = "1";
           StringTokenizer st = new StringTokenizer(command);
           try
           {
               st.nextToken();
               type = st.nextToken();
           }
           catch(Exception e){}
           try
           {
               L2Object target = activeChar.getTarget();
               L2Character player = null;
               if (target instanceof L2Character)
               {
                   player = (L2Character)target;
                   if (type.equals("1"))
                       player.startAbnormalEffect((short)0x0400);
                   else
                       player.startAbnormalEffect((short)0x0800);
                   player.setIsParalyzed(true);
                   StopMove sm = new StopMove(player);
                   player.sendPacket(sm);
                   player.broadcastPacket(sm);
               }
           }
           catch (Exception e)
           {
           }
       }
       else if (command.equals("admin_unpara"))
       {
           try
           {
               L2Object target = activeChar.getTarget();
               L2Character player = null;
               if (target instanceof L2Character)
               {
                   player = (L2Character)target;
                   player.stopAbnormalEffect((short)0x0400);
                   player.setIsParalyzed(false);
               }
           }
           catch (Exception e)
           {
           }
       }
       else if (command.startsWith("admin_para_all"))
       {
           try
           {
               for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
               {
                   if (!player.isGM())
                   {
                       player.startAbnormalEffect((short)0x0400);
                       player.setIsParalyzed(true);

                       StopMove sm = new StopMove(player);
                       player.sendPacket(sm);
                       player.broadcastPacket(sm);
                   }
               }
           }
           catch (Exception e)
           {
           }
       }
       else if (command.startsWith("admin_unpara_all"))
       {
           try
           {
               for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
               {
                   player.stopAbnormalEffect((short)0x0400);
                   player.setIsParalyzed(false);
               }
           }
           catch (Exception e)
           {
           }
       }
       else if (command.startsWith("admin_bighead"))
       {
           try
           {   
               L2Object target = activeChar.getTarget();
               L2Character player = null;
               if (target instanceof L2Character) 
               {
                   player = (L2Character)target;
                   player.startAbnormalEffect((short)0x2000);
                }
           }
           catch (Exception e)
           {
           }
       }
       else if (command.startsWith("admin_shrinkhead"))
       {
           try
           {
               L2Object target = activeChar.getTarget();
               L2Character player = null;
               if (target instanceof L2Character) 
               {
                   player = (L2Character)target;
                   player.stopAbnormalEffect((short)0x2000);
               }
           }
           catch (Exception e)
           {
           }
       }
       else if (command.startsWith("admin_gmspeed"))
       {
           int val;
           try
           {
               val = Integer.parseInt(command.substring(14));
               boolean sendMessage = activeChar.getEffect(7029) != null;

               activeChar.stopEffect(7029);
               if (val == 0 && sendMessage)
               {
                   SystemMessage sm = new SystemMessage(SystemMessage.EFFECT_S1_DISAPPEARED);
                   sm.addSkillName(7029);
                   activeChar.sendPacket(sm);
                }
                else if ((val >= 1) && (val <= 4))
                {
                    L2Skill gmSpeedSkill = SkillTable.getInstance().getInfo(7029, val);
                    activeChar.doCast(gmSpeedSkill);
                }
           }
           catch (Exception e)
           {
               SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
               sm.addString("Use //gmspeed value = [0...4].");
              activeChar.sendPacket(sm);
           }
           finally 
           {
               activeChar.updateEffectIcons();
           }
       }
       else if (command.startsWith("admin_polyself"))
       {
           StringTokenizer st = new StringTokenizer(command);
           try
           {
               st.nextToken();
               String id = st.nextToken();
               activeChar.getPoly().setPolyInfo("npc", id);
               activeChar.teleToLocation(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false);
               CharInfo info1 = new CharInfo(activeChar);
               activeChar.broadcastPacket(info1);
               UserInfo info2 = new UserInfo(activeChar);
               activeChar.sendPacket(info2);
           }
           catch(Exception e)
           {
           }
       }
       else if (command.startsWith("admin_unpolyself"))
       {
           activeChar.getPoly().setPolyInfo(null, "1");
           activeChar.decayMe();
           activeChar.spawnMe(activeChar.getX(),activeChar.getY(),activeChar.getZ());
           CharInfo info1 = new CharInfo(activeChar);
           activeChar.broadcastPacket(info1);
           UserInfo info2 = new UserInfo(activeChar);
           activeChar.sendPacket(info2);
       }
       else if (command.startsWith("admin_changename"))
       {
           try
           {
               String name = command.substring(17);
               String oldName = "null";
               try
               {
                   L2Object target = activeChar.getTarget();
                   L2Character player = null;
                   if (target instanceof L2Character)
                   {
                       player = (L2Character)target;
                       oldName = player.getName();
                   }
                   else if (target == null)
                   {
                       player = activeChar;
                       oldName = activeChar.getName();
                    }
                    if (player instanceof L2PcInstance)
                       L2World.getInstance().removeFromAllPlayers((L2PcInstance)player);
                    player.setName(name);
                    if (player instanceof L2PcInstance)
                       L2World.getInstance().addVisibleObject(player, null, null);
                    if (player instanceof L2PcInstance)
                    {
                        CharInfo info1 = new CharInfo((L2PcInstance)player);
                        player.broadcastPacket(info1);
                        UserInfo info2 = new UserInfo((L2PcInstance)player);
                        player.sendPacket(info2);
                     }
                     else if(player instanceof L2NpcInstance)
                     {
                         NpcInfo info1 = new NpcInfo((L2NpcInstance)player, null);
                         player.broadcastPacket(info1);
                     }
                     SystemMessage smA = new SystemMessage(SystemMessage.S1_S2);
                     smA.addString("Changed name from "+ oldName +" to "+ name +".");    
                     activeChar.sendPacket(smA);
               }
               catch (Exception e)
               {
               }
           }
           catch (StringIndexOutOfBoundsException e)
           {
           }
       }
       else if (command.equals("admin_clear_teams"))
       {
           try
           {
               for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
               {
                   player.setTeam(0);
                   player.broadcastUserInfo();
               }
           }
           catch (Exception e)
           {
           }
       }
       else if (command.startsWith("admin_setteam_close"))
       {
           String val = command.substring(20);
           int teamVal = Integer.parseInt(val);
           try
           {
               for (L2PcInstance player : activeChar.getKnownList().getKnownPlayers().values())
               {
                   if (activeChar.isInsideRadius(player, 400, false, true))
                   {
                       player.setTeam(0);
                       if (teamVal != 0)
                       {
                           SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                           sm.addString("You have joined team " + teamVal);
                           player.sendPacket(sm);
                       }
                       player.broadcastUserInfo();
                   }
               }
           }
           catch (Exception e)
           {
           }
       }
       else if (command.startsWith("admin_setteam"))
       {
           String val = command.substring(14);
           int teamVal = Integer.parseInt(val);
           L2Object target = activeChar.getTarget();
           L2PcInstance player = null;
           if (target instanceof L2PcInstance)
               player = (L2PcInstance)target;
           else
               return false;
           player.setTeam(teamVal);
           if (teamVal != 0)
           {
               SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
               sm.addString("You have joined team " + teamVal);
               player.sendPacket(sm);
           }
           player.broadcastUserInfo();
       }
       else if (command.startsWith("admin_social"))
       {
           try
           {
                String cmd = command.substring(13);
                StringTokenizer st = new StringTokenizer(cmd);
                String target=null;
                L2Object obj = activeChar.getTarget();
                if (st.countTokens() == 2)
                {
                    int social = Integer.parseInt(st.nextToken());
                    target = st.nextToken();
                    if (target != null)
                    {
                        L2PcInstance player = L2World.getInstance().getPlayer(target);
                        if (player != null)
                        {
                            if (performSocial(social,player))
                                activeChar.sendMessage(player.getName()+" was affected by your request.");
                        }
                        else
                        {
                            try
                            {
                                int radius = Integer.parseInt(target);
                                for (L2Object object : activeChar.getKnownList().getKnownObjects().values())
                                   if (activeChar.isInsideRadius(object, radius, false, false))
                                       performSocial(social,object);
                                activeChar.sendMessage(radius+ " units radius affected by your request.");
                            }
                            catch (NumberFormatException nbe)
                            {
                            }
                        }
                    }
                }
                else if (st.countTokens() == 1)
                {
                    int social = Integer.parseInt(st.nextToken());
                    if (obj == null)
                        obj = activeChar;
                    if (obj != null) 
                    {
                        if (performSocial(social,obj))
                             activeChar.sendMessage(obj.getName()+ " was affected by your request.");
                    }
                    else
                        activeChar.sendMessage("Incorrect target");
                }
                else
                    activeChar.sendMessage("Usage: //social social_id [player_name|radius]");
           }
           catch (Exception e)
           {
           }
       }
       else if (command.startsWith("admin_effect"))
       {
           try
           {
               String cmd = command.substring(13);
               StringTokenizer st = new StringTokenizer(cmd);
               L2Object obj = activeChar.getTarget();
               int level = 1;
               int skill = Integer.parseInt(st.nextToken());
               if (st.countTokens() == 2)
                   level = Integer.parseInt(st.nextToken());
               if (obj == null)
                   obj = activeChar;
               if (obj != null) 
               {
                   if (!(obj instanceof L2Character))
                       activeChar.sendMessage("Incorrect target");
                   else
                   {
                       L2Character target = (L2Character)obj;
                       MagicSkillUser MSU = new MagicSkillUser(target,activeChar,skill,level,1,0);
                       target.broadcastPacket(MSU);
                       activeChar.sendMessage(obj.getName()+" performs MSU "+skill+"/"+level+" by your request.");
                   }
              }
              else
                  activeChar.sendMessage("Incorrect target");
           }
           catch(Exception e)
           {
               activeChar.sendMessage("Usage: //effect skill [level]");
           }
       }
       return true;
   }
   
   private boolean performSocial(int action, L2Object target)
   {
       try
       {
           if (target instanceof L2Character)
           {
           if ((target instanceof L2Summon)||((target instanceof L2PcInstance) && ((action<2)||(action>16))))
               return false;
           L2Character character=(L2Character)target;
           character.broadcastPacket(new SocialAction(target.getObjectId(),action));
           }
       }
       catch(Exception e)
       {
       }
       return true;
   }

   public String[] getAdminCommandList()
   {
       return ADMIN_COMMANDS;
   }

   private boolean checkLevel(int level)
   {
       return (level >= REQUIRED_LEVEL);
   }

}
