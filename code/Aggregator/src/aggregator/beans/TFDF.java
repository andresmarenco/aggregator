package aggregator.beans;

import java.io.Serializable;

/**
 * Term Frequency/Document Frequency Bean
 * @author andres
 *
 */
public class TFDF implements Serializable {

	private static final long serialVersionUID = 201407310626L;
	private long termFrequency;
	private long documentFrequency;
	
	/**
	 * Default Constructor
	 */
	public TFDF() {
	}
	
	
	/**
	 * Constructor with values
	 * @param termFrequency
	 * @param documentFrequency
	 */
	public TFDF(long termFrequency, long documentFrequency) {
		this.termFrequency = termFrequency;
		this.documentFrequency = documentFrequency;
	}



	/**
	 * @return the termFrequency
	 */
	public long getTermFrequency() {
		return termFrequency;
	}
	
	/**
	 * Increments the current term frequency
	 * @param value Value to increment
	 */
	public void incrementTermFrequency(long value) {
		this.termFrequency += value;
	}

	/**
	 * @return the documentFrequency
	 */
	public long getDocumentFrequency() {
		return documentFrequency;
	}
	
	/**
	 * Increments the current document frequency
	 * @param value Value to increment
	 */
	public void incrementDocumentFrequency(long value) {
		this.documentFrequency += value;
	}

}
