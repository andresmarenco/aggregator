package aggregator.sampler.analysis.sizeestimation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.TFDF;
import aggregator.beans.Vertical;
import aggregator.util.delay.DelayHandler;
import aggregator.util.delay.FixedDelay;
import aggregator.util.delay.NullDelay;
import aggregator.util.delay.RandomDelay;
import aggregator.verticalwrapper.AbstractVerticalWrapper;
import aggregator.verticalwrapper.VerticalWrapperController;

public abstract class AbstractSizeEstimation {
	
	private static final String DELAY_KEY = "aggregator.sampler.delay";
	private static final String DELAY_MIN_KEY = "aggregator.sampler.delay.min";
	private static final String DELAY_MAX_KEY = "aggregator.sampler.delay.max";
	
	protected Vertical vertical;
	protected AbstractVerticalWrapper verticalWrapper;
	protected DelayHandler delayHandler;
	protected Log log = LogFactory.getLog(getClass());
	
	/**
	 * Default Constructor
	 * @param vertical Sampled Vertical
	 */
	public AbstractSizeEstimation(Vertical vertical) {
		VerticalWrapperController wrapperController = VerticalWrapperController.getInstance();
		
		this.vertical = vertical;
		this.verticalWrapper = wrapperController.createVerticalWrapper(vertical);
		this.initDelay();
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
}
