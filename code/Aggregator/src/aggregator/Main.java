package aggregator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import aggregator.beans.QueryResult;
import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;
import aggregator.beans.XMLSampledDocument;
import aggregator.dataaccess.ConnectionManager;
import aggregator.dataaccess.DirectConnectionManager;
import aggregator.dataaccess.VerticalDAO;
import aggregator.sampler.AbstractSampler;
import aggregator.sampler.indexer.AbstractSamplerIndexer;
import aggregator.sampler.indexer.LuceneSamplerIndexer;
import aggregator.sampler.output.FileSamplerOutput;
import aggregator.sampler.output.AbstractSamplerOutput;
import aggregator.sampler.parser.HTMLSamplerParser;
import aggregator.sampler.parser.SamplerParser;
import aggregator.util.CommonUtils;
import aggregator.util.IterableNodeList;
import aggregator.util.XMLUtils;
import aggregator.util.delay.RandomDelay;
import aggregator.verticalwrapper.AbstractVerticalWrapper;
import aggregator.verticalwrapper.VerticalWrapperController;

public class Main {
	
	public static void main(String... args) {
		CommonUtils.loadDefaultProperties();
		try
		{
			/*SampledDocument doc = new SampledDocument();
			doc.setTitle("TITLE");
			doc.setText("TEXT");
			
			SampledDocument doc2 = new SampledDocument();
			doc2.setTitle("TITLE2");
			doc2.setText("TEXT2");
			
			SamplerOutput samplerOutput = new FileSamplerOutput("vertical1");
			samplerOutput.openResources();
			samplerOutput.outputDocument(doc);
			samplerOutput.outputDocument(doc2);
			samplerOutput.closeResources();*/
			
			/*ConnectionManager cm = DirectConnectionManager.getInstance();
			cm.getConnection().close();*/
			
//			SamplerOutputController soc = new SamplerOutputController();
//			soc.newSamplerOutput("vertical1");
			
//			String xml = "<aa><dd><div class='meta'>"
//					+ "<div class='list-title'>"
//					+ "<span class='descriptor'>Title:</span>"
//					+ "(Uniform) Convergence of Twisted Ergodic Averages"
//					+ "</div>"
//					+ "<div class='list-authors'>"
//					+ "<span class='descriptor'>Authors:</span>"
//					+ "<a href='/find/math/1/au:+Eisner_T/0/1/0/all/0/1'>Tanja Eisner</a>"
//					+ ","
//					+ "<a href='/find/math/1/au:+Krause_B/0/1/0/all/0/1'>Ben Krause</a>"
//					+ "</div>"
//					+ "<div class='list-comments'>"
//					+ "<span class='descriptor'>Comments:</span>"
//					+ "28 pages</div>"
//					+ "<div class='list-subjects'>"
//					+ "<span class='descriptor'>Subjects:</span>"
//					+ "<span class='primary-subject'>Dynamical Systems (math.DS)</span>"
//					+ "; Classical Analysis and ODEs (math.CA); Number Theory (math.NT)"
//					+ "</div></div></dd></aa>";
			
//			
//			File file = new File("/home/andres/test.xml");
//			
//			BufferedReader br = new BufferedReader(new FileReader("/home/andres/test.xml"));
//			StringBuilder text = new StringBuilder();
//			String line = br.readLine();
//			
//			while(line != null) {
//				text.append(line);
//				line = br.readLine();
//			}
//			
//			br.close();
			
//			System.out.println(text.toString());
			
			
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder db = dbf.newDocumentBuilder();
//////			Document doc = db.parse(new InputSource(new StringReader("<div class=\"list-title\"><span class=\"descriptor\">Title:</span>  Tensor categories of endomorphisms and inclusions of von Neumann  algebras</div>")));
//////			Document doc = db.parse(new InputSource(new StringReader("<div class='list-title'><span class='descriptor'>Title:</span>  Tensor categories of endomorphisms and inclusions of von Neumann  algebras</div>")));
////			
////			
//			Document doc = db.parse(new File("/home/andres/test.xml"));
//////			
////////			Document doc = db.parse(new InputSource(new StringReader(text.toString())));
////////			Document doc = db.parse(new InputSource(new StringReader(xml)));
//////			
//////			
//////			
//////////			
//////			
////////			
////////			fis.close();
//			XPathFactory xpf = XPathFactory.newInstance();
//			XPath xp = xpf.newXPath();
////			javax.xml.xpath.XPathExpression xpe = xp.compile("string(.//td[@class='biblinks']/a[contains(., 'BibTeX')]/@href)");
//			javax.xml.xpath.XPathExpression xpe = xp.compile(".//span[@class='b_title']/preceding-sibling::node()");
//			
//			
////			NodeList nodeList = XMLUtils.executeXPath(doc, expression, NodeList.class);
//			
////			Node nodeList = (Node)xpe.evaluate(doc, XPathConstants.NODE);
//			
//			Object n = xpe.evaluate(doc);
//			System.out.println(n.getClass());
//			
//			Class<?> returnType = String.class;
//			
//			if(returnType == String.class) {
//				StringBuilder builder = new StringBuilder();
//				
//				
////				for(Node node : nodeList) {
////					String content = node.getTextContent();
////					if(StringUtils.isNotBlank(content)) {
////						builder.append(content).append(" ");
////					}
////				}
////				
////				result = returnType.cast(builder.toString().replaceAll("\\s+", " "));
//			} if(returnType == List.class) {
//				List<String> resultList = new ArrayList<String>();
////				for(Node node : nodeList) {
////					String content = node.getTextContent();
////					if(StringUtils.isNotBlank(content)) {
////						resultList.add(content);
////					}
////				}
////				
////				result = returnType.cast(resultList);
//			}
//			
//			
			
//			
//			NodeList nodeList = (NodeList)xpe.evaluate(doc, XPathConstants.NODESET);
//			for(Node n : new IterableNodeList(nodeList)) {
//				System.out.println(n.getTextContent());
////				if(n instanceof Element)
////					System.out.println(XMLUtils.serializeDOM((Element)n, false, true));
////				else
////					System.out.println(n);
//			}
			
			
//			Object textNode = xpe.evaluate(doc);
//			System.out.println(textNode);
			
			
//			Node node = (Node)xpe.evaluate(doc, XPathConstants.NODE);
//			System.out.println(XMLUtils.serializeDOM((Element)node, false, true));
			
//			System.out.println(r);
			
			
//			
////			System.out.println(XMLUtils.serializeDOM(doc));
//			NodeList list = (NodeList) xpe.evaluate(doc, XPathConstants.NODESET);
////			
//			Node node = list.item(0);
//			xpe = xp.compile("substring-after(.//div[@class='list-title'], 'Title:')");
//			Object r = xpe.evaluate(node);
//			
//			
////			Node n = (Node)xpe.evaluate(node, XPathConstants.NODE);
////						
////			System.out.println(XMLUtils.serializeDOM((Element)n, false, true));
////			
////			xpe = xp.compile("/self::*");
////			Object r = xpe.evaluate(doc);
////			
//			System.out.println(r);
//			
			
			
			
//			Document nd = db.newDocument();
//			nd.appendChild(nd.adoptNode(node.cloneNode(true)));
//			
//			System.out.println("ND:\n" + XMLUtils.serializeDOM(nd));
//			
//			xpe = xp.compile(".//div[@class='list-title']/text()");
//			Object n = xpe.evaluate(nd);
//			System.out.println(n);
			
//			Node n = (Node)xpe.evaluate(nd, XPathConstants.NODE);
			
//			System.out.println(XMLUtils.serializeDOM((Element)n, false, true));
			
//			Object n = xpe.evaluate(node);
//			System.out.println("TEXT " + n.getTextContent());
//			System.out.println("------------" + n.getNodeValue());
			
			
//			javax.xml.xpath.XPathExpression xpe = xp.compile(".//div[@class='list-title']/text()");
			
//			NodeList nl = (NodeList) xpe.evaluate(doc, XPathConstants.NODESET);
//			
//			System.out.println(nl.item(0).getTextContent());
			
//			Object n = xpe.evaluate(doc);
//			System.out.println(n);
			
			
//			System.out.println(XMLUtils.serializeDOM(doc));
			
//			file.
			
			
//			
			
			
//			VerticalDAO dao = new VerticalDAO(DirectConnectionManager.getInstance());
//			Vertical vertical = dao.loadVertical("arxiv");
//
//			AbstractSamplerOutput aso = AbstractSamplerOutput.newSamplerOutput(vertical);
//			
//			
//			VerticalWrapperController wc = VerticalWrapperController.getInstance();
//			AbstractVerticalWrapper wrapper = wc.createVerticalWrapper(vertical);
//			List<QueryResult> result = wrapper.executeQuery("math");
//			
//			aso.open();
////			
//////			SampledDocument<?> doc = wrapper.downloadDocument(result.get(0));
//////			System.out.println(doc.serialize());
////			
//			System.out.println("RESULTS!!");
//			for(QueryResult r : result) {
//				System.out.println("\n\nID: " + r.getId());
//				System.out.println("TITLE: " + r.getTitle());
//				System.out.println("SUMMARY: " + r.getSummary());
//				System.out.println("INFO: " + r.getInfo());
//				
//				System.out.println("AUTHORS:");
//				for(String a : r.getAuthors()) {
//					System.out.println(a);
//				}
//				
//				
//				System.out.println("KEYWORDS:");
//				for(String a : r.getKeywords()) {
//					System.out.println(a);
//				}
//			
//				aso.outputDocument(wrapper.downloadDocument(r));
//				break;
//			}
//			
//			
//			aso.close();
			
			
			
//			Document doc = (Document) wrapper.authenticate();
//			
//			System.out.println(XMLUtils.serializeDOM(doc));
//			
			
//			VerticalDAO dao = new VerticalDAO(DirectConnectionManager.getInstance());
//			Vertical vertical = dao.loadVertical("arxiv");
//			
//			VerticalWrapperController wc = VerticalWrapperController.getInstance();
//			AbstractVerticalWrapper wrapper = wc.createVerticalWrapper(vertical);
			
//			SampledDocument<?> xml = new XMLSampledDocument(FileSystems.getDefault().getPath("/home/andres/sample/arxiv/000000_20140723055053.html"));
//			SamplerParser sp = wc.getVerticalConfig(vertical).newIndexParserInstance();
//			
//			for(Map.Entry<String, String> pair : sp.parseDocument(xml)) {
//				System.out.println(pair.getKey() + ":  " + pair.getValue());
//			}
//			
//			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_4_9);
//			Directory directory = new RAMDirectory();
//			
//			String text = "This is the text to be indexed.";
//			
//			TokenStream stream  = analyzer.tokenStream(null, new StringReader(text));
//		    stream.reset();
//		    while (stream.incrementToken()) {
//		    	
//		    	System.out.println(stream.getAttribute(CharTermAttribute.class).toString());
//		    }
//			
//			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_4_9, analyzer);
//		    IndexWriter iwriter = new IndexWriter(directory, config);
//		    
//		    org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
//		    doc.
		    
//		    
//		    doc.add(new Field("fieldname", text, TextField.TYPE_STORED));
//		    iwriter.addDocument(doc);
//		    iwriter.close();
			    
		    // Now search the index:
//		    DirectoryReader ireader = DirectoryReader.open(directory);
//		    IndexSearcher isearcher = new IndexSearcher(ireader);
////		    // Parse a simple query that searches for "text":
//		    QueryParser parser = new QueryParser(Version.LUCENE_4_9, "fieldname", analyzer);
//		    Query query = parser.parse("indexed");
//		    ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
////		    assertEquals(1, hits.length);
//		    System.out.println("TOTAL: " + hits.length);
//		    // Iterate through the results:
//		    for (int i = 0; i < hits.length; i++) {
//		    	 org.apache.lucene.document.Document hitDoc = isearcher.doc(hits[i].doc);
//		    	 System.out.println("This is the text to be indexed. " + hitDoc.get("fieldname"));
////		      assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
//		    }
//		    ireader.close();
//		    directory.close();
			
			
			VerticalDAO dao = new VerticalDAO(DirectConnectionManager.getInstance());
			Vertical vertical = dao.loadVertical("arxiv");
			
//			SampledDocument<?> xml = new XMLSampledDocument(FileSystems.getDefault().getPath("/home/andres/aggregator/sample/arxiv/000000_20140727042350.html"));
//			AbstractSamplerIndexer asi = new LuceneSamplerIndexer(vertical);
//			
//			asi.tokenize(xml);
			
			AbstractSampler as = AbstractSampler.newInstance();
			as.execute(vertical);
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

}
