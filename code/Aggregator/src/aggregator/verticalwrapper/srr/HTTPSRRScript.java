package aggregator.verticalwrapper.srr;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aggregator.beans.QueryResult;
import aggregator.beans.Vertical;
import aggregator.util.IterableNodeList;
import aggregator.util.XMLUtils;

public class HTTPSRRScript implements SRRScript<Document> {
	
	private Hashtable<String, NodeList> nodeListTable;
	private Stack<QueryResult> srrResult;
	private Vertical vertical;
	private ListIterator<String> executionIterator;
	private Node currentNode;
	private Queue<Node> nodeHistory;
	private Queue<ListIterator<String>> executionIteratorHistory;
	private List<String> executionScript;
	private XMLUtils xmlUtils;
	private Log log = LogFactory.getLog(HTTPSRRScript.class);
	
	
	/**
	 * Default Constructor
	 */
	public HTTPSRRScript(Vertical vertical) {
		this.srrResult = new Stack<QueryResult>();
		this.vertical = vertical;
		this.nodeListTable = new Hashtable<String, NodeList>();
		this.nodeHistory = new LinkedList<Node>();
		this.executionIteratorHistory = new LinkedList<ListIterator<String>>();
		this.xmlUtils = new XMLUtils();
	}
	
	

	@Override
	public List<QueryResult> execute(Document queryResult, List<String> script) {
		
		try
		{
			executionScript = script;
			executionIterator = script.listIterator();
			currentNode = queryResult;
			
			while(executionIterator.hasNext()) {
				this.parseAndExecute(executionIterator.next());
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return this.srrResult;
	}
	
	
	
	
	/**
	 * Parses and executes the given command line
	 * @param commandLine Command line
	 */
	private void parseAndExecute(String commandLine) {
		if(StringUtils.isNotBlank(commandLine)) {
			commandLine = commandLine.trim();
			
			if(!commandLine.startsWith("#")) {
				int spaceIndex = commandLine.indexOf(' ');
				String cmd = (spaceIndex > -1) ? commandLine.substring(0, spaceIndex).trim() : commandLine;
				String params = (spaceIndex > -1) ? commandLine.substring(spaceIndex).trim() : "";
				
				if(StringUtils.equalsIgnoreCase(cmd, "LIST")) {
					spaceIndex = params.indexOf(' ');
					String listName = params.substring(0, spaceIndex).trim();
					String expression = params.substring(spaceIndex).trim();
					
					this.cmdList(listName, expression);
				} else if(StringUtils.equalsIgnoreCase(cmd, "FOREACH")) {
					this.cmdForeach(params);
				} else if(StringUtils.equalsIgnoreCase(cmd, "ENDFOR")) {
					this.cmdEndFor();
				} else if(StringUtils.equalsIgnoreCase(cmd, "ADDRESULT")) {
					this.cmdAddResult();
				} else if(StringUtils.equalsIgnoreCase(cmd, "SPLIT")) {
					spaceIndex = params.indexOf(' ');
					String variable = params.substring(0, spaceIndex);
					params = params.substring(spaceIndex).trim();
					
					this.cmdSplit(variable, params);
				} else if(StringUtils.equalsIgnoreCase(cmd, "SET")) {
					spaceIndex = params.indexOf(' ');
					String variable = params.substring(0, spaceIndex);
					params = params.substring(spaceIndex);
					
					this.cmdSet(variable, params);
				} else if(StringUtils.equalsIgnoreCase(cmd, "SETLIST")) {
					spaceIndex = params.indexOf(' ');
					String variable = params.substring(0, spaceIndex);
					params = params.substring(spaceIndex);
					
					this.cmdSetList(variable, params);
				} else {
					log.warn(MessageFormat.format("Unknown command {0} in {1}", cmd, commandLine));
				}
			}
		}
	}
	
	
	
	
	/**
	 * Creates a list based on the given expression
	 * @param listName List name
	 * @param expression XPath expression
	 */
	private void cmdList(String listName, String expression) {
		NodeList nodeList = xmlUtils.executeXPath(currentNode, expression, NodeList.class);
		if(nodeList != null) {
			this.nodeListTable.put(listName, nodeList);
			
			if(log.isInfoEnabled()) {
				log.info(MessageFormat.format("Found {0} items for list \"{1}\" in vertical {2}", nodeList.getLength(), listName, vertical.getName()));
			}
		}
	}
	
	
	
	
	/**
	 * Initializes a loop in the specified list
	 * @param listName List to iterate
	 */
	private void cmdForeach(String listName) {
		NodeList list = nodeListTable.get(listName);
		if((list != null) && (list.getLength() > 0)) {
			for(Node node : new IterableNodeList(list)) {
				nodeHistory.add(node);
			}
			
			currentNode = nodeHistory.poll();
		}
		nodeHistory.add(currentNode);
		executionIteratorHistory.add(executionIterator);
		
		executionIterator = executionScript.listIterator(executionIterator.nextIndex());
	}
	
	
	
	
	/**
	 * Finishes the current for loop
	 */
	private void cmdEndFor() {
		currentNode = nodeHistory.poll();
		if(!nodeHistory.isEmpty()) {
			executionIterator = executionScript.listIterator(executionIteratorHistory.peek().nextIndex());
		} else {
			executionIteratorHistory.remove();
		}
	}

	
	
	
	/**
	 * Adds a new {@code QueryResult} to the result list
	 */
	private void cmdAddResult() {
		this.srrResult.add(new QueryResult(vertical));
	}
	
	
	
	
	/**
	 * Splits the variable based on the delimiter
	 * @param variable Variable to split
	 * @param delimiter Delimiter string
	 */
	private void cmdSplit(String variable, String delimiter) {
		QueryResult queryResult = this.srrResult.peek();
		
		if(StringUtils.equalsIgnoreCase(variable, "authors")) {
			queryResult.setAuthors(this.splitList(queryResult.getAuthors(), delimiter));
		} else if(StringUtils.equalsIgnoreCase(variable, "keywords")) {
			queryResult.setKeywords(this.splitList(queryResult.getKeywords(), delimiter));
		}
	}
	
	
	
	
	/**
	 * Splits the elements of the current list based on the delimiter
	 * @param currentList List to split
	 * @param delimiter Delimiter string
	 * @return New list with separated items
	 */
	private List<String> splitList(List<String> currentList, String delimiter) {
		List<String> splittedList = new ArrayList<String>();
		for(String item : currentList) {
			String[] subitems = item.split(delimiter);
			for(String subitem : subitems) {
				if(StringUtils.isNotBlank(subitem)) {
					splittedList.add(subitem.trim());
				}
			}
		}
		
		return splittedList;
	}
	
	
	
	
	/**
	 * Sets the variable with the given XPath expression
	 * @param variable Variable to set
	 * @param expression XPath expression for the value
	 */
	@SuppressWarnings("unchecked")
	private void cmdSetList(String variable, String expression) {
		QueryResult queryResult = this.srrResult.peek();
		String stringValue = "";
		
		if(StringUtils.equalsIgnoreCase(variable, "id")) {
			stringValue = this.getNodeListValue(expression, String.class);
			queryResult.setId(stringValue);
		} else if(StringUtils.equalsIgnoreCase(variable, "title")) {
			stringValue = this.getNodeListValue(expression, String.class);
			queryResult.setTitle(stringValue);
		} else if(StringUtils.equalsIgnoreCase(variable, "summary")) {
			stringValue = this.getNodeListValue(expression, String.class);
			queryResult.setSummary(stringValue);
		} else if(StringUtils.equalsIgnoreCase(variable, "info")) {
			stringValue = this.getNodeListValue(expression, String.class);
			queryResult.setInfo(stringValue);
		} else if(StringUtils.equalsIgnoreCase(variable, "authors")) {
			queryResult.getAuthors().addAll(this.getNodeListValue(expression, List.class));
			stringValue = Arrays.toString(queryResult.getAuthors().toArray());
		} else if(StringUtils.equalsIgnoreCase(variable, "keywords")) {
			queryResult.getKeywords().addAll(this.getNodeListValue(expression, List.class));
			stringValue = Arrays.toString(queryResult.getKeywords().toArray());
		}
		
		if(log.isDebugEnabled()) {
			log.debug(MessageFormat.format("Setting \"{0}\" into \"{1}\" variable", stringValue, variable));
		}
	}
	
	
	
	
	/**
	 * Sets the variable with the given XPath expression
	 * @param variable Variable to set
	 * @param expression XPath expression for the value
	 */
	private void cmdSet(String variable, String expression) {
		QueryResult queryResult = this.srrResult.peek();
		String stringValue = "";
		
		if(StringUtils.equalsIgnoreCase(variable, "id")) {
			stringValue = xmlUtils.executeXPath(currentNode, expression, String.class);
			queryResult.setId(stringValue);
		} else if(StringUtils.equalsIgnoreCase(variable, "title")) {
			stringValue = xmlUtils.executeXPath(currentNode, expression, String.class);
			queryResult.setTitle(stringValue);
		} else if(StringUtils.equalsIgnoreCase(variable, "summary")) {
			stringValue = xmlUtils.executeXPath(currentNode, expression, String.class);
			queryResult.setSummary(stringValue);
		} else if(StringUtils.equalsIgnoreCase(variable, "info")) {
			stringValue = xmlUtils.executeXPath(currentNode, expression, String.class);
			queryResult.setInfo(stringValue);
		} else if(StringUtils.equalsIgnoreCase(variable, "authors")) {
			queryResult.getAuthors().add(xmlUtils.executeXPath(currentNode, expression, String.class));
			stringValue = Arrays.toString(queryResult.getAuthors().toArray());
		} else if(StringUtils.equalsIgnoreCase(variable, "keywords")) {
			queryResult.getKeywords().add(xmlUtils.executeXPath(currentNode, expression, String.class));
			stringValue = Arrays.toString(queryResult.getKeywords().toArray());
		}
		
		if(log.isDebugEnabled()) {
			log.debug(MessageFormat.format("Setting \"{0}\" into \"{1}\" variable", stringValue, variable));
		}
	}
	
	
	
	
	/**
	 * Finds the value of the given expression
	 * @param expression XPath expression
	 * @param returnType Return type
	 * @return Result of the evaluation
	 */
	private <T> T getNodeListValue(String expression, Class<T> returnType) {
		T result = null;
		IterableNodeList nodeList = new IterableNodeList(xmlUtils.executeXPath(currentNode, expression, NodeList.class));
		if(returnType == String.class) {
			StringBuilder builder = new StringBuilder();
			for(Node node : nodeList) {
				String content = node.getTextContent();
				if(StringUtils.isNotBlank(content)) {
					builder.append(content).append(" ");
				}
			}
			
			result = returnType.cast(builder.toString().replaceAll("\\s+", " "));
		} if(returnType == List.class) {
			List<String> resultList = new ArrayList<String>();
			for(Node node : nodeList) {
				String content = node.getTextContent().replaceAll("\\s+", " ");
				if(StringUtils.isNotBlank(content)) {
					resultList.add(content);
				}
			}
			
			result = returnType.cast(resultList);
		}
		
		return result;
	}
}
