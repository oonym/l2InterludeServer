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
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.NpcTable;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2DropCategory;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2BoxInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

/**
 * @author terry
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AdminEditNpc implements IAdminCommandHandler {
    private static Logger _log = Logger.getLogger(AdminEditChar.class.getName());
    private final static int PAGE_LIMIT = 7;
    
    private static String[] _adminCommands = {
            "admin_edit_npc",
            "admin_save_npc",
            "admin_show_droplist",
            "admin_edit_drop",
            "admin_add_drop",
            "admin_del_drop",
            "admin_showShop",
            "admin_showShopList",
            "admin_addShopItem",
            "admin_delShopItem",
            "admin_box_access",
            "admin_editShopItem",
            "admin_close_window"
    };
    private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;
    private static final int REQUIRED_LEVEL2 = Config.GM_NPC_VIEW;
    
    public boolean useAdminCommand(String command, L2PcInstance activeChar)
    {
        if (!((checkLevel(activeChar.getAccessLevel()) || checkLevel2(activeChar.getAccessLevel())) && activeChar.isGM())) return false;
        else if (command.startsWith("admin_showShop "))
        {
            String[] args = command.split(" ");
            if (args.length > 1)
                showShop(activeChar, Integer.parseInt(command.split(" ")[1]));
        }
        else if(command.startsWith("admin_showShopList "))
        {
            String[] args = command.split(" ");
            if (args.length > 2)
                showShopList(activeChar, Integer.parseInt(command.split(" ")[1]), Integer.parseInt(command.split(" ")[2]));
        }
        else if(command.startsWith("admin_edit_npc "))
        {
            int npcId = 0;
            
            try
            {
                npcId = Integer.parseInt(command.substring(15).trim());
            }
            catch(Exception e) {}
            if(npcId > 0){
                L2NpcTemplate npc = NpcTable.getInstance().getTemplate(npcId);
                Show_Npc_Property(activeChar, npc);
            }
            else
            {
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("Command error:");
                sm.addString("//edit_npc <npc_id>");
                activeChar.sendPacket(sm);
            }
        }
        else if(command.startsWith("admin_show_droplist "))
        {
            int npcId = 0;
            try
            {
                npcId = Integer.parseInt(command.substring(20).trim());
            }
            catch(Exception e){}
            
            if(npcId > 0)
            {
                showNpcDropList(activeChar, npcId);
            }
            else
            {
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("Command error:");
                sm.addString("//show_droplist <npc_id>");
                activeChar.sendPacket(sm);
            }
        }
        else if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
        else if(command.startsWith("admin_addShopItem "))
        {
            String[] args = command.split(" ");
            if (args.length > 1)
                addShopItem(activeChar, args);
        }
        else if(command.startsWith("admin_delShopItem "))
        {
            String[] args = command.split(" ");
            if (args.length > 2)
                delShopItem(activeChar, args);
        }
        else if(command.startsWith("admin_editShopItem "))
        {
            String[] args = command.split(" ");
            if (args.length > 2)
                editShopItem(activeChar, args);
        }
        else if(command.startsWith("admin_save_npc "))
        {
            //System.out.println("- " + command);
            try
            {
                save_npc_property(command.substring(14).trim());
            }
            catch (StringIndexOutOfBoundsException e)
            { }
        }
        else if(command.startsWith("admin_edit_drop "))
        {
            int npcId = -1, itemId = 0, category = -1000;
            try
            {
                StringTokenizer st = new StringTokenizer(command.substring(16).trim());
                if(st.countTokens() == 3)
                {            
    	            try
    	            {
                        npcId = Integer.parseInt(st.nextToken());
                        itemId = Integer.parseInt(st.nextToken());
                        category = Integer.parseInt(st.nextToken());
                        showEditDropData(activeChar, npcId, itemId, category);
    	            }
    	            catch(Exception e)
                    {}
                }
                else if(st.countTokens() == 6)
                {
                    try
                    {
                        npcId = Integer.parseInt(st.nextToken());
                        itemId = Integer.parseInt(st.nextToken());
                        category = Integer.parseInt(st.nextToken());
                        int min = Integer.parseInt(st.nextToken());
                        int max = Integer.parseInt(st.nextToken());
                        int chance = Integer.parseInt(st.nextToken());
                        
                        updateDropData(activeChar, npcId, itemId, min, max, category, chance);
                    }
                    catch(Exception e)
                    {
                        _log.fine("admin_edit_drop parements error: " + command);
                    }
                }
                else
                {
                    SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                    sm.addString("Command error:");
                    sm.addString("//edit_drop <npc_id> <item_id> <category> [<min> <max> <chance>]");
                    activeChar.sendPacket(sm);
                }
            }
            catch (StringIndexOutOfBoundsException e)
            {
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("Command error:");
                sm.addString("//edit_drop <npc_id> <item_id> <category> [<min> <max> <chance>]");
                activeChar.sendPacket(sm);
            }
        }
        else  if(command.startsWith("admin_add_drop "))
        {
            int npcId = -1;
            try
            {
                StringTokenizer st = new StringTokenizer(command.substring(15).trim());
                if(st.countTokens() == 1)
                {            
    	            try
    	            {
                        String[] input = command.substring(15).split(" ");
                        if (input.length < 1)
                            return true;
                        npcId = Integer.parseInt(input[0]);
    	            }
    	            catch(Exception e){}
    	            
    	            if(npcId > 0)
    	            {
    	                L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
    	                showAddDropData(activeChar, npcData);
    	            }
                }
                else if(st.countTokens() == 6)
                {
                    try
                    {
                        npcId = Integer.parseInt(st.nextToken());
                        int itemId = Integer.parseInt(st.nextToken());
                        int category = Integer.parseInt(st.nextToken());
                        int min = Integer.parseInt(st.nextToken());
                        int max = Integer.parseInt(st.nextToken());
                        int chance = Integer.parseInt(st.nextToken());
                        
                        addDropData(activeChar, npcId, itemId, min, max, category, chance);
                    }
                    catch(Exception e)
                    {
                        _log.fine("admin_add_drop parements error: " + command);
                    }
                }
                else
                {
                    SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                    sm.addString("Command error:");
                    sm.addString("//add_drop <npc_id> [<item_id> <category> <min> <max> <chance>]");
                    activeChar.sendPacket(sm);
                }
            }
            catch (StringIndexOutOfBoundsException e)
            {
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("Command error:");
                sm.addString("//add_drop <npc_id> [<item_id> <category> <min> <max> <chance>]");
                activeChar.sendPacket(sm);
            }
        }
        else if(command.startsWith("admin_del_drop "))
        {
            int npcId = -1, itemId = -1, category = -1000;
            try
            {
                String[] input = command.substring(15).split(" ");
                if (input.length >= 3)
                {
                    npcId = Integer.parseInt(input[0]);
                    itemId = Integer.parseInt(input[1]);
                    category = Integer.parseInt(input[2]);
                }
            }
            catch(Exception e){}
            
            if(npcId > 0)
            {
                deleteDropData(activeChar, npcId, itemId, category);
            }
            else
            {
                SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                sm.addString("Command error:");
                sm.addString("//del_drop <npc_id> <item_id> <category>");
                activeChar.sendPacket(sm);
            }
        }
        else if(command.startsWith("admin_box_access"))
        {
            L2Object target = activeChar.getTarget();
            String[] players = command.split(" ");
            if (target instanceof L2BoxInstance)
            {
                L2BoxInstance box = (L2BoxInstance) target;
                if (players.length > 1) {
                    boolean access = true;
                    for (int i = 1; i < players.length; i++)
                    {
                        if (players[i].equals("no"))
                        {
                            access = false;
                            continue;
                        }
                        box.grantAccess(players[i],access);
                    }
                }
                else
                {
                	try {
                		SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
                		String msg = "Access:";
                		for (Object p : box.getAccess())
                			msg += " "+(String)p;
                		sm.addString(msg);
                		activeChar.sendPacket(sm);
                	} catch (Exception e) { _log.info("box_access: "+e); }
                }
            }
        }
        
        return true;
    }

    private void editShopItem(L2PcInstance admin, String[] args)
    {
        int tradeListID = Integer.parseInt(args[1]);
        int itemID = Integer.parseInt(args[2]);
        L2TradeList tradeList = TradeController.getInstance().getBuyList(tradeListID);
        
        L2Item item = ItemTable.getInstance().getTemplate(itemID);
        if (tradeList.getPriceForItemId(itemID) < 0)
        {
            return;
        }
        
        if (args.length > 3)
        {
            int price = Integer.parseInt(args[3]);
            int order =  findOrderTradeList(itemID, tradeList.getPriceForItemId(itemID), tradeListID);
            
            tradeList.replaceItem(itemID, Integer.parseInt(args[3]));
            updateTradeList(itemID, price, tradeListID, order);
            
            admin.sendMessage("Updated price for "+item.getName()+" in Trade List "+tradeListID);
            showShopList(admin, tradeListID, 1);
            return;
        }
        
        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        
        TextBuilder replyMSG = new TextBuilder();
        replyMSG.append("<html><title>Merchant Shop Item Edit</title>");
        replyMSG.append("<body>");
        replyMSG.append("<br>Edit an entry in merchantList.");
        replyMSG.append("<br>Editing Item: "+item.getName());
        replyMSG.append("<table>");
        replyMSG.append("<tr><td width=100>Property</td><td width=100>Edit Field</td><td width=100>Old Value</td></tr>");
        replyMSG.append("<tr><td><br></td><td></td></tr>");
        replyMSG.append("<tr><td>Price</td><td><edit var=\"price\" width=80></td><td>"+tradeList.getPriceForItemId(itemID)+"</td></tr>");
        replyMSG.append("</table>");
        replyMSG.append("<center><br><br><br>");
        replyMSG.append("<button value=\"Save\" action=\"bypass -h admin_editShopItem " + tradeListID + " " + itemID + " $price\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        replyMSG.append("<br><button value=\"Back\" action=\"bypass -h admin_showShopList " + tradeListID +" 1\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        replyMSG.append("</center>");
        replyMSG.append("</body></html>");
        
        adminReply.setHtml(replyMSG.toString());        
        admin.sendPacket(adminReply);
    }

    private void delShopItem(L2PcInstance admin, String[] args)
    {
        int tradeListID = Integer.parseInt(args[1]);
        int itemID = Integer.parseInt(args[2]);
        L2TradeList tradeList = TradeController.getInstance().getBuyList(tradeListID);
        
        if (tradeList.getPriceForItemId(itemID) < 0)
        {
            return;
        }

        if (args.length > 3)
        {
        	int order =  findOrderTradeList(itemID, tradeList.getPriceForItemId(itemID), tradeListID);
        	
            tradeList.removeItem(itemID);
            deleteTradeList(tradeListID, order);
            
            admin.sendMessage("Deleted "+ItemTable.getInstance().getTemplate(itemID).getName()+" from Trade List "+tradeListID);
            showShopList(admin, tradeListID, 1);
            return;
        }
        
        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        
        TextBuilder replyMSG = new TextBuilder();
        replyMSG.append("<html><title>Merchant Shop Item Delete</title>");
        replyMSG.append("<body>");
        replyMSG.append("<br>Delete entry in merchantList.");
        replyMSG.append("<br>Item to Delete: "+ItemTable.getInstance().getTemplate(itemID).getName());
        replyMSG.append("<table>");
        replyMSG.append("<tr><td width=100>Property</td><td width=100>Value</td></tr>");
        replyMSG.append("<tr><td><br></td><td></td></tr>");
        replyMSG.append("<tr><td>Price</td><td>"+tradeList.getPriceForItemId(itemID)+"</td></tr>");
        replyMSG.append("</table>");
        replyMSG.append("<center><br><br><br>");
        replyMSG.append("<button value=\"Confirm\" action=\"bypass -h admin_delShopItem " + tradeListID + " " + itemID + " 1\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        replyMSG.append("<br><button value=\"Back\" action=\"bypass -h admin_showShopList " + tradeListID +" 1\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        replyMSG.append("</center>");
        replyMSG.append("</body></html>");
        
        adminReply.setHtml(replyMSG.toString());        
        admin.sendPacket(adminReply);
    }

    private void addShopItem(L2PcInstance admin, String[] args)
    {
        int tradeListID = Integer.parseInt(args[1]);
        
        L2TradeList tradeList = TradeController.getInstance().getBuyList(tradeListID);
        if (tradeList == null)
        {
            admin.sendMessage("TradeList not found!");
            return;
        }
        
        if (args.length > 3)
        {
        	int order = tradeList.getItems().size() + 1; // last item order + 1
            int itemID = Integer.parseInt(args[2]);
            int price = Integer.parseInt(args[3]);
            
            L2ItemInstance newItem = ItemTable.getInstance().createDummyItem(itemID);
            newItem.setPriceToSell(price);
            tradeList.addItem(newItem);
            storeTradeList(itemID, price, tradeListID, order);
            
            admin.sendMessage("Added "+newItem.getItem().getName()+" to Trade List "+tradeList.getListId());
            showShopList(admin, tradeListID, 1);
            return;
        }
        
        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        
        TextBuilder replyMSG = new TextBuilder();
        replyMSG.append("<html><title>Merchant Shop Item Add</title>");
        replyMSG.append("<body>");
        replyMSG.append("<br>Add a new entry in merchantList.");
        replyMSG.append("<table>");
        replyMSG.append("<tr><td width=100>Property</td><td>Edit Field</td></tr>");
        replyMSG.append("<tr><td><br></td><td></td></tr>");
        replyMSG.append("<tr><td>ItemID</td><td><edit var=\"itemID\" width=80></td></tr>");
        replyMSG.append("<tr><td>Price</td><td><edit var=\"price\" width=80></td></tr>");
        replyMSG.append("</table>");
        replyMSG.append("<center><br><br><br>");
        replyMSG.append("<button value=\"Save\" action=\"bypass -h admin_addShopItem " + tradeListID + " $itemID $price\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        replyMSG.append("<br><button value=\"Back\" action=\"bypass -h admin_showShopList " + tradeListID +" 1\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        replyMSG.append("</center>");
        replyMSG.append("</body></html>");
        
        adminReply.setHtml(replyMSG.toString());        
        admin.sendPacket(adminReply);
    }
    
    private void showShopList(L2PcInstance admin, int tradeListID, int page)
    {
        L2TradeList tradeList = TradeController.getInstance().getBuyList(tradeListID);
        if (page > tradeList.getItems().size() / PAGE_LIMIT + 1 || page < 1)
            return;
        
        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        TextBuilder html = itemListHtml(tradeList, page);
        
        adminReply.setHtml(html.toString());        
        admin.sendPacket(adminReply);
        
    }
    
    private TextBuilder itemListHtml(L2TradeList tradeList, int page)
    {
        TextBuilder replyMSG = new TextBuilder();
        
        replyMSG.append("<html><title>Merchant Shop List Page: "+page+"</title>");
        replyMSG.append("<body>");
        replyMSG.append("<br>Edit, add or delete entries in a merchantList.");
        replyMSG.append("<table>");
        replyMSG.append("<tr><td width=150>Item Name</td><td width=60>Price</td><td width=40>Delete</td></tr>");
        int start = ((page-1) * PAGE_LIMIT);
        int end = Math.min(((page-1) * PAGE_LIMIT) + (PAGE_LIMIT-1), tradeList.getItems().size() - 1);
        //System.out.println(end);
        for (L2ItemInstance item : tradeList.getItems(start, end+1))
        {
            replyMSG.append("<tr><td><a action=\"bypass -h admin_editShopItem "+tradeList.getListId()+" "+item.getItemId()+"\">"+item.getItem().getName()+"</a></td>");
            replyMSG.append("<td>"+item.getPriceToSell()+"</td>");
            replyMSG.append("<td><button value=\"Del\" action=\"bypass -h admin_delShopItem "+tradeList.getListId()+" "+item.getItemId()+"\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
            replyMSG.append("</tr>");
        }//*/
        replyMSG.append("<tr>");
        int min = 1;
        int max = tradeList.getItems().size() / PAGE_LIMIT + 1;
        if (page > 1)
        {
            replyMSG.append("<td><button value=\"Page"+(page - 1)+"\" action=\"bypass -h admin_showShopList "+tradeList.getListId()+" "+(page - 1)+"\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        }
        if (page < max)
        {
            if (page <= min)
                replyMSG.append("<td></td>");
            replyMSG.append("<td><button value=\"Page"+(page + 1)+"\" action=\"bypass -h admin_showShopList "+tradeList.getListId()+" "+(page + 1)+"\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
        }
        replyMSG.append("</tr><tr><td>.</td></tr>");
        replyMSG.append("</table>");
        replyMSG.append("<center>");
        replyMSG.append("<button value=\"Add\" action=\"bypass -h admin_addShopItem "+tradeList.getListId()+"\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        replyMSG.append("<button value=\"Close\" action=\"bypass -h admin_close_window\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        replyMSG.append("</center></body></html>");
        
        return replyMSG;
    }

    private void showShop(L2PcInstance admin, int merchantID)
    {
        List<L2TradeList> tradeLists = getTradeLists(merchantID);
        if(tradeLists == null)
        {
        	admin.sendMessage("Unknown npc template ID" + merchantID);
            return ;
        }
        
        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        
        TextBuilder replyMSG = new TextBuilder("<html><title>Merchant Shop Lists</title>");
        replyMSG.append("<body>");
        replyMSG.append("<br>Select a list to view");
        replyMSG.append("<table>");
        replyMSG.append("<tr><td>Mecrchant List ID</td></tr>");
        
        for (L2TradeList tradeList : tradeLists)
        {
            if (tradeList != null)
                replyMSG.append("<tr><td><a action=\"bypass -h admin_showShopList "+tradeList.getListId()+" 1\">Trade List "+tradeList.getListId()+"</a></td></tr>");
        }
        
        replyMSG.append("</table>");
        replyMSG.append("<center>");
        replyMSG.append("<button value=\"Close\" action=\"bypass -h admin_close_window\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        replyMSG.append("</center></body></html>");
        
        adminReply.setHtml(replyMSG.toString());        
        admin.sendPacket(adminReply);
    }
    
    private void storeTradeList(int itemID, int price, int tradeListID, int order)
    {
       	java.sql.Connection con = null;
        try
        {
        	con = L2DatabaseFactory.getInstance().getConnection();
	        PreparedStatement stmt = con.prepareStatement("INSERT INTO merchant_buylists values ("+itemID+","+price+","+tradeListID+","+order+")");
	        stmt.execute();
	        stmt.close();
        }catch (SQLException esql) {esql.printStackTrace();}
    }
    
    private void updateTradeList(int itemID, int price, int tradeListID, int order)
    {
       	java.sql.Connection con = null;
        try
        {
        	con = L2DatabaseFactory.getInstance().getConnection();
           	PreparedStatement stmt = con.prepareStatement("UPDATE merchant_buylists SET `price`='"+price+"' WHERE `shop_id`='"+tradeListID+"' AND `order`='"+order+"'");
	        stmt.execute();
	        stmt.close();
        }catch (SQLException esql) {esql.printStackTrace();}
    }
    
    private void deleteTradeList(int tradeListID, int order)
    {
       	java.sql.Connection con = null;
        try
        {
        	con = L2DatabaseFactory.getInstance().getConnection();
	        PreparedStatement stmt = con.prepareStatement("DELETE FROM merchant_buylists WHERE `shop_id`='"+tradeListID+"' AND `order`='"+order+"'");
	        stmt.execute();
	        stmt.close();
        }catch (SQLException esql) {esql.printStackTrace();}
    }
    
    private int  findOrderTradeList(int itemID, int price, int tradeListID)
    {
       	java.sql.Connection con = null;
       	int order = 0;
		try
        {
        	con = L2DatabaseFactory.getInstance().getConnection();
	        PreparedStatement stmt = con.prepareStatement("SELECT * FROM merchant_buylists WHERE `shop_id`='"+tradeListID+"' AND `item_id` ='"+itemID+"' AND `price` = '"+price+"'");
    	    ResultSet rs = stmt.executeQuery();
    	    rs.first();
    	    return rs.getInt("order");
        }catch (SQLException esql) {esql.printStackTrace();}
        finally{ try {con.close();} catch (SQLException e) {e.printStackTrace();}}
        return order;
    }

    private List<L2TradeList> getTradeLists(int merchantID)
    {
        String target = "npc_%objectId%_Buy";
        
        String content = HtmCache.getInstance().getHtm("data/html/merchant/"+merchantID+".htm");

        if (content == null)
        {
            content = HtmCache.getInstance().getHtm("data/html/merchant/7001.htm");
            
            if (content == null)
                return null;
        }

        List<L2TradeList> tradeLists = new FastList<L2TradeList>();
        
        String[] lines = content.split("\n");
        int pos = 0;
        
        for (String line : lines)
        {
            pos = line.indexOf(target); 
            if (pos >= 0)
            {
                int tradeListID = Integer.decode((line.substring(pos+target.length()+1)).split("\"")[0]);
                //System.out.println(tradeListID);
                tradeLists.add(TradeController.getInstance().getBuyList(tradeListID));
            }
        }
        
        return tradeLists;
    }

    private boolean checkLevel(int level) {
		return (level >= REQUIRED_LEVEL);
	}
    private boolean checkLevel2(int level) {
		return (level >= REQUIRED_LEVEL2);
	}
	
	public String[] getAdminCommandList() {
		return _adminCommands;
	}	
	
	private void Show_Npc_Property(L2PcInstance adminPlayer, L2NpcTemplate npc)
	{
        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        String content = HtmCache.getInstance().getHtm("data/html/admin/editnpc.htm");
	    
	    if (content != null)
        {
            adminReply.setHtml(content);
	        adminReply.replace("%NpcName%", npc.name);
	        adminReply.replace("%CollisionRadius%", String.valueOf(npc.collisionRadius));
	        adminReply.replace("%CollisionHeight%", String.valueOf(npc.collisionHeight));
	        adminReply.replace("%Level%", String.valueOf(npc.level));
	        adminReply.replace("%Sex%", String.valueOf(npc.sex));	        
	        adminReply.replace("%Type%", String.valueOf(npc.type));
	        adminReply.replace("%AttackRange%", String.valueOf(npc.baseAtkRange));
	        adminReply.replace("%MaxHp%", String.valueOf(npc.baseHpMax));
	        adminReply.replace("%MaxBaseMp%", String.valueOf(npc.baseMpMax));
	        adminReply.replace("%Exp%", String.valueOf(npc.revardExp));
	        adminReply.replace("%Sp%", String.valueOf(npc.revardSp));
	        adminReply.replace("%Patk%", String.valueOf(npc.basePAtk));
	        adminReply.replace("%Pdef%", String.valueOf(npc.basePDef));
	        adminReply.replace("%Matk%", String.valueOf(npc.baseMAtk));	        
	        adminReply.replace("%Mdef%", String.valueOf(npc.baseMDef));
	        adminReply.replace("%Atkspd%", String.valueOf(npc.basePAtkSpd));
	        adminReply.replace("%Agro%", String.valueOf(npc.aggroRange));
	        adminReply.replace("%Matkspd%", String.valueOf(npc.baseMAtkSpd));
	        adminReply.replace("%Rhand%", String.valueOf(npc.rhand));
	        adminReply.replace("%Lhand%", String.valueOf(npc.lhand));
	        adminReply.replace("%Armor%", String.valueOf(npc.armor));
	        adminReply.replace("%WalkSpeed%", String.valueOf(npc.baseRunSpd*0.7));
	        adminReply.replace("%RunSpeed%", String.valueOf(npc.baseRunSpd));
	        adminReply.replace("%NpcId%", String.valueOf(npc.npcId));
	    }
        else
	    {
	        adminReply.setHtml("<html><head><body>File not found: data/html/admin/editnpc.htm</body></html>");
	        //do nothing.
	    }
		
	    adminPlayer.sendPacket(adminReply);		
		
	}
	
	private void save_npc_property(String modifications)
	{
	    //System.out.println("- modifications:" + modifications);
	    
//	    L2NpcTemplate npcData = null;//NpcTable.getInstance().getTemplate()
	    StatsSet npcData = new StatsSet();
	    try
	    {
		    StringTokenizer st = new StringTokenizer(modifications, ",");
		    while(st.hasMoreTokens())
		    {
		        StringTokenizer st2 = new StringTokenizer(st.nextToken().trim());
		        if(st2.countTokens() != 2)
		        {
		            continue;
		        }
                String name = st2.nextToken().trim();
                String value = st2.nextToken().trim();
                
                //System.out.println(" - " + name + "=" + value);
                
                if(name.equals("id")){
                    npcData.set("npcId", Integer.parseInt(value));
                } 
                else if(name.equals("CollisionRadius") && npcData != null){
                    npcData.set("collision_radius", Integer.parseInt(value));
                }
                else if(name.equals("CollisionHeight") && npcData != null){
                	npcData.set("collision_height", Integer.parseInt(value));
                }
                else if(name.equals("Level") && npcData != null){
                    npcData.set("level",Integer.parseInt(value));
                }
                else if(name.equals("Sex") && npcData != null){
                    if(value.equals("0")){
                        npcData.set("sex","male");
                    }
                    else
                    {
                        npcData.set("sex","female");
                    }
                }
                else if(name.equals("Type") && npcData != null){
                    npcData.set("type",value);
                }
                else if(name.equals("AttackRange") && npcData != null){
                    npcData.set("attackrange",Integer.parseInt(value));
                }
                else if(name.equals("MaxHp") && npcData != null){
                    npcData.set("hp",Integer.parseInt(value));
                }
                else if(name.equals("MaxBaseMp") && npcData != null){
                    npcData.set("mp",Integer.parseInt(value));
                }
                else if(name.equals("Exp") && npcData != null){
                    npcData.set("exp",Integer.parseInt(value));
                }
                else if(name.equals("Sp") && npcData != null){
                    npcData.set("sp",Integer.parseInt(value));
                }
                else if(name.endsWith("Patk") && npcData != null){
                    npcData.set("patk",Integer.parseInt(value));
                }
                else if(name.endsWith("Pdef") && npcData != null){
                    npcData.set("pdef",Integer.parseInt(value));
                }
                else if(name.endsWith("Matk") && npcData != null){
                    npcData.set("matk",Integer.parseInt(value));
                }
                else if(name.endsWith("Mdef") && npcData != null){
                    npcData.set("mdef",Integer.parseInt(value));
                }
                else if(name.endsWith("Atkspd") && npcData != null){
                    npcData.set("atkspd",Integer.parseInt(value));
                }
                else if(name.endsWith("Agro") && npcData != null){
                	npcData.set("aggro",Integer.parseInt(value));
                }
                else if(name.endsWith("Matkspd") && npcData != null){
                    npcData.set("matkspd",Integer.parseInt(value));
                }
                else if(name.endsWith("Rhand") && npcData != null){
                    npcData.set("rhand",Integer.parseInt(value));
                }
                else if(name.endsWith("Lhand") && npcData != null){
                    npcData.set("lhand",Integer.parseInt(value));
                }
                else if(name.endsWith("Armor") && npcData != null){
                    npcData.set("armor",Integer.parseInt(value));
                }
//		            else if(name.endsWith("WalkSpeed") && npcData != null){
//		                npcData.setWalkSpeed(Integer.parseInt(value));
//		            }
                else if(name.endsWith("RunSpeed") && npcData != null){
                    npcData.set("runspd",Integer.parseInt(value));
                }
		    }
	    }
	    catch(Exception e)
	    {
	        _log.fine("save Npc data error");
	    }
	    
//	    L2NpcTemplate template = new L2NpcTemplate(npcData);
//	    NpcTable.getInstance().replaceTemplate(template);
	    
	    //save the npc data
        NpcTable.getInstance().saveNpc(npcData);
        // and reload
        NpcTable.getInstance().reloadNpc(npcData.getInteger("npcId"));
	}

	private void showNpcDropList(L2PcInstance admin, int npcId)
	{
	    L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
	    if(npcData == null)
	    {
	        SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
	        sm.addString("unknown npc template id" + npcId);
	        admin.sendPacket(sm);
	        return ;
	    }
	    
	    
	    NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
	    
	    TextBuilder replyMSG = new TextBuilder("<html><title>NPC: "+ npcData.name + "("+npcData.npcId+") 's drop manage</title>");
	    replyMSG.append("<body>");
	    replyMSG.append("<br>Notes: click[drop_id]to show the detail of drop data,click[del] to delete the drop data!");
	    replyMSG.append("<table>");
	    replyMSG.append("<tr><td>npc_id itemId category</td><td>item[id]</td><td>type</td><td>del</td></tr>");
	    
	    for(L2DropCategory cat:npcData.getDropData())
		    for(L2DropData drop : cat.getAllDrops())
		    {
		        replyMSG.append("<tr><td><a action=\"bypass -h admin_edit_drop " + npcData.npcId + " " + drop.getItemId()+ " " + cat.getCategoryType() + "\">"
	                    + npcData.npcId + " " + drop.getItemId() + " " + cat.getCategoryType() + "</a></td>" +
	                    "<td>" + ItemTable.getInstance().getTemplate(drop.getItemId()).getName() + "[" + drop.getItemId() + "]" + "</td><td>" + (drop.isQuestDrop()?"Q":(cat.isSweep()?"S":"D")) + "</td><td>" +
	                    "<a action=\"bypass -h admin_del_drop " + npcData.npcId + " " + drop.getItemId() +" "+ cat.getCategoryType() +"\">del</a></td></tr>");
		    }
	    
	    replyMSG.append("</table>");
	    replyMSG.append("<center>");
	    replyMSG.append("<button value=\"Add DropData\" action=\"bypass -h admin_add_drop "+ npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	    replyMSG.append("<button value=\"Close\" action=\"bypass -h admin_close_window\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	    replyMSG.append("</center></body></html>");
	    
	    adminReply.setHtml(replyMSG.toString());	    
	    admin.sendPacket(adminReply);
	    
	}
	
	private void showEditDropData(L2PcInstance admin, int npcId, int itemId, int category)
	{
	    java.sql.Connection con = null;
	    
	    try
	    {
	        con = L2DatabaseFactory.getInstance().getConnection();
	        
	        PreparedStatement statement = con.prepareStatement("SELECT mobId, itemId, min, max, category, chance FROM droplist WHERE mobId=" + npcId + " AND itemId=" + itemId+ " AND category=" + category);
	        ResultSet dropData = statement.executeQuery();
	        
	        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
	        
	        TextBuilder replyMSG = new TextBuilder("<html><title>the detail of dropdata: (" + npcId + " " + itemId + " " + category + ")</title>");
	        replyMSG.append("<body>");
	        
	        if(dropData.next()){
	            replyMSG.append("<table>");
	            replyMSG.append("<tr><td>Appertain of NPC</td><td>"+ NpcTable.getInstance().getTemplate(dropData.getInt("mobId")).name + "</td></tr>");
	            replyMSG.append("<tr><td>ItemName</td><td>"+ ItemTable.getInstance().getTemplate(dropData.getInt("itemId")).getName() + "(" + dropData.getInt("itemId") + ")</td></tr>");
	            replyMSG.append("<tr><td>Category</td><td>"+ ((category==-1)?"sweep":Integer.toString(category)) + "</td></tr>");
	            replyMSG.append("<tr><td>MIN(" + dropData.getInt("min") + ")</td><td><edit var=\"min\" width=80></td></tr>");
	            replyMSG.append("<tr><td>MAX(" + dropData.getInt("max") + ")</td><td><edit var=\"max\" width=80></td></tr>");
	            replyMSG.append("<tr><td>CHANCE("+ dropData.getInt("chance") + ")</td><td><edit var=\"chance\" width=80></td></tr>");
	            replyMSG.append("</table>");
	            
	            replyMSG.append("<center>");
	            replyMSG.append("<button value=\"Save Modify\" action=\"bypass -h admin_edit_drop " + npcId + " " + itemId + " " + category +" $min $max $chance\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	            replyMSG.append("<br><button value=\"DropList\" action=\"bypass -h admin_show_droplist " + dropData.getInt("mobId") +"\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	            replyMSG.append("</center>");
	        }
	        
	        dropData.close();
	        statement.close();
	        
	        replyMSG.append("</body></html>");
		    adminReply.setHtml(replyMSG.toString());
		    
		    admin.sendPacket(adminReply);	        
	    }
	    catch(Exception e){}
	    finally
	    {
	        try { con.close(); } catch (Exception e) {}
	    }
	}
	
	private void showAddDropData(L2PcInstance admin, L2NpcTemplate npcData)
	{	    
        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        
        TextBuilder replyMSG = new TextBuilder("<html><title>Add dropdata to " + npcData.name + "(" + npcData.npcId + ")</title>");
        replyMSG.append("<body>");
        replyMSG.append("<table>");
        replyMSG.append("<tr><td>Item-Id</td><td><edit var=\"itemId\" width=80></td></tr>");
        replyMSG.append("<tr><td>MIN</td><td><edit var=\"min\" width=80></td></tr>");
        replyMSG.append("<tr><td>MAX</td><td><edit var=\"max\" width=80></td></tr>");
        replyMSG.append("<tr><td>CATEGORY(sweep=-1)</td><td><edit var=\"category\" width=80></td></tr>");
        replyMSG.append("<tr><td>CHANCE(0-1000000)</td><td><edit var=\"chance\" width=80></td></tr>");
        replyMSG.append("</table>");

        replyMSG.append("<center>");
        replyMSG.append("<button value=\"SAVE\" action=\"bypass -h admin_add_drop " + npcData.npcId + " $itemId $category $min $max $chance\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        replyMSG.append("<br><button value=\"DropList\" action=\"bypass -h admin_show_droplist " + npcData.npcId +"\"  width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
        replyMSG.append("</center>");
        replyMSG.append("</body></html>");
	    adminReply.setHtml(replyMSG.toString());
	    
	    admin.sendPacket(adminReply);
	}
	
	private void updateDropData(L2PcInstance admin, int npcId, int itemId, int min, int max, int category, int chance)
	{
	    java.sql.Connection con = null;
	    
	    try
	    {
	        con = L2DatabaseFactory.getInstance().getConnection();
	        
	        PreparedStatement statement = con.prepareStatement("UPDATE droplist SET min=?, max=?, chance=? WHERE mobId=? AND itemId=? AND category=?");
	        statement.setInt(1, min);
	        statement.setInt(2, max);
	        statement.setInt(3, chance);
            statement.setInt(4, npcId);
            statement.setInt(5, itemId);
	        statement.setInt(6, category);
	        
	        statement.execute();
	        statement.close();
	        
	        PreparedStatement statement2 = con.prepareStatement("SELECT mobId FROM droplist WHERE mobId=? AND itemId=? AND category=?");
            statement2.setInt(1, npcId);
            statement2.setInt(2, itemId);
	        statement2.setInt(3, category);
	        
	        ResultSet npcIdRs = statement2.executeQuery();
	        if(npcIdRs.next()) npcId = npcIdRs.getInt("mobId");
	        npcIdRs.close();
	        statement2.close();

	        if(npcId > 0)
	        {
	            reLoadNpcDropList(npcId);
	            
		        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		        TextBuilder replyMSG = new TextBuilder("<html><title>Drop data modify complete!</title>");
		        replyMSG.append("<body>");
		        replyMSG.append("<center><button value=\"DropList\" action=\"bypass -h admin_show_droplist "+ npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
		        replyMSG.append("</body></html>");
		        
		        adminReply.setHtml(replyMSG.toString());
		        admin.sendPacket(adminReply);	            
	        }
	        else
	        {
	            SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
	            sm.addString("unknown error!");
	            admin.sendPacket(sm);
	        }
	            
	    }
	    catch(Exception e){ e.printStackTrace(); }
	    finally
	    {
	        try { con.close(); } catch (Exception e) {}
	    }
	    
	    //System.out.println("- updateDropData end");
	}
	
	private void addDropData(L2PcInstance admin, int npcId, int itemId, int min, int max, int category, int chance)
	{
	    java.sql.Connection con = null;
	    
	    try
	    {
	        con = L2DatabaseFactory.getInstance().getConnection();
	        
	        PreparedStatement statement = con.prepareStatement("INSERT INTO droplist(mobId, itemId, min, max, category, chance) values(?,?,?,?,?,?)");
	        statement.setInt(1, npcId);
	        statement.setInt(2, itemId);
	        statement.setInt(3, min);
	        statement.setInt(4, max);
            statement.setInt(5, category);
	        statement.setInt(6, chance);
	        statement.execute();
	        statement.close();
	        
	        reLoadNpcDropList(npcId);
    
	        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
	        TextBuilder replyMSG = new TextBuilder("<html><title>Add drop data complete!</title>");
	        replyMSG.append("<body>");
	        replyMSG.append("<center><button value=\"Continue add\" action=\"bypass -h admin_add_drop "+ npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("<br><br><button value=\"DropList\" action=\"bypass -h admin_show_droplist "+ npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
	        replyMSG.append("</center></body></html>");
	        
	        adminReply.setHtml(replyMSG.toString());
	        admin.sendPacket(adminReply);	        
	    }
	    catch(Exception e){}
	    finally
	    {
	        try { con.close(); } catch (Exception e) {}
	    }	        
	    
	    //System.out.println("- addDropData end");
	}
	
	private void deleteDropData(L2PcInstance admin, int npcId, int itemId, int category)
	{
	    java.sql.Connection con = null;
	    
	    try
	    {
	        con = L2DatabaseFactory.getInstance().getConnection();
	        
	        if(npcId > 0)
	        {
		        PreparedStatement statement2 = con.prepareStatement("DELETE FROM droplist WHERE mobId=? AND itemId=? AND category=?");
                statement2.setInt(1, npcId);
                statement2.setInt(2, itemId);
                statement2.setInt(3, category);
                statement2.execute();
		        statement2.close();
		        
		        reLoadNpcDropList(npcId);
		        
    	        NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
    	        TextBuilder replyMSG = new TextBuilder("<html><title>Delete drop data(" + npcId+", "+ itemId+", "+ category + ")complete</title>");
    	        replyMSG.append("<body>");
    	        replyMSG.append("<center><button value=\"DropList\" action=\"bypass -h admin_show_droplist "+ npcId + "\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");
    	        replyMSG.append("</body></html>");
    	        
    	        adminReply.setHtml(replyMSG.toString());
    	        admin.sendPacket(adminReply);	        		        
		        
	        }
	    }
	    catch(Exception e){}
	    finally
	    {
	        try { con.close(); } catch (Exception e) {}
	    }	        
	    
	}
	
	private void reLoadNpcDropList(int npcId)
	{
        L2NpcTemplate npcData = NpcTable.getInstance().getTemplate(npcId);
        if (npcData == null)
            return;
        
        // reset the drop lists
        npcData.clearAllDropData();
        
        // get the drops
	    java.sql.Connection con = null;
	    try
	    {
	        con = L2DatabaseFactory.getInstance().getConnection();	    
	        L2DropData dropData = null;
	        
	        npcData.getDropData().clear();	            
	        
	        PreparedStatement statement = con.prepareStatement("SELECT " + L2DatabaseFactory.getInstance().safetyString(new String[] {"mobId", "itemId", "min", "max", "category", "chance"}) + " FROM droplist WHERE mobId=?");
	        statement.setInt(1, npcId);
	        ResultSet dropDataList = statement.executeQuery();
	        
	        while(dropDataList.next())
	        {
	            dropData = new L2DropData();
	            
				dropData.setItemId(dropDataList.getInt("itemId"));
				dropData.setMinDrop(dropDataList.getInt("min"));
				dropData.setMaxDrop(dropDataList.getInt("max"));
				dropData.setChance(dropDataList.getInt("chance"));

		        int category = dropDataList.getInt("category");
		        npcData.addDropData(dropData, category);
	        }
	        dropDataList.close();
	        statement.close();
	    }
	    catch(Exception e){}
	    finally
	    {
	        try { con.close(); } catch (Exception e) {}
	    }	         
	}
}
