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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;







import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
import aggregator.util.IterableNodeList;
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
	 * Analyzes a snippet sample
	 * @param samplePath Sample Path
	 */
	public void analyzeSnippetSample(String samplePath) {
		samplePath = StringUtils.appendIfMissing(samplePath, File.separator);
		
		String analysisFileName = MessageFormat.format("{0}{1}_{2}.{3}",
				samplePath,
				vertical.getId(),
				CommonUtils.getTimestampString(),
				"{docType}");
		
		long startTime = System.currentTimeMillis();
		
		try(AbstractSamplerIndexer samplerIndexer = AbstractSamplerIndexer.newInstance(vertical);
				AnalysisExecutionLogFile executionLog = AnalysisExecutionLogFile.newInstance(analysisFileName);
				IntermediateResults results = new MemoryIntermediateResults()) {
			
			log.info("Starting analysis process...");
			executionLog.writeLogMessage("Starting analysis process...");

			try(DocumentAnalysisLogFile docAnalysisLogFile = DocumentAnalysisLogFile.newInstance(analysisFileName)) {
				Path snippetsPath = CommonUtils.getSamplePath().resolve(samplePath).resolve(Paths.get(samplePath).getFileName().toString() + ".xml");
				log.info(MessageFormat.format("Reading snippets file {0}", snippetsPath.getFileName().toString()));
				executionLog.writeLogMessage("Reading snippets file {0}", snippetsPath.getFileName().toString());
			
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				org.w3c.dom.Document xmlDoc = dBuilder.parse(snippetsPath.toFile());
				
				NodeList snippetNodes = xmlUtils.executeXPath(xmlDoc, "./samples/search_results/snippets/snippet", NodeList.class);
				for(Node snippet : new IterableNodeList(snippetNodes)) {
					String titleNode = null, idNode = null, descriptionNode = null, linkNode = null;
					idNode = snippet.getAttributes().getNamedItem("id").getTextContent();
						
					for(Node snippetChild : new IterableNodeList(snippet.getChildNodes())) {
						if(snippetChild.getNodeName().equals("link")) {
							Node cacheAttribute = snippetChild.getAttributes().getNamedItem("cache");
							if(cacheAttribute != null) {
								linkNode = snippetChild.getAttributes().getNamedItem("cache").getTextContent();
								linkNode = Paths.get(linkNode).getFileName().toString();
							} else {
								linkNode = idNode;
							}
						} else if(snippetChild.getNodeName().equals("title")) {
							titleNode = snippetChild.getTextContent().replaceAll("\\s+", " ");
						} else if(snippetChild.getNodeName().equals("description")) {
							descriptionNode = snippetChild.getTextContent().replaceAll("\\s+", " ");
						}
					}
				
					log.info(MessageFormat.format("Analyzing {0}", idNode));
					executionLog.writeLogMessage("Analyzing {0}", idNode);
				
//					if((StringUtils.isNotBlank(titleNode)) || (StringUtils.isNotBlank(descriptionNode))) {
					List<String> foundTerms = samplerIndexer.tokenize(linkNode, titleNode, descriptionNode);
		    		int foundTermsCount = foundTerms.size();
		    		int newTermsCount = samplerIndexer.getNewTerms().size();
		    		
		    		
		    		
		    		if(foundTermsCount > 0) {
			    		results.addDocumentTerms(linkNode, foundTerms);
			    		docAnalysisLogFile.writeDocument(linkNode, idNode, titleNode, foundTermsCount, newTermsCount);
			    		
			    		log.info(MessageFormat.format("Found {0} terms.", foundTerms.size()));
			    		executionLog.writeLogMessage("Snippet {0} ({1}) successfully sampled. Found {2} terms ({3} new terms)", linkNode, idNode, foundTermsCount, newTermsCount);
		    		} else {
		    			log.info("No found terms.");
						executionLog.writeLogMessage("No found terms for snippet {0} ({1}).", linkNode, idNode);
		    		}
				    		
//					} else {
//						
//					}
		    		
				}
				
			}
			
			// Stores the term frequency/document frequency file
			try(TFDFLogFile tfdfLog = TFDFLogFile.newInstance(analysisFileName)) {
				tfdfLog.writeTFDFs(samplerIndexer.getUniqueTerms());
			}
			
			// Stores the document terms list
			results.dumpDocumentTerms(analysisFileName);
			
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
								Path docsPath = filePath.resolveSibling(filePath.getFileName().toString().replace(".docTerms", ".docs"));
								
								try(BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()));
										BufferedReader docsReader = new BufferedReader(new FileReader(docsPath.toFile()))) {
									Map<String, TFDF> uniqueTerms = new HashMap<String, TFDF>();
									
									String line = reader.readLine();
									String docsline = docsReader.readLine();
									while((line = reader.readLine()) != null) {
										docsline = docsReader.readLine();
										String docId = docsline.substring(0, docsline.indexOf(','));
										
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
												uniqueTerms.put(entry.getKey(), new TFDF(entry.getValue(), docId));
											} else {
												TFDF tfdf = uniqueTerms.get(entry.getKey());
												tfdf.incrementTermFrequency(entry.getValue());
												tfdf.addDocument(docId);
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
	
	
	
	

	

	
	/**
	 * Reads the term frequency/document frequency file
	 * @param tfdfFile Term frequency/document frequency file
	 * @return Term frequency/document frequency
	 */
	public static HashMap<String, TFDF> readTFDF(Path tfdfFile) {
		HashMap<String, TFDF> result = new HashMap<String, TFDF>();
		try
		{
			List<String> tfdfLines = Files.readAllLines(tfdfFile);
			tfdfLines.remove(0);
			
			for(String line : tfdfLines) {
				if(StringUtils.isNotBlank(line)) {
					int lastIndex = line.lastIndexOf(',');
					String[] docs = line.substring(lastIndex+1).split("\\|");
					line = line.substring(0, lastIndex);
					
					lastIndex = line.lastIndexOf(',');
					line = line.substring(0, lastIndex);
					
					lastIndex = line.lastIndexOf(',');
					int tf = Integer.parseInt(line.substring(lastIndex+1));
					line = line.substring(0, lastIndex);
					
					result.put(line, new TFDF(tf, docs));
				}
			}
		}
		catch(Exception ex) {
			LogFactory.getLog(DocumentAnalysis.class).error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
}
