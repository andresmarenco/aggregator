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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiDocsEnum;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PowTermQuery;
import org.apache.lucene.search.ProductBooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.hpsf.Property;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.model.FieldsDocumentPart;
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

import aggregator.beans.DOCSampledDocument;
import aggregator.beans.PDFSampledDocument;
import aggregator.beans.QueryResult;
import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;
import aggregator.beans.VerticalCategory;
import aggregator.beans.VerticalCollection;
import aggregator.beans.VerticalCollection.VerticalCollectionData;
import aggregator.beans.VerticalConfig;
import aggregator.beans.XMLSampledDocument;
import aggregator.dataaccess.ConnectionManager;
import aggregator.dataaccess.DirectConnectionManager;
import aggregator.dataaccess.VerticalDAO;
import aggregator.sampler.AbstractSampler;
import aggregator.sampler.analysis.DatabaseIntermediateResults;
import aggregator.sampler.analysis.DocumentAnalysis;
import aggregator.sampler.analysis.sizeestimation.CaptureHistory;
import aggregator.sampler.analysis.sizeestimation.CollectionFactorEstimation;
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
import aggregator.util.analysis.AggregatorTokenizerAnalyzer;
import aggregator.util.delay.RandomDelay;
import aggregator.verticalselection.BasicMaxVerticalSelection;
import aggregator.verticalselection.BasicMeanVerticalSelection;
import aggregator.verticalselection.BasicSumVerticalSelection;
import aggregator.verticalselection.ExplainSelectionModel;
import aggregator.verticalselection.UiSDocumentCentricModel;
import aggregator.verticalselection.UiSDocumentCentricSimilarity;
import aggregator.verticalselection.RankVerticalSelection;
import aggregator.verticalselection.SizeMaxVerticalSelection;
import aggregator.verticalselection.SizeRankVerticalSelection;
import aggregator.verticalselection.AbstractSelectionModel;
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
			
//			FileInputStream fis = new FileInputStream("/home/andres/aggregator/sample/FW13-sample-docs/e075/5000_06.doc");
//			HWPFDocument doc = new HWPFDocument(fis);
//			WordExtractor word = new WordExtractor(doc);
//			
////			System.out.println(word.getDocSummaryInformation().);
//			for(String s : word.getParagraphText()) {
//				System.out.println(s);
//			}
			
//			for(Property p : word.getDocSummaryInformation().getProperties()) {
//				System.out.println(p.getID() + "  " + p.getValue());
//			}
			
			
			
//			List<String> v = new ArrayList<String>();
//			for(int i = 180; i<=185; i++) {
//				v.add(MessageFormat.format("e{0}", String.valueOf(i)));
//			}
			
//////			
			VerticalDAO dao = new VerticalDAO(DirectConnectionManager.getInstance());
			VerticalCollection collection = dao.loadVerticalCollection("FW13");
			
//			AbstractSamplerIndexer indexer = AbstractSamplerIndexer.newInstance();
//			indexer.storeIndex(MessageFormat.format("{0}-sample-docs", collection.getId()), collection);
				
			AbstractSelectionModel selection = AbstractSelectionModel.newInstance(collection);
//			AbstractSelectionModel explain = new ExplainSelectionModel(selection);
//			explain.execute("swahili dishes");
			
			
			selection.testQueries();
			selection.close();
			
//			dao.updateSampleSize(collection.getId(), "4shared", 10);
			
			
			
