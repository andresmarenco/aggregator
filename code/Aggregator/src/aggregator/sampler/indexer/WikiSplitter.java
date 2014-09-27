package aggregator.sampler.indexer;

import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aggregator.beans.WikiEntry;
import aggregator.beans.WikiEntry.WikiEntryRevision;
import aggregator.dataaccess.PooledConnectionManager;
import aggregator.dataaccess.WikiDAO;
import aggregator.util.FileWriterHelper;

public class WikiSplitter {

	private WikiDAO wikiDAO;
	private Log log = LogFactory.getLog(WikiSplitter.class);
	private static final int SIZE_PART = 1000000;
	private static final String WIKI_DUMP_FILENAME = "/mnt/data/enwiki-latest-pages-articles.xml";
	private static final String ROOT_TAG = "mediawiki";
	private int currentPart;
	private int currentPages;
	private FileWriterHelper output = null;
	
	
	public WikiSplitter() {
		this.wikiDAO = new WikiDAO(PooledConnectionManager.getInstance());
	}
	
	private void initOutput() {
		currentPages = 0;
		
		this.closeOutput();
		
		output = new FileWriterHelper(WIKI_DUMP_FILENAME + "." + StringUtils.leftPad(String.valueOf(currentPart++), 3, '0'));
		output.open(false);
		
		output.writeLine(MessageFormat.format("<{0}>", ROOT_TAG));
	}
	
	
	private void closeOutput() {
		if(output != null) {
			output.writeLine(MessageFormat.format("</{0}>", ROOT_TAG));
			output.close();
		}
	}
	
	private void writeEntry(WikiEntry entry) {
		currentPages++;
		output.writeLine(" <page>");
		output.writeLine("  <id>" + String.valueOf(entry.getId()) + "</id>");
		output.writeLine("  <ns>" + String.valueOf(entry.getNamespace()) + "</ns>");
		output.writeLine("  <title>" + StringEscapeUtils.escapeXml10(entry.getTitle()) + "</title>");
		
		output.writeLine("  <revision>");
		WikiEntryRevision revision = entry.getLastRevision();
		if(revision != null) {
			output.writeLine("   <id>" + revision.getId() + "</id>");
			output.writeLine("   <text>" + StringEscapeUtils.escapeXml10(revision.getText()) + "</text>");
		}
		
		output.writeLine("  </revision>");
		
		output.writeLine(" </page>");
		
		if(currentPages >= SIZE_PART) {
			initOutput();
		}
	}
	
	
	public void execute() {
		currentPart = 0;
		initOutput();
		
		// XML Dump of Wiki
		log.info("Reading Wikipedia dump...");
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
//		StringEscapeUtils.
		try(InputStream in = new FileInputStream(WIKI_DUMP_FILENAME)) {
			XMLStreamReader reader = inputFactory.createXMLStreamReader(in);
			
			WikiEntry entry = null;
			WikiEntryRevision revision = null;
			
			while(reader.hasNext()) {
				int event = reader.next();
				
				switch(event) {
				case XMLStreamConstants.START_ELEMENT: {
					String tagName = reader.getLocalName();
					if("page".equals(tagName)) {
						entry = new WikiEntry();
					} else if("revision".equals(tagName)) {
						revision = entry.newEntryRevision();
					} else if("redirect".equals(tagName)) {
						entry.setRedirect(reader.getAttributeValue(0));
					} else if("text".equals(tagName)) {
						revision.setText(reader.getElementText());
					} else if("id".equals(tagName)) {
						if(revision == null) {
							entry.setId(Long.parseLong(reader.getElementText()));
						} else {
							revision.setId(Long.parseLong(reader.getElementText()));
						}
					} else if("ns".equals(tagName)) {
						entry.setNamespace(Integer.parseInt(reader.getElementText()));
					} else if("title".equals(tagName)) {
						entry.setTitle(reader.getElementText());
					}
					break;
				}
				
				case XMLStreamConstants.END_ELEMENT: {
					String tagName = reader.getLocalName();
					if("revision".equals(tagName)) {
						entry.getRevisions().add(revision);
						revision = null;
					} else if("page".equals(tagName)) {
//						log.info(MessageFormat.format("Writing Wikipedia entry {0} ({1})", entry.getTitle(), String.valueOf(entry.getId())));
//						writeEntry(entry);
						
//						if(entry.getId() > 37574527) {
//						
							if(!entry.isRedirect()) {
								switch(entry.getNamespace()) {
								case WikiEntry.NAMESPACE_ARTICLE: {
									boolean write = true;
									for(String category : entry.getLastRevision().getCategories()) {
										if(category.startsWith("Disambiguation ") || category.startsWith("Lists ") ||
												category.startsWith("List-Class ") || category.equals("Lists") ||
												category.startsWith("Template-Class ") || category.startsWith("Templates ") || category.equals("Templates")) {
											write = false;
											break;
										}
									}
									
									if(write) {
										log.info(MessageFormat.format("Writing Wikipedia entry {0} ({1})", entry.getTitle(), String.valueOf(entry.getId())));
										writeEntry(entry);
									} else {
//										log.info(MessageFormat.format("Adding Wikipedia entry {0} ({1})", entry.getTitle(), String.valueOf(entry.getId())));
//										wikiDAO.insertEntry(entry);
									}
									
									break;
								}
								
								case WikiEntry.NAMESPACE_CATEGORY: {
									log.info(MessageFormat.format("Writing Wikipedia entry {0} ({1})", entry.getTitle(), String.valueOf(entry.getId())));
									writeEntry(entry);
									break;
								}
								
								default: {
//									log.info(MessageFormat.format("Adding Wikipedia entry {0} ({1})", entry.getTitle(), String.valueOf(entry.getId())));
//									wikiDAO.insertEntry(entry);
								}
								
								}
							} else {
//								log.info(MessageFormat.format("Adding Wikipedia redirect entry {0} ({1})", entry.getTitle(), String.valueOf(entry.getId())));
//								wikiDAO.insertEntry(entry);
							}
//						} else {
//							log.info(MessageFormat.format("Ignoring entry {0} ({1})", entry.getTitle(), String.valueOf(entry.getId())));
//						}
					}
					break;
				}
				
				}
			}
			
			
			this.closeOutput();
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
	}
}
