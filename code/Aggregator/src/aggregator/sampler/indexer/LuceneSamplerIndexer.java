package aggregator.sampler.indexer;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.TermsEnum;

import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;
import aggregator.beans.VerticalConfig;
import aggregator.sampler.parser.SamplerParser;
import aggregator.util.FileWriterHelper;

public class LuceneSamplerIndexer extends AbstractSamplerIndexer {
	
	private static final String TOKENS_FILE_PATTERN_KEY = "aggregator.sampler.indexer.tokensFilePattern";
	private Path tokensFile;

	/**
	 * Default Constructor
	 * @param vertical Vertical to index
	 * @param verticalConfig Vertical Configuration
	 */
	public LuceneSamplerIndexer(Vertical vertical, VerticalConfig verticalConfig) {
		super(vertical, verticalConfig);
		this.tokensFile = indexPath.resolve(System.getProperty(TOKENS_FILE_PATTERN_KEY));
	}

	@Override
	public List<String> tokenize(SampledDocument<?> document) {
		foundTerms.clear();
		newTerms.clear();
		
		try
		{
			List<Entry<String, String>> docText = samplerParser.parseDocument(document);
			
			
			
			
//			Files.newBufferedReader(tokensFile).
			
			
			Files.write(tokensFile, foundTerms, Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
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
