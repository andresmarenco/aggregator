package aggregator.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import aggregator.beans.Vertical;

public class SeedGenerator {
	
	private static final Log log = LogFactory.getLog(SeedGenerator.class);
	
	/**
	 * Generates a seed based on the vertical description
	 * @param vertical Source vertical
	 * @return Generated seed
	 */
	public static final String generateSeed(Vertical vertical) {
		List<String> terms = getTermsList(vertical);
		return terms.get(new Random().nextInt(terms.size()));
	}
	
	
	
	/**
	 * Generates a seed based on the given string
	 * @param terms Source for the terms
	 * @return Generated seed
	 */
	public static final String generateSeed(String source) {
		List<String> terms = getTermsList(source);
		return terms.get(new Random().nextInt(terms.size()));
	}
	
	
	
	
	/**
	 * Generates a seed based on the given list of terms
	 * @param terms List of source terms
	 * @return Generated seed
	 */
	public static final String generateSeed(List<String> terms) {
		return terms.get(new Random().nextInt(terms.size()));
	}
	
	
	
	
	/**
	 * Gets the list of possible terms based on the vertical description
	 * @param source Vertical source
	 * @return List of terms
	 */
	public static final List<String> getTermsList(Vertical source) {
		return getTermsList(source.getDescription());
	}
	
	
	
	
	/**
	 * Gets the list of possible terms based on the given string
	 * @param source Source for the terms
	 * @return List of terms
	 */
	public static final List<String> getTermsList(String source) {
		List<String> terms = new ArrayList<String>();
		
		try(Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_4_9))
		{
			TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(source));
			tokenStream.reset();
		    while (tokenStream.incrementToken()) {
		    	String term = tokenStream.getAttribute(CharTermAttribute.class).toString();
		    	if(!terms.contains(term)) {
		    		terms.add(term);
		    	}
		    
		    }
	    }
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return terms;
	}

}
