package aggregator.sampler.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;




import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;






import aggregator.beans.AnalysisConfig;
import aggregator.beans.DOCSampledDocument;
import aggregator.beans.PDFSampledDocument;
import aggregator.beans.SampledDocument;
import aggregator.beans.TFDF;
import aggregator.beans.Vertical;
import aggregator.beans.VerticalCollection;
import aggregator.beans.XMLSampledDocument;
import aggregator.beans.VerticalCollection.VerticalCollectionData;
import aggregator.sampler.indexer.AbstractSamplerIndexer;
import aggregator.sampler.output.AnalysisExecutionLogFile;
import aggregator.sampler.output.DocumentAnalysisLogFile;
import aggregator.sampler.output.TFDFLogFile;
import aggregator.sampler.output.TokensLogFile;
import aggregator.util.CommonUtils;
import aggregator.util.XMLUtils;
import aggregator.verticalwrapper.VerticalWrapperController;

public class DocumentAnalysis {
	
	private Vertical vertical;
	private AnalysisConfig analysisConfig;
	private XMLUtils xmlUtils;
	private Log log = LogFactory.getLog(DocumentAnalysis.class);
	
	
	/**
	 * Default Constructor
	 * @param vertical Sampled Vertical
	 */
	public DocumentAnalysis(Vertical vertical) {
		this.vertical = vertical;
		this.xmlUtils = new XMLUtils();
		this.analysisConfig = VerticalWrapperController.getInstance().getVerticalConfig(vertical).getAnalysisConfig();
	}
	

