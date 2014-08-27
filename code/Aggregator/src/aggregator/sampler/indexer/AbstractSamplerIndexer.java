package aggregator.sampler.indexer;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.SampledDocument;
import aggregator.beans.TFDF;
import aggregator.beans.Vertical;
import aggregator.beans.VerticalCollection;
import aggregator.sampler.analysis.IntermediateResults;
import aggregator.sampler.analysis.MemoryIntermediateResults;
import aggregator.sampler.parser.SamplerParser;
import aggregator.util.CommonUtils;
import aggregator.verticalwrapper.VerticalWrapperController;

public abstract class AbstractSamplerIndexer implements Closeable {
	
	public static final String INDEX_TEXT_FIELD = "text";
	public static final String INDEX_DOC_NAME_FIELD = "docName";
	public static final String INDEX_VERTICAL_FIELD = "vertical";
	
	private static final String SAMPLER_INDEXER_KEY = "aggregator.sampler.indexer";
	
	protected Vertical vertical;
	protected SamplerParser samplerParser;
	protected IntermediateResults intermediateResults;
	protected Path indexPath;
	protected Log log = LogFactory.getLog(getClass());
	
	
	/**
	 * Default Constructor
	 */
	public AbstractSamplerIndexer() {
		try
		{
			this.indexPath = CommonUtils.getIndexPath();
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	/**
	 * Default Constructor
	 * @param vertical Vertical to index
	 */
	public AbstractSamplerIndexer(Vertical vertical) {
		this.vertical = vertical;
		this.samplerParser = VerticalWrapperController.getInstance().getVerticalConfig(vertical).newIndexParserInstance();
		this.intermediateResults = new MemoryIntermediateResults();
		
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
	 * @return {@code AbstractSamplerIndexer} instance
	 */
	public static AbstractSamplerIndexer newInstance() {
		AbstractSamplerIndexer result = null;
		try
		{
			Class<? extends AbstractSamplerIndexer> classType = CommonUtils.getSubClassType(AbstractSamplerIndexer.class, System.getProperty(SAMPLER_INDEXER_KEY));
			if(classType == LuceneSamplerIndexer.class) {
				result = new LuceneSamplerIndexer();
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
	 * Creates a new instance of the corresponding {@code AbstractSamplerIndexer}
	 * based on the configuration file
	 * @param vertical Vertical to index
	 * @return {@code AbstractSamplerIndexer} instance
	 */
	public static AbstractSamplerIndexer newInstance(Vertical vertical) {
		AbstractSamplerIndexer result = null;
		try
		{
			Class<? extends AbstractSamplerIndexer> classType = CommonUtils.getSubClassType(AbstractSamplerIndexer.class, System.getProperty(SAMPLER_INDEXER_KEY));
			if(classType == LuceneSamplerIndexer.class) {
				result = new LuceneSamplerIndexer(vertical);
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
		return intermediateResults.getNewTerms();
	}



	/**
	 * @return the newTerms
	 */
	public List<String> getNewTerms() {
		return intermediateResults.getNewTerms();
	}


	/**
	 * @return the uniqueTerms
	 */
	public Set<Map.Entry<String, TFDF>> getUniqueTerms() {
		return intermediateResults.getUniqueTerms();
	}


	/**
	 * @return the uniqueTerms count
	 */
	public int getUniqueTermsCount() {
		return intermediateResults.getUniqueTermsCount();
	}



	/**
	 * @return the intermediateResults
	 */
	public IntermediateResults getIntermediateResults() {
		return intermediateResults;
	}



	/**
	 * @param intermediateResults the intermediateResults to set
	 */
	public void setIntermediateResults(IntermediateResults intermediateResults) {
		this.intermediateResults = intermediateResults;
	}



	/**
	 * Tokenizes the sampled document
	 * @param document Sampled document
	 * @return List of found terms
	 */
	public abstract List<String> tokenize(SampledDocument<?> document);
	
	/**
	 * Stores all the tokenized terms in the main index
	 * @param analysisPath Analysis path
	 * @param collection Vertical collection
	 */
	public abstract void storeIndex(String analysisPath, VerticalCollection collection);
	
}
