package aggregator.sampler.analysis.sizeestimation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import aggregator.beans.TFDF;
import aggregator.beans.Vertical;
import aggregator.sampler.analysis.DocumentAnalysis;
import aggregator.sampler.output.SizeEstimationExecutionLogFile;
import aggregator.sampler.output.SizeEstimationStatsLogFile;
import aggregator.util.CommonUtils;

public class SampleResample extends AbstractSizeEstimation {
	
	/**
	 * Default Constructor
	 * @param vertical Sampled Vertical
	 */
	public SampleResample(Vertical vertical) {
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
		Random random = new Random();
		
		try
		{
			try(SizeEstimationExecutionLogFile estimationLog = SizeEstimationExecutionLogFile.newInstance(sizeEstimationFileName);
					SizeEstimationStatsLogFile statsLog = SizeEstimationStatsLogFile.newInstance(sizeEstimationFileName);
					Scanner userInput = new Scanner(System.in)) {
			
				log.info("Starting size estimation proccess with Sample-Resample algorithm...");
				estimationLog.writeLogMessage("Starting size estimation proccess with Sample-Resample algorithm...");
				
				SummaryStatistics stats = new SummaryStatistics();
				
				String analysisFilesPattern = MessageFormat.format("{0}{1}_{2}.{3}", analysisPath, vertical.getId(), analysisTimestamp, "{docType}");
				Path tfdfFile = CommonUtils.getAnalysisPath().resolve(analysisFilesPattern.replace("{docType}", "tfdf"));
				Path docTermsFile = CommonUtils.getAnalysisPath().resolve(analysisFilesPattern.replace("{docType}", "docTerms"));
				
				// TF/DF file
				estimationLog.writeLogMessage("Reading TF/DF file ({0})", tfdfFile.toString());
				List<Map.Entry<String, TFDF>> tfdf = new ArrayList<Map.Entry<String,TFDF>>(DocumentAnalysis.readTFDF(tfdfFile).entrySet());
				int tfdfSize = tfdf.size();
				estimationLog.writeLogMessage("TF/DF file successfully read ({0} terms)", String.valueOf(tfdfSize));
				
				
				// DocTerms file
				estimationLog.writeLogMessage("Reading document terms file ({0})", docTermsFile.toString());
				BigDecimal sampleTotal;
				try(BufferedReader docTermsBR = new BufferedReader(new FileReader(docTermsFile.toFile()))) {
					sampleTotal = BigDecimal.valueOf(Integer.parseInt(docTermsBR.readLine()));
				}
				estimationLog.writeLogMessage("Document terms file successfully read ({0} documents)", String.valueOf(sampleTotal));
				
				
				
				Map.Entry<String, TFDF> entry;
				String submit;
				boolean repeat = true;
				
				do
				{
					if(tfdf.isEmpty()) {
						estimationLog.writeLogMessage("No more available terms to select");
						repeat = false;
						break;
					}
					
					// Selecting term
					do
					{
						entry = tfdf.remove(random.nextInt(tfdfSize));
						tfdfSize--;
						
						estimationLog.writeLogMessage("Randomly selected term: {0} (TF: {1} / DF: {2})", entry.getKey(), String.valueOf(entry.getValue().getTermFrequency()), String.valueOf(entry.getValue().getDocumentFrequency()));
						System.out.print(MessageFormat.format("Submit selected term: {0} (TF: {1} / DF: {2}) (Y/n)? ", entry.getKey(), String.valueOf(entry.getValue().getTermFrequency()), String.valueOf(entry.getValue().getDocumentFrequency())));
						submit = userInput.nextLine();
						if(tfdf.isEmpty()) {
							entry = null;
							break;
						}
					}
					while(!((StringUtils.isBlank(submit)) || (StringUtils.startsWithIgnoreCase(submit, "y"))));
					
					
					if(entry != null) {
						log.info(MessageFormat.format("Submitting query: {0}", entry.getKey()));
						estimationLog.writeLogMessage("Submitting query: {0}", entry.getKey());
						
						BigDecimal Dt = BigDecimal.valueOf(verticalWrapper.getDocumentsByTerm(entry.getKey()));
						
						if(Dt.compareTo(BigDecimal.ZERO) > 0) {
							log.info(MessageFormat.format("Found {0} documents for the term in the collection", Dt));
							estimationLog.writeLogMessage("Found {0} documents for the term in the collection", Dt);
							
							BigDecimal dt = BigDecimal.valueOf(entry.getValue().getDocumentFrequency());
							BigDecimal N = Dt.multiply(dt).divide(sampleTotal, 10, RoundingMode.HALF_UP);
							
							stats.addValue(N.doubleValue());
							
							statsLog.writeStats(stats.getN(), entry.getKey(), stats.getMean(), stats.getStandardDeviation(), stats.getVariance());
							
							log.info(MessageFormat.format("SRS: {0} (of {1} resamples). Standard Deviation: {2} / Variance: {3}", stats.getMean(), String.valueOf(stats.getN()), stats.getStandardDeviation(), stats.getVariance()));
							estimationLog.writeLogMessage("SRS: {0} (of {1} resamples). Standard Deviation: {2} / Variance: {3}", stats.getMean(), String.valueOf(stats.getN()), stats.getStandardDeviation(), stats.getVariance());
						} else {
							log.info("No documents found");
							estimationLog.writeLogMessage("No documents found");
						}
						
						
						
						// Ask if continue resampling
						System.out.print("Continue resampling (Y/n)? ");
						String continueResample = userInput.nextLine();
						repeat = ((StringUtils.isBlank(continueResample)) || (StringUtils.startsWithIgnoreCase(continueResample, "y")));
					}
				}
				while(repeat);
				
				
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
}
