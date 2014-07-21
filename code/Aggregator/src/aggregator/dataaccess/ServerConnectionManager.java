package aggregator.dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

public class ServerConnectionManager implements ConnectionManager {
	
	/** Current instance of the class for the Singleton Pattern */
	private static ConnectionManager _ClassInstance;
	
	public static final String DEFAULT_DATASOURCE_KEY = "jdbc/Aggregator";
	
	private DataSource _DataSource;
	
	/**
	 * Default Constructor
	 */
	private ServerConnectionManager() {
		try
		{
			InitialContext context = new InitialContext();
			_DataSource = (DataSource) context.lookup("java:/comp/env/" + DEFAULT_DATASOURCE_KEY);
		}
		catch(Exception ex) {
			_DataSource = null;
			ex.printStackTrace();
		}
	}
	
	
	
	/**
     * Returns an instance of the class
     * @return Singleton instance of the class
     */
    public synchronized static ConnectionManager getInstance()
    {
        try
		{
		    if(_ClassInstance == null)
		    {
			    _ClassInstance = new ServerConnectionManager();    
		    }
		    return _ClassInstance;
		}
		catch(Exception ex)
		{
            ex.printStackTrace();
		    return null;
		}
    }
	
	
	
	
	@Override
	public Connection getConnection() throws SQLException {
		return _DataSource.getConnection();
	}

	
	@Override
	public Object clone() throws CloneNotSupportedException {
    	throw new CloneNotSupportedException();
    }
}
