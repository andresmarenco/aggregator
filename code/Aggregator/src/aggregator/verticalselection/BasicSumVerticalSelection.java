package aggregator.verticalselection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.lucene.search.Query;

import aggregator.beans.VerticalCollection;

public class BasicSumVerticalSelection extends AbstractSelectionModel {
	
	/**
	 * Default Constructor
	 * @param collection Vertical Collection
	 */
	public BasicSumVerticalSelection(VerticalCollection collection) {
		super(collection);
	}

	@Override
	public List<Entry<String, Double>> executeModel(String queryString) {
		List<Entry<String, Double>> result = new ArrayList<Map.Entry<String,Double>>();
		try
		{
			CSIResultData csiResult = this.searchCSI(queryString);
			for(Map.Entry<String, CSIVertical> data : csiResult.getVerticals().entrySet()) {
				result.add(new ImmutablePair<String, Double>(data.getKey(), data.getValue().getScoresSum()));
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return result;
	}

	@Override
	public String getModelCodeName() {
		return "baselinesum";
	}

	@Override
	protected Query prepareQuery(String queryString) {
		// TODO Auto-generated method stub
		return null;
	}

	
}
