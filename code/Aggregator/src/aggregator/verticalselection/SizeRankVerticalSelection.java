package aggregator.verticalselection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.lucene.search.Query;

import aggregator.beans.VerticalCollection;

public class SizeRankVerticalSelection extends AbstractSelectionModel {
	
	/**
	 * Default Constructor
	 * @param collection Vertical Collection
	 */
	public SizeRankVerticalSelection(VerticalCollection collection) {
		super(collection);
	}

	@Override
	public List<Entry<String, Double>> executeModel(String queryString) {
		List<Entry<String, Double>> result = new ArrayList<Map.Entry<String,Double>>();
		try
		{
			CSIResultData csiResult = this.searchCSI(queryString);
			BigDecimal totalHits = BigDecimal.valueOf(csiResult.getTotalHits());
			
			for(Map.Entry<String, CSIVertical> data : csiResult.getVerticals().entrySet()) {
				BigDecimal verticalScore = BigDecimal.ZERO;
				BigDecimal sizeFactor = BigDecimal.valueOf(collection.getVerticalData(data.getKey()).getSizeFactor());
				
				for(CSIDocument document : data.getValue()) {
					// RankValue = 1 - (docRank/totalHits) 
					BigDecimal rankValue = BigDecimal.ONE.subtract(BigDecimal.valueOf(document.getRank()).divide(totalHits, 4, RoundingMode.HALF_EVEN));
					verticalScore = verticalScore.add(rankValue.multiply(BigDecimal.valueOf(document.getScore())));
				}
				
				verticalScore = verticalScore.multiply(sizeFactor);
				double logScore = Math.log10(verticalScore.doubleValue());
				
				if(logScore > 0) {
					result.add(new ImmutablePair<String, Double>(data.getKey(), logScore));
				}
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return result;
	}

	@Override
	public String getModelCodeName() {
		return "sizerankmodel";
	}

	@Override
	protected Query prepareQuery(String queryString) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
