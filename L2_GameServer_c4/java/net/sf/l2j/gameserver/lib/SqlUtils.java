package net.sf.l2j.gameserver.lib;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

import net.sf.l2j.L2DatabaseFactory;

public class SqlUtils
{
	private static Logger _log = Logger.getLogger(SqlUtils.class.getName());
	
    // =========================================================
    // Data Field
	private static SqlUtils _instance;

    // =========================================================
    // Property - Public
	public static SqlUtils getInstance()
	{
        if (_instance == null) _instance = new SqlUtils();
		return _instance;
	}

    // =========================================================
    // Method - Public
	public static Integer getIntValue(String resultField, String tableName, String whereClause)
	{
        String query = "";
		Integer res = null;
		
		PreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
            query = L2DatabaseFactory.getInstance().prepQuerySelect(new String[] {resultField}, tableName, whereClause, true);

			statement = L2DatabaseFactory.getInstance().getConnection().prepareStatement(query);
			rset = statement.executeQuery();
		
			if(rset.next()) res = rset.getInt(1);
		}
		catch(Exception e)
		{
			_log.warning("Error in query '" + query + "':"+e);
			e.printStackTrace();
		}
		finally
		{
			try{ rset.close();  } catch(Exception e) {}
			try{ statement.close(); } catch(Exception e) {}
		}

		return res;
	}

    public static Integer[] getIntArray(String resultField, String tableName, String whereClause)
    {
        String query = "";
        Integer[] res = null;
        
        PreparedStatement statement = null;
        ResultSet rset = null;

        try
        {
            query = L2DatabaseFactory.getInstance().prepQuerySelect(new String[] {resultField}, tableName, whereClause, false);
            statement = L2DatabaseFactory.getInstance().getConnection().prepareStatement(query);
            rset = statement.executeQuery();
            
            int rows = 0;
            
            while (rset.next())
                rows++;
            
            if (rows == 0) return new Integer[0];

            res = new Integer[rows-1];

            rset.first();
        
            int row = 0;
            while (rset.next())
            {
                res[row] = rset.getInt(1);
            }
        }
        catch(Exception e)
        {
            _log.warning("mSGI: Error in query '" + query + "':"+e);
            e.printStackTrace();
        }
        finally
        {
            try{ rset.close();  } catch(Exception e) {}
            try{ statement.close(); } catch(Exception e) {}
        }

        return res;
    }

	public static Integer[][] get2DIntArray(String[] resultFields, String usedTables, String whereClause)
	{
		long start = System.currentTimeMillis();

        String query = "";

		PreparedStatement statement = null;
		ResultSet rset = null;

		Integer res[][] = null;

		try
		{
            query = L2DatabaseFactory.getInstance().prepQuerySelect(resultFields, usedTables, whereClause, false);
            statement = L2DatabaseFactory.getInstance().getConnection().prepareStatement(query);
			rset = statement.executeQuery();

			int rows = 0;
			while(rset.next())
				rows++;

			res = new Integer[rows-1][resultFields.length];

			rset.first();

			int row = 0;
			while(rset.next())
			{
				for(int i=0; i<resultFields.length; i++)
			 		res[row][i] = rset.getInt(i+1);
				row++;
			}
		}
		catch(Exception e)
		{
			_log.warning("Error in query '" + query + "':"+e);
			e.printStackTrace();
		}
		finally
		{
			try{ rset.close();  } catch(Exception e) {}
			try{ statement.close(); } catch(Exception e) {}
		}

		_log.fine("Get all rows in query '" + query + "' in " + (System.currentTimeMillis()-start) + "ms");
		return res;
	}
}
