package aggregator.beans;

import java.io.Serializable;
import java.util.Properties;

public class AnalysisConfig implements Serializable {

	private static final long serialVersionUID = 201408170443L;
	private static final String ANALYSIS_DOCUMENT_ID_KEY = "analysis.docId";
	private static final String ANALYSIS_DOCUMENT_TITLE_KEY = "analysis.docTitle";
	private static final String ANALYSIS_TOTAL_RECORDS_KEY = "analysis.totalRecords";
	
	private String documentId;
	private String documentTitle;
	private String totalRecords;
	
	
	/**
	 * Default Constructor
	 */
	public AnalysisConfig() {
	}
	
	
	
	/**
	 * Creates a new instance of {@code AnalysisConfig} based on the properties file
	 * @param properties Properties file
	 * @return {@code AnalysisConfig} instance
	 */
	public static final AnalysisConfig newInstance(Properties properties) {
		AnalysisConfig config = new AnalysisConfig();
		config.setDocumentId(properties.getProperty(ANALYSIS_DOCUMENT_ID_KEY));
		config.setDocumentTitle(properties.getProperty(ANALYSIS_DOCUMENT_TITLE_KEY));
		config.setTotalRecords(properties.getProperty(ANALYSIS_TOTAL_RECORDS_KEY));
		
		return config;
	}


	/**
	 * @return the documentId
	 */
	public String getDocumentId() {
		return documentId;
	}


	/**
	 * @param documentId the documentId to set
	 */
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}


	/**
	 * @return the documentTitle
	 */
	public String getDocumentTitle() {
		return documentTitle;
	}


	/**
	 * @param documentTitle the documentTitle to set
	 */
	public void setDocumentTitle(String documentTitle) {
		this.documentTitle = documentTitle;
	}


	/**
	 * @return the totalRecords
	 */
	public String getTotalRecords() {
		return totalRecords;
	}


	/**
	 * @param totalRecords the totalRecords to set
	 */
	public void setTotalRecords(String totalRecords) {
		this.totalRecords = totalRecords;
	}

}
