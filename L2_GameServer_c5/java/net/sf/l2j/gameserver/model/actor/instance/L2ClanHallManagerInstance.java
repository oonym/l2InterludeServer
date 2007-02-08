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

import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.TeleportLocationTable;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.SiegeManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2TeleportLocation;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.WareHouseDepositList;
import net.sf.l2j.gameserver.serverpackets.WareHouseWithdrawalList;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2ClanHallManagerInstance extends L2FolkInstance
{
    protected static int Cond_All_False = 0;
    protected static int Cond_Busy_Because_Of_Siege = 1;
    protected static int Cond_Owner = 2;
    private int _clanHallId = -1;

	/**
	 * @param objectId
	 * @param template
	 */
	public L2ClanHallManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
    
    public void onBypassFeedback(L2PcInstance player, String command)
    {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        player.sendPacket( new ActionFailed() );
        int condition = validateCondition(player);
        if (condition <= Cond_All_False)
            return;
        else if (condition == Cond_Owner)
        {
            StringTokenizer st = new StringTokenizer(command, " ");
            String actualCommand = st.nextToken(); // Get actual command
            String val = "";
            if (st.countTokens() >= 1) {val = st.nextToken();}
     
            if (actualCommand.equalsIgnoreCase("banish_foreigner"))
            {
                getClanHall().banishForeigner(player);
                return;
            }
            else if(actualCommand.equalsIgnoreCase("manage_vault"))
            {
                if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) == L2Clan.CP_CL_VIEW_WAREHOUSE)
                {
                    if (val.equalsIgnoreCase("deposit"))
                        showVaultWindowDeposit(player);
                    else if (val.equalsIgnoreCase("withdraw"))
                        showVaultWindowWithdraw(player);
                    else
                    {
                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        html.setFile("data/html/clanHallManager/vault.htm");
                        this.sendHtmlMessage(player, html);
                    }
                }
                else
                    player.sendMessage("You are not authorized to do this!");
                return;
            }
            else if (actualCommand.equalsIgnoreCase("door"))
            {
                if ((player.getClanPrivileges() & L2Clan.CP_CH_OPEN_DOOR) == L2Clan.CP_CH_OPEN_DOOR)
                {
                    if (val.equalsIgnoreCase("open"))
                        getClanHall().openCloseDoors(true);
                    else if (val.equalsIgnoreCase("close"))
                        getClanHall().openCloseDoors(false);
                    else
                    {
                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        html.setFile("data/html/clanHallManager/door.htm");
                        sendHtmlMessage(player, html);
                    }
                }
                else
                    player.sendMessage("You are not authorized to do this!");
            }
            else if (actualCommand.equalsIgnoreCase("functions"))
            {
                if (val.equalsIgnoreCase("tele"))
                {
                    if (getClanHall().getFunction(ClanHall.FUNC_TELEPORT) == null)
                        return;
                    NpcHtmlMessage html = new NpcHtmlMessage(1);
                    html.setFile("data/html/clanHallManager/tele"+getClanHall().getLocation()+getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl()+".htm");
                    sendHtmlMessage(player, html);
                }
                else if (val.equalsIgnoreCase("item_creation"))
                {
                    if (getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE) == null)
                        return;
                    NpcHtmlMessage html = new NpcHtmlMessage(1);
                    html.setFile("data/html/clanHallManager/item"+getClanHall().getLocation()+getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getLvl()+".htm");
                    sendHtmlMessage(player, html);
                }
                else if (val.equalsIgnoreCase("support"))
                {
                    if (getClanHall().getFunction(ClanHall.FUNC_SUPPORT)== null)
                        return;
                    NpcHtmlMessage html = new NpcHtmlMessage(1);
                    html.setFile("data/html/clanHallManager/support"+getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl()+".htm");
                    html.replace("%mp%", String.valueOf(getCurrentMp()));
                    sendHtmlMessage(player, html);
                }
                else if (val.equalsIgnoreCase("back"))
                    showMessageWindow(player);
                else
                {
                    NpcHtmlMessage html = new NpcHtmlMessage(1);
                    html.setFile("data/html/clanHallManager/functions.htm");
                    if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP) != null)
                        html.replace("%xp_regen%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl()) + "%");
                    else
                        html.replace("%xp_regen%", "0");
                    if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP) != null)
                        html.replace("%hp_regen%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getLvl()) + "%");
                    else
                        html.replace("%hp_regen%", "0");
                    if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP) != null)
                        html.replace("%mp_regen%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getLvl()) + "%");
                    else
                        html.replace("%mp_regen", "0");
                    sendHtmlMessage(player, html);
                }                    
            }
            else if (actualCommand.equalsIgnoreCase("manage"))
            {
                if ((player.getClanPrivileges() & L2Clan.CP_CH_SET_FUNCTIONS) == L2Clan.CP_CH_SET_FUNCTIONS)
                {
                    if (val.equalsIgnoreCase("recovery"))
                    {
                        if(getClanHall().getGrade() < 3)
                        {
                            player.sendMessage("Your clan hall's grade is too low to use these functions");
                            return;
                        }
                        if (st.countTokens() >= 1)
                        {
                            val = st.nextToken();
                            if (val.equalsIgnoreCase("hp"))
                            {  
                                if (st.countTokens() >= 1)
                                {
                                    int fee;
                                    if (Config.DEBUG) _log.warning("Mp editing invoked");
                                    val = st.nextToken();
                                    int percent = Integer.valueOf(val);
                                    switch (percent)
                                    {
                                        case 80:
                                            fee = Config.CH_HPREG1_FEE;
                                            break;
                                        case 140:
                                            fee = Config.CH_HPREG2_FEE;
                                            break;
                                        case 200:
                                            fee = Config.CH_HPREG3_FEE;
                                            break;
                                        default:
                                            fee = Config.CH_HPREG4_FEE;
                                            break;
                                    }
                                    if (!getClanHall().updateFunctions(ClanHall.FUNC_RESTORE_HP, percent, fee, Config.CH_HPREG_FEE_RATIO, Calendar.getInstance().getTimeInMillis()+Config.CH_HPREG_FEE_RATIO, (getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP) == null)))
                                        player.sendMessage("You don't have enough adena in your clan's warehouse");
                                }
                            }
                            else if (val.equalsIgnoreCase("mp"))
                            {  
                                if (st.countTokens() >= 1)
                                {
                                    int fee;
                                    if (Config.DEBUG) _log.warning("Mp editing invoked");
                                    val = st.nextToken();
                                    int percent = Integer.valueOf(val);
                                    switch (percent)
                                    {
                                        case 5:
                                            fee = Config.CH_MPREG1_FEE;
                                            break;
                                        case 15:
                                            fee = Config.CH_MPREG2_FEE;
                                            break;
                                        default:
                                            fee = Config.CH_MPREG3_FEE;
                                            break;
                                    }
                                    if(!getClanHall().updateFunctions(ClanHall.FUNC_RESTORE_MP, percent, fee, Config.CH_MPREG_FEE_RATIO, Calendar.getInstance().getTimeInMillis()+Config.CH_MPREG_FEE_RATIO, (getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP) == null)))
                                        player.sendMessage("You don't have enough adena in your clan's warehouse");
                                }
                            }
                            else if (val.equalsIgnoreCase("exp"))
                            {  
                                if (st.countTokens() >= 1)
                                {
                                    int fee;
                                    if (Config.DEBUG) _log.warning("Exp editing invoked");
                                    val = st.nextToken();
                                    int percent = Integer.valueOf(val);
                                    switch (percent)
                                    {
                                        case 5:
                                            fee = Config.CH_EXPREG1_FEE;
                                            break;
                                        case 15:
                                            fee = Config.CH_EXPREG2_FEE;
                                            break;
                                        case 25:
                                            fee = Config.CH_EXPREG3_FEE;
                                            break;
                                        default:
                                            fee = Config.CH_EXPREG4_FEE;
                                            break;
                                    }
                                    if (!getClanHall().updateFunctions(ClanHall.FUNC_RESTORE_EXP, percent, fee, Config.CH_EXPREG_FEE_RATIO, Calendar.getInstance().getTimeInMillis()+Config.CH_EXPREG_FEE_RATIO, (getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP) == null)))
                                        player.sendMessage("You don't have enough adena in your clan's warehouse");
                                }
                            }
                            //int percent = Integer.valueOf(val);
                            //getClanHall().updateFunctions(new ClanHallFunction(), (getClanHall().getFunction()))
                        }
                        else
                        {
                            NpcHtmlMessage html = new NpcHtmlMessage(1);
                            html.setFile("data/html/clanHallManager/edit_recovery.htm");
                            if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP) != null){
                            	html.replace("%hp%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getLvl()) + "%");
                                html.replace("%hpPrice%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getLease()));
                                html.replace("%hpDate%",format.format(getClanHall().getFunction(ClanHall.FUNC_RESTORE_HP).getEndTime()));
                            }else{
                                html.replace("%hp%", "0");
                                html.replace("%hpPrice%", "0");
                                html.replace("%hpDate%","0");
                            }
                            if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP) != null){
                                html.replace("%exp%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getLvl()) + "%");
                                html.replace("%expPrice%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getLease()));
                                html.replace("%expDate%",format.format(getClanHall().getFunction(ClanHall.FUNC_RESTORE_EXP).getEndTime()));
                            }else{
                                html.replace("%exp%", "0");
                                html.replace("%expPrice%", "0");
                                html.replace("%expDate%","0");
                            }
                            if (getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP) != null){
                                html.replace("%mp%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getLvl()) + "%");
                                html.replace("%mpPrice%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getLease()));
                                html.replace("%mpDate%",format.format(getClanHall().getFunction(ClanHall.FUNC_RESTORE_MP).getEndTime()));
                            }else{
                                html.replace("%mp%", "0");
                                html.replace("%mpPrice%", "0");
                                html.replace("%mpDate%","0");
                            }
                            sendHtmlMessage(player, html);
                        }
                    }
                    else if (val.equalsIgnoreCase("other"))
                    {
                        if(getClanHall().getGrade() < 2)
                        {
                            player.sendMessage("Your clan hall's grade is too low to use these functions");
                            return;
                        }
                        if (st.countTokens() >= 1)
                        {
                            val = st.nextToken();
                            if (val.equalsIgnoreCase("item"))
                            {  
                                if (st.countTokens() >= 1)
                                {
                                	if (Config.DEBUG) _log.warning("Item editing invoked");
                                    val = st.nextToken();
                                    int lvl = Integer.valueOf(val);
                                    if (!getClanHall().updateFunctions(ClanHall.FUNC_ITEM_CREATE, lvl, 76000, 86400000, Calendar.getInstance().getTimeInMillis()+86400000, (getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE) == null)))
                                        player.sendMessage("You don't have enough adena in your clan's warehouse");
                                }
                            }
                            else if (val.equalsIgnoreCase("tele"))
                            {  
                                if (st.countTokens() >= 1)
                                {
                                    int fee;
                                    if (Config.DEBUG) _log.warning("Tele editing invoked");
                                    val = st.nextToken();
                                    int lvl = Integer.valueOf(val);
                                    switch (lvl)
                                    {
                                        case 1:
                                            fee = Config.CH_TELE1_FEE;
                                            break;
                                        case 2:
                                            fee = Config.CH_TELE2_FEE;
                                            break;
                                        default:
                                            fee = Config.CH_TELE3_FEE;
                                            break;
                                    }
                                    if (!getClanHall().updateFunctions(ClanHall.FUNC_TELEPORT, lvl, fee, Config.CH_TELE_FEE_RATIO, Calendar.getInstance().getTimeInMillis()+Config.CH_TELE_FEE_RATIO, (getClanHall().getFunction(ClanHall.FUNC_TELEPORT) == null)))
                                        player.sendMessage("You don't have enough adena in your clan's warehouse");
                                }
                            }
                            else if (val.equalsIgnoreCase("support"))
                            {  
                                if (st.countTokens() >= 1)
                                {
                                    int fee;
                                    if (Config.DEBUG) _log.warning("Support editing invoked");
                                    val = st.nextToken();
                                    int lvl = Integer.valueOf(val);
                                    switch (lvl)
                                    {
                                        case 1:
                                            fee = Config.CH_SUPPORT1_FEE;
                                            break;
                                        case 2:
                                            fee = Config.CH_SUPPORT2_FEE;
                                            break;
                                        case 3:
                                            fee = Config.CH_SUPPORT3_FEE;
                                            break;
                                        case 4:
                                            fee = Config.CH_SUPPORT4_FEE;
                                            break;
                                        default:
                                            fee = Config.CH_SUPPORT5_FEE;
                                            break;
                                    }
                                    if (!getClanHall().updateFunctions(ClanHall.FUNC_SUPPORT, lvl, fee, Config.CH_SUPPORT_FEE_RATIO, Calendar.getInstance().getTimeInMillis()+Config.CH_SUPPORT_FEE_RATIO, (getClanHall().getFunction(ClanHall.FUNC_SUPPORT) == null)))
                                        player.sendMessage("You don't have enough adena in your clan's warehouse");
                                }
                            }
                            //int percent = Integer.valueOf(val);
                            //getClanHall().updateFunctions(new ClanHallFunction(), (getClanHall().getFunction()))
                        }
                        else
                        {
                            NpcHtmlMessage html = new NpcHtmlMessage(1);
                            html.setFile("data/html/clanHallManager/edit_other.htm");
                            if (getClanHall().getFunction(ClanHall.FUNC_TELEPORT) != null){
                                html.replace("%tele%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLvl()));
                                html.replace("%telePrice%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getLease()));
                                html.replace("%teleDate%",format.format(getClanHall().getFunction(ClanHall.FUNC_TELEPORT).getEndTime()));
                            }else{
                                html.replace("%tele%", "0");
                                html.replace("%telePrice%", "0");
                                html.replace("%teleDate%","0");
                            }
                            if (getClanHall().getFunction(ClanHall.FUNC_SUPPORT) != null){
                                html.replace("%support%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLvl()));
                                html.replace("%supportPrice%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getLease()));
                                html.replace("%supportDate%",format.format(getClanHall().getFunction(ClanHall.FUNC_SUPPORT).getEndTime()));
                            }else{
                                html.replace("%support%", "0");
                                html.replace("%supportPrice%", "0");
                                html.replace("%supportDate%","0");
                            }
                            if (getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE) != null){
                                html.replace("%item%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getLvl()));
                                html.replace("%itemPrice%", String.valueOf(getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getLease()));
                                html.replace("%itemDate%",format.format(getClanHall().getFunction(ClanHall.FUNC_ITEM_CREATE).getEndTime()));                       
                            }else{
                                html.replace("%item%", "0");
                                html.replace("%itemPrice%", "0");
                                html.replace("%itemDate%","0");
                            }
                            sendHtmlMessage(player, html);
                        }
                    }
                    else if (val.equalsIgnoreCase("deco"))
                    {
                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        html.setFile("data/html/clanHallManager/deco.htm");
                        sendHtmlMessage(player, html);
                    }
                    else if (val.equalsIgnoreCase("back"))
                        showMessageWindow(player);
                    else
                    {
                        NpcHtmlMessage html = new NpcHtmlMessage(1);
                        html.setFile("data/html/clanHallManager/manage.htm");
                        sendHtmlMessage(player, html);
                    }
                }
                else
                    player.sendMessage("You are not authorized to do this!");
                return;
            }
            else if (actualCommand.equalsIgnoreCase("support"))
            {
                this.setTarget(player);
                L2Skill skill;
                if (val == "") return;

                try
                {
                    int skill_id = Integer.parseInt(val);
                    try
                    {
                        int skill_lvl = 0;
                        if (st.countTokens() >= 1) skill_lvl = Integer.parseInt(st.nextToken());
                        skill = SkillTable.getInstance().getInfo(skill_id,skill_lvl);
                        if (skill.getSkillType() == SkillType.SUMMON)
                            player.doCast(skill);
                        else
                            this.doCast(skill);
                    }
                    catch (Exception e)
                    {
                        player.sendMessage("Invalid skill level!");
                    }
                }
                catch (Exception e)
                {
                    player.sendMessage("Invalid skill!");
                }
                return;
            }
            else if (actualCommand.equalsIgnoreCase("goto"))
            {
                int whereTo = Integer.parseInt(val);
                doTeleport(player, whereTo);
                return;
            }
        }
        super.onBypassFeedback(player, command);
    }
	
	/**
	 * this is called when a player interacts with this NPC
	 * @param player
	 */
    public void onAction(L2PcInstance player)
    {
        player.sendPacket(new ActionFailed());
        player.setTarget(this);
        player.sendPacket(new MyTargetSelected(getObjectId(), -15));
        if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
        {
        	player.setLastFolkNPC(this);
        	showMessageWindow(player);
        }
    }
    
    private void sendHtmlMessage(L2PcInstance player, NpcHtmlMessage html)
    {
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    }
    
    private void showMessageWindow(L2PcInstance player)
    {
        player.sendPacket(new ActionFailed());
        String filename = "data/html/clanHallManager/chamberlain-no.htm";
        
        int condition = validateCondition(player);
        if (condition > Cond_All_False)
        {
            if (condition == Cond_Owner)                                               // Clan owns CH
                filename = "data/html/clanHallManager/chamberlain.htm";                         // Owner message window
        }

        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    }

    protected int validateCondition(L2PcInstance player)
    {   
        if (getClanHall() == null) return Cond_All_False;
    	if (player.isGM()) return Cond_Owner;
        if (player.getClan() != null)
        {                                     
            if (getClanHall().getOwnerId() == player.getClanId())                                          
                return Cond_Owner;
        }
        
        return Cond_All_False;
    }
    
    /** Return the L2ClanHall this L2NpcInstance belongs to. */
    public final ClanHall getClanHall()
    {
        if (_clanHallId < 0)
        {
            _clanHallId = ClanHallManager.getInstance().getClanHall(getX(), getY(), 1500).getId();
            if (_clanHallId < 0) return null;
        }
        return ClanHallManager.getInstance().getClanHall(_clanHallId);
    }
    
    private void showVaultWindowDeposit(L2PcInstance player)
    {
        player.sendPacket(new ActionFailed());
        player.setActiveWarehouse(player.getClan().getWarehouse());
        player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.Clan)); //Or Clan Hall??
    }

    private void showVaultWindowWithdraw(L2PcInstance player)
    {
        player.sendPacket(new ActionFailed());
        player.setActiveWarehouse(player.getClan().getWarehouse());
        player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.Clan)); //Or Clan Hall ??
    }
    
    private void doTeleport(L2PcInstance player, int val)
    {
       if(Config.DEBUG)
    	   player.sendMessage("doTeleport(L2PcInstance player, int val) is called");
        L2TeleportLocation list = TeleportLocationTable.getInstance().getTemplate(val);
        if (list != null)
        {
            //you cannot teleport to village that is in siege Not sure about this one though
            if (SiegeManager.getInstance().checkIfInZone(list.getLocX(), list.getLocY()))
            {
                player.sendPacket(new SystemMessage(707));
                return;
            }
            else if(player.reduceAdena("Teleport", list.getPrice(), this, true))
            {
                if (Config.DEBUG)
                 _log.warning("Teleporting player "+player.getName()+" for CH to new location: "+list.getLocX()+":"+list.getLocY()+":"+list.getLocZ());
                player.teleToLocation(list.getLocX(), list.getLocY(), list.getLocZ());
            }
        }
        else
        {
            _log.warning("No teleport destination with id:" +val);
        }
        player.sendPacket( new ActionFailed() );
    }
}