//			Analyzer analyzer = new AggregatorAnalyzer(CommonUtils.LUCENE_VERSION);
//			Directory index = new RAMDirectory();
//			IndexWriterConfig config = new IndexWriterConfig(CommonUtils.LUCENE_VERSION, analyzer);
//			config.setSimilarity(new UiSDocumentCentricSimilarity());
//			
//			
//			IndexWriter writer = new IndexWriter(index, config);
//			
//			org.apache.lucene.document.Document indexDoc = new org.apache.lucene.document.Document();
//			indexDoc.add(new TextField(AbstractSamplerIndexer.INDEX_CONTENTS_FIELD,
//					"text document sample data file text document read index cup scissors sting song the police"
//					, Field.Store.NO));
//			indexDoc.add(new StringField(AbstractSamplerIndexer.INDEX_DOC_NAME_FIELD, "d1", Field.Store.YES));
//			indexDoc.add(new StringField(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD, "v1", Field.Store.YES));
//			
//			writer.addDocument(indexDoc);
//			
//			
//			
//			indexDoc = new org.apache.lucene.document.Document();
//			indexDoc.add(new TextField(AbstractSamplerIndexer.INDEX_CONTENTS_FIELD,
//					"song text juice guitar solo paper pencil text title author title many years among sample"
//					, Field.Store.NO));
//			indexDoc.add(new StringField(AbstractSamplerIndexer.INDEX_DOC_NAME_FIELD, "d2", Field.Store.YES));
//			indexDoc.add(new StringField(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD, "v1", Field.Store.YES));
//			
//			writer.addDocument(indexDoc);
//			
//			
//			
//			indexDoc = new org.apache.lucene.document.Document();
//			indexDoc.add(new TextField(AbstractSamplerIndexer.INDEX_CONTENTS_FIELD,
//					"remember test data walk play youtube data data she gave thought seems remember"
//					, Field.Store.NO));
//			indexDoc.add(new StringField(AbstractSamplerIndexer.INDEX_DOC_NAME_FIELD, "d3", Field.Store.YES));
//			indexDoc.add(new StringField(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD, "v1", Field.Store.YES));
//			
//			writer.addDocument(indexDoc);
//			
//			
//			// Vertical 2
//			indexDoc = new org.apache.lucene.document.Document();
//			indexDoc.add(new TextField(AbstractSamplerIndexer.INDEX_CONTENTS_FIELD,
//					"song text juice guitar solo paper pencil text title author tuesday mind mind fine days"
//					, Field.Store.NO));
//			indexDoc.add(new StringField(AbstractSamplerIndexer.INDEX_DOC_NAME_FIELD, "d1", Field.Store.YES));
//			indexDoc.add(new StringField(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD, "v2", Field.Store.YES));
//			
//			writer.addDocument(indexDoc);
//			
//			
//			
//			indexDoc = new org.apache.lucene.document.Document();
//			indexDoc.add(new TextField(AbstractSamplerIndexer.INDEX_CONTENTS_FIELD,
//					"remember test data thought remember sunday late talk cook food food remember"
//					, Field.Store.NO));
//			indexDoc.add(new StringField(AbstractSamplerIndexer.INDEX_DOC_NAME_FIELD, "d2", Field.Store.YES));
//			indexDoc.add(new StringField(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD, "v2", Field.Store.YES));
//			
//			writer.addDocument(indexDoc);
//			
//			
//			
//			indexDoc = new org.apache.lucene.document.Document();
//			indexDoc.add(new TextField(AbstractSamplerIndexer.INDEX_CONTENTS_FIELD,
//					"remember remember sunday late talk cook food food other other other sunday sunday aggregator remember"
//					, Field.Store.NO));
//			indexDoc.add(new StringField(AbstractSamplerIndexer.INDEX_DOC_NAME_FIELD, "d3", Field.Store.YES));
//			indexDoc.add(new StringField(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD, "v2", Field.Store.YES));
//			
//			writer.addDocument(indexDoc);
//			
//			
//			
//			writer.close();
//			
//			
//			
//			DirectoryReader ireader = DirectoryReader.open(index);
//			IndexSearcher searcher = new IndexSearcher(ireader);
//			UiSDocumentCentricSimilarity dcs = new UiSDocumentCentricSimilarity();
//			
//			searcher.setSimilarity(dcs);
//			
////			MultiFields.getTerms(ireader, AbstractSamplerIndexer.INDEX_TEXT_FIELD).
//			
////			System.out.println(MultiFields.getTerms(ireader, AbstractSamplerIndexer.INDEX_TEXT_FIELD));
//			
////			TermsEnum termEnum = MultiFields.getTerms(ireader, AbstractSamplerIndexer.INDEX_TEXT_FIELD).iterator(null);
////			BytesRef bytesRef;
////	        while ((bytesRef = termEnum.next()) != null) {
////	        	System.out.println(bytesRef.utf8ToString());
////	        }
//			
//			
//			
//			System.out.println("\n\n\nQUERYING");
//			
//			
//			QueryParser parser = new QueryParser(CommonUtils.LUCENE_VERSION, AbstractSamplerIndexer.INDEX_CONTENTS_FIELD, analyzer);
//			parser.setDefaultOperator(Operator.AND);
//			
////			QueryParser parser = new MultiFieldQueryParser(CommonUtils.LUCENE_VERSION, new String[] {AbstractSamplerIndexer.INDEX_TEXT_FIELD, AbstractSamplerIndexer.INDEX_SNIPPET_FIELD}, analyzer);
////			Query query = parser.parse("\"sunday late\"");
////			Query query = parser.parse("remember food");
//			
//			
//			
//			
//			Query bq = parser.createBooleanQuery(AbstractSamplerIndexer.INDEX_CONTENTS_FIELD, "remember food food");
////			for(BooleanClause c : ((BooleanQuery)bq).getClauses()) {
////				System.out.println(c.toString());
////			}
//			
////			Query query = new ProductBooleanQuery((BooleanQuery)bq);
//			
//			Query query = new PowTermQuery(new Term(AbstractSamplerIndexer.INDEX_CONTENTS_FIELD, "rememb"), 1);
//			
//			
//			
////			Query query = new PBQ(AbstractSamplerIndexer.INDEX_TEXT_FIELD, "remember");
//			
//			
////			Query query = new TermQuery(new Term(AbstractSamplerIndexer.INDEX_TEXT_FIELD, "rememb"));
//			
////			Query query = bq;
//			
//			
////			query = MultiTermQuery
//			
////			query = new PBQ((BooleanQuery)query);
//			
//			
////			Query query = parser.createPhraseQuery(AbstractSamplerIndexer.INDEX_TEXT_FIELD, "remember food");
//			
//			
//			
////			
////			BooleanQuery query = new BooleanQuery();
////			query.add(new BooleanClause(new TermQuery(new Term(AbstractSamplerIndexer.INDEX_TEXT_FIELD, "rememb")), Occur.SHOULD));
////			query.add(new BooleanClause(new TermQuery(new Term(AbstractSamplerIndexer.INDEX_TEXT_FIELD, "food")), Occur.SHOULD));
////			
//			System.out.println(query.getClass());
//			
////			DocumentCentricModel customQuery = new DocumentCentricModel(query);
////			for(int i = 0; i < ireader.numDocs(); i++) {
//////				org.apache.lucene.document.Document doc = ireader.document(i);
//////				System.out.println(hits[i].doc + "  " + hits[i].score + "  " + doc.get(AbstractSamplerIndexer.INDEX_DOC_NAME_FIELD) + " " + doc.get(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD));
////				
////				System.out.println(searcher.explain(customQuery, i) + "\n\n\n");
////			}
//			
//			
////			org.apache.lucene.search.BooleanScorer
//			ScoreDoc[] hits = searcher.search(query, Integer.MAX_VALUE).scoreDocs;
//			for (int i = 0; i < hits.length; i++) {
//				org.apache.lucene.document.Document doc = ireader.document(hits[i].doc);
//				System.out.println("\n\n\nDOC: " + hits[i].doc + "  " + hits[i].score + "  " + doc.get(AbstractSamplerIndexer.INDEX_DOC_NAME_FIELD) + " " + doc.get(AbstractSamplerIndexer.INDEX_VERTICAL_FIELD));
//				
//				System.out.println(searcher.explain(query, hits[i].doc) + "\n\n\n");
//			}
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
//			Directory index = new NIOFSDirectory(CommonUtils.getIndexPath().resolve(collection.getId()).resolve(AbstractSamplerIndexer.getIndexName()).toFile());
//			DirectoryReader ireader = DirectoryReader.open(index);
//			
//			
//			IndexSearcher searcher = new IndexSearcher(ireader);
//			
//						QueryParser parser;
//			List<String> queriedFields = new ArrayList<String>();
//			
//			if(AbstractSamplerIndexer.isIndexSnippets()) {
//				queriedFields.add(AbstractSamplerIndexer.INDEX_TITLE_FIELD);
//				queriedFields.add(AbstractSamplerIndexer.INDEX_SNIPPET_FIELD);
//			}
//			
//			if(AbstractSamplerIndexer.isIndexDocs()) {
//				queriedFields.add(AbstractSamplerIndexer.INDEX_TEXT_FIELD);
//			}
//			
//			if(queriedFields.size() == 1) {
//				parser = new QueryParser(CommonUtils.LUCENE_VERSION, queriedFields.get(0), analyzer);
//			} else {
//				parser = new MultiFieldQueryParser(CommonUtils.LUCENE_VERSION, queriedFields.toArray(new String[0]), analyzer);
//			}
//			
//			
//			Query query = parser.parse("title science other");
//			searcher.setSimilarity(new DocumentCentricSimilarity());
//			
//			DocumentCentricModel customQuery = new DocumentCentricModel(query);
//
//			ScoreDoc[] hits = searcher.search(query, Integer.MAX_VALUE).scoreDocs;
//
//			System.out.println(query.getClass());
////			System.out.println(ireader.docFreq(new Term(AbstractSamplerIndexer.INDEX_TEXT_FIELD, "titl")));
//			System.out.println(hits.length);
//			for (int i = 0; i < hits.length; i++) {
//			   // iterating over the results
//			   // hits[i].doc gives you the doc
//				
//				System.out.println(hits[i].score);
////				System.out.println(searcher.explain(customQuery, hits[i].doc));
//				System.out.println(searcher.explain(query, 261));
//				break;
//			}
			
			
			
			
			
			
			
