package net.sf.l2j.gameserver.model.actor.instance;

import java.util.logging.Logger;

import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

/**
 * @author  l3x
 */
public class L2CastleBlacksmithInstance extends L2FolkInstance {
	private static Logger _log = Logger.getLogger(L2CastleChamberlainInstance.class.getName());

	protected static final int COND_ALL_FALSE = 0;
	protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	protected static final int COND_OWNER = 2;

    public L2CastleBlacksmithInstance(int objectId, L2NpcTemplate template) {
        super(objectId, template);
    }
    
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
                // player.setCurrentState(L2Character.STATE_INTERACT);
                // player.setInteractTarget(this);
                // player.moveTo(this.getX(), this.getY(), this.getZ(), INTERACTION_DISTANCE);

                // Notify the L2PcInstance AI with AI_INTENTION_INTERACT
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);                    
                
                // Send a Server->Client packet ActionFailed (target is out of interaction range) to the L2PcInstance player
                player.sendPacket(new ActionFailed());
            } else {
            	if (CastleManorManager.getInstance().isDisabled()) {
            		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
					html.setFile("data/html/npcdefault.htm");
					html.replace("%objectId%", String.valueOf(getObjectId()));
					html.replace("%npcname%", getName());
					player.sendPacket(html);
            	} else {
            		showMessageWindow(player, 0);
            	}

                // Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
                player.sendPacket(new ActionFailed());                  
                // player.setCurrentState(L2Character.STATE_IDLE);
            }
        }
    }
    
    public void onBypassFeedback(L2PcInstance player, String command) {
        player.sendPacket( new ActionFailed() );
        
        if (CastleManorManager.getInstance().isDisabled()) {
    		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile("data/html/npcdefault.htm");
			html.replace("%objectId%", String.valueOf(getObjectId()));
			html.replace("%npcname%", getName());
			player.sendPacket(html);
			return;
    	}

        int condition = validateCondition(player);
        if (condition <= COND_ALL_FALSE)
            return;

        if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
            return;
        else if (condition == COND_OWNER) {
        	if (command.startsWith("Chat")) {
                int val = 0;
                try {
                   val = Integer.parseInt(command.substring(5));
                } catch (IndexOutOfBoundsException ioobe) {
                } catch (NumberFormatException nfe) {}
                showMessageWindow(player, val);
            } else {
            	super.onBypassFeedback(player, command);
            }
        }
    }
    
    private void showMessageWindow(L2PcInstance player, int val) {
        player.sendPacket( new ActionFailed() );
        String filename = "data/html/castleblacksmith/castleblacksmith-no.htm";
        
        int condition = validateCondition(player);
        if (condition > COND_ALL_FALSE) {
            if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
                filename = "data/html/castleblacksmith/castleblacksmith-busy.htm";			// Busy because of siege
            else if (condition == COND_OWNER) {												// Clan owns castle
            	if (val == 0) 
            		filename = "data/html/castleblacksmith/castleblacksmith.htm";				
            	else
            		filename = "data/html/castleblacksmith/castleblacksmith-" + val + ".htm";
            }
                
        }
        
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
        html.replace("%castleid%", Integer.toString(getCastle().getCastleId()));
        player.sendPacket(html);
    }
    
    protected int validateCondition(L2PcInstance player) {   
        if (player.isGM()) return COND_OWNER;
        if (getCastle() != null && getCastle().getCastleId() > 0) {
            if (player.getClan() != null) {
                if (getCastle().getSiege().getIsInProgress())
                    return COND_BUSY_BECAUSE_OF_SIEGE;                                      // Busy because of siege
                else if (getCastle().getOwnerId() == player.getClanId()                     // Clan owns castle
                        && player.isClanLeader())                                           // Leader of clan
                    return COND_OWNER;  // Owner
            }
        }
        return COND_ALL_FALSE;
    }
}
