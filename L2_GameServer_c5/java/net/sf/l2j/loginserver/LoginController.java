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

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.Base64;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.lib.Log;

/**
 * This class ...
 * 
 * @version $Revision: 1.7.4.3 $ $Date: 2005/03/27 15:30:09 $
 */
public class LoginController
{
    protected static Logger _log = Logger.getLogger(LoginController.class.getName());

    private static LoginController _instance;

    //TODO: use 2 id maps (sever selection + login ok)
    /**
     * this map contains the session ids that belong to one account
     */
    private Map<String, SessionKey> _logins;

    /** this map contains the connections of the players that are in the loginserver*/
    private Map<String, Socket> _accountsInLoginServer;
    private int _maxAllowedOnlinePlayers;
    private Map<String, Integer> _hackProtection;
    private Map<String, String> _lastPassword;
    protected KeyPairGenerator _keyGen;
    protected ScrambledKeyPair[] _keyPairs;
    private AtomicInteger _keyPairToUpdate;
    private long _lastKeyPairUpdate;
    private Random _rnd;

    /**
     * <p>This class is used to represent session keys used by the client to authenticate in the gameserver</p>
     * <p>A SessionKey is made up of two 8 bytes keys. One is send in the {@link net.sf.l2j.loginserver.serverpacket.LoginOk LoginOk}
     * packet and the other is sent in {@link net.sf.l2j.loginserver.serverpacket.PlayOk PlayOk}</p>
     * @author -Wooden-
     *
     */
    public static class SessionKey
    {
        public int playOkID1;
        public int playOkID2;
        public int loginOkID1;
        public int loginOkID2;

        public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2)
        {
            playOkID1 = playOK1;
            playOkID2 = playOK2;
            loginOkID1 = loginOK1;
            loginOkID2 = loginOK2;
        }

        public String toString()
        {
            return "PlayOk: " + playOkID1 + " " + playOkID2 + " LoginOk:" + loginOkID1 + " "
                + loginOkID2;
        }

