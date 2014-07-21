package aggregator.verticalwrapper.query;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import aggregator.util.XMLUtils;

public class XMLQueryScript implements QueryScript<NodeList> {

	private Document authData;
	private NodeList queryResult;
	private Log log = LogFactory.getLog(XMLQueryScript.class);
	
	/**
	 * Default Constructor
	 * @param authData Authentication data
	 */
	public XMLQueryScript(Document authData) {
		this.authData = authData;
		this.queryResult = null;
	}
	

	@Override
	public NodeList execute(String query, List<String> script) {
		for(String command : script) {
			this.parseAndExecute(query, command);
		}
		
		return queryResult;
	}
	
	
	
	/**
	 * Parses and executes the given command line
	 * @param query Query string
	 * @param commandLine Command line
	 */
	private void parseAndExecute(String query, String commandLine) {
		if(StringUtils.isNotBlank(commandLine)) {
			int spaceIndex = commandLine.indexOf(' ');
			String cmd = commandLine.substring(0, spaceIndex).trim();
			String params = commandLine.substring(spaceIndex).trim();
			
			if(StringUtils.equalsIgnoreCase(cmd, "LIST")) {
				this.cmdList(query, params);
			} else {
				log.warn(MessageFormat.format("Unknown command {0} in {1}", cmd, commandLine));
			}
		}
	}
	
	
	
	
	/**
	 * Executes the XPath in the base XML document and saves it in the {@code queryResult} variable
	 * @param query Query string
	 * @param expression XPath Expression
	 */
	private void cmdList(String query, String expression) {
		expression = StringUtils.replace(expression, "{%QUERY}", query);
		
		this.queryResult = XMLUtils.executeXPath(authData, expression, NodeList.class);
	}

}
