package aggregator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import aggregator.beans.QueryResult;
import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;
import aggregator.dataaccess.ConnectionManager;
import aggregator.dataaccess.DirectConnectionManager;
import aggregator.dataaccess.VerticalDAO;
import aggregator.sampler.output.FileSamplerOutput;
import aggregator.sampler.output.AbstractSamplerOutput;
import aggregator.sampler.output.SamplerOutputController;
import aggregator.util.CommonUtils;
import aggregator.util.IterableNodeList;
import aggregator.util.XMLUtils;
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
			
			
			VerticalDAO dao = new VerticalDAO(DirectConnectionManager.getInstance());
			Vertical vertical = dao.loadVertical("ccsb");

			SamplerOutputController soc = new SamplerOutputController();
			AbstractSamplerOutput output = soc.newSamplerOutput(vertical);
			
			
			VerticalWrapperController wc = VerticalWrapperController.getInstance();
			AbstractVerticalWrapper wrapper = wc.createVerticalWrapper(vertical);
			List<QueryResult> result = wrapper.executeQuery("xpath");
			
			output.openResources();
			
//			SampledDocument<?> doc = wrapper.downloadDocument(result.get(0));
//			System.out.println(doc.serialize());
			
			System.out.println("RESULTS!!");
			for(QueryResult r : result) {
				System.out.println("\n\nID: " + r.getId());
				System.out.println("TITLE: " + r.getTitle());
				System.out.println("SUMMARY: " + r.getSummary());
				System.out.println("INFO: " + r.getInfo());
				
				System.out.println("AUTHORS:");
				for(String a : r.getAuthors()) {
					System.out.println(a);
				}
				
				
				System.out.println("KEYWORDS:");
				for(String a : r.getKeywords()) {
					System.out.println(a);
				}
			
//				output.outputDocument(wrapper.downloadDocument(r));
//				break;
			}
			
			
			output.closeResources();
			
			
			
//			Document doc = (Document) wrapper.authenticate();
//			
//			System.out.println(XMLUtils.serializeDOM(doc));
//			
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

}
