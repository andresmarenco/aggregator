package aggregator.verticalwrapper.srr;

import java.util.List;

import aggregator.beans.QueryResult;

public interface SRRScript<T> {
	/**
	 * Executes the search result records extraction
	 * @param queryResult Obtained query results
	 * @param script SRR Script
	 * @return List with extracted results
	 */
	public List<QueryResult> execute(T queryResult, List<String> script);
}
