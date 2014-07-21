package aggregator.verticalwrapper.auth;

import java.util.List;

public interface AuthScript<T> {
	/**
	 * Executes the authentication in the vertical
	 * @param script Authentication script
	 * @return Result of the authentication
	 */
	public T execute(List<String> script);
}
