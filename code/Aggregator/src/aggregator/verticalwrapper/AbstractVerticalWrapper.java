package aggregator.verticalwrapper;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.QueryResult;
import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;
import aggregator.beans.VerticalConfig;

public abstract class AbstractVerticalWrapper {
	
	protected Vertical vertical;
	protected VerticalConfig config;
	protected Log log = LogFactory.getLog(getClass());
	
	/**
	 * Default Constructor
	 * @param vertical Vertical Object
	 * @param config Vertical Configuration
	 */
	public AbstractVerticalWrapper(Vertical vertical, VerticalConfig config) {
		this.vertical = vertical;
		this.config = config;
	}
	
	/**
	 * Executes the required commands to authenticate into the vertical
	 * @return Authentication result
	 */
	public abstract Object authenticate();
	
	/**
	 * Submits the given query to the vertical
	 * @param query User's query
	 * @return List of results from the query
	 */
	public abstract List<QueryResult> executeQuery(String query);
	
	/**
	 * Downloads the complete document
	 * @param document Document summary
	 * @return Complete sampled document
	 */
	public abstract SampledDocument<?> downloadDocument(QueryResult document);
}
