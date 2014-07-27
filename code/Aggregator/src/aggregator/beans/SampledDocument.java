package aggregator.beans;

import java.io.Serializable;
import java.nio.file.Path;

public interface SampledDocument<T> extends Serializable {
	
	/**
	 * Gets the Document ID
	 * @return Document ID
	 */
	public String getId();
	
	/**
	 * Deserializes the String into the document 
	 * @param document String document
	 * @return Deserialized String
	 */
	public T deserialize(String document);
	
	/**
	 * Serializes the document into a String
	 * @return String of the document
	 */
	public String serialize();
	
	/**
	 * Sets the raw document
	 * @param document Document to set
	 */
	public void setDocument(T document);
	
	/**
	 * Gets the raw document
	 * @return Raw document
	 */
	public T getDocument();
	
	/**
	 * Reads the document from the given path
	 * @param path Document path
	 */
	public void readFromFile(Path path);
	
}
