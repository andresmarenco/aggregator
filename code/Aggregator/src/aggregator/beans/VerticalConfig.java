package aggregator.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import aggregator.util.CommonUtils;

public class VerticalConfig implements Serializable {

	private static final long serialVersionUID = 201407040244L;
	private static final String WRAPPER_TYPE_KEY = "wrapperType";
	private static final String AUTH_SCRIPT_KEY = "authScript";
	private static final String AUTH_SCRIPT_FILE_KEY = "authScript.readAsFile";
	private static final String QUERY_SCRIPT_KEY = "queryScript";
	private static final String QUERY_SCRIPT_FILE_KEY = "queryScript.readAsFile";
	private static final String SRR_SCRIPT_KEY = "srrScript";
	private static final String SRR_SCRIPT_FILE_KEY = "srrScript.readAsFile";
	private static final String ID_PATTERN_KEY = "idPattern";
	
	private String wrapperType;
	private List<String> authScript;
	private List<String> queryScript;
	/** Search Result Records Extraction */
	private List<String> srrScript;
	private String idPattern;
	private HTTPVerticalConfig httpConfig;
	
	/**
	 * Default Constructor
	 */
	public VerticalConfig() {
	}

	/**
	 * Creates a new instance of {@code VerticalConfig} based on the properties file
	 * @param properties Properties file
	 * @return {@code VerticalConfig} instance
	 */
	public static final VerticalConfig newInstance(Properties properties) {
		VerticalConfig config = new VerticalConfig();
		config.setWrapperType(properties.getProperty(WRAPPER_TYPE_KEY));
		
		if(Boolean.parseBoolean(properties.getProperty(AUTH_SCRIPT_FILE_KEY))) {
			config.setAuthScript(CommonUtils.readScriptFile(properties.getProperty(AUTH_SCRIPT_KEY)));
		} else {
			config.setAuthScript(new ArrayList<String>());
			config.getAuthScript().add(properties.getProperty(AUTH_SCRIPT_KEY));
		}
		
		if(Boolean.parseBoolean(properties.getProperty(QUERY_SCRIPT_FILE_KEY))) {
			config.setQueryScript(CommonUtils.readScriptFile(properties.getProperty(QUERY_SCRIPT_KEY)));
		} else {
			config.setQueryScript(new ArrayList<String>());
			config.getQueryScript().add(properties.getProperty(QUERY_SCRIPT_KEY));
		}
		
		if(Boolean.parseBoolean(properties.getProperty(SRR_SCRIPT_FILE_KEY))) {
			config.setSRRScript(CommonUtils.readScriptFile(properties.getProperty(SRR_SCRIPT_KEY)));
		} else {
			config.setSRRScript(new ArrayList<String>());
			config.getSRRScript().add(properties.getProperty(SRR_SCRIPT_KEY));
		}
		
		config.setIdPattern(properties.getProperty(ID_PATTERN_KEY));
		config.setHttpConfig(HTTPVerticalConfig.newInstance(properties));
		
		return config;
	}

	/**
	 * @return the wrapperType
	 */
	public String getWrapperType() {
		return wrapperType;
	}

	/**
	 * @param wrapperType the wrapperType to set
	 */
	public void setWrapperType(String wrapperType) {
		this.wrapperType = wrapperType;
	}

	/**
	 * @return the authScript
	 */
	public List<String> getAuthScript() {
		return authScript;
	}

	/**
	 * @param authScript the authScript to set
	 */
	public void setAuthScript(List<String> authScript) {
		this.authScript = authScript;
	}

	/**
	 * @return the queryScript
	 */
	public List<String> getQueryScript() {
		return queryScript;
	}

	/**
	 * @param queryScript the queryScript to set
	 */
	public void setQueryScript(List<String> queryScript) {
		this.queryScript = queryScript;
	}

	/**
	 * @return the srrScript
	 */
	public List<String> getSRRScript() {
		return srrScript;
	}

	/**
	 * @param srrScript the srrScript to set
	 */
	public void setSRRScript(List<String> srrScript) {
		this.srrScript = srrScript;
	}

	/**
	 * @return the idPattern
	 */
	public String getIdPattern() {
		return idPattern;
	}

	/**
	 * @param idPattern the idPattern to set
	 */
	public void setIdPattern(String idPattern) {
		this.idPattern = idPattern;
	}

	/**
	 * @return the httpConfig
	 */
	public HTTPVerticalConfig getHttpConfig() {
		return httpConfig;
	}

	/**
	 * @param httpConfig the httpConfig to set
	 */
	public void setHttpConfig(HTTPVerticalConfig httpConfig) {
		this.httpConfig = httpConfig;
	}
	
	
}
