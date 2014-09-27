package aggregator.beans;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Term Frequency/Document Frequency Bean
 * @author andres
 *
 */
public class TFDF implements Serializable {

	private static final long serialVersionUID = 201407310626L;
	private long termFrequency;
//	private long documentFrequency;
	private Set<String> documents;
	
	/**
	 * Default Constructor
	 */
	public TFDF() {
		this.documents = new HashSet<String>();
	}
	
	
	/**
	 * Constructor with values
	 * @param termFrequency
	 * @param document
	 */
	public TFDF(long termFrequency, String... document) {
		this();
		
		this.termFrequency = termFrequency;
		this.documents.addAll(Arrays.asList(document));
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
		return documents.size();
	}
	
//	/**
//	 * Increments the current document frequency
//	 * @param value Value to increment
//	 */
//	public void incrementDocumentFrequency(long value) {
//		this.documentFrequency += value;
//	}

	/**
	 * Adds a document to the list
	 * @param document Document to add
	 */
	public void addDocument(String document) {
		this.documents.add(document);
	}


	/**
	 * @return the documents
	 */
	public Set<String> getDocuments() {
		return documents;
	}
	
	
}
