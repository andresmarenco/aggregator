package aggregator.verticalselection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;

import aggregator.beans.VerticalCollection;

public class BasicMeanVerticalSelection extends VerticalSelection {
	
	/**
	 * Default Constructor
	 * @param collection Vertical Collection
	 */
	public BasicMeanVerticalSelection(VerticalCollection collection) {
		super(collection);
	}

	@Override
	public List<Entry<String, Double>> executeModel(String queryString) {
		List<Entry<String, Double>> result = new ArrayList<Map.Entry<String,Double>>();
		try
		{
			CSIResultData csiResult = this.searchCSI(queryString);
			for(Map.Entry<String, CSIVertical> data : csiResult.getVerticals().entrySet()) {
				result.add(new ImmutablePair<String, Double>(data.getKey(), data.getValue().getScoresAverage()));
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
		
		return result;
	}

	
}
