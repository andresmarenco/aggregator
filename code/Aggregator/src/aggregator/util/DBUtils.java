package aggregator.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DBUtils {
	
	private static Log log = LogFactory.getLog(DBUtils.class);

	/**
	 * Closes a Database Connection
	 * @param connection Database Connection
	 */
	public static void closeConnection(Connection connection) {
		try
		{
			if(connection != null) {
				connection.close();
			}
		}
		catch(SQLException ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	/**
	 * Closes a Database Statement
	 * @param stmt Database Statement
	 */
	public static void closeStatement(Statement stmt) {
		try
		{
			if(stmt != null) {
				stmt.close();
			}
		}
		catch(SQLException ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	/**
	 * Closes a Database ResultSet
	 * @param rs Database ResultSet
	 */
	public static void closeResultSet(ResultSet rs) {
		try
		{
			if(rs != null) {
				rs.close();
			}
		}
		catch(SQLException ex) {
			log.error(ex.getMessage(), ex);
		}
	}
}
