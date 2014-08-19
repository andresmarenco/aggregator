package aggregator.util.analysis;

import java.io.IOException;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.FilteringTokenFilter;
import org.apache.lucene.util.Version;

public class NumberFilter extends FilteringTokenFilter {
	
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

	/**
	 * Default Constructor
	 * @param version Lucene version
	 * @param in Input stream
	 */
	public NumberFilter(Version version, TokenStream in) {
		super(version, in);
	}

	@Override
	protected boolean accept() throws IOException {
		return !NumberUtils.isNumber(new String(termAtt.buffer(), 0, termAtt.length()));
	}

}
