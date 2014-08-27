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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.apache.lucene.util.Version;

import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;
import aggregator.beans.VerticalCollection;
import aggregator.beans.VerticalCollection.VerticalCollectionData;
import aggregator.sampler.output.IndexExecutionLogFile;
import aggregator.util.CommonUtils;
import aggregator.util.analysis.AggregatorIndexerAnalyzer;
import aggregator.util.analysis.AggregatorTokenizerAnalyzer;

public class LuceneSamplerIndexer extends AbstractSamplerIndexer {
	
	private Analyzer analyzer;
	private final Version luceneVersion = Version.LUCENE_4_9;
	
	/**
	 * Default Constructor
	 */
	public LuceneSamplerIndexer() {
		this.analyzer = new AggregatorTokenizerAnalyzer(luceneVersion);
	}

	
	/**
	 * Default Constructor
	 * @param vertical Vertical to index
	 */
	public LuceneSamplerIndexer(Vertical vertical) {
		super(vertical);
		this.analyzer = new AggregatorTokenizerAnalyzer(luceneVersion);
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
		
		try(Analyzer indexerAnalyzer = new AggregatorIndexerAnalyzer(luceneVersion);
				IndexExecutionLogFile indexLog = IndexExecutionLogFile.newInstance(
						MessageFormat.format("{0}{1}_{2}.{3}",
								analysisPath,
								collection.getId(),
								CommonUtils.getTimestampString(),
								"{docType}")))
								
		{
			long startTime = System.currentTimeMillis();
			log.info("Starting indexing process...");
			indexLog.writeLogMessage("Starting indexing process...");
			
			Path indexPath = this.indexPath.resolve(collection.getId());
			Directory index = new NIOFSDirectory(indexPath.toFile());
			
			IndexWriterConfig config = new IndexWriterConfig(luceneVersion, indexerAnalyzer);
			try(IndexWriter writer = new IndexWriter(index, config)) {
				
				for(VerticalCollectionData vertical : collection.getVerticals()) {
					
					Files.walk(CommonUtils.getAnalysisPath().resolve(analysisPath).resolve(vertical.getVerticalCollectionId()), 1).forEach(filePath -> {
						if (Files.isRegularFile(filePath)) {
							if(filePath.getFileName().toString().endsWith(".docs")) {
								Path docTerms = filePath.getParent().resolve(
										StringUtils.removeEnd(filePath.getFileName().toString(), ".docs").concat(".docTerms"));
								
								log.info(MessageFormat.format("Reading {0} documents file with {1} terms file...", filePath.getFileName().toString(), docTerms.getFileName().toString()));
								indexLog.writeLogMessage("Reading {0} documents file with {1} terms file...", filePath.getFileName().toString(), docTerms.getFileName().toString());
								
								try(BufferedReader docsReader = new BufferedReader(new FileReader(filePath.toFile()));
										BufferedReader termsReader = new BufferedReader(new FileReader(docTerms.toFile()))) {
									
									// Get rid of the header line
									String docLine = docsReader.readLine();
									String termsLine = termsReader.readLine();
									String docName, totalTerms;
									
									while((docLine = docsReader.readLine()) != null) {
										docName = docLine.substring(0, docLine.indexOf(","));
										totalTerms = docLine.substring(0, docLine.lastIndexOf(","));
										totalTerms = totalTerms.substring(totalTerms.lastIndexOf(",")+1);
										
										termsLine = termsReader.readLine();
										indexLog.writeLogMessage("Indexing {0} with {1} terms", docName, totalTerms);
										
										// Creating document for the index
										Document indexDoc = new Document();
										indexDoc.add(new TextField(INDEX_TEXT_FIELD, termsLine, Field.Store.NO));
										indexDoc.add(new StringField(INDEX_DOC_NAME_FIELD, docName, Field.Store.YES));
										indexDoc.add(new StringField(INDEX_VERTICAL_FIELD, vertical.getVertical().getId(), Field.Store.YES));
										
										writer.addDocument(indexDoc);
									}
									
									log.info("Documents successfully indexed!");
									indexLog.writeLogMessage("Documents successfully indexed!");
								}
								catch(IOException ex) {
									log.error("Error reading files");
								}
								
							}
						}
					});
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




	@Override
	public void close() throws IOException {
		this.analyzer.close();
	}
}
