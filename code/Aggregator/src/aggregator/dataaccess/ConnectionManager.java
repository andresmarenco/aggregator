package aggregator.dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionManager {
	
    /**
     * The Clone method is not supported due the Singleton Pattern
     * @return Always throws an exception
     * @throws java.lang.CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException;
    
    
    
    
	/**
	 * Gets a connection to the database
	 * @return Database connection
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException;
}
