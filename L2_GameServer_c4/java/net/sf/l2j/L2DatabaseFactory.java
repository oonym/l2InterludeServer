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
package net.sf.l2j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.DataSources;
import com.mchange.v2.c3p0.PoolConfig;
import com.mchange.v2.c3p0.PooledDataSource;

public class L2DatabaseFactory
{
    static Logger _log = Logger.getLogger(L2DatabaseFactory.class.getName());

    public static enum ProviderType
    {
        MySql,
        MsSql
    }

    // =========================================================
    // Data Field
    private static L2DatabaseFactory _instance;
    private ProviderType _Provider_Type;
	private DataSource _source;
	
    // =========================================================
    // Constructor
	public L2DatabaseFactory() throws SQLException
	{
		try
		{
			if (Config.DATABASE_MAX_CONNECTIONS < 2)
            {
                Config.DATABASE_MAX_CONNECTIONS = 2;
                _log.warning("at least " + Config.DATABASE_MAX_CONNECTIONS + " db connections are required.");
            }

			PoolConfig config = new PoolConfig();
			config.setAutoCommitOnClose(true);
			config.setInitialPoolSize(3);   // 3 is the default for c3p0 anyway  
			// if > MaxPoolSize, it will be ignored - no worry
			// (as said in c3p0 docs, it's only a suggestion
			// how many connections to acquire to start with)

			config.setMinPoolSize(1);
			config.setMaxPoolSize(Config.DATABASE_MAX_CONNECTIONS);

 
			config.setAcquireRetryAttempts(0); // try to obtain connections indefinitely (0 = never quit)
			config.setAcquireRetryDelay(500);  // 500 miliseconds wait before try to acquire connection again
			config.setCheckoutTimeout(0);      // 0 = wait indefinitely for new connection
			// if pool is exhausted
			config.setAcquireIncrement(5);     // if pool is exhausted, get 5 more connections at a time
			// cause there is a "long" delay on acquire connection
			// so taking more than one connection at once will make connection pooling 
			// more effective. 
 
			// this "connection_test_table" is automatically created if not already there
			config.setAutomaticTestTable("connection_test_table");  // very very fast test, don't worry
			config.setTestConnectionOnCheckin(true); // this will *not* make l2j slower in any way
 
			// testing OnCheckin used with IdleConnectionTestPeriod is faster than  testing on checkout
 
			config.setIdleConnectionTestPeriod(60); // test idle connection every 60 sec
			config.setMaxIdleTime(0); // 0 = idle connections never expire 
			// *THANKS* to connection testing configured above
			// but I prefer to disconnect all connections not used
			// for more than 1 hour  

			// enables statement caching,  there is a "semi-bug" in c3p0 0.9.0 but in 0.9.0.2 and later it's fixed
			config.setMaxStatementsPerConnection(100);

			config.setBreakAfterAcquireFailure(false);  // never fail if any way possible
			// setting this to true will make
			// c3p0 "crash" and refuse to work 
			// till restart thus making acquire
			// errors "FATAL" ... we don't want that
			// it should be possible to recover
 
			Class.forName(Config.DATABASE_DRIVER).newInstance();

			if (Config.DEBUG) _log.fine("Database Connection Working");

			DataSource unpooled = DataSources.unpooledDataSource(Config.DATABASE_URL, Config.DATABASE_LOGIN, Config.DATABASE_PASSWORD);
			_source = DataSources.pooledDataSource( unpooled, config);
			
			/* Test the connection */
			_source.getConnection().close();

            if (Config.DATABASE_DRIVER.toLowerCase().contains("microsoft"))
                _Provider_Type = ProviderType.MsSql;
            else
                _Provider_Type = ProviderType.MySql;
		}
		catch (SQLException x)
		{
			if (Config.DEBUG) _log.fine("Database Connection FAILED");
			// rethrow the exception
			throw x;
		}
		catch (Exception e)
		{
			if (Config.DEBUG) _log.fine("Database Connection FAILED");
			throw new SQLException("could not init DB connection:"+e);
		}
	}
    
    // =========================================================
    // Method - Public
    public final String prepQuerySelect(String[] fields, String tableName, String whereClause, boolean returnOnlyTopRecord)
    {
        String msSqlTop1 = "";
        String mySqlTop1 = "";
        if (returnOnlyTopRecord)
        {
            if (getProviderType() == ProviderType.MsSql) msSqlTop1 = " Top 1 ";
            if (getProviderType() == ProviderType.MySql) mySqlTop1 = " Limit 1 ";
        }
        String query = "SELECT " + msSqlTop1 + safetyString(fields) + " FROM " + tableName + " WHERE " + whereClause + mySqlTop1;
        return query;
    }

    public void shutdown()
    {
        try {
            ((PooledDataSource) _source).close();
        } catch (SQLException e) {_log.log(Level.INFO, "", e);}
        try {
            DataSources.destroy(_source);
        } catch (SQLException e) {_log.log(Level.INFO, "", e);}
    }

    public final String safetyString(String[] whatToCheck)
    {
        // NOTE: Use brace as a safty percaution just incase name is a reserved word
        String braceLeft = "`";
        String braceRight = "`";
        if (getProviderType() == ProviderType.MsSql)
        {
            braceLeft = "[";
            braceRight = "]";
        }

        String result = "";
        for(String word : whatToCheck)
        {
            if(result != "") result += ", ";
            result += braceLeft + word + braceRight;
        }
        return result;
    }

    // =========================================================
    // Property - Public
	public static L2DatabaseFactory getInstance() throws SQLException
	{
		if (_instance == null)
		{
			_instance = new L2DatabaseFactory();
		}
		return _instance;
	}
	
	public Connection getConnection() //throws SQLException
	{
		Connection con=null;
 
		while(con==null)
		{
			try
			{
				con=_source.getConnection();
			} catch (SQLException e)
			{
				_log.warning("L2DatabaseFactory: getConnection() failed, trying again "+e);
			}
		}
		return con;
	}
	
	public int getBusyConnectionCount() throws SQLException
	{
	    return ((PooledDataSource) _source).getNumBusyConnectionsDefaultUser();
	}

	public int getIdleConnectionCount() throws SQLException
	{
	    return ((PooledDataSource) _source).getNumIdleConnectionsDefaultUser();
	}

    public final ProviderType getProviderType() { return _Provider_Type; }
}
