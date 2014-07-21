package aggregator.verticalwrapper.auth;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

public class XMLAuthScript implements AuthScript<Document> {
	
	private Document authResult;
	private Log log = LogFactory.getLog(XMLAuthScript.class);
	
	/**
	 * Default Constructor
	 */
	public XMLAuthScript() {
		this.authResult = null;
	}

	@Override
	public Document execute(List<String> script) {
		for(String command : script) {
			this.parseAndExecute(command);
		}
		
		return authResult;
	}


	/**
	 * Parses and executes the given command line
	 * @param commandLine Command line
	 */
	private void parseAndExecute(String commandLine) {
		if(StringUtils.isNotBlank(commandLine)) {
			int spaceIndex = commandLine.indexOf(' ');
			String cmd = commandLine.substring(0, spaceIndex).trim();
			String params = commandLine.substring(spaceIndex).trim();
			
			if(StringUtils.equalsIgnoreCase(cmd, "OPEN")) {
				this.cmdOpen(params);
			} else {
				log.warn(MessageFormat.format("Unknown command {0} in {1}", cmd, commandLine));
			}
		}
	}
	
	
	
	
	/**
	 * Opens the given XML file and saves the document in {@code authResult} variable
	 * @param filename XML file
	 */
	private void cmdOpen(String filename) {
		try
		{
			File xmlFile = new File(filename);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			
			this.authResult = dBuilder.parse(xmlFile);
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
}
