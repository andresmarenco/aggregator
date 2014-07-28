package aggregator.sampler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.Vertical;
import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;
import aggregator.util.delay.DelayHandler;
import aggregator.util.delay.FixedDelay;
import aggregator.util.delay.NullDelay;
import aggregator.util.delay.RandomDelay;

public abstract class AbstractSampler {
	
	private static final String SAMPLER_KEY = "aggregator.sampler";
	private static final String LOG_FILE_PATTERN_KEY = "aggregator.sampler.logFilePattern";
	private static final String DELAY_KEY = "aggregator.sampler.delay";
	private static final String DELAY_MIN_KEY = "aggregator.sampler.delay.min";
	private static final String DELAY_MAX_KEY = "aggregator.sampler.delay.max";
	protected static final String SEED_KEY = "aggregator.sampler.seed";
	
	protected List<String> usedSeeds;
	protected String currentSeed;
	protected DelayHandler delayHandler;
	protected Path logPath;
	protected Log log = LogFactory.getLog(getClass());
	
	
	/**
	 * Default Constructor
	 */
	public AbstractSampler() {
		this.usedSeeds = new ArrayList<String>();
		this.reset();
		this.initDelay();
		
		try
		{
			this.logPath = CommonUtils.getLogPath();
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
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
	 * Creates a new instance of the corresponding {@code AbstractSampler}
	 * based on the configuration file
	 * @return {@code AbstractSampler} instance
	 */
	public static AbstractSampler newInstance() {
		AbstractSampler result = null;
		try
		{
			Class<? extends AbstractSampler> classType = CommonUtils.getSubClassType(AbstractSampler.class, System.getProperty(SAMPLER_KEY));
			if(classType != null) {
				result = classType.getConstructor().newInstance();
			}
		}
		catch(Exception ex) {
			LogFactory.getLog(AbstractSampler.class).error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
	
	/**
	 * Creates an execution log file
	 * @param vertical Vertical to sample
	 * @return Execution log file
	 */
	protected FileWriterHelper createExecutionLog(Vertical vertical) {
		String logFileName = System.getProperty(LOG_FILE_PATTERN_KEY)
				.replace("{vertical}", vertical.getId())
				.replace("{sampler}", this.getSamplerCodeName())
				.replace("{timestamp}", CommonUtils.getTimestampString());
		
		return new FileWriterHelper(logPath.resolve(logFileName));
	}
	
	
	
	/**
	 * Resets the sampler variables
	 */
	public void reset() {
		usedSeeds.clear();
		
		String seed = System.getProperty(SAMPLER_KEY);
		if(StringUtils.isNotBlank(seed)) {
			currentSeed = seed.trim();
		} else {
			log.error("No initial seed provided");
		}
	}
	
	
	/**
	 * Executes the sampling process
	 * @param vertical Vertical to sample
	 */
	public abstract void execute(Vertical vertical);
	
	/**
	 * Gets the sampler code name
	 * @return Sampler code name
	 */
	public abstract String getSamplerCodeName();
	
	/**
	 * Gets the sampler name
	 * @return Sampler name
	 */
	public abstract String getSamplerName();
}
