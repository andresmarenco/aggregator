package aggregator.sampler.indexer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import aggregator.beans.WikiEntry;
import aggregator.beans.WikiEntry.WikiEntryRevision;
import aggregator.dataaccess.DirectConnectionManager;
import aggregator.dataaccess.PooledConnectionManager;
import aggregator.dataaccess.WikiDAO;
import aggregator.util.CommonUtils;
import aggregator.util.analysis.AggregatorAnalyzer;

public class WikiIndexer {
	
	private WikiDAO wikiDAO;
	private Log log = LogFactory.getLog(WikiIndexer.class);
	private static final int THREAD_POOL_SIZE = 10;
	
	/**
	 * Default Constructor
	 */
	public WikiIndexer() {
		this.wikiDAO = new WikiDAO(PooledConnectionManager.getInstance());
//		this.wikiDAO = new WikiDAO(DirectConnectionManager.getInstance());
	}
	
	
	/**
	 * Indexes the Wikipedia dump
	 */
	public void execute() {
		try(Analyzer analyzer = new AggregatorAnalyzer(CommonUtils.LUCENE_VERSION))
		{
			log.info("Starting Wikipedia indexing process...");
			
			// Lucene Index
			log.info("Creating Lucene Index...");
			Path indexPath = CommonUtils.getIndexPath().resolve(AbstractSamplerIndexer.getWikiIndexName());
			Directory index = new NIOFSDirectory(indexPath.toFile());
			
			IndexWriterConfig config = new IndexWriterConfig(CommonUtils.LUCENE_VERSION, analyzer);
			
			try(IndexWriter writer = new IndexWriter(index, config)) {
				// XML Dump of Wiki
				log.info("Reading Wikipedia dump...");
				XMLInputFactory inputFactory = XMLInputFactory.newInstance();
				
				String[] files = { "/mnt/data/enwiki-latest-pages-articles.xml.000",
						"/mnt/data/enwiki-latest-pages-articles.xml.001",
						"/mnt/data/enwiki-latest-pages-articles.xml.002",
						"/mnt/data/enwiki-latest-pages-articles.xml.003",
						"/mnt/data/enwiki-latest-pages-articles.xml.004",
						"/mnt/data/enwiki-latest-pages-articles.xml.005"};
				
				for(String f : files) {
				
				
					try(InputStream in = new FileInputStream(f)) {
						XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
						
						WikiEntry entry = null;
						WikiEntryRevision revision = null;
						
						while(reader.hasNext()) {
							int event = reader.next();
							
							switch(event) {
							case XMLStreamConstants.START_ELEMENT: {
								String tagName = reader.getLocalName();
								if("page".equals(tagName)) {
									entry = new WikiEntry();
								} else if("revision".equals(tagName)) {
									revision = entry.newEntryRevision();
								} else if("redirect".equals(tagName)) {
									entry.setRedirect(reader.getAttributeValue(0));
								} else if("text".equals(tagName)) {
									revision.setText(reader.getElementText());
								} else if("id".equals(tagName)) {
									if(revision == null) {
										entry.setId(Long.parseLong(reader.getElementText()));
									} else {
										revision.setId(Long.parseLong(reader.getElementText()));
									}
								} else if("ns".equals(tagName)) {
									entry.setNamespace(Integer.parseInt(reader.getElementText()));
								} else if("title".equals(tagName)) {
									entry.setTitle(reader.getElementText());
								}
								break;
							}
							
							case XMLStreamConstants.END_ELEMENT: {
								String tagName = reader.getLocalName();
								if("revision".equals(tagName)) {
									entry.getRevisions().add(revision);
									revision = null;
								} else if("page".equals(tagName)) {
									log.info(MessageFormat.format("Adding Wikipedia entry {0} ({1})", entry.getTitle(), String.valueOf(entry.getId())));
									wikiDAO.insertEntry(entry);
									
									
//									switch(entry.getNamespace()) {
//									
//									case WikiEntry.NAMESPACE_ARTICLE: {
//										WikiEntryRevision lastRevision = entry.getLastRevision();
//										
//										for(String category : lastRevision.getCategories()) {
////											System.out.println(category);
//											if(category.startsWith("Disambiguation") || category.startsWith("Lists") ||
//													category.equals("Set indices") ||
//													category.startsWith("List-Class") || 
//													category.startsWith("Template-Class") || category.startsWith("Templates")) {
//												entry.setContentPage(false);
////												System.out.println("NO CONTENT!!!!!!!!!!!!!!!!!!!!!!");
//												break;
//											}
//										}
//										
//										if(entry.isContentPage()) {
//											log.info(MessageFormat.format("Indexing Wikipedia entry {0} ({1})", entry.getTitle(), String.valueOf(entry.getId())));
//											
//											Document wikiDoc = new Document();
//											wikiDoc.add(new LongField(AbstractSamplerIndexer.INDEX_DOC_NAME_FIELD,  entry.getId(), Field.Store.YES));
//											wikiDoc.add(new TextField(AbstractSamplerIndexer.INDEX_CONTENTS_FIELD, lastRevision.getText(), Field.Store.YES));
//											wikiDoc.add(new StringField(AbstractSamplerIndexer.INDEX_TITLE_FIELD, entry.getTitle(), Field.Store.YES));
//											
//											writer.addDocument(wikiDoc);
//										}
//										
//										break;
//									}
//									}
									
									
								}
								break;
							}
							
							}
						}
					}
				}
			}
			
//			executor.shutdown();
//			executor.awaitTermination(1, TimeUnit.DAYS);
			
			log.info("Wikipedia indexing process finished successfully...");
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	private static class WikiEntryDatabase implements Runnable {
		private WikiEntry entry;
		private WikiDAO wikiDAO;
		private Log log = LogFactory.getLog(getClass());

		public WikiEntryDatabase(WikiDAO wikiDAO, WikiEntry entry) {
			super();
			this.entry = entry;
			this.wikiDAO = wikiDAO;
		}
		
		@Override
		public void run() {
			try
			{
				log.info(MessageFormat.format("Adding Wikipedia entry {0} ({1})", entry.getTitle(), String.valueOf(entry.getId())));
				wikiDAO.insertEntry(entry);
			}
			catch(Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
	}
	
	
	
	private static class WikiEntryIndexer implements Runnable {
		private IndexWriter writer;
		private WikiEntry entry;
		private Log log = LogFactory.getLog(getClass());
		
		public WikiEntryIndexer(IndexWriter writer, WikiEntry entry) {
			super();
			this.writer = writer;
			this.entry = entry;
		}


		@Override
		public void run() {
			if((entry.getNamespace() == WikiEntry.NAMESPACE_ARTICLE) && (!entry.isRedirect())) {
				try
				{
					log.info(MessageFormat.format("Indexing Wikipedia entry {0} ({1})", entry.getTitle(), String.valueOf(entry.getId())));
					WikiEntryRevision lastRevision = entry.getLastRevision();
					
					Document wikiDoc = new Document();
					wikiDoc.add(new LongField(AbstractSamplerIndexer.INDEX_DOC_NAME_FIELD,  entry.getId(), Field.Store.YES));
					wikiDoc.add(new TextField(AbstractSamplerIndexer.INDEX_CONTENTS_FIELD, lastRevision.getText(), Field.Store.YES));
					
					writer.addDocument(wikiDoc);
				}
				catch(Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
		}
	}
}
