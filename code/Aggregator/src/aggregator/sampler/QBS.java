package aggregator.sampler;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import aggregator.beans.QueryResult;
import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;
import aggregator.sampler.indexer.AbstractSamplerIndexer;
import aggregator.sampler.output.AbstractSamplerOutput;
import aggregator.util.FileWriterHelper;
import aggregator.util.delay.NullDelay;
import aggregator.verticalwrapper.AbstractVerticalWrapper;
import aggregator.verticalwrapper.VerticalWrapperController;

public class QBS extends AbstractSampler {
	
	private static final String TOP_DOCS_KEY = "aggregator.sampler.QBS.topDocs";
	private static final String TOTAL_DOCS_KEY = "aggregator.sampler.QBS.totalDocs";
	
	private int topDocs;
	private int totalDocs;

	/**
	 * Default Constructor
	 */
	public QBS() {
		this.topDocs = Integer.parseInt(System.getProperty(TOP_DOCS_KEY, "4"));
		this.totalDocs = Integer.parseInt(System.getProperty(TOTAL_DOCS_KEY, "250"));
	}
	
	@Override
	public void execute(Vertical vertical) {
		try
		{
			this.reset();
			
			try(FileWriterHelper executionLog = this.createExecutionLog(vertical)) {
				executionLog.open(false);
				executionLog.writeLine(getExecutionLogMessage("Starting sampling with {0} algorithm...", this.getSamplerName()));
				
				int queryCount = 0;
				int docCount = 0;
				long foundTermsCount = 0;
				
				try(AbstractSamplerIndexer samplerIndexer = AbstractSamplerIndexer.newInstance(vertical, this);
						AbstractSamplerOutput samplerOutput = AbstractSamplerOutput.newSamplerOutput(vertical)) {

					AbstractVerticalWrapper verticalWrapper = VerticalWrapperController.getInstance().createVerticalWrapper(vertical);
					QueryStrategy queryStrategy = this.newQueryStrategy(vertical);
					long startTime = System.currentTimeMillis();
					
					do
					{
						String queryTerm = queryStrategy.getNextTerm();
						
						if(StringUtils.isNotBlank(queryTerm)) {
							log.info(MessageFormat.format("Sumbitting query: {0}", queryTerm));
							executionLog.writeLine(getExecutionLogMessage("Sumbitting query: {0}", queryTerm));

							List<QueryResult> queryResultList = verticalWrapper.executeQuery(queryTerm);
							queryCount++;
							
							int queryResultSize = queryResultList.size();
							int topResults = Math.min(queryResultSize, topDocs);
							executionLog.writeLine(getExecutionLogMessage("Identified {0} results... Trying to sample top {1} results", queryResultSize, topResults));
							
							for(QueryResult queryDocument : queryResultList) {
								if(!this.sampledDocuments.contains(queryDocument.getId())) {
									log.info(MessageFormat.format("Downloading document {0}", queryDocument.getId()));
									executionLog.writeLine(getExecutionLogMessage("Downloading document {0}", queryDocument.getId()));
									
									SampledDocument<?> sampledDoc = verticalWrapper.downloadDocument(queryDocument);
									if(sampledDoc != null) {
										docCount++;
										
										samplerOutput.outputDocument(sampledDoc);
										sampledDocuments.add(queryDocument.getId());
										
										samplerIndexer.tokenize(sampledDoc);
										queryStrategy.addTerms(samplerIndexer.getNewTerms());
										topResults--;
										
										int sampleFoundTerms = samplerIndexer.getFoundTerms().size();
										foundTermsCount += sampleFoundTerms;
										
										executionLog.writeLine(getExecutionLogMessage("Document successfully sampled (#{0}). Found {1} terms ({2} new terms)", String.valueOf(docCount), String.valueOf(sampleFoundTerms), String.valueOf(samplerIndexer.getNewTerms().size())));
									} else {
										executionLog.writeLine(getExecutionLogMessage("[ERROR] Document {0} produced no results", queryDocument.getId()));
									}
								} else {
									executionLog.writeLine(getExecutionLogMessage("Ignoring document {0} (already sampled)", queryDocument.getId()));
								}
								
								if(topResults == 0) {
									break;
								}
							}
							
							if(!(delayHandler instanceof NullDelay)) {
								log.info("Executing timeout");
								
								executionLog.writeLine(getExecutionLogMessage("Starting timeout..."));
								this.delayHandler.executeDelay();
								executionLog.writeLine(getExecutionLogMessage("Timeout finished"));
							}
						} else {
							executionLog.writeLine(getExecutionLogMessage("[ERROR] No new term available to continue sampling"));
							break;
						}
					}
					while(sampledDocuments.size() < this.totalDocs);
					
					long endTime = System.currentTimeMillis();
					Period executionTime = new Period(startTime, endTime);
					PeriodFormatter periodFormatter = new PeriodFormatterBuilder()
						.printZeroAlways()
						.appendHours()
						.appendSeparator("h ")
						.appendMinutes()
						.appendSeparator("m ")
						.appendSeconds()
						.appendLiteral("sec")
						.toFormatter();
					
					// Stores the term frequency/document frequency file
					samplerIndexer.storeTF_DF();
					
					log.info("Sampling process finished. See log for more details");
					executionLog.writeLine(getExecutionLogMessage("Sampling process finished in {0}", periodFormatter.print(executionTime)));
					executionLog.writeLine(getExecutionLogMessage("Executed queries: {0}", String.valueOf(queryCount)));
					executionLog.writeLine(getExecutionLogMessage("Documents sampled: {0}", String.valueOf(docCount)));
					executionLog.writeLine(getExecutionLogMessage("Total found terms: {0} ({1} unique terms)", String.valueOf(foundTermsCount), String.valueOf(samplerIndexer.getUniqueTermsCount())));
				}
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	@Override
	public String getSamplerCodeName() {
		return "QBS";
	}

	@Override
	public String getSamplerName() {
		return "Query-Based Sampler";
	}
}
