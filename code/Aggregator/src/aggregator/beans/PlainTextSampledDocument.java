package aggregator.beans;

public class PlainTextSampledDocument implements SampledDocument<String> {

	private static final long serialVersionUID = 201407150743L;
	
	private String id;
	private String document;
	
	/**
	 * Default Constructor
	 * @param id Document ID
	 */
	public PlainTextSampledDocument(String id) {
		this.id = id;
	}

	@Override
	public String deserialize(String document) {
		this.setDocument(document);
		return this.document;
	}

	@Override
	public String serialize() {
		return this.document;
	}

	@Override
	public void setDocument(String document) {
		this.document = document;
	}

	@Override
	public String getDocument() {
		return this.document;
	}

	@Override
	public String getId() {
		return this.id;
	}

}
