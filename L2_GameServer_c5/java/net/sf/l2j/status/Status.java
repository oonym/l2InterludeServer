package net.sf.l2j.status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javolution.text.TextBuilder;
import javolution.util.FastList;

import net.sf.l2j.Config;
import net.sf.l2j.Server;


public class Status extends Thread
{
    
    private ServerSocket    statusServerSocket;
    
    private int             		_uptime;
    private int             		_StatusPort;
    private String          		_StatusPW;
    private int						_mode;
    private List<LoginStatusThread> _loginStatus;
    
    public void run()
    {
        while (true)
        {
            try
            {
                Socket connection = statusServerSocket.accept();
                
                if(_mode == Server.MODE_GAMESERVER)
                {
                	new GameStatusThread(connection, _uptime, _StatusPW);
                }
                else if(_mode == Server.MODE_LOGINSERVER)
                {
                	LoginStatusThread lst = new LoginStatusThread(connection, _uptime);
                	if(lst.isAlive())
                	{
                		_loginStatus.add(lst);
                	}
                }
                if (this.isInterrupted())
                {
                    try
                    {
                        statusServerSocket.close();
                    }
                    catch (IOException io) { io.printStackTrace(); }
                    break;
                }
            }
            catch (IOException e)
            {
                if (this.isInterrupted())
                {
                    try
                    {
                        statusServerSocket.close();
                    }
                    catch (IOException io) { io.printStackTrace(); }
                    break;
                }
            }
        }
    }
    
    public Status(int mode) throws IOException
    {
        super("Status");
        _mode= mode;
        Properties telnetSettings = new Properties();
        InputStream is = new FileInputStream( new File(Config.TELNET_FILE));
        telnetSettings.load(is);
        is.close();
        
        _StatusPort       = Integer.parseInt(telnetSettings.getProperty("StatusPort", "12345"));
        _StatusPW         = telnetSettings.getProperty("StatusPW");
        if(_mode == Server.MODE_GAMESERVER)
        {
	        if (_StatusPW == null)
	        {
	            System.out.println("Server's Telnet Function Has No Password Defined!");
	            System.out.println("A Password Has Been Automaticly Created!");
	            _StatusPW = RndPW(10);
	            System.out.println("Password Has Been Set To: " + _StatusPW);
	        }
	        System.out.println("StatusServer Started! - Listening on Port: " + _StatusPort);
	        System.out.println("Password Has Been Set To: " + _StatusPW);
        }
        else
        {
        	System.out.println("StatusServer Started! - Listening on Port: " + _StatusPort);
        }
        statusServerSocket = new ServerSocket(_StatusPort);
        _uptime = (int) System.currentTimeMillis();
        _loginStatus = new FastList<LoginStatusThread>();
    }
    
    
    
    private String RndPW(int length)
    {
        TextBuilder password = new TextBuilder();
        String lowerChar= "qwertyuiopasdfghjklzxcvbnm";
        String upperChar = "QWERTYUIOPASDFGHJKLZXCVBNM";
        String digits = "1234567890";
        Random randInt = new Random();
        for (int i = 0; i < length; i++)
        {
            int charSet = randInt.nextInt(3);
            switch (charSet)
            {
                case 0:
                    password.append(lowerChar.charAt(randInt.nextInt(lowerChar.length()-1)));
                    break;
                case 1:
                    password.append(upperChar.charAt(randInt.nextInt(upperChar.length()-1)));
                    break;
                case 2:
                    password.append(digits.charAt(randInt.nextInt(digits.length()-1)));
                    break;
            }
        }
        return password.toString();
    }
    
    public void SendMessageToTelnets(String msg)
    {
    	List<LoginStatusThread> lsToRemove = new FastList<LoginStatusThread>();
    	for(LoginStatusThread ls :_loginStatus)
    	{
    		if(ls.isInterrupted())
    		{
    			lsToRemove.add(ls);
    		}
    		else
    		{
    			ls.printToTelnet(msg);
    		}
    	}
    }
}
