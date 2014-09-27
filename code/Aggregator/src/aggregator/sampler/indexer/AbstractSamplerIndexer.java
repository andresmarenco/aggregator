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
	
	public static final String INDEX_CONTENTS_FIELD = "contents";
	public static final String INDEX_DOC_NAME_FIELD = "docName";
	public static final String INDEX_VERTICAL_FIELD = "vertical";
	
	private static final String SAMPLER_INDEXER_KEY = "aggregator.sampler.indexer";
	private static final String INDEX_USE_VERTICAL_NAME_KEY = "aggregator.sampler.indexer.useVerticalName";
	private static final String INDEX_USE_VERTICAL_DESCRIPTION_KEY = "aggregator.sampler.indexer.useVerticalDescription";
	private static final String INDEX_USE_DOCS_KEY = "aggregator.sampler.indexer.useDocs";
	private static final String INDEX_USE_SNIPPETS_KEY = "aggregator.sampler.indexer.useSnippets";
	private static final String INDEX_USE_WORDNET_KEY = "aggregator.sampler.indexer.useWordNet";
	private static final String INDEX_NAME_KEY = "aggregator.sampler.indexer.name";
	
	protected Vertical vertical;
	protected SamplerParser samplerParser;
	protected IntermediateResults intermediateResults;
	protected Path indexPath;
	protected Log log = LogFactory.getLog(getClass());
	
	
	/**
	 * Default Constructor
	 */
	public AbstractSamplerIndexer() {
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
	 * Gets the name of the current index
	 * @return Name of the current index
	 */
	public static final String getIndexName() {
		return System.getProperty(INDEX_NAME_KEY);
	}
	
	
	/**
	 * Gets if the index contains the vertical name in each document
	 * @return True if the index contains the vertical name in each document
	 */
	public static final boolean isIndexVerticalName() {
		return Boolean.parseBoolean(System.getProperty(INDEX_USE_VERTICAL_NAME_KEY, "false"));
	}
	
	
	/**
	 * Gets if the index contains the vertical description in each document
	 * @return True if the index contains the vertical description in each document
	 */
	public static final boolean isIndexVerticalDescription() {
		return Boolean.parseBoolean(System.getProperty(INDEX_USE_VERTICAL_DESCRIPTION_KEY, "false"));
	}
	
	
	/**
	 * Gets if the index contains the snippets
	 * @return True if the index contains the snippets
	 */
	public static final boolean isIndexSnippets() {
		return Boolean.parseBoolean(System.getProperty(INDEX_USE_SNIPPETS_KEY, "false"));
	}
	
	
	/**
	 * Gets if the index contains the documents' text
	 * @return True if the index contains the documents' text
	 */
	public static final boolean isIndexDocs() {
		return Boolean.parseBoolean(System.getProperty(INDEX_USE_DOCS_KEY, "true"));
	}
	
	
	/**
	 * Gets if the index contains the WordNet text
	 * @return True if the index contains the WordNet text
	 */
	public static final boolean isIndexWordNet() {
		return Boolean.parseBoolean(System.getProperty(INDEX_USE_WORDNET_KEY, "true"));
	}



	/**
	 * Tokenizes the sampled document
	 * @param document Sampled document
	 * @return List of found terms
	 */
	public abstract List<String> tokenize(SampledDocument<?> document);

	/**
	 * Tokenizes the given string
	 * @param docId Document Id
	 * @param text String to sample
	 * @param more Extra string to sample
	 * @return List of found terms
	 */
	public abstract List<String> tokenize(String docId, String text, String... more);
	
	/**
	 * Stores all the tokenized terms in the main index
	 * @param collection Vertical collection
	 */
	public abstract void storeIndex(VerticalCollection collection);
	
}
