package aggregator.sampler.indexer;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.bouncycastle.crypto.prng.ThreadedSeedGenerator;

import aggregator.beans.SampledDocument;
import aggregator.beans.TFDF;
import aggregator.beans.Vertical;
import aggregator.beans.VerticalCollection;
import aggregator.beans.VerticalCollection.VerticalCollectionData;
import aggregator.dataaccess.DirectConnectionManager;
import aggregator.dataaccess.PooledConnectionManager;
import aggregator.dataaccess.VerticalDAO;
import aggregator.sampler.output.IndexExecutionLogFile;
import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;
import aggregator.util.analysis.AggregatorAnalyzer;
import aggregator.util.analysis.AggregatorTokenizerAnalyzer;
import aggregator.verticalselection.AbstractSelectionModel;

public class MultiSamplerIndexer extends AbstractSamplerIndexer {
	
private Analyzer analyzer;
	
	/**
	 * Default Constructor
	 */
	public MultiSamplerIndexer() {
		this.analyzer = new AggregatorTokenizerAnalyzer(CommonUtils.LUCENE_VERSION);
	}

	
	/**
	 * Default Constructor
	 * @param vertical Vertical to index
	 */
	public MultiSamplerIndexer(Vertical vertical) {
		super(vertical);
		this.analyzer = new AggregatorTokenizerAnalyzer(CommonUtils.LUCENE_VERSION);
	}

