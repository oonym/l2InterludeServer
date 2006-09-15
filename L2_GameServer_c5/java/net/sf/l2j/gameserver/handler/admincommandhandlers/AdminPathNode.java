package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.waypoint.WayPointNode;
import net.sf.l2j.gameserver.serverpackets.SystemMessage;

public class AdminPathNode implements IAdminCommandHandler
{
    private static String[] _adminCommands = {
        "admin_np",
        "admin_new_point",
        "admin_ep",
        "admin_edit_point",
        "admin_dp",
        "admin_delete_point",
        "admin_lp",
        "admin_link_points",
        "admin_up",
        "admin_unlink_points",
        "admin_vp",
        "admin_view_points",
        "admin_sp",
        "admin_save_points",
        "admin_op",
        "admin_open_points",
        "admin_cp",
        "admin_clear_points",
        "admin_tp",
        "admin_target_point",
        "admin_pn",
        "admin_path_nodes",
        "admin_lid"
    };
    private static final int REQUIRED_LEVEL = Config.GM_CREATE_NODES;
    
    public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
        
    	if(isCommand(command, "pn", "path_nodes"))
        {
    		activeChar.togglePathNodeMode();
            String value = "";
            if (activeChar.isPathNodeModeActive())
            {
                value = "On";
                activeChar.sendPacket(SystemMessage.sendString("Please ensure NpcID: "+Config.NEW_NODE_ID+" has Type L2Deco."));
            }
            else
            {
                value = "Off";
            }
            activeChar.sendPacket(SystemMessage.sendString("Path Node Mode switched " + value));
        }
    	else if (activeChar.isPathNodeModeActive())
    	{
    		if(isCommand(command, "np", "new_point"))
    		{
    			activeChar.addPathNodePoint();
                activeChar.sendPacket(SystemMessage.sendString("Path Node Added."));
    		}
    		else if(isCommand(command, "ep", "edit_point"))
    		{
    			L2Object target	= activeChar.getTarget();
    			if (target.isMarker())
    			{
    				//showEditPointMenu(activeChar, (WayPointNode)target);
    			}
    			else
    			{
                    activeChar.sendPacket(SystemMessage.sendString("Target is not a Path Node."));
    			}
    		}
    		else if(isCommand(command, "dp", "delete_point"))
    		{
    			activeChar.removePathNodePoint();
    		}
    		else if(isCommand(command, "lp", "link_points"))
    		{
    			L2Object target	= activeChar.getTarget();
                
                if (target == null || activeChar == null || activeChar.getSelectedNode() == null)
                    return true;
                
                if (target.equals(activeChar.getSelectedNode()))
                {
                    activeChar.sendPacket(SystemMessage.sendString("Target is an Invalid Path Node."));
                }
                
    			if (activeChar.getSelectedNode() != null && target.isMarker())
    			{
    				activeChar.addLink((WayPointNode)target);
                    activeChar.sendPacket(SystemMessage.sendString("Added Path Node Link."));
    			}
    			else
    			{
                    activeChar.sendPacket(SystemMessage.sendString("Target is not a Path Node."));
    			}
    		}
    		else if(isCommand(command, "up", "unlink_points"))
    		{
    			L2Object target	= activeChar.getTarget();
                
    			if (activeChar.getSelectedNode() != null && target.isMarker())
    			{
    				activeChar.removeLink((WayPointNode)target);
                    activeChar.sendPacket(SystemMessage.sendString("Removed Path Node Link."));
    			}
    			else
    			{
                    activeChar.sendPacket(SystemMessage.sendString("Target is not a Path Node."));
    			}
    		}
    		else if(isCommand(command, "vp", "view_points"))
    		{
    			activeChar.toggleViewPathNodes();
    		}
    		else if(isCommand(command, "sp", "save_points"))
    		{
    			String[] parts = command.split(" ");
                if (parts.length > 1)
                {
                    activeChar.savePathNodes(parts[1]);
                }
    		}
    		else if(isCommand(command, "op", "open_points"))
    		{
    			String[] parts = command.split(" ");
                if (parts.length > 1)
                {
                    activeChar.loadPathNodes(parts[1]);
                }
    		}
    		else if(isCommand(command, "cp", "clear_points"))
    		{
    			activeChar.clearPathNodes();
                activeChar.sendPacket(SystemMessage.sendString("Path Nodes Cleared."));
    		}
    		else if(isCommand(command, "tp", "target_point"))
    		{
    			L2Object target	= activeChar.getTarget();
    			if (target.isMarker())
    			{
    				activeChar.setSelectedNode((WayPointNode)target);
                    activeChar.sendPacket(SystemMessage.sendString("Set Path Node Target."));
    			}
    			else
    			{
                    activeChar.sendPacket(SystemMessage.sendString("Target is not a Path Node."));
    			}
    		}
            else if(isCommand(command, "lid"))
            {
                String[] parts = command.split(" ");
                if (parts.length >= 2)
                {
                    WayPointNode.setLineId(Integer.parseInt(parts[1]));
                    activeChar.sendMessage("ID = "+WayPointNode.LINE_ID);
                    activeChar.refreshLinks();
                    activeChar.sendMessage("Refreshed");
                }
            }
    	}
    	else
    	{
            activeChar.sendPacket(SystemMessage.sendString("PathNode Mode is not Active. Use //pn to toggle."));
    	}
        return true;
    }

	private boolean isCommand(String userCommand, String... commandList)
    {
    	for (String command : commandList)
    	{
    		if (userCommand.startsWith("admin_"+command))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public String[] getAdminCommandList() {
        return _adminCommands;
    }
    
    private boolean checkLevel(int level) {
        return (level >= REQUIRED_LEVEL);
    }    
}