//			for(VerticalCollectionData data : collection.getVerticals()) {
////				if(data.getVertical().getId().equalsIgnoreCase("orgprints")) {
//				if(data.getVerticalCollectionId().equalsIgnoreCase("e023")) {
////				if(v.contains(data.getVerticalCollectionId())) {
//					DocumentAnalysis docAnalysis = new DocumentAnalysis(data.getVertical());
//					docAnalysis.analyzeHTMLSample(MessageFormat.format("{0}-sample-docs/{1}", collection.getId(), data.getVerticalCollectionId()));
//				}
//			}
			
//			CollectionFactorEstimation size = new CollectionFactorEstimation(collection);
//			size.estimateSize(MessageFormat.format("{0}-sample-docs", collection.getId()));
			
			
//			AbstractSamplerIndexer indexer = AbstractSamplerIndexer.newInstance();
//			indexer.storeIndex(MessageFormat.format("{0}-sample-docs", collection.getId()), collection);
//			
//			
//			VerticalSelection selection = new SizeRankVerticalSelection(collection);
//			selection.precisionEval(java.nio.file.Paths.get("/home/andres/aggregator/analysis/FW13-eval/docsSnippets/sizerankmodel"),
//					java.nio.file.Paths.get("/home/andres/aggregator/analysis/FW13-QRELS-RS.txt"));
////			
//			selection.testQueries(selection.getModelCodeName());
//			selection.close();
//			
//
////			for(Entry<String,Double> data : selection.execute("world cup")) {
////				System.out.println(data.getKey() + "  " + data.getValue());
////			}
//			
//			selection.close();
			

			
			
			
//			List<Class<? extends VerticalSelection>> models = new ArrayList<Class<? extends VerticalSelection>>();
//			models.add(BasicMaxVerticalSelection.class);
//			models.add(BasicMeanVerticalSelection.class);
//			models.add(BasicSumVerticalSelection.class);
//			models.add(RankVerticalSelection.class);
//			models.add(SizeRankVerticalSelection.class);
//			models.add(SizeMaxVerticalSelection.class);
//			
//			for(Class<? extends VerticalSelection> model : models) {
//				VerticalSelection selection = model.getConstructor(VerticalCollection.class).newInstance(collection);
//				selection.testQueries(selection.getModelCodeName());
//				selection.close();
//			}

			
			
