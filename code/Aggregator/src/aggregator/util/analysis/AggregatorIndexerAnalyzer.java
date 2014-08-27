package aggregator.util.analysis;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

public class AggregatorIndexerAnalyzer extends Analyzer {

	/** Default maximum allowed token length */
	public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

	private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

	private final Version matchVersion;
	
	/**
	 * Creates a new {@link WhitespaceAnalyzer}
	 * @param matchVersion Lucene version to match See {@link <a href="#version">above</a>}
	 */
	public AggregatorIndexerAnalyzer(Version matchVersion) {
		this.matchVersion = matchVersion;
	}

	/**
	 * Set maximum allowed token length.  If a token is seen
	 * that exceeds this length then it is discarded.  This
	 * setting only takes effect the next time tokenStream or
	 * tokenStream is called.
	 */
	public void setMaxTokenLength(int length) {
		maxTokenLength = length;
	}
	    
	/**
	 * @see #setMaxTokenLength
	 */
	public int getMaxTokenLength() {
		return maxTokenLength;
	}
	  
	@Override
	protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
		final StandardTokenizer src = new StandardTokenizer(matchVersion, reader);
	    src.setMaxTokenLength(maxTokenLength);
	    TokenStream tok = new StandardFilter(matchVersion, src);
	    tok = new PorterStemFilter(tok);
	    
	    return new TokenStreamComponents(src, tok) {
	      @Override
	      protected void setReader(final Reader reader) throws IOException {
	        src.setMaxTokenLength(AggregatorIndexerAnalyzer.this.maxTokenLength);
	        super.setReader(reader);
	      }
	    };
	}
}
