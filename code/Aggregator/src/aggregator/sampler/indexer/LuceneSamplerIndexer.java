package aggregator.sampler.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;
import aggregator.beans.VerticalCollection;
import aggregator.beans.VerticalCollection.VerticalCollectionData;
import aggregator.dataaccess.DirectConnectionManager;
import aggregator.dataaccess.VerticalDAO;
import aggregator.sampler.output.IndexExecutionLogFile;
import aggregator.util.CommonUtils;
import aggregator.util.IterableNodeList;
import aggregator.util.XMLUtils;
import aggregator.util.analysis.AggregatorAnalyzer;
import aggregator.util.analysis.AggregatorTokenizerAnalyzer;
import aggregator.verticalselection.AbstractSelectionModel;

public class LuceneSamplerIndexer extends AbstractSamplerIndexer {
	
	private Analyzer analyzer;
	
	/**
	 * Default Constructor
	 */
	public LuceneSamplerIndexer() {
		this.analyzer = new AggregatorTokenizerAnalyzer(CommonUtils.LUCENE_VERSION);
	}

	
	/**
	 * Default Constructor
	 * @param vertical Vertical to index
	 */
	public LuceneSamplerIndexer(Vertical vertical) {
		super(vertical);
		this.analyzer = new AggregatorTokenizerAnalyzer(CommonUtils.LUCENE_VERSION);
	}

