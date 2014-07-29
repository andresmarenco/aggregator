package aggregator.util.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.FilteringTokenFilter;
import org.apache.lucene.util.Version;

public class SingleCharFilter extends FilteringTokenFilter {

	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

	/**
	 * Default Constructor
	 * @param version Lucene version
	 * @param in Input stream
	 */
	public SingleCharFilter(Version version, TokenStream in) {
		super(version, in);
	}

	@Override
	protected boolean accept() throws IOException {
		return termAtt.length() > 1;
	}

}
