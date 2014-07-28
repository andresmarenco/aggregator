package aggregator.sampler.indexer;

import java.io.StringReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;
import aggregator.sampler.AbstractSampler;
import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;

public class LuceneSamplerIndexer extends AbstractSamplerIndexer {
	
	private static final String TOKENS_FILE_PATTERN_KEY = "aggregator.sampler.indexer.tokensFilePattern";
	private Path tokensFile;
	private Analyzer analyzer;
	
	
	/**
	 * Default Constructor
	 * @param vertical Vertical to index
	 */
	public LuceneSamplerIndexer(Vertical vertical) {
		super(vertical);
		
		String tokensFileName = System.getProperty(TOKENS_FILE_PATTERN_KEY)
				.replace("{vertical}", vertical.getId())
				.replace("{timestamp}", CommonUtils.getTimestampString());
		
		this.tokensFile = indexPath.resolve(tokensFileName);
		this.analyzer = new StandardAnalyzer(Version.LUCENE_4_9);
	}
	
	
	

	/**
	 * Default Constructor
	 * @param vertical Vertical to index
	 * @param sampler Sampler class
	 */
	public LuceneSamplerIndexer(Vertical vertical, AbstractSampler sampler) {
		super(vertical);
		
		String tokensFileName = System.getProperty(TOKENS_FILE_PATTERN_KEY)
				.replace("{vertical}", vertical.getId())
				.replace("{sampler}", sampler.getSamplerCodeName())
				.replace("{timestamp}", CommonUtils.getTimestampString());
		
		this.tokensFile = indexPath.resolve(tokensFileName);
		this.analyzer = new StandardAnalyzer(Version.LUCENE_4_9);
	}

	@Override
	public List<String> tokenize(SampledDocument<?> document) {
		foundTerms.clear();
		newTerms.clear();
		
		try
		{
			List<Entry<String, String>> docText = samplerParser.parseDocument(document);
			StringBuilder stringBuilder = new StringBuilder();
			for(Entry<String, String> entry : docText) {
				stringBuilder.append(entry.getValue()).append(" ");
			}
			
			TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(stringBuilder.toString()));
			tokenStream.reset();
		    while (tokenStream.incrementToken()) {
		    	String term = tokenStream.getAttribute(CharTermAttribute.class).toString();
	    		foundTerms.add(term);
	    		if(!uniqueTerms.contains(term)) {
	    			uniqueTerms.add(term);
	    			newTerms.add(term);
	    		}
		    }
			
			try(FileWriterHelper fileWriter = new FileWriterHelper(tokensFile)) {
				fileWriter.open(true);
				for(String token : foundTerms) {
					fileWriter.writeLine(token);
				}
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return foundTerms;
	}

	@Override
	public void storeIndex() {
		// TODO Auto-generated method stub
		
	}

	
}
