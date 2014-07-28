package aggregator.util.delay;

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RandomDelay implements DelayHandler {
	
	private Random randomSeed;
	private Integer minimum;
	private Integer maximum;
	private Log log = LogFactory.getLog(RandomDelay.class);
	
	/**
	 * Default Constructor
	 */
	public RandomDelay() {
		this.randomSeed = new Random();
	}
	
	/**
	 * Default Constructor
	 * @param maxSecs Upper bound (in seconds) for the delay
	 */
	public RandomDelay(int maxSecs) {
		this.randomSeed = new Random();
		this.maximum = maxSecs;
	}
	
	/**
	 * Default Constructor
	 * @param minSecs Lower bound (in seconds) for the delay
	 * @param maxSecs Upper bound (in seconds) for the delay
	 */
	public RandomDelay(int minSecs, int maxSecs) {
		this.randomSeed = new Random();
		this.minimum = minSecs;
		this.maximum = maxSecs;
		
		if(maxSecs < minSecs) {
			maxSecs = minSecs;
		}
	}

	@Override
	public long executeDelay() {
		long timeout = 0;
		
		try
		{
			if((minimum == null) && (maximum == null)) {
				timeout = randomSeed.nextInt() * 1000;
			} else if((minimum == null) && (maximum != null)) {
				timeout = randomSeed.nextInt(maximum.intValue() + 1) * 1000;
			} else if((minimum != null) && (maximum != null)) {
				timeout = (minimum.longValue() * 1000) + (randomSeed.nextInt(maximum.intValue() - minimum.intValue() + 1) * 1000);
			}
			
			Thread.sleep(timeout);
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return timeout;
	}

}
