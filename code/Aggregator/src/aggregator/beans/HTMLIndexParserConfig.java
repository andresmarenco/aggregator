package aggregator.beans;

import java.io.Serializable;
import java.util.Properties;

public class HTMLIndexParserConfig implements IndexParserConfig, Serializable {

	private static final long serialVersionUID = 201407242023L;
	private static final String PARSER_READ_TITLE_KEY = "index.parser.readTitle";
	private static final String PARSER_READ_METATAGS_KEY = "index.parser.readMetaTags";
	private static final String PARSER_READ_BODY_KEY = "index.parser.readBody";
	private static final String PARSER_REMOVE_SCRIPTS_KEY = "index.parser.removeScripts";
	private static final String PARSER_READ_CONTENTS_KEY = "index.parser.readContents";
	
	private boolean readTitle;
	private boolean readMetaTags;
	private boolean readBody;
	private boolean removeScripts;
	private String readContents;

	/**
	 * Default Constructor
	 */
	public HTMLIndexParserConfig() {
	}
	
	
	
	/**
	 * Creates a new instance of {@code HTMLIndexParserConfig} based on the properties file
	 * @param properties Properties file
	 * @return {@code HTMLIndexParserConfig} instance
	 */
	public static final HTMLIndexParserConfig newInstance(Properties properties) {
		HTMLIndexParserConfig config = new HTMLIndexParserConfig();
		config.setReadTitle(Boolean.parseBoolean(properties.getProperty(PARSER_READ_TITLE_KEY, "true")));
		config.setReadMetaTags(Boolean.parseBoolean(properties.getProperty(PARSER_READ_METATAGS_KEY, "true")));
		config.setReadBody(Boolean.parseBoolean(properties.getProperty(PARSER_READ_BODY_KEY, "true")));
		config.setRemoveScripts(Boolean.parseBoolean(properties.getProperty(PARSER_REMOVE_SCRIPTS_KEY, "true")));
		config.setReadContents(properties.getProperty(PARSER_READ_CONTENTS_KEY).trim());
		
		return config;
	}



	/**
	 * @return the readTitle
	 */
	public boolean isReadTitle() {
		return readTitle;
	}



	/**
	 * @param readTitle the readTitle to set
	 */
	public void setReadTitle(boolean readTitle) {
		this.readTitle = readTitle;
	}



	/**
	 * @return the readMetaTags
	 */
	public boolean isReadMetaTags() {
		return readMetaTags;
	}



	/**
	 * @param readMetaTags the readMetaTags to set
	 */
	public void setReadMetaTags(boolean readMetaTags) {
		this.readMetaTags = readMetaTags;
	}



	/**
	 * @return the readBody
	 */
	public boolean isReadBody() {
		return readBody;
	}



	/**
	 * @param readBody the readBody to set
	 */
	public void setReadBody(boolean readBody) {
		this.readBody = readBody;
	}



	/**
	 * @return the readContents
	 */
	public String getReadContents() {
		return readContents;
	}



	/**
	 * @param readContents the readContents to set
	 */
	public void setReadContents(String readContents) {
		this.readContents = readContents;
	}



	/**
	 * @return the removeScripts
	 */
	public boolean isRemoveScripts() {
		return removeScripts;
	}



	/**
	 * @param removeScripts the removeScripts to set
	 */
	public void setRemoveScripts(boolean removeScripts) {
		this.removeScripts = removeScripts;
	}
	
	
}
