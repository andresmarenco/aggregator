package aggregator.sampler.output;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;

public abstract class AbstractSamplerOutput {

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
	 * Outputs the given document
	 * @param document Sampled document
	 */
	public abstract void outputDocument(SampledDocument<?> document);
	
	/**
	 * Opens the required resources
	 */
	public abstract void openResources();
	
	/**
	 * Closes the opened resources
	 */
	public abstract void closeResources();
}
