package aggregator.verticalwrapper.query;

import java.util.List;

public interface QueryScript<T> {
	/**
	 * Executes the given query script
	 * @param query Query string
	 * @param script Query script
	 * @return The query result
	 */
	public T execute(String query, List<String> script);
}
