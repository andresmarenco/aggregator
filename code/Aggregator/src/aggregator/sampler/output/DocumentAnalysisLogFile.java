package aggregator.sampler.output;

import java.nio.file.Path;
import java.text.MessageFormat;

import org.apache.commons.logging.LogFactory;

import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;

public class DocumentAnalysisLogFile extends FileWriterHelper {

	/**
	 * Default Constructor
	 * @param filePath File Path
	 */
	public DocumentAnalysisLogFile(Path filePath) {
		super(filePath);
		this.open(false);
		
		this.writeLine("FILENAME,DOCID,TITLE,FOUND_TERMS,NEW_TERMS");
	}

	
	/**
	 * Creates a new instance of the file in the default path
	 * @param fileName Name of the file
	 * @return Log file
	 */
	public static DocumentAnalysisLogFile newInstance(String fileName) {
		DocumentAnalysisLogFile result = null;
		try
		{
			Path analysisPath = CommonUtils.getAnalysisPath();
			result = new DocumentAnalysisLogFile(analysisPath.resolve(fileName.replace("{docType}", "docs")));
		}
		catch(Exception ex) {
			LogFactory.getLog(DocumentAnalysisLogFile.class).error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
	
	/**
	 * Writes the document entry
	 * @param filename File name
	 * @param docid Document ID
	 * @param title Document Title
	 * @param foundTerms Found terms in the document
	 * @param newTerms New terms in the document
	 */
	public void writeDocument(String filename, String docid, String title, long foundTerms, long newTerms) {
		this.writeLine(MessageFormat.format("{0},{1},{2},{3},{4}", filename, docid, title, String.valueOf(foundTerms), String.valueOf(newTerms)));
	}

}
