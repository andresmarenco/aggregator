package aggregator.sampler.output;

import aggregator.beans.SampledDocument;
import aggregator.beans.Vertical;

public class LuceneSamplerOutput extends AbstractSamplerOutput {

	/**
	 * Default Constructor
	 * @param vertical Vertical Object
	 */
	public LuceneSamplerOutput(Vertical vertical) {
		super(vertical);
	}
	
	@Override
	public void outputDocument(SampledDocument<?> document) {
		
	}

	@Override
	public void openResources() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeResources() {
		// TODO Auto-generated method stub
		
	}

}
