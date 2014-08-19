package aggregator.sampler.analysis.sizeestimation;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import aggregator.beans.QueryResult;
import aggregator.beans.TFDF;
import aggregator.beans.Vertical;
import aggregator.sampler.output.QueryDocumentsLogFile;
import aggregator.sampler.output.SizeEstimationExecutionLogFile;
import aggregator.sampler.output.SizeEstimationStatsLogFile;
import aggregator.util.CommonUtils;
import aggregator.util.FileWriterHelper;
import aggregator.util.delay.NullDelay;

public class CaptureHistory extends AbstractSizeEstimation {

	/**
	 * Default Constructor
	 * @param vertical Sampled Vertical
	 */
	public CaptureHistory(Vertical vertical) {
		super(vertical);
	}
	
	
	
	
	/**
	 * Estimates the size of the analyzed sample using the Sample-Resample algorithm
	 * @param analysisPath Path with the analysis files for the vertical
	 * @param analysisTimestamp Timestamp of the previously analyzed files
	 */
	public void estimateSize(String analysisPath, String analysisTimestamp) {
		analysisPath = StringUtils.appendIfMissing(analysisPath, File.separator);
		
		String sizeEstimationFileName = MessageFormat.format("{0}{1}_{2}.{3}",
				analysisPath,
				vertical.getId(),
				CommonUtils.getTimestampString(),
				"{docType}");
		
		long startTime = System.currentTimeMillis();
		int sample = 0;
		long docsCount = 0;
		Random random = new Random();
		SummaryStatistics stats = new SummaryStatistics();
		List<CHData> captureData = new ArrayList<CHData>();
		HashSet<String> markedDocuments = new HashSet<String>();
		Map.Entry<String, TFDF> entry;
		
		try
		{
			try(SizeEstimationExecutionLogFile estimationLog = SizeEstimationExecutionLogFile.newInstance(sizeEstimationFileName);
					SizeEstimationStatsLogFile statsLog = SizeEstimationStatsLogFile.newInstance(sizeEstimationFileName);
					CaptureHistoryDataLogFile chDataLog = CaptureHistoryDataLogFile.newInstance(sizeEstimationFileName);
					QueryDocumentsLogFile queryDocsLog = QueryDocumentsLogFile.newInstance(sizeEstimationFileName)) {
			
				log.info("Starting size estimation proccess with Capture-History algorithm...");
				estimationLog.writeLogMessage("Starting size estimation proccess with Capture-History algorithm...");
				
				String analysisFilesPattern = MessageFormat.format("{0}{1}_{2}.{3}", analysisPath, vertical.getId(), analysisTimestamp, "{docType}");
				Path tfdfFile = CommonUtils.getAnalysisPath().resolve(analysisFilesPattern.replace("{docType}", "tfdf"));
				
				// TF/DF file
				estimationLog.writeLogMessage("Reading TF/DF file ({0})", tfdfFile.toString());
				List<Map.Entry<String, TFDF>> tfdf = new ArrayList<Map.Entry<String,TFDF>>(this.readTFDF(tfdfFile).entrySet());
				int tfdfSize = tfdf.size();
				estimationLog.writeLogMessage("TF/DF file successfully read ({0} terms)", String.valueOf(tfdfSize));
				
				while(sample < 1000) {
					// Selecting random term
					do
					{
						entry = tfdf.remove(random.nextInt(tfdfSize));
						tfdfSize--;
						
						if(entry.getValue().getDocumentFrequency() > 2) {
							estimationLog.writeLogMessage("Randomly selected term: {0} (TF: {1} / DF: {2})", entry.getKey(), String.valueOf(entry.getValue().getTermFrequency()), String.valueOf(entry.getValue().getDocumentFrequency()));
							log.info(MessageFormat.format("Randomly selected term: {0} (TF: {1} / DF: {2})", entry.getKey(), String.valueOf(entry.getValue().getTermFrequency()), String.valueOf(entry.getValue().getDocumentFrequency())));
						} else {
							entry = null;
						}
					}
					while((entry == null) && (!tfdf.isEmpty()));
					
					
					// Submitting query
					if(entry != null) {
						List<QueryResult> queryResult = verticalWrapper.executeQuery(entry.getKey());
						
						sample++;
						int Mi = markedDocuments.size();
						int Ki = queryResult.size();
						int Ri = 0;
						for(QueryResult snippet : queryResult) {
							docsCount++;
							queryDocsLog.writeDocument(entry.getKey(), String.valueOf(docsCount), snippet.getId());
							
							if(!markedDocuments.add(snippet.getId())) {
								Ri++;
							}
						}
						
						// Saves the capture data
						CHData data = new CHData(sample, Ki, Ri, Mi);
						captureData.add(data);
						estimationLog.writeLogMessage("Sample {0}. Ki: {1}, Ri: {2}, Mi: {3}", String.valueOf(sample), String.valueOf(Ki), String.valueOf(Ri), String.valueOf(Mi));
						log.info(MessageFormat.format("Sample {0}. Ki: {1}, Ri: {2}, Mi: {3}", String.valueOf(sample), String.valueOf(Ki), String.valueOf(Ri), String.valueOf(Mi)));
						chDataLog.writeData(data);
						
						
						BigDecimal sampleSizeNum = BigDecimal.ZERO;
						BigDecimal sampleSizeDen = BigDecimal.ZERO;
						for(CHData row : captureData) {
							sampleSizeNum = sampleSizeNum.add(BigDecimal.valueOf(row.getKiMiSquare()));
							sampleSizeDen = sampleSizeDen.add(BigDecimal.valueOf(row.getRiMi()));
						}
						
						if(sampleSizeDen.compareTo(BigDecimal.ZERO) > 0) {
							double N = sampleSizeNum.divide(sampleSizeDen, 4, RoundingMode.HALF_UP).doubleValue();
							stats.addValue(N);
							statsLog.writeStats(stats.getN(), entry.getKey(), N, stats.getStandardDeviation(), stats.getVariance());
							
							estimationLog.writeLogMessage("Estimated size: {0} / Standard Deviation: {1} / Sample Variance: {2}", String.valueOf(N), String.valueOf(stats.getStandardDeviation()), String.valueOf(stats.getVariance()));
							log.info(MessageFormat.format("Estimated size: {0} / Standard Deviation: {1} / Sample Variance: {2}", String.valueOf(N), String.valueOf(stats.getStandardDeviation()), String.valueOf(stats.getVariance())));
						}
						
						
						// Handling delay (if needed)
						if(!(delayHandler instanceof NullDelay)) {
							log.info("Executing timeout");
							
							estimationLog.writeLogMessage("Starting timeout...");
							this.delayHandler.executeDelay();
							estimationLog.writeLogMessage("Timeout finished");
						}
						
					} else {
						estimationLog.writeLogMessage("No more terms to select");
						log.warn("No more available terms to select");
						break;
					}
				}
				
				
				// Logging statistics
				long endTime = System.currentTimeMillis();
				log.info("Size estimation process finished. See log for more details");
				estimationLog.writeLogMessage("Size estimation process finished in {0}", estimationLog.getTimePeriodMessage(startTime, endTime));
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	/**
	 * Data used in the Capture-History algorithm
	 * @author andres
	 *
	 */
	private static class CHData {
		// Sample number
		private int i;
		// Number of documents in the sample i
		private int Ki;
		// Number of documents in the sample i that were already marked
		private int Ri;
		// Number of marked documents gathered so far, prior to the most recent sample.
		private int Mi;
		
		
		public CHData(int i, int ki, int ri, int mi) {
			super();
			this.i = i;
			Ki = ki;
			Ri = ri;
			Mi = mi;
		}


		/**
		 * @return the i
		 */
		public int getI() {
			return i;
		}


		/**
		 * @return the Ki
		 */
		public int getKi() {
			return Ki;
		}


		/**
		 * @return the Ri
		 */
		public int getRi() {
			return Ri;
		}


		/**
		 * @return the Mi
		 */
		public int getMi() {
			return Mi;
		}
		
		/**
		 * Ki * (Mi)^2
		 * @return
		 */
		public double getKiMiSquare(){
			return Ki * Math.pow(Mi, 2);
		}
		
		/**
		 * Ri * Mi
		 * @return
		 */
		public double getRiMi() {
			return Ri * Mi;
		}
	}
	
	
	
	
	private static class CaptureHistoryDataLogFile extends FileWriterHelper {
		
		/**
		 * Default Constructor
		 * @param filePath File Path
		 */
		public CaptureHistoryDataLogFile(Path filePath) {
			super(filePath);
			
			this.open(false);
			this.writeLine("SAMPLE,Ki,Ri,Mi,Ki*(Mi)^2,Ri*Mi");
		}

		
		/**
		 * Creates a new instance of the file in the default path
		 * @param fileName Name of the file
		 * @return Log file
		 */
		public static CaptureHistoryDataLogFile newInstance(String fileName) {
			CaptureHistoryDataLogFile result = null;
			try
			{
				Path analysisPath = CommonUtils.getAnalysisPath();
				result = new CaptureHistoryDataLogFile(analysisPath.resolve(fileName.replace("{docType}", "CHdata")));
			}
			catch(Exception ex) {
				LogFactory.getLog(CaptureHistoryDataLogFile.class).error(ex.getMessage(), ex);
			}
			
			return result;
		}

		
		
		/**
		 * Writes the Capture-History data
		 * @param data Capture-History data
		 */
		public void writeData(CHData data) {
			this.writeLine(MessageFormat.format("{0},{1},{2},{3},{4},{5}",
					String.valueOf(data.getI()),
					String.valueOf(data.getKi()),
					String.valueOf(data.getRi()),
					String.valueOf(data.getMi()),
					String.valueOf(data.getKiMiSquare()),
					String.valueOf(data.getRiMi())));
		}
	}

}