//			// Now search the index:
//			Directory index = new NIOFSDirectory(CommonUtils.getIndexPath().resolve(collection.getId()).toFile());
//		    DirectoryReader ireader = DirectoryReader.open(index);
//		    IndexSearcher isearcher = new IndexSearcher(ireader);
//////		    // Parse a simple query that searches for "text":
//		    QueryParser parser = new QueryParser(Version.LUCENE_4_9, "text", new AggregatorAnalyzer(Version.LUCENE_4_9));
//		    
//		    Query query = parser.parse("Graphene");
//		    ScoreDoc[] hits = isearcher.search(query, null, Integer.MAX_VALUE).scoreDocs;
//////		    assertEquals(1, hits.length);
//		    System.out.println("TOTAL: " + hits.length);
//		    // Iterate through the results:
//		    for (int i = 0; i < hits.length; i++) {
//		    	 org.apache.lucene.document.Document hitDoc = isearcher.doc(hits[i].doc);
//		    	 System.out.println("This is the text to be indexed. " + hits[i].score + "  " + hitDoc.get("docName") + "  " + hitDoc.get("vertical"));
////		      assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
//		    }
//		    ireader.close();
//		    index.close();
			
			
			
			
//			DatabaseIntermediateResults ir = new DatabaseIntermediateResults(dao.loadVertical("ncsu"));
//			ir.addDocumentTerms("doc1", Arrays.asList((new String[] { "t1", "t2" })));
//			ir.addDocumentTerms("doc2", Arrays.asList((new String[] { "t4", "t2", "2f" })));
//			
//			ir.dumpDocumentTerms("FW13-sample-docs/e030/fsd.{docType}");
//			// Get current size of heap in bytes
//			long heapSize = Runtime.getRuntime().totalMemory(); 
//
//			// Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
//			long heapMaxSize = Runtime.getRuntime().maxMemory();
//
//			 // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
//			long heapFreeSize = Runtime.getRuntime().freeMemory(); 
//			
//			System.out.println(heapSize + "  " + heapMaxSize + "  " + heapFreeSize);
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
////			
//			VerticalWrapperController wc = VerticalWrapperController.getInstance();
////			AbstractVerticalWrapper wrapper = wc.createVerticalWrapper(vertical);
//			
//			SampledDocument<?> xml = new PDFSampledDocument(FileSystems.getDefault().getPath("/home/andres/aggregator/sample/FW13-sample-docs/e075/5257_03.pdf"));
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
//			SampledDocument<?> xml = new XMLSampledDocument(FileSystems.getDefault().getPath("/home/andres/aggregator/sample/FW13-sample-docs/e027/5005_04.html"));
//////			AbstractSamplerIndexer asi = new LuceneSamplerIndexer(vertical);
//////			
//////			asi.tokenize(xml);
////			
//////			AbstractSampler as = AbstractSampler.newInstance(vertical);
//////			as.execute();
////			
////			VerticalDAO dao = new VerticalDAO(DirectConnectionManager.getInstance());
//			Vertical vertical = dao.loadVertical("columbus");
//			
//			VerticalWrapperController vw = VerticalWrapperController.getInstance();
//			VerticalConfig vc = vw.getVerticalConfig(vertical);
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
