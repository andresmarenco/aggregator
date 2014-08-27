package aggregator.verticalselection;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import aggregator.beans.VerticalCollection;
import aggregator.sampler.indexer.AbstractSamplerIndexer;
import aggregator.util.CommonUtils;
import aggregator.util.analysis.AggregatorAnalyzer;

public abstract class VerticalSelection implements Closeable {

	protected Analyzer analyzer;
	protected VerticalCollection collection;
	protected Directory index;
	protected DirectoryReader ireader;
	protected Log log = LogFactory.getLog(VerticalSelection.class);
	
	/**
	 * Default Constructor
	 * @param collection Vertical Collection
	 */
	public VerticalSelection(VerticalCollection collection) {
		this.collection = collection;
		
		try
		{
			this.analyzer = new AggregatorAnalyzer(Version.LUCENE_4_9);
			this.index = new NIOFSDirectory(CommonUtils.getIndexPath().resolve(collection.getId()).toFile());
			this.ireader = DirectoryReader.open(index);
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	/**
	 * Executes the test run for the collection and outputs the expected FedWeb evaluation file 
	 * @param runtag Run tag for the test run
	 */
	public void testQueries(String runtag) {
		try(BufferedReader reader = new BufferedReader(new FileReader(CommonUtils.getAnalysisPath().resolve(collection.getId() + ".testQueries").toFile()));
				SelectionRunsFile runsFile = new SelectionRunsFile(CommonUtils.getAnalysisPath().resolve(MessageFormat.format("{0}", runtag))))
		{
			log.info("Starting test queries process...");
			
			String line;
			while((line = reader.readLine()) != null) {
				int separatorIndex = line.indexOf(',');
				String topicId = line.substring(0, separatorIndex);
				String queryString = line.substring(separatorIndex+1);
				
				log.info(MessageFormat.format("Querying topic {0}: {1}", topicId, queryString));
				
				List<Map.Entry<String, Double>> result = this.execute(queryString);
				
				int rank = 1;
				
				for(Map.Entry<String, Double> data : result) {
					runsFile.writeRunData(topicId, collection.getVerticalCode(data.getKey()), rank++, data.getValue(), runtag);
				}
			}
			
			log.info("Test queries process finished.");
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	/**
	 * Executes the query in the CSI and selects the appropriate verticals
	 * @param queryString User's query string
	 * @return Selected verticals for the query and their score
	 */
	public List<Map.Entry<String, Double>> execute(String queryString) {
		List<Map.Entry<String, Double>> result = this.executeModel(queryString);
		this.sortResultList(result);
		return result;
	}
	
	
	
	
	/**
	 * Searches the query in the centralized sample index (CSI)
	 * @param queryString User's query string
	 * @return Result data with the found documents grouped by the vertical
	 */
	protected CSIResultData searchCSI(String queryString) {
		CSIResultData resultData = new CSIResultData();
//		Map<String, DescriptiveStatistics> verticals = new HashMap<String, DescriptiveStatistics>();
		
		try
		{
			
			IndexSearcher isearcher = new IndexSearcher(ireader);
			QueryParser parser = new QueryParser(Version.LUCENE_4_9, AbstractSamplerIndexer.INDEX_TEXT_FIELD, analyzer);
			Query query = parser.parse(queryString);
			
			ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
			
			for(int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				String docId = hitDoc.get(AbstractSamplerIndexer.INDEX_DOC_NAME_FIELD);
				String verticalName = hitDoc.get(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD);
				float score = hits[i].score;
				
				resultData.addResult(verticalName, i+1, docId, score);
			}
			
			
//			for(ScoreDoc hit : hits) {
//				Document hitDoc = isearcher.doc(hit.doc);
//				
//				String verticalName = hitDoc.get(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD);
//				float score = hit.score;
//				
//				DescriptiveStatistics value = verticals.get(verticalName);
//				if(value == null) {
//					value = new DescriptiveStatistics();
//					verticals.put(verticalName, value);
//				}
//				
//				value.addValue(score);
//			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return resultData;
	}
	
	
	
	
	/**
	 * Sorts the results list by the score (descending)
	 * @param resultList Results list
	 */
	protected void sortResultList(List<Map.Entry<String, Double>> resultList) {
		Collections.sort(resultList, new Comparator<Map.Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
		}.reversed());
	}
	
	


	@Override
	public void close() throws IOException {
		this.ireader.close();
		this.index.close();
		this.analyzer.close();
	}
	
	
	
	
	/**
	 * Executes the query using the current model
	 * @param queryString User's query string
	 * @return Selected verticals for the query and their score
	 */
	protected abstract List<Map.Entry<String, Double>> executeModel(String queryString);
	
	
	
	
	protected static class CSIResultData {
		private int totalHits;
		private Map<String, CSIVertical> verticals;
		
		/**
		 * Default Constructor
		 */
		public CSIResultData() {
			this.verticals = new HashMap<String, CSIVertical>();
		}

		/**
		 * @return the totalHits
		 */
		public int getTotalHits() {
			return totalHits;
		}

		/**
		 * @return the verticals
		 */
		public Map<String, CSIVertical> getVerticals() {
			return verticals;
		}
		
		public void addResult(String verticalName, long rank, String docId, float score) {
			CSIVertical value = verticals.get(verticalName);
			if(value == null) {
				value = new CSIVertical();
				verticals.put(verticalName, value);
			}
			
			value.addDocument(rank, docId, score);
			this.totalHits++;
		}
	}
	
	
	
	protected static class CSIVertical extends ArrayList<CSIDocument> {
		private static final long serialVersionUID = 201408262132L;
		
		/**
		 * Default Constructor
		 */
		public CSIVertical() {
			super();
		}
		
		/**
		 * Gets the sum of all the documents' scores
		 * @return Sum of all the documents' scores
		 */
		public double getScoresSum() {
			double result = 0;
			for(CSIDocument doc : this) {
				result += doc.getScore();
			}
			return result;
		}
		
		/**
		 * Gets the max score among the documents
		 * @return Max score among the documents
		 */
		public double getScoresMax() {
			double result = 0;
			for(CSIDocument doc : this) {
				result = Math.max(doc.getScore(), result);
			}
			return result;
		}
		
		/**
		 * Gets the average score among the documents
		 * @return Average score among the documents
		 */
		public double getScoresAverage() {
			BigDecimal result = BigDecimal.valueOf(getScoresSum()).divide(BigDecimal.valueOf(this.size()), 6, RoundingMode.HALF_EVEN);
			return result.doubleValue();
		}
		
		/**
		 * Adds the document data
		 * @param rank Rank of the found document
		 * @param docId Document id
		 * @param score Score obtained
		 */
		public void addDocument(long rank, String docId, float score) {
			this.add(new CSIDocument(rank, docId, score));
		}
	}
	
	
	
	
	protected static class CSIDocument {
		private long rank;
		private String docId;
		private float score;
		
		/**
		 * Default Constructor
		 * @param rank Document Rank
		 * @param docId Document Id
		 * @param score Score obtained
		 */
		public CSIDocument(long rank, String docId, float score) {
			super();
			this.rank = rank;
			this.docId = docId;
			this.score = score;
		}

		/**
		 * @return the rank
		 */
		public long getRank() {
			return rank;
		}

		/**
		 * @return the docId
		 */
		public String getDocId() {
			return docId;
		}

		/**
		 * @return the score
		 */
		public float getScore() {
			return score;
		}
	}
}
