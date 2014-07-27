package aggregator.beans;

import java.io.StringReader;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import aggregator.util.XMLUtils;

public class XMLSampledDocument implements SampledDocument<Document> {

	private static final long serialVersionUID = 201407150728L;
	
	private String id;
	private Document document;
	private Log log = LogFactory.getLog(XMLSampledDocument.class);
	
	/**
	 * Default Constructor
	 * @param id Document ID
	 */
	public XMLSampledDocument(String id) {
		this.id = id;
	}
	
	
	/**
	 * Creates a {@code XMLSampledDocument} reading the given path
	 * @param path Document path
	 */
	public XMLSampledDocument(Path path) {
		this.id = path.getFileName().toString();
		this.readFromFile(path);
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
		return XMLUtils.serializeDOM(document, false, true);
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

	@Override
	public void readFromFile(Path path) {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		
		try
		{
			dBuilder = dbFactory.newDocumentBuilder();
			this.document = dBuilder.parse(path.toFile());
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
	}

}
