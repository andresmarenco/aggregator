package aggregator.dataaccess;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PooledConnectionManager implements ConnectionManager {
	
	private static final String POOL_SIZE_KEY = "aggregator.database.poolSize";
	
	/** Current instance of the class for the Singleton Pattern */
	private static ConnectionManager _ClassInstance;
	private ArrayBlockingQueue<Connection> connectionPool;
	private Log log = LogFactory.getLog(PooledConnectionManager.class);
	
	/**
	 * Default Constructor
	 */
	public PooledConnectionManager() {
		this.init();
	}
	
	/**
	 * Initializes the connection pool
	 */
	private void init() {
		try
		{
			ConnectionManager connectionManager = DirectConnectionManager.getInstance();
			
			int size = Integer.parseInt(System.getProperty(POOL_SIZE_KEY, "1"));
			log.info(MessageFormat.format("Creating connection pool of size {0}", String.valueOf(size)));
			
			this.connectionPool = new ArrayBlockingQueue<Connection>(size);
			for(int i = 0; i < size; i++) {
				this.connectionPool.add(new PooledConnection(connectionManager.getConnection()));
			}
			
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	

	@Override
	public Connection getConnection() throws SQLException {
		Connection response = null;
		try
		{
		    response = connectionPool.take();
		}
		catch(Exception ex)
		{
        	log.error(ex.getMessage(), ex);
		    response = null;
		}
		return response;
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
			    _ClassInstance = new PooledConnectionManager();    
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
	public Object clone() throws CloneNotSupportedException {
    	throw new CloneNotSupportedException();
    }

	
	private class PooledConnection implements Connection {
		
		private Connection baseConnection;
		
		public PooledConnection(Connection baseConnection) {
			this.baseConnection = baseConnection;
		}

		@Override
		public <T> T unwrap(Class<T> iface) throws SQLException {
			return baseConnection.unwrap(iface);
		}

		@Override
		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			return baseConnection.isWrapperFor(iface);
		}

		@Override
		public Statement createStatement() throws SQLException {
			return baseConnection.createStatement();
		}

		@Override
		public PreparedStatement prepareStatement(String sql)
				throws SQLException {
			return baseConnection.prepareStatement(sql);
		}

		@Override
		public CallableStatement prepareCall(String sql) throws SQLException {
			return baseConnection.prepareCall(sql);
		}

		@Override
		public String nativeSQL(String sql) throws SQLException {
			return baseConnection.nativeSQL(sql);
		}

		@Override
		public void setAutoCommit(boolean autoCommit) throws SQLException {
			baseConnection.setAutoCommit(autoCommit);
		}

		@Override
		public boolean getAutoCommit() throws SQLException {
			return baseConnection.getAutoCommit();
		}

		@Override
		public void commit() throws SQLException {
			baseConnection.commit();
		}

		@Override
		public void rollback() throws SQLException {
			baseConnection.rollback();
		}

		@Override
		public void close() throws SQLException {
			try
			{
			    connectionPool.put(this);
			}
			catch(Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}

		@Override
		public boolean isClosed() throws SQLException {
			return baseConnection.isClosed();
		}

		@Override
		public DatabaseMetaData getMetaData() throws SQLException {
			return baseConnection.getMetaData();
		}

		@Override
		public void setReadOnly(boolean readOnly) throws SQLException {
			baseConnection.setReadOnly(readOnly);
		}

		@Override
		public boolean isReadOnly() throws SQLException {
			return baseConnection.isReadOnly();
		}

		@Override
		public void setCatalog(String catalog) throws SQLException {
			baseConnection.setCatalog(catalog);
		}

		@Override
		public String getCatalog() throws SQLException {
			return baseConnection.getCatalog();
		}

		@Override
		public void setTransactionIsolation(int level) throws SQLException {
			baseConnection.setTransactionIsolation(level);
		}

		@Override
		public int getTransactionIsolation() throws SQLException {
			return baseConnection.getTransactionIsolation();
		}

		@Override
		public SQLWarning getWarnings() throws SQLException {
			return baseConnection.getWarnings();
		}

		@Override
		public void clearWarnings() throws SQLException {
			baseConnection.clearWarnings();
		}

		@Override
		public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
			return baseConnection.createStatement(resultSetType, resultSetConcurrency);
		}

		@Override
		public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
			return baseConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
		}

		@Override
		public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
			return baseConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
		}

		@Override
		public Map<String, Class<?>> getTypeMap() throws SQLException {
			return baseConnection.getTypeMap();
		}

		@Override
		public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
			baseConnection.setTypeMap(map);
		}

		@Override
		public void setHoldability(int holdability) throws SQLException {
			baseConnection.setHoldability(holdability);
		}

		@Override
		public int getHoldability() throws SQLException {
			return baseConnection.getHoldability();
		}

		@Override
		public Savepoint setSavepoint() throws SQLException {
			return baseConnection.setSavepoint();
		}

		@Override
		public Savepoint setSavepoint(String name) throws SQLException {
			return baseConnection.setSavepoint(name);
		}

		@Override
		public void rollback(Savepoint savepoint) throws SQLException {
			baseConnection.rollback(savepoint);
		}

		@Override
		public void releaseSavepoint(Savepoint savepoint) throws SQLException {
			baseConnection.releaseSavepoint(savepoint);
		}

		@Override
		public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
			return baseConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
		}

		@Override
		public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
			return baseConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		}

		@Override
		public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
			return baseConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		}

		@Override
		public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
			return baseConnection.prepareStatement(sql, autoGeneratedKeys);
		}

		@Override
		public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
			return baseConnection.prepareStatement(sql, columnIndexes);
		}

		@Override
		public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
			return baseConnection.prepareStatement(sql, columnNames);
		}

		@Override
		public Clob createClob() throws SQLException {
			return baseConnection.createClob();
		}

		@Override
		public Blob createBlob() throws SQLException {
			return baseConnection.createBlob();
		}

		@Override
		public NClob createNClob() throws SQLException {
			return baseConnection.createNClob();
		}

		@Override
		public SQLXML createSQLXML() throws SQLException {
			return baseConnection.createSQLXML();
		}

		@Override
		public boolean isValid(int timeout) throws SQLException {
			return baseConnection.isValid(timeout);
		}

		@Override
		public void setClientInfo(String name, String value) throws SQLClientInfoException {
			baseConnection.setClientInfo(name, value);
			
		}

		@Override
		public void setClientInfo(Properties properties) throws SQLClientInfoException {
			baseConnection.setClientInfo(properties);
		}

		@Override
		public String getClientInfo(String name) throws SQLException {
			return baseConnection.getClientInfo(name);
		}

		@Override
		public Properties getClientInfo() throws SQLException {
			return baseConnection.getClientInfo();
		}

		@Override
		public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
			return baseConnection.createArrayOf(typeName, elements);
		}

		@Override
		public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
			return baseConnection.createStruct(typeName, attributes);
		}

		@Override
		public void setSchema(String schema) throws SQLException {
			baseConnection.setSchema(schema);
		}

		@Override
		public String getSchema() throws SQLException {
			return baseConnection.getSchema();
		}

		@Override
		public void abort(Executor executor) throws SQLException {
			baseConnection.abort(executor);
		}

		@Override
		public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
			baseConnection.setNetworkTimeout(executor, milliseconds);
		}

		@Override
		public int getNetworkTimeout() throws SQLException {
			return baseConnection.getNetworkTimeout();
		}
		
	}
}
