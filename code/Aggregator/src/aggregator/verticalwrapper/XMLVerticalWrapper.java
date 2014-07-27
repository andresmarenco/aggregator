package aggregator.verticalwrapper;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aggregator.beans.QueryResult;
import aggregator.beans.Vertical;
import aggregator.beans.VerticalConfig;
import aggregator.beans.XMLSampledDocument;
import aggregator.util.XMLUtils;
import aggregator.verticalwrapper.auth.AuthScript;
import aggregator.verticalwrapper.auth.XMLAuthScript;
import aggregator.verticalwrapper.query.QueryScript;
import aggregator.verticalwrapper.query.XMLQueryScript;
import aggregator.verticalwrapper.srr.SRRScript;
import aggregator.verticalwrapper.srr.XMLSRRScript;

public class XMLVerticalWrapper extends AbstractVerticalWrapper {
	
	private XMLUtils xmlUtils;
	
	/**
	 * Default Constructor
	 * @param vertical Vertical Object
	 * @param config Vertical Configuration
	 */
	public XMLVerticalWrapper(Vertical vertical, VerticalConfig config) {
		super(vertical, config);
		
		this.xmlUtils = new XMLUtils();
	}
	
	
	
	
	@Override
	public Document authenticate() {
		AuthScript<Document> authScript = new XMLAuthScript();
		return authScript.execute(config.getAuthScript());
	}
	
	
	
	
	@Override
	public List<QueryResult> executeQuery(String query) {
		List<QueryResult> result = new ArrayList<QueryResult>();
		Document authData = this.authenticate();
		
		QueryScript<NodeList> queryScript = new XMLQueryScript(authData);
		SRRScript<NodeList> srrScript = new XMLSRRScript(vertical);
		
		NodeList queryResult = queryScript.execute(query, config.getQueryScript());
		
		result = srrScript.execute(queryResult, config.getSRRScript());
		
		return result;
	}
	
	
	
	
	@Override
	public XMLSampledDocument downloadDocument(QueryResult document) {
		XMLSampledDocument result = new XMLSampledDocument(document.getId());
		Document authData = authenticate();
		
		String expression = StringUtils.replace(config.getIdPattern(), "{%ID}", document.getId());
		Node node = xmlUtils.executeXPath(authData, expression, Node.class);
		
		try
		{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.newDocument();
			
			doc.appendChild(doc.importNode(node, true));

			result.setDocument(doc);
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return result;
	}
}
