package aggregator.sampler.output;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.LogFactory;

import aggregator.beans.TFDF;
import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;

public class TFDFLogFile extends FileWriterHelper {

	/**
	 * Default Constructor
	 * @param filePath File Path
	 */
	public TFDFLogFile(Path filePath) {
		super(filePath);
		this.open(false);
		
		this.writeLine("TERM,TF,DF");
	}

	
	/**
	 * Creates a new instance of the file in the default path
	 * @param fileName Name of the file
	 * @return Log file
	 */
	public static TFDFLogFile newInstance(String fileName) {
		TFDFLogFile result = null;
		try
		{
			Path analysisPath = CommonUtils.getAnalysisPath();
			result = new TFDFLogFile(analysisPath.resolve(fileName.replace("{docType}", "tfdf")));
		}
		catch(Exception ex) {
			LogFactory.getLog(TFDFLogFile.class).error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
	
	
	
	/**
	 * Writes the term frequency/document frequency
	 * @param tfdf Term frequency/document frequency
	 */
	public void writeTFDF(Map.Entry<String, TFDF> tfdf) {
		this.writeLine(MessageFormat.format("{0},{1},{2}", tfdf.getKey(), tfdf.getValue().getTermFrequency(), tfdf.getValue().getDocumentFrequency()));
	}
	
	
	
	
	/**
	 * Writes the set of term frequencies/document frequencies
	 * @param tfdfs Set of term frequencies/document frequencies
	 */
	public void writeTFDFs(Set<Map.Entry<String, TFDF>> tfdfs) {
		for(Map.Entry<String, TFDF> entry : tfdfs) {
			this.writeTFDF(entry);
		}
	}
}
