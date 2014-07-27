package aggregator.sampler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.Vertical;
import aggregator.util.CommonUtils;

public abstract class AbstractSampler {
	
	private static final String SAMPLER_KEY = "aggregator.sampler";
	protected static final String SEED_KEY = "aggregator.sampler.seed";
	
	protected List<String> usedSeeds;
	protected Log log = LogFactory.getLog(getClass());
	
	
	/**
	 * Default Constructor
	 */
	public AbstractSampler() {
		this.usedSeeds = new ArrayList<String>();
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
	 * Executes the sampling process
	 * @param vertical Vertical to sample
	 */
	public abstract void execute(Vertical vertical);
}
