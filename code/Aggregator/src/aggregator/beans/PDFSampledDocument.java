package aggregator.beans;

import java.io.FileInputStream;
import java.io.StringReader;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import aggregator.util.XMLUtils;

public class PDFSampledDocument implements SampledDocument<Document> {

	public static final String ROOT_NAME = "parsedPdf";
	private static final long serialVersionUID = 201408190421L;
	
	private String id;
	private Document document;
	private Log log = LogFactory.getLog(PDFSampledDocument.class);
	
	/**
	 * Default Constructor
	 * @param id Document ID
	 */
	public PDFSampledDocument(String id) {
		this.id = id;
	}
	
	
	/**
	 * Creates a {@code PDFSampledDocument} reading the given path
	 * @param path Document path
	 */
	public PDFSampledDocument(Path path) {
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
		try
		{
			PDFParser parser = new PDFParser(new FileInputStream(path.toFile()));
			parser.parse();
			
			try(COSDocument cosDoc = parser.getDocument();
					PDDocument pdDoc = new PDDocument(cosDoc))
			{
				PDFTextStripper pdfStripper = new PDFTextStripper();
				String parsedText = pdfStripper.getText(pdDoc);
				
				PDDocumentInformation info = pdDoc.getDocumentInformation();
				
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder dbuilder = dbf.newDocumentBuilder();
				
				this.document = dbuilder.newDocument();
				
				Node root = document.createElement(ROOT_NAME);
				document.appendChild(root);
				
				Node nodeId = document.createElement("id");
				nodeId.setTextContent(id);
				root.appendChild(nodeId);
				
				if(StringUtils.isNotBlank(info.getTitle())) {
					Node nodeTitle = document.createElement("title");
					nodeTitle.setTextContent(info.getTitle());
					root.appendChild(nodeTitle);
				}
				
				if(StringUtils.isNotBlank(info.getAuthor())) {
					Node nodeAuthor = document.createElement("author");
					nodeAuthor.setTextContent(info.getAuthor());
					root.appendChild(nodeAuthor);
				}
				
				if(StringUtils.isNotBlank(info.getSubject())) {
					Node nodeSubject = document.createElement("subject");
					nodeSubject.setTextContent(info.getSubject());
					root.appendChild(nodeSubject);
				}
				
				if(StringUtils.isNotBlank(info.getKeywords())) {
					Node nodeKeywords = document.createElement("keywords");
					nodeKeywords.setTextContent(info.getKeywords());
					root.appendChild(nodeKeywords);
				}
				
				if(StringUtils.isNotBlank(parsedText)) {
					Node nodeText = document.createElement("text");
					nodeText.setTextContent(parsedText);
					root.appendChild(nodeText);
				}
			}
		}
		catch(Exception ex) {
			log.error("Bad formed PDF. Couldn't fix.");
		}
	}

}
