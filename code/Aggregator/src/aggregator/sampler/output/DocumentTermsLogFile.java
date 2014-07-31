package aggregator.sampler.output;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;

import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;

public class DocumentTermsLogFile extends FileWriterHelper {
	
	/**
	 * Default Constructor
	 * @param filePath File Path
	 */
	public DocumentTermsLogFile(Path filePath) {
		super(filePath);
		this.open(false);
	}

	
	/**
	 * Creates a new instance of the file in the default path
	 * @param fileName Name of the file
	 * @return Log file
	 */
	public static DocumentTermsLogFile newInstance(String fileName) {
		DocumentTermsLogFile result = null;
		try
		{
			Path analysisPath = CommonUtils.getAnalysisPath();
			result = new DocumentTermsLogFile(analysisPath.resolve(fileName.replace("{docType}", "docTerms")));
		}
		catch(Exception ex) {
			LogFactory.getLog(DocumentTermsLogFile.class).error(ex.getMessage(), ex);
		}
		
		return result;
	}

	
	
	/**
	 * Writes the list of document terms
	 * @param documentTermsList Document terms list
	 */
	public void writeDocumentTerms(List<Map.Entry<String, List<String>>> documentTermsList) {
		this.writeLine(String.valueOf(documentTermsList.size()));
		for(Map.Entry<String, List<String>> document : documentTermsList) {
			for(String term : document.getValue()) {
				this.write(term + " ");
			}
			this.writeLine("");
		}
	}
}
