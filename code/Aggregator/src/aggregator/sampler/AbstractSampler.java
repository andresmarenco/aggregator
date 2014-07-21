package aggregator.sampler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractSampler {

	public static final String TIMESTAMP_PATTERN_KEY = "aggregator.sampler.timeStampPattern";
	protected Log log = LogFactory.getLog(getClass());
	
	/**
	 * Executes the sampling process
	 */
	public abstract void execute();
}