        /**
         * <p>Returns true if keys are equal.</p>
         * <p>Only checks the PlayOk part of the session key if server doesnt show the licence when player logs in.</p>
         * @param key
         */
        public boolean equals(SessionKey key)
        {
            // when server doesnt show licence it deosnt send the LoginOk packet, client doesnt have this part of the key then.
            if (Config.SHOW_LICENCE) return (playOkID1 == key.playOkID1 && loginOkID1 == key.loginOkID1
                && playOkID2 == key.playOkID2 && loginOkID2 == key.loginOkID2);
            else return (playOkID1 == key.playOkID1 && playOkID2 == key.playOkID2);
        }
    }

    private LoginController()
    {
        _log.info("LoginContoller initating");
        _logins = new FastMap<String, SessionKey>();
        _accountsInLoginServer = new FastMap<String, Socket>();
        _hackProtection = new FastMap<String, Integer>();
        _lastPassword = new FastMap<String, String>();
        _keyPairToUpdate = new AtomicInteger(0);
        _keyPairs = new ScrambledKeyPair[10];
        try
        {
            _keyGen = KeyPairGenerator.getInstance("RSA");
            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(1024, RSAKeyGenParameterSpec.F4);
            _keyGen.initialize(spec);
        }
        catch (GeneralSecurityException e)
        {
            _log.severe("Error in RSA setup:" + e);
            _log.info("Server shutting down now");
            System.exit(2);
        }
        _rnd = new Random();
        if (Config.DEBUG) _log.info("LoginContoller : RSA keygen initated");
        //generate the initial set of keys
        for (int i = 0; i < 10; i++)
        {
            _keyPairs[i] = new ScrambledKeyPair(_keyGen.generateKeyPair());
        }
        _lastKeyPairUpdate = System.currentTimeMillis();
        _log.info("Stored 10 KeyPair for RSA communication");
    }

    public static LoginController getInstance()
    {
        if (_instance == null)
        {
            _instance = new LoginController();
        }

        return _instance;
    }

    public SessionKey assignSessionKeyToLogin(String account, Socket _csocket)
    {
        SessionKey key;

        key = new SessionKey(_rnd.nextInt(), _rnd.nextInt(), _rnd.nextInt(), _rnd.nextInt());
        _logins.put(account, key);
        _accountsInLoginServer.put(account, _csocket);
        return key;
    }

    public void removeGameServerLogin(String account)
    {
        if (account != null)
        {
            _logins.remove(account);
        }
    }

    public void removeLoginServerLogin(String account)
    {
        if (account != null)
        {
            _accountsInLoginServer.remove(account);
        }
    }

    public boolean isAccountInLoginServer(String account)
    {
        return _accountsInLoginServer.containsKey(account);
    }

    public boolean isAccountInAnyGameServer(String account)
    {
        List<GameServerThread> gslist = GameServerListener.getInstance().getGameServerThreads();
        synchronized (gslist)
        {
            for (GameServerThread gs : gslist)
            {
                if (gs.isPlayerInGameServer(account)) return true;
            }
        }
        return false;
    }

    public int getGameServerIDforAccount(String account)
    {
        List<GameServerThread> gslist = GameServerListener.getInstance().getGameServerThreads();
        synchronized (gslist)
        {
            for (GameServerThread gs : gslist)
            {
                if (gs.isPlayerInGameServer(account)) return gs.getServerID();
            }
        }
        return -1;
    }

    public SessionKey getKeyForAccount(String account)
    {
        return _logins.get(account);
    }

    public int getTotalOnlinePlayerCount()
    {
        int playerCount = 0;
        List<GameServerThread> gslist = GameServerListener.getInstance().getGameServerThreads();
        synchronized (gslist)
        {
            for (GameServerThread gs : gslist)
            {
                playerCount += gs.getCurrentPlayers();
            }
        }
        return playerCount;
    }

    public int getOnlinePlayerCount(int ServerID)
    {
        List<GameServerThread> gslist = GameServerListener.getInstance().getGameServerThreads();
        synchronized (gslist)
        {
            for (GameServerThread gs : gslist)
            {
                if (gs.getServerID() == ServerID) return gs.getCurrentPlayers();
            }
        }
        return 0;
    }

    public int getMaxAllowedOnlinePlayers(int ServerID)
    {
        List<GameServerThread> gslist = GameServerListener.getInstance().getGameServerThreads();
        synchronized (gslist)
        {
            for (GameServerThread gs : gslist)
            {
                if (gs.getServerID() == ServerID) return gs.getMaxPlayers();
            }
        }
        return 0;
    }

    public void setMaxAllowedOnlinePlayers(int maxAllowedOnlinePlayers)
    {
        _maxAllowedOnlinePlayers = maxAllowedOnlinePlayers;
    }

    /**
     * @return
     */
    public boolean loginPossible(int access, int ServerID)
    {
        return ((getOnlinePlayerCount(ServerID) < _maxAllowedOnlinePlayers) || (access >= 50));
    }

    public Socket getLoginServerConnection(String loginName)
    {
        return _accountsInLoginServer.get(loginName);
    }

    public void setAccountAccessLevel(String user, int banLevel)
    {
        java.sql.Connection con = null;
        PreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();

            String stmt = "UPDATE accounts, characters SET accounts.access_level = ? WHERE characters.account_name = accounts.login AND characters.char_name=?";
            statement = con.prepareStatement(stmt);
            statement.setInt(1, banLevel);
            statement.setString(2, user);
            statement.executeUpdate();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("Could not set accessLevl:" + e);
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
            try
            {
                statement.close();
            }
            catch (Exception e)
            {
            }
        }
    }

    public boolean isGM(String user)
    {
        boolean ok = false;
        java.sql.Connection con = null;
        PreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT access_level FROM accounts WHERE login=?");
            statement.setString(1, user);
            ResultSet rset = statement.executeQuery();
            if (rset.next())
            {
                int accessLevel = rset.getInt(1);
                if (accessLevel >= Config.GM_MIN)
                {
                    ok = true;
                }
            }
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            _log.warning("could not check gm state:" + e);
            ok = false;
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
            try
            {
                statement.close();
            }
            catch (Exception e)
            {
            }
        }
        return ok;
    }

    /**
     * <p>This method returns one of the 10 {@link ScrambledKeyPair}.</p>
     * <p>One of them the re-newed asynchronously using a {@link UpdateKeyPairTask} if necessary.</p>
     * @return a scrambled keypair
     */
    public ScrambledKeyPair getScrambledRSAKeyPair()
    {
        // ensure that the task will update the keypair only after a keypair is returned.
        synchronized (_keyPairs)
        {
            if ((System.currentTimeMillis() - _lastKeyPairUpdate) > 1000 * 60) // update a key every minutes
            {
                if (_keyPairToUpdate.get() == 10) _keyPairToUpdate.set(0);
                UpdateKeyPairTask task = new UpdateKeyPairTask(_keyPairToUpdate.getAndIncrement());
                task.start();
                _lastKeyPairUpdate = System.currentTimeMillis();
            }
            return _keyPairs[_rnd.nextInt(10)];
        }
    }

    /**
     * user name is not case sensitive any more
     * @param user
     * @param password
     * @param address
     * @return
     */
    public boolean loginValid(String user, String password, InetAddress address) throws HackingException
    {
        boolean ok = false;

        Integer failedConnects = _hackProtection.get(address.getHostAddress());
        String lastPassword = _lastPassword.get(address.getHostAddress());

        Log.add("'" + user + "' " + address.getHostAddress(), "logins_ip");

        if (failedConnects != null && failedConnects > Config.LOGIN_TRY_BEFORE_BAN)
        {
            _log.warning("hacking detected from ip:" + address.getHostAddress()
                + " .. adding IP to banlist");
            failedConnects++;
            throw new HackingException(address.getHostAddress(), failedConnects);
        }

        java.sql.Connection con = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA");
            byte[] raw = password.getBytes("UTF-8");
            byte[] hash = md.digest(raw);

            byte[] expected = null;

            // this is here for temp debugging 
            // int busy = L2DatabaseFactory.getInstance().getBusyConnectionCount();
            // int idle = L2DatabaseFactory.getInstance().getIdleConnectionCount();
            //_log.info("DB connections busy:"+busy+" idle:"+idle);

            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT password FROM accounts WHERE login=?");
            statement.setString(1, user);
            ResultSet rset = statement.executeQuery();
            if (rset.next())
            {
                expected = Base64.decode(rset.getString(1));
                if (Config.DEBUG) _log.fine("account exists");
            }
            rset.close();
            statement.close();

            if (expected == null)
            {
                if (Config.AUTO_CREATE_ACCOUNTS)
                {
                    if ((user.length() >= 2) && (user.length() <= 14))
                    {
                        statement = con.prepareStatement("INSERT INTO accounts (login,password,lastactive,access_level,lastIP) values(?,?,?,?,?)");
                        statement.setString(1, user);
                        statement.setString(2, Base64.encodeBytes(hash));
                        statement.setLong(3, System.currentTimeMillis());
                        statement.setInt(4, 0);
                        statement.setString(5, address.getHostAddress());
                        statement.execute();
                        statement.close();

                        _log.info("created new account for " + user);
                        if (LoginServer.statusServer != null)
                            LoginServer.statusServer.SendMessageToTelnets("Account created for player "
                                + user);

                        return true;

                    }
                    _log.warning("Invalid username creation/use attempt: " + user);
                    return false;
                }
                _log.warning("account missing for user " + user);
                return false;
            }

            ok = true;
            for (int i = 0; i < expected.length; i++)
            {
                if (hash[i] != expected[i])
                {
                    ok = false;
                    break;
                }
            }
            if (ok)
            {
                statement = con.prepareStatement("UPDATE accounts SET lastactive=?, lastIP=? WHERE login=?");
                statement.setLong(1, System.currentTimeMillis());
                statement.setString(2, address.getHostAddress());
                statement.setString(3, user);
                statement.execute();
                statement.close();
            }
        }
        catch (Exception e)
        {
            // digest algo not found ??
            // out of bounds should not be possible
            _log.warning("could not check password:" + e);
            ok = false;
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
        }

        if (!ok)
        {
            Log.add("'" + user + "' " + address.getHostAddress(), "logins_ip_fails");

            // add 1 to the failed counter for this IP 
            int failedCount = 1;
            if (failedConnects != null)
            {
                failedCount = failedConnects.intValue() + 1;
            }

            if (password != lastPassword)
            {
                _hackProtection.put(address.getHostAddress(), new Integer(failedCount));
                _lastPassword.put(address.getHostAddress(), password);
            }
        }
        else
        {
            // for long running servers, this should prevent blocking 
            // of users that mistype their passwords once every day :)
            _hackProtection.remove(address.getHostAddress());
            _lastPassword.remove(address.getHostAddress());
            Log.add("'" + user + "' " + address.getHostAddress(), "logins_ip");
        }

        return ok;
    }

    public boolean loginBanned(String user)
    {
        boolean ok = false;

        java.sql.Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT access_level FROM accounts WHERE login=?");
            statement.setString(1, user);
            ResultSet rset = statement.executeQuery();
            if (rset.next())
            {
                int accessLevel = rset.getInt(1);
                if (accessLevel < 0) ok = true;
            }
            rset.close();
            statement.close();
        }
        catch (Exception e)
        {
            // digest algo not found ??
            // out of bounds should not be possible
            _log.warning("could not check ban state:" + e);
            ok = false;
        }
        finally
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
            }
        }

        return ok;
    }

    public boolean ipBlocked(String ipAddress)
    {
        int tries = 0;

        if (_hackProtection.containsKey(ipAddress)) tries = _hackProtection.get(ipAddress);

        if (tries > Config.LOGIN_TRY_BEFORE_BAN)
        {
            _hackProtection.remove(ipAddress);
            _log.warning("Removed host from hacklist! IP number: " + ipAddress);
            return true;
        }

        return false;
    }

    private class UpdateKeyPairTask extends Thread
    {
        private int _keyPairId;

        public UpdateKeyPairTask(int keyPairId)
        {
            _keyPairId = keyPairId;
        }

        public void run()
        {
            _keyPairs[_keyPairId] = new ScrambledKeyPair(_keyGen.generateKeyPair());

            if (Config.DEBUG) _log.info("Updated a RSA key");
        }
    }

    public static class ScrambledKeyPair
    {
        public KeyPair pair;
        public byte[] scrambledModulus;

        public ScrambledKeyPair(KeyPair pPair)
        {
            this.pair = pPair;
            scrambledModulus = scrambleModulus(((RSAPublicKey) this.pair.getPublic()).getModulus());
        }

        private byte[] scrambleModulus(BigInteger modulus)
        {
            byte[] scrambledMod = modulus.toByteArray();

            if (scrambledMod.length == 0x81 && scrambledMod[0] == 0x00)
            {
                byte[] temp = new byte[0x80];
                System.arraycopy(scrambledMod, 1, temp, 0, 0x80);
                scrambledMod = temp;
            }
            // step 1 : 0x4d-0x50 <-> 0x00-0x04
            for (int i = 0; i < 4; i++)
            {
                byte temp = scrambledMod[0x00 + i];
                scrambledMod[0x00 + i] = scrambledMod[0x4d + i];
                scrambledMod[0x4d + i] = temp;
            }
            // step 2 : xor first 0x40 bytes with  last 0x40 bytes
            for (int i = 0; i < 0x40; i++)
            {
                scrambledMod[i] = (byte) (scrambledMod[i] ^ scrambledMod[0x40 + i]);
            }
            // step 3 : xor bytes 0x0d-0x10 with bytes 0x34-0x38
            for (int i = 0; i < 4; i++)
            {
                scrambledMod[0x0d + i] = (byte) (scrambledMod[0x0d + i] ^ scrambledMod[0x34 + i]);
            }
            // step 4 : xor last 0x40 bytes with  first 0x40 bytes
            for (int i = 0; i < 0x40; i++)
            {
                scrambledMod[0x40 + i] = (byte) (scrambledMod[0x40 + i] ^ scrambledMod[i]);
            }
            _log.fine("Modulus was scrambled");

            return scrambledMod;
        }
    }
}
