package aggregator.dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DirectConnectionManager implements ConnectionManager {
	
	/** Current instance of the class for the Singleton Pattern */
	private static ConnectionManager _ClassInstance;
	
	public static final String URL_KEY = "aggregator.database.url";
	public static final String DRIVERNAME_KEY = "aggregator.database.driverName";
	public static final String USER_KEY = "aggregator.database.user";
	public static final String PASSWORD_KEY = "aggregator.database.password";
	
	private final String url;
	private final String driverName;
	private final String user;
	private final String password;
	
	
	/**
	 * Default Constructor
	 */
	private DirectConnectionManager() {
		this.url = System.getProperty(URL_KEY);
		this.driverName = System.getProperty(DRIVERNAME_KEY);
		this.user = System.getProperty(USER_KEY);
		this.password = System.getProperty(PASSWORD_KEY);
		
		try {
			Class.forName(this.driverName);
		} catch (ClassNotFoundException ex) {
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
			    _ClassInstance = new DirectConnectionManager();    
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
		return DriverManager.getConnection(url, user, password);
	}
	
	
	
	
	@Override
	public Object clone() throws CloneNotSupportedException {
    	throw new CloneNotSupportedException();
    }
}
