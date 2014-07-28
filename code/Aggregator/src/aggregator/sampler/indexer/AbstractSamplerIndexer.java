package aggregator.sampler.indexer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;
import aggregator.sampler.AbstractSampler;
import aggregator.sampler.parser.SamplerParser;
import aggregator.util.CommonUtils;
import aggregator.verticalwrapper.VerticalWrapperController;

public abstract class AbstractSamplerIndexer {
	
	private static final String SAMPLER_INDEXER_KEY = "aggregator.sampler.indexer";

	protected Vertical vertical;
	protected SamplerParser samplerParser;
	protected List<String> foundTerms;
	protected List<String> newTerms;
	protected List<String> uniqueTerms;
	protected Path indexPath;
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
		this.uniqueTerms = new ArrayList<String>();
		
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
		return uniqueTerms;
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
}