	@Override
	public List<String> tokenize(SampledDocument<?> document) {
		intermediateResults.clearFoundTerms();
		intermediateResults.clearNewTerms();
		
		try
		{
			HashMap<String, Integer> termFrequency = new HashMap<String, Integer>();
			List<Entry<String, String>> docText = samplerParser.parseDocument(document);
			
			// Joins the document elements
			StringBuilder stringBuilder = new StringBuilder();
			for(Entry<String, String> entry : docText) {
				stringBuilder.append(entry.getValue()).append(" ");
			}
			
			try(TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(stringBuilder.toString()))) {
				tokenStream.reset();
				
				// Tokenizes the document elements
			    while (tokenStream.incrementToken()) {
			    	String term = tokenStream.getAttribute(CharTermAttribute.class).toString();
			    	if(samplerParser.isValid(term)) {
			    		intermediateResults.addFoundTerm(term);
			    		
			    		Integer tf = termFrequency.get(term);
			    		if(tf == null) {
			    			termFrequency.put(term, 1);
			    		} else {
			    			termFrequency.put(term, tf+1);
			    		}
			    	}
			    }

				
				// Calculates the global term frequency/document frequency
				for(Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
					if(!intermediateResults.hasUniqueTerm(entry.getKey())) {
						intermediateResults.addUniqueTerm(entry.getKey(), entry.getValue());
						intermediateResults.addNewTerm(entry.getKey());
					} else {
						intermediateResults.increaseUniqueTermFrequencies(entry.getKey(), entry.getValue(), 1);
					}
				}
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return intermediateResults.getFoundTerms();
	}

	@Override
	public void storeIndex(String analysisPath, VerticalCollection collection) {
		analysisPath = StringUtils.appendIfMissing(analysisPath, File.separator);
		String indexName = getIndexName();
		VerticalDAO verticalDAO = new VerticalDAO(DirectConnectionManager.getInstance());
		
		
		try(Analyzer indexerAnalyzer = new AggregatorAnalyzer(CommonUtils.LUCENE_VERSION);
				IndexExecutionLogFile indexLog = IndexExecutionLogFile.newInstance(
						MessageFormat.format("{0}{1}_{2}_{3}.{4}",
								analysisPath,
								collection.getId(),
								indexName,
								CommonUtils.getTimestampString(),
								"{docType}")))
								
		{
			long startTime = System.currentTimeMillis();
			log.info(MessageFormat.format("Starting indexing process for {0} index...", indexName));
			indexLog.writeLogMessage("Starting indexing process for {0} index...", indexName);
			
			Path indexPath = this.indexPath.resolve(collection.getId()).resolve(indexName);
			Directory index = new NIOFSDirectory(indexPath.toFile());
			
			IndexWriterConfig config = new IndexWriterConfig(CommonUtils.LUCENE_VERSION, indexerAnalyzer);
			config.setSimilarity(AbstractSelectionModel.getDefaultSimilary());
			
			try(IndexWriter writer = new IndexWriter(index, config)) {
				
				for(VerticalCollectionData verticalData : collection.getVerticals()) {
					Indexer indexer = new BasicIndexer(analysisPath, verticalData, indexLog, writer);
					if(isIndexDocs()) indexer = new DocumentIndexer(indexer);
					if(isIndexSnippets()) indexer = new SnippetIndexer(indexer);
					
					verticalData.setSampleSize(0);
					
					// Reads the analyzed documents
					try(Stream<Path> pathList = Files.walk(CommonUtils.getAnalysisPath().resolve(analysisPath).resolve(verticalData.getVerticalCollectionId()), 1)) {
						Iterator<Path> pathIterator = pathList.iterator();
						Path filePath;
						
						while(pathIterator.hasNext()) {
							filePath = pathIterator.next();
							
							if (Files.isRegularFile(filePath)) {
								if(filePath.getFileName().toString().endsWith(".docs")) {
									log.info(MessageFormat.format("Reading {0} documents file...", filePath.getFileName().toString()));
									indexLog.writeLogMessage("Reading {0} documents file...", filePath.getFileName().toString());
									
									try(BufferedReader docsReader = new BufferedReader(new FileReader(filePath.toFile()))) {
										indexer.resetCurrentDocsFile(filePath);
										
										// Get rid of the header line
										String docLine = docsReader.readLine();
										while((docLine = docsReader.readLine()) != null) {
											String docName = docLine.substring(0, docLine.indexOf(","));
											writer.addDocument(indexer.indexDocument(docName));
											
											verticalData.setSampleSize(verticalData.getSampleSize()+1);
										}
										
										log.info("Documents successfully indexed!");
										indexLog.writeLogMessage("Documents successfully indexed!");
									}
									catch(IOException ex) {
										log.error("Error reading files");
									}
								}
							}
							
						}
					
					}
					
					
					verticalDAO.updateSampleSize(collection.getId(), verticalData.getVertical().getId(), verticalData.getSampleSize());
				}
			}
			
			// Logging statistics
			long endTime = System.currentTimeMillis();
			log.info("Indexing process finished. See log for more details");
			indexLog.writeLogMessage("Indexing process finished in {0}", indexLog.getTimePeriodMessage(startTime, endTime));
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	
	protected abstract class Indexer {
		protected String analysisPath;
		protected VerticalCollectionData verticalData;
		protected IndexExecutionLogFile indexLog;
		protected IndexWriter writer;
		
		public Indexer(String analysisPath, VerticalCollectionData verticalData, IndexExecutionLogFile indexLog, IndexWriter writer) {
			this.analysisPath = analysisPath;
			this.verticalData = verticalData;
			this.indexLog = indexLog;
			this.writer = writer;
		}
		
		protected abstract Document indexDocument(String docName);
		protected abstract void resetCurrentDocsFile(Path filePath);
	}
	
	
	
	
	protected class BasicIndexer extends Indexer {

		public BasicIndexer(String analysisPath, VerticalCollectionData verticalData, IndexExecutionLogFile indexLog, IndexWriter writer) {
			super(analysisPath, verticalData, indexLog, writer);
		}

		@Override
		protected Document indexDocument(String docName) {
			indexLog.writeLogMessage("Indexing {0}...", docName);
				
			// Creating document for the index
			Document indexDoc = new Document();
			indexDoc.add(new StringField(INDEX_DOC_NAME_FIELD, docName, Field.Store.YES));
			indexDoc.add(new StringField(INDEX_VERTICAL_FIELD, verticalData.getVertical().getId(), Field.Store.YES));
			
			return indexDoc;
		}

		@Override
		protected void resetCurrentDocsFile(Path filePath) {
		}
	}
	
	
	
	
	protected abstract class IndexerDecorator extends Indexer {
		private Indexer decoratedIndexer;
		
		public IndexerDecorator(Indexer indexer) {
			super(indexer.analysisPath, indexer.verticalData, indexer.indexLog, indexer.writer);
			this.decoratedIndexer = indexer;
		}
		
		@Override
		protected Document indexDocument(String docName) {
			return decoratedIndexer.indexDocument(docName);
		}
		
		@Override
		protected void resetCurrentDocsFile(Path filePath) {
			decoratedIndexer.resetCurrentDocsFile(filePath);
		}
	}
	
	
	
	
	protected class DocumentIndexer extends IndexerDecorator {
		protected Path docTerms;
		protected BufferedReader termsReader;
		
		public DocumentIndexer(Indexer indexer) {
			super(indexer);
		}
		
		@Override
		protected void resetCurrentDocsFile(Path filePath) {
			super.resetCurrentDocsFile(filePath);
			
			try
			{
				if(this.termsReader != null) {
					this.termsReader.close();
				}
				
				this.docTerms = filePath.resolveSibling(StringUtils.removeEnd(filePath.getFileName().toString(), ".docs").concat(".docTerms"));
				this.termsReader = new BufferedReader(new FileReader(docTerms.toFile()));
				
				log.info(MessageFormat.format("Reading {0} terms file...", docTerms.getFileName().toString()));
				indexLog.writeLogMessage("Reading {0} terms file...", docTerms.getFileName().toString());
				
				
				// Get rid of the header line
				termsReader.readLine();
			}
			catch(Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
		
		@Override
		protected Document indexDocument(String docName) {
			Document result = super.indexDocument(docName);
			
			try
			{
				String terms = result.get(INDEX_CONTENTS_FIELD);
				if(StringUtils.isNotBlank(terms)) {
					terms = terms.concat(" ").concat(termsReader.readLine());
				} else {
					terms = termsReader.readLine();
				}
				
				result.add(new TextField(INDEX_CONTENTS_FIELD, terms, Field.Store.NO));
			}
			catch(Exception ex) {
				log.error(ex.getMessage(), ex);
			}
			
			return result;
		}
	}
	
	
	
	
	protected class SnippetIndexer extends IndexerDecorator {
		protected Map<String, SnippetCache> snippetCache;
		protected XMLUtils xmlUtils;
		
		public SnippetIndexer(Indexer indexer) {
			super(indexer);
			
			this.xmlUtils = new XMLUtils();
			this.initSnippetsCache();
		}
		
		private void initSnippetsCache() {
			try
			{
				// Reading snippets XML document
				String snippetsFileName = analysisPath.replace("docs", "search");
				Path snippetsPath = CommonUtils.getSamplePath().resolve(snippetsFileName).resolve(verticalData.getVerticalCollectionId()).resolve(verticalData.getVerticalCollectionId().concat(".xml"));
	
				log.info(MessageFormat.format("Reading snippets file {0}", snippetsPath.getFileName().toString()));
				indexLog.writeLogMessage("Reading snippets file {0}", snippetsPath.getFileName().toString());
				
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				org.w3c.dom.Document xmlDoc = dBuilder.parse(snippetsPath.toFile());
				
				this.snippetCache = new HashMap<String, SnippetCache>();
				NodeList snippetNodes = xmlUtils.executeXPath(xmlDoc, "./samples/search_results/snippets/snippet", NodeList.class);
				for(Node snippet : new IterableNodeList(snippetNodes)) {
					String titleNode = null, descriptionNode = null, linkNode = null;
					for(Node snippetChild : new IterableNodeList(snippet.getChildNodes())) {
						if(snippetChild.getNodeName().equals("link")) {
							Node cacheAttribute = snippetChild.getAttributes().getNamedItem("cache");
							if(cacheAttribute != null) {
								linkNode = snippetChild.getAttributes().getNamedItem("cache").getTextContent();
							}
						} else if(snippetChild.getNodeName().equals("title")) {
							titleNode = snippetChild.getTextContent().replaceAll("\\s+", " ");
						} else if(snippetChild.getNodeName().equals("description")) {
							descriptionNode = snippetChild.getTextContent().replaceAll("\\s+", " ");
						}
					}
					
					if(linkNode != null) {
						SnippetCache cache = new SnippetCache();
						cache.setDescription(descriptionNode);
						cache.setTitle(titleNode);
						snippetCache.put(linkNode, cache);
					}
				}
				
				// To empty the doc from main memory (the garbage collector should take care of this)
				xmlDoc = null;
			}
			catch(Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
		
		
		@Override
		protected Document indexDocument(String docName) {
			Document result = super.indexDocument(docName);
			
			// Finds the snippetNode
			String snippetKey = MessageFormat.format("{0}{1}{2}{3}", analysisPath, verticalData.getVerticalCollectionId(), File.separator, docName);
			indexLog.writeLogMessage("Retrieving snippet data from {0}...", snippetKey);
			SnippetCache cache = snippetCache.get(snippetKey);
			
			if(cache != null) {
				String snippetText = cache.getTitle();
				if(StringUtils.isNotBlank(snippetText)) {
					snippetText = snippetText.concat(" ").concat(cache.getDescription());
				}
				
				if(StringUtils.isNotBlank(snippetText)) {
					String terms = result.get(INDEX_CONTENTS_FIELD);
					if(StringUtils.isNotBlank(terms)) {
						terms = terms.concat(" ").concat(snippetText);
					} else {
						terms = snippetText;
					}
				}
			}
			
			return result;
		}
		
		
		
		
		private class SnippetCache {
			private String title;
			private String description;
			
			public SnippetCache() {}
			
			public String getTitle() { return title; }
			public void setTitle(String title) { this.title = StringUtils.isNotBlank(title) ? title : ""; }

			public String getDescription() { return description; }
			public void setDescription(String description) { this.description = StringUtils.isNotBlank(description) ? description : ""; }
		}
	}
	
	
	
//	
//	
//	
//	
//	
//	
//	
//	
//	
//	/**
//	 * Stores the documents from the given vertical
//	 * @param analysisPath Analysis path
//	 * @param verticalData Indexed vertical
//	 * @param indexLog Index log
//	 * @param writer Index writer
//	 * @throws IOException
//	 */
//	private void storeDocsIndex(String analysisPath, VerticalCollectionData verticalData, IndexExecutionLogFile indexLog, IndexWriter writer) throws IOException {
//		Files.walk(CommonUtils.getAnalysisPath().resolve(analysisPath).resolve(verticalData.getVerticalCollectionId()), 1).forEach(filePath -> {
//			if (Files.isRegularFile(filePath)) {
//				if(filePath.getFileName().toString().endsWith(".docs")) {
//					Path docTerms = filePath.resolveSibling(
//							StringUtils.removeEnd(filePath.getFileName().toString(), ".docs").concat(".docTerms"));
//					
//					log.info(MessageFormat.format("Reading {0} documents file with {1} terms file...", filePath.getFileName().toString(), docTerms.getFileName().toString()));
//					indexLog.writeLogMessage("Reading {0} documents file with {1} terms file...", filePath.getFileName().toString(), docTerms.getFileName().toString());
//					
//					try(BufferedReader docsReader = new BufferedReader(new FileReader(filePath.toFile()));
//							BufferedReader termsReader = new BufferedReader(new FileReader(docTerms.toFile()))) {
//						
//						// Get rid of the header line
//						String docLine = docsReader.readLine();
//						String termsLine = termsReader.readLine();
//						
//						while((docLine = docsReader.readLine()) != null) {
//							termsLine = termsReader.readLine();
//							Document indexDoc = this.createIndexDoc(docLine, termsLine, indexLog, verticalData);
//							writer.addDocument(indexDoc);
//						}
//						
//						log.info("Documents successfully indexed!");
//						indexLog.writeLogMessage("Documents successfully indexed!");
//					}
//					catch(IOException ex) {
//						log.error("Error reading files");
//					}
//				}
//			}
//		});
//	}
//	
//	
//	
//	
//	/**
//	 * Stores the snippets from the given vertical
//	 * @param analysisPath Analysis path
//	 * @param verticalData Indexed vertical
//	 * @param indexLog Index log
//	 * @param writer Index writer
//	 * @throws Exception
//	 */
//	private void storeSnippetsIndex(String analysisPath, VerticalCollectionData verticalData, IndexExecutionLogFile indexLog, IndexWriter writer) throws Exception {
//		
//		
//		
//
//		// Reads the analyzed documents
//		Files.walk(CommonUtils.getAnalysisPath().resolve(analysisPath).resolve(verticalData.getVerticalCollectionId()), 1).forEach(filePath -> {
//			if (Files.isRegularFile(filePath)) {
//				if(filePath.getFileName().toString().endsWith(".docs")) {
//					log.info(MessageFormat.format("Reading {0} documents file...", filePath.getFileName().toString()));
//					indexLog.writeLogMessage("Reading {0} documents file...", filePath.getFileName().toString());
//					
//					try(BufferedReader docsReader = new BufferedReader(new FileReader(filePath.toFile()))) {
//						
//						// Get rid of the header line
//						String docLine = docsReader.readLine();
//						
//						while((docLine = docsReader.readLine()) != null) {
//							String docName = docLine.substring(0, docLine.indexOf(","));
//							
//							Document indexDoc = new Document();
//							indexDoc.add(new StringField(INDEX_DOC_NAME_FIELD, docName, Field.Store.YES));
//							indexDoc.add(new StringField(INDEX_VERTICAL_FIELD, verticalData.getVertical().getId(), Field.Store.YES));
//							
//							// Finds the snippetNode
//							String snippetKey = MessageFormat.format("{0}{1}{2}{3}", analysisPath, verticalData.getVerticalCollectionId(), File.separator, docName);
//							indexLog.writeLogMessage("Retrieving snippet data from {0}...", snippetKey);
//							SnippetCache cache = snippetCache.get(snippetKey);
//							
//							if(cache != null) {
//								if(StringUtils.isNotBlank(cache.getTitle())) {
//									indexDoc.add(new TextField(INDEX_TITLE_FIELD, cache.getTitle(), Field.Store.NO));
//								}
//								
//								if(StringUtils.isNotBlank(cache.getDescription())) {
//									indexDoc.add(new TextField(INDEX_SNIPPET_FIELD, cache.getDescription(), Field.Store.NO));
//								}
//							}
//							
//							writer.addDocument(indexDoc);
//						}
//						
//						log.info("Documents successfully indexed!");
//						indexLog.writeLogMessage("Documents successfully indexed!");
//					}
//					catch(IOException ex) {
//						log.error("Error reading files");
//					}
//				}
//			}
//		});
//	}
//	
//	
//	
//	
//	/**
//	 * Stores the documents and the snippets from the given vertical
//	 * @param analysisPath Analysis path
//	 * @param verticalData Indexed vertical
//	 * @param indexLog Index log
//	 * @param writer Index writer
//	 * @throws Exception
//	 */
//	private void storeDocsSnippetsIndex(String analysisPath, VerticalCollectionData verticalData, IndexExecutionLogFile indexLog, IndexWriter writer) throws Exception {
//		
//		// Reading snippets XML document
//		String snippetsFileName = analysisPath.replace("docs", "search");
//		Path snippetsPath = CommonUtils.getSamplePath().resolve(snippetsFileName).resolve(verticalData.getVerticalCollectionId()).resolve(verticalData.getVerticalCollectionId().concat(".xml"));
//
//		log.info(MessageFormat.format("Reading snippets file {0}", snippetsPath.getFileName().toString()));
//		indexLog.writeLogMessage("Reading snippets file {0}", snippetsPath.getFileName().toString());
//		
//		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//		org.w3c.dom.Document xmlDoc = dBuilder.parse(snippetsPath.toFile());
//		
//		Map<String, SnippetCache> snippetCache = new HashMap<String, LuceneSamplerIndexer.SnippetCache>();
//		NodeList snippetNodes = xmlUtils.executeXPath(xmlDoc, "./samples/search_results/snippets/snippet", NodeList.class);
//		for(Node snippet : new IterableNodeList(snippetNodes)) {
//			String titleNode = null, descriptionNode = null, linkNode = null;
//			for(Node snippetChild : new IterableNodeList(snippet.getChildNodes())) {
//				if(snippetChild.getNodeName().equals("link")) {
//					Node cacheAttribute = snippetChild.getAttributes().getNamedItem("cache");
//					if(cacheAttribute != null) {
//						linkNode = snippetChild.getAttributes().getNamedItem("cache").getTextContent();
//					}
//				} else if(snippetChild.getNodeName().equals("title")) {
//					titleNode = snippetChild.getTextContent().replaceAll("\\s+", " ");
//				} else if(snippetChild.getNodeName().equals("description")) {
//					descriptionNode = snippetChild.getTextContent().replaceAll("\\s+", " ");
//				}
//			}
//			
//			if(linkNode != null) {
//				SnippetCache cache = new SnippetCache();
//				cache.setDescription(descriptionNode);
//				cache.setTitle(titleNode);
//				snippetCache.put(linkNode, cache);
//			}
//		}
//		
//		// To empty the doc from main memory (the garbage collector should take care of this)
//		xmlDoc = null;
//		
//
//		// Reads the analyzed documents
//		Files.walk(CommonUtils.getAnalysisPath().resolve(analysisPath).resolve(verticalData.getVerticalCollectionId()), 1).forEach(filePath -> {
//			if (Files.isRegularFile(filePath)) {
//				if(filePath.getFileName().toString().endsWith(".docs")) {
//					Path docTerms = filePath.resolveSibling(
//							StringUtils.removeEnd(filePath.getFileName().toString(), ".docs").concat(".docTerms"));
//					
//					log.info(MessageFormat.format("Reading {0} documents file with {1} terms file...", filePath.getFileName().toString(), docTerms.getFileName().toString()));
//					indexLog.writeLogMessage("Reading {0} documents file with {1} terms file...", filePath.getFileName().toString(), docTerms.getFileName().toString());
//					
//					try(BufferedReader docsReader = new BufferedReader(new FileReader(filePath.toFile()));
//							BufferedReader termsReader = new BufferedReader(new FileReader(docTerms.toFile()))) {
//						
//						// Get rid of the header line
//						String docLine = docsReader.readLine();
//						String termsLine = termsReader.readLine();
//						
//						while((docLine = docsReader.readLine()) != null) {
//							termsLine = termsReader.readLine();
//							Document indexDoc = this.createIndexDoc(docLine, termsLine, indexLog, verticalData);
//							
//							// Finds the snippetNode
//							String snippetKey = MessageFormat.format("{0}{1}{2}{3}", analysisPath, verticalData.getVerticalCollectionId(), File.separator, indexDoc.get(INDEX_DOC_NAME_FIELD));
//							indexLog.writeLogMessage("Retrieving snippet data from {0}...", snippetKey);
//							SnippetCache cache = snippetCache.get(snippetKey);
//							
//							if(cache != null) {
//								if(StringUtils.isNotBlank(cache.getTitle())) {
//									indexDoc.add(new TextField(INDEX_TITLE_FIELD, cache.getTitle(), Field.Store.NO));
//								}
//								
//								if(StringUtils.isNotBlank(cache.getDescription())) {
//									indexDoc.add(new TextField(INDEX_SNIPPET_FIELD, cache.getDescription(), Field.Store.NO));
//								}
//							}
//							
//							writer.addDocument(indexDoc);
//						}
//						
//						log.info("Documents successfully indexed!");
//						indexLog.writeLogMessage("Documents successfully indexed!");
//					}
//					catch(IOException ex) {
//						log.error("Error reading files");
//					}
//				}
//			}
//		});
//	}
//	
//	
//	
//	
//	/**
//	 * Creates a basic index document
//	 * @param docLine Line with the document data
//	 * @param termsLine Line with the document terms
//	 * @param indexLog Index log
//	 * @param verticalData Indexed vertical
//	 * @return Lucene document to index
//	 */
//	private Document createIndexDoc(String docLine, String termsLine, IndexExecutionLogFile indexLog, VerticalCollectionData verticalData) {
//		String docName = docLine.substring(0, docLine.indexOf(","));
//		String totalTerms = docLine.substring(0, docLine.lastIndexOf(","));
//		totalTerms = totalTerms.substring(totalTerms.lastIndexOf(",")+1);
//		
//		indexLog.writeLogMessage("Indexing {0} with {1} terms", docName, totalTerms);
//		
//		// Creating document for the index
//		Document indexDoc = new Document();
//		indexDoc.add(new TextField(INDEX_CONTENTS_FIELD, termsLine, Field.Store.NO));
//		indexDoc.add(new StringField(INDEX_DOC_NAME_FIELD, docName, Field.Store.YES));
//		indexDoc.add(new StringField(INDEX_VERTICAL_FIELD, verticalData.getVertical().getId(), Field.Store.YES));
//		
//		return indexDoc;
//	}




	@Override
	public void close() throws IOException {
		this.analyzer.close();
	}
}
