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

import java.util.StringTokenizer;

import javolution.util.FastList;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.SeedProduction;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.BuyList;
import net.sf.l2j.gameserver.serverpackets.BuyListSeed;
import net.sf.l2j.gameserver.serverpackets.ExShowCropInfo;
import net.sf.l2j.gameserver.serverpackets.ExShowManorDefaultInfo;
import net.sf.l2j.gameserver.serverpackets.ExShowProcureCropDetail;
import net.sf.l2j.gameserver.serverpackets.ExShowSeedInfo;
import net.sf.l2j.gameserver.serverpackets.ExShowSellCropList;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2ManorManagerInstance extends L2MerchantInstance {

	//private static Logger _log = Logger.getLogger(L2ManorManagerInstance.class.getName());

    public L2ManorManagerInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    @Override
	public void onAction(L2PcInstance player) {
    	player.setLastFolkNPC(this);

        // Check if the L2PcInstance already target the L2NpcInstance
        if (this != player.getTarget()) {
            // Set the target of the L2PcInstance player
            player.setTarget(this);

            // Send a Server->Client packet MyTargetSelected to the L2PcInstance player
            // The player.getLevel() - getLevel() permit to display the correct color in the select window
            MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
            player.sendPacket(my);

            // Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
            player.sendPacket(new ValidateLocation(this));
        } else {
            // Send a Server->Client packet MyTargetSelected to the L2PcInstance player
            // The player.getLevel() - getLevel() permit to display the correct color
            MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
            player.sendPacket(my);

            // Calculate the distance between the L2PcInstance and the L2NpcInstance
            if (!isInsideRadius(player, INTERACTION_DISTANCE, false, false)) {
                // Notify the L2PcInstance AI with AI_INTENTION_INTERACT
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);

                // Send a Server->Client packet ActionFailed (target is out of interaction range) to the L2PcInstance player
                player.sendPacket(new ActionFailed());
            } else {
            	// If player is a lord of this manor, alternative message from NPC
            	if (CastleManorManager.getInstance().isDisabled()) {
                	NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/npcdefault.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", getName());
					player.sendPacket(html);
                } else if (!player.isGM()									 		// Player is not GM
                		&& getCastle() != null && getCastle().getCastleId() > 0 	// Verification of castle
                		&& player.getClan() != null 							 	// Player have clan
                		&& getCastle().getOwnerId() == player.getClanId()  		    // Player's clan owning the castle
                		&& player.isClanLeader() 									// Player is clan leader of clan (then he is the lord)
                		) {
                		showMessageWindow(player, "manager-lord.htm");
                } else {
                	showMessageWindow(player, "manager.htm");
                }
                // Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
                player.sendPacket(new ActionFailed());
                // player.setCurrentState(L2Character.STATE_IDLE);
            }
        }
    }

    private void showBuyWindow(L2PcInstance player, String val) {
        double taxRate = 0;
        player.tempInvetoryDisable();

        L2TradeList list = TradeController.getInstance().getBuyList(Integer.parseInt(val));

        if (list != null ) {
            list.getItems().get(0).setCount(1);
        	BuyList bl = new BuyList(list, player.getAdena(), taxRate);
            player.sendPacket(bl);
        } else {
            _log.info("possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
            _log.info("buylist id:" + val);
        }

        player.sendPacket(new ActionFailed());
    }

    @Override
	public void onBypassFeedback(L2PcInstance player, String command) 
    {
		// BypassValidation Exploit plug.
		if (player.getLastFolkNPC().getObjectId() != this.getObjectId())
			return;
		
    	if (command.startsWith("manor_menu_select")) 
    	{ 
    		// input string format:
			// manor_menu_select?ask=X&state=Y&time=X
    		
			if (CastleManorManager.getInstance().isUnderMaintenance()) 
			{
				player.sendPacket(new ActionFailed());
				player.sendPacket(new SystemMessage(SystemMessageId.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE));
				return;
			}

			String params = command.substring(command.indexOf("?")+1);
			StringTokenizer st = new StringTokenizer(params, "&");
			int ask   = Integer.parseInt(st.nextToken().split("=")[1]);
			int state = Integer.parseInt(st.nextToken().split("=")[1]);
			int time  = Integer.parseInt(st.nextToken().split("=")[1]);

			int castleId;
			if (state == -1) // info for current manor
				castleId = getCastle().getCastleId();
			else 			 // info for requested manor
				castleId = state;

			switch (ask) { // Main action
			case 1: // Seed purchase
				if (castleId != getCastle().getCastleId()) {
					player.sendPacket(new SystemMessage(SystemMessageId.HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR));
				} else {
					L2TradeList tradeList = new L2TradeList(0);
					FastList<SeedProduction> seeds = getCastle().getSeedProduction(CastleManorManager.PERIOD_CURRENT);

					for (SeedProduction s : seeds) {
						L2ItemInstance item = ItemTable.getInstance().createDummyItem(s.getId());
						item.setPriceToSell(s.getPrice());
						item.setCount(s.getCanProduce());
						if ((item.getCount() > 0) && (item.getPriceToSell() > 0))
							tradeList.addItem(item);
					}

					BuyListSeed bl = new BuyListSeed(tradeList, castleId, player.getAdena());
					player.sendPacket(bl);
				}
				break;
			case 2: // Crop sales
				player.sendPacket(new ExShowSellCropList(player, castleId, getCastle().getCropProcure(CastleManorManager.PERIOD_CURRENT)));
				break;
			case 3: // Current seeds (Manor info)
				if (time == 1 && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
					player.sendPacket(new ExShowSeedInfo(castleId, null));
				else
					player.sendPacket(new ExShowSeedInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getSeedProduction(time)));
				break;
			case 4: // Current crops (Manor info)
				if (time == 1 && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
					player.sendPacket(new ExShowCropInfo(castleId, null));
				else
					player.sendPacket(new ExShowCropInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getCropProcure(time)));
				break;
			case 5: // Basic info (Manor info)
				player.sendPacket(new ExShowManorDefaultInfo());
				break;
			case 6: // Buy harvester
				this.showBuyWindow(player, "3" + getNpcId());
				break;
			case 9: // Edit sales (Crop sales)
				player.sendPacket(new ExShowProcureCropDetail(state));
				break;
			}
		} else if (command.startsWith("help")) {
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // discard first
			String filename = "manor_client_help00" + st.nextToken() + ".htm";
			showMessageWindow(player, filename);
		} else {
			super.onBypassFeedback(player, command);
		}
	}

    public String getHtmlPath () {
    	return "data/html/manormanager/";
    }

    @Override
    public String getHtmlPath(int npcId, int val) {
		return "data/html/manormanager/manager.htm"; // Used only in parent method
													 // to return from "Territory status"
												     // to initial screen.
	}

	private void showMessageWindow(L2PcInstance player, String filename) {
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(getHtmlPath() + filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}
