package aggregator.sampler.parser;

import java.util.List;
import java.util.Map.Entry;

import aggregator.beans.SampledDocument;

public interface SamplerParser {
	/**
	 * Parses the sampled document
	 * @param document Sampled document
	 * @return List of key-value pairs with the parsed text of the document
	 */
	public List<Entry<String, String>> parseDocument(SampledDocument<?> document);
	
	/**
	 * Determines if a token is valid (not a number, not a stopword from the configuration file...)
	 * @param token Token to check
	 * @return True if the token is valid
	 */
	public boolean isValid(String token);
}
