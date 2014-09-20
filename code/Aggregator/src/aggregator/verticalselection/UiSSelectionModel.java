package aggregator.verticalselection;

import java.math.BigDecimal;

import aggregator.beans.VerticalCollection;
import aggregator.beans.VerticalCollection.VerticalCollectionData;
import aggregator.util.CommonUtils;

/**
 * Base to implement the selection model as defined by Balog, K. in
 * "Collection and Document Language Models for Resource Selection"
 * (http://trec.nist.gov/pubs/trec22/papers/uis-federated.pdf)
 */
public abstract class UiSSelectionModel extends AbstractSelectionModel {
	
//	private BigDecimal collectionSize;

	/**
	 * Default Constructor
	 * @param collection Vertical Collection
	 */
	public UiSSelectionModel(VerticalCollection collection) {
		super(collection);
		
	}
	
	
	
	
	/**
	 * Gets the collection (vertical) prior value
	 * @param vertical Vertical to compute the value
	 * @return Collection (vertical) prior value
	 */
	protected double getCollectionPrior(VerticalCollectionData vertical) {

		final BigDecimal collectionSize = (ireader != null) ? BigDecimal.valueOf(ireader.numDocs()) : BigDecimal.ZERO;
		
		BigDecimal sampleSize = BigDecimal.valueOf(vertical.getSampleSize());
//		System.out.println("SAMPLE " + sampleSize + "   COLLECTION: " + collectionSize);
		
		return sampleSize.divide(collectionSize.subtract(sampleSize), CommonUtils.DEFAULT_DIVISION_SCALE, CommonUtils.DEFAULT_ROUNDING_MODE).doubleValue();
	}
	
	
	
	
	/**
	 * Gets the P(d|c)
	 * @param vertical Vertical to compute the value
	 * @return P(d|c) value
	 */
	protected double getPdc(VerticalCollectionData vertical) {
		BigDecimal sampleSize = BigDecimal.valueOf(vertical.getSampleSize());
		return BigDecimal.ONE.divide(sampleSize, CommonUtils.DEFAULT_DIVISION_SCALE, CommonUtils.DEFAULT_ROUNDING_MODE).doubleValue();
	}

}
