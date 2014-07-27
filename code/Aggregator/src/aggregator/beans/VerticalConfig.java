package aggregator.beans;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.LogFactory;

import aggregator.sampler.parser.HTMLSamplerParser;
import aggregator.sampler.parser.SamplerParser;
import aggregator.util.CommonUtils;
import aggregator.verticalwrapper.AbstractVerticalWrapper;
import aggregator.verticalwrapper.HTTPVerticalWrapper;

public class VerticalConfig implements Serializable {

	private static final long serialVersionUID = 201407040244L;
	
	private static final String WRAPPER_TYPE_KEY = "wrapperType";
	private static final String INDEX_PARSER_TYPE_KEY = "index.parserType";
	private static final String AUTH_SCRIPT_KEY = "authScript";
	private static final String AUTH_SCRIPT_FILE_KEY = "authScript.readAsFile";
	private static final String QUERY_SCRIPT_KEY = "queryScript";
	private static final String QUERY_SCRIPT_FILE_KEY = "queryScript.readAsFile";
	private static final String SRR_SCRIPT_KEY = "srrScript";
	private static final String SRR_SCRIPT_FILE_KEY = "srrScript.readAsFile";
	private static final String ID_PATTERN_KEY = "idPattern";
	
	private Class<? extends AbstractVerticalWrapper> wrapperType;
	private Class<? extends SamplerParser> indexParserType;
	private List<String> authScript;
	private List<String> queryScript;
	/** Search Result Records Extraction */
	private List<String> srrScript;
	private String idPattern;
	private WrapperConfig wrapperConfig;
	private IndexParserConfig indexParserConfig;
	
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
		config.setWrapperType(CommonUtils.getSubClassType(AbstractVerticalWrapper.class, properties.getProperty(WRAPPER_TYPE_KEY)));
		if(config.getWrapperType() == HTTPVerticalWrapper.class) {
			config.setWrapperConfig(HTTPWrapperConfig.newInstance(properties));
		}
		
		config.setIndexParserType(CommonUtils.getSubClassType(SamplerParser.class, properties.getProperty(INDEX_PARSER_TYPE_KEY)));
		if(config.getIndexParserType() == HTMLSamplerParser.class) {
			config.setIndexParserConfig(HTMLIndexParserConfig.newInstance(properties));
		}
		
		
		try
		{
			Path verticalsPath = CommonUtils.getVerticalsPath();
			if(Boolean.parseBoolean(properties.getProperty(AUTH_SCRIPT_FILE_KEY))) {
				config.setAuthScript(CommonUtils.readScriptFile(verticalsPath.resolve(properties.getProperty(AUTH_SCRIPT_KEY))));
			} else {
				config.setAuthScript(new ArrayList<String>());
				config.getAuthScript().add(properties.getProperty(AUTH_SCRIPT_KEY));
			}
			
			if(Boolean.parseBoolean(properties.getProperty(QUERY_SCRIPT_FILE_KEY))) {
				config.setQueryScript(CommonUtils.readScriptFile(verticalsPath.resolve(properties.getProperty(QUERY_SCRIPT_KEY))));
			} else {
				config.setQueryScript(new ArrayList<String>());
				config.getQueryScript().add(properties.getProperty(QUERY_SCRIPT_KEY));
			}
			
			if(Boolean.parseBoolean(properties.getProperty(SRR_SCRIPT_FILE_KEY))) {
				config.setSRRScript(CommonUtils.readScriptFile(verticalsPath.resolve(properties.getProperty(SRR_SCRIPT_KEY))));
			} else {
				config.setSRRScript(new ArrayList<String>());
				config.getSRRScript().add(properties.getProperty(SRR_SCRIPT_KEY));
			}
		}
		catch(Exception ex) {
			LogFactory.getLog(VerticalConfig.class).error(ex.getMessage(), ex);
		}
		
		config.setIdPattern(properties.getProperty(ID_PATTERN_KEY));
		
		return config;
	}

	/**
	 * @return the wrapperType
	 */
	public Class<? extends AbstractVerticalWrapper> getWrapperType() {
		return wrapperType;
	}

	/**
	 * @param wrapperType the wrapperType to set
	 */
	public void setWrapperType(Class<? extends AbstractVerticalWrapper> wrapperType) {
		this.wrapperType = wrapperType;
	}

	/**
	 * @return the indexParserType
	 */
	public Class<? extends SamplerParser> getIndexParserType() {
		return indexParserType;
	}
	
	/**
	 * Creates an instance of the corresponding index parser
	 * @return Index parser instance
	 */
	public SamplerParser newIndexParserInstance() {
		SamplerParser result = null;
		if(indexParserType == HTMLSamplerParser.class) {
			result = new HTMLSamplerParser(this.indexParserConfig);
		}
		return result;
	}

	/**
	 * @param indexParserType the indexParserType to set
	 */
	public void setIndexParserType(Class<? extends SamplerParser> indexParserType) {
		this.indexParserType = indexParserType;
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
	 * @return the wrapperConfig
	 */
	public WrapperConfig getWrapperConfig() {
		return wrapperConfig;
	}

	/**
	 * @param wrapperConfig the wrapperConfig to set
	 */
	public void setWrapperConfig(WrapperConfig wrapperConfig) {
		this.wrapperConfig = wrapperConfig;
	}

	/**
	 * @return the indexParserConfig
	 */
	public IndexParserConfig getIndexParserConfig() {
		return indexParserConfig;
	}

	/**
	 * @param indexParserConfig the indexParserConfig to set
	 */
	public void setIndexParserConfig(IndexParserConfig indexParserConfig) {
		this.indexParserConfig = indexParserConfig;
	}
}
