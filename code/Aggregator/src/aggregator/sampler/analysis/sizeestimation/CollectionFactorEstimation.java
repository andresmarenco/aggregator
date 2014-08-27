package aggregator.sampler.analysis.sizeestimation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;





import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;





import aggregator.beans.VerticalCollection;
import aggregator.beans.VerticalCollection.VerticalCollectionData;
import aggregator.dataaccess.DirectConnectionManager;
import aggregator.dataaccess.VerticalDAO;
import aggregator.util.CommonUtils;

public class CollectionFactorEstimation {
	
	private VerticalCollection collection;
	private Log log = LogFactory.getLog(CollectionFactorEstimation.class);
	
	/**
	 * Default Constructor
	 * @param collection Vertical Collection
	 */
	public CollectionFactorEstimation(VerticalCollection collection) {
		this.collection = collection;
	}
	
	
	public void estimateSize(String analysisPath) {
		try
		{
			VerticalDAO verticalDAO = new VerticalDAO(DirectConnectionManager.getInstance());
			
			for(VerticalCollectionData vertical : collection.getVerticals()) {
				
				
				Files.walk(CommonUtils.getAnalysisPath().resolve(analysisPath).resolve(vertical.getVerticalCollectionId()), 1).forEach(filePath -> {
					if (Files.isRegularFile(filePath)) {
						if(filePath.getFileName().toString().endsWith(".docs")) {
							
							try(BufferedReader docsReader = new BufferedReader(new FileReader(filePath.toFile()))) {
								
								// Get rid of the header line
								String docLine = docsReader.readLine();
								String totalTerms, uniqueTerms;
								long total = 0, unique = 0, docs = 0;
								
								while((docLine = docsReader.readLine()) != null) {
									totalTerms = docLine.substring(0, docLine.lastIndexOf(","));
									totalTerms = totalTerms.substring(totalTerms.lastIndexOf(",")+1);
									uniqueTerms = docLine.substring(docLine.lastIndexOf(',')+1);
									
									unique += Long.parseLong(uniqueTerms);
									total += Long.parseLong(totalTerms);
									docs++;
								}
								
								BigDecimal factor = BigDecimal.valueOf(unique)
										.divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_EVEN)
										.multiply(BigDecimal.valueOf(docs));
								
								double size = Math.ceil(factor.doubleValue() * docs) + docs;
								
								
								System.out.println(vertical.getVertical().getId() + " " + docs + "  " + unique + "  " + total + "  " + Math.log10(factor.doubleValue()) + "  " + size);
								verticalDAO.updateSizeFactor(collection.getId(), vertical.getVertical().getId(), factor.doubleValue());
								
								
								
//								System.out.println()

//								System.exit(0);
							}
							catch(Exception ex) {
								log.error(ex.getMessage(), ex);
							}
						}
					}
				});
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
}