	@Override
	public List<String> tokenize(SampledDocument<?> document) {
		List<Entry<String, String>> docText = samplerParser.parseDocument(document);
		
		// Joins the document elements
		StringBuilder stringBuilder = new StringBuilder();
		for(Entry<String, String> entry : docText) {
			stringBuilder.append(entry.getValue()).append(" ");
		}
		
		return this.tokenize(document.getId(), stringBuilder);
	}
	
	
	
	
	@Override
	public List<String> tokenize(String docId, String text, String... more) {
		// Joins the document elements
		StringBuilder stringBuilder = new StringBuilder();
		
		if(StringUtils.isNotBlank(text)) {
			stringBuilder.append(text).append(" ");
		}
		
		for(String t : more) {
			if(StringUtils.isNotBlank(t)) {
				stringBuilder.append(t).append(" ");
			}
		}
		
		return this.tokenize(docId, stringBuilder);
	}
	
	
	
	
	/**
	 * Tokenizes the string builder data
	 * @param docId Document ID
	 * @param stringBuilder String builder data
	 * @return Tokens in the data
	 */
	private List<String> tokenize(String docId, StringBuilder stringBuilder) {
		intermediateResults.clearFoundTerms();
		intermediateResults.clearNewTerms();
		
		try
		{
			HashMap<String, Integer> termFrequency = new HashMap<String, Integer>();
			
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
						intermediateResults.addUniqueTerm(entry.getKey(), entry.getValue(), docId);
						intermediateResults.addNewTerm(entry.getKey());
					} else {
						intermediateResults.increaseUniqueTermFrequencies(entry.getKey(), entry.getValue(), docId);
					}
				}
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return intermediateResults.getFoundTerms();
	}
	
	
	
	private Path readFirstDocsFile(Set<String> docs, List<IndexExecutionLogFile> indexLogs, Path analysisPath) throws Exception {
		Path result = null;
		try(Stream<Path> pathList = Files.walk(analysisPath, 1)) {
			Iterator<Path> pathIterator = pathList.iterator();
			Path filePath;
			while(pathIterator.hasNext()) {
				filePath = pathIterator.next();
				if (Files.isRegularFile(filePath)) {
					if(filePath.getFileName().toString().endsWith(".docs")) {
						log.info(MessageFormat.format("Reading {0} documents file...", filePath.toString()));
						for(IndexExecutionLogFile indexLog : indexLogs) indexLog.writeLogMessage("Reading {0} documents file...", filePath.toString());
						int size = docs.size();
						
						try(BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
							String line = reader.readLine();
							while((line = reader.readLine()) != null) {
								docs.add(line.substring(0, line.indexOf(',')));
							}
						}
						
						for(IndexExecutionLogFile indexLog : indexLogs) indexLog.writeLogMessage("Adding {0} analyzed file(s) for index...", (docs.size() - size));
						
						result = filePath;
						break;
					}
				}
			}
		}
		
		return result;
	}
	
	private class IndexData implements Closeable {
		private VerticalCollection collection;
		private IndexExecutionLogFile indexLog;
		private IndexWriter writer;
		private int topWords;
		private int topDocs;
		private Path indexPath;
		private long startTime;
		
		public IndexData(String collectionId, VerticalDAO verticalDAO, Analyzer indexerAnalyzer, String indexName, int topWords, int topDocs) throws IOException {
			super();
			this.topWords = topWords;
			this.topDocs = topDocs;
			this.collection = verticalDAO.loadVerticalCollection(collectionId, indexName, true);
			
			this.indexLog = IndexExecutionLogFile.newInstance(
					MessageFormat.format("{0}{1}{2}_{3}.{4}",
							collection.getId(),
							File.separator,
							indexName,
							CommonUtils.getTimestampString(),
							"{docType}"));
			
			this.indexPath = CommonUtils.getIndexPath().resolve(collection.getId()).resolve(indexName);
			Directory index = new NIOFSDirectory(indexPath.toFile());
			
			IndexWriterConfig config = new IndexWriterConfig(CommonUtils.LUCENE_VERSION, indexerAnalyzer);
			config.setSimilarity(AbstractSelectionModel.getDefaultSimilary());
			
			this.writer = new IndexWriter(index, config);
			
			this.startTime = System.currentTimeMillis();
			log.info(MessageFormat.format("Starting indexing process for {0} index...", indexName));
			indexLog.writeLogMessage("Starting indexing process for {0} index...", indexName);
		}



		@Override
		public void close() throws IOException {
			// Logging statistics
			long endTime = System.currentTimeMillis();
			log.info("Indexing process finished. See log for more details");
			indexLog.writeLogMessage("Indexing process finished in {0}", indexLog.getTimePeriodMessage(startTime, endTime));
			
			this.indexLog.close();
			this.writer.close();
		}
	}
	
	
	private class MultiData extends ArrayList<IndexData> implements Closeable {
		private static final long serialVersionUID = 201410031726L;
		
		private List<IndexExecutionLogFile> indexLogs;
		private List<IndexWriter> indexWriters;
		private final int maxTopWords;
		private final int maxTopDocs;
		
		public MultiData(String collectionId, VerticalDAO verticalDAO, Analyzer indexerAnalyzer) throws IOException {
			this.add(new IndexData(collectionId, verticalDAO, indexerAnalyzer, "DSWd11", 1, 1));
			this.add(new IndexData(collectionId, verticalDAO, indexerAnalyzer, "DSWd13", 1, 3));
			this.add(new IndexData(collectionId, verticalDAO, indexerAnalyzer, "DSWd15", 1, 5));
			this.add(new IndexData(collectionId, verticalDAO, indexerAnalyzer, "DSWd31", 3, 1));
			this.add(new IndexData(collectionId, verticalDAO, indexerAnalyzer, "DSWd33", 3, 3));
			this.add(new IndexData(collectionId, verticalDAO, indexerAnalyzer, "DSWd35", 3, 5));
			this.add(new IndexData(collectionId, verticalDAO, indexerAnalyzer, "DSWd51", 5, 1));
			this.add(new IndexData(collectionId, verticalDAO, indexerAnalyzer, "DSWd53", 5, 3));
			this.add(new IndexData(collectionId, verticalDAO, indexerAnalyzer, "DSWd55", 5, 5));
			
			
			int topWords = 0, topDocs = 0;
			this.indexLogs = new ArrayList<IndexExecutionLogFile>();
			this.indexWriters = new ArrayList<IndexWriter>();
			for(IndexData d : this) {
				this.indexLogs.add(d.indexLog);
				this.indexWriters.add(d.writer);
				
				topWords = Math.max(topWords, d.topWords);
				topDocs = Math.max(topDocs, d.topDocs);
			}
			
			this.maxTopDocs = topDocs;
			this.maxTopWords = topWords;
		}
		
		@Override
		public void close() throws IOException {
			for(IndexData d : this) {
				d.close();
			}
		}
	}
	
	protected abstract class ThreadExecution implements Runnable {
		protected int threadCount;

		public ThreadExecution(int threadCount) {
			super();
			this.threadCount = threadCount;
		}
		
		
	}
	
	
	
	@Override
	public void storeIndex(VerticalCollection collection) {
		VerticalDAO verticalDAO = new VerticalDAO(PooledConnectionManager.getInstance());
		
		
		try(Analyzer indexerAnalyzer = new AggregatorAnalyzer(CommonUtils.LUCENE_VERSION);
				MultiData multiData = new MultiData(collection.getId(), verticalDAO, indexerAnalyzer))
		{
			Path wikiPath = CommonUtils.getIndexPath().resolve(getWikiIndexName());
			Path keywordsPath = CommonUtils.getAnalysisPath().resolve(collection.getId());
			final int threads = 16;
			final int collectionPart = collection.getVerticals().size() / threads;
			
			List<List<VerticalCollectionData>> threadCollections = new ArrayList<List<VerticalCollectionData>>();
			for(int t = 0; t < threads; t++) {
				threadCollections.add(collection.getVerticals().subList(t*collectionPart,
						(t == (threads-1)) ? collection.getVerticals().size() : (t*collectionPart)+collectionPart));
			}
			
			
			try(Directory wikiIndex = new NIOFSDirectory(wikiPath.toFile());
					IndexReader wikiReader = DirectoryReader.open(wikiIndex)) 
			{
				IndexSearcher wikiSearcher = new IndexSearcher(wikiReader);
				ExecutorService executor = Executors.newFixedThreadPool(threads);
				
				for(int threadCount = 0; threadCount < threadCollections.size(); threadCount++) {
					List<VerticalCollectionData> subCollection = threadCollections.get(threadCount);
					executor.execute(new ThreadExecution(threadCount) {
						
						@Override
						public void run() {
							try
							{
								for(int j = 0; j < subCollection.size(); j++) {
									VerticalCollectionData verticalData = subCollection.get(j);
									
									Set<String> docs = new LinkedHashSet<String>();
									Path docsPath = readFirstDocsFile(docs, multiData.indexLogs, CommonUtils.getAnalysisPath().resolve(collection.getId() + "-sample-docs").resolve(verticalData.getVerticalCollectionId()));
									int docsCount = docs.size();
										
									Path snippetsPath = readFirstDocsFile(docs, multiData.indexLogs, CommonUtils.getAnalysisPath().resolve(collection.getId() + "-sample-search").resolve(verticalData.getVerticalCollectionId()));
									int snippetsCount = docs.size();
										
									Indexer indexer = new BasicIndexer(indexerAnalyzer, verticalData, multiData);
									indexer = new VerticalMetaDataIndexer(indexer, isIndexVerticalName(), isIndexVerticalDescription());
										
									if(isIndexDocs()) indexer = new DocumentIndexer(indexer, docsPath, docsCount);
									if(isIndexSnippets()) indexer = new SnippetIndexer(indexer, snippetsPath, snippetsCount);
									if(isIndexWikiDocEnrichment()) indexer = new WikiDocEnrichment(indexer, keywordsPath, wikiIndex, wikiReader, wikiSearcher);
									
									int collectionVerticalIndex = (threadCount*collectionPart)+j;
									
									for(IndexData indexData : multiData) {
										indexData.collection.getVerticals().get(collectionVerticalIndex).setSampleSize(0);
									}
									
									for(String docID : docs) {
										List<Document> indexedDocs = indexer.indexDocument(docID);
										for(int i = 0; i < indexedDocs.size(); i++) {
											Document indexedDoc = indexedDocs.get(i);
											IndexData data = multiData.get(i);
										
											if(StringUtils.isNotBlank(indexedDoc.get(INDEX_CONTENTS_FIELD))) {
												data.writer.addDocument(indexedDoc);
												VerticalCollectionData multiVerticalData = multiData.get(i).collection.getVerticals().get(collectionVerticalIndex);
												multiVerticalData.setSampleSize(multiVerticalData.getSampleSize()+1);
											}
										}
									}
										
									for(IndexData indexData : multiData) {
										VerticalCollectionData multiVerticalData = indexData.collection.getVerticals().get(collectionVerticalIndex);
										verticalDAO.updateSampleSize(collection.getId(), multiVerticalData.getVerticalCollectionId(), multiVerticalData.getSampleSize());
									}
									
									
									log.info(MessageFormat.format("{0} documents successfully indexed!", verticalData.getSampleSize()));
									multiData.indexLogs.forEach(indexLog -> indexLog.writeLogMessage("{0} documents successfully indexed!", verticalData.getSampleSize()));
										
									indexer.close();
								}
							}
							catch(Exception ex) {
								log.error(ex.getMessage(), ex);
							}
						}
						
					});
					
				}
				
				executor.shutdown();
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			}
			
			
			
//			for(VerticalCollectionData verticalData : collection.getVerticals()) {
			
//			for(int j = 0; j < collection.getVerticals().size(); j++) {
//				VerticalCollectionData verticalData = collection.getVerticals().get(j);
//				
//				Set<String> docs = new LinkedHashSet<String>();
//				Path docsPath = this.readFirstDocsFile(docs, multiData.indexLogs, CommonUtils.getAnalysisPath().resolve(collection.getId() + "-sample-docs").resolve(verticalData.getVerticalCollectionId()));
//				int docsCount = docs.size();
//					
//				Path snippetsPath = this.readFirstDocsFile(docs, multiData.indexLogs, CommonUtils.getAnalysisPath().resolve(collection.getId() + "-sample-search").resolve(verticalData.getVerticalCollectionId()));
//				int snippetsCount = docs.size();
//					
//				Indexer indexer = new BasicIndexer(indexerAnalyzer, verticalData, multiData);
//				indexer = new VerticalMetaDataIndexer(indexer, isIndexVerticalName(), isIndexVerticalDescription());
//					
//				if(isIndexDocs()) indexer = new DocumentIndexer(indexer, docsPath, docsCount);
//				if(isIndexSnippets()) indexer = new SnippetIndexer(indexer, snippetsPath, snippetsCount);
//				if(isIndexWikiDocEnrichment()) indexer = new WikiDocEnrichment(indexer, keywordsPath);
//				
//				for(IndexData indexData : multiData) {
//					indexData.collection.getVerticals().get(j).setSampleSize(0);
//				}
//				
//				for(String docID : docs) {
//					List<Document> indexedDocs = indexer.indexDocument(docID);
//					for(int i = 0; i < indexedDocs.size(); i++) {
//						Document indexedDoc = indexedDocs.get(i);
//						IndexData data = multiData.get(i);
//					
//						if(StringUtils.isNotBlank(indexedDoc.get(INDEX_CONTENTS_FIELD))) {
//							data.writer.addDocument(indexedDoc);
//							VerticalCollectionData multiVerticalData = multiData.get(i).collection.getVerticals().get(j);
//							multiVerticalData.setSampleSize(multiVerticalData.getSampleSize()+1);
//						}
//					}
//				}
//					
//				for(IndexData indexData : multiData) {
//					VerticalCollectionData multiVerticalData = indexData.collection.getVerticals().get(j);
//					verticalDAO.updateSampleSize(collection.getId(), multiVerticalData.getVerticalCollectionId(), multiVerticalData.getSampleSize());
//				}
//				
//				
//				log.info(MessageFormat.format("{0} documents successfully indexed!", verticalData.getSampleSize()));
//				multiData.indexLogs.forEach(indexLog -> indexLog.writeLogMessage("{0} documents successfully indexed!", verticalData.getSampleSize()));
//					
//				indexer.close();
//			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	
	protected abstract class Indexer {
		protected VerticalCollectionData verticalData;
		protected MultiData multiData;
		protected Analyzer indexerAnalyzer;
		private Map<String, TFDF> tfdfs;
		private int docsCount;
		
		public Indexer(Analyzer indexerAnalyzer, VerticalCollectionData verticalData, MultiData multiData) {
			this.verticalData = verticalData;
			this.multiData = multiData;
			this.tfdfs = new HashMap<String, TFDF>();
			this.docsCount = 0;
			this.indexerAnalyzer = indexerAnalyzer;
		}
		
		protected abstract List<Document> indexDocument(String docName);
//		protected abstract void resetCurrentDocsFile(Path filePath);
		protected Map<String, TFDF> getTFDFs() { return this.tfdfs; };
		protected void close() {
			this.tfdfs.clear();
		}
		
		/**
		 * @return the docsCount
		 */
		public int getDocsCount() {
			return docsCount;
		}

		/**
		 * @param docsCount the docsCount to set
		 */
		public void setDocsCount(int docsCount) {
			this.docsCount = docsCount;
		}


		/**
		 * @param docsCount the docsCount to set
		 */
		public void incrementDocsCount(int docsCount) {
			this.docsCount += docsCount;
		}

		protected void addTFDFs(Path docsPath) {
			Path tfdfPath = docsPath.resolveSibling(StringUtils.removeEnd(docsPath.getFileName().toString(), ".docs").concat(".tfdf"));
			log.info(MessageFormat.format("Reading {0} TFDF file...", tfdfPath.toString()));
			multiData.indexLogs.forEach(indexLog -> indexLog.writeLogMessage("Reading {0} TFDF file...", tfdfPath.toString()));
			
//			for(Map.Entry<String, TFDF> entry : DocumentAnalysis.readTFDF(tfdfPath).entrySet()) {
//				
//				try(TokenStream tokenStream = indexerAnalyzer.tokenStream(null, entry.getKey())) {
//					tokenStream.reset();
//				
//					if(tokenStream.incrementToken()) {
//						String term = tokenStream.getAttribute(CharTermAttribute.class).toString();
//						
//						TFDF tfdf = this.getTFDFs().get(term);
//						if(tfdf == null) {
//							this.getTFDFs().put(term, entry.getValue());
//						} else {
//							tfdf.incrementTermFrequency(entry.getValue().getTermFrequency());
//							for(String tfdfDoc : entry.getValue().getDocuments()) {
//								tfdf.addDocument(tfdfDoc);
//							}
//						}
//					}
//				
//				}
//				catch(Exception ex) {
//					log.error(ex.getMessage(), ex);
//				}
//			}
		}
	}
	
	
	
	
	protected class BasicIndexer extends Indexer {

		public BasicIndexer(Analyzer indexerAnalyzer, VerticalCollectionData verticalData, MultiData multiData) {
			super(indexerAnalyzer, verticalData, multiData);
		}

		@Override
		protected List<Document> indexDocument(String docName) {
			List<Document> indexDocs = new ArrayList<Document>();
			
			for(IndexData data : multiData) {
				data.indexLog.writeLogMessage("Indexing {0}...", docName);
				
				// Creating document for the index
				Document indexDoc = new Document();
				indexDoc.add(new StringField(INDEX_DOC_NAME_FIELD, docName, Field.Store.YES));
				indexDoc.add(new StringField(INDEX_VERTICAL_FIELD, verticalData.getVertical().getId(), Field.Store.YES));
				
				indexDocs.add(indexDoc);
			}
			
			return indexDocs;
		}
	}
	
	
	
	
	protected abstract class IndexerDecorator extends Indexer {
		private Indexer decoratedIndexer;
		
		public IndexerDecorator(Indexer indexer) {
			super(indexer.indexerAnalyzer, indexer.verticalData, indexer.multiData);
			this.decoratedIndexer = indexer;
		}
		
		@Override
		protected List<Document> indexDocument(String docName) {
			return decoratedIndexer.indexDocument(docName);
		}
		
		@Override
		protected Map<String, TFDF> getTFDFs() {
			return decoratedIndexer.getTFDFs();
		}
		
		@Override
		protected void close() {
			decoratedIndexer.close();
		}
		
		@Override
		public int getDocsCount() {
			return decoratedIndexer.getDocsCount();
		}
		
		@Override
		public void setDocsCount(int docsCount) {
			decoratedIndexer.setDocsCount(docsCount);
		}
		
		@Override
		public void incrementDocsCount(int docsCount) {
			decoratedIndexer.incrementDocsCount(docsCount);
		}
	}
	
	
	
	protected class VerticalMetaDataIndexer extends IndexerDecorator {
		private boolean indexName;
		private boolean indexDescription;

		public VerticalMetaDataIndexer(Indexer indexer, boolean indexName, boolean indexDescription) {
			super(indexer);
			
			this.indexName = indexName;
			this.indexDescription = indexDescription;
		}
		
		@Override
		protected List<Document> indexDocument(String docName) {
			List<Document> result = super.indexDocument(docName);
			
			for(Document indexDoc : result) {
				if(indexName) {
					indexDoc.add(new TextField(INDEX_CONTENTS_FIELD, verticalData.getVertical().getName(), Field.Store.NO));
				}
				if(indexDescription) {
					indexDoc.add(new TextField(INDEX_CONTENTS_FIELD, verticalData.getVertical().getDescription(), Field.Store.NO));
				}
			}
			
			return result;
		}
	}
	
	
	
	
	protected class DocumentIndexer extends IndexerDecorator {
		protected Path docTerms;
		protected BufferedReader termsReader;
		protected Path docsPath;
		
		public DocumentIndexer(Indexer indexer, Path docsPath, int docsCount) {
			super(indexer);
			this.docsPath = docsPath;
			this.setDocsCount(docsCount);
			this.init();
		}
		
		
		private void init() {
			try
			{
				this.docTerms = docsPath.resolveSibling(StringUtils.removeEnd(docsPath.getFileName().toString(), ".docs").concat(".docTerms"));
				this.termsReader = new BufferedReader(new FileReader(docTerms.toFile()));
				
				log.info(MessageFormat.format("Reading {0} terms file...", docTerms.toString()));
				multiData.indexLogs.forEach(indexLog -> indexLog.writeLogMessage("Reading {0} terms file...", docTerms.toString()));
				
				// Get rid of the header line
				termsReader.readLine();
				
				this.addTFDFs(docsPath);
			}
			catch(Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
		
		@Override
		protected List<Document> indexDocument(String docName) {
			List<Document> result = super.indexDocument(docName);
			
			try
			{
				if(termsReader != null) {
					String terms = termsReader.readLine();
					if(terms != null) {
						for(Document indexDoc : result) {
							indexDoc.add(new TextField(INDEX_CONTENTS_FIELD, terms, Field.Store.NO));
						}
					} else {
						termsReader.close();
						termsReader = null;
					}
				}
			}
			catch(Exception ex) {
				log.error(ex.getMessage(), ex);
			}
			
			return result;
		}
		
		
		@Override
		protected void close() {
			super.close();
			
			try
			{
				if(termsReader != null) {
					termsReader.close();
				}
			}
			catch(Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
	}
	
	
	
	
	protected class SnippetIndexer extends IndexerDecorator {
		
		protected Map<String, SnippetCache> snippetCache;
		protected Path snippetsPath;
		
		public SnippetIndexer(Indexer indexer, Path snippetsPath, int docsCount) {
			super(indexer);
			
			this.snippetsPath = snippetsPath;
			this.setDocsCount(docsCount);
			this.initSnippetsCache();
			
			this.addTFDFs(snippetsPath);
		}
		
		private void initSnippetsCache() {
			this.snippetCache = new HashMap<String, SnippetCache>();
			
			try(BufferedReader docsReader = new BufferedReader(new FileReader(snippetsPath.toFile()));
					BufferedReader termsReader = new BufferedReader(new FileReader(snippetsPath.resolveSibling(snippetsPath.getFileName().toString().replace(".docs", ".docTerms")).toFile())))
			{
				
				String docsLine = docsReader.readLine();
				termsReader.readLine();
				
				while((docsLine = docsReader.readLine()) != null) {
					SnippetCache snippet = new SnippetCache();
					String title = docsLine.substring(docsLine.indexOf(',')+1, docsLine.lastIndexOf(','));
					title = title.substring(title.indexOf(',')+1, title.lastIndexOf(','));
					
					snippet.setTitle(title);
					snippet.setDescription(termsReader.readLine());
					
					this.snippetCache.put(docsLine.substring(0, docsLine.indexOf(',')), snippet);
				}
			}
			catch(Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
		
		
		@Override
		protected List<Document> indexDocument(String docName) {
			List<Document> result = super.indexDocument(docName);
			
			SnippetCache data = this.snippetCache.remove(docName);
			if(data != null) {
				for(Document indexDoc : result) {
					indexDoc.add(new StringField(INDEX_TITLE_FIELD, data.getTitle(), Field.Store.YES));
					indexDoc.add(new TextField(INDEX_CONTENTS_FIELD, data.getDescription(), Field.Store.NO));
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
	
	
	
	protected class WikiDocEnrichment extends IndexerDecorator {
		
		protected Directory wikiIndex;
		protected IndexReader wikiReader;
		protected IndexSearcher wikiSearcher;
		protected Path keywordsPath;
		protected final Comparator<Map.Entry<String, MutableDouble>> sortComparator = new Comparator<Map.Entry<String, MutableDouble>>() {
	    	@Override
	    	public int compare(Entry<String, MutableDouble> o1, Entry<String, MutableDouble> o2) {
	    		return o1.getValue().compareTo(o2.getValue());
	    	}
		}.reversed();
		
		
		public WikiDocEnrichment(Indexer indexer, Path keywordsPath, Directory wikiIndex, IndexReader wikiReader, IndexSearcher wikiSearcher) {
			super(indexer);
			
			this.keywordsPath = keywordsPath.resolve(verticalData.getVerticalCollectionId());
			this.wikiIndex = wikiIndex;
			this.wikiReader = wikiReader;
			this.wikiSearcher = wikiSearcher;
		}
		
		protected List<Map.Entry<String, MutableDouble>> cacheTFIDF(String docName) {
			List<Map.Entry<String, MutableDouble>> tfidf = new ArrayList<Map.Entry<String,MutableDouble>>();
			Path keywordsDoc = this.keywordsPath.resolve(docName + ".keywords");
			if(Files.exists(keywordsDoc)) {
				try(FileReader in = new FileReader(keywordsDoc.toFile());
						BufferedReader reader = new BufferedReader(in)) {
					
					String line;
					int total = 0;
					while((line = reader.readLine()) != null) {
						String[] data = line.split("\\|");
						tfidf.add(new ImmutablePair<String, MutableDouble>(data[0], new MutableDouble(Double.parseDouble(data[1]))));
						
						if(++total >= multiData.maxTopWords) {
							break;
						}
					}
				}
				catch(Exception ex) {
					log.error(ex.getMessage(), ex);
				}
				
			}
			return tfidf;
		}
		
//		protected List<Map.Entry<String, MutableDouble>> calculateTFIDF(String docName, String terms) {
//			List<Map.Entry<String, MutableDouble>> tfidf = new ArrayList<Map.Entry<String,MutableDouble>>();
//			
//			Path keywordsDoc = this.keywordsPath.resolve(docName + ".keywords");
//			if(Files.exists(keywordsDoc)) {
//				try(FileReader in = new FileReader(keywordsDoc.toFile());
//						BufferedReader reader = new BufferedReader(in)) {
//					
//					String line;
//					int total = 0;
//					while((line = reader.readLine()) != null) {
//						String[] data = line.split("\\|");
//						tfidf.add(new ImmutablePair<String, MutableDouble>(data[0], new MutableDouble(Double.parseDouble(data[1]))));
//						
//						if(++total >= multiData.maxTopWords) {
//							break;
//						}
//					}
//				}
//				catch(Exception ex) {
//					log.error(ex.getMessage(), ex);
//				}
//				
//			} else {
//				
//				try(TokenStream tokenStream = indexerAnalyzer.tokenStream(null, new StringReader(terms))) {
//					tokenStream.reset();
//					
//					Map<String, MutableDouble> tfidfMap = new HashMap<String, MutableDouble>();
//						
//					// Tokenizes the document elements and gets the term frequency
//				    while (tokenStream.incrementToken()) {
//				    	String term = tokenStream.getAttribute(CharTermAttribute.class).toString();
//				    	if(!NumberUtils.isNumber(term) && !term.equals("h2")) {
//					    	MutableDouble value = tfidfMap.get(term);
//					    	if(value == null) {
//					    		value = new MutableDouble(0);
//					    		tfidfMap.put(term, value);
//					    	}
//					    	value.increment();
//				    	}
//				    }
//				    
//				    BigDecimal N = BigDecimal.valueOf(this.getDocsCount());
//				    for(Map.Entry<String, MutableDouble> entry : tfidfMap.entrySet()) {
//				    	int tf = entry.getValue().intValue();
//				    	TFDF tfdf = this.getTFDFs().get(entry.getKey());
//				    	
//				    	if(tfdf != null) {
//					    	double idf = Math.log(N.divide(BigDecimal.valueOf(tfdf.getDocumentFrequency()), CommonUtils.DEFAULT_DIVISION_SCALE, CommonUtils.DEFAULT_ROUNDING_MODE).doubleValue());
//					    	
//					    	entry.getValue().setValue(tf * idf);
//					    	tfidf.add(entry);
//				    	}
//				    }
//				    
//				    Collections.sort(tfidf, sortComparator);
//				    
//				    
//				    try(FileWriterHelper fwriter = new FileWriterHelper(keywordsDoc)) {
//				    	fwriter.open(false);
//				    	
//				    	for(Map.Entry<String, MutableDouble> entry : tfidf) {
//				    		fwriter.writeLine(entry.getKey() + "|" + String.valueOf(entry.getValue().toDouble()));
//				    	}
//				    }
//				    
//				    tfidf = tfidf.subList(0, Math.min(multiData.maxTopWords, tfidf.size()));
//				    
//				}
//				catch(Exception ex) {
//					log.error(ex.getMessage(), ex);
//				}
//				
//			}
//			
//			return tfidf;
//		}
		
		
		protected class IndexGroup {
			private IndexData data;
			private Document indexDoc;
		}
		
		
		@Override
		protected List<Document> indexDocument(String docName) {
			List<Document> result = super.indexDocument(docName);
			List<Map.Entry<String, MutableDouble>> tfidf = this.cacheTFIDF(docName);
			Map<Integer, List<IndexGroup>> indexGroup = new HashMap<Integer, List<IndexGroup>>();
			
			for(int i = 0; i < result.size(); i++) {
				IndexGroup group = new IndexGroup();
				group.data = multiData.get(i);
				group.indexDoc = result.get(i);
				
				List<IndexGroup> listGroup = indexGroup.get(group.data.topWords);
				if(listGroup == null) {
					listGroup = new ArrayList<IndexGroup>();
					indexGroup.put(group.data.topWords, listGroup);
				}
				
				listGroup.add(group);
			}
			
			
			
			for(Map.Entry<Integer, List<IndexGroup>> groupEntry : indexGroup.entrySet()) {
				BooleanQuery query = new BooleanQuery();
				for(Map.Entry<String, MutableDouble> entry : tfidf.subList(0, Math.min(groupEntry.getKey(), tfidf.size()))) {
					query.add(new TermQuery(new Term(AbstractSamplerIndexer.INDEX_CONTENTS_FIELD, entry.getKey())), Occur.SHOULD);
				}


				log.info(MessageFormat.format("Searching in wiki: {0}...", query.toString()));
				
				try
				{
					ScoreDoc[] hits = wikiSearcher.search(query, multiData.maxTopDocs+10).scoreDocs;
					for(IndexGroup group : groupEntry.getValue()) {
						group.data.indexLog.writeLogMessage("Document {0}: {1} ({2}). Searching in wiki: {3}...", verticalData.getVertical().getId(), group.indexDoc.get(INDEX_TITLE_FIELD), group.indexDoc.get(INDEX_DOC_NAME_FIELD), query.toString());
	
						log.info(MessageFormat.format("Document {0}: {1} ({2})...", verticalData.getVertical().getId(), group.indexDoc.get(INDEX_TITLE_FIELD), group.indexDoc.get(INDEX_DOC_NAME_FIELD)));
						
						group.data.indexLog.writeLogMessage("Enriching document using top {0} words and top {1} documents...", group.data.topWords, group.data.topDocs);
						log.info(MessageFormat.format("Enriching document using top {0} words and top {1} documents...", group.data.topWords, group.data.topDocs));
						
						
						
						int total = 0;
	
						for (int j = 0; j < hits.length; j++) {
							Document wikiDoc = wikiReader.document(hits[j].doc);
							
							if((!StringUtils.containsIgnoreCase(wikiDoc.get(INDEX_TITLE_FIELD), "(disambiguation)") &&
									(!StringUtils.startsWith(wikiDoc.get(INDEX_TITLE_FIELD), "List of ")))) {
								group.data.indexLog.writeLogMessage("Adding wiki document for enrichment: {0} ({1})", wikiDoc.get(INDEX_TITLE_FIELD), wikiDoc.get(INDEX_DOC_NAME_FIELD));
								log.info(MessageFormat.format("Adding wiki document for enrichment: {0} ({1})", wikiDoc.get(INDEX_TITLE_FIELD), wikiDoc.get(INDEX_DOC_NAME_FIELD)));
								
								group.indexDoc.add(new TextField(INDEX_CONTENTS_FIELD, wikiDoc.get(AbstractSamplerIndexer.INDEX_CONTENTS_FIELD), Field.Store.NO));
								
								if(++total >= group.data.topDocs) {
									break;
								}
							}
						}
					}
				}
				catch(Exception ex) {
					log.error(ex.getMessage(), ex);
				}
			}
			

						
			return result;
		}
		
	}




	@Override
	public void close() throws IOException {
		this.analyzer.close();
	}

}
