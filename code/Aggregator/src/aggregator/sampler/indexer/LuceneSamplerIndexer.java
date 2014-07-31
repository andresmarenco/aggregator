package aggregator.sampler.indexer;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import aggregator.beans.SampledDocument;
import aggregator.beans.TFDF;
import aggregator.beans.Vertical;
import aggregator.sampler.AbstractSampler;
import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;
import aggregator.util.analysis.AggregatorAnalyzer;

public class LuceneSamplerIndexer extends AbstractSamplerIndexer {
	
	private Analyzer analyzer;
	
	
	/**
	 * Default Constructor
	 * @param vertical Vertical to index
	 */
	public LuceneSamplerIndexer(Vertical vertical) {
		super(vertical);
		
//		String tokensFileName = System.getProperty(TOKENS_FILE_PATTERN_KEY)
//				.replace("{vertical}", vertical.getId())
//				.replace("{timestamp}", CommonUtils.getTimestampString());
//		
//		String tf_dfFileName = System.getProperty(TF_DF_FILE_PATTERN_KEY)
//				.replace("{vertical}", vertical.getId())
//				.replace("{timestamp}", CommonUtils.getTimestampString());
		
//		this.tokensFile = indexPath.resolve(tokensFileName);
//		this.tf_dfFile = indexPath.resolve(tf_dfFileName);
		this.analyzer = new AggregatorAnalyzer(Version.LUCENE_4_9);
	}
	
	
	

//	/**
//	 * Default Constructor
//	 * @param vertical Vertical to index
//	 * @param sampler Sampler class
//	 */
//	public LuceneSamplerIndexer(Vertical vertical, AbstractSampler sampler) {
//		super(vertical);
//		
//		String tokensFileName = System.getProperty(TOKENS_FILE_PATTERN_KEY)
//				.replace("{vertical}", vertical.getId())
//				.replace("{sampler}", sampler.getSamplerCodeName())
//				.replace("{timestamp}", CommonUtils.getTimestampString());
//
//		String tf_dfFileName = System.getProperty(TF_DF_FILE_PATTERN_KEY)
//				.replace("{vertical}", vertical.getId())
//				.replace("{sampler}", sampler.getSamplerCodeName())
//				.replace("{timestamp}", CommonUtils.getTimestampString());
//
//		this.tf_dfFile = indexPath.resolve(tf_dfFileName);
//		this.tokensFile = indexPath.resolve(tokensFileName);
//		this.analyzer = new AggregatorAnalyzer(Version.LUCENE_4_9);
//	}

	@Override
	public List<String> tokenize(SampledDocument<?> document) {
		foundTerms.clear();
		newTerms.clear();
		
		try
		{
			HashMap<String, Integer> termFrequency = new HashMap<String, Integer>();
			List<Entry<String, String>> docText = samplerParser.parseDocument(document);
			
			// Joins the document elements
			StringBuilder stringBuilder = new StringBuilder();
			for(Entry<String, String> entry : docText) {
				stringBuilder.append(entry.getValue()).append(" ");
			}
			
			
			try(TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(stringBuilder.toString()))) {
				tokenStream.reset();
				
				// Tokenizes the document elements
			    while (tokenStream.incrementToken()) {
			    	String term = tokenStream.getAttribute(CharTermAttribute.class).toString();
		    		foundTerms.add(term);
		    		
		    		Integer tf = termFrequency.get(term);
		    		if(tf == null) {
		    			termFrequency.put(term, 1);
		    		} else {
		    			termFrequency.put(term, tf+1);
		    		}
			    }

				
				// Calculates the global term frequency/document frequency
				for(Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
					TFDF tf_df = uniqueTerms.get(entry.getKey());
					if(tf_df == null) {
						uniqueTerms.put(entry.getKey(), new TFDF(entry.getValue(), 1));
						newTerms.add(entry.getKey());
					} else {
						tf_df.incrementTermFrequency(entry.getValue());
						tf_df.incrementDocumentFrequency(1);
					}
				}
				
			    
			    // Stores the found terms into the corresponding file
//				try(FileWriterHelper fileWriter = new FileWriterHelper(tokensFile)) {
//					fileWriter.open(true);
//					for(String token : foundTerms) {
//						fileWriter.writeLine(token);
//					}
//				}
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




	@Override
	public void close() throws IOException {
		this.analyzer.close();
	}
}
