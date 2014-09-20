package aggregator.sampler.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import aggregator.util.FileWriterHelper;
import aggregator.util.IterableNodeList;
import aggregator.util.LRUMap;
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
				
				Path analysisFilePath = CommonUtils.getAnalysisPath();
				for(VerticalCollectionData verticalData : collection.getVerticals()) {
					Indexer indexer = new BasicIndexer(analysisPath, verticalData, indexLog);
					indexer = new VerticalMetaDataIndexer(indexer, isIndexVerticalName(), isIndexVerticalDescription());
					
					if(isIndexDocs()) indexer = new DocumentIndexer(indexer);
					if(isIndexSnippets()) indexer = new SnippetIndexer(indexer);
					if(isIndexWordNet()) indexer = new WordNetIndexer(indexer);
					
					verticalData.setSampleSize(0);
					
					// Reads the analyzed documents
					try(Stream<Path> pathList = Files.walk(analysisFilePath.resolve(analysisPath).resolve(verticalData.getVerticalCollectionId()), 1)) {
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
					
					int extraSize = indexer.finalize(writer);
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
		protected boolean indexVerticalName;
		
		public Indexer(String analysisPath, VerticalCollectionData verticalData, IndexExecutionLogFile indexLog) {
			this.analysisPath = analysisPath;
			this.verticalData = verticalData;
			this.indexLog = indexLog;
			this.indexVerticalName = isIndexVerticalName();
		}
		
		protected abstract Document indexDocument(String docName);
		protected abstract void resetCurrentDocsFile(Path filePath);
		protected abstract int finalize(IndexWriter writer) throws IOException;
	}
	
	
	
	
	protected class BasicIndexer extends Indexer {

		public BasicIndexer(String analysisPath, VerticalCollectionData verticalData, IndexExecutionLogFile indexLog) {
			super(analysisPath, verticalData, indexLog);
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
		
		@Override
		protected int finalize(IndexWriter writer) throws IOException {
			return 0;
		}
	}
	
	
	
	
	protected abstract class IndexerDecorator extends Indexer {
		private Indexer decoratedIndexer;
		
		public IndexerDecorator(Indexer indexer) {
			super(indexer.analysisPath, indexer.verticalData, indexer.indexLog);
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
		
		@Override
		protected int finalize(IndexWriter writer) throws IOException {
			return decoratedIndexer.finalize(writer);
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
		protected Document indexDocument(String docName) {
			Document result = super.indexDocument(docName);
			
			if(indexName) {
				result.add(new TextField(INDEX_CONTENTS_FIELD, verticalData.getVertical().getName(), Field.Store.NO));
			}
			if(indexDescription) {
				result.add(new TextField(INDEX_CONTENTS_FIELD, verticalData.getVertical().getDescription(), Field.Store.NO));
			}
			
			return result;
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
//				String terms = result.get(INDEX_CONTENTS_FIELD);
//				if(StringUtils.isNotBlank(terms)) {
//					terms = terms.concat(" ").concat(termsReader.readLine());
//				} else {
//					terms = termsReader.readLine();
//				}
//				
				String terms = termsReader.readLine();
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
//			SnippetCache cache = snippetCache.get(snippetKey);
			SnippetCache cache = snippetCache.remove(snippetKey);
			
			if(cache != null) {
				String snippetText = cache.getTitle();
				if(StringUtils.isNotBlank(snippetText)) {
					snippetText = snippetText.concat(" ").concat(cache.getDescription());
				}
				
				result.add(new TextField(INDEX_CONTENTS_FIELD, snippetText, Field.Store.NO));
			}
			
			return result;
		}
		
		
		@Override
		protected int finalize(IndexWriter writer) throws IOException {
			int total = super.finalize(writer);
			
			log.info("Adding remaining snippets...");
			indexLog.writeLogMessage("Adding remaining snippets...");
			
			Indexer indexer = new BasicIndexer(analysisPath, verticalData, indexLog);
			indexer = new VerticalMetaDataIndexer(indexer, isIndexVerticalName(), isIndexVerticalDescription());
			
			for(Map.Entry<String, SnippetCache> element : snippetCache.entrySet()) {
				Document doc = indexer.indexDocument(element.getKey());
				String snippetText = element.getValue().getTitle();
				if(StringUtils.isNotBlank(snippetText)) {
					snippetText = snippetText.concat(" ").concat(element.getValue().getDescription());
				}
				
				doc.add(new TextField(INDEX_CONTENTS_FIELD, snippetText, Field.Store.NO));
				
				writer.addDocument(doc);
				
				total++;
			}
			
			return total;
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
	
	
	
	
	protected class WordNetIndexer extends IndexerDecorator {
		protected Path docTerms;
		protected BufferedReader termsReader;
		protected FileWriterHelper synFile;
		protected Map<String, String> cache;
		
		private static final String WORDNET_KEY = "aggregator.sampler.indexer.WordNet";

		public WordNetIndexer(Indexer indexer) {
			super(indexer);
			
			cache = new LRUMap<String, String>(100000);
		}
		
		@Override
		protected void resetCurrentDocsFile(Path filePath) {
			super.resetCurrentDocsFile(filePath);
			
			try
			{
				if(this.termsReader != null) {
					this.termsReader.close();
				}
				
				if(this.synFile != null) {
					this.synFile.close();
				}
				
				this.docTerms = filePath.resolveSibling(StringUtils.removeEnd(filePath.getFileName().toString(), ".docs").concat(".docTerms"));
				this.termsReader = new BufferedReader(new FileReader(docTerms.toFile()));
				
				log.info(MessageFormat.format("Reading {0} terms file for WordNet...", docTerms.getFileName().toString()));
				indexLog.writeLogMessage("Reading {0} terms file for WordNet...", docTerms.getFileName().toString());

				this.synFile = new FileWriterHelper(filePath.resolveSibling(StringUtils.removeEnd(filePath.getFileName().toString(), ".docs").concat(".synonyms")));
				this.synFile.open(false);
				
				this.cache.clear();
				
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
				log.info(MessageFormat.format("Finding synonyms for document {0} in vertical {1}...", docName, verticalData.getVerticalCollectionId()));
				StringBuilder docSyns = new StringBuilder();
				
				String[] terms = termsReader.readLine().split("\\s+");
				for(String term : terms) {
					docSyns.append(this.findSynonyms(term));
				}
				
				String docSynsStr = docSyns.toString().trim();
				
				result.add(new TextField(INDEX_CONTENTS_FIELD, docSynsStr, Field.Store.NO));
				this.synFile.writeLine(docSynsStr);
			}
			catch(Exception ex) {
				log.error(ex.getMessage(), ex);
			}
			
			return result;
		}
		
		protected String findSynonyms(String term) {
			String result = cache.get(term);
			
			if(result == null) {
				try
				{
					StringBuilder builder = new StringBuilder();
					Process p = Runtime.getRuntime().exec(MessageFormat.format(System.getProperty(WORDNET_KEY), term, "-synsn"));
					try(BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
						String line;
						
						while ((line = stdout.readLine()) != null) {
							if(StringUtils.startsWithIgnoreCase(line, "Sense")) {
								String[] synonyms = stdout.readLine().split(",");
								for(String t : synonyms) {
									builder.append(t.trim()).append(" ");
								}
								
								
								line = stdout.readLine();
								if(line != null) {
									line = line.substring(line.indexOf("=> ") + 3);
									String[] hypernyms = line.split(",");
									
									
									for(String t : hypernyms) {
										builder.append(t.trim()).append(" ");
									}
								}
								
								break;
							}
						}
					}
					p.waitFor();
					
					result = builder.toString();
					cache.put(term, result);
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
