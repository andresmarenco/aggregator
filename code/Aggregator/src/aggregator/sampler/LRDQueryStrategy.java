package aggregator.sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import aggregator.beans.Vertical;
import aggregator.util.SeedGenerator;

/**
 * Learned Resource Description (lrd) query strategy
 * @author andres
 *
 */
public class LRDQueryStrategy implements QueryStrategy {
	
	private Random random;
	private List<String> availableTerms;
	private List<String> usedTerms;
	
	/**
	 * Default Constructor
	 * @param vertical Source vertical
	 */
	public LRDQueryStrategy(Vertical vertical) {
		this.availableTerms = SeedGenerator.getTermsList(vertical);
		this.usedTerms = new ArrayList<String>();
		this.random = new Random();
	}


	@Override
	public void addTerms(List<String> terms) {
		for(String term : terms) {
			if(!StringUtils.isNumeric(term)) {
				if((!availableTerms.contains(term)) && (!usedTerms.contains(term))) {
					availableTerms.add(term);
				}
			}
		}
	}
	

	@Override
	public String getNextTerm() {
		String result = null;
		if(!availableTerms.isEmpty()) {
			result = this.availableTerms.remove(random.nextInt(availableTerms.size()));
			usedTerms.add(result);
		}
		
		return result;
	}


	@Override
	public List<String> getUsedTerms() {
		return this.usedTerms;
	}


	@Override
	public String getCodeName() {
		return "LRD";
	}
}
