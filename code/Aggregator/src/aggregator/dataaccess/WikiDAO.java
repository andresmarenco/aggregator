package aggregator.dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;

import aggregator.beans.WikiEntry;
import aggregator.beans.WikiEntry.WikiEntryRevision;
import aggregator.util.LRUMap;

public class WikiDAO {

	private ConnectionManager connectionManager;
	private Log log = LogFactory.getLog(WikiDAO.class);
	private LRUMap<String, Long> categoryCache;
	
	/**
	 * Default Constructor
	 * @param connectionManager Connection Manager Instance
	 */
	public WikiDAO(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
		this.categoryCache = new LRUMap<String, Long>(1300000);
		
		try(Connection connection = connectionManager.getConnection();
				PreparedStatement stmt = connection.prepareStatement("select id, title from wiki_entry_cache");
				ResultSet rs = stmt.executeQuery()) {
			
			log.info("Pre-filling cache...");
			while(rs.next()) {
				this.categoryCache.put(rs.getString("title"), rs.getLong("id"));
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	public void reindex() {
		try(Connection connection = connectionManager.getConnection();
				PreparedStatement stmt = connection.prepareStatement("reindex table wiki_entry")) {
			
			stmt.execute();
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	/**
	 * Inserts a Wikipedia entry into the database
	 * @param entry Wikipedia entry
	 */
	public void insertEntry(WikiEntry entry) {
		if(entry != null) {
			switch(entry.getNamespace()) {
			case WikiEntry.NAMESPACE_ARTICLE: {
				this.insertArticle(entry);
				break;
			}
			
			case WikiEntry.NAMESPACE_CATEGORY: {
				this.insertCategory(entry);
				break;
			}
			}
		}
	}
	
	
	
	private void insertArticle(WikiEntry article) {
		WikiEntryRevision revision = article.getLastRevision();
		
		try(Connection connection = connectionManager.getConnection()) {
			try(PreparedStatement stmt = connection.prepareStatement("insert into wiki_entry (id, title, revision_id, wiki_namespace, redirect, content_page) values (?,?,?,?,?,?)")) {
				stmt.setLong(1, article.getId());
				stmt.setString(2, article.getTitle());
				stmt.setLong(3, revision.getId());
				stmt.setInt(4, WikiEntry.NAMESPACE_ARTICLE);
				stmt.setBoolean(5, article.isRedirect());
				stmt.setBoolean(6, article.isContentPage());
				
				stmt.execute();

				this.addCategories(connection, article, revision);
			}
			catch(PSQLException ex) {
				if(ex.getSQLState().equals("23505")) {
					log.error("Duplicate id in entry table");
					this.addCategories(connection, article, revision);
				} else {
					log.error(ex.getMessage(), ex);
				}
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	public boolean entryExists(WikiEntry entry) {
		boolean exists = false;
		try(Connection connection = connectionManager.getConnection()) {
			try(PreparedStatement stmt = connection.prepareStatement("select 1 from wiki_entry where id=?")) {
				stmt.setLong(1, entry.getId());
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
	
	
	
	private void insertCategory(WikiEntry category) {
		try(Connection connection = connectionManager.getConnection()) {
			WikiEntryRevision revision = category.getLastRevision();
			
			long categoryId = findCategory(connection, category.getTitle());
			if(categoryId == -1) {
				try(PreparedStatement insertStmt = connection.prepareStatement("insert into wiki_entry (id, title, revision_id, wiki_namespace, redirect, content_page) values (?,?,?,?,?,?)")) {
					insertStmt.setLong(1, category.getId());
					insertStmt.setString(2, category.getTitle());
					insertStmt.setLong(3, revision.getId());
					insertStmt.setInt(4, WikiEntry.NAMESPACE_CATEGORY);
					insertStmt.setBoolean(5, category.isRedirect());
					insertStmt.setBoolean(6, category.isContentPage());
					insertStmt.execute();
					
					this.categoryCache.put(category.getTitle(), category.getId());
					this.addCategories(connection, category, revision);
				}
				catch(PSQLException ex) {
					if(ex.getSQLState().equals("23505")) {
						log.error("Duplicate category for entry");
						this.addCategories(connection, category, revision);
					} else {
						log.error(ex.getMessage(), ex);
					}
				}
				
			} else if(categoryId != category.getId()) {
				log.info(MessageFormat.format("Updating entry for category: {0} -> {1} ({2})", String.valueOf(categoryId), String.valueOf(category.getId()), category.getTitle()));
				try(PreparedStatement updateStmt = connection.prepareStatement("update wiki_entry set id=?, revision_id=?, content_page=? where id=?")) {
					updateStmt.setLong(1, category.getId());
					updateStmt.setLong(2, revision.getId());
					updateStmt.setBoolean(3, category.isContentPage());
					updateStmt.setLong(4, categoryId);
					updateStmt.execute();
					
					this.categoryCache.put(category.getTitle(), category.getId());
					this.addCategories(connection, category, revision);
				}
				catch(PSQLException ex) {
					if(ex.getSQLState().equals("23505")) {
						log.error("Duplicate category for entry");
						this.addCategories(connection, category, revision);
					} else {
						log.error(ex.getMessage(), ex);
					}
				}
			}
			
			
			
//			if(!entryExists(category)) {
//				
//				if(categoryId == -1) {
//					
//				} else if(categoryId != category.getId()) {
//					
//				}
//			}
			
			
			
			

//			if(categoryId != category.getId()) {
//				if(!entryExists(category)) {
//					log.info(MessageFormat.format("Updating entry for category: {0} -> {1}", categoryId, category.getTitle()));
//					try(PreparedStatement updateStmt = connection.prepareStatement("update wiki_entry set id=?, revision_id=?, content_page=? where id=?")) {
//						updateStmt.setLong(1, category.getId());
//						updateStmt.setLong(2, revision.getId());
//						updateStmt.setBoolean(3, category.isContentPage());
//						updateStmt.setLong(4, categoryId);
//						updateStmt.execute();
//						
//						this.categoryCache.put(category.getTitle(), category.getId());
//					}
//				}
//			}
			
//			long categoryId = -1;
//			Long cacheData = this.categoryCache.get(category.getTitle());
//			if(cacheData == null) {
//				try(PreparedStatement stmt = connection.prepareStatement("select id from wiki_entry where title=? and wiki_namespace=?")) {
//					stmt.setString(1, category.getTitle());
//					stmt.setInt(2, WikiEntry.NAMESPACE_CATEGORY);
//				
//					try(ResultSet rs = stmt.executeQuery()) {
//						if(rs.next()) {
//							categoryId = rs.getLong("id");
//							if(categoryId != category.getId()) {
//								log.info(MessageFormat.format("Updating entry for category: {0} -> {1}", categoryId, category.getTitle()));
//								try(PreparedStatement updateStmt = connection.prepareStatement("update wiki_entry set id=?, revision_id=?, content_page=? where id=?")) {
//									updateStmt.setLong(1, category.getId());
//									updateStmt.setLong(2, revision.getId());
//									updateStmt.setLong(3, categoryId);
//									updateStmt.setBoolean(4, category.isContentPage());
//									updateStmt.execute();
//									
//									this.categoryCache.put(category.getTitle(), category.getId());
//								}
//							}
//						}
//					}
//				}
//				catch(Exception ex) {
//					log.error(ex.getMessage(), ex);
//				}
//			} else {
//				categoryId = cacheData.longValue();
//			}
//			
//			if(categoryId == -1) {
//				try(PreparedStatement insertStmt = connection.prepareStatement("insert into wiki_entry (id, title, revision_id, wiki_namespace, redirect, content_page) values (?,?,?,?,?,?)")) {
//					insertStmt.setLong(1, category.getId());
//					insertStmt.setString(2, category.getTitle());
//					insertStmt.setLong(3, revision.getId());
//					insertStmt.setInt(4, WikiEntry.NAMESPACE_CATEGORY);
//					insertStmt.setBoolean(5, category.isRedirect());
//					insertStmt.setBoolean(6, category.isContentPage());
//					insertStmt.execute();
//					
//					this.categoryCache.put(category.getTitle(), category.getId());
//				}			
//			}
//			
//			
//			this.addCategories(connection, category, revision);
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	private void addCategories(Connection connection, WikiEntry entry, WikiEntryRevision revision) {
		log.info(MessageFormat.format("Adding categories to entry {0}...", entry.getTitle()));
		
		for(String category : revision.getCategories()) {
			long categoryId = this.createCategory(connection, category);
			
			if(categoryId > -1) {
				try(PreparedStatement stmt = connection.prepareStatement("insert into wiki_entry_by_category (entry_id, category_id) values (?,?)")) {
					stmt.setLong(1, entry.getId());
					stmt.setLong(2, categoryId);
					
					stmt.execute();
				}
				catch(PSQLException ex) {
					if(ex.getSQLState().equals("23505")) {
						log.error(MessageFormat.format("Duplicate category {0} for entry {1}", categoryId, entry.getId()));
					} else {
						log.error(ex.getMessage(), ex);
					}
				}
				catch(Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		}
		
		
		
//		try(PreparedStatement stmt = connection.prepareStatement("insert into wiki_entry_by_category (entry_id, category_id) values (?,?)")) {
//			
//			for(String category : revision.getCategories()) {
//				long categoryId = this.createCategory(connection, category);
//				
//				if(categoryId > -1) {
//					stmt.setLong(1, entry.getId());
//					stmt.setLong(2, categoryId);
//
//					stmt.addBatch();
//				}
//			}
//			
//			stmt.executeBatch();
//		}
//		catch(Exception ex) {
//			log.error(ex.getMessage(), ex);
//		}
		
	}
	
	private long findCategory(Connection connection, String name) {
		long categoryId = -1;
		
		Long cacheData = this.categoryCache.get(name);
		if(cacheData == null) {
			try(PreparedStatement stmt = connection.prepareStatement("select id from wiki_entry_cache where title=?")) {
				stmt.setString(1, name);
				try(ResultSet rs = stmt.executeQuery()) {
					if(rs.next()) {
						categoryId = rs.getLong("id");
						categoryCache.put(name, categoryId);
					}
				}
			}
			catch(Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		} else {
			categoryId = cacheData.longValue();
		}
		
		return categoryId;
	}
	
	
	private long createCategory(Connection connection, String name) {
//		long categoryId = -1;
////		name = name.toLowerCase();
//		
////		try(Connection connection = connectionManager.getConnection()) {
//		
//		Long cacheData = this.categoryCache.get(name);
//		if(cacheData == null) {
//			try(PreparedStatement stmt = connection.prepareStatement("select id from wiki_entry_cache where title=?")) {
//				stmt.setString(1, name);
//				try(ResultSet rs = stmt.executeQuery()) {
//					if(rs.next()) {
//						categoryId = rs.getLong("id");
//						categoryCache.put(name, categoryId);
//					}
//				}
//			}
//			catch(Exception ex) {
//				log.error(ex.getMessage(), ex);
//			}
//		} else {
//			categoryId = cacheData.longValue();
//		}
		
		
		long categoryId = findCategory(connection, name);
		
		
		
		
		if(categoryId == -1) {		
			try(PreparedStatement stmt = connection.prepareStatement("select id from wiki_entry where title=? and wiki_namespace=?")) {
				stmt.setString(1, name);
				stmt.setInt(2, WikiEntry.NAMESPACE_CATEGORY);
				try(ResultSet rs = stmt.executeQuery()) {
					if(rs.next()) {
						categoryId = rs.getLong("id");
					} else {
						// Inserting category
						log.info(MessageFormat.format("Creating category entry for: {0}", name));
						try(PreparedStatement insertStmt = connection.prepareStatement("insert into wiki_entry (title, revision_id, wiki_namespace, redirect) values (?,?,?,?) returning id")) {
							insertStmt.setString(1, name);
							insertStmt.setInt(2, 0);
							insertStmt.setInt(3, WikiEntry.NAMESPACE_CATEGORY);
							insertStmt.setBoolean(4, false);
							
							try(ResultSet insertRs = insertStmt.executeQuery()) {
								if(insertRs.next()) {
									categoryId = insertRs.getLong(1);
								} else {
									log.error("No value returned after insert");
								}
							}
						}
					}
				}
			}
			catch(Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
		
		return categoryId;
	}
}
