package aggregator.sampler.output;

import java.nio.file.Path;
import java.text.MessageFormat;
import org.apache.commons.logging.LogFactory;

import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;

public class SizeEstimationStatsLogFile extends FileWriterHelper {
	
	/**
	 * Default Constructor
	 * @param filePath File Path
	 */
	public SizeEstimationStatsLogFile(Path filePath) {
		super(filePath);
		
		this.open(false);
		this.writeLine("COUNT,QUERY,MEAN_SIZE,STD_DEV,SAMPLE_VARIANCE");
	}

	
	/**
	 * Creates a new instance of the file in the default path
	 * @param fileName Name of the file
	 * @return Log file
	 */
	public static SizeEstimationStatsLogFile newInstance(String fileName) {
		SizeEstimationStatsLogFile result = null;
		try
		{
			Path analysisPath = CommonUtils.getAnalysisPath();
			result = new SizeEstimationStatsLogFile(analysisPath.resolve(fileName.replace("{docType}", "sizeStats")));
		}
		catch(Exception ex) {
			LogFactory.getLog(SizeEstimationStatsLogFile.class).error(ex.getMessage(), ex);
		}
		
		return result;
	}

	
	
	
	/**
	 * Writes the current statistics for the size estimation
	 * @param count Count of queries
	 * @param query Queried term
	 * @param meanSize Mean collection size
	 * @param standardDeviation Standard deviation
	 * @param sampleVariance Sample variance
	 */
	public void writeStats(long count, String query, double meanSize, double standardDeviation, double sampleVariance) {
		this.writeLine(MessageFormat.format("{0},{1},{2},{3},{4}",
				String.valueOf(count),
				query,
				String.valueOf(meanSize),
				String.valueOf(standardDeviation),
				String.valueOf(sampleVariance)));
	}
}
