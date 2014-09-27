package aggregator.sampler.analysis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.TFDF;
import aggregator.beans.Vertical;
import aggregator.dataaccess.ConnectionManager;
import aggregator.dataaccess.DirectConnectionManager;
import aggregator.dataaccess.VerticalDAO;
import aggregator.sampler.output.DocumentTermsLogFile;
import aggregator.sampler.output.TFDFLogFile;

public class DatabaseIntermediateResults implements IntermediateResults {
	
	private Vertical vertical;
	private ConnectionManager connectionManager;
	private String newTermsTable;
	private String foundTermsTable;
	private String uniqueTermsTable;
	private String documentTermsTable;
	private Log log = LogFactory.getLog(VerticalDAO.class);
	
	
	public DatabaseIntermediateResults(Vertical vertical) {
		this.vertical = vertical;
		this.connectionManager = DirectConnectionManager.getInstance();
		this.createTables();
	}
	
	
	
	
	private void createTables() {
		try(Connection connection = connectionManager.getConnection())
		{
			String verticalName = vertical.getId()
					.trim().replaceAll("[\\W]|_", "");
			verticalName = StringUtils.stripStart(verticalName, "-0123456789");
			
			this.newTermsTable = verticalName + "_newterms";
			this.foundTermsTable = verticalName + "_foundterms";
			this.uniqueTermsTable = verticalName + "_uniqueterms";
			this.documentTermsTable = verticalName + "_documentterms";
			
			this.close();
			
			String newTerms = MessageFormat.format("create unlogged table if not exists {0} (term character varying(1000)); ", newTermsTable);
			String foundTerms = MessageFormat.format("create unlogged table if not exists {0} (term character varying(1000)); ", foundTermsTable);
			String uniqueTerms = MessageFormat.format("create unlogged table if not exists {0} (term character varying(1000), tf integer, df integer); ", uniqueTermsTable);
			String documentTerms = MessageFormat.format("create unlogged table if not exists {0} (id serial, docId character varying(5000), terms text); ", documentTermsTable);
			
			try(PreparedStatement stmt = connection.prepareStatement(
					new StringBuilder()
					.append(newTerms)
					.append(foundTerms)
					.append(uniqueTerms)
					.append(documentTerms)
					.toString())) {
				
				stmt.execute();
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	@Override
	public void close() throws IOException {
		try(Connection connection = connectionManager.getConnection())
		{
			String newTerms = MessageFormat.format("drop table if exists {0}; ", newTermsTable);
			String foundTerms = MessageFormat.format("drop table if exists {0}; ", foundTermsTable);
			String uniqueTerms = MessageFormat.format("drop table if exists {0}; ", uniqueTermsTable);
			String documentTerms = MessageFormat.format("drop table if exists {0}; ", documentTermsTable);
			
			try(PreparedStatement stmt = connection.prepareStatement(
					new StringBuilder()
					.append(newTerms)
					.append(foundTerms)
					.append(uniqueTerms)
					.append(documentTerms)
					.toString())) {
				
				stmt.execute();
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	@Override
	public void addDocumentTerms(String docId, List<String> foundTerms) {
		try(Connection connection = connectionManager.getConnection()) {
			try(PreparedStatement stmt = connection.prepareStatement(
					MessageFormat.format("insert into {0} (docId, terms) values (?,?)", this.documentTermsTable))) {

				StringBuilder terms = new StringBuilder();
				for(String term : foundTerms) {
					terms.append(term).append(" ");
				}
				
				stmt.setString(1, docId);
				stmt.setString(2, terms.toString());
				stmt.execute();
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	@Override
	public void addFoundTerm(String term) {
		try(Connection connection = connectionManager.getConnection()) {
			try(PreparedStatement stmt = connection.prepareStatement(
					MessageFormat.format("insert into {0} (term) values (?)", this.foundTermsTable))) {
				
				stmt.setString(1, term);
				stmt.execute();
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	@Override
	public void addNewTerm(String term) {
		try(Connection connection = connectionManager.getConnection()) {
			try(PreparedStatement stmt = connection.prepareStatement(
					MessageFormat.format("insert into {0} (term) values (?)", this.newTermsTable))) {
				
				stmt.setString(1, term);
				stmt.execute();
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	@Override
	public void addUniqueTerm(String term, long termFrequency, String document) {
//		try(Connection connection = connectionManager.getConnection()) {
//			try(PreparedStatement stmt = connection.prepareStatement(
//					MessageFormat.format("insert into {0} (term, tf, df) values (?,?,?)", this.uniqueTermsTable))) {
//				
//				stmt.setString(1, term);
//				stmt.setLong(2, termFrequency);
//				stmt.setLong(3, 1);
//				stmt.execute();
//			}
//		}
//		catch(Exception ex) {
//			log.error(ex.getMessage(), ex);
//		}
	}
	
	
	
	
	@Override
	public void increaseUniqueTermFrequencies(String term, long termFrequency, String document) {
//		try(Connection connection = connectionManager.getConnection()) {
//			try(PreparedStatement stmt = connection.prepareStatement(
//					MessageFormat.format("update {0} set tf = tf+?, df = df+? where term=?", this.uniqueTermsTable))) {
//				
//				stmt.setLong(1, termFrequency);
//				stmt.setLong(2, documentFrequency);
//				stmt.setString(3, term);
//				stmt.execute();
//			}
//		}
//		catch(Exception ex) {
//			log.error(ex.getMessage(), ex);
//		}
	}
	
	
	
	
	@Override
	public void clearFoundTerms() {
		try(Connection connection = connectionManager.getConnection()) {
			try(PreparedStatement stmt = connection.prepareStatement("truncate table " + this.foundTermsTable)) {
				stmt.execute();
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	@Override
	public void clearNewTerms() {
		try(Connection connection = connectionManager.getConnection()) {
			try(PreparedStatement stmt = connection.prepareStatement("truncate table " + this.newTermsTable)) {
				stmt.execute();
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	@Override
	public void dumpDocumentTerms(String analysisFileName) {
		// Stores the document terms list
		try(Connection connection = connectionManager.getConnection();
				DocumentTermsLogFile docTermsLog = DocumentTermsLogFile.newInstance(analysisFileName)) {
			
			try(PreparedStatement stmt = connection.prepareStatement(
					MessageFormat.format("select docId, terms from {0} order by id", this.documentTermsTable), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					ResultSet rs = stmt.executeQuery()) {
				
				rs.last();
				docTermsLog.writeLine(String.valueOf(rs.getRow()));
				rs.beforeFirst();
				
				while(rs.next()) {
					docTermsLog.writeLine(rs.getString("terms"));
				}
			}
			
			
			
//			try(PreparedStatement stmt = connection.prepareStatement(
//					MessageFormat.format("select docId from {0} group by docId order by min(id)", this.documentTermsTable), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//					ResultSet rs = stmt.executeQuery()) {
//				
//				rs.last();
//				docTermsLog.writeLine(String.valueOf(rs.getRow()));
//				rs.beforeFirst();
//				
//				while(rs.next()) {
//					try(PreparedStatement data = connection.prepareStatement(
//							MessageFormat.format("select term from {0} where docId=?", this.documentTermsTable))) {
//						
//						data.setString(1, rs.getString("docId"));
//						
//						try(ResultSet terms = data.executeQuery()) {
//							while(terms.next()) {
//								docTermsLog.write(terms.getString("term") + " ");
//							}
//						}
//						docTermsLog.writeLine("");
//					}
//				}
//			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	@Override
	public void dumpTFDF(String analysisFileName) {
		// Stores the term frequency/document frequency file
//		try(TFDFLogFile tfdfLog = TFDFLogFile.newInstance(analysisFileName)) {
//		
//			try(Connection connection = connectionManager.getConnection()) {
//				try(PreparedStatement stmt = connection.prepareStatement("select * from " + this.uniqueTermsTable);
//						ResultSet rs = stmt.executeQuery()) {
//					
//					while(rs.next()) {
//						tfdfLog.writeTFDF(new ImmutablePair<String, TFDF>(
//								rs.getString("term"),
//								new TFDF(
//										rs.getLong("tf"),
//										rs.getLong("df"))
//								));
//					}
//				}
//			}
//			catch(Exception ex) {
//				log.error(ex.getMessage(), ex);
//			}
//		}
	}
	
	
	
	
	@Override
	public boolean hasUniqueTerm(String term) {
		boolean exists = false;
		try(Connection connection = connectionManager.getConnection()) {
			try(PreparedStatement stmt = connection.prepareStatement(
					MessageFormat.format("select 1 from {0} where term=?", this.newTermsTable))) {

				stmt.setString(1, term);
				try(ResultSet rs = stmt.executeQuery()) {
					exists = rs.next();
				}
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		return exists;
	}
	
	
	
	
	@Override
	public List<String> getNewTerms() {
		List<String> terms = new ArrayList<String>();
		try(Connection connection = connectionManager.getConnection()) {
			try(PreparedStatement stmt = connection.prepareStatement("select * from " + this.newTermsTable);
					ResultSet rs = stmt.executeQuery()) {
				
				while(rs.next()) {
					terms.add(rs.getString("term"));
				}
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		return terms;
	}
	
	
	
	
	@Override
	public List<String> getFoundTerms() {
		List<String> terms = new ArrayList<String>();
		try(Connection connection = connectionManager.getConnection()) {
			try(PreparedStatement stmt = connection.prepareStatement("select * from " + this.foundTermsTable);
					ResultSet rs = stmt.executeQuery()) {
				
				while(rs.next()) {
					terms.add(rs.getString("term"));
				}
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		return terms;
	}
	
	
	
	
	@Override
	public Set<Entry<String, TFDF>> getUniqueTerms() {
//		Set<Entry<String, TFDF>> result = new HashSet<Entry<String,TFDF>>();
//		
//		try(Connection connection = connectionManager.getConnection()) {
//			try(PreparedStatement stmt = connection.prepareStatement("select * from " + this.uniqueTermsTable);
//					ResultSet rs = stmt.executeQuery()) {
//				
//				while(rs.next()) {
//					result.add(new ImmutablePair<String, TFDF>(
//							rs.getString("term"),
//							new TFDF(
//									rs.getLong("tf"),
//									rs.getLong("df"))
//							));
//				}
//			}
//		}
//		catch(Exception ex) {
//			log.error(ex.getMessage(), ex);
//		}
//		return result;
		
		return null;
	}
	
	
	
	
	@Override
	public int getUniqueTermsCount() {
		int size = 0;
		try(Connection connection = connectionManager.getConnection()) {
			try(PreparedStatement stmt = connection.prepareStatement("select count(1) as size from " + this.uniqueTermsTable);
					ResultSet rs = stmt.executeQuery()) {
				
				if(rs.next()) {
					size = rs.getInt("size");
				}
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		return size;
	}

}
