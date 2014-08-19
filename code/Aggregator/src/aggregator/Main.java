package aggregator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
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
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXParser;
import org.jbibtex.BibTeXString;
import org.jbibtex.Key;
import org.jbibtex.Value;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import aggregator.beans.PDFSampledDocument;
import aggregator.beans.QueryResult;
import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;
import aggregator.beans.VerticalCategory;
import aggregator.beans.VerticalConfig;
import aggregator.beans.XMLSampledDocument;
import aggregator.dataaccess.ConnectionManager;
import aggregator.dataaccess.DirectConnectionManager;
import aggregator.dataaccess.VerticalDAO;
import aggregator.sampler.AbstractSampler;
import aggregator.sampler.analysis.DocumentAnalysis;
import aggregator.sampler.analysis.sizeestimation.CaptureHistory;
import aggregator.sampler.analysis.sizeestimation.SampleResample;
import aggregator.sampler.indexer.AbstractSamplerIndexer;
import aggregator.sampler.indexer.LuceneSamplerIndexer;
import aggregator.sampler.output.SampledDocumentOutput;
import aggregator.sampler.output.TokensLogFile;
import aggregator.sampler.parser.HTMLSamplerParser;
import aggregator.sampler.parser.SamplerParser;
import aggregator.util.CommonUtils;
import aggregator.util.IterableNodeList;
import aggregator.util.XMLUtils;
import aggregator.util.analysis.AggregatorAnalyzer;
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
//			List<QueryResult> result = wrapper.executeQuery("10.1073");
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
////				aso.outputDocument(wrapper.downloadDocument(r));
////				break;
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
//			String text = "This is the text to be indexed 124 p and other things.";
////			
//			Analyzer analyzer = new AggregatorAnalyzer(Version.LUCENE_4_9);
//			TokenStream stream  = analyzer.tokenStream(null, new StringReader(text));
//		    stream.reset();
//		    while (stream.incrementToken()) {
//		    	
//		    	System.out.println(stream.getAttribute(CharTermAttribute.class).toString());
//		    }
//		    
//		    stream.close();
//		    analyzer.close();
		    
		    
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
			
			
			
//			SampledDocument<?> xml = new XMLSampledDocument(FileSystems.getDefault().getPath("/home/andres/aggregator/sample/FW13-sample-docs/e009/5010_02.html"));
//System.out.println(xml.serialize());			
			
			
			
			
//			
////			SampledDocument<?> xml = new XMLSampledDocument(FileSystems.getDefault().getPath("/home/andres/aggregator/sample/arxiv/000000_20140727042350.html"));
////			AbstractSamplerIndexer asi = new LuceneSamplerIndexer(vertical);
////			
////			asi.tokenize(xml);
//			
////			AbstractSampler as = AbstractSampler.newInstance(vertical);
////			as.execute();
//			
//			VerticalDAO dao = new VerticalDAO(DirectConnectionManager.getInstance());
//			Vertical vertical = dao.loadVertical("msacademic");
//			
////			VerticalWrapperController vw = VerticalWrapperController.getInstance();
////			VerticalConfig vc = vw.getVerticalConfig(vertical);
//			
//			DocumentAnalysis docAnalysis = new DocumentAnalysis(vertical);
//			docAnalysis.analyzeHTMLSample("FW13-sample-docs/e010");
			
//			CaptureHistory ch = new CaptureHistory(vertical);
//			ch.estimateSize("FW13-sample-docs/e002", "20140818221616");
			
			
			
			
//			AbstractSamplerIndexer samplerIndexer = AbstractSamplerIndexer.newInstance(vertical);
//			System.out.println("START");
//			for(String t : samplerIndexer.tokenize(xml)) {
//				System.out.println(t);
//			}
			
////
//			HTMLSamplerParser sp = new HTMLSamplerParser(vc.getIndexParserConfig());
//////			
//			for(Entry<String,String> entry : sp.parseDocument(xml)) {
//				System.out.println(entry.getKey() + "   " + entry.getValue());
//			}
////			
//			
//			String pathname = "/home/andres/aggregator/analysis/FW13-sample-docs/e009/e009_{timestamp}.{docType}".replace("{timestamp}", CommonUtils.getTimestampString());
//		
//			try(DocumentAnalysis docAnalysis = new DocumentAnalysis(vertical)) {
//				docAnalysis.open(pathname);
//				
//				Files.walk(Paths.get("/home/andres/aggregator/sample/FW13-sample-docs/e009")).forEach(filePath -> {
//				    if (Files.isRegularFile(filePath) && filePath.getFileName().toString().endsWith(".html")) {
//				    	System.out.println("Analyzing " + filePath);
//			    		SampledDocument<?> doc = new XMLSampledDocument(filePath);
//			    		docAnalysis.analyzeDocument(doc);
//				    }
//				});
//				
//			}
//			
//			String bib = "@article{callan2001,"
//					+ "author = {Callan, Jamie and Connell, Margaret},"
//					+ "title = {Query-based Sampling of Text Databases},"
//					+ "journal = {ACM Transactions on Information Systems (TOIS)},"
//					+ "issue_date = {April 2001},"
//					+ "volume = {19},"
//					+ "number = {2},"
//					+ "month = apr,"
//					+ "year = {2001},"
//					+ "issn = {1046-8188},"
//					+ "pages = {97--130},"
//					+ "numpages = {34},"
//					+ "doi = {10.1145/382979.383040},"
//					+ "acmid = {383040},"
//					+ "publisher = {ACM},"
//					+ "address = {New York, NY, USA},"
//					+ "keywords = {distributed information retrieval, query-based sampling, resource ranking, resource selection, server selection}}";
//			
//
//			
			
//			String bib = "@TechReport{Menard90, author = \"Claude Menard\", year = \"1990\", title = \"{L}'\'{e}conomie des organisations\", type = \"Collection Rep\\{e}res, n 86\", institution = \"La D\'{e}couverte, Paris\", } ";
//			
//			BibTeXParser btp = new BibTeXParser() {
//				@Override
//				public void checkCrossReferenceResolution(org.jbibtex.Key key,
//						BibTeXEntry entry) {
//					
//				}
//				
//				@Override
//				public void checkStringResolution(org.jbibtex.Key key,
//						BibTeXString string) {
//				}
//			};
//			BibTeXDatabase btd = btp.parse(new StringReader(bib));
//			
//			Collection<org.jbibtex.BibTeXEntry> entries = btd.getEntries().values();
//			for(org.jbibtex.BibTeXEntry entry : entries){
////			    org.jbibtex.Value value = entry.getField(org.jbibtex.BibTeXEntry.KEY_TITLE);
////			    if(value == null){
////			        continue;
////			    }
////
////			    // Do something with the title value
//				
//				for(Entry<Key, Value> data : entry.getFields().entrySet()) {
//					System.out.println(data.getKey() + "  " + data.getValue().toUserString());
//				}
//			}
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

}
