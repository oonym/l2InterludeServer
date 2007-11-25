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
package net.sf.l2j.gameserver.handler;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 *
 * @version $Revision: 1.1.4.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class AdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminCommandHandler.class.getName());

	private static AdminCommandHandler _instance;

	private Map<String, IAdminCommandHandler> _datatable;

    //Alt privileges setting
    private static Logger _priviLog = Logger.getLogger("AltPrivilegesAdmin");
    private static FastMap<String,Integer> _privileges;

	public static AdminCommandHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new AdminCommandHandler();
		}
		return _instance;
	}

	private AdminCommandHandler()
	{
		_datatable = new FastMap<String, IAdminCommandHandler>();
	}

	public void registerAdminCommandHandler(IAdminCommandHandler handler)
	{
		String[] ids = handler.getAdminCommandList();
		for (int i = 0; i < ids.length; i++)
		{
			if (Config.DEBUG) _log.fine("Adding handler for command "+ids[i]);
			_datatable.put(ids[i], handler);
		}
	}

	public IAdminCommandHandler getAdminCommandHandler(String adminCommand)
	{
		String command = adminCommand;
		if (adminCommand.indexOf(" ") != -1) {
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		}
		if (Config.DEBUG)
			_log.fine("getting handler for command: "+command+
					" -> "+(_datatable.get(command) != null));
		return _datatable.get(command);
	}

    /**
     * @return
     */
    public int size()
    {
        return _datatable.size();
    }

    public final boolean checkPrivileges(L2PcInstance player, String adminCommand)
    {
        //Only a GM can execute a admin command
        if (!player.isGM())
            return false;

        //Skip special privileges handler?
        if (!Config.ALT_PRIVILEGES_ADMIN || Config.EVERYBODY_HAS_ADMIN_RIGHTS)
            return true;

        if (_privileges == null)
            _privileges = new FastMap<String,Integer>();

        String command = adminCommand;
        if (adminCommand.indexOf(" ") != -1) {
            command = adminCommand.substring(0, adminCommand.indexOf(" "));
        }

        //The command not exists
        if (!_datatable.containsKey(command))
            return false;

        int requireLevel = 0;

        if (!_privileges.containsKey(command))
        {
            //Try to loaded the command config
            boolean isLoaded = false;

            try
            {
                Properties Settings   = new Properties();
                InputStream is          = new FileInputStream(Config.COMMAND_PRIVILEGES_FILE);
                Settings.load(is);
                is.close();

                String stringLevel = Settings.getProperty(command);

                if (stringLevel != null)
                {
                    isLoaded = true;
                    requireLevel = Integer.parseInt(stringLevel);
                }
            }
            catch (Exception e) { }

            //Secure level?
            if (!isLoaded)
            {
                if (Config.ALT_PRIVILEGES_SECURE_CHECK)
                {
                    _priviLog.info("The command '" + command + "' haven't got a entry in the configuration file. The command cannot be executed!!");
                    return false;
                }

                requireLevel = Config.ALT_PRIVILEGES_DEFAULT_LEVEL;
            }

            _privileges.put(command,requireLevel);
        }
        else
        {
            requireLevel = _privileges.get(command);
        }

        if (player.getAccessLevel() < requireLevel)
        {
            _priviLog.warning("<GM>" + player.getName() + ": have not access level to execute the command '" + command +"'");
            return false;
        }

        return true;
    }
}
