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
package net.sf.l2j.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.Server;
import net.sf.l2j.status.Status;

/**
 * This class ...
 * 
 * @version $Revision: 1.9.4.4 $ $Date: 2005/03/27 15:30:09 $
 */
public class LoginServer extends FloodProtectedListener
{
    private static LoginServer _instance;
    
    private String					_externalHostname;
    private String					_internalHostname;
    public static Status			statusServer;
    public static GameServerTable	gameservertable;
    public LoginController			loginController;

	private GameServerListener _gslistener;

    public static int PROTOCOL_REV = 0x0102;

    static Logger           _log = Logger.getLogger(LoginServer.class.getName());

    public static void main(String[] args) throws Exception
    {
    	Server.SERVER_MODE = Server.MODE_LOGINSERVER;
//      Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME   = "./log.cfg"; // Name of log file
		
		/*** Main ***/
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER); 
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		InputStream is =  new FileInputStream(new File(LOG_NAME));  
		LogManager.getLogManager().readConfiguration(is);
		is.close();
		
		// Initialize config and l2 db factory
		Config.load();
		L2DatabaseFactory.getInstance();
		
		gameservertable = GameServerTable.getInstance();
        LoginServer server = LoginServer.getInstance();
        _log.config("Stand Alone LoginServer Listening on port " + Config.PORT_LOGIN);
        server.start();
        
        if ( Config.IS_TELNET_ENABLED ) {
		    statusServer = new Status(Server.SERVER_MODE);
		    statusServer.start();
		}
		else {
		    System.out.println("Telnet server is currently disabled.");
		}
    }

    public void shutdown(boolean restart)
    {
        this.interrupt();
        _gslistener.interrupt();
		gameservertable.shutDown();
		close();
        if(restart)
        {
        	Runtime.getRuntime().exit(2);
        }
        else
        {
        	Runtime.getRuntime().exit(0);
        }
    }

    private LoginServer() throws IOException
    {
        super(Config.GAME_SERVER_LOGIN_HOST, Config.PORT_LOGIN);

        _externalHostname = Config.EXTERNAL_HOSTNAME;
        if (_externalHostname == null)
        {
            _externalHostname = "localhost";
        }

        _internalHostname = Config.INTERNAL_HOSTNAME; //"InternalHostname");
        if (_internalHostname == null)
        {
            _internalHostname = "localhost";
        }

        _log.config("Hostname for external connections is: " + _externalHostname);
        _log.config("Hostname for internal connections is: " + _internalHostname);

        loginController = LoginController.getInstance();
        
        _gslistener = GameServerListener.getInstance();
        _gslistener.start();
        

        /*
         * ArrayList<String> bannedIpList = new ArrayList<String>();
         * Properties bannedIpFile = new Properties(); try {
         * bannedIpFile.loadFromXML(getClass().getResourceAsStream(Config.BANNED_IP_XML)); }
         * catch (InvalidPropertiesFormatException e) { e.printStackTrace(); }
         * catch (IOException e) { e.printStackTrace(); }
         * bannedIpFile.setProperty(loginIp, createAccounts);
         * bannedIpList.toString()
         */
        InputStream bannedFile = null;
        
        try
        {
            bannedFile =  new FileInputStream(new File("./banned_ip.cfg"));
            if (bannedFile != null)
            {
                int count = 0;
                InputStreamReader reader = new InputStreamReader(bannedFile);
                LineNumberReader lnr = new LineNumberReader(reader);
                String line = null;
                while ((line = lnr.readLine()) != null)
                {
                    line = line.trim();
                    if (line.length() > 0)
                    {
                        count++;
                        ClientThread.addBannedIP(line);
                    }
                }

                _log.info(count + " banned IPs defined");
            }
            else
            {
                _log.info("banned_ip.cfg not found");
            }

        }
        catch (Exception e)
        {
            _log.warning("error while reading banned file:" + e);
        }
        finally
        {
            try { bannedFile.close(); } catch (Exception e) {}
        }
    }
    
    /**
     * This returns a unique LoginServer instance (singleton)
     * This doesnt start the Login in the case of the creation of a new instance
     * like it used to do.
     * @throws GeneralSecurityException 
     */
    public static LoginServer getInstance()
    {
        // If no instances started before, try to start a new one
        if (_instance == null)
        {
            try
            {
            	_instance = new LoginServer();
            }
            catch (IOException e)
            {
                // Throws the exception, if any
                System.out.println(e.getMessage());
            }
        }
        // Return the actual instance
        return _instance;
    }
    
    /**
     * 
     */
    public boolean unblockIp(String ipAddress)
    {
        if (loginController.ipBlocked(ipAddress))
        {
            return true;
        }
        return false;
    }
    
    public static class ForeignConnection
    {
    	/**
		 * @param l
		 */
		public ForeignConnection(long time)
		{
			lastConnection = time;
			connectionNumber = 1;
		}
		public int connectionNumber;
    	public long lastConnection;
    }

	/* (non-Javadoc)
	 * @see net.sf.l2j.loginserver.FloodProtectedListener#addClient(java.net.Socket)
	 */
	@Override
	public void addClient(Socket s)
	{
		try
		{
			new ClientThread(s);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
