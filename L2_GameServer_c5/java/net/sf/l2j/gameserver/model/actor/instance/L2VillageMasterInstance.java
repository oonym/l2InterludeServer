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
package net.sf.l2j.gameserver.model.actor.instance;

import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.Set;

import javolution.lang.TextBuilder;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.CharTemplateTable;
import net.sf.l2j.gameserver.ClanTable;
import net.sf.l2j.gameserver.Olympiad;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.base.ClassType;
import net.sf.l2j.gameserver.model.base.PlayerClass;
import net.sf.l2j.gameserver.model.base.PlayerRace;
import net.sf.l2j.gameserver.model.base.SubClass;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.ItemList;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.UserInfo;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * This class ...
 * 
 * @version $Revision: 1.4.2.3.2.8 $ $Date: 2005/03/29 23:15:15 $
 */
public final class L2VillageMasterInstance extends L2FolkInstance
{
    //private static Logger _log = Logger.getLogger(L2VillageMasterInstance.class.getName());

    private static final int ELIXIR_ITEM_ID = 6319; // Mimir's Elixir (obtained through quest)
    private static final int DESTINY_ITEM_ID = 5011; // Star of Destiny

    /**
     * @param template
     */
    public L2VillageMasterInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void onBypassFeedback(L2PcInstance player, String command)
    {
        if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false)) return;

        player.sendPacket(new ActionFailed());

        String[] commandStr = command.split(" ");
        String actualCommand = commandStr[0]; // Get actual command

        String cmdParams = "";

        if (commandStr.length >= 2) cmdParams = commandStr[1];

        if (actualCommand.equalsIgnoreCase("create_clan"))
        {
            if (cmdParams.equals("")) return;

            createClan(player, cmdParams);
        }
        else if (actualCommand.equalsIgnoreCase("create_ally"))
        {
            if (cmdParams.equals("")) return;

            createAlly(player, cmdParams);
        }
        else if (actualCommand.equalsIgnoreCase("dissolve_ally"))
        {
            dissolveAlly(player);
        }
        else if (actualCommand.equalsIgnoreCase("dissolve_clan"))
        {
            dissolveClan(player, player.getClanId());
        }
        else if (actualCommand.equalsIgnoreCase("increase_clan_level"))
        {
            levelUpClan(player, player.getClanId());
        }
        else if (command.startsWith("Subclass"))
        {
            int cmdChoice = Integer.parseInt(command.substring(9, 10).trim());

            // Subclasses may not be changed while a skill is in use.
            if (player.isCastingNow() || player.isAllSkillsDisabled())
            {
                player.sendPacket(new SystemMessage(1295));
                return;
            }

            TextBuilder content = new TextBuilder("<html><body>");
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            Set<PlayerClass> subsAvailable;

            int paramOne = 0;
            int paramTwo = 0;

            try
            {
                int endIndex = command.length();

                if (command.length() > 13)
                {
                    endIndex = 13;
                    paramTwo = Integer.parseInt(command.substring(13).trim());
                }

                paramOne = Integer.parseInt(command.substring(11, endIndex).trim());
            }
            catch (Exception NumberFormatException)
            {
            }

            switch (cmdChoice)
            {
                case 1: // Add Subclass - Initial
                    // Avoid giving player an option to add a new sub class, if they have three already.
                    if (player.getTotalSubClasses() == 3)
                    {
                        player.sendMessage("You can now only change one of your current sub classes.");
                        return;
                    }

                    subsAvailable = getAvailableSubClasses(player);

                    if (subsAvailable != null && !subsAvailable.isEmpty())
                    {
                        content.append("Add Subclass:<br>Which sub class do you wish to add?<br>");

                        for (PlayerClass subClass : subsAvailable)
                            content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 4 "
                                + subClass.ordinal() + "\" msg=\"1268;"
                                + formatClassForDisplay(subClass) + "\">"
                                + formatClassForDisplay(subClass) + "</a><br>");
                    }
                    else
                    {
                        player.sendMessage("There are no sub classes available at this time.");
                        return;
                    }
                    break;
                case 2: // Change Class - Initial
                    content.append("Change Subclass:<br>");

                    final int baseClassId = player.getBaseClass();

                    if (player.getSubClasses().isEmpty())
                    {
                        content.append("You can't change sub classes when you don't have a sub class to begin with.<br>"
                            + "<a action=\"bypass -h npc_"
                            + getObjectId()
                            + "_Subclass 1\">Add subclass.</a>");
                    }
                    else
                    {
                        content.append("Which class would you like to switch to?<br>");

                        if (baseClassId == player.getActiveClass()) content.append(CharTemplateTable.getClassNameById(baseClassId)
                            + "&nbsp;<font color=\"LEVEL\">(Base Class)</font><br><br>");
                        else content.append("<a action=\"bypass -h npc_" + getObjectId()
                            + "_Subclass 5 0\">" + CharTemplateTable.getClassNameById(baseClassId)
                            + "</a>&nbsp;" + "<font color=\"LEVEL\">(Base Class)</font><br><br>");

                        for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext();)
                        {
                            SubClass subClass = subList.next();
                            int subClassId = subClass.getClassId();

                            if (subClassId == player.getActiveClass()) content.append(CharTemplateTable.getClassNameById(subClassId)
                                + "<br>");
                            else content.append("<a action=\"bypass -h npc_" + getObjectId()
                                + "_Subclass 5 " + subClass.getClassIndex() + "\">"
                                + CharTemplateTable.getClassNameById(subClassId) + "</a><br>");
                        }
                    }
                    break;
                case 3: // Change/Cancel Subclass - Initial
                    content.append("Change Subclass:<br>Which of the following sub classes would you like to change?<br>");
                    int classIndex = 1;

                    for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext();)
                    {
                        SubClass subClass = subList.next();

                        content.append("Sub-class " + classIndex + "<br1>");
                        content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 6 "
                            + subClass.getClassIndex() + "\">"
                            + CharTemplateTable.getClassNameById(subClass.getClassId()) + "</a><br>");

                        classIndex++;
                    }

                    content.append("<br>If you change a sub class, you'll start at level 40 after the 2nd class transfer.");
                    break;
                case 4: // Add Subclass - Action (Subclass 4 x[x])
                    boolean allowAddition = true;
                    /*
                     * If the character is less than level 75 on any of their previously chosen 
                     * classes then disallow them to change to their most recently added sub-class choice.
                     */
                    if (player.getLevel() < 75)
                    {
                        player.sendMessage("You may not add a new sub class before you are level 75 on your previous class.");
                        allowAddition = false;
                    }

                    if (Olympiad.getInstance().isRegisteredInComp(player)
                        || player.getOlympiadGameId() > 0)
                    {
                        player.sendPacket(new SystemMessage(
                                                            SystemMessage.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT));
                        return;
                    }

                    if (allowAddition)
                    {
                        if (!player.getSubClasses().isEmpty())
                        {
                            for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext();)
                            {
                                SubClass subClass = subList.next();

                                if (subClass.getLevel() < 75)
                                {
                                    player.sendMessage("You may not add a new sub class before you are level 75 on your previous sub class.");
                                    allowAddition = false;
                                    break;
                                }
                            }
                        }
                    }

                    /* 
                     * If quest checking is enabled, verify if the character has completed the Mimir's Elixir (Path to Subclass) 
                     * and Fate's Whisper (A Grade Weapon) quests by checking for instances of their unique reward items.
                     * 
                     * If they both exist, remove both unique items and continue with adding the sub-class.
                     */
                    if (!Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS)
                    {
                        L2ItemInstance elixirItem = player.getInventory().getItemByItemId(ELIXIR_ITEM_ID);
                        L2ItemInstance destinyItem = player.getInventory().getItemByItemId(
                                                                                           DESTINY_ITEM_ID);

                        if (elixirItem == null)
                        {
                            player.sendMessage("You must have completed the Mimir's Elixir quest to continue adding your sub class.");
                            return;
                        }

                        if (destinyItem == null)
                        {
                            player.sendMessage("You must have completed the Fate's Whisper quest to continue adding your sub class.");
                            return;
                        }

                        if (allowAddition)
                        {
                            player.destroyItemByItemId("Quest", ELIXIR_ITEM_ID, 1, this, true);
                            player.destroyItemByItemId("Quest", DESTINY_ITEM_ID, 1, this, true);
                        }
                    }

                    ////////////////// \\\\\\\\\\\\\\\\\\
                    if (allowAddition)
                    {
                        String className = CharTemplateTable.getClassNameById(paramOne);

                        if (!player.addSubClass(paramOne, player.getTotalSubClasses() + 1))
                        {
                            player.sendMessage("The sub class could not be added.");
                            return;
                        }

                        player.setActiveClass(player.getTotalSubClasses());

                        content.append("Add Subclass:<br>The sub class of <font color=\"LEVEL\">"
                            + className + "</font> has been added.");
                        player.sendPacket(new SystemMessage(1308)); // Transfer to new class.
                    }
                    else
                    {
                        html.setFile("data/html/villagemaster/SubClass_Fail.htm");
                    }
                    break;
                case 5: // Change Class - Action
                    /*
                     * If the character is less than level 75 on any of their previously chosen 
                     * classes then disallow them to change to their most recently added sub-class choice.
                     *
                     * Note: paramOne = classIndex
                     */

                    if (Olympiad.getInstance().isRegisteredInComp(player)
                        || player.getOlympiadGameId() > 0)
                    {
                        player.sendPacket(new SystemMessage(
                                                            SystemMessage.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT));
                        return;
                    }

                    player.setActiveClass(paramOne);

                    content.append("Change Subclass:<br>Your active sub class is now a <font color=\"LEVEL\">"
                        + CharTemplateTable.getClassNameById(player.getActiveClass()) + "</font>.");

                    player.sendPacket(new SystemMessage(1270)); // Transfer completed.
                    break;
                case 6: // Change/Cancel Subclass - Choice
                    content.append("Please choose a sub class to change to. If the one you are looking for is not here, "
                        + "please seek out the appropriate master for that class.<br>"
                        + "<font color=\"LEVEL\">Warning!</font> All classes and skills for this class will be removed.<br><br>");

                    subsAvailable = getAvailableSubClasses(player);

                    if (!subsAvailable.isEmpty())
                    {
                        for (PlayerClass subClass : subsAvailable)
                            content.append("<a action=\"bypass -h npc_" + getObjectId() + "_Subclass 7 "
                                + paramOne + " " + subClass.ordinal() + "\">"
                                + formatClassForDisplay(subClass) + "</a><br>");
                    }
                    else
                    {
                        player.sendMessage("There are no sub classes available at this time.");
                        return;
                    }
                    break;
                case 7: // Change Subclass - Action
                    /* 
                     * Warning: the information about this subclass will be removed from the 
                     * subclass list even if false!
                     */
                    if (player.modifySubClass(paramOne, paramTwo))
                    {
                        player.setActiveClass(paramOne);

                        content.append("Change Subclass:<br>Your sub class has been changed to <font color=\"LEVEL\">"
                            + CharTemplateTable.getClassNameById(paramTwo) + "</font>.");

                        player.sendPacket(new SystemMessage(1269)); // Subclass added.
                    }
                    else
                    {
                        /*
                         * This isn't good! modifySubClass() removed subclass from memory
                         * we must update _classIndex! Else IndexOutOfBoundsException can turn
                         * up some place down the line along with other seemingly unrelated
                         * problems.
                         */
                        player.setActiveClass(0); // Also updates _classIndex plus switching _classid to baseclass.

                        player.sendMessage("The sub class could not be added, you have been reverted to your base class.");
                        return;
                    }
                    break;
            }

            content.append("</body></html>");

            // If the content is greater than for a basic blank page,
            // then assume no external HTML file was assigned.
            if (content.length() > 26) html.setHtml(content.toString());

            player.sendPacket(html);
        }
        else
        {
            // this class dont know any other commands, let forward
            // the command to the parent class
            super.onBypassFeedback(player, command);
        }
    }

    public String getHtmlPath(int npcId, int val)
    {
        String pom = "";

        if (val == 0) pom = "" + npcId;
        else pom = npcId + "-" + val;

        return "data/html/villagemaster/" + pom + ".htm";
    }

    public void onAction(L2PcInstance player)
    {
        if (Config.DEBUG) _log.fine("Village Master activated");
        super.onAction(player);
    }

    //Private stuff
    public void createClan(L2PcInstance player, String clanName)
    {
        if (Config.DEBUG)
            _log.fine(player.getObjectId() + "(" + player.getName() + ") requested clan creation from "
                + getObjectId() + "(" + getName() + ")");
        if (player.getLevel() < 10)
        {
            SystemMessage sm = new SystemMessage(SystemMessage.FAILED_TO_CREATE_CLAN);
            player.sendPacket(sm);
            return;
        }

        if (player.getClanId() != 0)
        {
            SystemMessage sm = new SystemMessage(SystemMessage.FAILED_TO_CREATE_CLAN);
            player.sendPacket(sm);
            return;
        }

        if (!player.canCreateClan())
        {
            // you can't create clan 10 days
            SystemMessage sm = new SystemMessage(230);
            player.sendPacket(sm);
            return;
        }
        if (clanName.length() > 16)
        {
            SystemMessage sm = new SystemMessage(SystemMessage.CLAN_NAME_TOO_LONG);
            player.sendPacket(sm);
            return;
        }

        L2Clan clan = ClanTable.getInstance().createClan(player, clanName.trim());
        if (clan == null)
        {
            // clan name is already taken
            SystemMessage sm = new SystemMessage(SystemMessage.CLAN_NAME_INCORRECT);
            player.sendPacket(sm);
            return;
        }

        //should be update packet only
        PledgeShowInfoUpdate pu = new PledgeShowInfoUpdate(clan, player);
        player.sendPacket(pu);
        
        PledgeShowMemberListAll psmla = new PledgeShowMemberListAll(clan, player);
        player.sendPacket(psmla);

        UserInfo ui = new UserInfo(player);
        player.sendPacket(ui);

        SystemMessage sm = new SystemMessage(SystemMessage.CLAN_CREATED);
        player.sendPacket(sm);
    }

    private void dissolveClan(L2PcInstance player, int clanId)
    {
        if (player.getClan() == null) return;
        if (!player.isClanLeader())
        {
            //only clan leader
            SystemMessage sm = new SystemMessage(785);
            player.sendPacket(sm);
            return;
        }

        L2PcInstance[] clanMembers = player.getClan().getOnlineMembers(player.getName());
        player.setClan(null);
        player.setTitle(null);

        long deleteclantime = System.currentTimeMillis();
        player.setDeleteClanTime(deleteclantime);
        SiegeManager.getInstance().removeSiegeSkills(player);

        // The clan leader should take the XP penalty of a full death.
        player.deathPenalty(false);

        SystemMessage sm = new SystemMessage(193);

        for (int i = 0; i < clanMembers.length; i++)
        {
            clanMembers[i].setClan(null);
            clanMembers[i].setTitle(null);

            clanMembers[i].sendPacket(sm);
            clanMembers[i].broadcastUserInfo();
        }

        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET clanid = 0, clan_privs = 0 WHERE clanid=?");
            statement.setInt(1, clanId);
            statement.execute();
            statement.close();

            // save the leader DeleteClanTime
            statement = con.prepareStatement("UPDATE characters SET deleteclan = ? WHERE obj_Id=?");
            statement.setLong(1, deleteclantime);
            statement.setInt(2, player.getObjectId());
            statement.close();

            statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
            statement.setInt(1, clanId);
            statement.execute();
            statement.close();
            player.sendPacket(sm);
            player.broadcastUserInfo();

            con.close();
        }
        catch (Exception e)
        {
            _log.warning("could not dissolve clan:" + e);
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
        }
    }

    public void levelUpClan(L2PcInstance player, int clanId)
    {
        L2Clan clan = player.getClan();
        if (clan == null)
        {
            return;
        }
        if (!player.isClanLeader())
        {
            SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
            sm.addString("Only clan leader can increase the clan level."); //i was too lazy to check if such a sysmsg already exists
            player.sendPacket(sm);
            return;
        }

        boolean increaseClanLevel = false;

        switch (clan.getLevel())
        {
            case 0:
            {
                // upgrade to 1
                if (player.getSp() >= 35000 && player.getAdena() >= 650000)
                {
                    if (player.reduceAdena("ClanLvl", 650000, this, true))
                    {
	                    player.setSp(player.getSp() - 35000);
	                    increaseClanLevel = true;
                    }
                }
                break;
            }
            case 1:
            {
                // upgrade to 2
                if (player.getSp() >= 150000 && player.getAdena() >= 2500000)
                {
                    if (player.reduceAdena("ClanLvl", 2500000, this, true))
                    {
	                    player.setSp(player.getSp() - 150000);
	                    increaseClanLevel = true;
                    }
                }
                break;
            }
            case 2:
            {
                // upgrade to 3
                if (player.getSp() >= 500000 && player.getInventory().getItemByItemId(1419) != null)
                {
                    // itemid 1419 == proof of blood
                    if (player.destroyItemByItemId("ClanLvl", 1419, 1, player.getTarget(), false))
                    {
	                    player.setSp(player.getSp() - 500000);
	                    increaseClanLevel = true;
                    }
                }
                break;
            }
            case 3:
            {
                // upgrade to 4
                if (player.getSp() >= 1400000 && player.getInventory().getItemByItemId(3874) != null)
                {
                    // itemid 3874 == proof of alliance
                	if (player.destroyItemByItemId("ClanLvl", 3874, 1, player.getTarget(), false))
                	{
	                    player.setSp(player.getSp() - 1400000);
	                    increaseClanLevel = true;
                	}
                }
                break;
            }
            case 4:
            {
                // upgrade to 5
                if (player.getSp() >= 3500000 && player.getInventory().getItemByItemId(3870) != null)
                {
                    // itemid 3870 == proof of aspiration
                	if (player.destroyItemByItemId("ClanLvl", 3870, 1, player.getTarget(), false))
                	{
                		player.setSp(player.getSp() - 3500000);
                        increaseClanLevel = true;
                	}
                	/*
                    if (player.getInventory().getItemByItemId(3870) != null)
                    {
                        player.getInventory().destroyItemByItemId("ClanLvl", 3870, 1, player,
                                                                  player.getTarget());
                    }
                    else
                    {
                        player.reduceAdena("ClanLvl", 30000000, this, true);
                    }
                    increaseClanLevel = true;
                    */
                }
                break;
            }
        }

        if (increaseClanLevel)
        {
            // the player should know that he has less sp now :p
            StatusUpdate su = new StatusUpdate(player.getObjectId());
            su.addAttribute(StatusUpdate.SP, player.getSp());
            sendPacket(su);

            ItemList il = new ItemList(player, false);
            player.sendPacket(il);

            java.sql.Connection con = null;
            try
            {
                con = L2DatabaseFactory.getInstance().getConnection();
                PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET clan_level = ? WHERE clan_id = ?");
                statement.setInt(1, clan.getLevel() + 1);
                statement.setInt(2, clanId);
                statement.execute();
                statement.close();

                con.close();
            }
            catch (Exception e)
            {
                _log.warning("could not increase clan level:" + e);
            }
            finally
            {
                try
                {
                    con.close();
                }
                catch (Exception e)
                {
                }
            }

            clan.setLevel(clan.getLevel() + 1);
            if (clan.getLevel() > 3) SiegeManager.getInstance().addSiegeSkills(player);

            // notify all the members about it
            SystemMessage sm = new SystemMessage(SystemMessage.CLAN_LEVEL_INCREASED);
            clan.broadcastToOnlineMembers(sm);
            clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan, player));
            /*
             * Micht :
             * 	- use PledgeShowInfoUpdate instead of PledgeStatusChanged
             * 		to update clan level ingame
             * 	- remove broadcastClanStatus() to avoid members duplication
             */
            //clan.broadcastToOnlineMembers(new PledgeStatusChanged(clan));
            //clan.broadcastClanStatus();
        }
        else
        {
            SystemMessage sm = new SystemMessage(SystemMessage.FAILED_TO_INCREASE_CLAN_LEVEL);
            player.sendPacket(sm);
        }
    }

    public void createAlly(L2PcInstance player, String allyName)
    {
        //D5 You may not ally with clan you are battle with.
        //D6 Only the clan leader may apply for withdraw from alliance.
        //DD No response. Invitation to join an 
        //D7 Alliance leaders cannot withdraw.
        //D9 Different Alliance 
        //EB alliance information
        //Ec alliance name $s1
        //ee alliance leader: $s2 of $s1
        //ef affilated clans: total $s1 clan(s)
        //f6 you have already joined an alliance
        //f9 you cannot new alliance 10 days
        //fd cannot accept. clan ally is register as enemy during siege batle.
        //fe you have invited someone to your alliance.
        //100 do you wish to withdraw from the alliance
        //102 enter the name of the clan you wish to expel.
        //202 do you realy wish to dissolve the alliance
        //502 you have accepted alliance
        //602 you have failed to invite a clan into the alliance
        //702 you have withdrwa

        if (player.getClanId() == 0) return;

        if (Config.DEBUG)
            _log.fine(player.getObjectId() + "(" + player.getName() + ") requested ally creation from "
                + getObjectId() + "(" + getName() + ")");

        if (!player.isClanLeader())
        {
            //only clan leaders may create alliances.
            SystemMessage sm = new SystemMessage(504);
            player.sendPacket(sm);
            return;
        }
        if (player.getClan().getAllyId() != 0)
        {
            //is in ally
            SystemMessage sm = new SystemMessage(502);
            player.sendPacket(sm);
            return;
        }
        if (allyName.length() < 2)
        {
            // incorect alliance name
            SystemMessage sm = new SystemMessage(506);
            player.sendPacket(sm);
            return;
        }
        if (allyName.length() > 16)
        {
            // incorect length of alliance name
            SystemMessage sm = new SystemMessage(507);
            player.sendPacket(sm);
            return;
        }
        if (player.getClan().getLevel() < 5)
        {
            //only clan level 5 or higher.
            SystemMessage sm = new SystemMessage(549);
            player.sendPacket(sm);
            return;
        }
        if (ClanTable.getInstance().isAllyExists(allyName))
        {
            //name exists.
            SystemMessage sm = new SystemMessage(508);
            player.sendPacket(sm);
            return;
        }

        player.getClan().setAllyId(player.getClanId());
        player.getClan().setAllyName(allyName.trim());
        player.getClan().updateClanInDB();

        UserInfo ui = new UserInfo(player);
        player.sendPacket(ui);

        SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
        sm.addString("Alliance " + allyName + " has been created.");
        player.sendPacket(sm);
    }

    private void dissolveAlly(L2PcInstance player)
    {
        L2Clan playerClan = player.getClan();

        if (player.getClan() == null) return;

        int allyId = playerClan.getAllyId();

        if (allyId == 0) return;

        if (allyId != player.getClanId() || !player.isClanLeader())
        {
            player.sendPacket(new SystemMessage(SystemMessage.FEATURE_ONLY_FOR_ALLIANCE_LEADER));
            return;
        }

        playerClan.setAllyId(0);
        playerClan.setAllyName(null);

        // The clan leader should take the XP penalty of a full death.
        player.deathPenalty(false);

        SystemMessage sm = new SystemMessage(SystemMessage.ALLIANCE_DISOLVED);
        java.sql.Connection con = null;

        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("UPDATE clan_data SET ally_id = 0, ally_name = '' WHERE ally_id=?");
            statement.setInt(1, allyId);
            statement.execute();
            statement.close();

            player.sendPacket(sm);
            player.broadcastUserInfo();

            con.close();
        }
        catch (Exception e)
        {
            _log.warning("could not dissolve clan:" + e);
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
        }
    }

    private final Set<PlayerClass> getAvailableSubClasses(L2PcInstance player)
    {
        int charClassId = player.getClassId().ordinal();

        if (charClassId >= 88) charClassId = player.getClassId().getParent().ordinal();

        final PlayerRace npcRace = getVillageMasterRace();
        final ClassType npcTeachType = getVillageMasterTeachType();

        PlayerClass currClass = PlayerClass.values()[charClassId];

        /**
         * If the race of your main class is Elf or Dark Elf, 
         * you may not select each class as a subclass to the other class, 
         * and you may not select Overlord and Warsmith class as a subclass.
         * 
         * You may not select a similar class as the subclass. 
         * The occupations classified as similar classes are as follows:
         *  
         * Treasure Hunter, Plainswalker and Abyss Walker 
         * Hawkeye, Silver Ranger and Phantom Ranger 
         * Paladin, Dark Avenger, Temple Knight and Shillien Knight 
         * Warlocks, Elemental Summoner and Phantom Summoner 
         * Elder and Shillien Elder 
         * Swordsinger and Bladedancer 
         * Sorcerer, Spellsinger and Spellhowler
         * 
         */
        Set<PlayerClass> availSubs = currClass.getAvaliableSubclasses();

        if (availSubs != null)
        {
            for (PlayerClass availSub : availSubs)
            {
                for (Iterator<SubClass> subList = iterSubClasses(player); subList.hasNext();)
                {
                    SubClass prevSubClass = subList.next();

                    if (availSub.ordinal() == prevSubClass.getClassId()
                        || availSub.ordinal() == player.getBaseClass())
                        availSubs.remove(PlayerClass.values()[availSub.ordinal()]);
                }

                if ((npcRace == PlayerRace.Human || npcRace == PlayerRace.LightElf))
                {
                    // If the master is human or light elf, ensure that fighter-type 
                    // masters only teach fighter classes, and priest-type masters 
                    // only teach priest classes etc.
                    if (!availSub.isOfType(npcTeachType)) availSubs.remove(availSub);

                    // Remove any non-human or light elf classes.
                    else if (!availSub.isOfRace(PlayerRace.Human)
                        && !availSub.isOfRace(PlayerRace.LightElf)) availSubs.remove(availSub);
                }
                else
                {
                    // If the master is not human and not light elf, 
                    // then remove any classes not of the same race as the master.
                    if ((npcRace != PlayerRace.Human && npcRace != PlayerRace.LightElf)
                        && !availSub.isOfRace(npcRace)) availSubs.remove(availSub);
                }
            }
        }

        return availSubs;
    }

    private final String formatClassForDisplay(PlayerClass className)
    {
        String classNameStr = className.toString();
        char[] charArray = classNameStr.toCharArray();

        for (int i = 1; i < charArray.length; i++)
            if (Character.isUpperCase(charArray[i]))
                classNameStr = classNameStr.substring(0, i) + " " + classNameStr.substring(i);

        return classNameStr;
    }

    private final PlayerRace getVillageMasterRace()
    {
        String npcClass = getTemplate().getStatsSet().getString("jClass").toLowerCase();

        if (npcClass.indexOf("human") > -1) return PlayerRace.Human;

        if (npcClass.indexOf("darkelf") > -1) return PlayerRace.DarkElf;

        if (npcClass.indexOf("elf") > -1) return PlayerRace.LightElf;

        if (npcClass.indexOf("orc") > -1) return PlayerRace.Orc;

        return PlayerRace.Dwarf;
    }

    private final ClassType getVillageMasterTeachType()
    {
        String npcClass = getTemplate().getStatsSet().getString("jClass");

        if (npcClass.indexOf("sanctuary") > -1 || npcClass.indexOf("clergyman") > -1)
            return ClassType.Priest;

        if (npcClass.indexOf("mageguild") > -1 || npcClass.indexOf("patriarch") > -1)
            return ClassType.Mystic;

        return ClassType.Fighter;
    }

    private Iterator<SubClass> iterSubClasses(L2PcInstance player)
    {
        return player.getSubClasses().values().iterator();
    }
}
