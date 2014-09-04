package aggregator.sampler.output;

import java.nio.file.Path;
import java.text.MessageFormat;

import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;

public class SizeEstimationExecutionLogFile extends FileWriterHelper {
	
	private PeriodFormatter periodFormatter;

	/**
	 * Default Constructor
	 * @param filePath File Path
	 */
	public SizeEstimationExecutionLogFile(Path filePath) {
		super(filePath);
		
		this.periodFormatter = new PeriodFormatterBuilder()
			.printZeroAlways()
			.appendHours()
			.appendSeparator("h ")
			.appendMinutes()
			.appendSeparator("m ")
			.appendSeconds()
			.appendLiteral("sec")
			.toFormatter();

		this.open(false);
	}

	
	/**
	 * Creates a new instance of the file in the default path
	 * @param fileName Name of the file
	 * @return Log file
	 */
	public static SizeEstimationExecutionLogFile newInstance(String fileName) {
		SizeEstimationExecutionLogFile result = null;
		try
		{

			Path analysisPath = CommonUtils.getLogPath();
			result = new SizeEstimationExecutionLogFile(analysisPath.resolve(fileName.replace("{docType}", "sizeLog")));
		}
		catch(Exception ex) {
			LogFactory.getLog(SizeEstimationExecutionLogFile.class).error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
	
	
	/**
	 * Formats an execution log message
	 * @param pattern Message to log
	 * @param arguments Arguments for the message
	 */
	public void writeLogMessage(String pattern, Object... arguments) {
		this.writeLine(MessageFormat.format("[{0}] {1}", LocalDateTime.now().toString(timestampPattern), MessageFormat.format(pattern, arguments)));
	}
	
	
	
	/**
	 * Gets a formatted message with the time period
	 * @param startTime Start time
	 * @param endTime End time
	 * @return Formatted message with the time period
	 */
	public String getTimePeriodMessage(long startTime, long endTime) {
		Period executionTime = new Period(startTime, endTime);
		return this.periodFormatter.print(executionTime);
	}
}
