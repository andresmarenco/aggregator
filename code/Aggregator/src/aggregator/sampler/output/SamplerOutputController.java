package aggregator.sampler.output;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.Vertical;
import aggregator.util.CommonUtils;

public class SamplerOutputController {

	public static final String SAMPLER_OUTPUT_KEY = "aggregator.sampler.output";
	
	private Class<? extends AbstractSamplerOutput> samplerClassType;
	private Log log = LogFactory.getLog(SamplerOutputController.class);
	
	
	/**
	 * Default Constructor
	 */
	public SamplerOutputController() {
		this.samplerClassType = CommonUtils.getSubClassType(AbstractSamplerOutput.class, System.getProperty(SAMPLER_OUTPUT_KEY));
	}
	
	
	
	
	/**
	 * Creates a new sampler output of the defined type in the properties file
	 * @param vertical Vertical object
	 * @return Sampler Output
	 */
	public AbstractSamplerOutput newSamplerOutput(Vertical vertical) {
		AbstractSamplerOutput result = null;
		try
		{
			result = samplerClassType.getConstructor(Vertical.class).newInstance(vertical);
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return result;
	}
}
