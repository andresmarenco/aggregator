package aggregator.sampler.indexer;

import java.io.Closeable;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;
import aggregator.sampler.AbstractSampler;
import aggregator.sampler.parser.SamplerParser;
import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;
import aggregator.verticalwrapper.VerticalWrapperController;

public abstract class AbstractSamplerIndexer implements Closeable {
	
	private static final String SAMPLER_INDEXER_KEY = "aggregator.sampler.indexer";
	protected static final String TOKENS_FILE_PATTERN_KEY = "aggregator.sampler.indexer.tokensFilePattern";
	protected static final String TF_DF_FILE_PATTERN_KEY = "aggregator.sampler.indexer.tf_dfFilePattern";
	
	protected Vertical vertical;
	protected SamplerParser samplerParser;
	protected List<String> foundTerms;
	protected List<String> newTerms;
	protected HashMap<String, TF_DF> uniqueTerms;
	protected Path indexPath;
	protected Path tokensFile;
	protected Path tf_dfFile;
	protected Log log = LogFactory.getLog(getClass());
	
	/**
	 * Default Constructor
	 * @param vertical Vertical to index
	 */
	public AbstractSamplerIndexer(Vertical vertical) {
		this.vertical = vertical;
		this.samplerParser = VerticalWrapperController.getInstance().getVerticalConfig(vertical).newIndexParserInstance();
		this.foundTerms = new ArrayList<String>();
		this.newTerms = new ArrayList<String>();
		this.uniqueTerms = new HashMap<String, AbstractSamplerIndexer.TF_DF>();
		
		try
		{
			this.indexPath = CommonUtils.getIndexPath();
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	/**
	 * Creates a new instance of the corresponding {@code AbstractSamplerIndexer}
	 * based on the configuration file
	 * @param vertical Vertical to index=
	 * @param sampler Sampler class
	 * @return {@code AbstractSamplerIndexer} instance
	 */
	public static AbstractSamplerIndexer newInstance(Vertical vertical, AbstractSampler sampler) {
		AbstractSamplerIndexer result = null;
		try
		{
			Class<? extends AbstractSamplerIndexer> classType = CommonUtils.getSubClassType(AbstractSamplerIndexer.class, System.getProperty(SAMPLER_INDEXER_KEY));
			if(classType == LuceneSamplerIndexer.class) {
				result = new LuceneSamplerIndexer(vertical, sampler);
			} else {
				throw new ClassNotFoundException();
			}
		}
		catch(Exception ex) {
			LogFactory.getLog(AbstractSamplerIndexer.class).error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
	
	/**
	 * @return the foundTerms
	 */
	public List<String> getFoundTerms() {
		return foundTerms;
	}



	/**
	 * @return the newTerms
	 */
	public List<String> getNewTerms() {
		return newTerms;
	}


	/**
	 * @return the uniqueTerms
	 */
	public List<String> getUniqueTerms() {
		return new ArrayList<String>(uniqueTerms.keySet());
	}


	/**
	 * @return the uniqueTerms count
	 */
	public int getUniqueTermsCount() {
		return uniqueTerms.size();
	}

	
	
	/**
	 * Stores the term frequency/document frequency in the corresponding file
	 */
	public void storeTF_DF() {
		try(FileWriterHelper fileWriter = new FileWriterHelper(tf_dfFile)) {
			fileWriter.open(true);
			fileWriter.writeLine("TERM,TF,DF");
			
			for(Map.Entry<String, TF_DF> entry : uniqueTerms.entrySet()) {
				fileWriter.writeLine(MessageFormat.format("{0},{1},{2}", entry.getKey(), entry.getValue().getTermFrequency(), entry.getValue().getDocumentFrequency()));
			}
		}
	}



	/**
	 * Tokenizes the sampled document
	 * @param document Sampled document
	 * @return List of found terms
	 */
	public abstract List<String> tokenize(SampledDocument<?> document);
	
	/**
	 * Stores all the tokenized terms in the main index
	 */
	public abstract void storeIndex();
	
	


	
	/**
	 * Term Frequency/Document Frequency Bean
	 * @author andres
	 *
	 */
	protected static class TF_DF {
		private long termFrequency;
		private long documentFrequency;
		
		/**
		 * Default Constructor
		 */
		public TF_DF() {
		}
		
		
		/**
		 * Constructor with values
		 * @param termFrequency
		 * @param documentFrequency
		 */
		public TF_DF(long termFrequency, long documentFrequency) {
			this.termFrequency = termFrequency;
			this.documentFrequency = documentFrequency;
		}



		/**
		 * @return the termFrequency
		 */
		public long getTermFrequency() {
			return termFrequency;
		}
		
		/**
		 * Increments the current term frequency
		 * @param value Value to increment
		 */
		public void incrementTermFrequency(long value) {
			this.termFrequency += value;
		}

		/**
		 * @return the documentFrequency
		 */
		public long getDocumentFrequency() {
			return documentFrequency;
		}
		
		/**
		 * Increments the current document frequency
		 * @param value Value to increment
		 */
		public void incrementDocumentFrequency(long value) {
			this.documentFrequency += value;
		}
	}
}
