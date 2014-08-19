package aggregator.sampler;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import aggregator.beans.QueryResult;
import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;
import aggregator.sampler.indexer.AbstractSamplerIndexer;
import aggregator.sampler.output.DocumentTermsLogFile;
import aggregator.sampler.output.QueryDocumentsLogFile;
import aggregator.sampler.output.ExecutionLogFile;
import aggregator.sampler.output.QueriesLogFile;
import aggregator.sampler.output.SampledDocumentOutput;
import aggregator.sampler.output.TFDFLogFile;
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
	 * @param vertical Vertical to sample
	 */
	public QBS(Vertical vertical) {
		super(vertical);
		this.topDocs = Integer.parseInt(System.getProperty(TOP_DOCS_KEY, "10"));
		this.totalDocs = Integer.parseInt(System.getProperty(TOTAL_DOCS_KEY, "250"));
	}
	
	
	
	@Override
	public void execute() {
		try
		{
			this.reset();
			long startTime = System.currentTimeMillis();
			
			// Output Files
			String logFileName = this.getLogFilePattern(startTime);
			String analysisFileName = this.getAnalysisFilePattern(startTime);
			String sampleFileName = this.getSampleFilePattern(startTime);
			
			// Counters
			int queryCount = 0;
			int docCount = 0;
			long foundTermsCount = 0;
			
			try(ExecutionLogFile executionLog = ExecutionLogFile.newInstance(logFileName);
					QueriesLogFile queriesLog = QueriesLogFile.newInstance(analysisFileName);
					QueryDocumentsLogFile docsLog = QueryDocumentsLogFile.newInstance(analysisFileName)) {
				
				log.info("Starting sampling proccess...");
				executionLog.writeLogMessage("Starting sampling with {0} algorithm...", this.getSamplerName());
				
				try(AbstractSamplerIndexer samplerIndexer = AbstractSamplerIndexer.newInstance(vertical)) {
					AbstractVerticalWrapper verticalWrapper = VerticalWrapperController.getInstance().createVerticalWrapper(vertical);
					
					SampledDocumentOutput samplerOutput = new SampledDocumentOutput();
					
					do
					{
						String query = queryStrategy.getNextTerm();
						
						if(StringUtils.isNotBlank(query)) {
							queryCount++;
							
							log.info(MessageFormat.format("Sumbitting query: {0}", query));
							executionLog.writeLogMessage("Sumbitting query: {0}", query);

							List<QueryResult> queryResultList = verticalWrapper.executeQuery(query);
							
							final int resultCountPadding = String.valueOf(this.topDocs).length();
							String queryIdFormat = StringUtils.leftPad(String.valueOf(queryCount), 6, '0');
							
							int queryResultSize = queryResultList.size();
							int topResults = Math.min(queryResultSize, topDocs);
							int relativePosition = 1;
							
							executionLog.writeLogMessage("Identified {0} results... Trying to sample top {1} results", queryResultSize, topResults);
							queriesLog.writeQueryTerm(queryIdFormat, query, queryResultSize);
							
							for(QueryResult queryDocument : queryResultList) {
								if(!this.sampledDocuments.contains(queryDocument.getId())) {
									log.info(MessageFormat.format("Downloading document {0}", queryDocument.getId()));
									executionLog.writeLogMessage("Downloading document {0}", queryDocument.getId());
									
									SampledDocument<?> sampledDoc = verticalWrapper.downloadDocument(queryDocument);
									if(sampledDoc != null) {
										docCount++;

										sampledDocuments.add(queryDocument.getId());
										
										String resultCountFormat = StringUtils.leftPad(String.valueOf(relativePosition), resultCountPadding, '0');
										
										docsLog.writeDocument(queryIdFormat, resultCountFormat, queryDocument.getId());
										String docFileId = MessageFormat.format("{0}_{1}.html", queryIdFormat, resultCountFormat);
										samplerOutput.outputDocument(sampledDoc, sampleFileName.replace("{queryId}", queryIdFormat).replace("{resultCount}", resultCountFormat));
										
										List<String> foundTerms = samplerIndexer.tokenize(sampledDoc);
										documentTerms.add(new ImmutablePair<String, List<String>>(docFileId, new ArrayList<String>(foundTerms)));
										queryStrategy.addTerms(samplerIndexer.getNewTerms());
										topResults--;
										
										int sampleFoundTerms = foundTerms.size();
										foundTermsCount += sampleFoundTerms;
										
										executionLog.writeLogMessage("Document successfully sampled (#{0}: {1}). Found {2} terms ({3} new terms)", String.valueOf(docCount), docFileId, String.valueOf(sampleFoundTerms), String.valueOf(samplerIndexer.getNewTerms().size()));
									} else {
										executionLog.writeLogMessage("[ERROR] Document {0} produced no results", queryDocument.getId());
									}
								} else {
									executionLog.writeLogMessage("Ignoring document {0} (already sampled)", queryDocument.getId());
								}
								
								if((topResults == 0) || (docCount >= this.totalDocs)) {
									break;
								}
								
								relativePosition++;
							}
							
							// Handling delay (if needed)
							if(!(delayHandler instanceof NullDelay)) {
								log.info("Executing timeout");
								
								executionLog.writeLogMessage("Starting timeout...");
								this.delayHandler.executeDelay();
								executionLog.writeLogMessage("Timeout finished");
							}
						} else {
							executionLog.writeLogMessage("[ERROR] No new term available to continue sampling");
							break;
						}
					}
					while(docCount < this.totalDocs);
					
					// Stores the term frequency/document frequency file
					try(TFDFLogFile tfdfLog = TFDFLogFile.newInstance(analysisFileName)) {
						tfdfLog.writeTFDFs(samplerIndexer.getUniqueTerms());
					}
					
					// Stores the document terms list
					try(DocumentTermsLogFile docTermsLog = DocumentTermsLogFile.newInstance(analysisFileName)) {
						docTermsLog.writeDocumentTerms(documentTerms);
					}

					// Logging statistics
					long endTime = System.currentTimeMillis();
					log.info("Sampling process finished. See log for more details");
					executionLog.writeLogMessage("Sampling process finished in {0}", executionLog.getTimePeriodMessage(startTime, endTime));
					
					executionLog.writeLogMessage("Executed queries: {0}", String.valueOf(queryCount));
					executionLog.writeLogMessage("Documents sampled: {0}", String.valueOf(docCount));
					executionLog.writeLogMessage("Total found terms: {0} ({1} unique terms)", String.valueOf(foundTermsCount), String.valueOf(samplerIndexer.getUniqueTermsCount()));
				}
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	@Override
	public String getSamplerName() {
		return "Query-Based Sampler";
	}

	@Override
	public String getSamplerCodeName() {
		return "QBS";
	}
	
	@Override
	public String getSamplerExecutionCodeName() {
		return MessageFormat.format("{0}_{1}R_{2}T_{3}",
				this.getSamplerCodeName(),
				String.valueOf(this.topDocs),
				String.valueOf(this.totalDocs),
				queryStrategy.getCodeName());
	}
}
