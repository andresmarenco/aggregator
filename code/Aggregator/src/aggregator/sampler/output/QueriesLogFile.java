package aggregator.sampler.output;

import java.nio.file.Path;
import java.text.MessageFormat;

import org.apache.commons.logging.LogFactory;

import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;

public class QueriesLogFile extends FileWriterHelper {

	/**
	 * Default Constructor
	 * @param filePath File Path
	 */
	public QueriesLogFile(Path filePath) {
		super(filePath);
		this.open(false);
		
		this.writeLine("QUERY_ID,TERM,RESULTS");
	}

	
	/**
	 * Creates a new instance of the file in the default path
	 * @param fileName Name of the file
	 * @return Log file
	 */
	public static QueriesLogFile newInstance(String fileName) {
		QueriesLogFile result = null;
		try
		{
			Path analysisPath = CommonUtils.getAnalysisPath();
			result = new QueriesLogFile(analysisPath.resolve(fileName.replace("{docType}", "queries")));
		}
		catch(Exception ex) {
			LogFactory.getLog(QueriesLogFile.class).error(ex.getMessage(), ex);
		}
		
		return result;
	}

	
	
	/**
	 * Writes a query term into the file
	 * @param queryId Query Id
	 * @param term Query Term
	 * @param results Quantity of returned results
	 */
	public void writeQueryTerm(String queryId, String term, int results) {
		this.writeLine(MessageFormat.format("{0},{1},{2}", queryId, term, results));
	}
}
