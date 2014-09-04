package aggregator.sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.Vertical;
import aggregator.util.CommonUtils;
import aggregator.util.delay.DelayHandler;
import aggregator.util.delay.FixedDelay;
import aggregator.util.delay.NullDelay;
import aggregator.util.delay.RandomDelay;

public abstract class AbstractSampler {
	
	private static final String SAMPLER_KEY = "aggregator.sampler";
	private static final String QUERY_STRATEGY_KEY = "aggregator.sampler.queryStrategy";
	private static final String ANALYSIS_FILE_PATTERN_KEY = "aggregator.sampler.analysisFilePattern";
	private static final String LOG_FILE_PATTERN_KEY = "aggregator.sampler.logFilePattern";
	private static final String SAMPLE_FILE_PATTERN_KEY = "aggregator.sampler.sampleFilePattern";
	private static final String DELAY_KEY = "aggregator.sampler.delay";
	private static final String DELAY_MIN_KEY = "aggregator.sampler.delay.min";
	private static final String DELAY_MAX_KEY = "aggregator.sampler.delay.max";
	
	protected List<String> sampledDocuments;
	protected List<Map.Entry<String, List<String>>> documentTerms;
	protected DelayHandler delayHandler;
	protected Vertical vertical;
	protected QueryStrategy queryStrategy;
	
	protected Log log = LogFactory.getLog(getClass());
	
	
	/**
	 * Default Constructor
	 * @param vertical Vertical to sample
	 */
	public AbstractSampler(Vertical vertical) {
		this.vertical = vertical;
		this.sampledDocuments = new ArrayList<String>();
		this.documentTerms = new ArrayList<Map.Entry<String,List<String>>>();
		
		this.reset();
		this.initDelay();
		this.initQueryStrategy();
	}
	
	
	/**
	 * Initializes the delay handler
	 */
	private void initDelay() {
		String delayTime = System.getProperty(DELAY_KEY);
		if((StringUtils.isNotBlank(delayTime)) && (NumberUtils.isNumber(delayTime))) {
			this.delayHandler = new FixedDelay(Integer.parseInt(delayTime));
		} else {
			String minDelayTime = System.getProperty(DELAY_MIN_KEY);
			String maxDelayTime = System.getProperty(DELAY_MAX_KEY);
			
			if(((StringUtils.isNotBlank(minDelayTime)) && (NumberUtils.isNumber(minDelayTime))) &&
					((StringUtils.isNotBlank(maxDelayTime)) && (NumberUtils.isNumber(maxDelayTime))))
			{
				this.delayHandler = new RandomDelay(Integer.parseInt(minDelayTime), Integer.parseInt(maxDelayTime));
			} else {
				this.delayHandler = new NullDelay();
			}
		}
	}
	
	
	
	/**
	 * Creates a new instance of the configured query strategy
	 * @return Query strategy instance
	 */
	private void initQueryStrategy() {
		Class<? extends QueryStrategy> queryStrategyClass = CommonUtils.getSubClassType(QueryStrategy.class, System.getProperty(QUERY_STRATEGY_KEY));
		if(queryStrategyClass == LRDQueryStrategy.class) {
			this.queryStrategy = new LRDQueryStrategy(vertical);
		} else {
			log.error("Undefined or unknown query strategy");
		}
	}
	
	
	
	/**
	 * Creates a new instance of the corresponding {@code AbstractSampler}
	 * based on the configuration file
	 * @param vertical Vertical to sample
	 * @return {@code AbstractSampler} instance
	 */
	public static AbstractSampler newInstance(Vertical vertical) {
		AbstractSampler result = null;
		try
		{
			Class<? extends AbstractSampler> classType = CommonUtils.getSubClassType(AbstractSampler.class, System.getProperty(SAMPLER_KEY));
			if(classType != null) {
				result = classType.getConstructor(Vertical.class).newInstance(vertical);
			}
		}
		catch(Exception ex) {
			LogFactory.getLog(AbstractSampler.class).error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
	
	/**
	 * Gets the analysis file pattern
	 * @param timestamp Execution timestamp
	 * @return Analysis file pattern
	 */
	protected String getAnalysisFilePattern(long timestamp) {
		return this.getFilePattern(ANALYSIS_FILE_PATTERN_KEY, timestamp);
	}
	
	
	/**
	 * Gets the log file pattern
	 * @param timestamp Execution timestamp
	 * @return Log file pattern
	 */
	protected String getLogFilePattern(long timestamp) {
		return this.getFilePattern(LOG_FILE_PATTERN_KEY, timestamp);
	}
	
	
	/**
	 * Gets the sample file pattern
	 * @param timestamp Execution timestamp
	 * @return Sample file pattern
	 */
	protected String getSampleFilePattern(long timestamp) {
		return this.getFilePattern(SAMPLE_FILE_PATTERN_KEY, timestamp);
	}
	
	
	/**
	 * Gets the file pattern replacing the available values
	 * @param filePatternKey Key for the file pattern to use
	 * @param timestamp Execution timestamp
	 * @return File pattern
	 */
	private String getFilePattern(String filePatternKey, long timestamp) {
		return System.getProperty(filePatternKey)
				.replace("{vertical}", vertical.getId())
				.replace("{execution}", this.getSamplerExecutionCodeName())
				.replace("{sampler}", this.getSamplerCodeName())
				.replace("{timestamp}", CommonUtils.getTimestampString(timestamp));
	}
	
	
	
	
	/**
	 * Resets the sampler variables
	 */
	public void reset() {
		sampledDocuments.clear();
		documentTerms.clear();
	}
	
	
	
	/**
	 * Resets the sampler variables and sets a new vertical to sample
	 * @param vertical Vertical to sample
	 */
	public void reset(Vertical vertical) {
		this.vertical = vertical;
		this.initQueryStrategy();
		this.reset();
	}	
	
	/**
	 * Executes the sampling process
	 */
	public abstract void execute();
	
	/**
	 * Gets the sampler name
	 * @return Sampler name
	 */
	public abstract String getSamplerName();
	
	/**
	 * Gets the sampler code name
	 * @return Sampler code name
	 */
	public abstract String getSamplerCodeName();
	
	/**
	 * Gets the sampler execution code name
	 * @return Sampler execution code name
	 */
	public abstract String getSamplerExecutionCodeName();
}
