package aggregator.sampler.output;

import java.nio.file.Path;
import java.text.MessageFormat;

import org.apache.commons.logging.LogFactory;

import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;

public class QueryDocumentsLogFile extends FileWriterHelper {

	/**
	 * Default Constructor
	 * @param filePath File Path
	 */
	public QueryDocumentsLogFile(Path filePath) {
		super(filePath);
		this.open(false);
		
		this.writeLine("QUERY_ID,DOC_NUMBER,DOC_ID");
	}

	
	/**
	 * Creates a new instance of the file in the default path
	 * @param fileName Name of the file
	 * @return Log file
	 */
	public static QueryDocumentsLogFile newInstance(String fileName) {
		QueryDocumentsLogFile result = null;
		try
		{
			Path analysisPath = CommonUtils.getAnalysisPath();
			result = new QueryDocumentsLogFile(analysisPath.resolve(fileName.replace("{docType}", "queryDocs")));
		}
		catch(Exception ex) {
			LogFactory.getLog(QueryDocumentsLogFile.class).error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
	
	
	/**
	 * Writes the document entry
	 * @param queryId Query Id
	 * @param docNum Document number in the query
	 * @param docId Document Id
	 */
	public void writeDocument(String queryId, String docNum, String docId) {
		this.writeLine(MessageFormat.format("{0},{1},{2}", queryId, docNum, docId));
	}

}
