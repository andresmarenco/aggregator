package aggregator.sampler;

import java.util.List;

public interface QueryStrategy {
	
	/**
	 * Adds the list of terms to the available ones for querying
	 * @param terms List of new available terms
	 */
	public void addTerms(List<String> terms);
	
	/**
	 * Gets a unused query term
	 * @return Query term
	 */
	public String getNextTerm();
}
