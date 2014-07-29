package aggregator.dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.Vertical;
import aggregator.beans.VerticalCategory;
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
		ResultSet rs = null;
		Vertical result = null;
		
		try
		{
			connection = connectionManager.getConnection();
			stmt = connection.prepareStatement("select v.*, f.fedweb_code from ir_vertical as v left outer join ir_vertical_fedweb as f on v.id = f.vertical_id where v.id = ?");
			stmt.setString(1, id);
			rs = stmt.executeQuery();
			
			if(rs.next()) {
				result = new Vertical();
				result.setId(id);
				result.setName(rs.getString("name"));
				result.setDescription(rs.getString("description"));
				result.setFedWebCode(rs.getString("fedweb_code"));
				result.setCategory(new VerticalCategory(rs.getString("vertical_category_id")));
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
			stmt = connection.prepareStatement("select vc.*, f.fedweb_code from ir_vertical_category as vc left outer join ir_vertical_category_fedweb as f on vc.id = f.vertical_category_id where vc.id = ?");
			stmt.setString(1, id);
			rs = stmt.executeQuery();
			
			if(rs.next()) {
				result = new VerticalCategory();
				result.setId(id);
				result.setName(rs.getString("name"));
				result.setDescription(rs.getString("description"));
				result.setFedWebCode(rs.getString("fedweb_code"));
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
}
