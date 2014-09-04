package aggregator.verticalselection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PowTermQuery;
import org.apache.lucene.search.ProductBooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.similarities.Similarity;

import aggregator.beans.VerticalCollection;
import aggregator.beans.VerticalCollection.VerticalCollectionData;
import aggregator.sampler.indexer.AbstractSamplerIndexer;
import aggregator.util.CommonUtils;

public class UiSDocumentCentricModel extends UiSSelectionModel {

	/**
	 * Default Constructor
	 * @param collection Vertical Collection
	 */
	public UiSDocumentCentricModel(VerticalCollection collection) {
		super(collection);
	}

	@Override
	protected Query prepareQuery(String queryString) {
		QueryParser parser = new QueryParser(CommonUtils.LUCENE_VERSION, AbstractSamplerIndexer.INDEX_CONTENTS_FIELD, analyzer);
		parser.setDefaultOperator(Operator.AND);
		Query query = parser.createBooleanQuery(AbstractSamplerIndexer.INDEX_CONTENTS_FIELD, queryString);
		if(query instanceof BooleanQuery) {
			query = new ProductBooleanQuery((BooleanQuery)query);
		} else if(query instanceof TermQuery) {
			query = new PowTermQuery(((TermQuery) query).getTerm(), 1);
		} else {
			log.warn("Not decided what to do with " + query.getClass().getSimpleName());
		}
		
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
				double verticalScore = 0;
				
				// P(q|c) = sum(P(d|c) * score)
				for(CSIDocument document : data.getValue()) {
					verticalScore += (getPdc(verticalData) * document.getScore());
				}
				
				// Document Centric Model = P(q|c) * P(c) 
				verticalScore *= getCollectionPrior(verticalData);
				if(verticalScore > 0) {
					result.add(new ImmutablePair<String, Double>(data.getKey(), verticalScore));
				}
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
