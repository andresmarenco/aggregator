package aggregator.sampler.output;

import java.io.Closeable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;
import aggregator.util.CommonUtils;

public abstract class AbstractSamplerOutput implements Closeable {

	private static final String SAMPLER_OUTPUT_KEY = "aggregator.sampler.output";
	
	protected Vertical vertical;
	protected Log log = LogFactory.getLog(getClass());

	/**
	 * Default Constructor
	 * @param vertical Vertical Object
	 */
	public AbstractSamplerOutput(Vertical vertical) {
		this.vertical = vertical;
	}
	
	/**
	 * Creates a new sampler output of the defined type in the properties file
	 * @param vertical Vertical object
	 * @return Sampler Output
	 */
	public static AbstractSamplerOutput newSamplerOutput(Vertical vertical) {
		AbstractSamplerOutput result = null;
		try
		{
			Class<? extends AbstractSamplerOutput> classType = CommonUtils.getSubClassType(AbstractSamplerOutput.class, System.getProperty(SAMPLER_OUTPUT_KEY));
			if(classType != null) {
				result = classType.getConstructor(Vertical.class).newInstance(vertical);
			}
		}
		catch(Exception ex) {
			LogFactory.getLog(AbstractSamplerOutput.class).error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
	
	/**
	 * Outputs the given document
	 * @param document Sampled document
	 */
	public abstract void outputDocument(SampledDocument<?> document);
	
	/**
	 * Opens the required resources
	 */
	public abstract void open();
	
	/**
	 * Closes the opened resources
	 */
	public abstract void close();
}
