package aggregator.beans;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class HTMLIndexParserConfig implements IndexParserConfig, Serializable {

	private static final long serialVersionUID = 201407242023L;
	private static final String PARSER_READ_TITLE_KEY = "index.parser.readTitle";
	private static final String PARSER_READ_METATAGS_KEY = "index.parser.readMetaTags";
	private static final String PARSER_READ_BODY_KEY = "index.parser.readBody";
	private static final String PARSER_REMOVE_SCRIPTS_KEY = "index.parser.removeScripts";
	private static final String PARSER_REMOVE_NUMBERS_KEY = "index.parser.removeNumbers";
	private static final String PARSER_READ_CONTENTS_KEY = "index.parser.readContents";
	private static final String PARSER_READ_BIBTEX_KEY = "index.parser.readBibTeX";
	private static final String PARSER_IGNORE_BIBTEX_KEY = "index.parser.readBibTeX.ignore";
	private static final String PARSER_STOPWORDS_KEY = "index.parser.stopwords";
	
	private boolean readTitle;
	private boolean readMetaTags;
	private boolean readBody;
	private boolean removeScripts;
	private boolean removeNumbers;
	private String readContents;
	private String readBibTeX;
	private List<String> ignoreBibTeX;
	private List<String> stopwords;

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
		config.setRemoveNumbers(Boolean.parseBoolean(properties.getProperty(PARSER_REMOVE_NUMBERS_KEY, "false")));
		config.setReadContents(properties.getProperty(PARSER_READ_CONTENTS_KEY, "").trim());
		config.setReadBibTeX(properties.getProperty(PARSER_READ_BIBTEX_KEY, "").trim());
		config.setStopwords(Arrays.asList(properties.getProperty(PARSER_STOPWORDS_KEY, "").toLowerCase().split("\\|")));
		config.setIgnoreBibTeX(Arrays.asList(properties.getProperty(PARSER_IGNORE_BIBTEX_KEY, "").toLowerCase().split("\\|")));
		
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



	/**
	 * @return the removeNumbers
	 */
	public boolean isRemoveNumbers() {
		return removeNumbers;
	}



	/**
	 * @param removeNumbers the removeNumbers to set
	 */
	public void setRemoveNumbers(boolean removeNumbers) {
		this.removeNumbers = removeNumbers;
	}



	/**
	 * @return the stopwords
	 */
	public List<String> getStopwords() {
		return stopwords;
	}



	/**
	 * @param stopwords the stopwords to set
	 */
	public void setStopwords(List<String> stopwords) {
		this.stopwords = stopwords;
	}



	/**
	 * @return the readBibTeX
	 */
	public String getReadBibTeX() {
		return readBibTeX;
	}



	/**
	 * @param readBibTeX the readBibTeX to set
	 */
	public void setReadBibTeX(String readBibTeX) {
		this.readBibTeX = readBibTeX;
	}



	/**
	 * @return the ignoreBibTeX
	 */
	public List<String> getIgnoreBibTeX() {
		return ignoreBibTeX;
	}



	/**
	 * @param ignoreBibTeX the ignoreBibTeX to set
	 */
	public void setIgnoreBibTeX(List<String> ignoreBibTeX) {
		this.ignoreBibTeX = ignoreBibTeX;
	}
	
	
}
