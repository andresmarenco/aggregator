package aggregator.beans;

import java.io.FileInputStream;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import aggregator.util.XMLUtils;

public class DOCSampledDocument implements SampledDocument<Document> {

	public static final String ROOT_NAME = "parsedDoc";
	private static final long serialVersionUID = 201408190421L;
	
	private String id;
	private Document document;
	private Log log = LogFactory.getLog(DOCSampledDocument.class);
	
	/**
	 * Default Constructor
	 * @param id Document ID
	 */
	public DOCSampledDocument(String id) {
		this.id = id;
	}
	
	
	/**
	 * Creates a {@code DOCSampledDocument} reading the given path
	 * @param path Document path
	 */
	public DOCSampledDocument(Path path) {
		this.id = path.getFileName().toString();
		this.readFromFile(path);
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public Document deserialize(String document) {
		return null;
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
	public void readFromFile(Path path) {
		try
		{
			try(FileInputStream fis = new FileInputStream(path.toFile())) {
				HWPFDocument doc = new HWPFDocument(fis);
				try(WordExtractor word = new WordExtractor(doc)) {
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder dbuilder = dbf.newDocumentBuilder();
					
					this.document = dbuilder.newDocument();
					
					Node root = document.createElement(ROOT_NAME);
					document.appendChild(root);
					
					Node nodeId = document.createElement("id");
					nodeId.setTextContent(id);
					root.appendChild(nodeId);
					
					String parsedText = word.getText();
					
					if(StringUtils.isNotBlank(parsedText)) {
						Node nodeText = document.createElement("text");
						nodeText.setTextContent(parsedText);
						root.appendChild(nodeText);
					}
				}
			}
		}
		catch(Exception ex) {
			log.error("Bad formed DOC. Couldn't fix.");
		}
	}

}
