package aggregator.util;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XMLUtils {
	
	private static final Log log = LogFactory.getLog(XMLUtils.class);
	private XPath xpath;
	
	
	/**
	 * Default Constructor
	 */
	public XMLUtils() {
		this.xpath = XPathFactory.newInstance().newXPath();
	}
	
	
	
	
	/**
	 * Evaluates an XPath Expression in the given document
	 * @param item The starting context (a node, for example)
	 * @param expression XPath Expression
	 * @param returnType Return type for the expression evaluation (one of {@link XPathConstants})
	 * @return Result of the expression
	 */
	@SuppressWarnings("unchecked")
	public <T> T executeXPath(Object item, String expression, Class<T> returnType) {
		T result = null;
		
		try
		{
			QName constantType = null;
			
			if(returnType == String.class) {
				constantType = XPathConstants.STRING;
			} else if(returnType == Double.class) {
				constantType = XPathConstants.NUMBER;
			} else if(returnType == Boolean.class) {
				constantType = XPathConstants.BOOLEAN;
			} else if(returnType == Node.class) {
				constantType = XPathConstants.NODE;
			} else if(returnType == NodeList.class) {
				constantType = XPathConstants.NODESET;
			} else {
				throw new IllegalArgumentException("Cannot convert to " + returnType.getName());
			}
			
			XPathExpression xpathExpression = xpath.compile(expression);
			result = returnType.cast(xpathExpression.evaluate(item, constantType));
			
			if((result != null) && (result instanceof String)) {
				result = (T) StringEscapeUtils.unescapeHtml4((String)result).replaceAll(String.valueOf((char)160), "").replaceAll("\\s+", " ").trim();
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
	
	
	
	/**
	 * Serializes a DOM
	 * @param node Node to serialize
	 * @return String representing the XML document
	 */
	public static String serializeDOM(Node node) {
		return serializeDOM(node, false, false);
	}
	
	
	
	
	/**
	 * Serializes a DOM
	 * @param node Node to serialize
	 * @param compactFormat Print in compact format
	 * @param omitDeclaration Omit XML declaration
	 * @return String representing the XML document
	 */
	public static String serializeDOM(Node node, boolean compactFormat, boolean omitDeclaration) {
		String result = null;
		try
		{
			Format format = (compactFormat) ? Format.getCompactFormat() : Format.getPrettyFormat();
			XMLOutputter out = new XMLOutputter(format.setOmitDeclaration(omitDeclaration));
			
			if(node instanceof Document) {
				result = out.outputString(new DOMBuilder().build((Document)node));
			} else if(node instanceof Element) {
				result = out.outputString(new DOMBuilder().build((Element)node));
			} else if(node instanceof Text) {
				result = out.outputString(new DOMBuilder().build((Text)node));
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
}
