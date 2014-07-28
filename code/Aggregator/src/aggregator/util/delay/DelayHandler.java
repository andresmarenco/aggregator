package aggregator.util.delay;

public interface DelayHandler {
	/**
	 * Executes the delay in the given object
	 * @param object Object to delay
	 * @return Delayed seconds
	 */
	public long executeDelay();
}
