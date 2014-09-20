package aggregator.dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.Vertical;
import aggregator.beans.VerticalCategory;
import aggregator.beans.VerticalCollection;
import aggregator.beans.VerticalCollection.VerticalCollectionData;
import aggregator.util.DBUtils;

public class VerticalDAO {
	
	private ConnectionManager connectionManager;
	private Log log = LogFactory.getLog(VerticalDAO.class);
	
	/**
	 * Default Constructor
	 * @param connectionManager Connection Manager Instance
	 */
	public VerticalDAO(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	
	
	
	/**
	 * Load the corresponding vertical data
	 * @param id Id of the vertical
	 * @return Vertical Object
	 */
	public Vertical loadVertical(String id) {
		Connection connection = null;
		PreparedStatement stmt = null;
		Vertical result = null;
		
		try
		{
			connection = connectionManager.getConnection();
			stmt = connection.prepareStatement("select * from ir_vertical where id = ?;"
					+ "select category_id from ir_vertical_by_category where vertical_id = ?");
			
			stmt.setString(1, id);
			stmt.setString(2, id);
			
			if(stmt.execute()) {
				try(ResultSet data = stmt.getResultSet()) {
					if(data.next()) {
						result = new Vertical();
						result.setId(id);
						result.setName(data.getString("name"));
						result.setDescription(data.getString("description"));
						
						if(stmt.getMoreResults()) {
							try(ResultSet categories = stmt.getResultSet()) {
								result.setCategories(new ArrayList<VerticalCategory>());
								while(categories.next()) {
									result.getCategories().add(new VerticalCategory(categories.getString("category_id")));
								}
							}
						}
					}
				}
			}
		}
		catch(SQLException ex) {
			log.error(ex.getMessage(), ex);
		}
		finally {
			DBUtils.closeStatement(stmt);
			DBUtils.closeConnection(connection);
		}
		
		return result;
	}
	
	
	
	
	/**
	 * Loads the corresponding vertical category data
	 * @param id Id of the vertical category
	 * @return Vertical Category Object
	 */
	public VerticalCategory loadVerticalCategory(String id) {
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		VerticalCategory result = null;
		
		try
		{
			connection = connectionManager.getConnection();
			stmt = connection.prepareStatement("select * from ir_vertical_category id = ?");
			stmt.setString(1, id);
			rs = stmt.executeQuery();
			
			if(rs.next()) {
				result = new VerticalCategory();
				result.setId(id);
				result.setName(rs.getString("name"));
				result.setDescription(rs.getString("description"));
			}
		}
		catch(SQLException ex) {
			log.error(ex.getMessage(), ex);
		}
		finally {
			DBUtils.closeResultSet(rs);
			DBUtils.closeStatement(stmt);
			DBUtils.closeConnection(connection);
		}
		
		return result;
	}
	
	
	
	
	/**
	 * Loads the collection and its verticals
	 * @param collectionId Collection Id
	 * @return Collection data
	 */
	public VerticalCollection loadVerticalCollection(String collectionId) {
		return this.loadVerticalCollection(collectionId, false);
	}
	
	
	/**
	 * Loads the collection and its verticals
	 * @param collectionId Collection Id
	 * @param loadVerticalData Loads the complete vertical individual data
	 * @return Collection data
	 */
	public VerticalCollection loadVerticalCollection(String collectionId, boolean loadVerticalData) {
		VerticalCollection result = null;
		try(Connection connection = connectionManager.getConnection())
		{
			try(PreparedStatement stmt = connection.prepareStatement("select * from ir_collection where id=?;"
							+ "select * from ir_vertical_by_collection where collection_id=? order by id")) {
				
				stmt.setString(1, collectionId);
				stmt.setString(2, collectionId);
				
				if(stmt.execute()) {
					try(ResultSet data = stmt.getResultSet()) {
						if(data.next()) {
							result = new VerticalCollection();
							result.setId(collectionId);
							result.setName(data.getString("name"));
							result.setVerticals(new ArrayList<VerticalCollectionData>());
							
							if(stmt.getMoreResults()) {
								try(ResultSet verticals = stmt.getResultSet()) {
									while(verticals.next()) {
										result.getVerticals().add(
												new VerticalCollectionData(
														verticals.getString("id"),
														loadVerticalData ? this.loadVertical(verticals.getString("vertical_id")) : new Vertical(verticals.getString("vertical_id")),
														verticals.getDouble("size_factor"),
														verticals.getInt("sample_size")));
									}
								}
							}
						}
					}
				}
			}
			
		}
		catch(SQLException ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
	
	
	
	/**
	 * Updates the size factor of the vertical in the collection
	 * @param collectionId Collection ID
	 * @param verticalId Vertical ID
	 * @param sizeFactor Size factor
	 */
	public void updateSizeFactor(String collectionId, String verticalId, double sizeFactor) {
		try(Connection connection = connectionManager.getConnection()) {
			try(PreparedStatement stmt = connection.prepareStatement("update ir_vertical_by_collection set size_factor = ? where vertical_id=? and collection_id=?")) {
				stmt.setDouble(1, sizeFactor);
				stmt.setString(2, verticalId);
				stmt.setString(3, collectionId);
				stmt.execute();
			}
		}
		catch(SQLException ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	/**
	 * Updates the sample size of the vertical in the collection
	 * @param collectionId Collection ID
	 * @param verticalId Vertical ID
	 * @param sampleSize Sample size
	 */
	public void updateSampleSize(String collectionId, String verticalId, int sampleSize) {
		try(Connection connection = connectionManager.getConnection()) {
			try(PreparedStatement stmt = connection.prepareStatement("update ir_vertical_by_collection set sample_size = ? where vertical_id=? and collection_id=?")) {
				stmt.setInt(1, sampleSize);
				stmt.setString(2, verticalId);
				stmt.setString(3, collectionId);
				stmt.execute();
			}
		}
		catch(SQLException ex) {
			log.error(ex.getMessage(), ex);
		}
	}
}