	/**
	 * Analyzes an XML Sample
	 * @param samplePath Sample Path
	 */
	public void analyzeHTMLSample(String samplePath) {
		samplePath = StringUtils.appendIfMissing(samplePath, File.separator);
		
		String analysisFileName = MessageFormat.format("{0}{1}_{2}.{3}",
				samplePath,
				vertical.getId(),
				CommonUtils.getTimestampString(),
				"{docType}");
		
		long startTime = System.currentTimeMillis();
		
		try(AbstractSamplerIndexer samplerIndexer = AbstractSamplerIndexer.newInstance(vertical);
				AnalysisExecutionLogFile executionLog = AnalysisExecutionLogFile.newInstance(analysisFileName);
				IntermediateResults results = new DatabaseIntermediateResults(vertical)) {
//				IntermediateResults results = new MemoryIntermediateResults()) {
		
//			samplerIndexer.setIntermediateResults(results);
			
			log.info("Starting analysis process...");
			executionLog.writeLogMessage("Starting analysis process...");
			
			
			
			
//			List<Map.Entry<String, List<String>>> documentTerms = new ArrayList<Map.Entry<String,List<String>>>();
			
			try(TokensLogFile tokens = TokensLogFile.newInstance(analysisFileName);
					DocumentAnalysisLogFile docAnalysisLogFile = DocumentAnalysisLogFile.newInstance(analysisFileName)) {
				
				Files.walk(Paths.get(CommonUtils.getSamplePath().toString(), samplePath)).forEach(filePath -> {
				    if (Files.isRegularFile(filePath)) {
				    	SampledDocument<?> doc = null;
			    		
				    	if(filePath.getFileName().toString().endsWith(".html")) {
					    	doc = new XMLSampledDocument(filePath);
				    	} else if(filePath.getFileName().toString().endsWith(".pdf")) {
				    		doc = new PDFSampledDocument(filePath);
				    	} else if(filePath.getFileName().toString().endsWith(".doc")) {
				    		doc = new DOCSampledDocument(filePath);
				    	}
				    	
				    	if((doc != null) && (doc.getDocument() != null)) {
				    		log.info(MessageFormat.format("Analyzing {0}", filePath));
				    		executionLog.writeLogMessage("Analyzing {0}", filePath);
				    		
				    		List<String> foundTerms = samplerIndexer.tokenize(doc);
				    		int foundTermsCount = foundTerms.size();
				    		int newTermsCount = samplerIndexer.getNewTerms().size();
				    		
				    		results.addDocumentTerms(doc.getId(), foundTerms);
//				    		documentTerms.add(new ImmutablePair<String, List<String>>(doc.getId(), new ArrayList<String>(foundTerms)));
				    		log.info(MessageFormat.format("Found {0} terms.", foundTerms.size()));
				    		tokens.writeTokens(foundTerms);
				    		
				    		String docTitle = xmlUtils.executeXPath(doc.getDocument(), analysisConfig.getDocumentTitle(), String.class);
				    		String docId = xmlUtils.executeXPath(doc.getDocument(), analysisConfig.getDocumentId(), String.class);
				    		
				    		docAnalysisLogFile.writeDocument(filePath.getFileName().toString(), docId, docTitle, foundTermsCount, newTermsCount);
				    		
				    		executionLog.writeLogMessage("Document {0} ({1}) successfully sampled. Found {2} terms ({3} new terms)", filePath.getFileName().toString(), docId, foundTermsCount, newTermsCount);
				    	}
				    }
				});
			}
			
			// Stores the term frequency/document frequency file
			try(TFDFLogFile tfdfLog = TFDFLogFile.newInstance(analysisFileName)) {
				tfdfLog.writeTFDFs(samplerIndexer.getUniqueTerms());
			}
			
//			// Stores the document terms list
//			try(DocumentTermsLogFile docTermsLog = DocumentTermsLogFile.newInstance(analysisFileName)) {
//				docTermsLog.writeDocumentTerms(documentTerms);
//			}
			results.dumpDocumentTerms(analysisFileName);
//			results.dumpTFDF(analysisFileName);
			
			// Logging statistics
			long endTime = System.currentTimeMillis();
			log.info("Analysis process finished. See log for more details");
			executionLog.writeLogMessage("Analysis process finished in {0}", executionLog.getTimePeriodMessage(startTime, endTime));
						
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	/**
	 * Computes the term frequencies/document frequencies of an analyzed collection
	 * @param collection Vertical collection
	 * @param analysisPath Analysis Path
	 */
	public static void computeTFDF(VerticalCollection collection, String analysisPath) {
		try
		{
			Path collectionPath = CommonUtils.getAnalysisPath().resolve(analysisPath);
			for(VerticalCollectionData data : collection.getVerticals()) {
				Path verticalPath = collectionPath.resolve(data.getVerticalCollectionId());
				if(Files.exists(verticalPath)) {
					Files.walk(verticalPath, 1).forEach(filePath -> {
						if (Files.isRegularFile(filePath)) {
							if(filePath.getFileName().toString().endsWith(".docTerms")) {
								String tfdfName = collectionPath.getFileName().toString() +
										File.separator +
										data.getVerticalCollectionId() +
										File.separator +
										filePath.getFileName().toString().replace(".docTerms", ".{docType}");
								
								LogFactory.getLog(DocumentAnalysis.class).info(MessageFormat.format("Reading {0} terms file...", filePath.toString()));
								
								try(BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
									Map<String, TFDF> uniqueTerms = new HashMap<String, TFDF>();
									
									String line = reader.readLine();
									while((line = reader.readLine()) != null) {
										HashMap<String, Integer> termFrequency = new HashMap<String, Integer>();
										for(String term : line.split("\\s+")) {
											Integer tf = termFrequency.get(term);
								    		if(tf == null) {
								    			termFrequency.put(term, 1);
								    		} else {
								    			termFrequency.put(term, tf+1);
								    		}
										}
										
										
										// Calculates the global term frequency/document frequency
										for(Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
											if(!uniqueTerms.containsKey(entry.getKey())) {
												uniqueTerms.put(entry.getKey(), new TFDF(entry.getValue(), 1));
											} else {
												TFDF tfdf = uniqueTerms.get(entry.getKey());
												tfdf.incrementTermFrequency(entry.getValue());
												tfdf.incrementDocumentFrequency(1);
											}
										}
									}
									

									
									
									// Stores the term frequency/document frequency file
									try(TFDFLogFile tfdfLog = TFDFLogFile.newInstance(tfdfName)) {
										tfdfLog.writeTFDFs(uniqueTerms.entrySet());
									}
								}
								catch(Exception ex) {
									LogFactory.getLog(DocumentAnalysis.class).error(ex.getMessage(), ex);
								}
								
							}
						}
					});
					
					
				}
			}
		}
		catch(Exception ex) {
			LogFactory.getLog(DocumentAnalysis.class).error(ex.getMessage(), ex);
		}
	}
	
}
