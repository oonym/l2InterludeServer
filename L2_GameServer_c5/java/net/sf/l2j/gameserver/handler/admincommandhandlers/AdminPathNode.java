package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class AdminPathNode implements IAdminCommandHandler
{
    private static String[] _adminCommands = {
        "admin_pn_info",
        "admin_show_path",
        "admin_path_debug",
        "admin_show_pn",
        "admin_find_path",
    };
    private static final int REQUIRED_LEVEL = Config.GM_CREATE_NODES;
    
    public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        if (!Config.ALT_PRIVILEGES_ADMIN)
            if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM())) return false;
        //Config.NEW_NODE_ID
    	if(command.equals("admin_pn_info"))
        {
    		
        }
    	else if(command.equals("admin_show_path"))
        {
    		
        }
    	else if(command.equals("admin_path_debug"))
        {
    		
        }
    	else if(command.equals("admin_show_pn"))
        {
    		
        }
    	else if(command.equals("admin_find_path"))
        {
    		
        }
        return true;
    }
    
    public String[] getAdminCommandList() {
        return _adminCommands;
    }
    
    private boolean checkLevel(int level) {
        return (level >= REQUIRED_LEVEL);
    }    
}
