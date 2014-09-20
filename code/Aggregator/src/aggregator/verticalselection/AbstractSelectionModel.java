package aggregator.verticalselection;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;

import aggregator.beans.VerticalCollection;
import aggregator.sampler.indexer.AbstractSamplerIndexer;
import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;
import aggregator.util.analysis.AggregatorAnalyzer;

public abstract class AbstractSelectionModel implements Closeable {

	protected Analyzer analyzer;
	protected VerticalCollection collection;
	protected Directory index;
	protected DirectoryReader ireader;
	protected final int rankCutOff;
	protected final int maxAnalyzedDocuments;
	protected Log log = LogFactory.getLog(AbstractSelectionModel.class);
	
	private static final String SELECTION_MODEL_KEY = "aggregator.verticalSelection.model";
	private static final String EVALUATION_GDEVAL_KEY = "aggregator.evaluation.gdeval";
	private static final String EVALUATION_TREC_EVAL_KEY = "aggregator.evaluation.trec_eval";
	private static final String EVALUATION_QRELS_FILE_SUFFIX_KEY = "aggregator.evaluation.qrelsFileSuffix";
	private static final String RANK_CUTOFF_KEY = "aggregator.verticalSelection.rankCutOff";
	
	
	/**
	 * Default Constructor
	 * @param collection Vertical Collection
	 */
	public AbstractSelectionModel(VerticalCollection collection) {
		this.rankCutOff = Integer.parseInt(System.getProperty(RANK_CUTOFF_KEY, "200"));
		
		if(collection != null) {
			this.collection = collection;
			this.maxAnalyzedDocuments = collection.getVerticals().size() * this.rankCutOff;
			
			try
			{
				this.analyzer = new AggregatorAnalyzer(CommonUtils.LUCENE_VERSION);
				this.index = new NIOFSDirectory(CommonUtils.getIndexPath().resolve(collection.getId()).resolve(AbstractSamplerIndexer.getIndexName()).toFile());
				this.ireader = DirectoryReader.open(index);
			}
			catch(Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		} else {
			this.maxAnalyzedDocuments = Integer.MAX_VALUE;
		}
	}
	
	
	
	public void resetIndex(Directory index) {
		try
		{
			if(this.index != null) { this.index.close(); }
			if(this.ireader != null) { this.ireader.close(); }
			
			this.index = index;
			this.ireader = DirectoryReader.open(index);
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	/**
	 * Creates a new instance of the configured selection model
	 * @param collection Vertical Collection
	 */
	public static AbstractSelectionModel newInstance(VerticalCollection collection) {
		AbstractSelectionModel result = null;
		try
		{
			Class<? extends AbstractSelectionModel> classType = CommonUtils.getSubClassType(AbstractSelectionModel.class, System.getProperty(SELECTION_MODEL_KEY));
			if(classType != null) {
				result = classType.getConstructor(VerticalCollection.class).newInstance(collection);
			}
		}
		catch(Exception ex) {
			LogFactory.getLog(AbstractSelectionModel.class).error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
	
	
	
	/**
	 * Creates a new instance of the default similarity based on the configured selection model
	 */
	public static Similarity getDefaultSimilary() {
		Similarity result = null;
		try
		{
			result = AbstractSelectionModel.newInstance(null).getSimilarityModel();
		}
		catch(Exception ex) {
			LogFactory.getLog(AbstractSelectionModel.class).error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
	
	
	
	/**
	 * Executes the test run for the collection and outputs the expected FedWeb evaluation file
	 */
	public void testQueries() {
		this.testQueries(this.getModelCodeName());
	}
	
	
	
	
	/**
	 * Executes the test run for the collection and outputs the expected FedWeb evaluation file 
	 * @param runtag Run tag for the test run
	 */
	public void testQueries(String runtag) {
		try
		{
			Path runsFilePath = CommonUtils.getAnalysisPath()
					.resolve(collection.getId().concat("-eval"))
					.resolve(AbstractSamplerIndexer.getIndexName())
					.resolve(runtag);
	
			try(BufferedReader reader = new BufferedReader(new FileReader(CommonUtils.getAnalysisPath().resolve(collection.getId() + "-topics.txt").toFile()));
					SelectionRunsFile runsFile = new SelectionRunsFile(runsFilePath))
			{
				log.info("Starting test queries process...");
				
				
				String line;
				while((line = reader.readLine()) != null) {
					int separatorIndex = line.indexOf(':');
					String topicId = line.substring(0, separatorIndex);
					String queryString = line.substring(separatorIndex+1);
					
					log.info(MessageFormat.format("Querying topic {0}: {1}", topicId, queryString));
					
					List<Map.Entry<String, Double>> result = this.execute(queryString);
					
					int rank = 1;
					
					for(Map.Entry<String, Double> data : result) {
						runsFile.writeRunData(topicId, collection.getVerticalCode(data.getKey()), rank++, Math.log10(data.getValue()), this.getModelCodeName());
					}
				}
			}

			
			// Evaluating the execution
			Path qrelsPath = CommonUtils.getAnalysisPath().resolve(collection.getId().concat(System.getProperty(EVALUATION_QRELS_FILE_SUFFIX_KEY)));
			
			log.info("Executing nDCG evaluation...");
			this.gdeval(runsFilePath, qrelsPath);
			
			log.info("Executing TREC evaluation...");
			this.trec_eval(runsFilePath, qrelsPath);
			
			log.info("Executing P@k evaluation...");
			this.precisionEval(runsFilePath, qrelsPath);
			
			log.info("Test queries process finished.");
		
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	/**
	 * Normalized Discounted Cumulative Gain (nDCG) evaluation
	 * @param runsFile Runs file
	 * @param qrels QRELS file
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void gdeval(Path runsFile, Path qrels) throws IOException, InterruptedException {
		String gdeval = System.getProperty(EVALUATION_GDEVAL_KEY);
		Process process = Runtime.getRuntime().exec(MessageFormat.format("{0} {1} {2}", gdeval, qrels.toString(), runsFile.toString()));
		
		try(BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
				BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
		
			String line;
			
			Path evalPath = runsFile.resolveSibling(runsFile.getFileName().toString().concat(".eval"));
			try(FileWriterHelper evalFile = new FileWriterHelper(evalPath)) {
				evalFile.open(false);
				
				while ((line = stdout.readLine()) != null) {
					evalFile.writeLine(line);
				}
		    }
			
			while ((line = stderr.readLine()) != null) {
				log.error(line);
		    }
		}
		
		process.waitFor();
	}
	
	
	
	
	/**
	 * TREC Evaluation
	 * @param runsFile Runs File
	 * @param qrels QRELS File
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public void trec_eval(Path runsFile, Path qrels) throws IOException, InterruptedException {
		String trec_eval = System.getProperty(EVALUATION_TREC_EVAL_KEY);
		Process process = Runtime.getRuntime().exec(MessageFormat.format("{0} -q -c -M1000 {1} {2}", trec_eval, qrels.toString(), runsFile.toString()));
		
		try(BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
				BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
		
			String line;
			
			Path evalPath = runsFile.resolveSibling(runsFile.getFileName().toString().concat(".trec_eval"));
			try(FileWriterHelper evalFile = new FileWriterHelper(evalPath)) {
				evalFile.open(false);
				
				while ((line = stdout.readLine()) != null) {
					evalFile.writeLine(line);
				}
		    }
			
			while ((line = stderr.readLine()) != null) {
				log.error(line);
		    }
		}
		
		process.waitFor();
	}
	
	
	
	/**
	 * Normalized P@k evaluation
	 * @param runsFile Runs File
	 * @param qrels QRELS File
	 * @throws IOException  
	 */
	public void precisionEval(Path runsFile, Path qrels) throws IOException {
		// Read QRELS file
		Map<String, Map<String, Integer>> qrelsData = new LinkedHashMap<String, Map<String,Integer>>();
		try(BufferedReader qrelsReader = new BufferedReader(new FileReader(qrels.toFile()))) {
			String line;
			while((line = qrelsReader.readLine()) != null) {
				String[] data = line.split("\\s+");
				String topic = data[0];
				String engine = data[2];
				Integer gradedPrecision = Integer.parseInt(data[3]);
				
				Map<String, Integer> enginesPrecision = qrelsData.get(topic);
				if(enginesPrecision == null) {
					enginesPrecision = new HashMap<String, Integer>();
					qrelsData.put(topic, enginesPrecision);
				}
				
				enginesPrecision.put(engine, gradedPrecision);
			}
		}

		
		
		// Read runs file
		Map<String, List<String>> topicsEngines = new LinkedHashMap<String, List<String>>(); 
		try(BufferedReader runsReader = new BufferedReader(new FileReader(runsFile.toFile()))) {
			String line;
			while((line = runsReader.readLine()) != null) {
				String[] data = line.split("\\s+");
				String topic = data[0];
				String engine = data[2];
				
				List<String> enginesList = topicsEngines.get(topic);
				if(enginesList == null) {
					enginesList = new ArrayList<String>();
					topicsEngines.put(topic, enginesList);
				}
				
				enginesList.add(engine);
			}
			
		}
		
		
		// Comparator to order the QRELS
		Comparator<Integer> qrelsComparator = new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o1.compareTo(o2);
			}
		}.reversed();
		
		
		
		
		// Compute precision for topic
		Path evalPath = runsFile.resolveSibling(runsFile.getFileName().toString().concat(".precision"));
		try(FileWriterHelper evalFile = new FileWriterHelper(evalPath)) {
			evalFile.open(false);
			
			DecimalFormat decimalFormat = new DecimalFormat("0.00000");
			double amean_1 = 0, amean_5 = 0, amean_10 = 0;
			int topicsCount = 0;
			
			evalFile.writeLine("runid,topic,nP@1,nP@5,nP@10");
			
			for(Entry<String, List<String>> entry : topicsEngines.entrySet()) {
				Map<String, Integer> qrelsTopic = qrelsData.get(entry.getKey());
				
				double p_1 = 0, p_5 = 0, p_10 = 0;
				double norm_1 = 0, norm_5 = 0, norm_10 = 0;
				int count = 0;
				
				if(qrelsTopic != null) {
					for(String engine : entry.getValue()) {
						Integer qrelsValue = qrelsTopic.get(engine);
						
						if(qrelsValue != null) {
							if(count < 1) {
								p_1 += qrelsValue;
							}
							
							if(count < 5) {
								p_5 += qrelsValue;
							}
							
							if(count < 10) {
								p_10 += qrelsValue;
							}
						}
						
						count++;
					}
				
				
				
					List<Integer> orderedQrelsTopic = new ArrayList<Integer>(qrelsTopic.values());
					Collections.sort(orderedQrelsTopic, qrelsComparator);
					
					for(int i = 0; i < (Math.min(orderedQrelsTopic.size(), 10)); i++) {
						if(i < 1) {
							norm_1 += orderedQrelsTopic.get(i);
						}
						
						if(i < 5) {
							norm_5 += orderedQrelsTopic.get(i);
						}
						
						if(i < 10) {
							norm_10 += orderedQrelsTopic.get(i);
						}
					}
					
					p_1 /= norm_1;
					p_5 /= norm_5;
					p_10 /= norm_10;
					
					amean_1 += p_1;
					amean_5 += p_5;
					amean_10 += p_10;
					
					evalFile.writeLine(MessageFormat.format("{0},{1},{2},{3},{4}",
							runsFile.getFileName().toString(),
							entry.getKey(),
							decimalFormat.format(p_1),
							decimalFormat.format(p_5),
							decimalFormat.format(p_10)));
					
					topicsCount++;
				
				}
			}
			
			amean_1 /= topicsCount;
			amean_5 /= topicsCount;
			amean_10 /= topicsCount;
			
			evalFile.writeLine(MessageFormat.format("{0},amean,{1},{2},{3}",
					runsFile.getFileName().toString(),
					decimalFormat.format(amean_1),
					decimalFormat.format(amean_5),
					decimalFormat.format(amean_10)));
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
		
		try
		{
			IndexSearcher isearcher = new IndexSearcher(ireader);
			isearcher.setSimilarity(getSimilarityModel());
			
			Query query = this.prepareQuery(queryString);
			ScoreDoc[] hits = isearcher.search(query, null, Integer.MAX_VALUE).scoreDocs;
//			System.out.println("TOTAL: " + hits.length);
			for(int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				String docId = hitDoc.get(AbstractSamplerIndexer.INDEX_DOC_NAME_FIELD);
				String verticalName = hitDoc.get(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD);
				float score = hits[i].score;
				
				resultData.addResult(verticalName, i+1, docId, score);
				if(resultData.isComplete()) {
					break;
				}
			}
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
	
	
	
	
	/**
	 * Gets the default similarity for this model
	 * @return Similarity model
	 */
	public Similarity getSimilarityModel() {
		return new DefaultSimilarity();
	}
	
	


	@Override
	public void close() throws IOException {
		this.ireader.close();
		this.index.close();
		this.analyzer.close();
	}
	
	
	
	
	/**
	 * Prepares the query instance given the query string
	 * @param queryString User's query string
	 * @return Query instance
	 */
	protected abstract Query prepareQuery(String queryString);
	
	/**
	 * Executes the query using the current model
	 * @param queryString User's query string
	 * @return Selected verticals for the query and their score
	 */
	protected abstract List<Map.Entry<String, Double>> executeModel(String queryString);
	
	/**
	 * Gets the selection model code name
	 * @return Model code name
	 */
	public abstract String getModelCodeName();
	
	
	
	protected class CSIResultData {
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
		
		public boolean isComplete() {
			return totalHits >= maxAnalyzedDocuments;
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
			
			if(value.addDocument(rank, docId, score)) {
				this.totalHits++;
			}
		}
	}
	
	
	
	protected class CSIVertical extends ArrayList<CSIDocument> {
		private static final long serialVersionUID = 201408262132L;
		private int count;
		
		/**
		 * Default Constructor
		 */
		public CSIVertical() {
			super();
			this.count = 0;
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
		 * @return true is the document was added
		 */
		public boolean addDocument(long rank, String docId, float score) {
			boolean added = false;
			if(count < rankCutOff) {
				this.add(new CSIDocument(rank, docId, score));
				count++;
				added = true;
			}
			
			return added;
		}
	}
	
	
	
	
	protected class CSIDocument {
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
