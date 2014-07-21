package aggregator.verticalwrapper.srr;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;

import aggregator.beans.QueryResult;
import aggregator.util.XMLUtils;

public class XMLCommandParser {
	
	private Log log = LogFactory.getLog(XMLCommandParser.class);
	
	
	/**
	 * Default Constructor
	 */
	public XMLCommandParser() {
	}
	
	
	
	
	/**
	 * Parses and executes the given command line
	 * @param node Node with results
	 * @param queryResult Object to set the results 
	 * @param commandLine Command line
	 */
	public void parseAndExecute(Node node, QueryResult queryResult, String commandLine) {
		if(StringUtils.isNotBlank(commandLine)) {
			int spaceIndex = commandLine.indexOf(' ');
			String cmd = commandLine.substring(0, spaceIndex).trim();
			String params = commandLine.substring(spaceIndex).trim();
			
			if(StringUtils.equalsIgnoreCase(cmd, "SET")) {
				spaceIndex = params.indexOf(' ');
				String variable = params.substring(0, spaceIndex);
				params = params.substring(spaceIndex);
				
				this.cmdSet(node, queryResult, variable, params);
			} else {
				log.warn(MessageFormat.format("Unknown command {0} in {1}", cmd, commandLine));
			}
		}
	}
	
	
	
	
	/**
	 * Sets the variable with the given XPath expression 
	 * @param node Node with the results
	 * @param queryResult Object to set the results
	 * @param variable Variable to set
	 * @param expression XPath expression for the value
	 */
	private void cmdSet(Node node, QueryResult queryResult, String variable, String expression) {
		String value = XMLUtils.executeXPath(node, expression, String.class);
		if(StringUtils.equalsIgnoreCase(variable, "id")) {
			queryResult.setId(value);
		} else if(StringUtils.equalsIgnoreCase(variable, "title")) {
			queryResult.setTitle(value);
		} else if(StringUtils.equalsIgnoreCase(variable, "summary")) {
			queryResult.setSummary(value);
		}
	}

}
