package aggregator.util.delay;

public class NullDelay implements DelayHandler {

	@Override
	public long executeDelay() {
		return 0;
	}

}
