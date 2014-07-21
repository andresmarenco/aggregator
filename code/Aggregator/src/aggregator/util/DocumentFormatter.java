package aggregator.util;

public interface DocumentFormatter<T> {
	/**
	 * Converts the document into the output format
	 * @param document Document
	 * @return Output string
	 */
	public String toString(T document);
	
	/**
	 * Converts the string into the document object
	 * @param input Document string
	 * @return Document object
	 */
	public T toObject(String input);
	
	/**
	 * Gets the File Header
	 * @return File Header
	 */
	public String getFileHeader();
	
	/**
	 * Gets the File Footer
	 * @return File Footer
	 */
	public String getFileFooter();
}
