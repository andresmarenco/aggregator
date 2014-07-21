package aggregator.beans;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import aggregator.util.XMLUtils;

public class XMLSampledDocument implements SampledDocument<Document> {

	private static final long serialVersionUID = 201407150728L;
	private Document document;
	private String id;
	private Log log = LogFactory.getLog(XMLSampledDocument.class);
	
	/**
	 * Default Constructor
	 * @param id Document ID
	 */
	public XMLSampledDocument(String id) {
		this.id = id;
	}

	@Override
	public Document deserialize(String document) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		
		try
		{
			dBuilder = dbFactory.newDocumentBuilder();
			this.document = dBuilder.parse(new InputSource(new StringReader(document)));
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return this.document;
	}

	@Override
	public String serialize() {
		return XMLUtils.serializeDOM(document);
	}

	@Override
	public void setDocument(Document document) {
		this.document = document;
	}

	@Override
	public Document getDocument() {
		return this.document;
	}

	@Override
	public String getId() {
		return this.id;
	}

}
