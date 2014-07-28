package aggregator.util.delay;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FixedDelay implements DelayHandler {
	
	private int timeout;
	private Log log = LogFactory.getLog(FixedDelay.class);
	
	/**
	 * Default Constructor
	 * @param secs Seconds to delay
	 */
	public FixedDelay(int secs) {
		this.timeout = secs;
	}
	

	@Override
	public long executeDelay() {
		try
		{
			Thread.sleep(timeout*1000);
		}
		catch (InterruptedException ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return timeout;
	}
	

}
