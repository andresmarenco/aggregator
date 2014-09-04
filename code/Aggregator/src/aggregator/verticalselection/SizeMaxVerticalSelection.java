package aggregator.verticalselection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.lucene.search.Query;

import aggregator.beans.VerticalCollection;

public class SizeMaxVerticalSelection extends AbstractSelectionModel {
	
	/**
	 * Default Constructor
	 * @param collection Vertical Collection
	 */
	public SizeMaxVerticalSelection(VerticalCollection collection) {
		super(collection);
	}

	@Override
	public List<Entry<String, Double>> executeModel(String queryString) {
		List<Entry<String, Double>> result = new ArrayList<Map.Entry<String,Double>>();
		try
		{
			CSIResultData csiResult = this.searchCSI(queryString);
			for(Map.Entry<String, CSIVertical> data : csiResult.getVerticals().entrySet()) {
				double score = data.getValue().getScoresMax();
				double sizeFactor = collection.getVerticalData(data.getKey()).getSizeFactor();
				
				double logScore = Math.log10(score * sizeFactor);
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
		return "sizemaxmodel";
	}

	@Override
	protected Query prepareQuery(String queryString) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
