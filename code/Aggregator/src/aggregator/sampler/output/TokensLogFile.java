package aggregator.sampler.output;

import java.nio.file.Path;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;

public class TokensLogFile extends FileWriterHelper {

	/**
	 * Default Constructor
	 * @param filePath File Path
	 */
	public TokensLogFile(Path filePath) {
		super(filePath);
		this.open(false);
	}

	
	/**
	 * Creates a new instance of the file in the default path
	 * @param fileName Name of the file
	 * @return Log file
	 */
	public static TokensLogFile newInstance(String fileName) {
		TokensLogFile result = null;
		try
		{
			Path analysisPath = CommonUtils.getAnalysisPath();
			result = new TokensLogFile(analysisPath.resolve(fileName.replace("{docType}", "tokens")));
		}
		catch(Exception ex) {
			LogFactory.getLog(TokensLogFile.class).error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
	
	
	/**
	 * Writes the token into the file
	 * @param token Token
	 */
	public void writeToken(String token) {
		this.writeLine(token);
	}
	
	
	
	/**
	 * Writes the list of tokens into the file
	 * @param tokens Tokens list
	 */
	public void writeTokens(List<String> tokens) {
		for(String token : tokens) {
			this.writeToken(token);
		}
	}
}
