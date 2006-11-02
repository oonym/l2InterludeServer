package net.sf.l2j.gameserver.model.actor.instance;

import java.util.List;
import java.util.StringTokenizer;

import javolution.lang.TextBuilder;

import net.sf.l2j.gameserver.ItemTable;
import net.sf.l2j.gameserver.model.CropProcure;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.WareHouseDepositList;
import net.sf.l2j.gameserver.serverpackets.WareHouseWithdrawalList;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * Castle Chamberlains implementation
 * used for:
 * - tax rate control
 * - regional manor system control
 * - castle treasure control
 * - ...
 */
public class L2CastleChamberlainInstance extends L2FolkInstance
{
    //private static Logger _log = Logger.getLogger(L2CastleChamberlainInstance.class.getName());

	protected static int Cond_All_False = 0;
	protected static int Cond_Busy_Because_Of_Siege = 1;
	protected static int Cond_Owner = 2;

    public L2CastleChamberlainInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }
    
    public void onBypassFeedback(L2PcInstance player, String command)
    {
		player.sendPacket( new ActionFailed() );

		int condition = validateCondition(player);
		if (condition <= Cond_All_False)
            return;

		if (condition == Cond_Busy_Because_Of_Siege)
            return;
		else if (condition == Cond_Owner)
		{
	        StringTokenizer st = new StringTokenizer(command, " ");
            String actualCommand = st.nextToken(); // Get actual command

	        String val = "";
	        if (st.countTokens() >= 1) {val = st.nextToken();}
	 
			if (actualCommand.equalsIgnoreCase("banish_foreigner"))
            {
                getCastle().banishForeigner();                                                      // Move non-clan members off castle area
                return;
            }
			else if (actualCommand.equalsIgnoreCase("list_siege_clans"))
            {
                getCastle().getSiege().listRegisterClan(player);                                    // List current register clan
                return;
            }
			else if (actualCommand.equalsIgnoreCase("manage_siege_defender"))
			{
                getCastle().getSiege().listRegisterClan(player);
                return;
			}
			else if(actualCommand.equalsIgnoreCase("manage_vault"))
			{
	            if (val.equalsIgnoreCase("deposit"))
                    showVaultWindowDeposit(player);
	            else if (val.equalsIgnoreCase("withdraw"))
                    showVaultWindowWithdraw(player);
	            else
	            {
                    TextBuilder msg = new TextBuilder("<html><body>");
			    	msg.append("%npcname%:<br>");
			    	msg.append("Manage Vault<br>");
			    	msg.append("<table width=200>");
			    	msg.append("<tr><td><a action=\"bypass -h npc_%objectId%_manage_vault deposit\">Deposit Item</a></td></tr>");
			    	msg.append("<tr><td><a action=\"bypass -h npc_%objectId%_manage_vault withdraw\">Withdraw Item</a></td></tr>");
			    	msg.append("</table>");
			        msg.append("</body></html>");

			        this.sendHtmlMessage(player, msg.toString());
	            }
                return;
			}
	        else if(actualCommand.equalsIgnoreCase("manor")) // manor control
	        {
	            int cmd = Integer.parseInt(st.nextToken());
	            switch(cmd)
	            {
	                //TODO uncomment this with manor system commit
	                case 1: // view/edit manor stats
	                    //showChatWindow(player,L2Manor.getInstance().getCropReward(_castle));
                        showManorProcure(player);
	                    break;
	                /*
	                case 2: // set reward type
	                    int cropId = Integer.parseInt(st.nextToken());
	                    int reward = Integer.parseInt(st.nextToken());
	                    L2World.getInstance().getCastle(_castle).setRewardType(cropId,reward);
	                    break;
	                case 3: // edit reward
	                    int crop = Integer.parseInt(st.nextToken());
	                    int currentReward = Integer.parseInt(st.nextToken());
	                    /msg = new NpcHtmlMessage(1);
	                    msg.setHtml(pageEditManor(crop,currentReward));
	                    player.sendPacket(msg);
	                    player.sendPacket(new ActionFailed());
	                    break;
	                */
	                default:
	                    //_log.info("Invalid bypass for manor control: "+command+" by "+player.getName()+", hack?");
	            }
                return;
	        }
	        else if(actualCommand.equalsIgnoreCase("operate_door")) // door control
	        {
	            if (val != "")
	            {
		            boolean open = (Integer.parseInt(val) == 1);
		            while (st.hasMoreTokens())
		           	{
		           	    getCastle().openCloseDoor(player, Integer.parseInt(st.nextToken()), open);
		           	}
	            }

	            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
	    		html.setFile("data/html/chamberlain/" + getTemplate().npcId + "-d.htm");
	    		html.replace("%objectId%", String.valueOf(getObjectId()));
	    		html.replace("%npcname%", getName());
	    		player.sendPacket(html);
                return;
	        }
	        else if(actualCommand.equalsIgnoreCase("tax_set")) // tax rates control
	        {
	            if (val != "")
	                getCastle().setTaxPercent(player, Integer.parseInt(val));

                TextBuilder msg = new TextBuilder("<html><body>");
	        	msg.append(getName() + ":<br>");
	        	msg.append("Current tax rate: " + getCastle().getTaxPercent() + "%<br>");
	        	msg.append("<table>");
	        	msg.append("<tr>");
	        	msg.append("<td>Change tax rate to:</td>");
	        	msg.append("<td><edit var=\"value\" width=40><br>");
	        	msg.append("<button value=\"Adjust\" action=\"bypass -h npc_%objectId%_tax_set $value\" width=80 height=15></td>");
	        	msg.append("</tr>");
	        	msg.append("</table>");
	        	msg.append("</center>");
	        	msg.append("</body></html>");

		        this.sendHtmlMessage(player, msg.toString());
                return;
	        }
		}

        super.onBypassFeedback(player, command);
    }

    public void onAction(L2PcInstance player)
	{
        player.sendPacket(new ActionFailed());
		player.setTarget(this);
		player.sendPacket(new MyTargetSelected(getObjectId(), -15));

        if (isInsideRadius(player, INTERACTION_DISTANCE, false, false))
            showMessageWindow(player);
	}
    
    @SuppressWarnings("unused")
    private String pageEditManor(int crop, int current)
    {
        String text = "<html><body><table width=270 bgcolor=\"111111\">";
        text += "<tr><td>Crop</td><td>Type I reward</td><td>Type II reward</td></tr>";
        text += "</table><table width=270>";
        /* 2be uncommented with manor system commit
        String cropName = ItemTable.getInstance().getTemplate(crop).getName();
        String reward1Name = ItemTable.getInstance().getTemplate(L2Manor.getInstance().getRewardItem(crop,1)).getName();
        String reward2Name = ItemTable.getInstance().getTemplate(L2Manor.getInstance().getRewardItem(crop,2)).getName();
        int reward1Amount = L2Manor.getInstance().getRewardAmount(crop,1);
        int reward2Amount = L2Manor.getInstance().getRewardAmount(crop,2);
        
        text += "<tr><td><font color=\"LEVEL\">"+cropName+"</font></td><td><a action=\"bypass -h npc_"+getObjectId()+"_manor_2 "+crop+" 1\">"+reward1Name+"/"+reward1Amount+"</a></td><td><a action=\"bypass -h npc_"+getObjectId()+"_manor_2 "+crop+" 2\">"+reward2Name+"/"+reward2Amount+"</a></td></tr>";
        */
        text += "</table></body></html>";
        return text;
    }
    
    private void showManorProcure(L2PcInstance player)
    {
        if (getCastle() == null)
            return;

        List<CropProcure> crops = getCastle().getManorRewards();
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        String r="";

        for(CropProcure crop : crops)
        {
            r += "<tr><td>" + ItemTable.getInstance().getTemplate(crop.getId()).getName() + "</td>";
            r += "<td>" + crop.getAmount() + "</td>";
            r += "<td>" + crop.getReward() + "</td></tr>";
        }
        
        html.setFile("data/html/chamberlain/chamberlain-manor-procure.htm");
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        html.replace("%table%", r);
        player.sendPacket(html);
    }

    private void sendHtmlMessage(L2PcInstance player, String htmlMessage)
    {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setHtml(htmlMessage);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
        player.sendPacket(html);
    }
    
    private void showMessageWindow(L2PcInstance player)
	{
		player.sendPacket( new ActionFailed() );
		String filename = "data/html/chamberlain/chamberlain-no.htm";
		
		int condition = validateCondition(player);
		if (condition > Cond_All_False)
		{
	        if (condition == Cond_Busy_Because_Of_Siege)
	            filename = "data/html/chamberlain/chamberlain-busy.htm";					// Busy because of siege
	        else if (condition == Cond_Owner)												// Clan owns castle
	            filename = "data/html/chamberlain/chamberlain.htm";							// Owner message window
		}
		
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private void showVaultWindowDeposit(L2PcInstance player)
	{
		player.sendPacket(new ActionFailed());
		player.setActiveWarehouse(player.getClan().getWarehouse());
        player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.Clan)); //Or Castle ??
	}

	private void showVaultWindowWithdraw(L2PcInstance player)
	{
		player.sendPacket(new ActionFailed());
		player.setActiveWarehouse(player.getClan().getWarehouse());
        player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.Clan)); //Or Castle ??
	}
	
	protected int validateCondition(L2PcInstance player)
	{
		if (getCastle() != null && getCastle().getCastleId() > 0)
		{
		    if (player.getClan() != null)
		    {
		        if (getCastle().getSiege().getIsInProgress())
		            return Cond_Busy_Because_Of_Siege;										// Busy because of siege
		        else if (getCastle().getOwnerId() == player.getClanId()						// Clan owns castle
		                && player.isClanLeader())			                                // Leader of clan
		            return Cond_Owner;	// Owner
		    }
		}
		
		return Cond_All_False;
	}
}
