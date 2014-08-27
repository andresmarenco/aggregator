package aggregator.sampler.parser;

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;
import org.jbibtex.BibTeXString;
import org.jbibtex.Key;
import org.jbibtex.Value;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aggregator.beans.DOCSampledDocument;
import aggregator.beans.HTMLIndexParserConfig;
import aggregator.beans.IndexParserConfig;
import aggregator.beans.PDFSampledDocument;
import aggregator.beans.SampledDocument;
import aggregator.beans.XMLSampledDocument;
import aggregator.util.IterableNodeList;
import aggregator.util.XMLUtils;

public class HTMLSamplerParser implements SamplerParser {
	
	public static final String TITLE_KEY = "title";
	public static final String BODY_KEY = "body";
	public static final String META_PREFIX_KEY = "meta";
	public static final String BIBTEX_PREFIX_KEY = "bibtex";
	public static final String PDF_PREFIX_KEY = "pdf";
	public static final String DOC_PREFIX_KEY = "doc";
	
	private HTMLIndexParserConfig config;
	private XMLUtils xmlUtils;
	private BibTeXParser bibtexParser;
	private Log log = LogFactory.getLog(HTMLSamplerParser.class);
	
	/**
	 * Default Constructor
	 * @param indexParserConfig HTML Index parser configuration for the vertical 
	 */
	public HTMLSamplerParser(IndexParserConfig indexParserConfig) {
		this.xmlUtils = new XMLUtils();
		if(indexParserConfig instanceof HTMLIndexParserConfig) {
			this.config = (HTMLIndexParserConfig)indexParserConfig;
		} else {
			log.error("Wrong class type for Index Parser Config");
		}
		
		
		try
		{
			this.bibtexParser = new BibTeXParser() {
				@Override
				public void checkCrossReferenceResolution(Key key, BibTeXEntry entry) {
					log.warn("Unresolved string: \"" + key.getValue() + "\"");
				}
				
				@Override
				public void checkStringResolution(Key key, BibTeXString string) {
					log.warn("Unresolved cross-reference: \"" + key.getValue() + "\"");
				}
			};
		}
		catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
	
	
	
	
	@Override
	public List<Entry<String, String>> parseDocument(SampledDocument<?> document) {
		List<Entry<String, String>> result = new ArrayList<Entry<String,String>>();
		try
		{
			if((document != null) && (document.getClass() != null)) {
				// .PDF Document
				if(document instanceof PDFSampledDocument) {
					PDFSampledDocument pdfDoc = (PDFSampledDocument)document;
					if(config.isReadPDF()) {
						result.addAll(this.getPDFText(pdfDoc.getDocument()));
					}
				}
				
				// .DOC document
				if(document instanceof DOCSampledDocument) {
					DOCSampledDocument docDoc = (DOCSampledDocument)document;
					if(config.isReadPDF()) {
						result.addAll(this.getDOCText(docDoc.getDocument()));
					}
				}
				
				// Regular HTML document
				if(document instanceof XMLSampledDocument) {
					if(StringUtils.isNotBlank(config.getResetDocument())) {
						String replaceDoc = xmlUtils.executeXPath(document.getDocument(), config.getResetDocument(), String.class);
						if(StringUtils.isNotBlank(replaceDoc)) {
							document = new XMLSampledDocument(document.getId());
							document.deserialize(replaceDoc);
						}
					}
					
					XMLSampledDocument xmlDoc = (XMLSampledDocument)document;
					
					if(config.isRemoveScripts()) {
						this.removeScripts(xmlDoc.getDocument());
					}
					
					if(config.isReadTitle()) {
						result.add(new MutablePair<String, String>(TITLE_KEY, this.getTitle(xmlDoc.getDocument())));
					}
					
					if(config.isReadMetaTags()) {
						result.addAll(this.getMetaTags(xmlDoc.getDocument()));
					}
					
					if(config.isReadBody()) {
						result.add(new MutablePair<String, String>(BODY_KEY, this.getBody(xmlDoc.getDocument())));
					}
					
					if(StringUtils.isNotBlank(config.getReadContents())) {
						result.addAll(this.getExtraText(xmlDoc.getDocument(), config.getReadContents()));
					}
					
					if(StringUtils.isNotBlank(config.getReadBibTeX())) {
						result.addAll(this.getBibTeX(xmlDoc.getDocument(), config.getReadBibTeX()));
					}
				}
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return result;
	}




	@Override
	public boolean isValid(String token) {
		boolean valid = true;
		if(config.isRemoveNumbers()) {
			valid = !NumberUtils.isNumber(token);
		}
		return valid && (!config.getStopwords().contains(token));
	}
	
	
	
	
	/**
	 * Gets the contents of the title HTML tag
	 * @param document Origin document
	 * @return Title contents
	 */
	private String getTitle(Document document) {
		return xmlUtils.executeXPath(document, ".//title", String.class);
	}
	
	
	
	
	/**
	 * Gets the contents of the HTML meta tags
	 * @param document Origin document
	 * @return List with the meta tags contents
	 */
	private List<Entry<String, String>> getMetaTags(Document document) {
		List<Entry<String, String>> result = new ArrayList<Entry<String,String>>();
		NodeList nodeList = xmlUtils.executeXPath(document, ".//meta", NodeList.class);
		for(Node node : new IterableNodeList(nodeList)) {
			Node content = node.getAttributes().getNamedItem("content");
			if((content != null) && (StringUtils.isNotBlank(content.getTextContent()))) {
				String nameText = "";
				
				for(int i = 0; i < node.getAttributes().getLength(); i++) {
					Node attribute = node.getAttributes().item(i);
					if(!attribute.getNodeName().equalsIgnoreCase("content")) continue;
					nameText = attribute.getTextContent();
					
					if((attribute.getNodeName().equalsIgnoreCase("name")) || (attribute.getNodeName().equalsIgnoreCase("property"))) {
						break;
					}
				}
				
				result.add(new MutablePair<String, String>(MessageFormat.format("{0}:{1}", META_PREFIX_KEY, nameText), content.getTextContent().trim()));
			}
		}
		
		return result;
	}
	
	
	
	
	/**
	 * Gets the contents of the body HTML tag
	 * @param document Origin document
	 * @return Body contents
	 */
	private String getBody(Document document) {
		StringBuilder result = new StringBuilder();
		NodeList nodeList = xmlUtils.executeXPath(document, ".//body", NodeList.class);
		for(Node node : new IterableNodeList(nodeList)) {
			String content = node.getTextContent();
			if(StringUtils.isNotBlank(content)) {
				result.append(content.replaceAll("\\s+", " ").trim());
			}
		}
		
		return result.toString();
	}
	
	
	
	
	/**
	 * Remove all the script nodes from the document
	 * @param document Origin document
	 */
	private void removeScripts(Document document) {
		NodeList nodeList = xmlUtils.executeXPath(document, ".//script|.//style", NodeList.class);
		for(Node node : new IterableNodeList(nodeList)) {
			node.getParentNode().removeChild(node);
		}
	}
	
	
	
	
	/**
	 * Gets the contents of the given DOC Document
	 * @param document Origin document
	 * @return List with the text contents
	 */
	private List<Entry<String, String>> getDOCText(Document document) {
		Node rootNode = xmlUtils.executeXPath(document, ".//" + DOCSampledDocument.ROOT_NAME, Node.class);
		List<Entry<String, String>> result = new ArrayList<Entry<String,String>>();
		for(Node node : new IterableNodeList(rootNode.getChildNodes())) {
			String content = node.getTextContent();
			if(StringUtils.isNotBlank(content)) {
				result.add(new MutablePair<String, String>(MessageFormat.format("{0}:{1}", DOC_PREFIX_KEY, node.getNodeName()), content.replaceAll("\\s+", " ").trim()));
			}
		}
		
		return result;
	}
	
	
	
	
	/**
	 * Gets the contents of the given PDF Document
	 * @param document Origin document
	 * @return List with the text contents
	 */
	private List<Entry<String, String>> getPDFText(Document document) {
		Node rootNode = xmlUtils.executeXPath(document, ".//" + PDFSampledDocument.ROOT_NAME, Node.class);
		List<Entry<String, String>> result = new ArrayList<Entry<String,String>>();
		for(Node node : new IterableNodeList(rootNode.getChildNodes())) {
			String content = node.getTextContent();
			if(StringUtils.isNotBlank(content)) {
				result.add(new MutablePair<String, String>(MessageFormat.format("{0}:{1}", PDF_PREFIX_KEY, node.getNodeName()), content.replaceAll("\\s+", " ").trim()));
			}
		}
		
		return result;
	}
	
	
	
	
	/**
	 * Gets the contents of the given XPath expressions
	 * @param document Origin document
	 * @param expressions XPath expressions
	 * @return List with the text contents
	 */
	private List<Entry<String, String>> getExtraText(Document document, String expression) {
		List<Entry<String, String>> result = new ArrayList<Entry<String,String>>();
		NodeList nodeList = xmlUtils.executeXPath(document, expression, NodeList.class);
		
		for(Node node : new IterableNodeList(nodeList)) {
			String content = node.getTextContent();
			if(StringUtils.isNotBlank(content)) {
				result.add(new MutablePair<String, String>(expression, content.replaceAll("\\s+", " ").trim()));
			}
		}
		
		return result;
	}
	
	
	
	
	/**
	 * Gets the contents of the given BibTeX reference based on the XPath expressions
	 * @param document Origin document
	 * @param expressions XPath expressions
	 * @return List with the text contents
	 */
	private List<Entry<String, String>> getBibTeX(Document document, String expression) {
		List<Entry<String, String>> result = new ArrayList<Entry<String,String>>();
		NodeList nodeList = xmlUtils.executeXPath(document, expression, NodeList.class);
		
		try
		{
			for(Node node : new IterableNodeList(nodeList)) {
				String content = node.getTextContent();
				if(StringUtils.isNotBlank(content)) {
					int commaIndex = content.indexOf(',');
					String start = content.substring(0, content.indexOf('{')+1);
					String end = content.substring(commaIndex);
					
					content = start+"docid"+end;
					
					BibTeXDatabase bibtexDB = bibtexParser.parse(new StringReader(content));
					Collection<BibTeXEntry> entries = bibtexDB.getEntries().values();
					for(BibTeXEntry entry : entries){
						for(Entry<Key, Value> data : entry.getFields().entrySet()) {
							String key = data.getKey().getValue().trim().toLowerCase();
							
							if(!config.getIgnoreBibTeX().contains(key)) {
								result.add(new MutablePair<String, String>(MessageFormat.format("{0}:{1}", BIBTEX_PREFIX_KEY, key), data.getValue().toUserString().replaceAll("\\s+", " ").trim()));
							}
						}
					}
				}
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
}
