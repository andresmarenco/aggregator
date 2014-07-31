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
	
	/**
	 * Gets the list of used terms
	 * @return List of used terms
	 */
	public List<String> getUsedTerms();
	
	/**
	 * Gets the code name for the query strategy
	 * @return Code name for the query strategy
	 */
	public String getCodeName();
}
