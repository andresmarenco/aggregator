package aggregator.verticalselection;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.UiSDocumentCentricQuery;
import org.apache.lucene.search.PQ;
import org.apache.lucene.search.PowTermQuery;
import org.apache.lucene.search.ProductBooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;

import aggregator.beans.VerticalCollection;
import aggregator.beans.VerticalCollection.VerticalCollectionData;
import aggregator.sampler.indexer.AbstractSamplerIndexer;
import aggregator.util.BigFunctions;
import aggregator.util.CommonUtils;

public class UiSDocumentCentricModel extends UiSSelectionModel {

	/**
	 * Default Constructor
	 * @param collection Vertical Collection
	 */
	public UiSDocumentCentricModel(VerticalCollection collection) {
		super(collection);
	}
	
	
//	public UiSDocumentCentricModel(VerticalCollection collection, Directory index) {
//		super(collection);
//		
//		try
//		{
//			if(this.index != null) { this.index.close(); }
//			if(this.ireader != null) { this.ireader.close(); }
//			
//			this.index = index;
//			this.ireader = DirectoryReader.open(index);
//		}
//		catch(Exception ex) {
//			log.error(ex.getMessage(), ex);
//		}
//	}

	@Override
	protected Query prepareQuery(String queryString) {
		QueryParser parser = new QueryParser(CommonUtils.LUCENE_VERSION, AbstractSamplerIndexer.INDEX_CONTENTS_FIELD, analyzer);
		parser.setDefaultOperator(Operator.AND);
		Query query = parser.createBooleanQuery(AbstractSamplerIndexer.INDEX_CONTENTS_FIELD, queryString);
		query = new UiSDocumentCentricQuery(query, collection);
		
//		if(query instanceof BooleanQuery) {
////			query = new PQ((BooleanQuery)query);
//			query = new DocumentCentricQuery((BooleanQuery)query);
//		} else if(query instanceof TermQuery) {
//			query = new PowTermQuery(((TermQuery) query).getTerm(), 1);
//		} else {
//			log.warn("Not decided what to do with " + query.getClass().getSimpleName());
//		}
		
//		return new UiSDocumentCentricScoreQuery(query, collection);
		
		return query;
	}

	@Override
	protected List<Entry<String, Double>> executeModel(String queryString) {
		List<Entry<String, Double>> result = new ArrayList<Map.Entry<String,Double>>();
		try
		{
			CSIResultData csiResult = this.searchCSI(queryString);
			for(Map.Entry<String, CSIVertical> data : csiResult.getVerticals().entrySet()) {

				VerticalCollectionData verticalData = collection.getVerticalData(data.getKey());
//				System.out.println(data.getKey() + "  " + data.getValue() + "  " + getPdc(verticalData));
//				double pdc = getPdc(verticalData);
				
				double verticalScore = 0;
				
				// P(q|c) = sum(P(d|c) * score)
				for(CSIDocument document : data.getValue()) {
//					System.out.println("  " + document.getScore());
					
//					verticalScore += (pdc * document.getScore());
					verticalScore += document.getScore();
				}
//				
//				System.out.println("    = " + verticalScore);
				// 70,76051713
				// Document Centric Model = P(q|c) * P(c) 
				verticalScore *= getCollectionPrior(verticalData);
//				BigDecimal.ONE.
				
//				System.out.println("       = " + verticalScore + "  ln = " + Math.log(verticalScore));
//				System.out.println("       = " + verticalScore);
				
//				if(verticalScore < 0) {
//					String score = Double.toString(verticalScore);
//					verticalScore = Double.valueOf(score.substring(0, score.indexOf("E"))) - 1000;
//					
//					
//				} else {
//					verticalScore = Math.log(verticalScore);
//				}
//				
//				System.out.println("       log = " + verticalScore);
				
				
				result.add(new ImmutablePair<String, Double>(data.getKey(), verticalScore));
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return result;
	}
	
	@Override
	public Similarity getSimilarityModel() {
		return new UiSDocumentCentricSimilarity(collection);
	}

	@Override
	public String getModelCodeName() {
		return "UiSDC";
	}
	
}
